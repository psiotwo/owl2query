package cz.cvut.kbss.owl2query.model.owlapi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.cvut.kbss.owl2query.model.Hierarchy;
import cz.cvut.kbss.owl2query.model.InternalReasonerException;
import cz.cvut.kbss.owl2query.model.OWL2Ontology;
import cz.cvut.kbss.owl2query.model.OWL2QueryFactory;
import cz.cvut.kbss.owl2query.model.OWLObjectType;
import cz.cvut.kbss.owl2query.model.SizeEstimate;
import cz.cvut.kbss.owl2query.model.SizeEstimateImpl;

public class JRacerOWL2Ontology {
//	implements OWL2Ontology<RacerSymbol> {
//}
//
//	private static final Logger LOG = Logger
//			.getLogger(OWLAPIv3OWL2Ontology.class.getName());
//
//	private OWL2QueryFactory<RacerSymbol> factory;
//	private SizeEstimate<RacerSymbol> sizeEstimate;
//
//	private RacerClient c;
//
//	public JRacerOWL2Ontology(RacerClient c) {
//		this.c = c;
//		this.sizeEstimate = new SizeEstimateImpl<RacerSymbol>(this);
//	}
//
//	private <X extends RacerResult> Set<X> asSet(final RacerList<X> list) {
//		final Set<X> set = new HashSet<X>();
//
//		for (Iterator<X> i = list.iterator(); i.hasNext(); set.add(i.next()))
//			;
//
//		return set;
//	}
//
//	@Override
//	public Set<RacerSymbol> getClasses() {
//		return asSet((RacerList<RacerSymbol>) c.allAtomicConcepts$());
//	}
//
//	@Override
//	public Set<RacerSymbol> getObjectProperties() {
//		// TODO
//		return asSet((RacerList<RacerSymbol>) c.allRoles$());
//	}
//
//	@Override
//	public Set<RacerSymbol> getDataProperties() {
//		// TODO
//		return asSet((RacerList<RacerSymbol>) c.allRoles$());
//	}
//
//	@Override
//	public Set<RacerSymbol> getIndividuals() {
//		return asSet((RacerList<RacerSymbol>) c.allIndividuals$());
//	}
//
//	@Override
//	public Set<RacerSymbol> getDifferents(RacerSymbol i) {
//		return asSet((RacerList<RacerSymbol>) c
//				.owlapiGetDifferentIndividuals$(i));
//	}
//
//	@Override
//	public Set<RacerSymbol> getDomains(RacerSymbol pred) {
//		return asSet((RacerList<RacerSymbol>) c.owlapiGetDomains$(pred));
//	}
//
//	public Set<RacerSymbol> getEquivalentClasses(RacerSymbol ce) {
//		return asSet((RacerList<RacerSymbol>) c.owlapiGetEquivalentClasses$(ce));
//	}
//
//	@Override
//	public Set<RacerSymbol> getInverses(RacerSymbol ope) {
//		return asSet((RacerList<RacerSymbol>) c
//				.owlapiGetInverseProperties$(ope));
//	}
//
//	@Override
//	public Set<RacerSymbol> getRanges(RacerSymbol pred) {
//		return asSet((RacerList<RacerSymbol>) c.owlapiGetRanges$(pred));
//	}
//
//	@Override
//	public Set<RacerSymbol> getSames(RacerSymbol i) {
//		return asSet((RacerList<RacerSymbol>) c.owlapiGetSameIndividuals$(i));
//	}
//
//	@Override
//	public Set<RacerSymbol> getTypes(RacerSymbol i, boolean direct) {
//		return asSet((RacerList<RacerSymbol>) c.owlapiGetTypes$(i, direct));
//	}
//
//	@Override
//	public boolean is(RacerSymbol e, final OWLObjectType... tt) {
//		boolean result = false;
//
//		for (final OWLObjectType t : tt) {
//			switch (t) {
//			case OWLLiteral:
//				result = false;// TODO
//				break;
//			case OWLAnnotationProperty:
//				result = false;// TODO
//				break;
//			case OWLDataProperty:
//				result = c.owlapiIsDefinedDataProperty(e) != null;// TODO
//				break;
//			case OWLObjectProperty:
//				result = c.owlapiIsDefinedObjectProperty(e) != null;// TODO
//			case OWLClass:
//				result = c.owlapiIsDefinedClass(e) != null;// TODO
//			case OWLNamedIndividual:
//				result = c.owlapiIsDefinedIndividual(e) != null;// TODO
//				break;
//			default:
//				break;
//			}
//			if (result) {
//				break;
//			}
//		}
//
//		return result;
//	}
//
//	@Override
//	public boolean isSameAs(RacerSymbol i1, RacerSymbol i2) {
//		return c.owlapiIsSameIndividual(i1, i2) != null; // TODO
//	}
//
//	@Override
//	public boolean isDifferentFrom(RacerSymbol i1, RacerSymbol i2) {
//		return c.owlapiIsDifferentIndividual(i1, i2) != null; // TODO
//	}
//
//	@Override
//	public boolean isTypeOf(RacerSymbol ce, RacerSymbol i, boolean direct) {
//		return c.owlapiHasType(i, ce, direct) != null; // TODO
//	}
//
//	@Override
//	public void ensureConsistency() {
//		if (LOG.isLoggable(Level.CONFIG)) {
//			LOG.config("Ensure consistency");
//		}
//
//		if (LOG.isLoggable(Level.CONFIG)) {
//			LOG.config("	* isConsistent ?");
//		}
//		if (!c.aboxConsistentP()) {
//			throw new InternalReasonerException();
//		}
//		if (LOG.isLoggable(Level.CONFIG)) {
//			LOG.config("	* true");
//		}
//	}
//
//	@Override
//	public Set<RacerSymbol> getIndividualsWithProperty(RacerSymbol pvP,
//			RacerSymbol pvIL) {
//		final OWLPropertyExpression<?, ?> pex = asOWLPropertyExpression(pvP);
//
//		final Set<RacerSymbol> set = new HashSet<RacerSymbol>();
//
//		if (pex.isObjectPropertyExpression()) {
//			final OWLNamedIndividual object = asOWLNamedIndividual(pvIL);
//
//			for (final OWLNamedIndividual i : getIndividuals()) {
//				if (c.isEntailed(f.getRacerSymbolPropertyAssertionAxiom(
//						(RacerSymbolPropertyExpression) pex, i, object))) {
//					set.add(i);
//				}
//			}
//		} else if (pex.isDataPropertyExpression()) {
//			final OWLLiteral object = asOWLLiteral(pvIL);
//
//			for (final OWLNamedIndividual i : getIndividuals()) {
//				if (c.isEntailed(f.getOWLDataPropertyAssertionAxiom(
//						(OWLDataPropertyExpression) pex, i, object))) {
//					set.add(i);
//				}
//			}
//		}
//
//		return set;
//	}
//
//	public Set<RacerSymbol> getPropertyValues(RacerSymbol pvP, RacerSymbol pvI) {
//		final OWLPropertyExpression<?, ?> pex = asOWLPropertyExpression(pvP);
//		final OWLNamedIndividual ni = asOWLNamedIndividual(pvI);
//
//		if (pex != null) {
//			if (pex.isObjectPropertyExpression()) {
//				return c.getObjectPropertyValues(ni,
//						(RacerSymbolPropertyExpression) pex).getFlattened();
//			} else if (pex.isDataPropertyExpression()) {
//				return c.getDataPropertyValues(ni, (OWLDataProperty) pex); // TODO
//			}
//		}
//
//		throw new InternalReasonerException();
//	}
//
//	public SizeEstimate<RacerSymbol> getSizeEstimate() {
//		return sizeEstimate;
//	}
//
//	public boolean hasPropertyValue(RacerSymbol p, RacerSymbol s, RacerSymbol o) {
//		final OWLPropertyExpression<?, ?> pex = asOWLPropertyExpression(p);
//
//		if (pex.isObjectPropertyExpression()) {
//			return c.isEntailed(f.getRacerSymbolPropertyAssertionAxiom(
//					(RacerSymbolPropertyExpression) pex,
//					asOWLNamedIndividual(s), asOWLNamedIndividual(o)));
//		} else if (pex.isDataPropertyExpression()) {
//			return c.isEntailed(f.getOWLDataPropertyAssertionAxiom(
//					(OWLDataPropertyExpression) pex, asOWLNamedIndividual(s),
//					asOWLLiteral(o)));
//
//		}
//		return false;
//	}
//
//	public boolean isClassAlwaysNonEmpty(RacerSymbol sc) {
//		final OWLAxiom axiom = f.getOWLSubClassOfAxiom(
//				asRacerSymbolExpression(sc), f.getOWLNothing());
//
//		try {
//			m.applyChange(new AddAxiom(o, axiom));
//
//			boolean classAlwaysNonEmpty = !c.isConsistent();
//
//			m.applyChange(new RemoveAxiom(o, axiom));
//
//			return classAlwaysNonEmpty;
//		} catch (OWLOntologyChangeException e) {
//			throw new InternalReasonerException();
//		}
//	}
//
//	public boolean isClassified() {
//		// TODO
//		// return c.isClassified();
//		return false;
//	}
//
//	public boolean isSatisfiable(RacerSymbol arg) {
//		return c.isSatisfiable(asRacerSymbolExpression(arg));
//	}
//
//	public Set<RacerSymbol> retrieveIndividualsWithProperty(RacerSymbol odpe) {
//		final OWLPropertyExpression<?, ?> ope = asOWLPropertyExpression(odpe);
//
//		final Set<RacerSymbol> set = new HashSet<RacerSymbol>();
//		try {
//			if (ope.isObjectPropertyExpression()) {
//				for (final OWLNamedIndividual i : getIndividuals()) {
//					if (!c.getObjectPropertyValues(i,
//							(RacerSymbolPropertyExpression) ope).isEmpty()) {
//						set.add(i);
//					}
//				}
//			} else if (ope.isObjectPropertyExpression()) {
//				for (final OWLNamedIndividual i : getIndividuals()) {
//					if (!c.getObjectPropertyValues(i,
//							(RacerSymbolPropertyExpression) ope).isEmpty()) {
//						set.add(i);
//					}
//				}
//			}
//		} catch (Exception e) {
//			throw new InternalReasonerException(e);
//		}
//		return set;
//	}
//
//	public Map<RacerSymbol, Boolean> getKnownInstances(final RacerSymbol ce) {
//		final Map<RacerSymbol, Boolean> m = new HashMap<RacerSymbol, Boolean>();
//		final RacerSymbolExpression cex = asRacerSymbolExpression(ce);
//
//		for (final RacerSymbol x : getIndividuals()) {
//			m.put(x, false);
//		}
//
//		if (!cex.isAnonymous()) {
//			for (final RacerSymbol x : cex.asRacerSymbol().getIndividuals(o)) {
//				m.put(x, true);
//			}
//		}
//
//		return m;
//	}
//
//	public Boolean isKnownTypeOf(RacerSymbol ce, RacerSymbol i) {
//		final OWLIndividual ii = asOWLNamedIndividual(i);
//
//		if (ii.getTypes(o).contains(ce)) {
//			return true;
//		}
//
//		return null;
//	}
//
//	public Boolean hasKnownPropertyValue(RacerSymbol p, RacerSymbol s,
//			RacerSymbol ob) {
//		final OWLIndividual is = asOWLNamedIndividual(s);
//		final OWLPropertyExpression<?, ?> pex = asOWLPropertyExpression(p);
//
//		if (pex != null) {
//			if (pex.isObjectPropertyExpression()) {
//				final RacerSymbolPropertyExpression ope = ((RacerSymbolPropertyExpression) p)
//						.getSimplified();
//
//				if (ope instanceof RacerSymbolInverseOf) {
//					final RacerSymbolPropertyExpression opeInv = ope
//							.getInverseProperty().getSimplified();
//
//					for (final RacerSymbolPropertyAssertionAxiom ax : o
//							.getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION)) {
//						if (ax.getObject().equals(s)
//								&& ax.getProperty().equals(opeInv)
//								&& ax.getSubject().equals(ob)) {
//							return true;
//						}
//					}
//					return false;
//				} else {
//					return is.getObjectPropertyValues(
//							(RacerSymbolPropertyExpression) pex, o)
//							.contains(ob);
//				}
//			} else if (pex.isDataPropertyExpression()) {
//				return is.getDataPropertyValues(o).get(pex).contains(ob);
//			}
//		}
//
//		return false;
//	}
//
//	public Set<RacerSymbol> getInstances(RacerSymbol ic, boolean direct) {
//		final RacerSymbolExpression c = asRacerSymbolExpression(ic);
//
//		return c.getInstances(c, direct).getFlattened();
//	}
//
//	@Override
//	public OWL2QueryFactory<RacerSymbol> getFactory() {
//		return factory;
//	}
//
//	@Override
//	public boolean isRealized() {
//		// try {
//		// return c.isRealised();
//		// } catch (OWLReasonerException e) {
//		// throw new InternalReasonerException();
//		// }
//		// TODO
//		return false;
//	}
//
//	@Override
//	public boolean isComplexClass(RacerSymbol c) {
//		return (c instanceof RacerSymbolExpression);
//	}
//
//	@Override
//	public Collection<RacerSymbol> getKnownPropertyValues(RacerSymbol pvP,
//			RacerSymbol pvI) {
//
//		final OWLPropertyExpression p = asOWLPropertyExpression(pvP);
//		final OWLNamedIndividual ni = asOWLNamedIndividual(pvI);
//
//		Collection result;
//
//		if (p.isObjectPropertyExpression()) {
//			result = structuralReasonec.getObjectPropertyValues(ni,
//					(RacerSymbolPropertyExpression) p).getFlattened();
//		} else if (p.isDataPropertyExpression()) {
//			result = structuralReasonec.getDataPropertyValues(ni,
//					(OWLDataProperty) p);
//		} else {
//			throw new InternalReasonerException();
//		}
//
//		if (result == null) {
//			result = Collections.emptySet();
//		}
//
//		return result;
//	}
//
//	private final Hierarchy<RacerSymbol, RacerSymbol> classHierarchy = new Hierarchy<RacerSymbol, RacerSymbol>() {
//
//		@Override
//		public Set<RacerSymbol> getEquivs(RacerSymbol ce) {
//			final RacerSymbolExpression cex = asRacerSymbolExpression(ce);
//
//			return c.getEquivalentClasses(cex).getEntities();
//		}
//
//		@Override
//		public Set<RacerSymbol> getSubs(RacerSymbol superCE, boolean direct) {
//			final RacerSymbolExpression cex = asRacerSymbolExpression(superCE);
//
//			return c.getSubClasses(cex, direct).getFlattened();
//		}
//
//		@Override
//		public Set<RacerSymbol> getSupers(RacerSymbol superCE, boolean direct) {
//			final RacerSymbolExpression cex = asRacerSymbolExpression(superCE);
//
//			return c.getSuperClasses(cex, direct).getFlattened();
//		}
//
//		@Override
//		public Set<RacerSymbol> getTops() {
//			return Collections.singleton(f.getOWLThing());
//		}
//
//		@Override
//		public Set<RacerSymbol> getBottoms() {
//			return Collections.singleton(f.getOWLNothing());
//		}
//	};
//
//	@Override
//	public Hierarchy<RacerSymbol, RacerSymbol> getClassHierarchy() {
//		return classHierarchy;
//	}
//
//	private final Hierarchy<RacerSymbol, RacerSymbol> toldClassHierarchy = new Hierarchy<RacerSymbol, RacerSymbol>() {
//
//		@Override
//		public Set<RacerSymbol> getEquivs(RacerSymbol ce) {
//			final RacerSymbolExpression cex = asRacerSymbolExpression(ce);
//			if (cex.isAnonymous()) {
//				return Collections.emptySet();
//			} else {
//				return structuralReasonec.getEquivalentClasses(cex)
//						.getEntities();
//				// final Set<RacerSymbol> set = new HashSet<RacerSymbol>();
//				// for (final RacerSymbolExpression oce : cex.asRacerSymbol()
//				// .getEquivalentClasses(o)) {
//				// if (!oce.isAnonymous()) {
//				// set.add(oce.asRacerSymbol());
//				// }
//				// }
//				// return set;
//			}
//		}
//
//		@Override
//		public Set<RacerSymbol> getSubs(RacerSymbol superCE, boolean direct) {
//			final RacerSymbolExpression cex = asRacerSymbolExpression(superCE);
//			if (cex.isAnonymous()) {
//				return Collections.emptySet();
//			} else {
//				// final Set<RacerSymbol> set = new HashSet<RacerSymbol>();
//				return structuralReasonec.getSubClasses(cex, direct)
//						.getFlattened();
//				//
//				// for (final RacerSymbolExpression oce : cex.asRacerSymbol()
//				// .getSubClasses(o)) {
//				// if (!oce.isAnonymous() && !set.contains(oce.asRacerSymbol()))
//				// {
//				// set.add(oce.asRacerSymbol());
//				// if (!direct) {
//				// set.addAll(getSubs(oce, direct));
//				// }
//				// }
//				// }
//				//
//				// return set;
//			}
//		}
//
//		@Override
//		public Set<RacerSymbol> getSupers(RacerSymbol superCE, boolean direct) {
//			final RacerSymbolExpression cex = asRacerSymbolExpression(superCE);
//			if (cex.isAnonymous()) {
//				return Collections.emptySet();
//			} else {
//				return structuralReasonec.getSuperClasses(cex, direct)
//						.getFlattened();
//				//
//				// final Set<RacerSymbol> set = new HashSet<RacerSymbol>();
//				// for (final RacerSymbolExpression oce : cex.asRacerSymbol()
//				// .getSuperClasses(o)) {
//				// if (!oce.isAnonymous() && !set.contains(oce.asRacerSymbol()))
//				// {
//				// set.add(oce.asRacerSymbol());
//				// if (!direct) {
//				// set.addAll(getSupers(oce, direct));
//				// }
//				// }
//				// }
//				//
//				// return set;
//			}
//		}
//
//		@Override
//		public Set<RacerSymbol> getTops() {
//			return Collections.singleton(f.getOWLThing());
//		}
//
//		@Override
//		public Set<RacerSymbol> getBottoms() {
//			return Collections.singleton(f.getOWLNothing());
//		}
//	};
//
//	@Override
//	public Hierarchy<RacerSymbol, RacerSymbol> getToldClassHierarchy() {
//		return toldClassHierarchy;
//	}
//
//	private final Hierarchy<RacerSymbol, RacerSymbol> propertyHierarchy = new Hierarchy<RacerSymbol, RacerSymbol>() {
//
//		@Override
//		public Set<RacerSymbol> getEquivs(RacerSymbol ce) {
//			final RacerSymbolExpression cex = asRacerSymbolExpression(ce);
//
//			if (cex.isDataPropertyExpression()) {
//				return new HashSet<RacerSymbol>(c.getEquivalentDataProperties(
//						(OWLDataProperty) cex).getEntities());
//			} else if (cex.isObjectPropertyExpression()) {
//				return new HashSet<RacerSymbol>(r
//						.getEquivalentObjectProperties(
//								(RacerSymbolProperty) cex).getEntities());
//			} else {
//				throw new InternalReasonerException();
//			}
//		}
//
//		@Override
//		public Set<RacerSymbol> getSubs(RacerSymbol superCE, boolean direct) {
//			final RacerSymbolExpression cex = asRacerSymbolExpression(superCE);
//			if (cex.equals(f.getOWLBottomObjectProperty())) {
//				return Collections.emptySet();
//			} else if (cex.equals(f.getOWLTopObjectProperty())) {
//				final Set<RacerSymbol> set = new HashSet<RacerSymbol>();
//				set.addAll(getObjectProperties());
//
//				if (direct) {
//					for (RacerSymbol op : new HashSet<RacerSymbol>(set)) {
//						if (!getSupers(op, true).contains(
//								f.getOWLTopObjectProperty())) {
//							set.remove(op);
//						}
//					}
//				}
//
//				if (set.isEmpty()) {
//					set.add(f.getOWLBottomObjectProperty());
//				}
//
//				return set;
//			} else if (cex.equals(f.getOWLTopDataProperty())) {
//				final Set<RacerSymbol> set = new HashSet<RacerSymbol>();
//				set.addAll(getDataProperties());
//
//				if (direct) {
//					for (RacerSymbol op : new HashSet<RacerSymbol>(set)) {
//						if (!getSupers(op, true).contains(
//								f.getOWLTopDataProperty())) {
//							set.remove(op);
//						}
//					}
//				}
//
//				if (set.isEmpty()) {
//					set.add(f.getOWLBottomObjectProperty());
//				}
//
//				return set;
//			}
//
//			final Set<RacerSymbol> set = new HashSet<RacerSymbol>();
//
//			if (cex.isDataPropertyExpression()) {
//				set.addAll(r
//						.getSubDataProperties((OWLDataProperty) cex, direct)
//						.getFlattened());
//				if (!direct || set.isEmpty()) {
//					set.add(f.getOWLBottomDataProperty());
//				}
//
//			} else if (cex.isObjectPropertyExpression()) {
//				set.addAll(c.getSubObjectProperties((RacerSymbolProperty) cex,
//						direct).getFlattened());
//				if (!direct || set.isEmpty()) {
//					set.add(f.getOWLBottomObjectProperty());
//				}
//			} else {
//				throw new InternalReasonerException();
//			}
//
//			return set;
//		}
//
//		@Override
//		public Set<RacerSymbol> getSupers(RacerSymbol superCE, boolean direct) {
//			final RacerSymbolExpression cex = asRacerSymbolExpression(superCE);
//
//			if (cex.equals(f.getOWLTopObjectProperty())) {
//				return Collections.emptySet();
//			} else if (cex.equals(f.getOWLBottomObjectProperty())) {
//				final Set<RacerSymbol> set = new HashSet<RacerSymbol>();
//				set.addAll(getObjectProperties());
//
//				if (direct) {
//					for (RacerSymbol op : new HashSet<RacerSymbol>(set)) {
//						if (!getSubs(op, true).contains(
//								f.getOWLBottomObjectProperty())) {
//							set.remove(op);
//						}
//					}
//				}
//
//				if (set.isEmpty()) {
//					set.add(f.getOWLTopObjectProperty());
//				}
//
//				return set;
//			} else if (cex.equals(f.getOWLBottomDataProperty())) {
//				final Set<RacerSymbol> set = new HashSet<RacerSymbol>();
//				set.addAll(getDataProperties());
//
//				if (direct) {
//					for (RacerSymbol op : new HashSet<RacerSymbol>(set)) {
//						if (!getSubs(op, true).contains(
//								f.getOWLBottomDataProperty())) {
//							set.remove(op);
//						}
//					}
//				}
//
//				if (set.isEmpty()) {
//					set.add(f.getOWLTopDataProperty());
//				}
//
//				return set;
//			}
//			final Set<RacerSymbol> set = new HashSet<RacerSymbol>();
//
//			if (cex.isDataPropertyExpression()) {
//				set.addAll(c.getSuperDataProperties((OWLDataProperty) cex,
//						direct).getFlattened());
//
//				if (!direct || set.isEmpty()) {
//					set.add(f.getOWLTopDataProperty());
//				}
//			} else if (cex.isObjectPropertyExpression()) {
//				set.addAll(c.getSuperObjectProperties(
//						(RacerSymbolPropertyExpression) cex, direct)
//						.getFlattened());
//				if (!direct || set.isEmpty()) {
//					set.add(f.getOWLTopObjectProperty());
//				}
//			} else {
//				throw new InternalReasonerException();
//			}
//
//			return set;
//		}
//
//		@Override
//		public Set<RacerSymbol> getTops() {
//			return new HashSet<RacerSymbol>(Arrays.asList(f
//					.getOWLTopObjectProperty(), f.getOWLTopDataProperty()));
//		}
//
//		@Override
//		public Set<RacerSymbol> getBottoms() {
//			return new HashSet<RacerSymbol>(Arrays
//					.asList(f.getOWLBottomObjectProperty(), f
//							.getOWLBottomDataProperty()));
//		}
//	};
//
//	@Override
//	public Hierarchy<RacerSymbol, RacerSymbol> getPropertyHierarchy() {
//		return propertyHierarchy;
//	}
//
//	@Override
//	public Set<RacerSymbol> getFunctionalProperties() {
//		final Set<RacerSymbol> set = new HashSet<RacerSymbol>();
//
//		for (final RacerSymbolProperty p : getObjectProperties()) {
//			if (c.isEntailed(f.getOWLFunctionalObjectPropertyAxiom(p
//					.asRacerSymbolProperty()))) {
//				set.add(p);
//			}
//		}
//
//		for (final OWLDataProperty p : getDataProperties()) {
//			if (c.isEntailed(f.getOWLFunctionalDataPropertyAxiom(p
//					.asOWLDataProperty()))) {
//				set.add(p);
//			}
//		}
//
//		return set;
//	}
//
//	@Override
//	public Set<RacerSymbolProperty> getAsymmetricProperties() {
//		final Set<RacerSymbolProperty> set = new HashSet<RacerSymbolProperty>();
//
//		for (final RacerSymbolProperty p : getObjectProperties()) {
//			if (c.isEntailed(f.getOWLAsymmetricObjectPropertyAxiom(p))) {
//				set.add(p);
//			}
//		}
//
//		return set;
//	}
//
//	@Override
//	public Set<RacerSymbol> getInverseFunctionalProperties() {
//		final Set<RacerSymbolProperty> set = new HashSet<RacerSymbolProperty>();
//
//		for (final RacerSymbolProperty p : getObjectProperties()) {
//			if (c.isEntailed(f.getOWLInverseFunctionalObjectPropertyAxiom(p))) {
//				set.add(p);
//			}
//		}
//
//		return set;
//	}
//
//	@Override
//	public Set<RacerSymbol> getIrreflexiveProperties() {
//		final Set<RacerSymbolProperty> set = new HashSet<RacerSymbolProperty>();
//
//		for (final RacerSymbolProperty p : getObjectProperties()) {
//			if (c.isEntailed(f.getOWLIrreflexiveObjectPropertyAxiom(p))) {
//				set.add(p);
//			}
//		}
//
//		return set;
//	}
//
//	@Override
//	public Set<RacerSymbol> getReflexiveProperties() {
//		final Set<RacerSymbolProperty> set = new HashSet<RacerSymbolProperty>();
//
//		for (final RacerSymbolProperty p : getObjectProperties()) {
//			if (c.isEntailed(f.getOWLReflexiveObjectPropertyAxiom(p))) {
//				set.add(p);
//			}
//		}
//
//		return set;
//	}
//
//	@Override
//	public Set<RacerSymbol> getSymmetricProperties() {
//		final Set<RacerSymbolProperty> set = new HashSet<RacerSymbolProperty>();
//
//		for (final RacerSymbolProperty p : getObjectProperties()) {
//			if (c.isEntailed(f.getOWLSymmetricObjectPropertyAxiom(p))) {
//				set.add(p);
//			}
//		}
//
//		return set;
//	}
//
//	@Override
//	public Set<RacerSymbol> getTransitiveProperties() {
//		final Set<RacerSymbolProperty> set = new HashSet<RacerSymbolProperty>();
//
//		for (final RacerSymbolProperty p : getObjectProperties()) {
//			if (c.isEntailed(f.getOWLTransitiveObjectPropertyAxiom(p))) {
//				set.add(p);
//			}
//		}
//
//		return set;
//	}
//
//	@Override
//	public boolean isAsymmetricProperty(RacerSymbol Term) {
//		return r
//				.isEntailed(f
//						.getOWLAsymmetricObjectPropertyAxiom(asRacerSymbolProperty(Term)));
//	}
//
//	@Override
//	public boolean isFunctionalProperty(RacerSymbol Term) {
//		final RacerSymbolExpression p = asRacerSymbolExpression(Term);
//
//		if (p instanceof RacerSymbolProperty) {
//			return r
//					.isEntailed(f
//							.getOWLFunctionalObjectPropertyAxiom(asRacerSymbolProperty(Term)));
//		} else if (p instanceof OWLDataProperty) {
//			return c.isEntailed(f
//					.getOWLFunctionalDataPropertyAxiom((OWLDataProperty) Term));
//		} else {
//			return false;
//		}
//	}
//
//	@Override
//	public boolean isInverseFunctionalProperty(RacerSymbol Term) {
//		return r
//				.isEntailed(f
//						.getOWLInverseFunctionalObjectPropertyAxiom(asRacerSymbolProperty(Term)));
//	}
//
//	@Override
//	public boolean isIrreflexiveProperty(RacerSymbol Term) {
//		return r
//				.isEntailed(f
//						.getOWLIrreflexiveObjectPropertyAxiom(asRacerSymbolProperty(Term)));
//	}
//
//	@Override
//	public boolean isReflexiveProperty(RacerSymbol Term) {
//		return r
//				.isEntailed(f
//						.getOWLReflexiveObjectPropertyAxiom(asRacerSymbolProperty(Term)));
//	}
//
//	@Override
//	public boolean isSymmetricProperty(RacerSymbol Term) {
//		return r
//				.isEntailed(f
//						.getOWLSymmetricObjectPropertyAxiom(asRacerSymbolProperty(Term)));
//
//	}
//
//	@Override
//	public boolean isTransitiveProperty(RacerSymbol Term) {
//		return r
//				.isEntailed(f
//						.getOWLTransitiveObjectPropertyAxiom(asRacerSymbolProperty(Term)));
//	}
}
