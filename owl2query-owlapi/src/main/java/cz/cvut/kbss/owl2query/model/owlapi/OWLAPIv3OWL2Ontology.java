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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.stream.Collectors;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import cz.cvut.kbss.owl2query.model.Hierarchy;
import cz.cvut.kbss.owl2query.model.InternalReasonerException;
import cz.cvut.kbss.owl2query.model.OWL2Ontology;
import cz.cvut.kbss.owl2query.model.OWL2QueryException;
import cz.cvut.kbss.owl2query.model.OWL2QueryFactory;
import cz.cvut.kbss.owl2query.model.OWLObjectType;
import cz.cvut.kbss.owl2query.model.SizeEstimate;
import cz.cvut.kbss.owl2query.model.SizeEstimateImpl;
import org.semanticweb.owlapi.search.EntitySearcher;

public class OWLAPIv3OWL2Ontology implements OWL2Ontology<OWLObject> {

	private static final Logger LOG = Logger
			.getLogger(OWLAPIv3OWL2Ontology.class.getName());

	private final OWLOntology o;
	private final OWLOntologyManager m;
	private final OWLDataFactory f;
	private final OWLReasoner r;
	private final OWL2QueryFactory<OWLObject> factory;
	private final SizeEstimate<OWLObject> sizeEstimate;

	private final OWLReasoner structuralReasoner;

	public OWLAPIv3OWL2Ontology(final OWLOntologyManager m, final OWLOntology o, final OWLReasoner r) {
		this.o = o;
		this.m = m;
		this.f = m.getOWLDataFactory();
		this.r = r;
		structuralReasoner = new StructuralReasonerFactory().createReasoner(o);
		structuralReasoner.precomputeInferences(InferenceType.values());
		this.factory = new OWLAPIv3QueryFactory(m, o);

		this.sizeEstimate = new SizeEstimateImpl<>(this);
	}

	private OWLNamedIndividual asOWLNamedIndividual(final OWLObject e) {
		if (e instanceof OWLNamedIndividual) {
			return (OWLNamedIndividual) e;
		} else if (e instanceof OWLEntity) {
			return f.getOWLNamedIndividual(((OWLEntity) e).getIRI());
		} else {
			throw new InternalReasonerException();
		}
	}

	private OWLLiteral asOWLLiteral(final OWLObject e) {
		if (e instanceof OWLLiteral) {
			return (OWLLiteral) e;
		} else {
			throw new InternalReasonerException();
		}
	}

	private OWLClassExpression asOWLClassExpression(final OWLObject e) {
		if (e instanceof OWLClassExpression) {
			return (OWLClassExpression) e;
		} else if (e instanceof OWLEntity) {
			final OWLEntity ee = (OWLEntity) e;
			// if (o.containsClassInSignature(ee.getIRI())) {
			return f.getOWLClass(ee.getIRI());
			// }
		}

		return null;
	}

	private OWLPropertyExpression asOWLPropertyExpression(
			final OWLObject e) {
		if (e instanceof OWLEntity) {
			final OWLEntity ee = (OWLEntity) e;
			if (is(ee, OWLObjectType.OWLDataProperty)) {
				return f.getOWLDataProperty(ee.getIRI());
			} else {
				return f.getOWLObjectProperty(ee.getIRI());
			}
		} else if (e instanceof OWLPropertyExpression) {
			return (OWLPropertyExpression) e;
		}
		throw new IllegalArgumentException();
	}

	private OWLObjectProperty asOWLObjectProperty(final OWLObject e) {
		if (e instanceof OWLObjectProperty) {
			return (OWLObjectProperty) e;
		} else if (e instanceof OWLEntity) {
			final OWLEntity ee = (OWLEntity) e;
			if (is(ee, OWLObjectType.OWLObjectProperty)) {
				return f.getOWLObjectProperty(ee.getIRI());
			}
		}

		return null;
	}

	public Set<OWLClass> getClasses() {
		Set<OWLClass> set = o.classesInSignature(Imports.INCLUDED).collect(Collectors.toSet());
		set.add(f.getOWLThing());
		set.add(f.getOWLNothing());
		return set;
	}

	public Set<OWLObjectProperty> getObjectProperties() {
		final Set<OWLObjectProperty> set = o.objectPropertiesInSignature(Imports.INCLUDED).collect(Collectors.toSet());
		set.add(f.getOWLBottomObjectProperty());
		set.add(f.getOWLTopObjectProperty());
		return set;
	}

	public Set<OWLDataProperty> getDataProperties() {
		final Set<OWLDataProperty> set = o.dataPropertiesInSignature(Imports.INCLUDED).collect(Collectors.toSet());
		set.add(f.getOWLBottomDataProperty());
		set.add(f.getOWLTopDataProperty());
		return set;
	}

	public Set<OWLNamedIndividual> getIndividuals() {
		return o.individualsInSignature(Imports.INCLUDED).collect(Collectors.toSet());
	}

