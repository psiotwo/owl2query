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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.cvut.kbss.owl2query.model.ResultBinding;
import cz.cvut.kbss.owl2query.model.Term;
import cz.cvut.kbss.owl2query.model.Variable;

class StaticCostQueryPlan<G> extends QueryPlan<G> {
	private static final Logger log = Logger
			.getLogger(StaticCostQueryPlan.class.getName());

	private List<QueryAtom<G>> sortedAtoms;

	private int index;

	private int size;

	private QueryCost<G> cost;

	private double minCost;
	
	public StaticCostQueryPlan(InternalQuery<G> query) {
		super(query);

		QuerySizeEstimator.computeSizeEstimate(query);

		index = 0;
		size = query.getAtoms().size();
		cost = new QueryCost<G>(query.getOntology());
		sortedAtoms = null;

		if (size == 0) {
			return;
		} else if (size == 1) {
			sortedAtoms = query.getAtoms();
		} else {
			minCost = chooseOrdering(new ArrayList<QueryAtom<G>>(query
					.getAtoms()), new ArrayList<QueryAtom<G>>(size),
					new HashSet<Variable<G>>(), false, Double.POSITIVE_INFINITY);

			if (log.isLoggable(Level.FINE)) {
				log.fine("WINNER : Cost=" + minCost + " ,atoms=" + sortedAtoms);
			}
		}
	}

	public double getMinCost() {
		return minCost;
	}
	
	private double chooseOrdering(List<QueryAtom<G>> atoms,
			List<QueryAtom<G>> orderedAtoms, Set<Variable<G>> boundVars,
			boolean notOptimal, double minCost) {
		if (atoms.isEmpty()) {
			if (notOptimal) {
				if (sortedAtoms == null) {
					sortedAtoms = new ArrayList<QueryAtom<G>>(orderedAtoms);
				}
			} else {
				double queryCost = estimate(orderedAtoms,
						new HashSet<Term<G>>());
				if (log.isLoggable(Level.FINER)) {
					log.finer("Cost " + queryCost + " for " + orderedAtoms);
				}
				if (queryCost < minCost) {
					sortedAtoms = new ArrayList<QueryAtom<G>>(orderedAtoms);
					minCost = queryCost;
				}
			}

			return minCost;
		}

		for (int i = 0; i < atoms.size(); i++) {
			final QueryAtom<G> atom = atoms.get(i);

			boolean newNonOptimal = notOptimal;
			final Set<Variable<G>> newBoundVars = new HashSet<Variable<G>>(
					boundVars);
			// TODO reorder UV atoms after all class and property variables are
			// bound.

			if (!atom.isGround()) {
				int boundCount = 0;
				int unboundCount = 0;

				for (Term<G> a : atom.getArguments()) {
					if (a.isVariable()) {
						if (newBoundVars.add(a.asVariable())) {
							unboundCount++;
						} else {
							boundCount++;
						}
					}
				}

				// if( unboundCount > 0 && atom.getPredicate().equals(
				// QueryPredicate.Not ) ) {
				// log.fine( "Unbound vars for not" );
				// continue;
				// }

				if (boundCount == 0 && newBoundVars.size() > unboundCount) {
					if (sortedAtoms != null) {
						if (log.isLoggable(Level.FINEST)) {
							log.finest("Stop at not optimal ordering");
						}
						continue;
					} else {
						if (log.isLoggable(Level.FINEST)) {
							log
									.finest("Continue not optimal ordering, no solution yet.");
						}
						newNonOptimal = true;
					}
				}
			}

			atoms.remove(atom);
			orderedAtoms.add(atom);

			if (log.isLoggable(Level.FINEST)) {
				log.finest("Atom[" + i + "/" + atoms.size() + "] " + atom
						+ " from " + atoms + " to " + orderedAtoms);
			}

			minCost = chooseOrdering(atoms, orderedAtoms, newBoundVars,
					newNonOptimal, minCost);

			atoms.add(i, atom);
			orderedAtoms.remove(orderedAtoms.size() - 1);
		}

		return minCost;
	}

	private double estimate(final List<QueryAtom<G>> atoms,
			final Collection<Term<G>> bound) {
		double totalCost = 1.0;

		int n = atoms.size();

		Set<Term<G>> lastBound = new HashSet<Term<G>>(bound);
		final List<Set<Term<G>>> boundList = new ArrayList<Set<Term<G>>>(n);
		for (final QueryAtom<G> atom : atoms) {
			boundList.add(lastBound);
			lastBound = new HashSet<Term<G>>(lastBound);
			lastBound.addAll(atom.getArguments());
		}

		for (int i = n - 1; i >= 0; i--) {
			QueryAtom<G> atom = atoms.get(i);

			cost.estimate(atom, boundList.get(i));

			// System.out.println("EST: " + atom +
			// " with "+boundList.get(i)+", SC=" + cost.getStaticCost() +
			// ", BC="+cost.getBranchCount());

			totalCost = cost.getStaticCost() + cost.getBranchCount()
					* totalCost;
		}

		return totalCost;
	}

	@Override
	public QueryAtom<G> next(final ResultBinding<G> binding) {
		final QueryAtom<G> a = sortedAtoms.get(index++);
		if (a.isGround()) {
			return a;
		} else {
			return a.apply(binding, query.getOntology());
		}
	}

	@Override
	public boolean hasNext() {
		return index < size;
	}

	@Override
	public void back() {
		index--;
	}

	@Override
	public void reset() {
		index = 0;
	}
}
