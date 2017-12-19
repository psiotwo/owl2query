package cz.cvut.kbss.owl2query.model.pellet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.logic.Constant;
import org.semanticweb.kaon2.api.owl.elements.DataPropertyExpression;
import org.semanticweb.kaon2.api.owl.elements.DataRange;
import org.semanticweb.kaon2.api.owl.elements.Description;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.OWLEntity;
import org.semanticweb.kaon2.api.owl.elements.ObjectPropertyExpression;

import cz.cvut.kbss.owl2query.engine.AbstractOWL2QueryFactory;

public class KaonQueryFactory extends AbstractOWL2QueryFactory<Object> {

	private ObjectPropertyExpression asObjectPropertyExpression(Object o) {
		if (o instanceof ObjectPropertyExpression) {
			return (ObjectPropertyExpression) o;
		} else if (o instanceof OWLEntity) {
			final OWLEntity e = (OWLEntity) o;
			return KAON2Manager.factory().objectProperty(e.getURI());
		}

		throw new UnsupportedOperationException();
	}

	private DataPropertyExpression asDataPropertyExpression(Object o) {
		if (o instanceof DataPropertyExpression) {
			return (DataPropertyExpression) o;
		} else if (o instanceof OWLEntity) {
			final OWLEntity e = (OWLEntity) o;
			return KAON2Manager.factory().dataProperty(e.getURI());
		}

		throw new UnsupportedOperationException();
	}

	private Description asDescription(Object o) {
		if (o instanceof Description) {
			return (Description) o;
		} else if (o instanceof OWLEntity) {
			final OWLEntity e = (OWLEntity) o;
			return KAON2Manager.factory().owlClass(e.getURI());
		}

		throw new UnsupportedOperationException();
	}

	private Individual asIndividual(Object o) {
		if (o instanceof Individual) {
			return (Individual) o;
		} else if (o instanceof OWLEntity) {
			final OWLEntity e = (OWLEntity) o;
			return KAON2Manager.factory().individual(e.getURI());
		}

		throw new UnsupportedOperationException();
	}

	private DataRange asDataRange(Object o) {
		if (o instanceof DataRange) {
			return (DataRange) o;
		}

		throw new UnsupportedOperationException();
	}

	private Constant asConstant(Object o) {
		if (o instanceof Constant) {
			return (Constant) o;
		}

		throw new UnsupportedOperationException();
	}

	@Override
	public Object inverseObjectProperty(Object op) {
		return KAON2Manager.factory().inverseObjectProperty(
				asObjectPropertyExpression(op));
	}

	@Override
	public Object literal(String s) {
		throw new UnsupportedOperationException();
		// return KAON2Manager.factory().literal(arg0, arg1, arg2);
	}

	@Override
	public Object literal(String s, String lang) {
		throw new UnsupportedOperationException();
		// return KAON2Manager.factory().literal(arg0, arg1, arg2);
	}

	@Override
	public Object typedLiteral(String s, String dt) {
		throw new UnsupportedOperationException();
		// return KAON2Manager.factory().literal(arg0, arg1, arg2);
	}

	@Override
	public Object namedClass(String uri) {
		return KAON2Manager.factory().owlClass(uri);
	}

	@Override
	public Object namedDataProperty(String uri) {
		return KAON2Manager.factory().dataProperty(uri);
	}

	@Override
	public Object namedDataRange(String uri) {
		return KAON2Manager.factory().datatype(uri);
	}

	@Override
	public Object namedIndividual(String uri) {
		return KAON2Manager.factory().individual(uri);
	}

	@Override
	public Object namedObjectProperty(String uri) {
		return KAON2Manager.factory().objectProperty(uri);
	}

	@Override
	public Object objectAllValuesFrom(Object ope, Object ce) {
		return KAON2Manager.factory().objectAll(
				asObjectPropertyExpression(ope), asDescription(ce));
	}

	@Override
	public Object objectComplementOf(Object ce) {
		return KAON2Manager.factory().objectNot(asDescription(ce));
	}

	@Override
	public Object objectExactCardinality(int card, Object ope, Object ce) {
		return KAON2Manager.factory().objectCardinality(card, card,
				asObjectPropertyExpression(ope), asDescription(ce));
	}