	public Set<OWLLiteral> getLiterals() {
		Set<OWLLiteral> set = new HashSet<>();
		for (OWLIndividual i : getIndividuals()) {
			o.dataPropertyAssertionAxioms(i).map(OWLPropertyAssertionAxiom::getObject).forEach(set::add);
		}
		return set;
	}

	public Set<? extends OWLObject> getDifferents(OWLObject i) {
		if (!(i instanceof OWLEntity)) {
			throw new InternalReasonerException();
		}

		return r.getDifferentIndividuals(asOWLNamedIndividual(i)).entities().collect(Collectors.toSet());
	}

	public Set<? extends OWLObject> getDomains(OWLObject pred) {
		final OWLPropertyExpression ope = asOWLPropertyExpression(pred);

		if (ope != null) {
			if (ope.isAnonymous()) {
				throw new InternalReasonerException();
			} else if (ope.isObjectPropertyExpression()) {
				r.getObjectPropertyDomains(ope.asOWLObjectProperty(), true); // TODO
			} else if (ope.isDataPropertyExpression()) {
				r.getDataPropertyDomains(ope.asOWLDataProperty(),true); // TODO
			}
		}

		throw new InternalReasonerException();
	}

	public Set<? extends OWLObject> getEquivalentClasses(OWLObject ce) {
		final OWLClassExpression c = asOWLClassExpression(ce);

		return r.getEquivalentClasses(c).entities().collect(Collectors.toSet());
	}

	public Set<? extends OWLObject> getInverses(OWLObject ope) {
		final OWLPropertyExpression opex = asOWLPropertyExpression(ope);

		if (opex.isObjectPropertyExpression()) {

			if (opex.isAnonymous()) {
				return r.getEquivalentObjectProperties(
						((OWLObjectPropertyExpression) opex).getNamedProperty()).entities().collect(Collectors.toSet());
			} else {
				return r.getInverseObjectProperties(
						((OWLObjectPropertyExpression) opex).getNamedProperty()).entities().collect(Collectors.toSet());
			}
		}
		throw new InternalReasonerException();
	}

	public Set<? extends OWLObject> getRanges(OWLObject pred) {
		final OWLPropertyExpression ope = asOWLPropertyExpression(pred);

		if (ope != null) {
			if (ope.isAnonymous()) {
				throw new InternalReasonerException();
			} else if (ope.isObjectPropertyExpression()) {
				return r.getObjectPropertyRanges(ope.asOWLObjectProperty(), true).entities().collect(Collectors.toSet()); // TODO
			} else if (ope.isDataPropertyExpression()) {
				return EntitySearcher.getRanges(ope.asOWLDataProperty(), o).collect(Collectors.toSet()); // TODO
			}
		}
		throw new InternalReasonerException();
	}

	public Set<? extends OWLObject> getSames(OWLObject i) {
		if (!(i instanceof OWLEntity)) {
			throw new InternalReasonerException();
		}

		return r.getSameIndividuals(asOWLNamedIndividual(i)).entities().collect(Collectors.toSet());
	}

	public Set<OWLClass> getTypes(OWLObject i, boolean direct) {
		if (!(i instanceof OWLEntity)) {
			throw new InternalReasonerException();
		}

		return r.getTypes(asOWLNamedIndividual(i), direct).entities().collect(Collectors.toSet());
	}

	public boolean is(OWLObject e, final OWLObjectType... tt) {
		boolean result = false;

		for (final OWLObjectType t : tt) {
			switch (t) {
			case OWLLiteral:
				result = e instanceof OWLLiteral;
				break;
			case OWLAnnotationProperty:
				if (e instanceof OWLEntity) {
					result = o.containsAnnotationPropertyInSignature(((OWLEntity) e).getIRI(), Imports.INCLUDED);
				}
				break;
			case OWLDataProperty:
				if (e instanceof OWLEntity) {
					result = o.containsDataPropertyInSignature(((OWLEntity) e).getIRI(), Imports.INCLUDED)
							|| e.equals(f.getOWLTopDataProperty())
							|| e.equals(f.getOWLBottomDataProperty());
				}
				break;
			case OWLObjectProperty:
				if (e instanceof OWLEntity) {
					result = o.containsObjectPropertyInSignature(((OWLEntity) e).getIRI(), Imports.INCLUDED)
							|| (e.equals(f.getOWLTopObjectProperty()) || e
									.equals(f.getOWLBottomObjectProperty()));
				}
				break;
			case OWLClass:
				if (e instanceof OWLEntity) {
					result = o.containsClassInSignature(((OWLEntity) e).getIRI(), Imports.INCLUDED)
							|| e.equals(f.getOWLThing())
							|| e.equals(f.getOWLNothing());
				}
				break;
			case OWLNamedIndividual:
				if (e instanceof OWLEntity) {
					result = o.containsIndividualInSignature(((OWLEntity) e).getIRI(), Imports.INCLUDED);
				}

				break;
			default:
				break;
			}
			if (result) {
				break;
			}
		}

		return result;
	}

