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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import cz.cvut.kbss.owl2query.model.GroundTerm;
import cz.cvut.kbss.owl2query.model.OWL2Ontology;
import cz.cvut.kbss.owl2query.model.Term;

class Core<G> implements QueryAtom<G> {
	private InternalQuery<G> query;

	private Term<G> term;

	private GroundTerm<G> rollUp;

	public Core(final Term<G> term, final GroundTerm<G> rollUp,
			InternalQuery<G> atom) {
		this.query = atom;
		this.term = term;
		this.rollUp = rollUp;
	}

	public QueryAtom<G> apply(
			final Map<? extends Term<G>, ? extends Term<G>> binding, OWL2Ontology<G> ont) {
		if (binding.containsKey(term)) {
			return new Core<G>(binding.get(term), rollUp, query.apply(binding));
		} else {
			return new Core<G>(term, rollUp, query.apply(binding));
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Core))
			return false;

		final Core<G> c = (Core<G>) obj;

		return query.equals(c.query) && term.equals(c.term);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Term<G>> getArguments() {
		return Arrays.asList(term);
	}

	public Term<G> getTerm() {
		return term;
	}

	public GroundTerm<G> getRollUp() {
		return rollUp;
	}

	public InternalQuery<G> getQuery() {
		return query;
	}

	/**
	 * {@inheritDoc}
	 */
	public QueryPredicate getPredicate() {
		return QueryPredicate.Core;
	}

	@Override
	public int hashCode() {
		return 17 * query.hashCode() + 3 * term.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isGround() {
		return term.isGround();
	}

	@Override
	public String toString() {
		return getPredicate().shortForm() + "(" + term + ": " + query + ")";
	}
}
