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

public interface OWL2Query<G> {

	/**
	 * Adds a distinguished variable to the query with its type - there can be
	 * more variable types to support punning.
	 */
	OWL2Query<G> addDistVar(final Variable<G> a, boolean result);

	/**
	 * @deprecated
	 */
	OWL2Query<G> addDistVar(final Variable<G> a);

	/**
	 * Adds a distinguished variable that appears in the result projection to
	 * the query;
	 * 
	 * @deprecated
	 */
	OWL2Query<G> addResultVar(final Variable<G> a);

	// ABOX atoms
	OWL2Query<G> Type(final Term<G> c, final Term<G> i);

	OWL2Query<G> PropertyValue(final Term<G> pA, final Term<G> iA,
			final Term<G> ilA);

	OWL2Query<G> SameAs(final Term<G> i1, final Term<G> i2);

	OWL2Query<G> DifferentFrom(final Term<G> i1, final Term<G> i2);

	// TBOX atoms
	OWL2Query<G> SubClassOf(final Term<G> cA1, final Term<G> cA2);

	OWL2Query<G> EquivalentClass(final Term<G> cA1, final Term<G> cA2);

	OWL2Query<G> DisjointWith(final Term<G> cA1, final Term<G> cA2);

	OWL2Query<G> ComplementOf(final Term<G> cA1, final Term<G> cA2);

	// RBOX atoms
	OWL2Query<G> SubPropertyOf(final Term<G> pA1, final Term<G> pA2);

	OWL2Query<G> EquivalentProperty(final Term<G> pA1, final Term<G> pA2);

	OWL2Query<G> InverseOf(final Term<G> pA1, final Term<G> pA2);

	OWL2Query<G> ObjectProperty(final Term<G> pA);

	OWL2Query<G> DatatypeProperty(final Term<G> pA);

	OWL2Query<G> Functional(final Term<G> pA);

	OWL2Query<G> InverseFunctional(final Term<G> pA);

	OWL2Query<G> Transitive(final Term<G> pA);

	OWL2Query<G> Symmetric(final Term<G> pA);

	OWL2Query<G> Asymmetric(final Term<G> pA);

	OWL2Query<G> Reflexive(final Term<G> pA);

	OWL2Query<G> Irreflexive(final Term<G> pA);

	// annotation
	// Query<G> Annotation(final Term<G> iA, final Term<G> pA, final Term<G>
	// ilA);

	// nonmonotonic atoms
	OWL2Query<G> StrictSubClassOf(final Term<G> c1, final Term<G> c2);

	OWL2Query<G> DirectSubClassOf(final Term<G> c1, final Term<G> c2);

	OWL2Query<G> DirectSubPropertyOf(final Term<G> p1, final Term<G> p2);

	OWL2Query<G> StrictSubPropertyOf(final Term<G> p1, final Term<G> p2);

	OWL2Query<G> DirectType(final Term<G> c, final Term<G> i);

	// negation as failure
	OWL2Query<G> Not(final OWL2Query<G> qb);

	OWL2Query<G> distinct(boolean b);

	<T> OWL2Query<G> external(T expression);
}