	public boolean isSameAs(OWLObject i1, OWLObject i2) {
		final OWLIndividual ii1 = asOWLNamedIndividual(i1);
		final OWLIndividual ii2 = asOWLNamedIndividual(i2);

		if (i1.equals(i2)) {
			return true;
		}

		return r.isEntailed(f.getOWLSameIndividualAxiom(ii1, ii2));
	}

	public boolean isDifferentFrom(OWLObject i1, OWLObject i2) {
		if ((!(i1 instanceof OWLEntity)) || (!(i2 instanceof OWLEntity))) {
			throw new InternalReasonerException();
		}

		return r.isEntailed(f.getOWLDifferentIndividualsAxiom(
				asOWLNamedIndividual(i1),
				asOWLNamedIndividual(i2)));
	}

	public boolean isTypeOf(OWLObject ce, OWLObject i, boolean direct) {

		if (is(i, OWLObjectType.OWLLiteral)) {
			return false;
		}

		final OWLNamedIndividual ii = asOWLNamedIndividual(i);
		final OWLClassExpression cce = asOWLClassExpression(ce);

		if (direct) {
			return r.getInstances(cce, true).containsEntity(ii);
		} else {
			return r.isEntailed(f.getOWLClassAssertionAxiom(cce, ii));
		}
	}

	public void ensureConsistency() {
		if (LOG.isLoggable(Level.CONFIG)) {
			LOG.config("Ensure consistency");
		}

		if (LOG.isLoggable(Level.CONFIG)) {
			LOG.config("	* isConsistent ?");
		}
		if (!r.isConsistent()) {
			throw new InternalReasonerException();
		}
		if (LOG.isLoggable(Level.CONFIG)) {
			LOG.config("	* true");
		}
	}

	public Set<? extends OWLObject> getIndividualsWithProperty(OWLObject pvP,
			OWLObject pvIL) {
		final OWLPropertyExpression pex = asOWLPropertyExpression(pvP);

		final Set<OWLObject> set = new HashSet<>();

		if (pex != null) {
			if (pex.isObjectPropertyExpression()) {
				if (!is(pvIL, OWLObjectType.OWLNamedIndividual)) {
					return set;
				}

				final OWLNamedIndividual object = asOWLNamedIndividual(pvIL);

				for (final OWLNamedIndividual i : getIndividuals()) {
					if (r.isEntailed(f.getOWLObjectPropertyAssertionAxiom(
							(OWLObjectPropertyExpression) pex, i, object))) {
						set.add(i);
					}
				}
			} else if (pex.isDataPropertyExpression()) {
				if (!is(pvIL, OWLObjectType.OWLLiteral)) {
					return set;
				}

				final OWLLiteral object = asOWLLiteral(pvIL);

				for (final OWLNamedIndividual i : getIndividuals()) {
					if (r.isEntailed(f.getOWLDataPropertyAssertionAxiom(
							(OWLDataPropertyExpression) pex, i, object))) {
						set.add(i);
					}
				}
			}
		}

		return set;
	}

	public Set<? extends OWLObject> getPropertyValues(OWLObject pvP,
			OWLObject pvI) {
		final OWLPropertyExpression pex = asOWLPropertyExpression(pvP);
		final OWLNamedIndividual ni = asOWLNamedIndividual(pvI);

		if (pex != null) {
			if (pex.isObjectPropertyExpression()) {

				if (pex.isOWLTopObjectProperty()) {
					return getIndividuals();
				} else {
					return r.getObjectPropertyValues(ni,
							(OWLObjectPropertyExpression) pex).entities().collect(Collectors.toSet());
				}
			} else if (pex.isDataPropertyExpression()) {
				if (pex.isOWLTopDataProperty()) {
					return getLiterals();
				} else {
					return r.getDataPropertyValues(ni, (OWLDataProperty) pex);
				}
			}
		}
		throw new IllegalArgumentException();
	}

	public SizeEstimate<OWLObject> getSizeEstimate() {
		return sizeEstimate;
	}

	public boolean hasPropertyValue(OWLObject p, OWLObject s, OWLObject o) {
		final OWLPropertyExpression pex = asOWLPropertyExpression(p);

		if (pex.isObjectPropertyExpression()) {
			return r.isEntailed(f.getOWLObjectPropertyAssertionAxiom(
					(OWLObjectPropertyExpression) pex, asOWLNamedIndividual(s),
					asOWLNamedIndividual(o)));
		} else if (pex.isDataPropertyExpression()) {
			return r.isEntailed(f.getOWLDataPropertyAssertionAxiom(
					(OWLDataPropertyExpression) pex, asOWLNamedIndividual(s),
					asOWLLiteral(o)));

		}
		return false;
	}

	public boolean isClassAlwaysNonEmpty(OWLObject sc) {
		final OWLAxiom axiom = f.getOWLSubClassOfAxiom(asOWLClassExpression(sc), f.getOWLNothing());

		try {
			m.applyChange(new AddAxiom(o, axiom));

			boolean classAlwaysNonEmpty = !r.isConsistent();

			m.applyChange(new RemoveAxiom(o, axiom));

			return classAlwaysNonEmpty;
		} catch (OWLOntologyChangeException e) {
			throw new InternalReasonerException();
		}
	}

