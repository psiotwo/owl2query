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
package cz.cvut.kbss.owl2query.model;

import java.util.List;

public interface QueryResult<G> extends Iterable<ResultBinding<G>> {

    /**
     * Adds a new binding to the query result.
     * <p>
     * If the associated query contains a LIMIT clause, this method will return {@code false} once the limit has been
     * reached the specified binding cannot be added.
     * <p>
     * Note that if the associated query contains an OFFSET clause, this method simply skips the binding and returns
     * {@code true} if the offset value has not been reached, yet.
     *
     * @param binding to be added
     * @return {@code true} if binding was added, {@code false} if the binding was rejected because the result is full
     */
    boolean add(final ResultBinding<G> binding);

    /**
     * Returns result variables.
     *
     * @return variables that appear in the result
     */
    public List<Variable<G>> getResultVars();

    public boolean isDistinct();

    /**
     * Tests whether the result is empty or not.
     *
     * @return true if the result contains not bindings
     */
    public boolean isEmpty();

    /**
     * Returns number of bindings in the result.
     *
     * @return number of bindings
     */
    public int size();
}
