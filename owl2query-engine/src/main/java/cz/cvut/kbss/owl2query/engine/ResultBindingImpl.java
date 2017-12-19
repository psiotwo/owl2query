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

import java.util.LinkedHashMap;
import java.util.Map;

import cz.cvut.kbss.owl2query.model.GroundTerm;
import cz.cvut.kbss.owl2query.model.ResultBinding;
import cz.cvut.kbss.owl2query.model.Variable;

@SuppressWarnings("serial")
class ResultBindingImpl<G> extends LinkedHashMap<Variable<G>, GroundTerm<G>>
		implements ResultBinding<G> {

	ResultBindingImpl() {
	}

	ResultBindingImpl(final Map<Variable<G>, GroundTerm<G>> bindings) {
		super(bindings);
	}

	@Override
	public ResultBinding<G> clone() {
		return new ResultBindingImpl<G>(this);
	}
}
