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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cz.cvut.kbss.owl2query.model.GroundTerm;
import cz.cvut.kbss.owl2query.model.OWL2Ontology;
import cz.cvut.kbss.owl2query.model.Term;
import cz.cvut.kbss.owl2query.model.Variable;

class QueryAtomImpl<G> implements QueryAtom<G> {

	protected final QueryPredicate predicate;

	protected final List<Term<G>> arguments;

	protected boolean ground;

	public QueryAtomImpl(final QueryPredicate predicate, final Term<G> argument) {
		this(predicate, Collections.singletonList(argument));
	}

	public QueryAtomImpl(final QueryPredicate predicate, final Term<G> a1,
			Term<G> a2) {
		this(predicate, Arrays.asList(a1, a2));
	}

	public QueryAtomImpl(final QueryPredicate predicate, final Term<G> a1,
			Term<G> a2, Term<G> a3) {
		this(predicate, Arrays.asList(a1, a2, a3));
	}

	private QueryAtomImpl(final QueryPredicate predicate,
			final List<Term<G>> arguments) {
		if (predicate == null) {
			throw new RuntimeException("Predicate cannot be null.");
		}

		this.predicate = predicate;
		this.arguments = arguments;
		//
		ground = true;
		for (final Term<G> a : arguments) {
			if (!a.getVariables().isEmpty()) {
				ground = false;
				break;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public QueryPredicate getPredicate() {
		return predicate;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Term<G>> getArguments() {
		return arguments;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isGround() {
		return ground;
	}

	/**
	 * {@inheritDoc}
	 */
	public QueryAtom<G> apply(
			final Map<? extends Term<G>, ? extends Term<G>> binding, OWL2Ontology<G> ont) {
		final List<Term<G>> newArguments = new ArrayList<Term<G>>();

		for (final Term<G> a : arguments) {
			newArguments
					.add(a.apply((Map<Variable<G>, GroundTerm<G>>) binding, ont));
		}

		return new QueryAtomImpl<G>(predicate, newArguments);
	}

	@Override
	public int hashCode() {
		return 31 * predicate.hashCode() + arguments.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final QueryAtomImpl<G> other = (QueryAtomImpl<G>) obj;

		return predicate.equals(other.predicate)
				&& arguments.equals(other.arguments);
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();

		for (int i = 0; i < arguments.size(); i++) {
			final Term<G> a = arguments.get(i);
			if (i > 0) {
				sb.append(", ");
			}

			sb.append(a.shortForm());
		}

		return predicate.shortForm() + "(" + sb.toString() + ")";
	}

}
