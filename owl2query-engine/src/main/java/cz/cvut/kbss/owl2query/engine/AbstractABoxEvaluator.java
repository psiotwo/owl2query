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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.cvut.kbss.owl2query.model.GroundTerm;
import cz.cvut.kbss.owl2query.model.OWL2Ontology;
import cz.cvut.kbss.owl2query.model.OWL2QueryFactory;
import cz.cvut.kbss.owl2query.model.QueryResult;
import cz.cvut.kbss.owl2query.model.ResultBinding;
import cz.cvut.kbss.owl2query.model.Term;
import cz.cvut.kbss.owl2query.model.VarType;
import cz.cvut.kbss.owl2query.model.Variable;

abstract class AbstractABoxEvaluator<G> implements QueryEvaluator<G> {
	private static final Logger log = OWL2QueryEngine.log;

	protected InternalQuery<G> schemaQuery;
	protected InternalQuery<G> aboxQuery;

	protected OWL2Ontology<G> ontology;

	/**
	 * {@inheritDoc}
	 */
	public QueryResult<G> evaluate(InternalQuery<G> query) {
		if (log.isLoggable(Level.FINE)) {
			log.fine("Executing query " + query.getAtoms());
		}

		partitionQuery(query);

		final QueryResult<G> result;

		if (schemaQuery.getAtoms().isEmpty()) {
			result = new QueryResultImpl<G>(query);
			result.add(new ResultBindingImpl<G>());
		} else {
			if (log.isLoggable(Level.FINE)) {
				log.fine("Executing TBox query: " + schemaQuery);
			}
			result = new CombinedQueryEngine<G>().evaluate(schemaQuery);
			if (log.isLoggable(Level.FINE)) {
				log.fine("Partial binding after schema query : " + result);
			}
		}

		QueryResult<G> newResult;
		if (aboxQuery.getAtoms().isEmpty()) {
			newResult = result;
			if (log.isLoggable(Level.FINER)) {
				log.finer("ABox query empty ... returning.");
			}
		} else {
			newResult = new QueryResultImpl<G>(query);
			for (ResultBinding<G> binding : result) {
				final InternalQuery<G> query2 = aboxQuery.apply(binding);

				if (log.isLoggable(Level.FINE)) {
					log.fine("Executing ABox query: " + query2);
				}
				final QueryResult<G> aboxResult = execABoxQuery(query2);

				for (ResultBinding<G> newBinding : aboxResult) {
					for (final Variable<G> var : binding.keySet()) {
						newBinding.put(var, binding.get(var));
					}

					newResult.add(newBinding);
				}
			}
		}
		return newResult;
	}

	private final void partitionQuery(final InternalQuery<G> query) {

		schemaQuery = new QueryImpl<G>(query);
		aboxQuery = new QueryImpl<G>(query);

		for (final QueryAtom<G> atom : query.getAtoms()) {
			switch (atom.getPredicate()) {
			case Type:
			case PropertyValue:
			case SameAs:
			case DifferentFrom:
				aboxQuery.add(atom);
				break;
			default:
				schemaQuery.add(atom);
			}
		}

		for (final Variable<G> a : query.getDistVars()) {
			if (aboxQuery.getVars().contains(a)) {
				aboxQuery.addDistVar(a);
			}
			if (schemaQuery.getVars().contains(a)) {
				schemaQuery.addDistVar(a);
			}
		}

		for (final Variable<G> a : query.getResultVars()) {
			if (aboxQuery.getVars().contains(a)) {
				aboxQuery.addResultVar(a);
			}
			if (schemaQuery.getVars().contains(a)) {
				schemaQuery.addResultVar(a);
			}
		}

		for (final Variable<G> v : aboxQuery.getDistVarsOfTypes(VarType.CLASS)) {
			if (!schemaQuery.getVars().contains(v)) {
				schemaQuery.SubClassOf(v, ontology.getFactory().wrap(
						ontology.getFactory().getThing()));
			}
		}

		for (final Variable<G> v : aboxQuery.getDistVarsOfTypes(
				VarType.OBJECT_PROPERTY, VarType.OBJECT_OR_DATA_PROPERTY,
				VarType.DATA_PROPERTY)) {
			if (!schemaQuery.getVars().contains(v)) {
				schemaQuery.SubPropertyOf(v, v);
			}
		}

	}

	protected abstract QueryResult<G> execABoxQuery(final InternalQuery<G> q);
}

/**
 * Iterator that iterates over all possible variable bindings.
 * 
 * Given an input map ?var1 -> i11, ..., i1N ... ?varM -> iM1, ..., iMN
 * 
 * 
 * @param <G>
 */
