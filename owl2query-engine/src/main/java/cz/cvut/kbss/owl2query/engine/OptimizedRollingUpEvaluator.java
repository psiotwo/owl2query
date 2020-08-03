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

import cz.cvut.kbss.owl2query.model.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class OptimizedRollingUpEvaluator<G> extends AbstractABoxEvaluator<G> {
	private static final Logger log = OWL2QueryEngine.log;

	@Override
	public QueryResult<G> execABoxQuery(final InternalQuery<G> q) {
		final QueryResult<G> results = new QueryResultImpl<>(q);
		final OWL2Ontology<G> kb = q.getOntology();

		if (q.getDistVars().isEmpty()) {
			if (OWL2QueryEngine.execBooleanABoxQuery(q)) {
				results.add(new ResultBindingImpl<>());
			}
		} else {
			final Map<Variable<G>, Set<? extends G>> varBindings = new HashMap<>();

			for (final Variable<G> currVar : q
					.getDistVarsOfTypes(VarType.INDIVIDUAL)) {
				varBindings.put(currVar, kb.getInstances(q.rollUpTo(currVar, Collections.emptySet()),
						false));
			}

			if (log.isLoggable(Level.FINER))
				log.finer("Var bindings: " + varBindings);

			final List<Variable<G>> varList = new ArrayList<>(
					varBindings.keySet());

			final Map<Variable<G>, Collection<ResultBinding<G>>> goodLists = new HashMap<>();

			final Variable<G> first = varList.get(0);
			final Collection<ResultBinding<G>> c = new HashSet<>();

			for (final G a : varBindings.get(first)) {
				final ResultBinding<G> bind = new ResultBindingImpl<>();
				bind.put(first, kb.getFactory().wrap(a));
				c.add(bind);
			}

			goodLists.put(first, c);

			Collection<ResultBinding<G>> previous = goodLists.get(first);
			for (int i = 1; i < varList.size(); i++) {
				final Variable<G> next = varList.get(i);

				final Collection<ResultBinding<G>> newBindings = new HashSet<>();

				for (final ResultBinding<G> binding : previous) {
					for (final G testBind : varBindings.get(next)) {
						final ResultBinding<G> bindingCandidate = binding
								.clone();

						bindingCandidate.put(next, kb.getFactory().wrap(
								testBind));

						boolean queryTrue = OWL2QueryEngine
								.execBooleanABoxQuery(q.apply(bindingCandidate));
						if (queryTrue) {
							newBindings.add(bindingCandidate);
							if (log.isLoggable(Level.FINER)) {
								log.finer("Accepted binding: "
										+ bindingCandidate);
							}
						} else {
							if (log.isLoggable(Level.FINER)) {
								log.finer("Rejected binding: "
										+ bindingCandidate);
							}
						}
					}
				}

				previous = newBindings;
			}

			// no var. should be marked as both INDIVIDUAL and LITERAL in an
			// ABox query.
			boolean hasLiterals = !q.getDistVarsOfTypes(VarType.LITERAL,
					VarType.INDIVIDUAL_OR_LITERAL).isEmpty();

			if (hasLiterals) {
				for (final ResultBinding<G> b : previous) {
					for (final Iterator<ResultBinding<G>> i = new LiteralIterator<>(
							q, b, kb.getFactory()); i.hasNext();) {
						results.add(i.next());
					}
				}
			} else {
				for (final ResultBinding<G> b : previous) {
					results.add(b);
				}
			}
		}
		return results;
	}
}
