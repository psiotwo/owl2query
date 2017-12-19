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

import cz.cvut.kbss.owl2query.model.AllValuesFrom;
import cz.cvut.kbss.owl2query.model.ExactCardinality;
import cz.cvut.kbss.owl2query.model.GroundTerm;
import cz.cvut.kbss.owl2query.model.HasValue;
import cz.cvut.kbss.owl2query.model.IntersectionOf;
import cz.cvut.kbss.owl2query.model.MaxCardinality;
import cz.cvut.kbss.owl2query.model.MinCardinality;
import cz.cvut.kbss.owl2query.model.OWL2Ontology;
import cz.cvut.kbss.owl2query.model.OWL2Query;
import cz.cvut.kbss.owl2query.model.OWL2QueryFactory;
import cz.cvut.kbss.owl2query.model.OWLObjectType;
import cz.cvut.kbss.owl2query.model.ObjectComplementOf;
import cz.cvut.kbss.owl2query.model.ObjectHasSelf;
import cz.cvut.kbss.owl2query.model.SomeValuesFrom;
import cz.cvut.kbss.owl2query.model.Term;
import cz.cvut.kbss.owl2query.model.UnionOf;
import cz.cvut.kbss.owl2query.model.VarType;
import cz.cvut.kbss.owl2query.model.Variable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractOWL2QueryFactory<G> implements OWL2QueryFactory<G> {

	public OWL2Query<G> createQuery(final OWL2Ontology<G> o) {
		return new QueryImpl<G>(o);
	}

	public Variable<G> variable(String name) {
		return new VariableImpl<G>(name);
	}

	public GroundTerm<G> wrap(final G gt) {
		return new GroundTermImpl<G>(gt);
	}
	
//    protected abstract OWL2Ontology getOntology();
    
    @Override
    public AllValuesFrom<G> allValuesFrom(Term<G> ope, Term<G> ce) {
        return new AllValuesFromImpl(ope, ce);
    }
    
    @Override
    public ObjectComplementOf<G> objectComplementOf(Term<G> c) {
        return new ObjectComplementOfImpl(c);
    }
    
    @Override
    public ObjectHasSelf<G> objectHasSelf(Term<G> ope) {
        return new ObjectHasSelfImpl(ope);
    }

    @Override
    public HasValue<G> hasValue(Term<G> ope, Term<G> ni) {
        return new HasValueImpl(ope, ni);
    }

    @Override
    public IntersectionOf<G> intersectionOf(Set<Term<G>> c) {
        return new IntersectionOfImpl(c);
    }

    @Override
    public MinCardinality<G> minCardinality(final int card, final Term<G> ope) {
        return new MinCardinalityImplG(card, ope);
    }
    
    @Override
    public MinCardinality<G> minCardinality(final int card, final Term<G> ope,
            final Term<G> ce) {
        return new MinCardinalityImplS(card, ope, ce);
    }

    @Override
    public MaxCardinality<G> maxCardinality(final int card, final Term<G> ope,
            final Term<G> ce) {
        return new MaxCardinalityImplS(card, ope, ce);
    }

    @Override
    public MaxCardinality<G> maxCardinality(final int card, final Term<G> ope) {
        return new MaxCardinalityImplG(card, ope);
    }

    @Override
    public ExactCardinality<G> exactCardinality(final int card, final Term<G> ope,
            final Term<G> ce) {
        return new ExactCardinalityImplS(card, ope, ce);
    }

    @Override
    public ExactCardinality<G> exactCardinality(final int card, final Term<G> ope) {
        return new ExactCardinalityImplG(card, ope);
    }

    @Override
    public Term<G> oneOf(Set<Term<G>> nis) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SomeValuesFrom<G> someValuesFrom(final Term<G> ope, final Term<G> ce) {
        return new SomeValuesFromImpl(ope, ce);
    }

    @Override
    public UnionOf<G> unionOf(Set<Term<G>> c) {
        return new UnionOfImpl(c);
    }

    /***************************************************/
    /****************** Inner classes ******************/
    /***************************************************/
    public class AllValuesFromImpl extends AllValuesFrom<G> {

        public AllValuesFromImpl(Term<G> ope, Term<G> ce) {
            super(ope, ce);
        }

        @Override
        public Term<G> apply(Map<Variable<G>, GroundTerm<G>> binding, OWL2Ontology<G> ont) {
            final Term<G> ope2 = terms.get(0).apply(binding, ont);
            final Term<G> ce2 = terms.get(1).apply(binding, ont);

            return allValuesFromInner(ope2, ce2, ont);
        }

        @Override
        public VarType getVariableType(Variable<G> var) {
            if (var.equals(terms.get(0))) {
                return VarType.OBJECT_OR_DATA_PROPERTY;
            } else if (var.equals(terms.get(1))) {
                return VarType.CLASS;
            } else {
                throw new IllegalArgumentException();
            }
        }

        protected Term<G> allValuesFromInner(final Term<G> ope, final Term<G> ce, OWL2Ontology<G> ont) {
            if (ce.isGround() && ope.isGround()) {
                final G ceG = ce.asGroundTerm().getWrappedObject();
                final G opeG = ope.asGroundTerm().getWrappedObject();

                if (ont.is(opeG, OWLObjectType.OWLDataProperty)) {
                    return wrap(dataAllValuesFrom(opeG, ceG));
                } else {
                    return wrap(objectAllValuesFrom(opeG, ceG));
                }
            } else {
                return new AllValuesFromImpl(ope, ce);
            }
        }
    }

    public class ObjectComplementOfImpl extends ObjectComplementOf<G> {

        public ObjectComplementOfImpl(Term<G> ce) {
            super(ce);
        }

        @Override
        public Term<G> apply(Map<Variable<G>, GroundTerm<G>> binding, OWL2Ontology<G> ont) {
            final Term<G> c2 = terms.get(0).apply(binding, ont);
            return objectComplementOfInner(c2, ont);
        }

        @Override
        public VarType getVariableType(Variable<G> var) {
            if (var.equals(terms.get(0))) {
                return VarType.CLASS;
            } else {
                throw new IllegalArgumentException();
            }
        }

        protected Term<G> objectComplementOfInner(final Term<G> c, OWL2Ontology<G> ont) {
            if (c.isGround()) {
                return wrap(objectComplementOf(c.asGroundTerm()
                        .getWrappedObject()));
            } else {
                return new ObjectComplementOfImpl(c);
            }
        }
    }

    public class ObjectHasSelfImpl extends ObjectHasSelf<G> {

        public ObjectHasSelfImpl(Term<G> ope) {
            super(ope);
        }

        @Override
        public Term<G> apply(Map<Variable<G>, GroundTerm<G>> binding, OWL2Ontology<G> ont) {
            final Term<G> op2 = terms.get(0).apply(binding, ont);
            return objectHasSelfInner(op2, ont);
        }

        @Override
        public VarType getVariableType(Variable<G> var) {
            if (var.equals(terms.get(0))) {
                return VarType.OBJECT_OR_DATA_PROPERTY;
            } else {
                throw new IllegalArgumentException();
            }
        }

        protected Term<G> objectHasSelfInner(final Term<G> ope, OWL2Ontology<G> ont) {
            if (ope.isGround()) {
                final G opeG = ope.asGroundTerm().getWrappedObject();
                return wrap(objectHasSelf(opeG));
            } else {
                return new ObjectHasSelfImpl(ope);
            }
        }
    }

    public class HasValueImpl extends HasValue<G> {

        public HasValueImpl(Term<G> ope, Term<G> ce) {
            super(ope, ce);
        }

        @Override
        public Term<G> apply(Map<Variable<G>, GroundTerm<G>> binding, OWL2Ontology<G> ont) {
            final Term<G> ope2 = terms.get(0).apply(binding, ont);
            final Term<G> ni2 = terms.get(1).apply(binding, ont);

            return hasValueInner(ope2, ni2, ont);
        }

        @Override
        public VarType getVariableType(Variable<G> var) {
            if (var.equals(terms.get(0))) {
                return VarType.OBJECT_OR_DATA_PROPERTY;
            } else if (var.equals(terms.get(1))) {
                return VarType.INDIVIDUAL;
            } else {
                throw new IllegalArgumentException();
            }
        }

        protected Term<G> hasValueInner(final Term<G> ope, final Term<G> ni, OWL2Ontology<G> ont) {
            if (ni.isGround() && ope.isGround()) {
                final G niG = ni.asGroundTerm().getWrappedObject();
                final G opeG = ope.asGroundTerm().getWrappedObject();

                if (ont.is(opeG, OWLObjectType.OWLDataProperty)) {
                    return wrap(dataHasValue(opeG, niG));
                } else {
                    return wrap(objectHasValue(opeG, niG));
                }
            } else {
                return new HasValueImpl(ope, ni);
            }
        }
    }

    public class IntersectionOfImpl extends IntersectionOf<G> {

        public IntersectionOfImpl(Set<? extends Term<G>> ces) {
            super(ces);
        }

        @Override
        public Term<G> apply(Map<Variable<G>, GroundTerm<G>> binding, OWL2Ontology<G> ont) {
            Set<Term<G>> nts = new HashSet<Term<G>>();

            for (final Term<G> x : this.terms) {
                nts.add(x.apply(binding, ont));
            }
            return intersectionOfInner(nts, ont);
        }

        @Override
        public VarType getVariableType(Variable<G> var) {
            if (terms.contains(var)) {
                return VarType.CLASS;
            } else {
                throw new IllegalArgumentException();
            }
        }

        protected Term<G> intersectionOfInner(final Set<Term<G>> c, OWL2Ontology<G> ont) {
            boolean ground = true;

            Set<Term<G>> terms = new HashSet<Term<G>>();
            Set<G> ces = new HashSet<G>();

            boolean data = false;

            for (final Term<G> o : c) {
                if (!o.isGround()) {
                    ground = false;
                    break;
                } else {
                    G g = o.asGroundTerm().getWrappedObject();
                    data |= !ont.is(g, OWLObjectType.OWLClass);
                    ces.add(g);
                }
            }

            if (ground) {
                if (data) {
                    return wrap(dataIntersectionOf(ces));
                } else {
                    return wrap(objectIntersectionOf(ces));
                }
            } else {
                return new IntersectionOfImpl(c);
            }
        }
    }

    public class MinCardinalityImplG extends MinCardinality<G> {

        public MinCardinalityImplG(int card, Term<G> ope) {
            super(card, ope);
        }

        @Override
        public Term<G> apply(Map<Variable<G>, GroundTerm<G>> binding, OWL2Ontology<G> ont) {
            Term<G> opeB = terms.get(0).apply(binding, ont);
            return minCardinalityInner(card, opeB, ont);
        }

        @Override
        public VarType getVariableType(Variable<G> var) {
            if (var.equals(terms.get(0))) {
                return VarType.OBJECT_OR_DATA_PROPERTY;
            } else if (var.equals(terms.get(1))) {
                return VarType.CLASS;
            } else {
                throw new IllegalArgumentException();
            }
        }

        protected Term<G> minCardinalityInner(final int card, final Term<G> ope, OWL2Ontology<G> ont) {

            if (ope.isGround()) {
                final G opeG = ope.asGroundTerm().getWrappedObject();

                if (ont.is(opeG, OWLObjectType.OWLDataProperty)) {
                    return wrap(dataMinCardinality(card, opeG,
                            getTopDatatype()));
                } else {
                    return wrap(objectMinCardinality(card, opeG, getThing()));
                }
            } else {
                return new MinCardinalityImplG(card, ope);
            }
        }
    }

    public class MinCardinalityImplS extends MinCardinality<G> {

        public MinCardinalityImplS(int card, Term<G> ope, Term<G> ce) {
            super(card, ope, ce);
        }

        @Override
        public Term<G> apply(Map<Variable<G>, GroundTerm<G>> binding, OWL2Ontology<G> ont) {
            final Term<G> ope2 = terms.get(0).apply(binding, ont);
            final Term<G> ce2 = terms.get(1).apply(binding, ont);
            return minCardinalityInner(card, ope2, ce2, ont);
        }

        @Override
        public VarType getVariableType(Variable<G> var) {
            if (var.equals(terms.get(0))) {
                return VarType.OBJECT_OR_DATA_PROPERTY;
            } else if (var.equals(terms.get(1))) {
                return VarType.CLASS;
            } else {
                throw new IllegalArgumentException();
            }
        }

        protected Term<G> minCardinalityInner(final int card, final Term<G> ope,
                final Term<G> ce, OWL2Ontology<G> ont) {

            if (ce.isGround() && ope.isGround()) {
                final G ceG = ce.asGroundTerm().getWrappedObject();
                final G opeG = ope.asGroundTerm().getWrappedObject();

                if (ont.is(opeG, OWLObjectType.OWLDataProperty)) {
                    return wrap(dataMinCardinality(card, opeG, ceG));
                } else {
                    return wrap(objectMinCardinality(card, opeG, ceG));
                }
            } else {
                return new MinCardinalityImplS(card, ope, ce);
            }
        }
    }

    public class MaxCardinalityImplS extends MaxCardinality<G> {

        public MaxCardinalityImplS(int card, Term<G> ope, Term<G> ce) {
            super(card, ope, ce);
        }

        @Override
        public Term<G> apply(Map<Variable<G>, GroundTerm<G>> binding, OWL2Ontology<G> ont) {
            final Term<G> ope2 = terms.get(0).apply(binding, ont);
            final Term<G> ce2 = terms.get(1).apply(binding, ont);
            return maxCardinalityInner(card, ope2, ce2, ont);
        }

        @Override
        public VarType getVariableType(Variable<G> var) {
            if (var.equals(terms.get(0))) {
                return VarType.OBJECT_OR_DATA_PROPERTY;
            } else if (var.equals(terms.get(1))) {
                return VarType.CLASS;
            } else {
                throw new IllegalArgumentException();
            }
        }

        protected Term<G> maxCardinalityInner(final int card, final Term<G> ope,
                final Term<G> ce, OWL2Ontology<G> ont) {

            if (ce.isGround() && ope.isGround()) {
                final G ceG = ce.asGroundTerm().getWrappedObject();
                final G opeG = ope.asGroundTerm().getWrappedObject();

                if (ont.is(opeG, OWLObjectType.OWLDataProperty)) {
                    return wrap(dataMaxCardinality(card, opeG, ceG));
                } else {
                    return wrap(objectMaxCardinality(card, opeG, ceG));
                }
            } else {
                return new MaxCardinalityImplS(card, ope, ce);
            }
        }
    }

    public class MaxCardinalityImplG extends MaxCardinality<G> {

        public MaxCardinalityImplG(int card, Term<G> ope) {
            super(card, ope);
        }

        @Override
        public Term<G> apply(Map<Variable<G>, GroundTerm<G>> binding, OWL2Ontology<G> ont) {
            return maxCardinalityInner(card, terms.get(0).apply(binding, ont), ont);
        }

        @Override
        public VarType getVariableType(Variable<G> var) {
            if (var.equals(terms.get(0))) {
                return VarType.OBJECT_OR_DATA_PROPERTY;
            } else if (var.equals(terms.get(1))) {
                return VarType.CLASS;
            } else {
                throw new IllegalArgumentException();
            }
        }

        protected Term<G> maxCardinalityInner(final int card, final Term<G> ope, OWL2Ontology<G> ont) {

            if (ope.isGround()) {
                final G opeG = ope.asGroundTerm().getWrappedObject();

                if (ont.is(opeG, OWLObjectType.OWLDataProperty)) {
                    return wrap(dataMaxCardinality(card, opeG,
                            getTopDatatype()));
                } else {
                    return wrap(objectMaxCardinality(card, opeG, getThing()));
                }
            } else {
                return new MaxCardinalityImplG(card, ope);
            }
        }
    };

    public class ExactCardinalityImplS extends ExactCardinality<G> {

        public ExactCardinalityImplS(int card, Term<G> ope, Term<G> ce) {
            super(card, ope, ce);
        }

        @Override
        public Term<G> apply(Map<Variable<G>, GroundTerm<G>> binding, OWL2Ontology<G> ont) {
            final Term<G> ope2 = terms.get(0).apply(binding, ont);
            final Term<G> ce2 = terms.get(1).apply(binding, ont);
            return exactCardinalityInner(card, ope2, ce2, ont);
        }

        @Override
        public VarType getVariableType(Variable<G> var) {
            if (var.equals(terms.get(0))) {
                return VarType.OBJECT_OR_DATA_PROPERTY;
            } else if (var.equals(terms.get(1))) {
                return VarType.CLASS;
            } else {
                throw new IllegalArgumentException();
            }
        }

        protected Term<G> exactCardinalityInner(final int card, final Term<G> ope,
                final Term<G> ce, OWL2Ontology<G> ont) {

            if (ce.isGround() && ope.isGround()) {
                final G ceG = ce.asGroundTerm().getWrappedObject();
                final G opeG = ope.asGroundTerm().getWrappedObject();

                if (ont.is(opeG, OWLObjectType.OWLDataProperty)) {
                    return wrap(dataExactCardinality(card, opeG, ceG));
                } else {
                    return wrap(objectExactCardinality(card, opeG, ceG));
                }
            } else {
                return new ExactCardinalityImplS(card, ope, ce);
            }
        }
    };

    public class ExactCardinalityImplG extends ExactCardinality<G> {

        public ExactCardinalityImplG(int card, Term<G> ope) {
            super(card, ope);
        }

        @Override
        public Term<G> apply(Map<Variable<G>, GroundTerm<G>> binding, OWL2Ontology<G> ont) {
            return exactCardinalityInner(card, terms.get(0).apply(binding, ont), ont);
        }

        @Override
        public VarType getVariableType(Variable<G> var) {
            if (var.equals(terms.get(0))) {
                return VarType.OBJECT_OR_DATA_PROPERTY;
            } else if (var.equals(terms.get(1))) {
                return VarType.CLASS;
            } else {
                throw new IllegalArgumentException();
            }
        }

        public Term<G> exactCardinalityInner(final int card, final Term<G> ope, OWL2Ontology<G> ont) {

            if (ope.isGround()) {
                final G opeG = ope.asGroundTerm().getWrappedObject();

                if (ont.is(opeG, OWLObjectType.OWLDataProperty)) {
                    return wrap(dataExactCardinality(card, opeG,
                            getTopDatatype()));
                } else {
                    return wrap(objectExactCardinality(card, opeG, getThing()));
                }
            } else {
                return new ExactCardinalityImplG(card, ope);
            }
        }
    };

    public class SomeValuesFromImpl extends SomeValuesFrom<G> {

        public SomeValuesFromImpl(Term<G> ope, Term<G> ce) {
            super(ope, ce);
        }

        @Override
        public Term<G> apply(Map<Variable<G>, GroundTerm<G>> binding, OWL2Ontology<G> ont) {
            final Term<G> ope2 = terms.get(0).apply(binding, ont);
            final Term<G> ce2 = terms.get(1).apply(binding, ont);

            return someValuesFromInner(ope2, ce2, ont);
        }

        @Override
        public VarType getVariableType(Variable<G> var) {
            if (var.equals(terms.get(0))) {
                return VarType.OBJECT_OR_DATA_PROPERTY;
            } else if (var.equals(terms.get(1))) {
                return VarType.CLASS;
            } else {
                throw new IllegalArgumentException();
            }
        }

        public Term<G> someValuesFromInner(final Term<G> ope, final Term<G> ce, OWL2Ontology<G> ont) {

            if (ce.isGround() && ope.isGround()) {
                final G ceG = ce.asGroundTerm().getWrappedObject();
                final G opeG = ope.asGroundTerm().getWrappedObject();

                if (ont.is(opeG, OWLObjectType.OWLDataProperty)) {
                    return wrap(dataSomeValuesFrom(opeG, ceG));
                } else {
                    return wrap(objectSomeValuesFrom(opeG, ceG));
                }
            } else {
                return new SomeValuesFromImpl(ope, ce);
            }
        }
    };

    public class UnionOfImpl extends UnionOf<G> {

        public UnionOfImpl(Set<? extends Term<G>> ces) {
            super(ces);
        }

        @Override
        public Term<G> apply(Map<Variable<G>, GroundTerm<G>> binding, OWL2Ontology<G> ont) {
            Set<Term<G>> nts = new HashSet<Term<G>>();

            for (final Term<G> x : this.terms) {
                nts.add(x.apply(binding, ont));
            }
            return unionOfInner(nts, ont);
        }

        @Override
        public VarType getVariableType(Variable<G> var) {
            if (terms.contains(var)) {
                return VarType.CLASS;
            } else {
                throw new IllegalArgumentException();
            }
        }

        protected Term<G> unionOfInner(Set<Term<G>> c, OWL2Ontology<G> ont) {

            boolean ground = true;

            Set<Term<G>> terms = new HashSet<Term<G>>();
            Set<G> ces = new HashSet<G>();

            boolean data = false;

            for (final Term<G> o : c) {
                if (!o.isGround()) {
                    ground = false;
                    break;
                } else {
                    G g = o.asGroundTerm().getWrappedObject();
                    data |= !ont.is(g, OWLObjectType.OWLClass);
                    ces.add(g);
                }
            }

            if (ground) {
                if (data) {
                    return wrap(dataUnionOf(ces));
                } else {
                    return wrap(objectUnionOf(ces));
                }

            } else {
                return new UnionOfImpl(c);
            }
        }
    };
}