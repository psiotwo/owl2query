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

import java.util.Iterator;
import java.util.List;

import cz.cvut.kbss.owl2query.model.QueryResult;
import cz.cvut.kbss.owl2query.model.ResultBinding;
import cz.cvut.kbss.owl2query.model.Variable;

class CarthesianProductResult<G> implements QueryResult<G> {

	private final List<Variable<G>> resultVars;
	private final List<QueryResult<G>> queryResults;

	private int size;

	public CarthesianProductResult(final List<Variable<G>> resultVars,
			final List<QueryResult<G>> queryResults) {
		this.resultVars = resultVars;
		this.queryResults = queryResults;

		size = 1;
		for (final QueryResult<G> result : queryResults) {
			size *= result.size();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean add(ResultBinding<G> binding) {
		throw new UnsupportedOperationException(
				"CarthesianProductResult do not support addition!");
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Variable<G>> getResultVars() {
		return resultVars;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isDistinct() {
		for (final QueryResult<G> result : queryResults) {
			if (!result.isDistinct())
				return false;
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator<ResultBinding<G>> iterator() {
		return new CarthesianProductIterator<>(queryResults);
	}

	/**
	 * {@inheritDoc}
	 */
	public int size() {
		return size;
	}
}
