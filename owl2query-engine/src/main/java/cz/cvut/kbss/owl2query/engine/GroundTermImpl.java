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

class GroundTermImpl<T> implements GroundTerm<T> {

	private T groundTerm;
	
	/**
	 * Constructs a URI
	 */
	public GroundTermImpl(final T groundTerm) {
		this.groundTerm = groundTerm;
	}

	public boolean isVariable() {
		return false;
	}

	public T getWrappedObject() {
		return groundTerm;
	}

	public GroundTerm<T> asGroundTerm() {
		return this;
	}

	public Variable<T> asVariable() {
		throw new IllegalArgumentException();
	}

	public boolean isGround() {
		return true;
	}

	@Override
	public int hashCode() {
		return groundTerm.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final GroundTermImpl<T> other = (GroundTermImpl<T>) obj;
		if (groundTerm == null) {
			if (other.groundTerm != null)
				return false;
		} else if (!groundTerm.equals(other.groundTerm))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return groundTerm + "";
	}

	public Term<T> apply(Map<Variable<T>, GroundTerm<T>> binding, OWL2Ontology<T> ont) {
		return this;
	}

	public Set<Variable<T>> getVariables() {
		return Collections.emptySet();
	}

	public VarType getVariableType(Variable<T> var) {
		return null;
	}

	public String shortForm() {
		if (groundTerm.toString().startsWith("http://")
				|| groundTerm.toString().startsWith("file://")) {

			if ( groundTerm.toString().contains("#")) {
				return groundTerm.toString().substring(
						groundTerm.toString().lastIndexOf("#")+1);
			} else {
				return groundTerm.toString().substring(
						groundTerm.toString().lastIndexOf("/")+1);
			}
		} else {
			return groundTerm.toString();
		}
	}
}
