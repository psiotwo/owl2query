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
import cz.cvut.kbss.owl2query.model.ResultBinding;

abstract class QueryPlan<G> {

	protected InternalQuery<G> query;

	public QueryPlan(final InternalQuery<G> query) {
		this.query = query;
	}

	public InternalQuery<G> getQuery() {
		return query;
	}

	/**
	 * Returns next atom to be executed w.r. to the current binding.
	 * 
	 * @param binding
	 * @return
	 */
	public abstract QueryAtom<G> next(final ResultBinding<G> binding);

	/**
	 * Goes one level back to the last atom.
	 */
	public abstract void back();

	/**
	 * Checks whether there is another atom to execute.
	 * 
	 * @return true if there is another atom to execute.
	 */
	public abstract boolean hasNext();

	/**
	 * Resets the query planner.
	 */
	public abstract void reset();
}
