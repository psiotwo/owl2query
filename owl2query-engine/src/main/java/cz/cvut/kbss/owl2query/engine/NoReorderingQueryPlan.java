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
import java.util.ListIterator;

import cz.cvut.kbss.owl2query.model.ResultBinding;

class NoReorderingQueryPlan<G> extends QueryPlan<G> {

	private ListIterator<QueryAtom<G>> i;

	public NoReorderingQueryPlan(InternalQuery<G> query) {
		super(query);

		i = query.getAtoms().listIterator();
	}

	@Override
	public QueryAtom<G> next(final ResultBinding<G> binding) {
		final QueryAtom<G> a = i.next();

		if (a.isGround()) {
			return a;
		} else {
			return a.apply(binding, query.getOntology());
		}
	}

	@Override
	public boolean hasNext() {
		return i.hasNext();
	}

	@Override
	public void back() {
		i.previous();
	}

	@Override
	public void reset() {
		i = query.getAtoms().listIterator();
	}
}
