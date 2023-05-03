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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.cvut.kbss.owl2query.UnsupportedQueryException;
import cz.cvut.kbss.owl2query.model.Configuration;
import cz.cvut.kbss.owl2query.model.GroundTerm;
import cz.cvut.kbss.owl2query.model.Hierarchy;
import cz.cvut.kbss.owl2query.model.OWL2Ontology;
import cz.cvut.kbss.owl2query.model.OWL2QueryFactory;
import cz.cvut.kbss.owl2query.model.QueryResult;
import cz.cvut.kbss.owl2query.model.ResultBinding;
import cz.cvut.kbss.owl2query.model.Term;
import cz.cvut.kbss.owl2query.model.VarType;
import cz.cvut.kbss.owl2query.model.Variable;
import cz.cvut.kbss.owl2query.util.DisjointSet;

class CombinedQueryEngine<G> implements QueryEvaluator<G> {
	public static final Logger log = Logger.getLogger(CombinedQueryEngine.class
			.getName());

	private OWL2Ontology<G> kb;

	private OWL2QueryFactory<G> f;

	private QueryPlan<G> plan;

	private InternalQuery<G> query;

	private QueryResult<G> result;

	private Set<Variable<G>> downMonotonic;

	private QueryPlan<G> getExecutionPlan(InternalQuery<G> query) {
		if (Configuration.SAMPLING_RATIO == 0) {
			if (log.isLoggable(Level.FINE)) {
				log.fine("Using no reordering query plan.");
			}
			return new NoReorderingQueryPlan<>(query);
		} else if (query.getAtoms().size() <= Configuration.STATIC_REORDERING_LIMIT) {
			if (log.isLoggable(Level.FINE)) {
				log.fine("Using full query plan.");
			}
			return new StaticCostQueryPlan<>(query);
		} else {
			if (log.isLoggable(Level.FINE)) {
				log.fine("Using incremental query plan.");
			}
			return new IncrementalQueryPlan<>(query);
		}
	}

	private void prepare(InternalQuery<G> query) {
		if (log.isLoggable(Level.FINE)) {
			log.fine("Preparing plan ...");
		}

		this.kb = query.getOntology();
		this.f = kb.getFactory();
		if (kb == null) {
			throw new RuntimeException("No input data set is given for query!");
		}

		this.result = new QueryResultImpl<>(query);

		this.query = setupCores(query);

		if (log.isLoggable(Level.FINE)) {
			log.fine("After setting-up cores : " + this.query);
		}

		this.plan = getExecutionPlan(this.query);
		this.plan.reset();

		if (Configuration.OPTIMIZE_DOWN_MONOTONIC) {
			// TODO use down monotonic variables for implementation of
			// DirectType atom
			downMonotonic = new HashSet<>();
			setupDownMonotonicVariables(this.query);
			if (log.isLoggable(Level.FINE)) {
				log.fine("Variables to be optimized : " + downMonotonic);
			}
		}
	}

	private InternalQuery<G> setupCores(final InternalQuery<G> query) {
		final Iterator<Variable<G>> undistVarIterator = query.getUndistVars()
				.iterator();
		if (!undistVarIterator.hasNext()) {
			return query;
		}
		final DisjointSet<Object> coreVertices = new DisjointSet<>();

		final InternalQuery<G> transformedQuery = query
				.apply(new ResultBindingImpl<>());

		while (undistVarIterator.hasNext()) {
			final Term<G> a = undistVarIterator.next();

			coreVertices.add(a);

			for (final QueryAtom<G> atom : query.findAtoms(
					QueryPredicate.PropertyValue, null, a, null)) {
				coreVertices.add(atom);
				coreVertices.union(a, atom);

				final Term<G> a2 = atom.getArguments().get(2);
				if (query.getUndistVars().contains(a2)) {
					coreVertices.add(a2);
					coreVertices.union(a, a2);
				}
				transformedQuery.remove(atom);
			}
			for (final QueryAtom<G> atom : query.findAtoms(
					QueryPredicate.PropertyValue, null, null, a)) {
				coreVertices.add(atom);
				coreVertices.union(a, atom);

				final Term<G> a2 = atom.getArguments().get(0);
				if (query.getUndistVars().contains(a2)) {
					coreVertices.add(a2);
					coreVertices.union(a, a2);
				}
				transformedQuery.remove(atom);
			}

			for (final QueryAtom<G> atom : query.findAtoms(QueryPredicate.Type,
					null, a)) {
				coreVertices.add(atom);
				coreVertices.union(a, atom);
				transformedQuery.remove(atom);
			}
		}

		final Map<Variable<G>, Set<G>> map = new HashMap<>();
		final Map<Variable<G>, InternalQuery<G>> map2 = new HashMap<>();

		for (final Set<Object> set : coreVertices.getEquivalenceSets()) {
			final InternalQuery<G> coreQuery = new QueryImpl<>(kb);
			for (final Object a : set) {
				if (a instanceof QueryAtom) {
					final QueryAtom<G> queryAtom = (QueryAtom<G>) a;
					transformedQuery.remove(queryAtom);
					coreQuery.add(queryAtom);
					for (final Term<G> t : queryAtom.getArguments()) {
						if (query.getDistVars().contains(t)) {
							coreQuery.addDistVar(t.asVariable());
						}
					}

				}
			}

			for (final Variable<G> distVar : coreQuery.getDistVars()) {
				Set<G> s = map.get(distVar);
				InternalQuery<G> s2 = map2.get(distVar);

				if (s == null) {
					s = new HashSet<>();
					map.put(distVar, s);
				}

				if (s2 == null) {
					map2.put(distVar, coreQuery.apply(Collections
							.<Variable<G>, Term<G>> emptyMap()));
				} else {
					for (final QueryAtom<G> atom : coreQuery.getAtoms()) {
						s2.add(atom);
					}

					s2.addDistVar(distVar);
					s2.addResultVar(distVar);
				}

				s.add(coreQuery.rollUpTo(distVar, new HashSet<>()));
			}
		}

		for (final Variable<G> var : map.keySet()) {
			transformedQuery
					.Core(var, f.wrap(f.objectIntersectionOf(map.get(var))),
							map2.get(var));
			transformedQuery.addDistVar(var);
			transformedQuery.addResultVar(var);
		}

		return transformedQuery;
	}

