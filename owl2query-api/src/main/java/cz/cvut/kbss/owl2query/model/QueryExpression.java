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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class QueryExpression<T> implements Term<T> {

	protected final List<Term<T>> terms;

	public QueryExpression(final Term<T>... terms) {
		this.terms = Arrays.asList(terms);
	}

	public abstract Term<T> apply(final Map<Variable<T>, GroundTerm<T>> binding, OWL2Ontology<T> ont);

	public GroundTerm<T> asGroundTerm() {
		throw new UnsupportedOperationException();
	}

	public Variable<T> asVariable() {
		throw new UnsupportedOperationException();
	}

	public Set<Variable<T>> getVariables() {
		Set<Variable<T>> c = new HashSet<Variable<T>>();
		for (final Term<T> tx : terms) {
			c.addAll(tx.getVariables());
		}
		return c;
	}

	public boolean isGround() {
		return false;
	}

	public boolean isVariable() {
		return false;
	}
	
	public String shortForm() {
		return toString();
	}
} 
