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

import java.util.Set;

public interface SizeEstimate<G> {

	void compute(Set<G> concepts, Set<G> properties);

	boolean isComputed(G predicate);

	void computeAll();

	double avg(G pred);

	int size(G pred);

	int getClassCount();

	long getCost(KBOperation getDirectInstances);

	double avgInstancesPerClass(boolean direct);

	double avgSubjectsPerProperty();

	double avgPairsPerProperty();

	int classesPerInstance(G instance, boolean direct);

	double avgClassesPerInstance(boolean direct);

	double sames(G saLHS);

	double avgSamesPerInstance();

	int getInstanceCount();

	double differents(G dfLHS);

	double avgDifferentsPerInstance();

	double superClasses(G clazzLHS, boolean direct);

	double avgSuperClasses(boolean direct);

	double equivClasses(G clazzLHS);

	double avgEquivClasses();

	double avgSubClasses(boolean direct);

	double disjointClasses(G dwLHS);
	
	double avgDisjointClasses();
	
    double complements(G coLHS);
	
    double avgComplementClasses();
	
	double avgSuperProperties(boolean direct);

	double superProperties(G spLHS, boolean direct);

	double equivProperties(G spLHS);

	double disjointProperties(G dwLHS);

	double avgDisjointProperties();

	double avgEquivProperties();

	double avgSubProperties(boolean direct);

	double inverses(G ioLHS);
	
    double avgInverseProperties();
	
	int getObjectPropertyCount();
	int getDataPropertyCount();
	
	int getFunctionalPropertyCount();	
	int getInverseFunctionalPropertyCount();	
	int getTransitivePropertyCount();
	int getSymmetricPropertyCount();
	int getAsymmetricPropertyCount();
	int getReflexivePropertyCount();
	int getIrreflexivePropertyCount();

}
