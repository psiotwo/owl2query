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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import cz.cvut.kbss.owl2query.model.GroundTerm;
import cz.cvut.kbss.owl2query.model.OWL2Ontology;
import cz.cvut.kbss.owl2query.model.Term;
import cz.cvut.kbss.owl2query.model.VarType;
import cz.cvut.kbss.owl2query.model.Variable;

class VariableImpl<T> implements Variable<T> {

	private String id = null;

	public VariableImpl(final String string) {
		this.id = string;
	}

	
	public boolean isVariable() {
		return true;
	}

	
	public String getName() {
		return id;
	}

	
	public GroundTerm<T> asGroundTerm() {
		throw new IllegalArgumentException();
	}

	
	public Variable<T> asVariable() {
		return this;
	}

	
	public boolean isGround() {
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VariableImpl other = (VariableImpl) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "?" + id;
	}

	
	public String shortForm() {
		if (id.startsWith("?")) {
			return "_:" + id.substring(1);
		} else {
			return "?" + id;
		}
	}
	
	
	
	public Term<T> apply(Map<Variable<T>, GroundTerm<T>> binding, OWL2Ontology<T> ont) {
		if (binding.containsKey(this)) {
			return binding.get(this);
		} else {
			return this;
		}
	}

	
	public Set<Variable<T>> getVariables() {
		return Collections.<Variable<T>> singleton(this);
	}

	
	public VarType getVariableType(Variable<T> var) {
		return null;
	}
	
}
