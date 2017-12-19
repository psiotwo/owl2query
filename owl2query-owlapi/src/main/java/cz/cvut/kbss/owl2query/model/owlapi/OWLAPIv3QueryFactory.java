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
package cz.cvut.kbss.owl2query.model.owlapi;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import cz.cvut.kbss.owl2query.engine.AbstractOWL2QueryFactory;

public class OWLAPIv3QueryFactory extends AbstractOWL2QueryFactory<OWLObject> {

	final OWLDataFactory f;
	final OWLOntologyManager m;
	final OWLOntology o;

	public OWLAPIv3QueryFactory(final OWLOntologyManager m, final OWLOntology o) {
		this.m = m;
		this.o = o;
		this.f = m.getOWLDataFactory();
	}

	private OWLClassExpression getOWLClassExpression(final OWLObject ope) {
		if (ope instanceof OWLClassExpression) {
			return (OWLClassExpression) ope;
		} else if (ope instanceof OWLEntity) {
			return f.getOWLClass(((OWLEntity) ope).getIRI());
		} else {
			throw new IllegalArgumentException("Illegal object type: " + ope);
		}
	}

	private Set<OWLClassExpression> getOWLClassExpressions(
			final Set<OWLObject> ce) {
		final Set<OWLClassExpression> ces = new HashSet<OWLClassExpression>();

		for (final OWLObject o : ce) {
			ces.add(getOWLClassExpression(o));
		}

		return ces;
	}

	private Set<OWLDataRange> getOWLDataRanges(final Set<OWLObject> ce) {
		final Set<OWLDataRange> ces = new HashSet<OWLDataRange>();

		for (final OWLObject o : ce) {
			ces.add(getOWLDataRange(o));
		}

		return ces;
	}

	private OWLObjectPropertyExpression getOWLObjectPropertyExpression(
			final OWLObject ope) {
		if (ope instanceof OWLObjectPropertyExpression) {
			return (OWLObjectPropertyExpression) ope;
		} else if (ope instanceof OWLEntity) {
			return f.getOWLObjectProperty(((OWLEntity) ope).getIRI());
		} else {
			throw new IllegalArgumentException("Illegal object type: " + ope);
		}
	}

	private OWLDataPropertyExpression getOWLDataPropertyExpression(
			final OWLObject dpe) {
		if (dpe instanceof OWLDataPropertyExpression) {
			return (OWLDataPropertyExpression) dpe;
		} else if (dpe instanceof OWLEntity) {
			return f.getOWLDataProperty(((OWLEntity) dpe).getIRI());
		} else {
			throw new IllegalArgumentException("Illegal object type: " + dpe);
		}
	}

	private OWLNamedIndividual getOWLNamedIndividual(final OWLObject ni) {
		if (ni instanceof OWLNamedIndividual) {
			return (OWLNamedIndividual) ni;
		} else if (ni instanceof OWLEntity) {
			return f.getOWLNamedIndividual(((OWLEntity) ni).getIRI());
		} else {
			throw new IllegalArgumentException("Illegal object type: " + ni);
		}
	}

	private Set<OWLNamedIndividual> getOWLNamedIndividuals(
			final Set<OWLObject> nisO) {
		final Set<OWLNamedIndividual> nis = new HashSet<OWLNamedIndividual>();

		for (final OWLObject o : nisO) {
			nis.add(getOWLNamedIndividual(o));
		}

		return nis;
	}

	private OWLLiteral getOWLLiteral(final OWLObject ni) {
		if (ni instanceof OWLLiteral) {
			return (OWLLiteral) ni;
		} else {
			throw new IllegalArgumentException("Illegal object type: " + ni);
		}
	}

	private Set<OWLLiteral> getOWLLiterals(final Set<OWLObject> nisO) {
		final Set<OWLLiteral> nis = new HashSet<OWLLiteral>();

		for (final OWLObject o : nisO) {
			nis.add(getOWLLiteral(o));
		}

		return nis;
	}