	public boolean isClassified() {
		// TODO
		// return r.isClassified();
		return false;
	}

	public boolean isSatisfiable(OWLObject arg) {
		return r.isSatisfiable(asOWLClassExpression(arg));
	}

	public Set<? extends OWLObject> retrieveIndividualsWithProperty(
			OWLObject odpe) {
		final OWLPropertyExpression ope = asOWLPropertyExpression(odpe);

		final Set<OWLObject> set = new HashSet<>();
		try {
			if (ope.isObjectPropertyExpression()) {
				for (final OWLNamedIndividual i : getIndividuals()) {
					if (!r.getObjectPropertyValues(i,
							(OWLObjectPropertyExpression) ope).isEmpty()) {
						set.add(i);
					}
				}
			} else if (ope.isObjectPropertyExpression()) {
				for (final OWLNamedIndividual i : getIndividuals()) {
					if (!r.getObjectPropertyValues(i,
							(OWLObjectPropertyExpression) ope).isEmpty()) {
						set.add(i);
					}
				}
			}
		} catch (Exception e) {
			throw new InternalReasonerException(e);
		}
		return set;
	}

	public Map<OWLObject, Boolean> getKnownInstances(final OWLObject ce) {
		final Map<OWLObject, Boolean> m = new HashMap<>();
		final OWLClassExpression cex = asOWLClassExpression(ce);

		for (final OWLObject x : getIndividuals()) {
			m.put(x, false);
		}

		if (!cex.isAnonymous()) {
			final OWLClass owlClass = cex.asOWLClass();
			for (final OWLObject x : EntitySearcher.getIndividuals(owlClass, o).collect(Collectors.toList())) {
				m.put(x, true);
			}
		}

		return m;
	}

	public Boolean isKnownTypeOf(OWLObject ce, OWLObject i) {
		final OWLIndividual ii = asOWLNamedIndividual(i);

		if (EntitySearcher.getTypes(ii, o).collect(Collectors.toList()).contains(ce)) {
			return true;
		}

		return null;
	}

	public Boolean hasKnownPropertyValue(OWLObject p, OWLObject s, OWLObject ob) {
		final OWLIndividual is = asOWLNamedIndividual(s);
		final OWLPropertyExpression pex = asOWLPropertyExpression(p);

		if (pex != null) {
			if (pex.isObjectPropertyExpression()) {
				final OWLObjectPropertyExpression ope = ((OWLObjectPropertyExpression) p);

				if (ope instanceof OWLObjectInverseOf) {
					final OWLObjectPropertyExpression opeInv = ope.getInverseProperty();

					return o.axioms(AxiomType.OBJECT_PROPERTY_ASSERTION).anyMatch(ax -> ax.getObject().equals(s)
							&& ax.getProperty().equals(opeInv)
							&& ax.getSubject().equals(ob));
				} else {
					return EntitySearcher.getObjectPropertyValues(is, (OWLObjectPropertyExpression) pex, o).collect(Collectors.toList()).contains(ob);
				}
			} else if (pex.isDataPropertyExpression()) {
				return EntitySearcher.getDataPropertyValues(is, (OWLDataPropertyExpression) pex, o).collect(Collectors.toList()).contains(ob);
			}
		}

		return false;
	}

	public Set<? extends OWLObject> getInstances(OWLObject ic, boolean direct) {
		final OWLClassExpression c = asOWLClassExpression(ic);

		return r.getInstances(c, direct).entities().collect(Collectors.toSet());
	}

	public OWL2QueryFactory<OWLObject> getFactory() {
		return factory;
	}

	public boolean isRealized() {
		// try {
		// return r.isRealised();
		// } catch (OWLReasonerException e) {
		// throw new InternalReasonerException();
		// }
		// TODO
		return false;
	}

	public boolean isComplexClass(OWLObject c) {
		return (c instanceof OWLClassExpression);
	}

	public Collection<? extends OWLObject> getKnownPropertyValues(
			OWLObject pvP, OWLObject pvI) {

		final OWLPropertyExpression p = asOWLPropertyExpression(pvP);
		final OWLNamedIndividual ni = asOWLNamedIndividual(pvI);

		Collection<? extends OWLObject> result = Collections.emptySet();

		if (p == null || ni == null) {
			return result;
		}

		if (p.isObjectPropertyExpression()) {
			result = structuralReasoner.getObjectPropertyValues(ni,
					(OWLObjectPropertyExpression) p).getFlattened();
		} else if (p.isDataPropertyExpression() && !p.isAnonymous()) {
			result = structuralReasoner.getDataPropertyValues(ni,
					(OWLDataProperty) p);
		} else {
			throw new IllegalArgumentException();
		}

		return result;
	}

