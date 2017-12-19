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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.cvut.kbss.owl2query.model.InternalReasonerException;
import cz.cvut.kbss.owl2query.model.KBOperation;
import cz.cvut.kbss.owl2query.model.OWL2Ontology;
import cz.cvut.kbss.owl2query.model.OWLObjectType;
import cz.cvut.kbss.owl2query.model.SizeEstimate;
import cz.cvut.kbss.owl2query.model.Term;

class QueryCost<G> {
    private double staticCost;

    private double branchCount;

    private OWL2Ontology<G> kb;

    private SizeEstimate<G> estimate;

    public QueryCost(OWL2Ontology<G> kb) {
        this.kb = kb;
        this.estimate = kb.getSizeEstimate();
    }

    public double estimate(final QueryAtom<G> atom) {
        return estimate(atom, new HashSet<Term<G>>());
    }

    /**
     * Estimates the cost of evaluation a query atom w.r.t variables that are
     * bound at that point The atom cost has no meaning in its absolute value,
     * but comparison of these numbers should advise, how beneficial is it to
     * evaluate one atom instead of another one.
     *
     * @param atom
     * @param bound
     * @return
     */
    public double estimate(final QueryAtom<G> atom,
                           final Collection<Term<G>> bound) {
        boolean direct = false;
        boolean strict = false;

        final List<Term<G>> arguments = atom.getArguments();
        for (Term<G> a : arguments) {
            if (a.isGround()) {
                bound.add(a);
            }
        }

        switch (atom.getPredicate()) {
            case DirectType:
                direct = true;
            case Type:
                Term<G> clazz = arguments.get(0);
                Term<G> instance = arguments.get(1);

                if (bound.containsAll(arguments)) {
                    staticCost = direct ? estimate
                            .getCost(KBOperation.IS_DIRECT_TYPE) : estimate
                            .getCost(KBOperation.IS_TYPE);
                    branchCount = 1;
                } else if (bound.contains(clazz)) {
                    staticCost = direct ? estimate
                            .getCost(KBOperation.GET_DIRECT_INSTANCES) : estimate
                            .getCost(KBOperation.GET_INSTANCES);
                    branchCount = (clazz.isGround()) ? estimate.size(clazz
                            .asGroundTerm().getWrappedObject()) : estimate
                            .avgInstancesPerClass(direct);
                } else if (bound.contains(instance)) {
                    staticCost = estimate.getCost(KBOperation.GET_TYPES);
                    branchCount = (instance.isGround()) ? estimate
                            .classesPerInstance(instance.asGroundTerm()
                                    .getWrappedObject(), direct) : estimate
                            .avgClassesPerInstance(direct);
                } else {
                    staticCost = estimate.getClassCount()
                            * (direct ? estimate
                            .getCost(KBOperation.GET_DIRECT_INSTANCES)
                            : estimate.getCost(KBOperation.GET_INSTANCES));
                    branchCount = estimate.getClassCount()
                            * estimate.avgInstancesPerClass(direct);
                }
                break;

            // case Annotation: // TODO
            case PropertyValue:
                if (bound.containsAll(arguments)) {
                    staticCost = estimate.getCost(KBOperation.HAS_PROPERTY_VALUE);
                    branchCount = 1;
                } else {
                    Term<G> predicate = arguments.get(0);
                    Term<G> subject = arguments.get(1);
                    Term<G> object = arguments.get(2);

                    if (bound.contains(predicate)) {
                        if (bound.contains(subject)) {
                            staticCost = estimate
                                    .getCost(KBOperation.GET_PROPERTY_VALUE);
                            branchCount = (predicate.isGround()) ? estimate
                                    .avg(predicate.asGroundTerm()
                                            .getWrappedObject()) : estimate
                                    .avgSubjectsPerProperty();
                        } else if (bound.contains(object)) {
                            staticCost = estimate
                                    .getCost(KBOperation.GET_PROPERTY_VALUE);
                            if (predicate.isGround()) {
                                if (kb.is(predicate.asGroundTerm()
                                        .getWrappedObject(),
                                        OWLObjectType.OWLObjectProperty)) {
                                    branchCount = estimate.avg(inv(predicate
                                            .asGroundTerm().getWrappedObject()));
                                } else {
                                    branchCount = estimate.avgSubjectsPerProperty();
                                }
                            } else {
                                branchCount = estimate.avgSubjectsPerProperty();
                            }
                        } else {
                            staticCost = estimate
                                    .getCost(KBOperation.GET_PROPERTY_VALUE)
                                    /*
                                          * TODO should be st. like
                                          * GET_INSTANCES_OF_ROLLED_CONCEPT that reflects
                                          * the complexity of the concept.
                                          */
                                    + ((predicate.isGround()) ? estimate
                                    .avg(predicate.asGroundTerm()
                                            .getWrappedObject()) : estimate
                                    .avgSubjectsPerProperty())
                                    * estimate
                                    .getCost(KBOperation.GET_PROPERTY_VALUE);
                            branchCount = ((predicate.isGround()) ? estimate
                                    .size(predicate.asGroundTerm()
                                            .getWrappedObject()) : estimate
                                    .avgPairsPerProperty());
                        }
                    } else if (bound.contains(subject) || bound.contains(object)) {
                        staticCost = getPropertyCount()
                                * estimate.getCost(KBOperation.GET_PROPERTY_VALUE);
                        branchCount = getPropertyCount()
                                * estimate.avgSubjectsPerProperty();
                    } else {
                        staticCost = getPropertyCount()
                                * (estimate.getCost(KBOperation.GET_PROPERTY_VALUE)
                                /*
                                     * TODO should be st. like
                                     * GET_INSTANCES_OF_ROLLED_CONCEPT that reflects the
                                     * complexity of the concept.
                                     */ + estimate.avgSubjectsPerProperty()
                                * estimate
                                .getCost(KBOperation.GET_PROPERTY_VALUE));
                        branchCount = estimate.avgPairsPerProperty()
                                * getPropertyCount();
                    }
                }
                break;

            case SameAs:
                Term<G> saLHS = arguments.get(0);
                Term<G> saRHS = arguments.get(1);

                if (bound.containsAll(arguments)) {
                    staticCost = estimate.getCost(KBOperation.IS_SAME_AS);
                    branchCount = 1;
                } else if (bound.contains(saLHS) || bound.contains(saRHS)) {
                    staticCost = estimate.getCost(KBOperation.GET_SAMES);

                    if (bound.contains(saLHS)) {
                        branchCount = (saLHS.isGround()) ? estimate.sames(saLHS
                                .asGroundTerm().getWrappedObject()) : estimate
                                .avgSamesPerInstance();
                    } else {
                        branchCount = (saRHS.isGround()) ? estimate.sames(saRHS
                                .asGroundTerm().getWrappedObject()) : estimate
                                .avgSamesPerInstance();
                    }
                } else {
                    staticCost = estimate.getInstanceCount()
                            * estimate.getCost(KBOperation.GET_SAMES);
                    branchCount = estimate.getInstanceCount()
                            * estimate.avgSamesPerInstance();
                }
                break;
            case DifferentFrom:
                Term<G> dfLHS = arguments.get(0);
                Term<G> dfRHS = arguments.get(1);

                if (bound.containsAll(arguments)) {
                    staticCost = estimate.getCost(KBOperation.IS_DIFFERENT_FROM);
                    branchCount = 1;
                } else if (bound.contains(dfLHS) || bound.contains(dfRHS)) {
                    staticCost = estimate.getCost(KBOperation.GET_DIFFERENTS);

                    if (bound.contains(dfLHS)) {
                        branchCount = (dfLHS.isGround()) ? estimate
                                .differents(dfLHS.asGroundTerm().getWrappedObject())
                                : estimate.avgDifferentsPerInstance();
                    } else {
                        branchCount = (dfRHS.isGround()) ? estimate
                                .differents(dfRHS.asGroundTerm().getWrappedObject())
                                : estimate.avgDifferentsPerInstance();
                    }
                } else {
                    staticCost = estimate.getInstanceCount()
                            * estimate.getCost(KBOperation.GET_DIFFERENTS);
                    branchCount = estimate.getInstanceCount()
                            * estimate.avgDifferentsPerInstance();
                }
                break;

            case DirectSubClassOf:
                direct = true;
            case StrictSubClassOf:
                strict = true;
            case SubClassOf:
                Term<G> clazzLHS = arguments.get(0);
                Term<G> clazzRHS = arguments.get(1);

                if (bound.containsAll(arguments)) {
                    if (strict) {
                        if (direct) {
                            staticCost = estimate
                                    .getCost(KBOperation.GET_DIRECT_SUB_OR_SUPERCLASSES);
                        } else {
                            staticCost = estimate
                                    .getCost(KBOperation.IS_SUBCLASS_OF)
                                    + estimate
                                    .getCost(KBOperation.GET_EQUIVALENT_CLASSES);
                        }
                    } else {
                        staticCost = estimate.getCost(KBOperation.IS_SUBCLASS_OF);
                    }

                    branchCount = 1;
                } else if (bound.contains(clazzLHS) || bound.contains(clazzRHS)) {
                    if (strict && !direct) {
                        staticCost = estimate
                                .getCost(KBOperation.GET_SUB_OR_SUPERCLASSES)
                                + estimate
                                .getCost(KBOperation.GET_EQUIVALENT_CLASSES);
                    } else {
                        staticCost = direct ? estimate
                                .getCost(KBOperation.GET_DIRECT_SUB_OR_SUPERCLASSES)
                                : estimate
                                .getCost(KBOperation.GET_SUB_OR_SUPERCLASSES);
                    }
                    if (bound.contains(clazzLHS)) {
                        branchCount = (clazzLHS.isGround()) ? estimate
                                .superClasses(clazzLHS.asGroundTerm()
                                        .getWrappedObject(), direct) : estimate
                                .avgSuperClasses(direct);

                        if (strict) {
                            branchCount -= (clazzLHS.isGround()) ? estimate
                                    .equivClasses(clazzLHS.asGroundTerm()
                                            .getWrappedObject()) : estimate
                                    .avgEquivClasses();
                            branchCount = Math.max(branchCount, 0);
                        }
                    } else {
                        branchCount = (clazzRHS.isGround()) ? estimate
                                .superClasses(clazzRHS.asGroundTerm()
                                        .getWrappedObject(), direct) : estimate
                                .avgSuperClasses(direct);

                        if (strict) {
                            branchCount -= (clazzRHS.isGround()) ? estimate
                                    .equivClasses(clazzRHS.asGroundTerm()
                                            .getWrappedObject()) : estimate
                                    .avgEquivClasses();
                            branchCount = Math.max(branchCount, 0);
                        }
                    }
                } else {
                    if (strict && !direct) {
                        staticCost = estimate
                                .getCost(KBOperation.GET_SUB_OR_SUPERCLASSES)
                                + estimate
                                .getCost(KBOperation.GET_EQUIVALENT_CLASSES);
                    } else {
                        staticCost = direct ? estimate
                                .getCost(KBOperation.GET_DIRECT_SUB_OR_SUPERCLASSES)
                                : estimate
                                .getCost(KBOperation.GET_SUB_OR_SUPERCLASSES);
                    }

                    staticCost *= estimate.getClassCount();

                    branchCount = estimate.getClassCount()
                            * estimate.avgSubClasses(direct);

                    if (strict) {
                        branchCount -= estimate.avgEquivClasses();
                        branchCount = Math.max(branchCount, 0);
                    }
                }
                break;
            case EquivalentClass:
                Term<G> eqcLHS = arguments.get(0);
                Term<G> eqcRHS = arguments.get(1);

                if (bound.containsAll(arguments)) {
                    staticCost = estimate.getCost(KBOperation.IS_EQUIVALENT_CLASS);
                    branchCount = 1;
                } else if (bound.contains(eqcLHS) || bound.contains(eqcRHS)) {
                    staticCost = estimate
                            .getCost(KBOperation.GET_EQUIVALENT_CLASSES);

                    if (bound.contains(eqcLHS)) {
                        branchCount = (eqcLHS.isGround()) ? estimate
                                .equivClasses(eqcLHS.asGroundTerm()
                                        .getWrappedObject()) : estimate
                                .avgEquivClasses();
                    } else {
                        branchCount = (eqcRHS.isGround()) ? estimate
                                .equivClasses(eqcRHS.asGroundTerm()
                                        .getWrappedObject()) : estimate
                                .avgEquivClasses();
                    }
                } else {
                    staticCost = estimate.getClassCount()
                            * estimate.getCost(KBOperation.GET_EQUIVALENT_CLASSES);
                    branchCount = estimate.getClassCount()
                            * estimate.avgEquivClasses();
                }
                break;
            case DisjointWith:
                Term<G> dwLHS = arguments.get(0);
                Term<G> dwRHS = arguments.get(1);

                if (bound.containsAll(arguments)) {
                    staticCost = estimate
                            .getCost(KBOperation.IS_DISJOINTCLASS_WITH);
                    branchCount = 1;
                } else if (bound.contains(dwLHS) || bound.contains(dwRHS)) {
                    staticCost = estimate.getCost(KBOperation.GET_DISJOINT_CLASSES);

                    if (bound.contains(dwLHS)) {
                        branchCount = (dwLHS.isGround() ? estimate
                                .disjointClasses(dwLHS.asGroundTerm()
                                        .getWrappedObject()) : estimate
                                .avgDisjointClasses());
                    } else {
                        branchCount = (dwRHS.isGround() ? estimate
                                .disjointClasses(dwRHS.asGroundTerm()
                                        .getWrappedObject()) : estimate
                                .avgDisjointClasses());
                    }
                } else {
                    staticCost = estimate.getClassCount()
                            * estimate.getCost(KBOperation.GET_DISJOINT_CLASSES);
                    branchCount = estimate.getClassCount()
                            * estimate.avgDisjointClasses();
                }
                break;
            case ComplementOf:
                Term<G> coLHS = arguments.get(0);
                Term<G> coRHS = arguments.get(1);

                if (bound.containsAll(arguments)) {
                    staticCost = estimate
                            .getCost(KBOperation.IS_COMPLEMENTCLASS_OF);
                    branchCount = 1;
                } else if (bound.contains(coLHS) || bound.contains(coRHS)) {
                    staticCost = estimate
                            .getCost(KBOperation.GET_COMPLEMENT_CLASSES);

                    if (bound.contains(coLHS)) {
                        branchCount = (!coLHS.isVariable()) ? estimate
                                .complements(coLHS.asGroundTerm()
                                        .getWrappedObject()) : estimate
                                .avgComplementClasses();
                    } else {
                        branchCount = (!coRHS.isVariable()) ? estimate
                                .complements(coRHS.asGroundTerm()
                                        .getWrappedObject()) : estimate
                                .avgComplementClasses();
                    }
                } else {
                    staticCost = estimate.getClassCount()
                            * estimate.getCost(KBOperation.GET_COMPLEMENT_CLASSES);
                    branchCount = estimate.getClassCount()
                            * estimate.avgComplementClasses();
                }
                break;

            case DirectSubPropertyOf:
                direct = true;
            case StrictSubPropertyOf:
                strict = true;
            case SubPropertyOf:
                Term<G> spLHS = arguments.get(0);
                Term<G> spRHS = arguments.get(1);

                if (bound.containsAll(arguments)) {
                    if (strict) {
                        if (direct) {
                            staticCost = estimate
                                    .getCost(KBOperation.GET_DIRECT_SUB_OR_SUPERPROPERTIES);
                        } else {
                            staticCost = estimate
                                    .getCost(KBOperation.IS_SUBPROPERTY_OF)
                                    + estimate
                                    .getCost(KBOperation.GET_EQUIVALENT_PROPERTIES);
                        }
                    } else {
                        staticCost = estimate
                                .getCost(KBOperation.IS_SUBPROPERTY_OF);
                    }

                    branchCount = 1;
                } else if (bound.contains(spLHS) || bound.contains(spRHS)) {
                    if (strict && !direct) {
                        staticCost = estimate
                                .getCost(KBOperation.GET_SUB_OR_SUPERPROPERTIES)
                                + estimate
                                .getCost(KBOperation.GET_EQUIVALENT_PROPERTIES);
                    } else {
                        staticCost = direct ? estimate
                                .getCost(KBOperation.GET_DIRECT_SUB_OR_SUPERPROPERTIES)
                                : estimate
                                .getCost(KBOperation.GET_SUB_OR_SUPERPROPERTIES);
                    }
                    if (bound.contains(spLHS)) {
                        branchCount = (spLHS.isGround()) ? estimate
                                .superProperties(spLHS.asGroundTerm()
                                        .getWrappedObject(), direct) : estimate
                                .avgSuperProperties(direct);

                        if (strict) {
                            branchCount -= (spLHS.isGround()) ? estimate
                                    .equivProperties(spLHS.asGroundTerm()
                                            .getWrappedObject()) : estimate
                                    .avgEquivProperties();
                            branchCount = Math.max(branchCount, 0);

                        }
                    } else {
                        branchCount = (spRHS.isGround()) ? estimate
                                .superProperties(spRHS.asGroundTerm()
                                        .getWrappedObject(), direct) : estimate
                                .avgSuperProperties(direct);

                        if (strict) {
                            branchCount -= (spRHS.isGround()) ? estimate
                                    .equivProperties(spRHS.asGroundTerm()
                                            .getWrappedObject()) : estimate
                                    .avgEquivProperties();
                            branchCount = Math.max(branchCount, 0);

                        }
                    }
                } else {
                    if (strict && !direct) {
                        staticCost = estimate
                                .getCost(KBOperation.GET_SUB_OR_SUPERPROPERTIES)
                                + estimate
                                .getCost(KBOperation.GET_EQUIVALENT_PROPERTIES);
                    } else {
                        staticCost = direct ? estimate
                                .getCost(KBOperation.GET_DIRECT_SUB_OR_SUPERPROPERTIES)
                                : estimate
                                .getCost(KBOperation.GET_SUB_OR_SUPERPROPERTIES);
                    }

                    staticCost *= estimate.getObjectPropertyCount()
                            + estimate.getDataPropertyCount();

                    branchCount = (estimate.getObjectPropertyCount() + estimate
                            .getDataPropertyCount())
                            * estimate.avgSubProperties(direct);

                    if (strict) {
                        branchCount -= estimate.avgEquivProperties();
                        branchCount = Math.max(branchCount, 0);

                    }
                }
                break;

            case EquivalentProperty:
                Term<G> eqpLHS = arguments.get(0);
                Term<G> eqpRHS = arguments.get(1);

                if (bound.containsAll(arguments)) {
                    staticCost = estimate
                            .getCost(KBOperation.IS_EQUIVALENT_PROPERTY);
                    branchCount = 1;
                } else if (bound.contains(eqpLHS) || bound.contains(eqpRHS)) {
                    staticCost = estimate
                            .getCost(KBOperation.GET_EQUIVALENT_PROPERTIES);

                    if (bound.contains(eqpLHS)) {
                        branchCount = (!eqpLHS.isVariable()) ? estimate
                                .equivProperties(eqpLHS.asGroundTerm()
                                        .getWrappedObject()) : estimate
                                .avgEquivProperties();
                    } else {
                        branchCount = (!eqpRHS.isVariable()) ? estimate
                                .equivProperties(eqpRHS.asGroundTerm()
                                        .getWrappedObject()) : estimate
                                .avgEquivProperties();
                    }
                } else {
                    staticCost = (estimate.getObjectPropertyCount() + estimate
                            .getDataPropertyCount())
                            * estimate
                            .getCost(KBOperation.GET_EQUIVALENT_PROPERTIES);
                    branchCount = (estimate.getObjectPropertyCount() + estimate
                            .getDataPropertyCount())
                            * estimate.avgEquivProperties();
                }
                break;
            case InverseOf:
                Term<G> ioLHS = arguments.get(0);
                Term<G> ioRHS = arguments.get(1);

                if (bound.containsAll(arguments)) {
                    staticCost = estimate.getCost(KBOperation.IS_INVERSE_OF);
                    branchCount = 1;
                } else if (bound.contains(ioLHS) || bound.contains(ioRHS)) {
                    staticCost = estimate.getCost(KBOperation.GET_INVERSES);

                    if (bound.contains(ioLHS)) {
                        branchCount = (!ioLHS.isVariable()) ? estimate
                                .inverses(ioLHS.asGroundTerm().getWrappedObject())
                                : estimate.avgInverseProperties();
                    } else {
                        branchCount = (!ioRHS.isVariable()) ? estimate
                                .inverses(ioRHS.asGroundTerm().getWrappedObject())
                                : estimate.avgInverseProperties();
                    }
                } else {
                    staticCost = estimate.getObjectPropertyCount()
                            * estimate.getCost(KBOperation.GET_INVERSES);
                    branchCount = estimate.getObjectPropertyCount()
                            * estimate.avgInverseProperties();
                }
                break;
            case ObjectProperty:
                if (bound.containsAll(arguments)) {
                    staticCost = 0;// estimate.getCost(KBOperation.IS_OBJECT_PROPERTY);
                    branchCount = 1;
                } else {
                    staticCost = 0;// estimate.getCost(KBOperation.GET_OBJECT_PROPERTIES);
                    branchCount = estimate.getObjectPropertyCount();
                }
                break;
            case DatatypeProperty:
                if (bound.containsAll(arguments)) {
                    staticCost = 0;// estimate.getCost(KBOperation.IS_DATATYPE_PROPERTY);
                    branchCount = 1;
                } else {
                    staticCost = 0;
                    // estimate
                    // .getCost(KBOperation.GET_DATATYPE_PROPERTIES);
                    branchCount = estimate.getDataPropertyCount();
                }
                break;
            case Functional:
                if (bound.containsAll(arguments)) {
                    staticCost = estimate
                            .getCost(KBOperation.IS_FUNCTIONAL_PROPERTY);
                    branchCount = 1;
                } else {
                    staticCost = estimate
                            .getCost(KBOperation.GET_FUNCTIONAL_PROPERTIES);
                    branchCount = estimate.getFunctionalPropertyCount();
                }
                break;
            case InverseFunctional:
                if (bound.containsAll(arguments)) {
                    staticCost = estimate
                            .getCost(KBOperation.IS_INVERSE_FUNCTIONAL_PROPERTY);
                    branchCount = 1;
                } else {
                    staticCost = estimate
                            .getCost(KBOperation.GET_INVERSE_FUNCTIONAL_PROPERTIES);
                    branchCount = estimate.getInverseFunctionalPropertyCount();
                }
                break;
            case Transitive:
                if (bound.containsAll(arguments)) {
                    staticCost = estimate
                            .getCost(KBOperation.IS_TRANSITIVE_PROPERTY);
                    branchCount = 1;
                } else {
                    staticCost = estimate
                            .getCost(KBOperation.GET_TRANSITIVE_PROPERTIES);
                    branchCount = estimate.getTransitivePropertyCount();
                }
                break;
            case Symmetric:
                if (bound.containsAll(arguments)) {
                    staticCost = estimate
                            .getCost(KBOperation.IS_SYMMETRIC_PROPERTY);
                    branchCount = 1;
                } else {
                    staticCost = estimate
                            .getCost(KBOperation.GET_SYMMETRIC_PROPERTIES);
                    branchCount = estimate.getSymmetricPropertyCount();
                }
                break;
            case Asymmetric:
                if (bound.containsAll(arguments)) {
                    staticCost = estimate
                            .getCost(KBOperation.IS_ASYMMETRIC_PROPERTY);
                    branchCount = 1;
                } else {
                    staticCost = estimate
                            .getCost(KBOperation.GET_ASYMMETRIC_PROPERTIES);
                    branchCount = estimate.getAsymmetricPropertyCount();
                }
                break;
            case Reflexive:
                if (bound.containsAll(arguments)) {
                    staticCost = estimate
                            .getCost(KBOperation.IS_REFLEXIVE_PROPERTY);
                    branchCount = 1;
                } else {
                    staticCost = estimate
                            .getCost(KBOperation.GET_REFLEXIVE_PROPERTIES);
                    branchCount = estimate.getReflexivePropertyCount();
                }
                break;
            case Irreflexive:
                if (bound.containsAll(arguments)) {
                    staticCost = estimate
                            .getCost(KBOperation.IS_IRREFLEXIVE_PROPERTY);
                    branchCount = 1;
                } else {
                    staticCost = estimate
                            .getCost(KBOperation.GET_IRREFLEXIVE_PROPERTIES);
                    branchCount = estimate.getIrreflexivePropertyCount();
                }
                break;
            case Not:
                if (!bound.containsAll(arguments)) {
                    // estimate(((NotQueryAtom) atom).getQuery(), bound);
                    staticCost = arguments.size() * estimate.getInstanceCount();
                } else {
                    staticCost = estimate.getCost(KBOperation.IS_TYPE);
                    branchCount = 1;
                }
                break;

            case Core:
                final List<QueryAtom<G>> atoms = ((Core<G>) atom).getQuery()
                        .getAtoms();
                double totalStaticCount = 1.0;
                double totalBranchCount = 1.0;

                branchCount = 1;
                staticCost = 1.0;

                int n = atoms.size();

                Set<Term<G>> lastBound = new HashSet<Term<G>>(bound);
                List<Set<Term<G>>> boundList = new ArrayList<Set<Term<G>>>(n);
                for (final QueryAtom<G> atom2 : atoms) {
                    boundList.add(lastBound);
                    lastBound = new HashSet<Term<G>>(lastBound);
                    lastBound.addAll(atom2.getArguments());
                }

                for (int i = n - 1; i >= 0; i--) {
                    QueryAtom<G> atom2 = atoms.get(i);

                    estimate(atom2, boundList.get(i));

                    totalBranchCount *= branchCount;
                    totalStaticCount = staticCost + branchCount * totalStaticCount;
                }

                staticCost = totalStaticCount;
                branchCount = totalBranchCount;
                break;
            default:
                throw new InternalReasonerException("Unknown atom type "
                        + atom.getPredicate() + ".");
        }

        return staticCost;
    }

    /**
     * {@inheritDoc}
     */
    public double getBranchCount() {
        return branchCount;
    }

    /**
     * {@inheritDoc}
     */
    public double getStaticCost() {
        return staticCost;
    }

    private int getPropertyCount() {
        return estimate.getObjectPropertyCount()
                + estimate.getDataPropertyCount();
    }

    private G inv(G pred) {
        return kb.getFactory().inverseObjectProperty(pred);// getInverses(pred).iterator().next();
        // // TODO
        // kb.getRBox().getRole(
        // pred
        // ).getInverse().getName();
    }

}
