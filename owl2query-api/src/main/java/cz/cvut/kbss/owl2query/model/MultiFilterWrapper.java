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

/**
 * @author Petr Kremen
 */
public class MultiFilterWrapper<G> implements Filter<G> {

	private enum FilterType {
		AND, OR;
	}

	private FilterType type;
	private Filter[] filters;

	private MultiFilterWrapper(final FilterType m, final Filter... filters) {
		this.type = m;
		this.filters = filters;
	}

	public boolean accept(final ResultBinding<G> binding) {
		switch (type) {
		case AND:
			for (final Filter f : filters) {
				if (!f.accept(binding)) {
					return false;
				}
			}
			return true;
		case OR:
			for (final Filter f : filters) {
				if (f.accept(binding)) {
					return true;
				}
			}
			return false;
		default:
			throw new RuntimeException("Filter type not supported : " + type);
		}
	}

	public static Filter and(final Filter... filters) {
		return new MultiFilterWrapper(FilterType.AND, filters);
	}

	public static Filter or(final Filter... filters) {
		return new MultiFilterWrapper(FilterType.OR, filters);
	}
}