	private final Hierarchy<OWLObject, OWLClass> classHierarchy = new Hierarchy<OWLObject, OWLClass>() {

		public Set<OWLClass> getEquivs(OWLObject ce) {
			final OWLClassExpression cex = asOWLClassExpression(ce);
			return r.getEquivalentClasses(cex).entities().collect(Collectors.toSet());
		}

		public Set<OWLClass> getSubs(OWLObject superCE, boolean direct) {
			final OWLClassExpression cex = asOWLClassExpression(superCE);
			Set<OWLClass> set = r.getSubClasses(cex, direct).entities().collect(Collectors.toSet());
			if (!direct) {
				set.add(f.getOWLNothing());
			}
			return set;
		}

		public Set<OWLClass> getSupers(OWLObject superCE, boolean direct) {
			final OWLClassExpression cex = asOWLClassExpression(superCE);
			Set<OWLClass> set = r.getSuperClasses(cex, direct).entities().collect(Collectors.toSet());
			if (!direct) {
				set.add(f.getOWLThing());
			}
			return set;
		}

		@Override
		public boolean isEquiv(OWLObject equivG1, OWLObject equivG2) {
			return r.isEntailed(f.getOWLEquivalentClassesAxiom(
					asOWLClassExpression(equivG1),
					asOWLClassExpression(equivG2)));
		}

		@Override
		public boolean isSub(OWLObject subG1, OWLObject superG2, boolean direct) {
			return r.isEntailed(f.getOWLSubClassOfAxiom(
					asOWLClassExpression(subG1), asOWLClassExpression(superG2)));
			// return getSubs(superG2,direct).contains(subG1);
		}

		@Override
		public boolean isDisjointWith(OWLObject disjointG1, OWLObject disjointG2) {
			return r.isEntailed(f.getOWLDisjointClassesAxiom(
					asOWLClassExpression(disjointG1),
					asOWLClassExpression(disjointG2)));
		}

		@Override
		public Set<OWLClass> getComplements(OWLObject complementG) {
			return r.getEquivalentClasses(
					f.getOWLObjectComplementOf(asOWLClassExpression(complementG))).entities().collect(Collectors.toSet());
		}

		@Override
		public boolean isComplementWith(OWLObject complementG1,
				OWLObject complementG2) {
			return r.isEntailed(f.getOWLEquivalentClassesAxiom(
					f.getOWLObjectComplementOf(asOWLClassExpression(complementG1)),
					asOWLClassExpression(complementG2)));
		}

		public Set<OWLClass> getTops() {
			return Collections.singleton(f.getOWLThing());
		}

		public Set<OWLClass> getBottoms() {
			return Collections.singleton(f.getOWLNothing());
		}

		public Set<OWLClass> getDisjoints(OWLObject disjointG) {
			return r.getDisjointClasses(asOWLClassExpression(disjointG)).entities().collect(Collectors.toSet());
		}

	};

	public Hierarchy<OWLObject, OWLClass> getClassHierarchy() {
		return classHierarchy;
	}