	private OWLDataRange getOWLDataRange(final OWLObject ope) {
		if (ope instanceof OWLDataRange) {
			return (OWLDataRange) ope;
		} else if (ope instanceof OWLEntity) {
			return f.getOWLDatatype(((OWLEntity) ope).getIRI());
		} else {
			throw new IllegalArgumentException("Illegal object type: " + ope);
		}
	}

	
	public OWLClass getNothing() {
		return f.getOWLNothing();
	}

	
	public OWLClass getThing() {
		return f.getOWLThing();
	}

	
	public OWLDatatype getTopDatatype() {
		return f.getTopDatatype();
	}

	
	public OWLDataProperty getBottomDataProperty() {
		return f.getOWLBottomDataProperty();
	}

	
	public OWLObjectProperty getBottomObjectProperty() {
		return f.getOWLBottomObjectProperty();
	}

	
	public OWLDataProperty getTopDataProperty() {
		return f.getOWLTopDataProperty();
	}

	
	public OWLObjectProperty getTopObjectProperty() {
		return f.getOWLTopObjectProperty();
	}

	
	public OWLObject dataExactCardinality(int card, OWLObject dpe, OWLObject dr) {
		return f.getOWLDataExactCardinality(card,
				getOWLDataPropertyExpression(dpe), getOWLDataRange(dr));
	}

	
	public OWLObject dataMaxCardinality(int card, OWLObject dpe, OWLObject dr) {
		return f.getOWLDataMaxCardinality(card,
				getOWLDataPropertyExpression(dpe), getOWLDataRange(dr));
	}

	
	public OWLObject dataMinCardinality(int card, OWLObject dpe, OWLObject dr) {
		return f.getOWLDataMinCardinality(card,
				getOWLDataPropertyExpression(dpe), getOWLDataRange(dr));
	}

	
	public OWLObject dataOneOf(Set<OWLObject> nis) {
		return f.getOWLDataOneOf(getOWLLiterals(nis));
	}

	
	public OWLObject dataSomeValuesFrom(OWLObject dpe, OWLObject ce) {
		return f.getOWLDataSomeValuesFrom(getOWLDataPropertyExpression(dpe),
				getOWLDataRange(ce));
	}

	
	public OWLObject inverseObjectProperty(OWLObject op) {
		return f.getOWLObjectInverseOf(getOWLObjectPropertyExpression(op));
	}

	
	public OWLObject literal(String s) {
		return f.getOWLLiteral(s,OWL2Datatype.RDF_PLAIN_LITERAL);
	}

	
	public OWLObject literal(String s, String lang) {
		return f.getOWLLiteral(s, lang);
	}

	
	public OWLObject namedClass(String iri) {
		return f.getOWLClass(IRI.create(iri));
	}

	
	public OWLObject namedDataProperty(String iri) {
		return f.getOWLDataProperty(IRI.create(iri));
	}

	
	public OWLObject namedDataRange(String iri) {
		return f.getOWLDatatype(IRI.create(iri));
	}

	
	public OWLObject namedIndividual(String iri) {
		return f.getOWLNamedIndividual(IRI.create(iri));
	}

	
	public OWLObject namedObjectProperty(String iri) {
		return f.getOWLObjectProperty(IRI.create(iri));
	}

	
	public OWLObject objectAllValuesFrom(OWLObject ope, OWLObject ce) {
		return f.getOWLObjectAllValuesFrom(getOWLObjectPropertyExpression(ope),
				getOWLClassExpression(ce));
	}

	
	public OWLObject objectComplementOf(OWLObject c) {
		return f.getOWLObjectComplementOf(getOWLClassExpression(c));
	}

	
	public OWLObject objectExactCardinality(int card, OWLObject ope,
			OWLObject ce) {
		return f.getOWLObjectExactCardinality(card,
				getOWLObjectPropertyExpression(ope), getOWLClassExpression(ce));
	}

	
	public OWLObject objectHasSelf(OWLObject ope) {
		return f.getOWLObjectHasSelf(getOWLObjectPropertyExpression(ope));
	}

	
	public OWLObject objectHasValue(OWLObject ope, OWLObject ni) {
		return f.getOWLObjectHasValue(getOWLObjectPropertyExpression(ope),
				getOWLNamedIndividual(ni));
	}

	
	public OWLObject objectIntersectionOf(Set<OWLObject> c) {
		return f.getOWLObjectIntersectionOf(getOWLClassExpressions(c));
	}

	
	public OWLObject objectMaxCardinality(int card, OWLObject ope, OWLObject ce) {
		return f.getOWLObjectMaxCardinality(card,
				getOWLObjectPropertyExpression(ope), getOWLClassExpression(ce));
	}

	
	public OWLObject objectMinCardinality(int card, OWLObject ope, OWLObject ce) {
		return f.getOWLObjectMinCardinality(card,
				getOWLObjectPropertyExpression(ope), getOWLClassExpression(ce));
	}

	
	public OWLObject objectOneOf(Set<OWLObject> nis) {
		return f.getOWLObjectOneOf(getOWLNamedIndividuals(nis));
	}

	
	public OWLObject objectSomeValuesFrom(OWLObject ope, OWLObject ce) {
		return f.getOWLObjectSomeValuesFrom(
				getOWLObjectPropertyExpression(ope), getOWLClassExpression(ce));
	}

	
	public OWLObject objectUnionOf(Set<OWLObject> set) {
		return f.getOWLObjectUnionOf(getOWLClassExpressions(set));
	}

	
	public OWLObject typedLiteral(String s, String datatype) {
		return f.getOWLLiteral(s, f.getOWLDatatype(IRI.create(datatype)));

	}

	
	public OWLObject dataAllValuesFrom(OWLObject ope, OWLObject ce) {
		return f.getOWLDataAllValuesFrom(getOWLDataPropertyExpression(ope),
				getOWLDataRange(ce));
	}

	
	public OWLObject dataHasValue(OWLObject ope, OWLObject ni) {
		return f.getOWLDataHasValue(getOWLDataPropertyExpression(ope),
				getOWLLiteral(ni));
	}

	
	public OWLObject dataIntersectionOf(Set<OWLObject> c) {
		return f.getOWLDataIntersectionOf(getOWLDataRanges(c));
	}

	
	public OWLObject dataUnionOf(Set<OWLObject> c) {
		return f.getOWLDataUnionOf(getOWLDataRanges(c));
	}
}
