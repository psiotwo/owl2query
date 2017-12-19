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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.cvut.kbss.owl2query.model.OWL2Ontology;
import cz.cvut.kbss.owl2query.model.QueryResult;
import cz.cvut.kbss.owl2query.model.ResultBinding;
import cz.cvut.kbss.owl2query.model.Term;
import cz.cvut.kbss.owl2query.model.VarType;
import cz.cvut.kbss.owl2query.model.Variable;

class SimpleRollingUpExec<G> extends AbstractABoxEvaluator<G> {
	private static final Logger LOG = OWL2QueryEngine.log;

	@Override
	public QueryResult<G> execABoxQuery(final InternalQuery<G> q) {
		final QueryResult<G> results = new QueryResultImpl<G>(q);
		final OWL2Ontology<G> onto = q.getOntology();

		if (q.getDistVars().isEmpty()) {
			if (OWL2QueryEngine.execBooleanABoxQuery(q)) {
				results.add(new ResultBindingImpl<G>());
			}
		} else {
			final Map<Variable<G>, Set<? extends G>> varBindings = new HashMap<Variable<G>, Set<? extends G>>();

			// roll-up the query into all variables
			for (final Variable<G> currVar : q
					.getDistVarsOfTypes(VarType.INDIVIDUAL)) {
				varBindings.put(currVar, onto.getInstances(q.rollUpTo(currVar,
						Collections.<Term<G>> emptySet()), false));
			}

			if (LOG.isLoggable(Level.FINER))
				LOG.finer("Variable bindings: " + varBindings);

			final Iterator<ResultBinding<G>> i = new BindingIterator<G>(
					varBindings, onto.getFactory());

			boolean hasLiterals = !q.getDistVarsOfTypes(
					VarType.INDIVIDUAL_OR_LITERAL, VarType.LITERAL).isEmpty();

			if (hasLiterals) {
				while (i.hasNext()) {
					final ResultBinding<G> b = i.next();

					final Iterator<ResultBinding<G>> l = new LiteralIterator<G>(
							q, b, onto.getFactory());
					while (l.hasNext()) {
						ResultBinding<G> mappy = l.next();
						boolean queryTrue = OWL2QueryEngine
								.execBooleanABoxQuery(q.apply(mappy));
						if (queryTrue)
							results.add(mappy);
					}
				}
			} else {
				while (i.hasNext()) {
					final ResultBinding<G> b = i.next();
					boolean queryTrue = (q.getDistVarsOfTypes(
							VarType.INDIVIDUAL, VarType.INDIVIDUAL_OR_LITERAL)
							.size() == 1)
							|| OWL2QueryEngine.execBooleanABoxQuery(q.apply(b));
					if (queryTrue)
						results.add(b);
				}
			}
		}

		return results;
	}
}