	private final Hierarchy<OWLObject, OWLClass> toldClassHierarchy = new Hierarchy<OWLObject, OWLClass>() {

		public Set<OWLClass> getEquivs(OWLObject ce) {
			final OWLClassExpression cex = asOWLClassExpression(ce);
			if (cex.isAnonymous()) {
				return Collections.emptySet();
			} else {
				return structuralReasoner.getEquivalentClasses(cex).entities().collect(Collectors.toSet());
				// final Set<OWLClass> set = new HashSet<OWLClass>();
				// for (final OWLClassExpression oce : cex.asOWLClass()
				// .getEquivalentClasses(o)) {
				// if (!oce.isAnonymous()) {
				// set.add(oce.asOWLClass());
				// }
				// }
				// return set;
			}
		}

		public Set<OWLClass> getSubs(OWLObject superCE, boolean direct) {
			final OWLClassExpression cex = asOWLClassExpression(superCE);
			if (cex.isAnonymous()) {
				return Collections.emptySet();
			} else {
				// final Set<OWLClass> set = new HashSet<OWLClass>();
				return structuralReasoner.getSubClasses(cex, direct).entities().collect(Collectors.toSet());
				//
				// for (final OWLClassExpression oce : cex.asOWLClass()
				// .getSubClasses(o)) {
				// if (!oce.isAnonymous() && !set.contains(oce.asOWLClass())) {
				// set.add(oce.asOWLClass());
				// if (!direct) {
				// set.addAll(getSubs(oce, direct));
				// }
				// }
				// }
				//
				// return set;
			}
		}

		public Set<OWLClass> getSupers(OWLObject superCE, boolean direct) {
			final OWLClassExpression cex = asOWLClassExpression(superCE);
			if (cex.isAnonymous()) {
				return Collections.emptySet();
			} else {
				return structuralReasoner.getSuperClasses(cex, direct).entities().collect(Collectors.toSet());
				//
				// final Set<OWLClass> set = new HashSet<OWLClass>();
				// for (final OWLClassExpression oce : cex.asOWLClass()
				// .getSuperClasses(o)) {
				// if (!oce.isAnonymous() && !set.contains(oce.asOWLClass())) {
				// set.add(oce.asOWLClass());
				// if (!direct) {
				// set.addAll(getSupers(oce, direct));
				// }
				// }
				// }
				//
				// return set;
			}
		}

		public Set<OWLClass> getTops() {
			return Collections.singleton(f.getOWLThing());
		}

		public Set<OWLClass> getBottoms() {
			return Collections.singleton(f.getOWLNothing());
		}

		public Set<OWLClass> getDisjoints(OWLObject disjointG) {
			final OWLClassExpression cex = asOWLClassExpression(disjointG);
			if (cex.isAnonymous()) {
				return Collections.emptySet();
			} else {
				return structuralReasoner.getDisjointClasses(cex).entities().collect(Collectors.toSet());
			}
		}

		@Override
		public boolean isEquiv(OWLObject equivG1, OWLObject equivG2) {
			return structuralReasoner.isEntailed(f
					.getOWLEquivalentClassesAxiom(
							asOWLClassExpression(equivG1),
							asOWLClassExpression(equivG2)));
		}

		@Override
		public boolean isSub(OWLObject subG1, OWLObject superG2, boolean direct) {
			return getSubs(superG2, direct).contains(subG1);
		}

		@Override
		public boolean isDisjointWith(OWLObject disjointG1, OWLObject disjointG2) {
			return structuralReasoner.isEntailed(f.getOWLDisjointClassesAxiom(
					asOWLClassExpression(disjointG1),
					asOWLClassExpression(disjointG2)));
		}

		@Override
		public Set<OWLClass> getComplements(OWLObject complementG) {
			return structuralReasoner
					.getEquivalentClasses(
							f.getOWLObjectComplementOf(asOWLClassExpression(complementG))).entities().collect(
							Collectors.toSet());
		}

		@Override
		public boolean isComplementWith(OWLObject complementG1,
				OWLObject complementG2) {
			return structuralReasoner
					.isEntailed(f.getOWLEquivalentClassesAxiom(
							f.getOWLObjectComplementOf(asOWLClassExpression(complementG1)),
							asOWLClassExpression(complementG2)));
		}

	};

	public Hierarchy<OWLObject, OWLClass> getToldClassHierarchy() {
		return toldClassHierarchy;
	}

