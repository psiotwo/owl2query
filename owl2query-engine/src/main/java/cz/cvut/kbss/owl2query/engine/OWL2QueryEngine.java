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
package cz.cvut.kbss.owl2query.engine;

import cz.cvut.kbss.owl2query.model.*;
import cz.cvut.kbss.owl2query.parser.QueryParseException;
import cz.cvut.kbss.owl2query.parser.arq.SparqlARQParser;
import cz.cvut.kbss.owl2query.util.DisjointSet;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OWL2QueryEngine {
    public static Logger log = Logger
            .getLogger(OWL2QueryEngine.class.getName());

    private static <G> QueryEvaluator<G> getQueryExec() {
        return new CombinedQueryEngine<>();
    }

    public static <G> QueryResult<G> exec(final String sparql,
                                          final OWL2Ontology<G> kb) {
        try {
            return exec(new SparqlARQParser<G>().parse(sparql, kb));
        } catch (final QueryParseException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            return new QueryResultImpl<>(new QueryImpl<>(kb));
        }
    }

    public static <G> QueryResult<G> exec(final OWL2Query<G> qx) {
        InternalQuery<G> query = (InternalQuery<G>) qx;

        query.getOntology().ensureConsistency();

        if (query.getAtoms().isEmpty()) {
            final QueryResultImpl<G> results = new QueryResultImpl<>(query);
            results.add(new ResultBindingImpl<>());
            return results;
        }

        // PREPROCESSING
        if (log.isLoggable(Level.FINE)) {
            log.fine("Preprocessing:\n" + query);
        }
        query = preprocess(query);

        // SIMPLIFICATION
        if (Configuration.SIMPLIFY_QUERY) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("Simplifying:\n" + query);
            }
            simplify(query);
        }

        // SPLITTING
        if (log.isLoggable(Level.FINE)) {
            log.fine("Splitting:\n" + query);
        }

        final List<InternalQuery<G>> queries = split(query);

        // EVALUATE
        if (queries.isEmpty()) {
            throw new InternalReasonerException(
                    "Splitting query returned no results!");
        } else if (queries.size() == 1) {
//			System.out.println(queries.get(0));
            return execSingleQuery(queries.get(0));
        } else {
            final List<QueryResult<G>> results = new ArrayList<>(queries.size());
            for (final InternalQuery<G> q : queries) {
                results.add(execSingleQuery(q));
            }

            return new CarthesianProductResult<>(query.getResultVars(),
                    results);
        }
    }

    private static <G> QueryResult<G> execSingleQuery(InternalQuery<G> query) {
        if (!query.canHaveResults()) {
            return new QueryResultImpl<>(query);
        }

        final QueryEvaluator<G> e = getQueryExec();

        return e.evaluate(query);
    }

    /**
     * If a query has disconnected components such as C(x), D(y) then it should
     * be answered as two separate queries. The answers to each query should be
     * combined at the end by taking Cartesian product. We combine results on a
     * tuple basis as results are iterated. This way we avoid generating the
     * full Cartesian product. Splitting the query ensures the correctness of
     * the answer, e.g. rolling-up technique becomes applicable.
     *
     * @param query Query to be split
     * @return List of queries (contains the initial query if the initial query
     *         is connected)
     */
    public static <G> List<InternalQuery<G>> split(InternalQuery<G> query) {
        try {
            final Set<Variable<G>> resultVars = new HashSet<>(query.getResultVars());

            final DisjointSet<Variable<G>> disjointSet = new DisjointSet<>();

            // partition query according to variables
            for (final QueryAtom<G> atom : query.getAtoms()) {
                Variable<G> toMerge = null;

                for (final Term<G> arg : atom.getArguments()) {
                    for (final Variable<G> var : arg.getVariables()) {
                        disjointSet.add(var);
                        if (toMerge != null) {
                            disjointSet.union(toMerge, var);
                        }
                        toMerge = var;
                    }
                }
            }

            final Collection<Set<Variable<G>>> equivalenceSets = disjointSet.getEquivalenceSets();

            if (equivalenceSets.size() == 1)
                return Collections.singletonList(query);

            final Map<Term<G>, InternalQuery<G>> queries = new HashMap<>();
            InternalQuery<G> groundQuery = null;
            for (final QueryAtom<G> atom : query.getAtoms()) {
                Term<G> representative = null;
                for (final Term<G> arg : atom.getArguments()) {
                    if (arg.isVariable()) {
                        representative = disjointSet.find(arg.asVariable());
                        break;
                    }
                }

                InternalQuery<G> newQuery;
                if (representative == null) {
                    if (groundQuery == null) {
                        groundQuery = new QueryImpl<>(query);
                    }
                    newQuery = groundQuery;
                } else {
                    newQuery = queries.get(representative);
                    if (newQuery == null) {
                        newQuery = new QueryImpl<>(query);
                        queries.put(representative, newQuery);
                    }
                    for (final Term<G> arg : atom.getArguments()) {
                        if (resultVars.contains(arg)) {
                            newQuery.addResultVar(arg.asVariable());
                        }

                        if (query.getDistVars().contains(arg)) {
                            newQuery.addDistVar(arg.asVariable());
                        }
                    }
                }

                newQuery.add(atom);
            }

            final List<InternalQuery<G>> list = new ArrayList<>(queries.values());

            if (groundQuery != null) {
                list.add(0, groundQuery);
            }

            return list;
        } catch (RuntimeException e) {
            log.log(Level.WARNING,
                    "Query split failed, continuing with query execution.", e);
            return Collections.singletonList(query);
        }
    }

    /**
     * Simplifies the query.
     */
    private static <G> void simplify(InternalQuery<G> query) {
        final Map<Variable<G>, Set<G>> allInferredTypes = new HashMap<>();

        final OWL2Ontology<G> kb = query.getOntology();
        for (final Variable<G> var : query.getVars()) {
            final Set<G> inferredTypes = new HashSet<>();

            // domain simplification
            for (final QueryAtom<G> pattern : query.findAtoms(
                    QueryPredicate.PropertyValue, null, var, null)) {
                if (!pattern.getArguments().get(1).isVariable()) {
                    inferredTypes.addAll(kb.getDomains(pattern.getArguments()
                            .get(1).asGroundTerm().getWrappedObject()));
                }
            }

            // range simplification
            for (final QueryAtom<G> pattern : query.findAtoms(
                    QueryPredicate.PropertyValue, null, null, var)) {
                if (!pattern.getArguments().get(1).isVariable()) {
                    inferredTypes.addAll(kb.getRanges(pattern.getArguments()
                            .get(1).asGroundTerm().getWrappedObject()));
                }
            }

            if (!inferredTypes.isEmpty())
                allInferredTypes.put(var, inferredTypes);
        }

        for (final QueryAtom<G> atom : new ArrayList<>(query.getAtoms())) {
            if (atom.getPredicate() == QueryPredicate.Type) {
                final Term<G> clazz = atom.getArguments().get(0);
                if (!clazz.isVariable()) {
                    final G clazzGT = clazz.asGroundTerm().getWrappedObject();
                    final Set<G> inferred = allInferredTypes.get(atom.getArguments().get(1));

                    if ((inferred != null) && !inferred.isEmpty()) {
                        if (inferred.contains(clazz)) {
                            query.remove(atom);
                        } else if (kb.isClassified()) {
                            final Hierarchy<G, ? extends G> h = kb
                                    .getClassHierarchy();

                            final Set<? extends G> subs = h.getSubs(clazzGT,
                                    false);
                            final Set<? extends G> eqs = h.getEquivs(clazz
                                    .asGroundTerm().getWrappedObject());
                            if (!Collections.disjoint(inferred, subs)
                                    || !Collections.disjoint(inferred, eqs))
                                query.remove(atom);
                        }
                    }
                }
            }
        }
    }

    private static <G> InternalQuery<G> preprocess(final InternalQuery<G> query) {
        InternalQuery<G> q = query;

        // SAMEAS
        // get rid of SameAs atoms that contain at least one undistinguished
        // variable.
//		System.out.println("Preprocessing ...");
        for (final QueryAtom<G> atom : q.findAtoms(QueryPredicate.SameAs, null, null)) {
//			System.out.println("> atom=" + atom);

            final Term<G> a1 = atom.getArguments().get(0);
            final Term<G> a2 = atom.getArguments().get(1);

            if (!a1.isVariable() || q.getUndistVars().contains(a1)) {
//				System.out.println(">> SameAs a1 : a1=" + a1 + ", a2=" + a2);
                q = q.apply(Collections.singletonMap(a1, a2));
            } else if (!a2.isVariable() || q.getUndistVars().contains(a2)) {
//				System.out.println(">> SameAs a2 : a1=" + a1 + ", a2=" + a2);
                q = q.apply(Collections.singletonMap(a2, a1));
            }
        }

        // get rid of SameAs with same arguments - in a separate for cycle to
        // find all such atoms.
        for (final QueryAtom<G> atom : q.findAtoms(QueryPredicate.SameAs, null,
                null)) {
            if (atom.getArguments().get(0).equals(atom.getArguments().get(1))) {
                q.remove(atom);
            }
        }

        // DIFFERENTFROM
        // get rid of DifferentFrom atoms where one argument is an
        // undistinguished variable and the other is NOT an undistinguished
        // variable
        for (final QueryAtom<G> atom : q.findAtoms(
                QueryPredicate.DifferentFrom, null, null)) {
            final Term<G> t1 = atom.getArguments().get(0);
            final Term<G> t2 = atom.getArguments().get(1);

            if (q.getUndistVars().contains(t1)
                    && !q.getUndistVars().contains(t2)) {
                q.remove(atom);
                q.Type(t2, t1);
            } else if (q.getUndistVars().contains(t2)
                    && !q.getUndistVars().contains(t1)) {
                q.remove(atom);
                q.Type(t1, t2);
            }
        }

        // Undistinguished variables + CLASS and PROPERTY variables
        // TODO bug : queries Type(_:x,?x) and PropertyValue(_:x, ?x, . ) and
        // PropertyValue(., ?x, _:x) have to be enriched with one more atom
        // evaluating class/property DVs.
        for (final QueryAtom<G> a : new HashSet<>(q.getAtoms())) {
            switch (a.getPredicate()) {
                case Type:
                case DirectType:
                    final Term<G> clazz = a.getArguments().get(0);

                    if (q.getUndistVars().contains(a.getArguments().get(1))
                            && q.getDistVars().contains(clazz)) {
                        q.SubClassOf(clazz, clazz);
                    }
                    break;
                case PropertyValue:
                    final Term<G> property = a.getArguments().get(0);

                    if ((q.getUndistVars().contains(a.getArguments().get(1)) || (q
                            .getUndistVars().contains(a.getArguments().get(2))))
                            && q.getDistVars().contains(property)) {
                        q.SubPropertyOf(property, property);
                    }
                    break;
                default:
                    break;
            }
        }

        return q;
    }

    /**
     * Executes all boolean ABox atoms
     */
    public static <G> boolean execBooleanABoxQuery(final InternalQuery<G> query) {
        // if (!query.getDistVars().isEmpty()) {
        // throw new InternalReasonerException(
        // "Executing execBoolean with nonboolean query : " + query);
        // }

        boolean querySatisfied;

        final OWL2Ontology<G> kb = query.getOntology();
        kb.ensureConsistency();

        // unless proven otherwise all (ground) triples are satisfied
        Boolean allTriplesSatisfied = true;

        for (final QueryAtom<G> atom : query.getAtoms()) {
            // by default we don't know if triple is satisfied
            Boolean tripleSatisfied = null;
            // we can only check ground triples
            if (atom.isGround()) {
                final List<Term<G>> arguments = atom.getArguments();

                switch (atom.getPredicate()) {
                    case Type:
                        tripleSatisfied = kb.isKnownTypeOf(arguments.get(0)
                                .asGroundTerm().getWrappedObject(), arguments
                                .get(1).asGroundTerm().getWrappedObject());
                        break;
                    // case Annotation:
                    case PropertyValue:
                        tripleSatisfied = kb.hasKnownPropertyValue(arguments.get(0)
                                .asGroundTerm().getWrappedObject(), arguments
                                .get(1).asGroundTerm().getWrappedObject(),
                                arguments.get(2).asGroundTerm().getWrappedObject());
                        break;
                    default:
                }
            }

            // if we cannot decide the truth value of this triple (without a
            // consistency
            // check) then over all truth value cannot be true. However, we will
            // continue
            // to see if there is a triple that is obviously false
            if (tripleSatisfied == null)
                allTriplesSatisfied = null;
            else if (!tripleSatisfied) {
                // if one triple is false then the whole query, which is the
                // conjunction of
                // all triples, is false. We can stop now.
                allTriplesSatisfied = false;

                if (log.isLoggable(Level.FINER))
                    log.finer("Failed atom: " + atom);

                break;
            }
        }

        // if we reached a verdict, return it
        if (allTriplesSatisfied != null) {
            querySatisfied = allTriplesSatisfied;
        } else {
            // do the unavoidable consistency check
            if (!query.getConstants().isEmpty()) {
                final GroundTerm<G> testInd = query.getConstants().iterator()
                        .next();
                final G testClass = query.rollUpTo(testInd, Collections.emptySet());

                if (log.isLoggable(Level.FINER))
                    log.finer("Boolean query: " + testInd + " -> " + testClass);

                querySatisfied = kb.isTypeOf(testClass,
                        testInd.getWrappedObject(), false);
            } else {
                final Variable<G> testVar = query.getUndistVars().iterator()
                        .next();
                final G testClass = query.rollUpTo(testVar, Collections.emptySet());

                querySatisfied = kb.isClassAlwaysNonEmpty(testClass);
            }
        }

        return querySatisfied;
    }

    // FIXME contains is not correct due to various object types that can be
    // there.
    public static <G> boolean checkGround(final QueryAtom<G> atom,
                                          final OWL2Ontology<G> kb) {

        final List<Term<G>> arguments = atom.getArguments();

        switch (atom.getPredicate()) {
            case Type:
                return kb.isTypeOf(arguments.get(0).asGroundTerm()
                        .getWrappedObject(), arguments.get(1).asGroundTerm()
                        .asGroundTerm().getWrappedObject(), false);
            case DirectType:
                return kb.getTypes(
                        arguments.get(0).asGroundTerm().getWrappedObject(), true)
                        .contains(
                                arguments.get(1).asGroundTerm().getWrappedObject());
            // case Annotation:
            case PropertyValue:
                return kb.hasPropertyValue(arguments.get(0).asGroundTerm()
                        .getWrappedObject(), arguments.get(1).asGroundTerm()
                        .getWrappedObject(), arguments.get(2).asGroundTerm()
                        .getWrappedObject());
            case SameAs:
                return kb.isSameAs(arguments.get(0).asGroundTerm()
                        .getWrappedObject(), arguments.get(1).asGroundTerm()
                        .getWrappedObject());
            case DifferentFrom:
                return kb.isDifferentFrom(arguments.get(0).asGroundTerm()
                        .getWrappedObject(), arguments.get(1).asGroundTerm()
                        .getWrappedObject());
            case EquivalentClass:
                if (arguments.get(0).asGroundTerm().getWrappedObject()
                        .equals(arguments.get(1).asGroundTerm().getWrappedObject())) {
                    return true;
                }

                return kb
                        .getClassHierarchy()
                        .isEquiv(
                                arguments.get(0).asGroundTerm().getWrappedObject(),
                                arguments.get(1).asGroundTerm().getWrappedObject());
            case SubClassOf:
                if (arguments.get(0).asGroundTerm().getWrappedObject()
                        .equals(arguments.get(1).asGroundTerm().getWrappedObject())) {
                    return true;
                }

                return kb
                        .getClassHierarchy()
                        .isSub(arguments.get(0).asGroundTerm().getWrappedObject(), arguments.get(1).asGroundTerm().getWrappedObject(), false);
            case DirectSubClassOf:
                return kb
                        .getClassHierarchy()
                        .isSub(
                                arguments.get(0).asGroundTerm().getWrappedObject(),
                                arguments.get(1).asGroundTerm().getWrappedObject(),
                                true);
            case StrictSubClassOf:
                return kb
                        .getClassHierarchy()
                        .isSub(
                                arguments.get(0).asGroundTerm().getWrappedObject(),
                                arguments.get(1).asGroundTerm().getWrappedObject(),
                                true)
                        && !kb.getClassHierarchy()
                        .isEquiv(
                                arguments.get(1).asGroundTerm()
                                        .getWrappedObject(),
                                arguments.get(0).asGroundTerm()
                                        .getWrappedObject());
            case DisjointWith:
                return kb.getClassHierarchy().getDisjoints(arguments.get(0).asGroundTerm().getWrappedObject()).contains(arguments
                        .get(1).asGroundTerm().getWrappedObject());
            case ComplementOf:
                return kb.getClassHierarchy().isComplementWith(arguments.get(0).asGroundTerm().getWrappedObject(), arguments
                        .get(1).asGroundTerm().getWrappedObject());
            case EquivalentProperty:
                return kb.getPropertyHierarchy().getEquivs(arguments.get(0).asGroundTerm().getWrappedObject()).contains(
                        arguments.get(1).asGroundTerm().getWrappedObject());
            case SubPropertyOf:
                if (arguments.get(0).asGroundTerm().getWrappedObject()
                        .equals(arguments.get(1).asGroundTerm().getWrappedObject())) {
                    return true;
                }
                return kb
                        .getPropertyHierarchy()
                        .isSub(
                                arguments.get(0).asGroundTerm().getWrappedObject(),
                                arguments.get(1).asGroundTerm().getWrappedObject(),
                                false);
            case DirectSubPropertyOf:
                return kb
                        .getPropertyHierarchy()
                        .isSub(
                                arguments.get(0).asGroundTerm().getWrappedObject(),
                                arguments.get(1).asGroundTerm().getWrappedObject(),
                                true);
            case StrictSubPropertyOf:
                return kb.getPropertyHierarchy()
                        .isSub(
                                arguments.get(0).asGroundTerm().getWrappedObject(),
                                arguments.get(1).asGroundTerm().getWrappedObject(),
                                false)
                        && !kb.getPropertyHierarchy().isEquiv(
                        arguments.get(0).asGroundTerm().getWrappedObject(),
                        arguments.get(1).asGroundTerm().getWrappedObject()
                );
            // case InverseOf:
            // return kb.isInverse(arguments.get(0).asGroundTerm(),
            // arguments.get(
            // 1).asGroundTerm());
            case ObjectProperty:
                return kb.is(arguments.get(0).asGroundTerm().getWrappedObject(),
                        OWLObjectType.OWLObjectProperty);
            case DatatypeProperty:
                return kb.is(arguments.get(0).asGroundTerm().getWrappedObject(),
                        OWLObjectType.OWLDataProperty);
            case Functional:
                return kb.isFunctionalProperty(arguments.get(0).asGroundTerm().getWrappedObject());
            case InverseFunctional:
                return kb.isInverseFunctionalProperty(arguments.get(0).asGroundTerm().getWrappedObject());
            case Symmetric:
                return kb.isSymmetricProperty(arguments.get(0).asGroundTerm().getWrappedObject());
            case Asymmetric:
                return kb.isAsymmetricProperty(arguments.get(0).asGroundTerm().getWrappedObject());
            case Transitive:
                return kb.isTransitiveProperty(arguments.get(0).asGroundTerm().getWrappedObject());
            case Not:
//			System.out.println("CHECKING "
//					+ ((NotQueryAtom<G>) atom).getQuery().getAtoms());
                for (final QueryAtom<G> a : ((NotQueryAtom<G>) atom).getQuery()
                        .getAtoms()) {
//				System.out.println("Checking " + a);
                    if (checkGround(a, kb)) {
//					System.out.println("    --> " + false);
                        return false;
                    }
                }
                return true;
            case Core:
                return execBooleanABoxQuery(((Core<G>) atom).getQuery());
            default:
                throw new IllegalArgumentException("Unknown atom type : "
                        + atom.getPredicate());
        }
    }
}
