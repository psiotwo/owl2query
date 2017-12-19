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

public interface OWL2QueryFactory<G> {

	OWL2Query<G> createQuery(OWL2Ontology<G> o);

	// TERMS
	// variable
	Variable<G> variable(String name);

	GroundTerm<G> wrap(final G gt);

	// class
	G namedClass(final String uri);

	G namedObjectProperty(final String uri);

	G namedDataProperty(final String uri);

	G namedDataRange(final String uri);

	G namedIndividual(final String uri);

	G literal(String s);

	G literal(String s, String lang);

	G typedLiteral(String s, String datatype);

	G getThing();

	G getNothing();

	G getBottomObjectProperty();

	G getBottomDataProperty();

	G getTopDatatype();

	G getTopDataProperty();

	G getTopObjectProperty();

	G objectAllValuesFrom(final G ope, final G ce);

	G objectComplementOf(final G c);

	G objectHasSelf(final G ope);

	G objectHasValue(final G ope, final G ni);

	G objectIntersectionOf(final Set<G> c);

	G objectMinCardinality(final int card, final G ope, final G ce);

	G objectMaxCardinality(final int card, final G ope, final G ce);

	G objectExactCardinality(final int card, final G ope, G ce);

	G objectOneOf(final Set<G> nis);

	G objectSomeValuesFrom(final G ope, final G ce);

	G objectUnionOf(final Set<G> set);

	G dataAllValuesFrom(final G ope, final G ce);

	G dataHasValue(final G ope, final G ni);

	G dataIntersectionOf(final Set<G> c);

	G dataUnionOf(final Set<G> c);

	G dataMinCardinality(final int card, final G ope, final G dr);

	G dataMaxCardinality(final int card, final G ope, final G dr);

	G dataExactCardinality(final int card, final G ope, final G dr);

	G dataOneOf(final Set<G> nis);

	G dataSomeValuesFrom(final G ope, final G ce);

	G inverseObjectProperty(final G op);    
                
    // Expression factory methods
    AllValuesFrom<G> allValuesFrom(final Term<G> ope, final Term<G> ce);

    ObjectComplementOf<G> objectComplementOf(final Term<G> c);

    ObjectHasSelf<G> objectHasSelf(final Term<G> ope);

    HasValue<G> hasValue(final Term<G> ope, final Term<G> ni);

    IntersectionOf<G> intersectionOf(final Set<Term<G>> c);
        
    MinCardinality<G> minCardinality(final int card, final Term<G> ope);

    MinCardinality<G> minCardinality(final int card, final Term<G> ope, final Term<G> ce);

    MaxCardinality<G> maxCardinality(final int card, final Term<G> ope);
        
	MaxCardinality<G> maxCardinality(final int card, final Term<G> ope, final Term<G> ce);

    ExactCardinality<G> exactCardinality(final int card, final Term<G> ope);
        
	ExactCardinality<G> exactCardinality(final int card, final Term<G> ope, Term<G> ce);

	Term<G> oneOf(final Set<Term<G>> nis);

    SomeValuesFrom<G> someValuesFrom(final Term<G> ope, final Term<G> ce);
 
    UnionOf<G> unionOf(final Set<Term<G>> set);
}