	// down-monotonic variables = Class variables in Type atoms and Property
	// variables in PropertyValue atoms
	private void setupDownMonotonicVariables(final InternalQuery<G> query) {
		for (final QueryAtom<G> atom : query.getAtoms()) {
			Term<G> arg;

			switch (atom.getPredicate()) {
			case PropertyValue:
			case Type:
				arg = atom.getArguments().get(0);
				if (arg.isVariable()) {
					downMonotonic.add(arg.asVariable());
				}
				break;
			default:
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public QueryResult<G> evaluate(InternalQuery<G> query) {
		if (log.isLoggable(Level.FINE)) {
			log.fine("Executing query " + query);
		}

		prepare(query);

		if ( query.getValues() == null || query.getValues().isEmpty() ) {
			exec(new ResultBindingImpl<>());
		} else {
			for (ResultBinding<G> b : query.getValues()) {
				exec(b);
			}
		}

		return result;
	}

	private void exec(ResultBinding<G> bindingX) {
		if (!plan.hasNext()) {
			// TODO if result vars are not same as dist vars.
			if (!bindingX.isEmpty() || result.isEmpty()) {
				if (log.isLoggable(Level.FINE)) {
					log.fine("Found binding: " + bindingX);
				}

				if (!result.getResultVars().containsAll(bindingX.keySet())) {
					ResultBinding<G> newBinding = new ResultBindingImpl<>();
					for (Variable<G> var : result.getResultVars()) {
						GroundTerm<G> value = bindingX.get(var);
						newBinding.put(var, value);
					}
					bindingX = newBinding;
				}

				result.add(bindingX);
			}
			if (log.isLoggable(Level.FINER)) {
				log.finer("Returning ... binding=" + bindingX);
			}
			return;
		}

		QueryAtom<G> atom3 = plan.next(bindingX);

		final BindingIterator<G> it = resolveComplexExpressionVariables(atom3
				.getArguments());

		while (it.hasNext()) {
			QueryAtom<G> current;

			ResultBinding<G> binding = bindingX.clone();
			binding.putAll(it.next());

			current = atom3.apply(binding, kb);

			if (log.isLoggable(Level.FINER)) {
				log.finer("Evaluating " + current);
			}

			if (current.isGround()) {
				if (OWL2QueryEngine.checkGround(current, kb)) {
					exec(binding);
				}
			} else {

				final List<Term<G>> arguments = current.getArguments();

				boolean direct = false;
				boolean strict = false;

				switch (current.getPredicate()) {

				case DirectType:
					direct = true;
				case Type: // TODO implementation of downMonotonic
					// vars
					final Term<G> tC = arguments.get(0);
					final Term<G> tI = arguments.get(1);

					Set<? extends G> instanceCandidates = null;
					if (tI.equals(tC)) {
						instanceCandidates = kb.getIndividuals().size() < kb
								.getClasses().size() ? kb.getIndividuals() : kb
								.getClasses();
						for (final G ic : instanceCandidates) {
							if (kb.isTypeOf(ic, ic, direct)) {
								final ResultBinding<G> candidateBinding = binding
										.clone();
								candidateBinding.put(tI.asVariable(),
										f.wrap(ic));
								exec(candidateBinding);
							}
						}
					} else {
						final Set<? extends G> classCandidates;

						if (tC.isGround()) {
							classCandidates = Collections.singleton(tC
									.asGroundTerm().getWrappedObject());
							instanceCandidates = kb.getInstances(tC
									.asGroundTerm().getWrappedObject(), direct);
						} else if (tI.isGround()) {
							classCandidates = kb.getTypes(tI.asGroundTerm()
									.getWrappedObject(), direct); // TODO
							instanceCandidates = Collections.singleton(tI
									.asGroundTerm().getWrappedObject());
						} else {
							classCandidates = kb.getClasses();
						}

						// explore all possible bindings
						boolean loadInstances = (instanceCandidates == null);
						for (final G cls : classCandidates) {
							if (loadInstances) {
								instanceCandidates = kb.getInstances(cls,
										direct);
							}
							for (final G inst : instanceCandidates) {
								runNext(binding, arguments, cls, inst);
							}
						} // finish explore bindings
					}
					break;

				case PropertyValue: // TODO implementation of downMonotonic
					// vars
					final Term<G> pvP = arguments.get(0);
					final Term<G> pvI = arguments.get(1);
					final Term<G> pvIL = arguments.get(2);

					Collection<? extends G> propertyCandidates;
					Collection<? extends G> subjectCandidates = null;
					Collection<? extends G> objectCandidates = null;

					boolean loadProperty;
					boolean loadSubjects;
					boolean loadObjects;

					if (pvP.isGround()) {
						propertyCandidates = Collections.singleton(pvP
								.asGroundTerm().getWrappedObject());
						if (pvI.isGround()) {
							subjectCandidates = Collections.singleton(pvI
									.asGroundTerm().getWrappedObject());

							objectCandidates = kb.getPropertyValues(pvP
									.asGroundTerm().getWrappedObject(), pvI
									.asGroundTerm().getWrappedObject());
						} else if (pvIL.isGround()) {
							objectCandidates = Collections.singleton(pvIL
									.asGroundTerm().getWrappedObject());
							subjectCandidates = kb.getIndividualsWithProperty(
									pvP.asGroundTerm().getWrappedObject(), pvIL
											.asGroundTerm().getWrappedObject());
						}
						loadProperty = false;
					} else {
						if (pvI.isGround()) {
							subjectCandidates = Collections.singleton(pvI
									.asGroundTerm().getWrappedObject());
						}

						if (pvIL.isGround()) {
							objectCandidates = Collections.singleton(pvIL
									.asGroundTerm().getWrappedObject());
						}

						if (query.getDistVarsOfTypes(VarType.OBJECT_PROPERTY)
								.contains(pvP)) {
							propertyCandidates = kb.getObjectProperties();
						} else if (query.getDistVarsOfTypes(
								VarType.DATA_PROPERTY).contains(pvP)) {
							propertyCandidates = kb.getDataProperties();
						} else {
							propertyCandidates = getObjectAndDataProperties();
						}
						loadProperty = true;
					}

					loadSubjects = (subjectCandidates == null);
					loadObjects = (objectCandidates == null);

					for (final G property : propertyCandidates) {
						// TODO replaG this nasty if-cascade with some map
						// for
						// var
						// bindings.
						if (loadObjects && loadSubjects) {
							if (pvI.equals(pvIL)) {
								if (pvI.equals(pvP)) {
									if (!kb.hasPropertyValue(property,
											property, property)) {
										continue;
									}
									runNext(binding, arguments, property,property,property);
								} else {
									for (final G i : kb.getIndividuals()) {
										if (!kb.hasPropertyValue(property, i, i)) {
											continue;
										}
										runNext(binding, arguments, property,i,i);
									}
								}
							} else {
								if (pvI.equals(pvP)) {
									for (final G i : kb.getIndividuals()) {
										if (!kb.hasPropertyValue(property,
												property, i)) {
											continue;
										}
										runNext(binding, arguments, property,property,i);
									}
								} else if (pvIL.equals(pvP)) {
									for (final G i : kb.getIndividuals()) {
										if (!kb.hasPropertyValue(property, i,
												property)) {
											continue;
										}
										runNext(binding, arguments, property,i,property);
									}
								} else {
									for (final G subject : kb.getIndividuals()) {
										for (final G object : kb
												.getPropertyValues(property,
														subject)) {
											runNext(binding, arguments, property,subject,object);
										}
									}
								}
							}
						} else if (loadObjects) {
							// subject is known.
							if (pvP.equals(pvIL)) {
								if (!kb.hasPropertyValue(subjectCandidates
										.iterator().next(), property, property)) {
									// terminate
									subjectCandidates = Collections.emptySet();
								}
							}

							for (final G subject : subjectCandidates) {
								for (final G object : kb.getPropertyValues(
										property, subject)) {
									runNext(binding, arguments, property,
											subject, object);
								}
							}
						} else {
							// object is known.
							for (final G object : objectCandidates) {
								if (loadSubjects) {
									if (pvI.equals(pvP)) {
										if (kb.hasPropertyValue(property,
												property, object)) {
											subjectCandidates = Collections
													.singleton(property);
										} else {
											// terminate
											subjectCandidates = Collections
													.emptySet();
										}
									} else {
										subjectCandidates = new HashSet<>(
												kb.getIndividualsWithProperty(
														property, object));
									}
								}

								for (final G subject : subjectCandidates) {
									if (loadProperty
											&& !kb.hasPropertyValue(property,
													subject, object)) {
										continue;
									}

									runNext(binding, arguments, property,
											subject, object);
								}
							}
						}
					} // finish visiting non-ground triple.
					break;

				case SameAs:
					// optimize - merge nodes
					final Term<G> saI1 = arguments.get(0);
					final Term<G> saI2 = arguments.get(1);

					for (final G known : getSymmetricCandidates(
							VarType.INDIVIDUAL, saI1, saI2)) {

						final Collection<? extends G> dependents;

						if (saI1.equals(saI2)) {
							dependents = Collections.singleton(known);
						} else {
							dependents = kb.getSames(known);
						}

						for (final G dependent : dependents) {
							runSymetricCheck(current, saI1, known, saI2,
									dependent, binding);
						}
					}
					break;

				case DifferentFrom:
					// optimize - different from map
					final Term<G> dfI1 = arguments.get(0);
					final Term<G> dfI2 = arguments.get(1);

					if (!dfI1.equals(dfI2)) {
						for (final G known : getSymmetricCandidates(
								VarType.INDIVIDUAL, dfI1, dfI2)) {
							for (final G dependent : kb.getDifferents(known)) {
								runSymetricCheck(current, dfI1, known, dfI2,
										dependent, binding);
							}
						}
					} else {
						if (log.isLoggable(Level.FINER)) {
							log.finer("Atom "
									+ current
									+ "cannot be satisfied in any consistent ontology.");
						}
					}
					break;

				// TBOX ATOMS
				case DirectSubClassOf:
					direct = true;
				case StrictSubClassOf:
					strict = true;
				case SubClassOf:
					final Term<G> scLHS = arguments.get(0);
					final Term<G> scRHS = arguments.get(1);

					if (scLHS.equals(scRHS) && scLHS.isVariable()) {
						// TODO optimization for downMonotonic variables
						for (final G ic : kb.getClasses()) {
							runNext(binding, arguments, ic, ic);
						}
					} else {
						final boolean lhsDM = isDownMonotonic(scLHS);
						final boolean rhsDM = isDownMonotonic(scRHS);

						if (lhsDM || rhsDM) {
							downMonotonic(kb.getClassHierarchy(),
									kb.getClasses(), lhsDM, scLHS, scRHS,
									binding, direct, strict);
						} else {
							final Collection<G> lhsCandidates;
							Collection<G> rhsCandidates = null;

							if (scLHS.isGround()) {
								final G scLHSGT = scLHS.asGroundTerm()
										.getWrappedObject();

								lhsCandidates = Collections.singleton(scLHSGT);
								rhsCandidates = new HashSet<>(kb
										.getClassHierarchy().getSupers(scLHSGT,
												direct));

								rhsCandidates.addAll(kb.getClassHierarchy()
										.getEquivs(scLHSGT));

								if (strict) {
									rhsCandidates.removeAll(kb
											.getClassHierarchy().getEquivs(
													scLHSGT));
								} else if (!kb.isComplexClass(scLHSGT)) {
									rhsCandidates.add(scLHSGT);
								}
							} else if (scRHS.isGround()) {
								final G scRHSGT = scRHS.asGroundTerm()
										.getWrappedObject();

								rhsCandidates = Collections.singleton(scRHSGT);
								lhsCandidates = new HashSet<>(kb
										.getClassHierarchy().getSubs(scRHSGT,
												direct));

								lhsCandidates.addAll(kb.getClassHierarchy()
										.getEquivs(scRHSGT));

								if (strict) {
									lhsCandidates.removeAll(kb
											.getClassHierarchy().getEquivs(
													scRHSGT));
								} else if (!kb.isComplexClass(scRHSGT)) {
									lhsCandidates.add(scRHSGT);
								}
							} else {
								lhsCandidates = new HashSet<>(kb.getClasses());
							}

							boolean reload = (rhsCandidates == null);
							for (final G subject : lhsCandidates) {
								if (reload) {
									rhsCandidates = new HashSet<>(kb
											.getClassHierarchy().getSupers(
													subject, direct));
									if (strict) {
										rhsCandidates.removeAll(kb
												.getClassHierarchy().getEquivs(
														subject));
									} else if (!kb.isComplexClass(subject)) {
										rhsCandidates.add(subject);
									}
								}
								for (final G object : rhsCandidates) {
									runNext(binding, arguments, subject, object);
								}
							}
						}
					}
					break;

				case EquivalentClass: // TODO implementation of
					// downMonotonic
					// vars
					final Term<G> eqcLHS = arguments.get(0);
					final Term<G> eqcRHS = arguments.get(1);

					for (final G known : getSymmetricCandidates(VarType.CLASS,
							eqcLHS, eqcRHS)) {
						// TODO optimize - try just one - if success then
						// take
						// all
						// found bindings and extend them for other
						// equivalent
						// classes as well.
						// meanwhile just a simple check below

						final Collection<? extends G> dependents;

						if (eqcLHS.equals(eqcRHS)) {
							dependents = Collections.singleton(known);
						} else {
							dependents = kb.getClassHierarchy()
									.getEquivs(known);
						}

						for (final G dependent : dependents) {
							int size = result.size();

							runSymetricCheck(current, eqcLHS, known, eqcRHS,
									dependent, binding);

							if (result.size() == size) {
								// no binding found, so that there is no
								// need to
								// explore other equivalent classes - they
								// fail
								// as
								// well.
								break;
							}
						}
					}
					break;

				case DisjointWith: // TODO implementation of downMonotonic vars
					final Term<G> dwLHS = arguments.get(0);
					final Term<G> dwRHS = arguments.get(1);

					if (!dwLHS.equals(dwRHS)) {
						// TODO optimizeTBox
						for (final G known : getSymmetricCandidates(
								VarType.CLASS, dwLHS, dwRHS)) {
							for (final G dependent : kb.getClassHierarchy()
									.getDisjoints(known)) {
								runSymetricCheck(current, dwLHS, known, dwRHS,
										dependent, binding);
							}
						}
					} else {
						log.finer("Atom "
								+ current
								+ "cannot be satisfied in any consistent ontology.");
					}
					break;

				case ComplementOf:
					final Term<G> coLHS = arguments.get(0);
					final Term<G> coRHS = arguments.get(1);

					if (!coLHS.equals(coRHS)) {
						// TODO optimizeTBox
						for (final G known : getSymmetricCandidates(
								VarType.CLASS, coLHS, coRHS)) {
							for (final G dependent : kb
									.getClassHierarchy().getEquivs(f.objectComplementOf(known))) {
								runSymetricCheck(current, coLHS, known, coRHS,
										dependent, binding);
							}
						}
					} else {
						log.finer("Atom "
								+ current
								+ "cannot be satisfied in any consistent ontology.");
					}
					break;

				// RBOX ATOMS
				case DirectSubPropertyOf:
					direct = true;
				case StrictSubPropertyOf:
					strict = true;
				case SubPropertyOf:
					final Term<G> spLHS = arguments.get(0);
					final Term<G> spRHS = arguments.get(1);

					final Set<G> allProperties = getObjectAndDataProperties();

					if (spLHS.equals(spRHS)) {
						// TODO optimization for downMonotonic variables
						for (final G ic : allProperties) {
							runNext(binding, arguments, ic, ic);
						}
					} else {
						final boolean lhsDM = isDownMonotonic(spLHS);
						final boolean rhsDM = isDownMonotonic(spRHS);

						if (lhsDM || rhsDM) {
							downMonotonic(kb.getPropertyHierarchy(),
									allProperties, lhsDM, spLHS, spRHS,
									binding, direct, strict);
						} else {
							final Set<G> spLhsCandidates;
							Set<G> spRhsCandidates = null;

							if (spLHS.isGround()) {
								final G spLHSGT = spLHS.asGroundTerm()
										.getWrappedObject();

								spLhsCandidates = Collections
										.singleton(spLHSGT);
								spRhsCandidates = new HashSet<>(kb
										.getPropertyHierarchy().getSupers(
												spLHSGT, direct));
								if (strict) {
									spRhsCandidates.removeAll(kb
											.getPropertyHierarchy().getEquivs(
													spLHSGT));
								} else {
									spRhsCandidates.add(spLHSGT);
								}
							} else if (spRHS.isGround()) {
								final G spRHSGT = spRHS.asGroundTerm()
										.getWrappedObject();

								spRhsCandidates = Collections
										.singleton(spRHSGT);
								spLhsCandidates = new HashSet<>(kb
										.getPropertyHierarchy().getSubs(
												spRHSGT, direct));
								if (strict) {
									spLhsCandidates.removeAll(kb
											.getPropertyHierarchy().getEquivs(
													spRHSGT));
								} else {
									spLhsCandidates.add(spRHSGT);
								}

							} else {
								spLhsCandidates = getObjectAndDataProperties();
							}
							boolean reload = (spRhsCandidates == null);
							for (final G subject : spLhsCandidates) {
								if (reload) {
									spRhsCandidates = new HashSet<>(kb
											.getPropertyHierarchy().getSupers(
													subject, direct));
									if (strict) {
										spRhsCandidates.removeAll(kb
												.getPropertyHierarchy()
												.getEquivs(subject));
									} else {
										spRhsCandidates.add(subject);
									}
								}
								for (final G object : spRhsCandidates) {
									runNext(binding, arguments, subject, object);
								}
							}
						}
					}
					break;

				case EquivalentProperty: // TODO implementation of
					// downMonotonic
					// vars
					final Term<G> eqpLHS = arguments.get(0);
					final Term<G> eqpRHS = arguments.get(1);

					// TODO optimize - try just one - if success then take
					// all
					// found
					// bindings and extend them for other equivalent classes
					// as
					// well.
					// meanwhile just a simple check below
					for (final G known : getSymmetricCandidates(
							VarType.OBJECT_OR_DATA_PROPERTY, eqpLHS, eqpRHS)) {
						final Set<? extends G> dependents;

						if (eqpLHS.equals(eqpRHS)) {
							dependents = Collections.singleton(known);
						} else {
							dependents = kb.getPropertyHierarchy().getEquivs(
									known);
						}

						for (final G dependent : dependents) {
							int size = result.size();
							runSymetricCheck(current, eqpLHS, known, eqpRHS,
									dependent, binding);
							if (result.size() == size) {
								// no binding found, so that there is no
								// need to
								// explore other equivalent classes - they
								// fail
								// as
								// well.
								break;
							}

						}
					}
					break;

				case InverseOf: // TODO implementation of downMonotonic vars
					final Term<G> ioLHS = arguments.get(0);
					final Term<G> ioRHS = arguments.get(1);

					if (!ioLHS.equals(ioRHS)) {
						for (final G known : getSymmetricCandidates(
								VarType.OBJECT_PROPERTY, ioLHS, ioRHS)) {

							// meanwhile workaround
							for (final G dependent : kb.getInverses(known)) {
								runSymetricCheck(current, ioLHS, known, ioRHS,
										dependent, binding);
							}
						}
                        break;
                    }
				case Symmetric:
					runAllPropertyChecks(arguments.get(0).asVariable(),
							kb.getSymmetricProperties(), binding);
					break;

				case ObjectProperty:
					runAllPropertyChecks(arguments.get(0).asVariable(),
							kb.getObjectProperties(), binding);
					break;

				case DatatypeProperty:
					runAllPropertyChecks(arguments.get(0).asVariable(),
							kb.getDataProperties(), binding);
					break;

				case Functional:
					runAllPropertyChecks(arguments.get(0).asVariable(),
							kb.getFunctionalProperties(), binding);
					break;

				case InverseFunctional:
					runAllPropertyChecks(arguments.get(0).asVariable(),
							kb.getInverseFunctionalProperties(), binding);
					break;

				case Transitive:
					runAllPropertyChecks(arguments.get(0).asVariable(),
							kb.getTransitiveProperties(), binding);
					break;

				case Asymmetric:
					runAllPropertyChecks(arguments.get(0).asVariable(),
							kb.getAsymmetricProperties(), binding);
					break;

				case Reflexive:
					runAllPropertyChecks(arguments.get(0).asVariable(),
							kb.getReflexiveProperties(), binding);
					break;

				case Irreflexive:
					runAllPropertyChecks(arguments.get(0).asVariable(),
							kb.getIrreflexiveProperties(), binding);
					break;

				case Core:
					final Core<G> c = (Core<G>) current;
					final Variable<G> var = c.getTerm().asVariable();

					for (final G i : kb.getInstances(c.getRollUp()
							.getWrappedObject(), false)) {
						GroundTerm<G> g = f.wrap(i);

						if (OWL2QueryEngine.execBooleanABoxQuery(query
								.apply(Collections.singletonMap(var, g)))) {
							ResultBinding<G> candidateBinding = binding.clone();
							candidateBinding.put(var, g);
							exec(candidateBinding);
						}
					}
					break;

				case Not:
					NotQueryAtom<G> notAtom = (NotQueryAtom<G>) current;

					List<Variable<G>> vars = new ArrayList<>(notAtom
							.getQuery().getResultVars());
					List<QueryResult<G>> candidates = new ArrayList<>();

					for (final Variable<G> v : notAtom.getQuery()
							.getResultVars()) {
						final List<G> cands = new ArrayList<>();
						cands.addAll(kb.getIndividuals());
						cands.addAll(kb.getClasses());
						cands.addAll(kb.getObjectProperties());
						cands.addAll(kb.getDataProperties());

						final QueryResult<G> candR = new QueryResult<G>() {

							public Iterator<ResultBinding<G>> iterator() {
								return new Iterator<ResultBinding<G>>() {

									final Iterator<G> ity = cands.iterator();

									public boolean hasNext() {
										return ity.hasNext();
									}

									public ResultBinding<G> next() {
										return new ResultBindingImpl<>(
												Collections.singletonMap(v,
														f.wrap(ity.next())));
									}

									public void remove() {
										throw new UnsupportedQueryException(
												"Removing not supported");
									}
								};
							}

							public boolean add(ResultBinding<G> binding) {
								return true;
							}

							public List<Variable<G>> getResultVars() {
								return Arrays.asList(v);
							}

							public boolean isDistinct() {
								return false;
							}

							public boolean isEmpty() {
								return size() == 0;
							}

							public int size() {
								return cands.size();
							}

						};
						candidates.add(candR);
					}

					CarthesianProductResult<G> results = new CarthesianProductResult<>(
							vars, candidates);

					List<ResultBinding<G>> list = new ArrayList<>();

					InternalQuery<G> qX = notAtom.getQuery();

					for (ResultBinding<G> bI : OWL2QueryEngine.exec(qX.distinct(true))) {
						list.add(bI);
					}

					for (ResultBinding<G> bI : results) {
						ResultBinding<G> candidateBinding = binding.clone();
						candidateBinding.putAll(bI);
						if (list.contains(bI)) {
							continue;
						}

						exec(candidateBinding);
					}
					break;

				default:
					if ( current instanceof External ) {
						final External<G> cur = (External<G>) current;
						final ResultBinding<G> candidateBinding = binding.clone();
						final Iterator<ResultBinding<G>> i = cur.eval(candidateBinding, kb);
						while (i.hasNext()) {
							ResultBinding<G> r = i.next();
							exec(r);
						}
					} else {
						throw new UnsupportedQueryException("Unknown atom type '"
							+ current.getPredicate() + "'.");
					}
				}
			}
		}

		if (log.isLoggable(Level.FINER)) {
			log.finer("Returning ... " + bindingX);
		}

		plan.back();
	}

	private BindingIterator<G> resolveComplexExpressionVariables(
			final Collection<Term<G>> terms) {
		final Map<Variable<G>, Set<? extends G>> map = new HashMap<>();

		for (final Term<G> t : terms) {
			if (!t.isGround() && !t.isVariable()) {
				for (final Variable<G> var : t.getVariables()) {
					VarType v = t.getVariableType(var);

					switch (v) {
					case CLASS:
						map.put(var, kb.getClasses());
						break;
					case OBJECT_OR_DATA_PROPERTY:
						Set<G> set = new HashSet<>();
						set.addAll(kb.getObjectProperties());
						set.addAll(kb.getDataProperties());
						map.put(var, set);
						break;
					case OBJECT_PROPERTY:
						map.put(var, kb.getObjectProperties());
						break;
					case DATA_PROPERTY:
						map.put(var, kb.getDataProperties());
						break;
					case INDIVIDUAL_OR_LITERAL:
					case INDIVIDUAL:
						map.put(var, kb.getIndividuals());
						break;
					case LITERAL:
					default:
						throw new IllegalArgumentException();
					}
				}
			}
		}

		return new BindingIterator<>(map, f);
	}

	private void downMonotonic(final Hierarchy<G, ? extends G> taxonomy,
			final Collection<? extends G> all, final boolean lhsDM,
			final Term<G> lhs, final Term<G> rhs,
			final ResultBinding<G> binding, boolean direct, boolean strict) {
		final Term<G> downMonotonic = lhsDM ? lhs : rhs;
		final Term<G> theOther = lhsDM ? rhs : lhs;
		Collection<? extends G> candidates;

		if (theOther.isVariable()) {
			candidates = new HashSet<G>(all);
			// TODO more refined evaluation in case that both
			// variables are down-monotonic
		} else if (lhsDM) {
			candidates = Collections.singleton(rhs.asGroundTerm()
					.getWrappedObject());
		} else {
			// if (!top.isURI()) {// TODO ATermUtils.isComplexClass(top)) {
			// candidates = kb.getEquivalentClasses(top);
			//
			// if (!strict && candidates.isEmpty()) {
			// candidates = flatten(kb.getSubClasses(top, true));
			// }
			// } else {
			candidates = taxonomy.getTops();
			// }
		}

		for (final G candidate : candidates) {
			final ResultBinding<G> newBinding = binding.clone();

			if (theOther.isVariable()) {
				newBinding.put(theOther.asVariable(),
						kb.getFactory().wrap(candidate));
			}

			final Set<G> toDo = new HashSet<>(lhsDM ? taxonomy.getSubs(
					candidate, direct) : taxonomy.getSupers(candidate, direct));

			if (strict) {
				toDo.removeAll(taxonomy.getEquivs(candidate));
			} else {
				toDo.add(candidate);
			}

			runRecursively(taxonomy, downMonotonic, candidate, newBinding,
					new HashSet<>(toDo), direct, strict);
		}
	}

	private boolean isDownMonotonic(final Term<G> scLHS) {
		// TODO more refined condition to allow optimization for other atoms as
		// well - Type and
		// PropertyValue as well.

		return Configuration.OPTIMIZE_DOWN_MONOTONIC
				&& downMonotonic.contains(scLHS);
	}

	private void runNext(final ResultBinding<G> binding,
			final List<Term<G>> arguments, final G... values) {

		final ResultBinding<G> candidateBinding = binding.clone();

		for (int i = 0; i < arguments.size(); i++) {
			if (arguments.get(i).isVariable()) {
				candidateBinding.put(arguments.get(i).asVariable(),
						f.wrap(values[i]));
			}
		}

		exec(candidateBinding);
	}

	private Set<? extends G> getSymmetricCandidates(VarType forType,
			Term<G> cA, Term<G> cB) {
		final Set<? extends G> candidates;

		if (cA.isGround()) {
			candidates = Collections.singleton(cA.asGroundTerm()
					.getWrappedObject());
		} else if (cB.isGround()) {
			candidates = Collections.singleton(cB.asGroundTerm()
					.getWrappedObject());
		} else {
			switch (forType) {
			case CLASS:
				candidates = kb.getClasses();
				break;
			case OBJECT_OR_DATA_PROPERTY:
				candidates = getObjectAndDataProperties();
				break;
			case OBJECT_PROPERTY:
				candidates = kb.getObjectProperties();
				break;
			case DATA_PROPERTY:
				candidates = kb.getDataProperties();
				break;
			case INDIVIDUAL:
				candidates = kb.getIndividuals();
				break;
			default:
				throw new RuntimeException(
						"Unsupported type for symmetric check : " + forType);
			}
		}

		return candidates;
	}

	private void runRecursively(final Hierarchy<G, ? extends G> t,
			final Term<G> downMonotonic, final G rootCandidate,
			final ResultBinding<G> binding, final Set<G> toDo,
			final boolean direct, final boolean strict) {
		int size = result.size();

		if (log.isLoggable(Level.FINE)) {
			log.fine("Trying : " + rootCandidate + ", done=" + toDo);
		}

		if (!strict) {
			toDo.remove(rootCandidate);
			runNext(binding, Collections.singletonList(downMonotonic), rootCandidate);
		}

		if (strict || result.size() > size) {
			final Set<? extends G> subs = t.getSubs(rootCandidate, direct);

			for (final G subject : subs) {
				if (!toDo.contains(subject)) {
					continue;
				}
				runRecursively(t, downMonotonic, subject, binding, toDo, false,
						false);
			}
		} else {
			if (log.isLoggable(Level.FINE)) {
				log.fine("Skipping subs of " + rootCandidate);
			}
			// toDo.removeAll(t.getFlattenedSubs(rootCandidate, false));
			toDo.removeAll(t.getSubs(rootCandidate, false));
		}
	}

	private void runSymetricCheck(
			@SuppressWarnings("unused") QueryAtom<G> current, Term<G> cA,
			G known, Term<G> cB, G dependent, ResultBinding<G> binding) {
		final ResultBinding<G> candidateBinding = binding.clone();

		if (cA.isGround()) {
			candidateBinding.put(cB.asVariable(), f.wrap(dependent));
		} else if (cB.isGround()) {
			candidateBinding.put(cA.asVariable(), f.wrap(dependent));
		} else {
			candidateBinding.put(cA.asVariable(), f.wrap(known));
			candidateBinding.put(cB.asVariable(), f.wrap(dependent));
		}

		exec(candidateBinding);
	}

	private Set<G> getObjectAndDataProperties() {
		final Set<G> sss = new HashSet<>(kb.getObjectProperties());
		sss.addAll(kb.getDataProperties());
		return sss;
	}

	private void runAllPropertyChecks(final Variable<G> var,
			final Set<? extends G> candidates, ResultBinding<G> binding) {
		final Hierarchy<G, ? extends G> h = kb.getPropertyHierarchy();

		if (isDownMonotonic(var)) {
			for (final G top : h.getTops()) {

				if (candidates.contains(top)) {
					runRecursively(h, var, top, binding, new HashSet<>(
							candidates), false, false);
				}
			}
		} else {
			for (final G candidate : candidates) {
				final ResultBinding<G> candidateBinding = binding.clone();

				candidateBinding.put(var, f.wrap(candidate));

				exec(candidateBinding);
			}
		}
	}
}
