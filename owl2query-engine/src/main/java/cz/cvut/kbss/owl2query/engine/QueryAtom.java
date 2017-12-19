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
import java.util.List;
import java.util.Map;

import cz.cvut.kbss.owl2query.model.Term;

public interface QueryAtom<G> {

	/**
	 * Returns predicate of the query atom.
	 * 
	 * @return predicate of the query atom
	 */
	public QueryPredicate getPredicate();

	/**
	 * Returns arguments of the atom.
	 * 
	 * @return arguments of the atom
	 */
	public List<Term<G>> getArguments();

	/**
	 * 
	 * @return true if the atom is ground, i.e. does not use variables, either
	 *         distinguished or undistinguished ones.
	 */
	public boolean isGround();

	/**
	 * Applies variable binding to the current atom and returns the result.
	 * Current atom is not affected.
	 * 
	 * @param binding
	 *            QueryBinding to apply
	 * @return a query atom with applied query binding
	 */
	public QueryAtom<G> apply(final Map<? extends Term<G>, ? extends Term<G>> binding, OWL2Ontology<G> ont);
}
