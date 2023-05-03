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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.cvut.kbss.owl2query.model.*;
import org.apache.jena.sparql.syntax.ElementBind;

import cz.cvut.kbss.owl2query.UnsupportedQueryException;

class QueryImpl<G> implements InternalQuery<G> {

	private static final Logger LOG = Logger.getLogger(InternalQuery.class
			.getName());

	private final List<QueryAtom<G>> allAtoms = new ArrayList<>();
	private final OWL2Ontology<G> ontology;

	private final Set<GroundTerm<G>> individualsAndLiterals = new HashSet<>();

	// VARIABLES
	private final List<Variable<G>> resultVars = new ArrayList<>();
	private final Set<Variable<G>> distVars = new HashSet<>();
	private final Map<Variable<G>, Set<VarType>> variables = new HashMap<>();

	private boolean distinct = false;
	private boolean empty = false;

	private int offset = 0;
	private int limit = Integer.MAX_VALUE;

	private List<ResultBinding<G>> values = Collections.emptyList();

	QueryImpl(final OWL2Ontology<G> kb) {
		this.ontology = kb;
	}

	public QueryImpl(final InternalQuery<G> query) {
		this(query.getOntology());
	}

	private boolean checkType(Term<G> t, final VarType type) {
		if (t.isVariable()) {
			Set<VarType> vars = this.variables.get(t);
			if (vars == null) {
				vars = new HashSet<>();
				this.variables.put(t.asVariable(), vars);
			}
			empty &= !type.updateIfValid(vars);
		} else if (t.isGround()) {
			boolean result;
			result = ontology.is(t.asGroundTerm().getWrappedObject(), type
					.getAllowedTypes());
			if (!result && LOG.isLoggable(Level.WARNING)) {
				LOG.warning("'" + t + "' is not an object of type '" + type
						+ "'.");
			}
			return result;
		}

		// TODO

		return true;
	}

