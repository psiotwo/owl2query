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
package cz.cvut.kbss.owl2query.parser.arq;

import cz.cvut.kbss.owl2query.model.OWL2Query;
import cz.cvut.kbss.owl2query.model.OWL2Rule;

class OWL2RuleImpl<G> implements OWL2Rule<G> {

	private OWL2Query<G> head;
	private OWL2Query<G> body;

	public OWL2RuleImpl(OWL2Query<G> head, OWL2Query<G> body) {
		this.head = head;
		this.body = body;
	}

	@Override
	public OWL2Query<G> getBody() {
		return body;
	}

	@Override
	public OWL2Query<G> getHead() {
		return head;
	}
	
	@Override
	public String toString() {
		return getBody().toString() + " ==> " + getHead().toString();
	}
}
