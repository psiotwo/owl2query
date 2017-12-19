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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SizeEstimateImpl<G> implements SizeEstimate<G> {

	protected static final Logger log = Logger.getLogger(SizeEstimate.class
			.getName());

	private boolean PRINTSTATISTICS = false;
	private final Set<G> EMPTY_SET = new HashSet<G>();
	public static double UNKNOWN_PROB = 0.5;
	public static boolean CHECK_CONCEPT_SAT = false;

	private static final long noSatCost = 1;
	private long oneSatCost;
	private long classificationCost;
	private long realizationCost;
	private long instanceRetrievalCost;
	private long classRetrievalCost;

	private OWL2Ontology<G> ontology;

	private boolean computed = false;

	private int pCount;
	private int opCount;
	private int dpCount;

	private int fpCount;
	private int ifpCount;
	private int spCount;
	private int tpCount;
	private int aspCount;
	private int rpCount;
	private int irpCount;

	private int cCount;
	private int iCount;

	private final Map<G, Integer> instancesPC = new HashMap<G, Integer>();
	private final Map<G, Integer> directInstancesPC = new HashMap<G, Integer>();
	private final Map<G, Integer> classesPI = new HashMap<G, Integer>();
	private final Map<G, Integer> directClassesPI = new HashMap<G, Integer>();
	private final Map<G, Integer> pairsPP = new HashMap<G, Integer>();
	private final Map<G, Integer> sames = new HashMap<G, Integer>();
	private final Map<G, Integer> differents = new HashMap<G, Integer>();
	private final Map<G, Double> avgObjectsPP = new HashMap<G, Double>();
	private final Map<G, Integer> equivClasses = new HashMap<G, Integer>();
	private final Map<G, Integer> subClasses = new HashMap<G, Integer>();
	private final Map<G, Integer> directSubClasses = new HashMap<G, Integer>();
	private final Map<G, Integer> superClasses = new HashMap<G, Integer>();
	private final Map<G, Integer> directSuperClasses = new HashMap<G, Integer>();
	private Map<G, Integer> disjointClasses = new HashMap<G, Integer>();
	private Map<G, Integer> complementClasses = new HashMap<G, Integer>();;
	private final Map<G, Integer> equivProperties = new HashMap<G, Integer>();
	private final Map<G, Integer> subProperties = new HashMap<G, Integer>();
	private final Map<G, Integer> directSubProperties = new HashMap<G, Integer>();
	private final Map<G, Integer> superProperties = new HashMap<G, Integer>();
	private final Map<G, Integer> directSuperProperties = new HashMap<G, Integer>();
	private Map<G, Integer> disjointProperties = new HashMap<G, Integer>();
	private Map<G, Integer> inverses;

	private double avgClassesPI;
	private double avgDirectClassesPI;
	private double avgSamesPI;
	private double avgDifferentsPI;
	private double avgSubClasses;
	private double avgDirectSubClasses;
	private double avgSuperClasses;
	private double avgDirectSuperClasses;
	private double avgEquivClasses;
	private double avgDisjointClasses;
	private double avgComplements;
	private double avgSubProperties;
	private double avgDirectSubProperties;
	private double avgSuperProperties;
	private double avgDirectSuperProperties;
	private double avgEquivProperties;
	private double avgDisjointProperties;
	private double avgInversesPP;
	private double avgPairsPP;
	private double avgSubjectsPerProperty;
	private double avgInstancesPC;
	private double avgDirectInstances;

	public SizeEstimateImpl(OWL2Ontology<G> kb) {
		this.ontology = kb;

		cCount = ontology.getClasses().size();
		iCount = ontology.getIndividuals().size();

		opCount = ontology.getObjectProperties().size();
		dpCount = ontology.getDataProperties().size();
		pCount = opCount + dpCount;

		fpCount = kb.getFunctionalProperties().size();
		ifpCount = kb.getInverseFunctionalProperties().size();
		tpCount = kb.getTransitiveProperties().size();
		spCount = kb.getSymmetricProperties().size();
		aspCount = kb.getAsymmetricProperties().size();
		rpCount = kb.getReflexiveProperties().size();
		irpCount = kb.getIrreflexiveProperties().size();

		inverses = new HashMap<G, Integer>();

		instancesPC.put(ontology.getFactory().getThing(), iCount);
		instancesPC.put(ontology.getFactory().getNothing(), 0);

		subClasses.put(ontology.getFactory().getThing(), cCount);
		directSubClasses.put(ontology.getFactory().getThing(), cCount); //

		subClasses.put(ontology.getFactory().getNothing(), 1);
		directSubClasses.put(ontology.getFactory().getNothing(), 0);

		superClasses.put(ontology.getFactory().getThing(), 1);
		directSuperClasses.put(ontology.getFactory().getThing(), 0);
		superClasses.put(ontology.getFactory().getNothing(), cCount);
		directSuperClasses.put(ontology.getFactory().getNothing(), cCount);

		equivClasses.put(ontology.getFactory().getThing(), 1);
		equivClasses.put(ontology.getFactory().getNothing(), 1);

		disjointClasses.put(kb.getFactory().getThing(), 1);
		disjointClasses.put(kb.getFactory().getNothing(), pCount);

		disjointProperties.put(kb.getFactory().getThing(), 1);
		disjointProperties.put(kb.getFactory().getNothing(), pCount);

		complementClasses.put(kb.getFactory().getThing(), 1);
		complementClasses.put(kb.getFactory().getNothing(), cCount);

		computed = false;

		avgSubjectsPerProperty = 1;
		avgPairsPP = 1;

		computeKBCosts();
	}

	public boolean isKBComputed() {
		return computed;
	}

	public void computeKBCosts() {
		int classCount = ontology.getClasses().size();
		int indCount = ontology.getIndividuals().size();

		// FIXME the following constants are chosen based on very limited
		// empirical analysis

		// complexityFactor
		// combine (i) size of the KB, (ii) expressivity, (iii) whether the
		// satisfiability is tested for a named class or a complex class
		oneSatCost = 2;// kb.getIndividuals().size();

		// this is a very rough and pretty inaccurate estimate
		// of classification. the number of sat checks done during
		// classification varies widely but due to various optimizations
		// it is a relatively small percentage of the brute-force n^2
		classificationCost = ontology.isClassified() ? noSatCost : (classCount
				* classCount * oneSatCost) / 10;

		// the same arguments for classification applies here too
		realizationCost = ontology.isRealized() ? noSatCost
				: classificationCost + (oneSatCost * classCount * indCount);

		// instance retrieval performs sat checks on only individuals that
		// are not ruled out by obvious (non-)instance checks thus it is
		// again a very small percentage
		instanceRetrievalCost = ontology.isRealized() ? noSatCost
				: (indCount * oneSatCost) / 100;

		// either KB is realized and this operation is pretty much free or
		// we perform realization and pay the cost
		// NOTE: the behavior to realize the KB at every type retrieval query
		// is subject to change and would require a change here too
		classRetrievalCost = ontology.isRealized() ? noSatCost
				: realizationCost;
	}

	public void computeAll() {
		if (!computed) {
			computeKBCosts();

			if (log.isLoggable(Level.FINE)) {
				log.fine("   NoSat cost : " + noSatCost + " ms.");
				log.fine("  OneSat cost : " + oneSatCost + " ms.");
				log.fine("Classify cost : " + classificationCost + " ms.");
				log.fine(" Realize cost : " + realizationCost + " ms.");
				log.fine("      IR cost : " + instanceRetrievalCost + " ms.");
				log.fine("      CR cost : " + classRetrievalCost + " ms.");
			}

			final HashSet<G> s = new HashSet<G>(ontology.getObjectProperties());
			s.addAll(ontology.getDataProperties());
			compute(new HashSet<G>(ontology.getClasses()), s);
			computed = true;
		}
	}

	public boolean isComputed(G term) {
		return instancesPC.containsKey(term) || pairsPP.containsKey(term)
				|| classesPI.containsKey(term);
	}

	private double average(final Collection<Integer> x) {
		if (x.isEmpty()) {
			return 0;
		}

		int a = 0;

		for (final Iterator<Integer> i = x.iterator(); i.hasNext();) {
			a += i.next();
		}

		return ((double) a) / x.size();
	}

	public void compute(Set<G> cs, Set<G> ps) {
		Collection<G> concepts = new HashSet<G>(cs);
		Collection<G> properties = new HashSet<G>(ps);

		concepts.removeAll(instancesPC.keySet());
		properties.removeAll(pairsPP.keySet());

		if (concepts.isEmpty() && properties.isEmpty()) {
			return;
		}

		// final Timer timer = kb.timers.startTimer("sizeEstimate");

		log.fine("Size estimation started");

		final Random randomGen = new Random();

		final Map<G, Integer> pSubj = new HashMap<G, Integer>();
		final Map<G, Integer> pObj = new HashMap<G, Integer>();

		final Hierarchy<G, ? extends G> taxonomy;

		if (ontology.isClassified()) {
			taxonomy = ontology.getClassHierarchy();
		} else {
			taxonomy = ontology.getToldClassHierarchy();
		}

		for (final Iterator<G> i = concepts.iterator(); i.hasNext();) {
			G c = i.next();

			if (!ontology.is(c, OWLObjectType.OWLClass))
				continue;

			log.config("Trying class " + c);

			// if (taxonomy.contains(c)) {
			subClasses.put(c, taxonomy.getSubs(c, false).size());
			directSubClasses.put(c, taxonomy.getSubs(c, true).size());
			superClasses.put(c, taxonomy.getSupers(c, false).size());
			directSuperClasses.put(c, taxonomy.getSupers(c, true).size());
			equivClasses.put(c, taxonomy.getEquivs(c).size() + 1);
			disjointClasses.put(c, taxonomy.getDisjoints(c).size() + 1);
			complementClasses.put(c, taxonomy.getDisjoints(c).size() + 1);
			// } else {
			// subClasses.put(c, 1);
			// directSubClasses.put(c, 1);
			// superClasses.put(c, 1);
			// directSuperClasses.put(c, 1);
			// equivClasses.put(c, 1);
			// }

			// final Map<G, Set<G>> toldDisjoints = kb.getToldDisjoints();
			//
			// if (toldDisjoints.containsKey(c)) {
			// disjoints.put(c, toldDisjoints.get(c).size());
			// complements.put(c, toldDisjoints.get(c).size()); // TODO
			// } else {
			// disjoints.put(c, 1);
			// complements.put(c, 1);
			// }

			if (ontology.isRealized() && ontology.isComplexClass(c)) {
				instancesPC.put(c, ontology.getInstances(c, false).size());
				directInstancesPC.put(c, ontology.getInstances(c, true).size());
			} else {
				instancesPC.put(c, 0);
				directInstancesPC.put(c, 0);

				if (CHECK_CONCEPT_SAT) {
					if (!ontology.isSatisfiable(c))
						i.remove();

					if (!ontology.isSatisfiable(ontology.getFactory()
							.objectComplementOf(c))) {
						i.remove();
						instancesPC.put(c, ontology.getIndividuals().size());
					}
				}
			}

			if (log.isLoggable(Level.FINE))
				log.fine("Initialize " + c + " = " + size(c));
		}

		for (final G p : properties) {
			if (!ontology.is(p, OWLObjectType.OWLObjectProperty,
					OWLObjectType.OWLDataProperty))
				continue;

			pairsPP.put(p, 0);
			pSubj.put(p, 0);
			pObj.put(p, 0);

			subProperties.put(p,
					ontology.getPropertyHierarchy().getSubs(p, false).size());
			directSubProperties.put(p,
					ontology.getPropertyHierarchy().getSubs(p, true).size());
			superProperties.put(p,
					ontology.getPropertyHierarchy().getSupers(p, false).size());
			directSuperProperties.put(p, ontology.getPropertyHierarchy()
					.getSupers(p, true).size());
			equivProperties.put(p, ontology.getPropertyHierarchy().getEquivs(p)
					.size() + 1);
			disjointProperties.put(p, ontology.getPropertyHierarchy()
					.getEquivs(p).size() + 1);
			if (ontology.is(p,OWLObjectType.OWLObjectProperty)) {			
				inverses.put(p, ontology.getInverses(p).size());				
			} else {
				inverses.put(p, 0);
			}
		}

		for (final G ind : ontology.getIndividuals()) {
			sames.put(ind, 1); // TODO
			differents.put(ind, iCount); // TODO

			float random = randomGen.nextFloat();
			if (random > Configuration.SAMPLING_RATIO)
				continue;

			if (ontology.isRealized()) {
				classesPI.put(ind, ontology.getTypes(ind, false).size());
				directClassesPI.put(ind, ontology.getTypes(ind, true).size());
			} else {
				classesPI.put(ind, 0);
				directClassesPI.put(ind, 0);

				for (final G c : concepts) {
					// estimate for number of instances per given class

					final Boolean isKnownType = ontology.isKnownTypeOf(c, ind);
					if (isKnownType == Boolean.TRUE
							|| (CHECK_CONCEPT_SAT && (isKnownType == null) && (randomGen
									.nextFloat() < UNKNOWN_PROB))) {

						instancesPC.put(c, size(c) + 1);
						directInstancesPC.put(c, size(c) + 1); // TODO
						classesPI.put(ind, classesPerInstance(ind, false) + 1);
						directClassesPI.put(ind,
								classesPerInstance(ind, true) + 1); // TODO
					}
				}
			}

			for (final G p : properties) {
				if (log.isLoggable(Level.FINER))
					log.finer("Looking for known '" + p + "' values of " + ind);
				int knownSize = ontology.getKnownPropertyValues(p, ind).size();

				if (knownSize > 0) {
					// if (log.isLoggable(Level.FINEST))
					// log.finest("Update " + p + " by " + knownSize);
					pairsPP.put(p, size(p) + knownSize);
					pSubj.put(p, pSubj.get(p) + 1);
				}

				if (ontology.is(p, OWLObjectType.OWLObjectProperty)) {
					final G inv = ontology.getFactory()
							.inverseObjectProperty(p);
					if (!ontology.getKnownPropertyValues(inv, ind).isEmpty()) {
						pObj.put(p, pObj.get(p) + 1);
					}
				}
			}
		}

		if (log.isLoggable(Level.FINER))
			log.finer("Computing averages");

		if (!computed) {
			avgClassesPI = average(classesPI.values());
			avgDirectClassesPI = average(directClassesPI.values());
		}

		if (!ontology.isRealized()) {
			if (log.isLoggable(Level.FINER))
				log.finer("Computing instances per class");
			for (final G c : concepts) {
				Integer size = instancesPC.get(c);
				log.finest("Computing instancesPC=" + instancesPC + ", c=" + c);
				if (size == null) {
					size = 0;
				}

				// post processing in case of sampling
				if (size == 0)
					instancesPC.put(c, 1);
				else
					instancesPC.put(c,
							(int) (size / Configuration.SAMPLING_RATIO));

				size = directInstancesPC.get(c);

				if (size == null) {
					size = 0;
				}

				// postprocessing in case of sampling
				if (size == 0)
					directInstancesPC.put(c, 1);
				else
					directInstancesPC.put(c,
							(int) (size / Configuration.SAMPLING_RATIO));
			}

			final int avgCPI = Double.valueOf(avgClassesPI).intValue();
			final int avgDCPI = Double.valueOf(avgDirectClassesPI).intValue();

			if (log.isLoggable(Level.FINER))
				log.finer("Computing individual types");
			for (final G i : ontology.getIndividuals()) {
				Integer size = classesPI.get(i);

				if (size == null) {
					size = avgCPI;
				}

				// postprocessing in case of sampling
				if (size == 0)
					classesPI.put(i, 1);
				else
					classesPI.put(i,
							(int) (size / Configuration.SAMPLING_RATIO));

				size = directClassesPI.get(i);

				if (size == null) {
					size = avgDCPI;
				}

				// postprocessing in case of sampling
				if (size == 0)
					directClassesPI.put(i, 1);
				else
					directClassesPI.put(i,
							(int) (size / Configuration.SAMPLING_RATIO));
			}
		}

		if (log.isLoggable(Level.FINER))
			log.finer("Computing subjects/objects per property");

		for (final G p : properties) {
			if (log.isLoggable(Level.FINER))
				log.finer("Computing p=" + p);
			if (!ontology.is(p, OWLObjectType.OWLObjectProperty,
					OWLObjectType.OWLDataProperty))
				continue;

			int size = size(p);
			if (size == 0)
				pairsPP.put(p, 1);
			else
				pairsPP.put(p, (int) (size / Configuration.SAMPLING_RATIO));

			// Role role = kb.getRBox().getRole(p);
			final G invP = ontology.getFactory().inverseObjectProperty(p);

			// G invP = ((inverses != null) && !inverses.isEmpty()) ?
			// inverses
			// .iterator().next()
			// : null;

			int subjCount = pSubj.get(p);
			if (subjCount == 0)
				subjCount = 1;
			int objCount = pObj.get(p);
			if (objCount == 0)
				objCount = 1;

			double avg = Double.valueOf((double) size / subjCount);
			avgObjectsPP.put(p, avg);
			// avgSubjectsPerProperty = Math
			// .max(avgSubjectsPerProperty, subjCount);
			avgSubjectsPerProperty += subjCount;
			if (invP != null) {
				avg = Double.valueOf((double) size / objCount);
				avgObjectsPP.put(invP, avg);
				// avgSubjectsPerProperty = Math.max(avgSubjectsPerProperty,
				// objCount);
				avgSubjectsPerProperty += objCount;
			}
		}
		if (log.isLoggable(Level.FINER))
			log.finer("Computing averages");

		if (properties.size() > 0) {
			avgSubjectsPerProperty = avgSubjectsPerProperty
					/ (2 * properties.size());
			avgPairsPP = average(pairsPP.values());
		} else {
			avgSubjectsPerProperty = 1;
			avgPairsPP = 1;
		}

		avgInstancesPC = average(instancesPC.values());
		avgDirectInstances = average(directInstancesPC.values());
		avgSamesPI = average(sames.values());
		avgDifferentsPI = average(differents.values());

		avgSubClasses = average(subClasses.values());
		avgDirectSubClasses = average(directSubClasses.values());
		avgSuperClasses = average(superClasses.values());
		avgDirectSuperClasses = average(directSuperClasses.values());
		avgEquivClasses = average(equivClasses.values());
		avgDisjointClasses = average(disjointClasses.values());
		avgComplements = average(complementClasses.values());
		avgSubProperties = average(subProperties.values());
		avgDirectSubProperties = average(directSubProperties.values());
		avgSuperProperties = average(superProperties.values());
		avgDirectSuperProperties = average(directSuperProperties.values());
		avgEquivProperties = average(equivProperties.values());
		avgDisjointProperties = average(disjointProperties.values());
		avgInversesPP = average(inverses.values());

		// timer.stop();

		if (PRINTSTATISTICS) {
			printStatistics();
		}

		// if (log.isLoggable(Level.FINE)) {
		// NumberFormat nf = new DecimalFormat("0.00");
		// log.fine("Size estimation finished in "
		// + nf.format(timer.getLast() / 1000.0) + " sec");
		// }
	}

	private void printStatistics() {
		// final Statistics<G, String> instances = new Statistics<G, String>();
		// instances.add("classes", classesPI);
		// instances.add("sames", sames);
		// instances.add("differents", differents);
		// System.out.println(instances.toString());

		System.out.println("Avg classes per instance:" + avgClassesPI);
		System.out.println("Avg sames per individual:" + avgSamesPerInstance());
		System.out.println("Avg differents per individual:"
				+ avgDifferentsPerInstance());

		// final Statistics<G, String> classes = new Statistics<G, String>();

		// classes.add("size", instancesPC);
		// classes.add("subs", subClasses);
		// classes.add("supers", superClasses);
		// classes.add("equivs", equivClasses);
		// // classes.add("complements", complements);
		// classes.add("disjoints", disjoints);
		//
		// System.out.println(classes.toString());

		System.out.println("Avg individuals per class:" + avgInstancesPC);
		System.out.println("Avg subclasses:" + avgSubClasses(false));
		System.out.println("Avg direct subclasses:" + avgSubClasses(true));
		System.out.println("Avg superclasses:" + avgSuperClasses(false));
		System.out.println("Avg direct superclasses:" + avgSuperClasses(true));
		System.out.println("Avg equivalent classes:" + avgEquivClasses());
		System.out.println("Avg complement classes:" + avgComplementClasses());
		System.out.println("Avg disjoint classes:" + avgDisjointClasses());

		// TODO
		// final StatisticsTable<G, String> properties = new StatisticsTable<G,
		// String>();

		// properties.add("size", pairsPP);
		// properties.add("avgs", avgObjectsPP);
		// properties.add("subs", subProperties);
		// properties.add("supers", superProperties);
		// properties.add("equivs", equivProperties);
		// // properties.add("inverses", inverses);
		//
		// System.out.println(properties.toString());

		System.out.println("Avg pairs per property:" + avgPairsPerProperty());
		System.out.println("Avg subjects per property:"
				+ avgSubjectsPerProperty());
		System.out.println("Avg subproperties:" + avgSubProperties(false));
		System.out.println("Avg superproperties:" + avgSuperProperties(false));
		System.out.println("Avg equivalent properties:" + avgEquivProperties());
		System.out.println("Avg disjoint property:" + avgDisjointProperties());
		System.out.println("Avg inverse properties:" + avgInverseProperties());

		System.out.println("NoSatCost: " + noSatCost);
		System.out.println("OneSatCost: " + oneSatCost);
		System.out.println("ClassificationCost: " + classificationCost);
		System.out.println("RealizationCost: " + realizationCost);
		System.out.println("ClassRetrievalCost: " + classRetrievalCost);
		System.out.println("InstanceRetrievalCost: " + instanceRetrievalCost);
	}

	// TODO replace by object type dependent one
	public int size(G c) {
		if (instancesPC.containsKey(c)) {
			return instancesPC.get(c);
		} else if (pairsPP.containsKey(c)) {
			return pairsPP.get(c);
		} else {
			if (ontology.is(c, OWLObjectType.OWLObjectProperty,
					OWLObjectType.OWLDataProperty)) {
				compute(EMPTY_SET, Collections.singleton(c));
			} else {
				compute(Collections.singleton(c), EMPTY_SET);
			}
			return size(c);
		}
	}

	public int classesPerInstance(G i, boolean direct) {
		final Map<G, Integer> map = direct ? directClassesPI : classesPI;

		if (map.containsKey(i)) {
			return map.get(i);
		}

		throw new InternalReasonerException("Instance number estimate : " + i
				+ " is not found!");
	}

	public double avg(G pred) {
		if (!avgObjectsPP.containsKey(pred)) {
			compute(EMPTY_SET, Collections.singleton(pred));
		}

		if (avgObjectsPP.get(pred) != null) {

			return avgObjectsPP.get(pred);
		} else {
			return 0;
		}
	}

	public int getClassCount() {
		return cCount;
	}

	public int getInstanceCount() {
		return iCount;
	}

	public int getPropertyCount() {
		return pCount;
	}

	public int getObjectPropertyCount() {
		return opCount;
	}

	public int getDataPropertyCount() {
		return dpCount;
	}

	public int getFunctionalPropertyCount() {
		return fpCount;
	}

	public int getInverseFunctionalPropertyCount() {
		return ifpCount;
	}

	public int getTransitivePropertyCount() {
		return tpCount;
	}

	public int getSymmetricPropertyCount() {
		return spCount;
	}

	public int getAsymmetricPropertyCount() {
		return aspCount;
	}

	public int getReflexivePropertyCount() {
		return rpCount;
	}

	public int getIrreflexivePropertyCount() {
		return irpCount;
	}

	public double avgInstancesPerClass(boolean direct) {
		return direct ? avgDirectInstances : avgInstancesPC;
	}

	public double avgDirectInstancesPerClass() {
		return avgDirectInstances;
	}

	public double avgPairsPerProperty() {
		return avgPairsPP;
	}

	public double avgSubjectsPerProperty() {
		return avgSubjectsPerProperty;
	}

	public double avgSubClasses(boolean direct) {
		return direct ? avgDirectSubClasses : avgSubClasses;
	}

	public double avgSuperClasses(boolean direct) {
		return direct ? avgDirectSuperClasses : avgSuperClasses;
	}

	public double avgEquivClasses() {
		return avgEquivClasses;
	}

	public double avgDisjointClasses() {
		return avgDisjointClasses;
	}

	public double avgComplementClasses() {
		return avgComplements;
	}

	public double avgSubProperties(boolean direct) {
		return direct ? avgDirectSubProperties : avgSubProperties;
	}

	public double avgSuperProperties(boolean direct) {
		return direct ? avgDirectSuperProperties : avgSuperProperties;
	}

	public double avgEquivProperties() {
		return avgEquivProperties;
	}

	public double avgDisjointProperties() {
		return avgDisjointProperties;
	}

	// public double avgInverseProperties() {
	// return avgInversesPP;
	// }

	public double avgSamesPerInstance() {
		return avgSamesPI;
	}

	public double avgDifferentsPerInstance() {
		return avgDifferentsPI;
	}

	public double avgClassesPerInstance(final boolean direct) {
		return direct ? avgDirectClassesPI : avgClassesPI;
	}

	public double subClasses(G sup, boolean direct) {
		final Map<G, Integer> map = (direct ? directSubClasses : subClasses);

		if (!map.containsKey(sup)) {
			compute(Collections.singleton(sup), EMPTY_SET);
			if (log.isLoggable(Level.FINE)) {
				log.fine("Computing additionally " + sup);
			}
		}
		return map.get(sup);
		//
		// throw new InternalReasonerException("Sub estimate for " + sup
		// + " is not found!");
	}

	public double subProperties(G sup, boolean direct) {
		final Map<G, Integer> map = (direct ? directSubProperties
				: subProperties);

		if (!map.containsKey(sup)) {
			compute(EMPTY_SET, Collections.singleton(sup));
			if (log.isLoggable(Level.FINE)) {
				log.fine("Computing additionally " + sup);
			}
		}
		return map.get(sup);
	}

	public double superClasses(G sup, boolean direct) {
		final Map<G, Integer> map = (direct ? directSuperClasses : superClasses);

		if (!map.containsKey(sup)) {
			compute(Collections.singleton(sup), EMPTY_SET);
			if (log.isLoggable(Level.FINE)) {
				log.fine("Computing additionally " + sup);
			}
		}

		Integer i = map.get(sup);

		if (i == null) {
			i = 0;
		}

		return i;
	}

	public double superProperties(G sup, boolean direct) {
		final Map<G, Integer> map = (direct ? directSuperProperties
				: superProperties);

		if (!map.containsKey(sup)) {
			compute(EMPTY_SET, Collections.singleton(sup));
			if (log.isLoggable(Level.FINE)) {
				log.fine("Computing additionally " + sup);
			}
		}
		return map.get(sup);
	}

	public double equivClasses(G sup) {
		if (!equivClasses.containsKey(sup)) {
			compute(Collections.singleton(sup), EMPTY_SET);
			if (log.isLoggable(Level.FINE)) {
				log.fine("Computing additionally " + sup);
			}
		}
		Integer d = equivClasses.get(sup);

		if (d != null) {
			return d.doubleValue();
		} else {
			return 0;
		}
	}

	public double equivProperties(G sup) {
		if (!equivProperties.containsKey(sup)) {
			compute(EMPTY_SET, Collections.singleton(sup));
			if (log.isLoggable(Level.FINE)) {
				log.fine("Computing additionally " + sup);
			}
		}

		Integer d = equivProperties.get(sup);

		if (d != null) {
			return d.doubleValue();
		} else {
			return 0;
		}
	}

	public double disjointProperties(G sup) {
		if (!disjointProperties.containsKey(sup)) {
			compute(EMPTY_SET, Collections.singleton(sup));
			if (log.isLoggable(Level.FINE)) {
				log.fine("Computing additionally " + sup);
			}
		}
		return disjointProperties.get(sup);
	}

	public double sames(G sup) {
		if (sames.containsKey(sup)) {
			return sames.get(sup);
		}

		throw new InternalReasonerException("Sames estimate for " + sup
				+ " is not found!");
	}

	public double differents(G sup) {
		if (differents.containsKey(sup)) {
			return differents.get(sup);
		}

		throw new InternalReasonerException("Sames estimate for " + sup
				+ " is not found!");
	}

	public double disjointClasses(G sup) {
		if (!disjointClasses.containsKey(sup)) {
			compute(Collections.singleton(sup), EMPTY_SET);
			if (log.isLoggable(Level.FINE)) {
				log.fine("Computing additionally " + sup);
			}
		}
		return disjointClasses.get(sup);
	}


	public double avgInverseProperties() {
		return avgInverseProperties();
	}

	public double complements(G sup) {
		if (!complementClasses.containsKey(sup)) {
			compute(Collections.singleton(sup), EMPTY_SET);
			if (log.isLoggable(Level.FINE)) {
				log.fine("Computing additionally " + sup);
			}
		}
		return complementClasses.get(sup);
	}

	public double inverses(G sup) {
		if (!inverses.containsKey(sup)) {
			compute(EMPTY_SET, Collections.singleton(sup));
			if (log.isLoggable(Level.FINE)) {
				log.fine("Computing additionally " + sup);
			}
		}
		return inverses.get(sup);
	}

	public long getCost(KBOperation operation) {
		long cost;
		switch (operation) {

		// TODO
		case IS_DIRECT_TYPE:
			cost = getCost(KBOperation.IS_TYPE);
			break;

		// if realized trivial, oth. 1 sat (more frq than hpv, but less than sc)
		case IS_TYPE:
			cost = (ontology.isRealized() ? noSatCost : oneSatCost);
			break;

		// rare sat (nonempty dependency set of an edge in Compl. G.)
		case HAS_PROPERTY_VALUE:
			cost = noSatCost;
			break;

		// // use told taxonomy - to be provided by KB - not to classify the
		// whole
		// // KB
		// // now triv. if classified, otherwise 1 sat
		case IS_SUBCLASS_OF:
		case IS_EQUIVALENT_CLASS:
			cost = oneSatCost;
			break;
		//
		// // 1 sat
		case IS_DISJOINTCLASS_WITH:
		case IS_COMPLEMENTCLASS_OF:
			cost = oneSatCost;
			break;

		// // triv
		case IS_SUBPROPERTY_OF:
		case IS_EQUIVALENT_PROPERTY:
		case IS_DISJOINTPROPERTY_WITH:
			cost = noSatCost;
			break;

		// // triv
		// case IS_OBJECT_PROPERTY:
		// case IS_DATATYPE_PROPERTY:
		// cost = noSatCost;
		// break;
		//
		// // one sat. check if any
		case IS_REFLEXIVE_PROPERTY:
		case IS_IRREFLEXIVE_PROPERTY:
		case IS_SYMMETRIC_PROPERTY:
		case IS_ASYMMETRIC_PROPERTY:
		case IS_FUNCTIONAL_PROPERTY:
		case IS_INVERSE_FUNCTIONAL_PROPERTY:
		case IS_TRANSITIVE_PROPERTY:
			cost = oneSatCost;
			break;

		// // triv.
		case IS_INVERSE_OF:
			cost = noSatCost;
			break;

		case GET_INVERSES:
			cost = noSatCost;
			break;

		case GET_INSTANCES:
			cost = instanceRetrievalCost;
			break;

		// TODO
		case GET_DIRECT_INSTANCES:
			cost = instanceRetrievalCost + classificationCost;
			break;

		// if realized triv, otherwise TODO
		// binary class retrieval. Currently, realization
		// case GET_DIRECT_TYPES:
		case GET_TYPES:
			cost = classRetrievalCost;
			break;

		// instance retrieval for a small set of instances, meanwhile as
		// instance retrieval.
		case GET_PROPERTY_VALUE:
			cost = noSatCost;// (long) (0.01 * instanceRetrievalCost);
			break;

		// 1 sat (rare)
		case IS_SAME_AS:
			cost = oneSatCost;
			break;

		case GET_SAMES:
			cost = oneSatCost;

			// 1 sat
		case IS_DIFFERENT_FROM:
			cost = oneSatCost;
			break;

		// meanwhile instance retrieval
		case GET_DIFFERENTS:
			cost = instanceRetrievalCost;
			break;

		// // trivial
		// case GET_OBJECT_PROPERTIES:
		// case GET_DATATYPE_PROPERTIES:
		// cost = noSatCost;
		// break;

		// // currently trivial - not complete impl.
		case GET_FUNCTIONAL_PROPERTIES:
		case GET_INVERSE_FUNCTIONAL_PROPERTIES:
		case GET_TRANSITIVE_PROPERTIES:
		case GET_SYMMETRIC_PROPERTIES:
		case GET_ASYMMETRIC_PROPERTIES:
		case GET_REFLEXIVE_PROPERTIES:
		case GET_IRREFLEXIVE_PROPERTIES:
			cost = noSatCost;
			break;

		// trivial if classified and named, otherwise classification
		case GET_SUB_OR_SUPERCLASSES:
		case GET_DIRECT_SUB_OR_SUPERCLASSES: // TODO
		case GET_EQUIVALENT_CLASSES:
			cost = classificationCost;
			break;

		// classification
		case GET_DISJOINT_CLASSES:
		case GET_COMPLEMENT_CLASSES:
			cost = classificationCost;
			break;

		// trivial
		case GET_SUB_OR_SUPERPROPERTIES:
		case GET_DIRECT_SUB_OR_SUPERPROPERTIES: // TODO
		case GET_EQUIVALENT_PROPERTIES:
		case GET_DISJOINT_PROPERTIES:
			cost = noSatCost;
			break;

		default:
			throw new IllegalArgumentException("Unknown KB Operation type : "
					+ operation);
		}

		return cost;
	}
}