	private final Hierarchy<OWLObject, OWLProperty> propertyHierarchy = new Hierarchy<OWLObject, OWLProperty>() {

		public Set<OWLProperty> getEquivs(OWLObject ce) {
			final OWLPropertyExpression cex = asOWLPropertyExpression(ce);

			if (cex.isDataPropertyExpression()) {
				return r.getEquivalentDataProperties((OWLDataProperty) cex).entities().collect(Collectors.toSet());
			} else if (cex.isObjectPropertyExpression()) {
				return r.getEquivalentObjectProperties((OWLObjectProperty) cex).entities()
						.filter(ex -> !ex.isAnonymous()).map(AsOWLObjectProperty::asOWLObjectProperty).collect(
						Collectors.toSet());
			} else {
				throw new InternalReasonerException();
			}
		}

		public Set<OWLProperty> getSubs(OWLObject superCE, boolean direct) {
			final OWLPropertyExpression cex = asOWLPropertyExpression(superCE);
			// if (cex.equals(f.getOWLBottomObjectProperty())) {
			// return Collections.singleton((OWLProperty)
			// f.getOWLBottomObjectProperty());
			// } else if (cex.equals(f.getOWLTopObjectProperty())) {
			// final Set<OWLProperty> set = new HashSet<OWLProperty>();
			// set.addAll(getObjectProperties());
			//
			// if (direct) {
			// for (OWLProperty op : new HashSet<OWLProperty>(set)) {
			// if (!getSupers(op, true).contains(
			// f.getOWLTopObjectProperty())) {
			// set.remove(op);
			// }
			// }
			// }
			//
			// set.add(f.getOWLBottomObjectProperty());
			//
			// return set;
			// } else if (cex.equals(f.getOWLTopDataProperty())) {
			// final Set<OWLProperty> set = new HashSet<OWLProperty>();
			// set.addAll(getDataProperties());
			//
			// if (direct) {
			// for (OWLProperty op : new HashSet<OWLProperty>(set)) {
			// if (!getSupers(op, true).contains(
			// f.getOWLTopDataProperty())) {
			// set.remove(op);
			// }
			// }
			// }
			//
			// set.add(f.getOWLBottomObjectProperty());
			//
			// return set;
			// }

			final Set<OWLProperty> set = new HashSet<>();

			if (cex.isDataPropertyExpression()) {
				set.addAll(r
						.getSubDataProperties((OWLDataProperty) cex, direct).entities().collect(Collectors.toSet()));
				if (!direct) {
					set.add(f.getOWLBottomDataProperty());
				}
			} else if (cex.isObjectPropertyExpression()) {
				r.getSubObjectProperties((OWLObjectProperty) cex, direct).entities()
				 .filter(ex -> !ex.isAnonymous()).forEach(ex -> set.add(ex.asOWLObjectProperty()));
				if (!direct) {
					set.add(f.getOWLBottomObjectProperty());
				}
			} else {
				throw new InternalReasonerException();
			}

			return set;
		}

		public Set<OWLProperty> getSupers(OWLObject superCE, boolean direct) {
			final OWLPropertyExpression cex = asOWLPropertyExpression(superCE);
			//
			// if (cex.equals(f.getOWLTopObjectProperty())) {
			// return Collections.emptySet();
			// } else if (cex.equals(f.getOWLBottomObjectProperty())) {
			// final Set<OWLProperty> set = new HashSet<OWLProperty>();
			// set.addAll(getObjectProperties());
			//
			// if (direct) {
			// for (OWLProperty op : new HashSet<OWLProperty>(set)) {
			// if (!getSubs(op, true).contains(
			// f.getOWLBottomObjectProperty())) {
			// set.remove(op);
			// }
			// }
			// } else {
			// set.add(f.getOWLTopObjectProperty());
			// }
			//
			// return set;
			// } else if (cex.equals(f.getOWLBottomDataProperty())) {
			// final Set<OWLProperty> set = new HashSet<OWLProperty>();
			// set.addAll(getDataProperties());
			//
			// if (direct) {
			// for (OWLProperty op : new HashSet<OWLProperty>(set)) {
			// if (!getSubs(op, true).contains(
			// f.getOWLBottomDataProperty())) {
			// set.remove(op);
			// }
			// }
			// } else {
			// set.add(f.getOWLTopDataProperty());
			// }
			// return set;
			// }
			final Set<OWLProperty> set = new HashSet<>();

			if (cex.isDataPropertyExpression()) {
				set.addAll(r.getSuperDataProperties((OWLDataProperty) cex,
						direct).entities().collect(Collectors.toSet()));

				if (!direct) {
					set.add(f.getOWLTopDataProperty());
				}
			} else if (cex.isObjectPropertyExpression()) {
				r.getSuperObjectProperties((OWLObjectPropertyExpression) cex, direct).entities()
				 .filter(ex -> !ex.isAnonymous()).forEach(ex -> set.add(ex.asOWLObjectProperty()));
				if (!direct) {
					set.add(f.getOWLTopObjectProperty());
				}
			} else {
				throw new InternalReasonerException();
			}

			return set;
		}

		public Set<OWLProperty> getTops() {
			return new HashSet<>(Arrays.asList(
					f.getOWLTopObjectProperty(), f.getOWLTopDataProperty()));
		}

		public Set<OWLProperty> getBottoms() {
			return new HashSet<>(Arrays.asList(
					f.getOWLBottomObjectProperty(),
					f.getOWLBottomDataProperty()));
		}

		public Set<OWLProperty> getDisjoints(OWLObject disjointG) {
			final OWLPropertyExpression cex = asOWLPropertyExpression(disjointG);

			if (cex.isDataPropertyExpression()) {
				return r.getDisjointDataProperties((OWLDataProperty) cex).entities().collect(Collectors.toSet());
			} else if (cex.isObjectPropertyExpression()) {
				return r.getDisjointObjectProperties(((OWLObjectProperty) cex)).entities()
						.filter(ex -> !ex.isAnonymous()).map(AsOWLObjectProperty::asOWLObjectProperty).collect(
						Collectors.toSet());
			} else {
				throw new InternalReasonerException();
			}
		}

		@Override
		public boolean isEquiv(OWLObject equivG1, OWLObject equivG2) {
			final OWLPropertyExpression cex1 = asOWLPropertyExpression(equivG1);
			final OWLPropertyExpression cex2 = asOWLPropertyExpression(equivG2);

			if (cex1.isDataPropertyExpression()) {
				return cex2.isDataPropertyExpression()
						&& r.isEntailed(f.getOWLEquivalentDataPropertiesAxiom(
								(OWLDataPropertyExpression) cex1,
								(OWLDataPropertyExpression) cex2));
			} else {
				return cex2.isObjectPropertyExpression()
						&& r.isEntailed(f
								.getOWLEquivalentObjectPropertiesAxiom(
										(OWLObjectPropertyExpression) cex1,
										(OWLObjectPropertyExpression) cex2));
			}
		}

		@Override
		public boolean isSub(OWLObject subG1, OWLObject superG2, boolean direct) {
			return getSubs(superG2, direct).contains(subG1);
		}

		@Override
		public boolean isDisjointWith(OWLObject disjointG1, OWLObject disjointG2) {
			return getDisjoints(disjointG1).contains(disjointG2); // TODO
																	// reasoner
																	// directly
		}

		@Override
		public Set<OWLProperty> getComplements(OWLObject complementG) {
			throw new UnsupportedOperationException("NOT supported yet.");
		}

		@Override
		public boolean isComplementWith(OWLObject complementG1,
				OWLObject complementG2) {
			throw new UnsupportedOperationException("NOT supported yet.");
		}

	};

