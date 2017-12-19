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

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.atlas.io.IndentedLineBuffer;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.serializer.MySerializer;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import cz.cvut.kbss.owl2query.UnsupportedQueryException;
import cz.cvut.kbss.owl2query.engine.InternalQuery;
import cz.cvut.kbss.owl2query.engine.QueryAtom;
import cz.cvut.kbss.owl2query.model.OWL2Ontology;
import cz.cvut.kbss.owl2query.model.OWL2Query;
import cz.cvut.kbss.owl2query.model.OWLObjectType;
import cz.cvut.kbss.owl2query.model.Term;
import cz.cvut.kbss.owl2query.model.Variable;
import cz.cvut.kbss.owl2query.parser.QueryWriter;

public class SparqlARQWriter<G> implements QueryWriter<G> {

	private String BASE_NM = "http://pellet.owldl.com/ns/sdle#";

	private Map<String, Variable<G>> undistVars = new HashMap<String, Variable<G>>();

	private OWL2Ontology<G> ont;
	
	@Override
	public void write(OWL2Query<G> query, Writer os, final OWL2Ontology<G> kb) {
		undistVars.clear();
		InternalQuery<G> internalQuery = (InternalQuery<G>) query;

		// Query q = QueryFactory.make();
		Query q = new Query() {
			public void serialize(IndentedLineBuffer buff, Syntax outSyntax) {
				MySerializer.serialize(this, buff, outSyntax);
			}
		};

		if (internalQuery.getResultVars().isEmpty()) {
			q.setQueryAskType();
		} else {
			q.setQuerySelectType();
			for (final Variable<G> v : internalQuery.getResultVars()) {
				q.addResultVar(v.getName());
			}
		}
		ElementGroup eg = new ElementGroup();
		q.setQueryPattern(eg);
		ElementTriplesBlock bp = new ElementTriplesBlock();
		eg.addElement(bp);

		for (final QueryAtom<G> a : internalQuery.getAtoms()) {
			// Node s = null;
			Node p = null;
			// Node o = null;

			switch (a.getPredicate()) {
			case DirectType:
				p = Node.createURI(BASE_NM + "directType");
			case Type:
				if (p == null) {
					p = RDF.type.asNode();
				}

				bp.addTriple(new Triple(individual2Node(
						a.getArguments().get(1), internalQuery), p,
						concept2Node(a.getArguments().get(0), internalQuery)));

				break;

			case PropertyValue:
				bp.addTriple(new Triple(individual2Node(
						a.getArguments().get(1), internalQuery), property2Node(
						a.getArguments().get(0), internalQuery),
						individualOrLiteral2Node(a.getArguments().get(2),
								internalQuery)));

				break;

			case SameAs:
				bp.addTriple(new Triple(individual2Node(
						a.getArguments().get(0), internalQuery), OWL.sameAs
						.asNode(), individual2Node(a.getArguments().get(1),
						internalQuery)));
				break;
			case DifferentFrom:
				bp.addTriple(new Triple(individual2Node(
						a.getArguments().get(0), internalQuery),
						OWL.differentFrom.asNode(), individual2Node(a
								.getArguments().get(1), internalQuery)));
				break;
			case DirectSubClassOf:
				p = Node.createURI(BASE_NM + "directSubClassOf");
			case StrictSubClassOf:
				if (p == null) {
					p = Node.createURI(BASE_NM + "strictSubClassOf");
				}
			case SubClassOf:
				if (p == null) {
					p = RDFS.subClassOf.asNode();
				}

				bp.addTriple(new Triple(concept2Node(a.getArguments().get(0),
						internalQuery), p, concept2Node(
						a.getArguments().get(1), internalQuery)));

				break;
			case EquivalentClass:
				bp.addTriple(new Triple(concept2Node(a.getArguments().get(0),
						internalQuery), OWL.equivalentClass.asNode(),
						concept2Node(a.getArguments().get(1), internalQuery)));

				break;
			case ComplementOf:
				bp.addTriple(new Triple(concept2Node(a.getArguments().get(0),
						internalQuery), OWL.complementOf.asNode(),
						concept2Node(a.getArguments().get(1), internalQuery)));

				break;
			case DisjointWith:
				bp.addTriple(new Triple(concept2Node(a.getArguments().get(0),
						internalQuery), OWL.disjointWith.asNode(),
						concept2Node(a.getArguments().get(1), internalQuery)));

				break;
			case DirectSubPropertyOf:
				p = Node.createURI(BASE_NM + "directSubPropertyOf");
			case StrictSubPropertyOf:
				if (p == null) {
					p = Node.createURI(BASE_NM + "strictSubClassOf");
				}
			case SubPropertyOf:
				if (p == null) {
					p = RDFS.subPropertyOf.asNode();
				}

				bp.addTriple(new Triple(property2Node(a.getArguments().get(0),
						internalQuery), p, property2Node(a.getArguments()
						.get(1), internalQuery)));

				break;
			case EquivalentProperty:
				if (p == null) {
					p = OWL.equivalentProperty.asNode();
				}

				bp.addTriple(new Triple(property2Node(a.getArguments().get(0),
						internalQuery), p, property2Node(a.getArguments()
						.get(1), internalQuery)));

				break;
			case Functional:
				bp.addTriple(new Triple(property2Node(a.getArguments().get(0),
						internalQuery), RDF.type.asNode(),
						OWL.FunctionalProperty.asNode()));
				break;
			case Symmetric:
				bp.addTriple(new Triple(property2Node(a.getArguments().get(0),
						internalQuery), RDF.type.asNode(),
						OWL.SymmetricProperty.asNode()));
				break;
			case Asymmetric:
				bp.addTriple(new Triple(property2Node(a.getArguments().get(0),
						internalQuery), RDF.type.asNode(),
						OWL2.AsymmetricProperty.asNode()));
				break;
			case Reflexive:
				bp.addTriple(new Triple(property2Node(a.getArguments().get(0),
						internalQuery), RDF.type.asNode(),
						OWL2.ReflexiveProperty.asNode()));
				break;
			case Irreflexive:
				bp.addTriple(new Triple(property2Node(a.getArguments().get(0),
						internalQuery), RDF.type.asNode(),
						OWL2.IrreflexiveProperty.asNode()));
				break;
			case Transitive:
				bp.addTriple(new Triple(property2Node(a.getArguments().get(0),
						internalQuery), RDF.type.asNode(),
						OWL.TransitiveProperty.asNode()));
				break;
			case InverseFunctional:
				bp.addTriple(new Triple(property2Node(a.getArguments().get(0),
						internalQuery), RDF.type.asNode(),
						OWL.InverseFunctionalProperty.asNode()));
				break;
			case InverseOf:
				bp.addTriple(new Triple(property2Node(a.getArguments().get(0),
						internalQuery), OWL.inverseOf.asNode(), property2Node(a
						.getArguments().get(1), internalQuery)));
				break;
			case ObjectProperty:
				bp.addTriple(new Triple(property2Node(a.getArguments().get(0),
						internalQuery), RDF.type.asNode(), OWL.ObjectProperty
						.asNode()));
				break;
			case DatatypeProperty:
				bp.addTriple(new Triple(property2Node(a.getArguments().get(0),
						internalQuery), RDF.type.asNode(), OWL.DatatypeProperty
						.asNode()));
				break;
			default:
				throw new UnsupportedOperationException("Not Supported Yet");
			}

		}

		try {
			os.write(q.toString(Syntax.syntaxSPARQL));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Node getVariable(final Variable<G> var, final InternalQuery<G> query) {
		Variable<G> v = undistVars.get(var.getName());
		if (v == null) {
			v = var;
			undistVars.put(var.getName(), var);
		}

		if (query.getUndistVars().contains(v)) {
			return Var.createAnon(new AnonId(v.getName()));
		} else {
			return Var.alloc(var.getName());
		}
	}

	public Node individual2Node(final Term<G> i, final InternalQuery<G> query) {
		if (i.isVariable()) {
			return getVariable(i.asVariable(), query);
		} else {
			try {
				String s = i.asGroundTerm().getWrappedObject().toString();

				URI uri = URI.create(s.substring(1, s.length() - 1));
				return Node.createURI(uri.toString());
			} catch (IllegalArgumentException e) {
				return Node.createLiteral(i.asGroundTerm().getWrappedObject()
						.toString());
			}
		}
	}

	public Node individualOrLiteral2Node(final Term<G> i,
			final InternalQuery<G> query) {
		if (i.isVariable()) {
			return getVariable(i.asVariable(), query);
		} else {
			try {
				String s = i.asGroundTerm().toString();
				String val = s.substring(1, s.length() - 1);

				// System.out.println(s);
				// if (s != null && s.length() > 0 && s.charAt(0) == '"') {//
				// literal

				if (ont.is(i.asGroundTerm().getWrappedObject(),
						OWLObjectType.OWLLiteral)) {// literal
					int ti = s.lastIndexOf("^^");
					String type = ont.getDatatypeOfLiteral(i.asGroundTerm()
							.getWrappedObject());
					String lit = s;
					// if(sx != null){
					// type = sx;
					// }else {
					// type = s.substring(ti + 2);
					// }
					// lit = s.substring(0, ti);
					if (lit.charAt(0) == '"')
						lit = lit.substring(1, lit.length() - 1);
					if (type != null)
						return Node.createLiteral(lit, null, new BaseDatatype(
								type));
					else
						return Node.createLiteral(lit);
				} else {// uri
					URI uri = URI.create(val);
					return Node.createURI(uri.toString());
				}

			} catch (IllegalArgumentException e) {
				return Node.createLiteral(i.asGroundTerm().getWrappedObject()
						.toString());
			}
		}
	}

	public Node concept2Node(final Term<G> c, final InternalQuery<G> query) {
		if (c.isVariable()) {
			return getVariable(c.asVariable(), query);
		} else {
			try {
				String s = c.asGroundTerm().getWrappedObject().toString();
				if (s.charAt(0) == '<') {
					s = s.substring(1, s.length() - 1);
				}
				return Node.createURI(URI.create(s).toString());
			} catch (IllegalArgumentException e) {
				throw new UnsupportedQueryException(
						"Concept/role constructs are not supported: "
								+ c.asGroundTerm().getWrappedObject());
			}
		}
	}
	
	public Node property2Node(final Term<G> p, final InternalQuery<G> query) {
		if (p.isVariable()) {
			return getVariable(p.asVariable(), query);
		} else {
			String uri = p.asGroundTerm().getWrappedObject().toString();
			if (uri.charAt(0) == '<')
				uri = uri.substring(1, uri.length() - 1);
			return Node.createURI(uri);
			// return Node.createURI(p
			// .asGroundTerm()
			// .getWrappedObject()
			// .toString()
			// .substring(
			// 1,
			// p.asGroundTerm().getWrappedObject().toString()
			// .length() - 1));
		}
	}
}