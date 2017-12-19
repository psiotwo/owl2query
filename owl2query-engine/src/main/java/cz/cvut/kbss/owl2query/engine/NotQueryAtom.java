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

import cz.cvut.kbss.owl2query.model.OWL2Ontology;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import cz.cvut.kbss.owl2query.model.Term;

class NotQueryAtom<G> implements QueryAtom<G> {
	private InternalQuery<G> query;

	public NotQueryAtom(InternalQuery<G> atom) {
		this.query = atom;
	}

	public QueryAtom<G> apply(final Map<? extends Term<G>, ? extends Term<G>> binding, OWL2Ontology<G> ont) {
		return new NotQueryAtom<G>(query.apply(binding));
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof NotQueryAtom))
			return false;
		return query.equals(((NotQueryAtom<G>) obj).query);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Term<G>> getArguments() {
		return Arrays.asList((Term<G>[]) query.getResultVars().toArray(
				new Term[] {}));
	}

	public InternalQuery<G> getQuery() {
		return query;
	}

	/**
	 * {@inheritDoc}
	 */
	public QueryPredicate getPredicate() {
		return QueryPredicate.Not;
	}

	@Override
	public int hashCode() {
		return 17 * query.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isGround() {
		return query.getDistVars().isEmpty();
	}

	@Override
	public String toString() {
		return "Not(" + query + ")";
	}
}