	/**
	 * Updates term types
	 *
	 * @return false if the term type is undefined or of incompatible types.
	 */
	private boolean setTermType(final QueryAtom<G> atom) {
		final List<Term<G>> args = atom.getArguments();

		boolean def = true;

		switch (atom.getPredicate()) {
		case Type:
		case DirectType:
			def &= checkType(args.get(0), VarType.CLASS);
			def &= checkType(args.get(1), VarType.INDIVIDUAL);
			break;
		case PropertyValue:
			Term<G> p = args.get(0);
			Term<G> s = args.get(1);
			Term<G> o = args.get(2);
			def &= checkType(s, VarType.INDIVIDUAL);
			if (o.isVariable()) {
				if (p.isVariable()) {
					def &= checkType(p, VarType.OBJECT_OR_DATA_PROPERTY);
					def &= checkType(o, VarType.INDIVIDUAL_OR_LITERAL);
				} else if (ontology.is(p.asGroundTerm().getWrappedObject(),
						OWLObjectType.OWLObjectProperty)) {
					def &= checkType(p, VarType.OBJECT_PROPERTY);
					def &= checkType(o, VarType.INDIVIDUAL);
				} else if (ontology.is(p.asGroundTerm().getWrappedObject(),
						OWLObjectType.OWLDataProperty)) {
					def &= checkType(p, VarType.DATA_PROPERTY);
					def &= checkType(o, VarType.LITERAL);
				} else {
					// neither object nor data property -> stop
					empty = true;
				}
			} else if (ontology.is(o.asGroundTerm().getWrappedObject(),
					OWLObjectType.OWLLiteral)) {
				def &= checkType(p, VarType.DATA_PROPERTY);
				def &= checkType(o, VarType.LITERAL);
			} else {
				def &= checkType(p, VarType.OBJECT_PROPERTY);
				def &= checkType(o, VarType.INDIVIDUAL);
			}
			break;
		case SameAs:
		case DifferentFrom:
			def &= checkType(args.get(0), VarType.INDIVIDUAL);
			def &= checkType(args.get(1), VarType.INDIVIDUAL);
			break;
		case DatatypeProperty:
			def &= checkType(args.get(0), VarType.DATA_PROPERTY);
			break;
		case ObjectProperty:
		case Transitive:
		case Reflexive:
		case Irreflexive:
		case InverseFunctional:
		case Symmetric:
		case Asymmetric:
			def &= checkType(args.get(0), VarType.OBJECT_PROPERTY);
			break;
		case Functional:
			def &= checkType(args.get(0), VarType.OBJECT_OR_DATA_PROPERTY);
			break;
		case InverseOf:
			def &= checkType(args.get(0), VarType.OBJECT_PROPERTY);
			def &= checkType(args.get(1), VarType.OBJECT_PROPERTY);
			break;
		case SubPropertyOf:
		case EquivalentProperty:
		case StrictSubPropertyOf:
		case DirectSubPropertyOf:
			def &= checkType(args.get(0), VarType.OBJECT_OR_DATA_PROPERTY);
			def &= checkType(args.get(1), VarType.OBJECT_OR_DATA_PROPERTY);
			break;
		case SubClassOf:
		case EquivalentClass:
		case DisjointWith:
		case ComplementOf:
		case StrictSubClassOf:
		case DirectSubClassOf:
			def &= checkType(args.get(0), VarType.CLASS);
			def &= checkType(args.get(1), VarType.CLASS);
			break;
		case Core:
			// do nothing
			// for (final Term<G> t : args) {
			// def &= checkType(t, VarType.INDIVIDUAL_OR_LITERAL);
			// }
			break;
		case Not:
			// do nothing
			break;
		default:
			break;
		}

		return def;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Returns a set of variables of ONE of the types.
	 */
	public Set<Variable<G>> getDistVarsOfTypes(final VarType... t) {
		final Set<Variable<G>> vars = new HashSet<>();

		for (final Variable<G> v : distVars) {
			final Set<VarType> varsx = this.variables.get(v);
			if (varsx != null && !Collections.disjoint(varsx, Arrays.asList(t))) {
				vars.add(v);
			}
		}

		return vars;
	}

	/**
	 * {@inheritDoc}
	 */
	public InternalQuery<G> add(final QueryAtom<G> atom) throws UnsupportedQueryException  {
		if (!allAtoms.contains(atom)) {
			if (!setTermType(atom)) {
				throw new UnsupportedQueryException("Query atom " + atom +" is invalid.");
			}
 			allAtoms.add(atom);
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public InternalQuery<G> addDistVar(Variable<G> a) {
		return addDistVar(a, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public InternalQuery<G> addDistVar(Variable<G> a, boolean result) {
		distVars.add(a);

		if (result && !resultVars.contains(a)) {
			resultVars.add(a);
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public InternalQuery<G> addResultVar(Variable<G> a) {
		addDistVar(a,true);
        return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<QueryAtom<G>> getAtoms() {
		return Collections.unmodifiableList(allAtoms);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<GroundTerm<G>> getConstants() {
		return Collections.unmodifiableSet(individualsAndLiterals);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<Variable<G>> getDistVars() {
		return Collections.unmodifiableSet(distVars);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<Variable<G>> getUndistVars() {
		final Set<Variable<G>> result = new HashSet<>(variables
				.keySet());

		result.removeAll(getDistVars());

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Variable<G>> getResultVars() {
		return Collections.unmodifiableList(resultVars);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<Variable<G>> getVars() {
		return Collections.unmodifiableSet(variables.keySet());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isBoolean() {
		return distVars.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	public OWL2Ontology<G> getOntology() {
		return ontology;
	}

	/**
	 * {@inheritDoc}
	 */
	public InternalQuery<G> apply(
			final Map<? extends Term<G>, ? extends Term<G>> binding) {
		final QueryImpl<G> query = new QueryImpl<>(this);

		for (final QueryAtom<G> atom : getAtoms()) {
			try {
				query.add(atom.apply(binding, ontology));
			} catch( UnsupportedQueryException e ) {
				// DO nothing
			}
		}

		query.resultVars.addAll(this.resultVars);
		query.resultVars.removeAll(binding.keySet());

		query.distVars.addAll(this.distVars);
		query.distVars.removeAll(binding.keySet());

		query.individualsAndLiterals.addAll(this.individualsAndLiterals);

		for (final Term<G> t : binding.keySet()) {
			if (distVars.contains(t) && binding.get(t).isGround() ) {
				query.individualsAndLiterals.add(binding.get(t).asGroundTerm());
			}
		}

		return query;
	}

	public <X extends Term<G>> G rollUpTo(final Term<G> var,
			final Collection<X> visited) {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("rollUp(" + var + ", " + this);
		}

		G result = _rollUpTo(var, new HashSet<>(), visited);

		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Rolling up " + var + " to " + result);
		}

		return result;
	}

	/**
	 *
	 *
	 * @param var
	 *            a term to which the query should be rolled
	 * @param visited
	 *            a set of visited edges
	 */
	public <X extends Term<G>> G _rollUpTo(final Term<G> var,
			final Set<QueryAtom<G>> visited, final Collection<X> stopTerms) {
		Set<G> classParts = new HashSet<>();

		for (final QueryAtom<G> atom : findAtoms(QueryPredicate.Type, null, var)) {
			final Term<G> arg = atom.getArguments().get(0);
			if (arg.isVariable()) {
				throw new InternalReasonerException(
						"Variables as predicates are not supported yet");
			} else {
				classParts.add(arg.asGroundTerm().getWrappedObject());
			}
		}

		if (var.isGround()) {
			final G g = var.asGroundTerm().getWrappedObject();
			final Set<G> col = Collections.singleton(g);

			final G gg = ontology.getFactory().objectOneOf(col);

			classParts.add(gg);
		}

		final OWL2QueryFactory<G> factory = ontology.getFactory();

		if (!stopTerms.contains(var)) {
			final Collection<QueryAtom<G>> inEdges = findAtoms(
					QueryPredicate.PropertyValue, null, null, var);
			for (final QueryAtom<G> a : inEdges) {
				if (visited.contains(a)) {
					continue;
				}
				visited.add(a);

				// obj must be an individual/individual variable
				final Term<G> pred = a.getArguments().get(0);
				final Term<G> subj = a.getArguments().get(1);

				if (pred.isVariable()) {
					throw new InternalReasonerException();
				}

				classParts.add(factory.objectSomeValuesFrom(ontology
						.getFactory().inverseObjectProperty(
								pred.asGroundTerm().getWrappedObject()),
						_rollUpTo(subj, visited, stopTerms)));
			}

			final Collection<QueryAtom<G>> outEdges = findAtoms(
					QueryPredicate.PropertyValue, null, var, null);
			for (final QueryAtom<G> a : outEdges) {
				if (visited.contains(a)) {
					continue;
				}
				visited.add(a);

				final Term<G> pred = a.getArguments().get(0);
				final Term<G> obj = a.getArguments().get(2);

				if (pred.isVariable()) {
					throw new InternalReasonerException();
				}

				if (ontology.is(pred.asGroundTerm().getWrappedObject(),
						OWLObjectType.OWLObjectProperty)) {
					classParts.add(factory.objectSomeValuesFrom(pred
							.asGroundTerm().getWrappedObject(), _rollUpTo(obj,
							visited, stopTerms)));
				} else if (ontology.is(pred.asGroundTerm().getWrappedObject(),
						OWLObjectType.OWLDataProperty)) {

					if (obj.isVariable()) {
						classParts.add(factory.dataSomeValuesFrom(pred
								.asGroundTerm().getWrappedObject(), factory
								.getTopDatatype()));
					} else {
						classParts.add(factory.dataSomeValuesFrom(pred
								.asGroundTerm().getWrappedObject(), factory
								.dataOneOf(Collections.singleton(obj
										.asGroundTerm().getWrappedObject()))));

					}
				} else {
					throw new InternalReasonerException();
				}
			}

		}
		if (classParts.isEmpty()) {
			classParts.add(factory.getThing());
		}
		// if (classParts.size() == 1) {
		// return classParts.iterator().next();
		// } else {
		return ontology.getFactory().objectIntersectionOf(classParts);
		// }
	}

	/**
	 * {@inheritDoc}
	 */
	public List<QueryAtom<G>> findAtoms(final QueryPredicate predicate,
			final Term<G>... args) {
		final List<QueryAtom<G>> list = new ArrayList<>();
		for (final QueryAtom<G> atom : allAtoms) {
			if (!predicate.equals(atom.getPredicate())) {
				continue;
			}
			int i = 0;
			boolean add = true;
			for (final Term<G> arg : atom.getArguments()) {
				final Term<G> argValue = args[i++];
				if ((argValue != null && argValue != arg)) {
					add = false;
					break;
				}
			}

			if (add) {
				list.add(atom);
			}
		}
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	public void remove(QueryAtom<G> atom) {
		if (!allAtoms.contains(atom)) {
			return;
		}

		allAtoms.remove(atom);

		final Set<Term<G>> rest = new HashSet<>();

		for (final QueryAtom<G> atom2 : allAtoms) {
			rest.addAll(atom2.getArguments());
		}

		final Set<Term<G>> toRemove = new HashSet<>(atom.getArguments());
		toRemove.removeAll(rest);

		for (final Term<G> a : toRemove) {
			variables.remove(a);
			distVars.remove(a);
			resultVars.remove(a);
			individualsAndLiterals.remove(a);
		}
	}

	@Override
	public String toString() {
		return toString(false);
	}

	public String toString(boolean multiLine) {
		final StringBuilder sb = new StringBuilder();

		sb.append("Q(");
		for (int i = 0; i < resultVars.size(); i++) {
			Variable<G> var = resultVars.get(i);
			if (i > 0)
				sb.append(", ");
			sb.append(var.asVariable().getName());
		}
		sb.append(")");

		if (allAtoms.size() > 0) {
			sb.append(" :-");
			if (multiLine)
				sb.append("\n");
			for (int i = 0; i < allAtoms.size(); i++) {
				final QueryAtom<G> a = allAtoms.get(i);
				if (i > 0) {
					sb.append(",");
					if (multiLine)
						sb.append("\n");
				}

				sb.append(a.toString());
			}
		}

		sb.append(".");
		if (multiLine)
			sb.append("\n");
		return sb.toString();
	}

	public OWL2Query<G> distinct(boolean d) {
		this.distinct = d;
		return this;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public boolean canHaveResults() {
		return !empty;
	}

	public OWL2Query<G> Asymmetric(Term<G> pA) {
		return add(new QueryAtomImpl<>(QueryPredicate.Asymmetric, pA));
	}

	public OWL2Query<G> ComplementOf(Term<G> cA1, Term<G> cA2) {
		return add(new QueryAtomImpl<>(QueryPredicate.ComplementOf, cA1, cA2));
	}

	public OWL2Query<G> DatatypeProperty(Term<G> pA) {
		return add(new QueryAtomImpl<>(QueryPredicate.DatatypeProperty, pA));
	}

	public OWL2Query<G> DifferentFrom(Term<G> i1, Term<G> i2) {
		return add(new QueryAtomImpl<>(QueryPredicate.DifferentFrom, i1, i2));
	}

	public OWL2Query<G> DirectSubClassOf(Term<G> c1, Term<G> c2) {
		return add(new QueryAtomImpl<>(QueryPredicate.DirectSubClassOf, c1, c2));
	}

	public OWL2Query<G> DirectSubPropertyOf(Term<G> p1, Term<G> p2) {
		return add(new QueryAtomImpl<>(QueryPredicate.DirectSubPropertyOf, p1,
				p2));
	}

	public OWL2Query<G> DirectType(Term<G> c, Term<G> i) {
		return add(new QueryAtomImpl<>(QueryPredicate.DirectType, c, i));
	}

	public OWL2Query<G> DisjointWith(Term<G> cA1, Term<G> cA2) {
		return add(new QueryAtomImpl<>(QueryPredicate.DisjointWith, cA1, cA2));
	}

	public OWL2Query<G> EquivalentClass(Term<G> cA1, Term<G> cA2) {
		return add(new QueryAtomImpl<>(QueryPredicate.EquivalentClass, cA1,
				cA2));
	}

	public OWL2Query<G> EquivalentProperty(Term<G> pA1, Term<G> pA2) {
		return add(new QueryAtomImpl<>(QueryPredicate.EquivalentProperty, pA1,
				pA2));
	}

	public OWL2Query<G> Functional(Term<G> pA) {
		return add(new QueryAtomImpl<>(QueryPredicate.Functional, pA));
	}

	public OWL2Query<G> InverseFunctional(Term<G> pA) {
		return add(new QueryAtomImpl<>(QueryPredicate.InverseFunctional, pA));
	}

	public OWL2Query<G> InverseOf(Term<G> pA1, Term<G> pA2) {
		return add(new QueryAtomImpl<>(QueryPredicate.InverseOf, pA1, pA2));
	}

	public OWL2Query<G> Irreflexive(Term<G> pA) {
		return add(new QueryAtomImpl<>(QueryPredicate.Irreflexive, pA));
	}

	public OWL2Query<G> ObjectProperty(Term<G> pA) {
		return add(new QueryAtomImpl<>(QueryPredicate.ObjectProperty, pA));
	}

	public OWL2Query<G> PropertyValue(Term<G> pA, Term<G> iA, Term<G> ilA) {
		return add(new QueryAtomImpl<>(QueryPredicate.PropertyValue, pA, iA,
				ilA));
	}

	public OWL2Query<G> Reflexive(Term<G> pA) {
		return add(new QueryAtomImpl<>(QueryPredicate.Reflexive, pA));
	}

	public OWL2Query<G> SameAs(Term<G> i1, Term<G> i2) {
		return add(new QueryAtomImpl<>(QueryPredicate.SameAs, i1, i2));
	}

	// SPARQL-DL nonmonotonic extension
	public OWL2Query<G> StrictSubClassOf(final Term<G> c1, final Term<G> c2) {
		return add(new QueryAtomImpl<>(QueryPredicate.StrictSubClassOf, c1, c2));
	}

	public OWL2Query<G> StrictSubPropertyOf(final Term<G> c1, final Term<G> c2) {
		return add(new QueryAtomImpl<>(QueryPredicate.StrictSubPropertyOf, c1,
				c2));
	}

	public OWL2Query<G> SubClassOf(Term<G> cA1, Term<G> cA2) {
		return add(new QueryAtomImpl<>(QueryPredicate.SubClassOf, cA1, cA2));
	}

	public OWL2Query<G> SubPropertyOf(Term<G> pA1, Term<G> pA2) {
		return add(new QueryAtomImpl<>(QueryPredicate.SubPropertyOf, pA1, pA2));
	}

	public OWL2Query<G> Symmetric(Term<G> pA) {
		return add(new QueryAtomImpl<>(QueryPredicate.Symmetric, pA));
	}

	public OWL2Query<G> Transitive(Term<G> pA) {
		return add(new QueryAtomImpl<>(QueryPredicate.Transitive, pA));
	}

	public OWL2Query<G> Type(Term<G> c, Term<G> i) {
		return add(new QueryAtomImpl<>(QueryPredicate.Type, c, i));
	}

	public OWL2Query<G> Not(OWL2Query<G> qb) {
		return add(new NotQueryAtom<>((QueryImpl<G>) qb));
	}

	public OWL2Query<G> Core(Term<G> c, GroundTerm<G> rollUp, InternalQuery<G> q) {
		return add(new Core<>(c, rollUp, q));
	}

	@Override
	public <T> OWL2Query<G> external( T t ) {
		if (t instanceof ElementBind) {
			ElementBind b = (ElementBind) t;
			return add(new ARQBindExternal<>(b.getVar(), b.getExpr()));
		} else {
			LOG.config("Unsupported external atom: " + t + ", ignoring.");
			return this;
		}
	}

	@Override
	public OWL2Query<G> setOffset(int offset) {
		if (offset < 0) {
			throw new IllegalArgumentException("Offset cannot be less than 0.");
		}
		this.offset = offset;
		return this;
	}

	@Override
	public int getOffset() {
		return offset;
	}

	@Override
	public OWL2Query<G> setLimit(int limit) {
		if (limit < 0) {
			throw new IllegalArgumentException("Limit cannot be less than 0.");
		}
		this.limit = limit;
		return this;
	}

	@Override
	public int getLimit() {
		return limit;
	}

	@Override
	public OWL2Query<G> setValues(List<ResultBinding<G>> values) {
		this.values = values;
		return this;
	}

	@Override
	public List<ResultBinding<G>> getValues() {
		return values;
	}

}
