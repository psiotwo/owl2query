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

import java.util.Map;
import java.util.Set;

public interface Term<T> {

	/**
	 * Returns true, if the term is a variable.
	 */
	public boolean isVariable();

	/**
	 * Returns true, if the term is an expression that represents a ground term,
	 * i.e. no variable occurs in its body
	 */
	public boolean isGround();

	public Variable<T> asVariable();

	public GroundTerm<T> asGroundTerm();

	public Set<Variable<T>> getVariables();

	public VarType getVariableType(final Variable<T> var);

	public Term<T> apply(Map<Variable<T>, GroundTerm<T>> binding, OWL2Ontology<T> ont);
	
	public String shortForm();
}