	public Hierarchy<OWLObject, OWLProperty> getPropertyHierarchy() {
		return propertyHierarchy;
	}

	public Set<OWLProperty> getFunctionalProperties() {
		final Set<OWLProperty> set = new HashSet<>();

		for (final OWLObjectProperty p : getObjectProperties()) {
			if (r.isEntailed(f.getOWLFunctionalObjectPropertyAxiom(p
					.asOWLObjectProperty()))) {
				set.add(p);
			}
		}

		for (final OWLDataProperty p : getDataProperties()) {
			if (r.isEntailed(f.getOWLFunctionalDataPropertyAxiom(p
					.asOWLDataProperty()))) {
				set.add(p);
			}
		}

		return set;
	}

	public Set<? extends OWLObject> getInverseFunctionalProperties() {
		final Set<OWLObjectProperty> set = new HashSet<>();

		for (final OWLObjectProperty p : getObjectProperties()) {
			if (r.isEntailed(f.getOWLInverseFunctionalObjectPropertyAxiom(p))) {
				set.add(p);
			}
		}

		return set;
	}

	public Set<? extends OWLObject> getIrreflexiveProperties() {
		final Set<OWLObjectProperty> set = new HashSet<>();

		for (final OWLObjectProperty p : getObjectProperties()) {
			if (r.isEntailed(f.getOWLIrreflexiveObjectPropertyAxiom(p))) {
				set.add(p);
			}
		}

		return set;
	}

	public Set<? extends OWLObject> getReflexiveProperties() {
		final Set<OWLObjectProperty> set = new HashSet<>();

		for (final OWLObjectProperty p : getObjectProperties()) {
			if (r.isEntailed(f.getOWLReflexiveObjectPropertyAxiom(p))) {
				set.add(p);
			}
		}

		return set;
	}

	public Set<? extends OWLObject> getSymmetricProperties() {
		final Set<OWLObjectProperty> set = new HashSet<>();

		for (final OWLObjectProperty p : getObjectProperties()) {
			if (r.isEntailed(f.getOWLSymmetricObjectPropertyAxiom(p))) {
				set.add(p);
			}
		}

		return set;
	}

	public Set<OWLObjectProperty> getAsymmetricProperties() {
		final Set<OWLObjectProperty> set = new HashSet<>();

		for (final OWLObjectProperty p : getObjectProperties()) {
			if (r.isEntailed(f.getOWLAsymmetricObjectPropertyAxiom(p))) {
				set.add(p);
			}
		}

		return set;
	}

	public Set<? extends OWLObject> getTransitiveProperties() {
		final Set<OWLObjectProperty> set = new HashSet<>();

		for (final OWLObjectProperty p : getObjectProperties()) {
			if (r.isEntailed(f.getOWLTransitiveObjectPropertyAxiom(p))) {
				set.add(p);
			}
		}

		return set;
	}

	public boolean isFunctionalProperty(OWLObject Term) {
		final OWLPropertyExpression p = asOWLPropertyExpression(Term);

		if (p instanceof OWLObjectProperty) {
			return r.isEntailed(f
					.getOWLFunctionalObjectPropertyAxiom(asOWLObjectProperty(Term)));
		} else if (p instanceof OWLDataProperty) {
			return r.isEntailed(f
					.getOWLFunctionalDataPropertyAxiom((OWLDataProperty) Term));
		} else {
			return false;
		}
	}

	public boolean isInverseFunctionalProperty(OWLObject Term) {
		return r.isEntailed(f
				.getOWLInverseFunctionalObjectPropertyAxiom(asOWLObjectProperty(Term)));
	}

	public boolean isIrreflexiveProperty(OWLObject Term) {
		return r.isEntailed(f
				.getOWLIrreflexiveObjectPropertyAxiom(asOWLObjectProperty(Term)));
	}

	public boolean isReflexiveProperty(OWLObject Term) {
		return r.isEntailed(f
				.getOWLReflexiveObjectPropertyAxiom(asOWLObjectProperty(Term)));
	}

	public boolean isSymmetricProperty(OWLObject Term) {
		return r.isEntailed(f
				.getOWLSymmetricObjectPropertyAxiom(asOWLObjectProperty(Term)));

	}

	public boolean isAsymmetricProperty(OWLObject Term) {
		return r.isEntailed(f
				.getOWLAsymmetricObjectPropertyAxiom(asOWLObjectProperty(Term)));
	}

	public boolean isTransitiveProperty(OWLObject Term) {
		return r.isEntailed(f
				.getOWLTransitiveObjectPropertyAxiom(asOWLObjectProperty(Term)));
	}

	public String getDatatypeOfLiteral(OWLObject literal) {
		if (literal instanceof OWLLiteral) {
			return ((OWLLiteral) literal).getDatatype().getIRI().toString();
		} else {
			throw new OWL2QueryException("Expected literal, but got " + literal);
		}
	}
}
