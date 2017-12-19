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
import java.util.Map;
import java.util.Set;

public interface OWL2Ontology<G> {

	public SizeEstimate<G> getSizeEstimate();

	public OWL2QueryFactory<G> getFactory();

	public void ensureConsistency();

	public boolean isClassified();

	public boolean isRealized();

	/**
	 * Checks whether the given class expression is satisfiable, i.e. whether
	 * there is a model of the ontology in which the class extension is
	 * non-empty
	 * 
	 * @param ce
	 *            class expression to be tested
	 * @return true if ce is satisfiable
	 */
	public boolean isSatisfiable(G ce);

	/**
	 * Returns true iff 'ce' represents the class expression that is necessarily
	 * nonempty in all models of the ontology. This is a completely different
	 * notion than (i) class satisfiability which requires the class to has
	 * non-empty extension in some model, and (ii) class unsatisfiability that
	 * requires the class to have an empty extension in each model.
	 * 
	 * @param ce
	 *            the class expression to be tested
	 * @return true if ce has a non empty extension in all models of the
	 *         ontology.
	 */
	public boolean isClassAlwaysNonEmpty(G ce);

	public Collection<?> retrieveIndividualsWithProperty(G Term);

	public boolean hasPropertyValue(G p, G s, G o);

	public Boolean hasKnownPropertyValue(G p, G s, G o);

	public Collection<? extends G> getPropertyValues(G pvP, G pvI);

	public Collection<? extends G> getKnownPropertyValues(G pvP, G pvI);

	public Collection<? extends G> getIndividualsWithProperty(G pvP, G pvIL);

	// boolean isAnnotation(Term i, Term property, Term i2);
	//
	// Collection<Term> getAnnotations(Term subject, Term property);
	//
	// Collection<? extends Term> getIndividualsWithAnnotation(Term property,
	// Term object);

	// public boolean isOntologyProperty(G p);

	// ////////////////////////////////////////////////////////////////////////

	public boolean isComplexClass(G o);

	/**
	 * Returns true, if 'o' is known to be of one of the given types in the
	 * ontology.
	 * 
	 * @param o
	 * @param types
	 * @return
	 */
	public boolean is(G o, final OWLObjectType... types);

	// individuals
	public Set<? extends G> getIndividuals();

	public Set<? extends G> getInstances(G ic, boolean direct);

	public Map<G, Boolean> getKnownInstances(G ic);

	public Set<? extends G> getSames(G i);

	public boolean isSameAs(G i1, G i2);

	public Set<? extends G> getDifferents(G i);

	public boolean isDifferentFrom(G i1, G i2);

	// classes
	// /**
	// * return all referenced classes + TOP + BOTTOM
	// */
	public Set<? extends G> getClasses();

	public Hierarchy<G, ? extends G> getClassHierarchy();

	public Hierarchy<G, ? extends G> getToldClassHierarchy();

	public Hierarchy<G, ? extends G> getPropertyHierarchy();

	public Set<? extends G> getTypes(G i, boolean direct);

	public boolean isTypeOf(G ce, G i, boolean direct);

	public Boolean isKnownTypeOf(G ce, G i);

	// Collection<G> getComplements(G
	// ce);
	//
	// boolean isComplement(G ce1, G
	// ce2);

	public Set<? extends G> getDomains(G pred);

	public Set<? extends G> getRanges(G pred);

	// properties
//	public Set<? extends G> getProperties();

	public Set<? extends G> getObjectProperties();

	public Set<? extends G> getDataProperties();

//	boolean isInverse(G Term, G Term2);
	public Set<? extends G> getInverses(G ope);

	public boolean isSymmetricProperty(G Term);

	public Set<? extends G> getSymmetricProperties();

	public boolean isAsymmetricProperty(G Term);

	public Set<? extends G> getAsymmetricProperties();

	public boolean isReflexiveProperty(G Term);

	public Set<? extends G> getReflexiveProperties();

	public boolean isIrreflexiveProperty(G Term);

	public Set<? extends G> getIrreflexiveProperties();

	public boolean isFunctionalProperty(G Term);

	public Set<? extends G> getFunctionalProperties();

	public boolean isInverseFunctionalProperty(G Term);

	public Set<? extends G> getInverseFunctionalProperties();

	public boolean isTransitiveProperty(G Term);

	public Set<? extends G> getTransitiveProperties();
	
	public String getDatatypeOfLiteral(G literal);
}
