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

import cz.cvut.kbss.owl2query.engine.AbstractOWL2QueryFactory;

public class JRacerQueryFactory extends AbstractOWL2QueryFactory<OWLObject> {

	final OWLDataFactory f;
	final OWLOntologyManager m;
	final OWLOntology o;

	public JRacerQueryFactory(final OWLOntologyManager m, final OWLOntology o) {
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

	@Override
	public OWLClass getNothing() {
		return f.getOWLNothing();
	}

	@Override
	public OWLClass getThing() {
		return f.getOWLThing();
	}

	@Override
	public OWLDatatype getTopDatatype() {
		return f.getTopDatatype();
	}

	@Override
	public OWLDataProperty getBottomDataProperty() {
		return f.getOWLBottomDataProperty();
	}

	@Override
	public OWLObjectProperty getBottomObjectProperty() {
		return f.getOWLBottomObjectProperty();
	}

	@Override
	public OWLDataProperty getTopDataProperty() {
		return f.getOWLTopDataProperty();
	}

	@Override
	public OWLObjectProperty getTopObjectProperty() {
		return f.getOWLTopObjectProperty();
	}

	@Override
	public OWLObject dataExactCardinality(int card, OWLObject dpe, OWLObject dr) {
		return f.getOWLDataExactCardinality(card,
				getOWLDataPropertyExpression(dpe), getOWLDataRange(dr));
	}

	@Override
	public OWLObject dataMaxCardinality(int card, OWLObject dpe, OWLObject dr) {
		return f.getOWLDataMaxCardinality(card,
				getOWLDataPropertyExpression(dpe), getOWLDataRange(dr));
	}

	@Override
	public OWLObject dataMinCardinality(int card, OWLObject dpe, OWLObject dr) {
		return f.getOWLDataMinCardinality(card,
				getOWLDataPropertyExpression(dpe), getOWLDataRange(dr));
	}

	@Override
	public OWLObject dataOneOf(Set<OWLObject> nis) {
		return f.getOWLDataOneOf(getOWLLiterals(nis));
	}

	@Override
	public OWLObject dataSomeValuesFrom(OWLObject dpe, OWLObject ce) {
		return f.getOWLDataSomeValuesFrom(getOWLDataPropertyExpression(dpe),
				getOWLDataRange(ce));
	}

	@Override
	public OWLObject inverseObjectProperty(OWLObject op) {
		return f.getOWLObjectInverseOf(getOWLObjectPropertyExpression(op));
	}

	@Override
	public OWLObject literal(String s) {
		return f.getOWLStringLiteral(s);
	}

	@Override
	public OWLObject literal(String s, String lang) {
		return f.getOWLStringLiteral(s, lang);
	}

	@Override
	public OWLObject namedClass(String iri) {
		return f.getOWLClass(IRI.create(iri));
	}

	@Override
	public OWLObject namedDataProperty(String iri) {
		return f.getOWLDataProperty(IRI.create(iri));
	}

	@Override
	public OWLObject namedDataRange(String iri) {
		return f.getOWLDatatype(IRI.create(iri));
	}

	@Override
	public OWLObject namedIndividual(String iri) {
		return f.getOWLNamedIndividual(IRI.create(iri));
	}

	@Override
	public OWLObject namedObjectProperty(String iri) {
		return f.getOWLObjectProperty(IRI.create(iri));
	}

	@Override
	public OWLObject objectAllValuesFrom(OWLObject ope, OWLObject ce) {
		return f.getOWLObjectAllValuesFrom(getOWLObjectPropertyExpression(ope),
				getOWLClassExpression(ce));
	}

	@Override
	public OWLObject objectComplementOf(OWLObject c) {
		return f.getOWLObjectComplementOf(getOWLClassExpression(c));
	}

	@Override
	public OWLObject objectExactCardinality(int card, OWLObject ope,
			OWLObject ce) {
		return f.getOWLObjectExactCardinality(card,
				getOWLObjectPropertyExpression(ope), getOWLClassExpression(ce));
	}

	@Override
	public OWLObject objectHasSelf(OWLObject ope) {
		return f.getOWLObjectHasSelf(getOWLObjectPropertyExpression(ope));
	}

	@Override
	public OWLObject objectHasValue(OWLObject ope, OWLObject ni) {
		return f.getOWLObjectHasValue(getOWLObjectPropertyExpression(ope),
				getOWLNamedIndividual(ni));
	}

	@Override
	public OWLObject objectIntersectionOf(Set<OWLObject> c) {
		return f.getOWLObjectIntersectionOf(getOWLClassExpressions(c));
	}

	@Override
	public OWLObject objectMaxCardinality(int card, OWLObject ope, OWLObject ce) {
		return f.getOWLObjectMaxCardinality(card,
				getOWLObjectPropertyExpression(ope), getOWLClassExpression(ce));
	}

	@Override
	public OWLObject objectMinCardinality(int card, OWLObject ope, OWLObject ce) {
		return f.getOWLObjectMinCardinality(card,
				getOWLObjectPropertyExpression(ope), getOWLClassExpression(ce));
	}

	@Override
	public OWLObject objectOneOf(Set<OWLObject> nis) {
		return f.getOWLObjectOneOf(getOWLNamedIndividuals(nis));
	}

	@Override
	public OWLObject objectSomeValuesFrom(OWLObject ope, OWLObject ce) {
		return f.getOWLObjectSomeValuesFrom(
				getOWLObjectPropertyExpression(ope), getOWLClassExpression(ce));
	}

	@Override
	public OWLObject objectUnionOf(Set<OWLObject> set) {
		return f.getOWLObjectUnionOf(getOWLClassExpressions(set));
	}

	@Override
	public OWLObject typedLiteral(String s, String datatype) {
		return f.getOWLTypedLiteral(s, f.getOWLDatatype(IRI.create(datatype)));

	}

	@Override
	public OWLObject dataAllValuesFrom(OWLObject ope, OWLObject ce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLObject dataHasValue(OWLObject ope, OWLObject ni) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLObject dataIntersectionOf(Set<OWLObject> c) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLObject dataUnionOf(Set<OWLObject> c) {
		// TODO Auto-generated method stub
		return null;
	}
}