class BindingIterator<G> implements Iterator<ResultBinding<G>> {
	private final List<List<G>> varB = new ArrayList<List<G>>();
	private final List<Variable<G>> vars = new ArrayList<Variable<G>>();
	private final OWL2QueryFactory<G> factory;
	private final int[] indices;

	private boolean more = true;

	public BindingIterator(final Map<Variable<G>, Set<? extends G>> bindings,
			final OWL2QueryFactory<G> factory) {
		vars.addAll(bindings.keySet());
		this.factory = factory;

		for (final Variable<G> var : vars) {
			final Set<? extends G> values = bindings.get(var);
			if (values.isEmpty()) {
				more = false;
				break;
			} else {
				varB.add(new ArrayList<G>(values));
			}
		}

		indices = new int[vars.size()];
	}

	private boolean incIndex(int index) {
		if (indices[index] + 1 < varB.get(index).size()) {
			indices[index]++;
		} else {
			if (index == indices.length - 1) {
				return false;
			} else {
				indices[index] = 0;
				return incIndex(index + 1);
			}
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		return more;
	}

	/**
	 * {@inheritDoc}
	 */
	public ResultBinding<G> next() {
		if (!more)
			return null;

		final ResultBinding<G> next = new ResultBindingImpl<G>();

		for (int i = 0; i < indices.length; i++) {
			next.put(vars.get(i), factory.wrap(varB.get(i).get(indices[i])));
		}

		if (indices.length > 0) {
			more = incIndex(0);
		} else {
			more = false;
		}

		return next;
	}

	/**
	 * {@inheritDoc}
	 */
	public void remove() {
		throw new UnsupportedOperationException(
				"Removal from this iterator is not supported.");
	}
}

class LiteralIterator<G> implements Iterator<ResultBinding<G>> {
	private int[] indices;

	private ResultBinding<G> binding;

	private Set<Variable<G>> litVars;

	private List<List<GroundTerm<G>>> litVarBindings = new ArrayList<List<GroundTerm<G>>>();

	private boolean more = true;

	public LiteralIterator(final InternalQuery<G> q,
			final ResultBinding<G> binding, final OWL2QueryFactory<G> factory) {
		final OWL2Ontology<G> kb = q.getOntology();
		this.binding = binding;
		this.litVars = q.getDistVarsOfTypes(VarType.LITERAL,
				VarType.INDIVIDUAL_OR_LITERAL);

		indices = new int[litVars.size()];
		int index = 0;
		for (final Term<G> litVar : litVars) {
			// final Datatype dtype = ;// q.getDatatype(litVar); TODO after
			// recognizing Datatypes and adjusting Query model supply the
			// corresponding literal.

			final List<G> foundLiterals = new ArrayList<G>();
			boolean first = true;

			for (final QueryAtom<G> atom : q.findAtoms(
					QueryPredicate.PropertyValue, null, null, litVar)) {

				Term<G> subject = atom.getArguments().get(1);
				final Term<G> predicate = atom.getArguments().get(0);

				if (subject.isVariable()) {
					subject = binding.get(subject);
				}

				litVarBindings.add(index, new ArrayList<GroundTerm<G>>());

				final Collection<? extends G> act = kb.getPropertyValues(
						predicate.asGroundTerm().getWrappedObject(), subject
								.asGroundTerm().getWrappedObject()); // dtype);

				if (first) {
					foundLiterals.addAll(act);
				} else {
					foundLiterals.retainAll(act);
					first = false;
				}
			}

			if (foundLiterals.size() > 0) {
				for (final G gt : foundLiterals) {
					litVarBindings.get(index).add(factory.wrap(gt));
				}
				index++;
			} else {
				more = false;
			}
		}
	}

	private boolean incIndex(int index) {
		if (indices[index] + 1 < litVarBindings.get(index).size()) {
			indices[index]++;
		} else {
			if (index == indices.length - 1) {
				return false;
			} else {
				indices[index] = 0;
				return incIndex(index + 1);
			}
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void remove() {
		throw new UnsupportedOperationException(
				"Removal from this iterator is not supported.");
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		return more;
	}

	/**
	 * {@inheritDoc}
	 */
	public ResultBinding<G> next() {
		if (!more)
			return null;

		final ResultBinding<G> next = binding.clone();

		int index = 0;
		for (final Variable<G> o1 : litVars) {
			GroundTerm<G> o2 = litVarBindings.get(index).get(indices[index++]);
			next.put(o1, o2);
		}

		more = incIndex(0);

		return next;
	}
}