	@Override
	public Object objectHasSelf(Object ope) {
		return KAON2Manager.factory().objectSelf(
				asObjectPropertyExpression(ope));
	}

	@Override
	public Object objectHasValue(Object ope, Object ni) {
		return KAON2Manager.factory().objectHasValue(
				asObjectPropertyExpression(ope), asIndividual(ni));
	}

	@Override
	public Object objectIntersectionOf(Set<Object> c) {
		final Set<Description> set = new HashSet<Description>();

		for (final Object o : c) {
			set.add(asDescription(o));
		}

		return KAON2Manager.factory().objectAnd(set);
	}

	@Override
	public Object objectMaxCardinality(int card, Object ope, Object ce) {
		return KAON2Manager.factory().objectCardinality(0, card,
				asObjectPropertyExpression(ope), asDescription(ce));
	}

	@Override
	public Object objectMinCardinality(int card, Object ope, Object ce) {
		return KAON2Manager.factory().objectCardinality(card, -1,
				asObjectPropertyExpression(ope), asDescription(ce));
	}

	@Override
	public Object objectSomeValuesFrom(Object ope, Object ce) {
		return KAON2Manager.factory().objectSome(
				asObjectPropertyExpression(ope), asDescription(ce));
	}

	@Override
	public Object objectUnionOf(Set<Object> c) {
		final Set<Description> set = new HashSet<Description>();

		for (final Object o : c) {
			set.add(asDescription(o));
		}

		return KAON2Manager.factory().objectOr(set);
	}

	@Override
	public Object getNothing() {
		return KAON2Manager.factory().nothing();
	}

	@Override
	public Object getThing() {
		return KAON2Manager.factory().thing();
	}

	@Override
	public Object getTopDatatype() {
		return KAON2Manager.factory().rdfsLiteral();
	}

	@Override
	public Object dataSomeValuesFrom(Object dpe, Object ce) {
		return KAON2Manager.factory().dataSome(asDataRange(ce),
				asDataPropertyExpression(dpe));
	}

	@Override
	public Object dataExactCardinality(int card, Object dpe, Object dr) {
		return KAON2Manager.factory().dataCardinality(card, card,
				asDataPropertyExpression(dpe), asDataRange(dr));
	}

	@Override
	public Object dataMaxCardinality(int card, Object ope, Object dr) {
		return KAON2Manager.factory().dataCardinality(0, card,
				asDataPropertyExpression(ope), asDataRange(dr));
	}

	@Override
	public Object dataMinCardinality(int card, Object ope, Object dr) {
		return KAON2Manager.factory().dataCardinality(card, 0,
				asDataPropertyExpression(ope), asDataRange(dr));
	}

	@Override
	public Object getBottomDataProperty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getBottomObjectProperty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getTopDataProperty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getTopObjectProperty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object dataOneOf(Set<Object> nis) {
		Collection<Constant> c = new HashSet<Constant>();

		for (final Object a : nis) {
			c.add(asConstant(a));
		}

		return KAON2Manager.factory().dataOneOf(c);
	}

	@Override
	public Object objectOneOf(Set<Object> nis) {
		Collection<Individual> c = new HashSet<Individual>();

		for (final Object a : nis) {
			c.add(asIndividual(a));
		}

		return KAON2Manager.factory().objectOneOf(c);
	}

	@Override
	public Object dataAllValuesFrom(Object ope, Object ce) {
		return KAON2Manager.factory().dataAll(asDataRange(ce),
				asDataPropertyExpression(ope));
	}

	@Override
	public Object dataHasValue(Object ope, Object ni) {
		return KAON2Manager.factory().dataHasValue(
				asDataPropertyExpression(ope), asConstant(ni));
	}

	@Override
	public Object dataIntersectionOf(Set<Object> c) {
		// final Set<DataRange> set = new HashSet<DataRange>();
		//
		// for (final Object o : c) {
		// set.add(asDataRange(o));
		// }
		//
		// return KAON2Manager.factory().dataAnd(set);
		throw new UnsupportedOperationException();
	}

	@Override
	public Object dataUnionOf(Set<Object> c) {
		// final Set<DataRange> set = new HashSet<DataRange>();
		//
		// for (final Object o : c) {
		// set.add(asDataRange(o));
		// }
		//
		// return KAON2Manager.factory().dataOr(set);
		throw new UnsupportedOperationException();
	}
}
