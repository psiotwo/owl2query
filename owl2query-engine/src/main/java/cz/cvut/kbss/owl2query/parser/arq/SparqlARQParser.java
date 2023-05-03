/*******************************************************************************
 * Copyright (C) 2011 Czech Technical University in Prague                                                                                                                                                        
 *
 * This program is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any 
 * later version. 
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
 * details. You should have received a copy of the GNU General Public License 
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package cz.cvut.kbss.owl2query.parser.arq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import cz.cvut.kbss.owl2query.engine.ResultBindingImpl;
import cz.cvut.kbss.owl2query.model.*;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementNotExists;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;

import cz.cvut.kbss.owl2query.UnsupportedQueryException;
import cz.cvut.kbss.owl2query.parser.QueryParseException;
import cz.cvut.kbss.owl2query.parser.QueryParser;

public class SparqlARQParser<G> implements QueryParser<G> {

    public OWL2Query<G> parse(final InputStream stream, final OWL2Ontology<G> kb)
            throws QueryParseException {
        return parse(parse(stream), kb);
    }

    public OWL2Query<G> parse(final String queryStr, final OWL2Ontology<G> o)
            throws QueryParseException {
        return parseAskOrSelect(QueryFactory.create(queryStr, Syntax.syntaxSPARQL_11), o);
    }

    public OWL2Rule<G> parseConstruct(InputStream stream, OWL2Ontology<G> o)
            throws QueryParseException {
        return parseConstruct(parse(stream), o);
    }

    public OWL2Rule<G> parseConstruct(String queryStr, OWL2Ontology<G> o)
            throws QueryParseException {
        return parseConstruct(QueryFactory.create(queryStr, Syntax.syntaxSPARQL_11), o);
    }

    private String parse(final InputStream stream) {
        try (final BufferedReader r = new BufferedReader(new InputStreamReader(
                stream))) {
            String line;
            final StringBuilder queryString = new StringBuilder();
            while ((line = r.readLine()) != null) {
                queryString.append(line).append("\n");
            }
            return queryString.toString();
        } catch (final IOException e) {
            throw new QueryParseException(
                    "Error creating a reader from the input stream.", e);
        }
    }

    private OWL2Query<G> parseAskOrSelect(final org.apache.jena.query.Query sparqlQuery,
                                          final OWL2Ontology<G> ontology) throws QueryParseException {

        if (sparqlQuery.isDescribeType())
            throw new IllegalArgumentException("DESCRIBE queries are not supported.");

        // MEANWHILE, ONLY ONE QUERY GRAPH - named graphs, optionals, filters,
        // etc. to be added later.
        final Element pattern = sparqlQuery.getQueryPattern();

        if (!(pattern instanceof ElementGroup))
            throw new UnsupportedQueryException("ElementGroup was expected, but found '" + pattern.getClass() + "'.");

        OWL2Query<G> query = ontology.getFactory().createQuery(ontology).distinct(
                sparqlQuery.isDistinct());

        // very important to call this function so that getResultVars() will
        // work fine for SELECT * queries
        sparqlQuery.setResultVars();
        setLimitAndOffset(sparqlQuery, query);
        final SPARQLDLNotQueryPatternARQParser<G> parser = parseQuery(ontology, pattern, query, sparqlQuery.getResultVars());
        setValues(ontology, sparqlQuery, query, parser);

        return query;
    }

    private void setLimitAndOffset(org.apache.jena.query.Query sparqlQuery, OWL2Query<G> owl2Query) {
        if (sparqlQuery.hasLimit()) {
            owl2Query.setLimit((int) sparqlQuery.getLimit());
        }
        if (sparqlQuery.hasOffset()) {
            owl2Query.setOffset((int) sparqlQuery.getOffset());
        }
    }

    private void setValues(OWL2Ontology<G> ontology, org.apache.jena.query.Query sparqlQuery, OWL2Query<G> owl2Query, final SPARQLDLNotQueryPatternARQParser<G> parser) {
        if (sparqlQuery.hasValues()) {
            final List<ResultBinding<G>> bindings = new ArrayList<>();
            sparqlQuery.getValuesData().forEach(b -> {
                final Iterator<Var> it = b.vars();
                final ResultBinding<G> map = new ResultBindingImpl<>();
                while (it.hasNext()) {
                    final Var v = it.next();
                    map.put(ontology.getFactory().variable(v.getVarName()), parser.node2term(b.get(v), null).asGroundTerm());
                }
                bindings.add(map);
            });
            owl2Query.setValues(bindings);
        }
    }

    private OWL2Rule<G> parseConstruct(final org.apache.jena.query.Query sparqlQuery,
                                       final OWL2Ontology<G> ontology) throws QueryParseException {
        if (!sparqlQuery.isConstructType())
            throw new UnsupportedQueryException(
                    "This parser supports only SPARQL CONSTRUCT queries.");

        // MEANWHILE, ONLY ONE QUERY GRAPH - named graphs, optionals, filters,
        // etc. to be added later.
        final Element pattern = sparqlQuery.getQueryPattern();
        if (!(pattern instanceof ElementGroup))
            throw new UnsupportedQueryException(
                    "ElementGroup was expected, but found '"
                            + pattern.getClass() + "'.");
        sparqlQuery.setResultVars();

        final OWL2Query<G> body = ontology.getFactory().createQuery(ontology);
        parseQuery(ontology, pattern, body, sparqlQuery.getResultVars());

        final OWL2Query<G> head = ontology.getFactory().createQuery(ontology);
        Template t = sparqlQuery.getConstructTemplate();
        parseQuery(ontology, t, head, sparqlQuery.getResultVars());

        return new OWL2RuleImpl<>(head, body);
    }

    public void parseQuery(final OWL2Ontology<G> o, final Template t, OWL2Query<G> current, List<String> allDistVars) {
        final SPARQLDLNotQueryPatternARQParser<G> parser = new SPARQLDLNotQueryPatternARQParser<>(o);
        final List<Triple> triples = t.getTriples();
        for (final String i : allDistVars) {
            current.addResultVar(o.getFactory().variable(i));
        }
        parser.parse(triples, current);
    }

    private SPARQLDLNotQueryPatternARQParser<G> parseQuery(final OWL2Ontology<G> o, final Element e, OWL2Query<G> current,
                                                           List<String> allDistVars) {
        final SPARQLDLNotQueryPatternARQParser<G> parser = new SPARQLDLNotQueryPatternARQParser<>(o);
        _parseQuery(o, e, current, allDistVars, parser);
        return parser;
    }

    private void _parseQuery(final OWL2Ontology<G> o, final Element e, OWL2Query<G> current,
                             List<String> allDistVars, SPARQLDLNotQueryPatternARQParser<G> parser) {
        if (e instanceof ElementTriplesBlock) {
            for (final String i : allDistVars) {
                current.addResultVar(o.getFactory().variable(i));
            }
            parser.parse(((ElementTriplesBlock) e).getPattern().getList(), current);
        } else if (e instanceof ElementPathBlock) {
            for (final String i : allDistVars) {
                current.addResultVar(o.getFactory().variable(i));
            }
            parser.parseTriplePath(((ElementPathBlock) e).getPattern().getList(), current);
        } else if (e instanceof ElementNotExists) {
            final OWL2Query<G> query2 = o.getFactory().createQuery(o).distinct(true);
            List<String> vars = new ArrayList<>();

            for (Var v : PatternVars.vars(((ElementNotExists) e).getElement())) {
                if (v.isBlankNodeVar()) {
                    continue;
                }
                query2.addResultVar(o.getFactory().variable(v.getVarName()));
            }
            parseQuery(o, ((ElementNotExists) e).getElement(), query2, vars);
            current.Not(query2);
        } else if (e instanceof ElementGroup) {
            for (final Element el : ((ElementGroup) e).getElements()) {
                _parseQuery(o, el, current, allDistVars, parser);
            }
        } else if (e instanceof ElementBind) {
            current.external(e);
        } else {
            throw new UnsupportedQueryException(
                    "Complex query patterns are not supported yet." + e);
        }
    }
}