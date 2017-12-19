package cz.cvut.kbss.owl2query.model.pellet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.Ontology;
import org.semanticweb.kaon2.api.OntologyManager;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.reasoner.Reasoner;

import cz.cvut.kbss.owl2query.model.Hierarchy;
import cz.cvut.kbss.owl2query.model.OWL2Ontology;
import cz.cvut.kbss.owl2query.model.OWL2QueryException;
import cz.cvut.kbss.owl2query.model.OWL2QueryFactory;
import cz.cvut.kbss.owl2query.model.OWLObjectType;
import cz.cvut.kbss.owl2query.model.SizeEstimate;
import cz.cvut.kbss.owl2query.model.SizeEstimateImpl;

public class KaonOWL2Ontology implements OWL2Ontology<Object> {

	private SizeEstimate<Object> estimate;

	private OWL2QueryFactory<Object> factory = new KaonQueryFactory();

	final OntologyManager m;
	final Ontology o;
	final Reasoner r;

	public KaonOWL2Ontology(final OntologyManager m, final Ontology o,
			final Reasoner r) {
		this.m = m;
		this.o = o;
		this.r = r;

		estimate = new SizeEstimateImpl<Object>(this);
	}

	@Override
	public void ensureConsistency() {
		try {
			if (!r.isSatisfiable()) {
				throw new OWL2QueryException("Inconsistent ontology.");
			}
		} catch (KAON2Exception e) {
			throw new OWL2QueryException(e);
		} catch (InterruptedException e) {
			throw new OWL2QueryException(e);
		}
	}

	@Override
	public Set<? extends Object> getAsymmetricProperties() {
		throw new UnsupportedOperationException();
	}

	private final Hierarchy<Object, OWLClass> classHierarchy = new Hierarchy<Object, OWLClass>() {

		@Override
		public Set<OWLClass> getTops() {
			try {
				return r.getSubsumptionHierarchy().thingNode().getOWLClasses();
			} catch (KAON2Exception e) {
				throw new OWL2QueryException(e);
			} catch (InterruptedException e) {
				throw new OWL2QueryException(e);
			}
		}

		@Override
		public Set<OWLClass> getSupers(Object subG, boolean direct) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<OWLClass> getSubs(Object superG, boolean direct) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<OWLClass> getEquivs(Object equivG) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<OWLClass> getBottoms() {
			try {
				return r.getSubsumptionHierarchy().nothingNode()
						.getOWLClasses();
			} catch (KAON2Exception e) {
				throw new OWL2QueryException(e);
			} catch (InterruptedException e) {
				throw new OWL2QueryException(e);
			}
		}
	};

	@Override
	public Hierarchy<Object, ? extends Object> getClassHierarchy() {
		return classHierarchy;
	}

	@Override
	public Set<? extends Object> getClasses() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Object> getDataProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Object> getDifferents(Object i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Object> getDomains(Object pred) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWL2QueryFactory<Object> getFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Object> getFunctionalProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Object> getIndividuals() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends Object> getIndividualsWithProperty(Object pvP,
			Object pvIL) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Object> getInstances(Object ic, boolean direct) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Object> getInverseFunctionalProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Object> getInverses(Object ope) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Object> getIrreflexiveProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Object, Boolean> getKnownInstances(Object ic) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends Object> getKnownPropertyValues(Object pvP,
			Object pvI) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Object> getObjectProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Hierarchy<Object, ? extends Object> getPropertyHierarchy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends Object> getPropertyValues(Object pvP, Object pvI) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Object> getRanges(Object pred) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Object> getReflexiveProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Object> getSames(Object i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SizeEstimate<Object> getSizeEstimate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Object> getSymmetricProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Hierarchy<Object, ? extends Object> getToldClassHierarchy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Object> getTransitiveProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Object> getTypes(Object i, boolean direct) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean hasKnownPropertyValue(Object p, Object s, Object o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasPropertyValue(Object p, Object s, Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean is(Object o, OWLObjectType... types) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAsymmetricProperty(Object Term) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isClassAlwaysNonEmpty(Object ce) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isClassified() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isComplexClass(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDifferentFrom(Object i1, Object i2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFunctionalProperty(Object Term) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInverseFunctionalProperty(Object Term) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isIrreflexiveProperty(Object Term) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Boolean isKnownTypeOf(Object ce, Object i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRealized() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReflexiveProperty(Object Term) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSameAs(Object i1, Object i2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSatisfiable(Object ce) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSymmetricProperty(Object Term) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTransitiveProperty(Object Term) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTypeOf(Object ce, Object i, boolean direct) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<?> retrieveIndividualsWithProperty(Object Term) {
		// TODO Auto-generated method stub
		return null;
	}

	// @Override
	// public Set<? extends ATermAppl> getClasses() {
	// return kb.getClasses();
	// }
	//
	// @Override
	// public Set<ATermAppl> getObjectProperties() {
	// return kb.getObjectProperties();
	// }
	//
	// @Override
	// public Set<ATermAppl> getDataProperties() {
	// return kb.getDataProperties();
	// }
	//
	// @Override
	// public Set<ATermAppl> getDifferents(ATermAppl i) {
	// return kb.getDifferents(i);
	// }
	//
	// @Override
	// public Set<ATermAppl> getDomains(ATermAppl pred) {
	// return kb.getDomains(pred);
	// }
	//
	// @Override
	// public Set<ATermAppl> getIndividuals() {
	// return kb.getIndividuals();
	// }
	//
	// @Override
	// public Set<ATermAppl> getInstances(ATermAppl ic, boolean direct) {
	// return kb.getInstances(ic, direct);
	// }
	//
	// @Override
	// public Set<ATermAppl> getInverses(ATermAppl ope) {
	// return kb.getInverses(ope);
	// }
	//
	// @Override
	// public Set<ATermAppl> getRanges(ATermAppl pred) {
	// return kb.getRanges(pred);
	// }
	//
	// @Override
	// public Set<ATermAppl> getSames(ATermAppl i) {
	// return kb.getSames(i);
	// }
	//
	// @Override
	// public Set<ATermAppl> getTypes(ATermAppl i, boolean direct) {
	// final Set<ATermAppl> set = new HashSet<ATermAppl>();
	//
	// for (final Set<ATermAppl> s : kb.getTypes(i, direct)) {
	// set.addAll(s);
	// }
	//
	// return set;
	// }
	//
	// @Override
	// public boolean is(ATermAppl e, final OWLObjectType... tt) {
	// boolean result = false;
	//
	// for (final OWLObjectType t : tt) {
	// switch (t) {
	// case OWLLiteral:
	// result = ATermUtils.isLiteral(e);
	// break;
	// case OWLAnnotationProperty:
	// result = kb.isAnnotationProperty(e);
	// break;
	// case OWLDataProperty:
	// result = kb.isDatatypeProperty(e);
	// break;
	// case OWLObjectProperty:
	// result = kb.isObjectProperty(e);
	// break;
	// case OWLClass:
	// result = kb.isClass(e);
	// break;
	// case OWLNamedIndividual:
	// result = kb.isIndividual(e);
	// break;
	// default:
	// break;
	// }
	// if (result) {
	// break;
	// }
	// }
	//
	// return result;
	// }
	//
	// @Override
	// public boolean isDifferentFrom(ATermAppl i1, ATermAppl i2) {
	// return kb.isDifferentFrom(i1, i2);
	// }
	//
	// @Override
	// public boolean isSameAs(ATermAppl i1, ATermAppl i2) {
	// return kb.isSameAs(i1, i2);
	// }
	//
	// @Override
	// public boolean isTypeOf(ATermAppl ce, ATermAppl i, boolean direct) {
	// boolean result;
	//
	// if (PelletOptions.USE_CACHING) {
	// kb.isSatisfiable(ce);
	// kb.isSatisfiable(ATermUtils.makeNot(ce));
	// }
	//
	// if (direct) {
	// result = kb.getTypes(i, true).contains(ce);
	// } else {
	// result = kb.isType(i, ce);
	// }
	//
	// return result;
	// }
	//
	// static int cc = 0;
	//
	// @Override
	// public void ensureConsistency() {
	// try {
	// kb.ensureConsistency();
	// } catch (Exception e) {
	// throw new InternalReasonerException(e);
	// }
	// }
	//
	// // @Override
	// // public long getConsistencyCount() {
	// // return kb.getABox().consistencyCount;
	// // }
	//
	// @Override
	// public Collection<ATermAppl> getIndividualsWithProperty(ATermAppl pvP,
	// ATermAppl pvIL) {
	// return kb.getIndividualsWithProperty(pvP, pvIL);
	// }
	//
	// @Override
	// public Collection<ATermAppl> getPropertyValues(ATermAppl pvP, ATermAppl
	// pvI) {
	// return kb.getPropertyValues(pvP, pvI);
	// }
	//
	// // @Override
	// // public long getSatisfiabilityCount() {
	// // return kb.getABox().satisfiabilityCount;
	// // }
	//
	// @Override
	// public SizeEstimate<ATermAppl> getSizeEstimate() {
	// return estimate;
	// }
	//
	// @Override
	// public boolean hasPropertyValue(ATermAppl p, ATermAppl s, ATermAppl o) {
	// return kb.hasPropertyValue(s, p, o);
	// }
	//
	// @Override
	// public boolean isClassAlwaysNonEmpty(ATermAppl sc) {
	// if (PelletOptions.USE_CACHING) {
	// if (!kb.isSatisfiable(sc)) {
	// return false;
	// }
	//
	// kb.isSatisfiable(ATermUtils.makeNot(sc));
	// }
	//
	// List<Pair<ATermAppl, DependencySet>> UC = kb.getTBox().getUC();
	// ATermAppl newUC = ATermUtils.normalize(ATermUtils.makeNot(sc));
	//
	// UC.add(new Pair<ATermAppl, DependencySet>(newUC,
	// DependencySet.INDEPENDENT));
	//
	// ABox copy = kb.getABox().copy();
	// copy.setInitialized(false);
	// boolean classAlwaysNonEmpty = !copy.isConsistent();
	//
	// UC.remove(UC.size() - 1);
	//
	// return classAlwaysNonEmpty;
	// }
	//
	// @Override
	// public boolean isClassified() {
	// return kb.isClassified();
	// }
	//
	// @Override
	// public boolean isSatisfiable(ATermAppl arg) {
	// return kb.isSatisfiable(arg);
	// }
	//
	// @Override
	// public Collection<ATermAppl> retrieveIndividualsWithProperty(ATermAppl p)
	// {
	// return kb.retrieveIndividualsWithProperty(p);
	// }
	//
	// @Override
	// public Map<ATermAppl, Boolean> getKnownInstances(ATermAppl ce) {
	// final CandidateSet<ATermAppl> cs = kb.getABox().getObviousInstances(ce);
	// final Map<ATermAppl, Boolean> map = new HashMap<ATermAppl, Boolean>();
	//
	// for (final ATermAppl a : cs.getKnowns()) {
	// map.put(a, true);
	// }
	//
	// for (final ATermAppl a : cs.getUnknowns()) {
	// map.put(a, false);
	// }
	//
	// return map;
	// }
	//
	// @Override
	// public Boolean hasKnownPropertyValue(ATermAppl p, ATermAppl s, ATermAppl
	// o) {
	// return evaluate(kb.hasKnownPropertyValue(s, p, o));
	// }
	//
	// private Boolean evaluate(final Bool b) {
	// if (b.isKnown()) {
	// return b.isTrue();
	// } else {
	// return null;
	// }
	// }
	//
	// @Override
	// public Boolean isKnownTypeOf(ATermAppl ce, ATermAppl i) {
	// return evaluate(kb.isKnownType(i, ce));
	// }
	//
	// @Override
	// public OWL2QueryFactory<ATermAppl> getFactory() {
	// return factory;
	// }
	//
	// @Override
	// public boolean isRealized() {
	// return kb.isRealized();
	// }
	//
	// @Override
	// public boolean isComplexClass(ATermAppl o) {
	// return ATermUtils.isComplexClass(o);
	// }
	//
	// @Override
	// public Collection<? extends ATermAppl> getKnownPropertyValues(
	// ATermAppl pvP, ATermAppl pvI) {
	// if (kb.isObjectProperty(pvP)) {
	// final Set<ATermAppl> knowns = new HashSet<ATermAppl>();
	//
	// kb.getABox().getObjectPropertyValues(pvI, kb.getRole(pvP), knowns,
	// new HashSet<ATermAppl>(), true);
	//
	// return knowns;
	// } else if (kb.isDatatypeProperty(pvP)) {
	// return kb.getABox().getObviousDataPropertyValues(pvI,
	// kb.getRole(pvP), null);
	// } else {
	// throw new InternalReasonerException();
	// }
	// }
	//
	// private final Hierarchy<ATermAppl, ATermAppl> classHierarchy = new
	// Hierarchy<ATermAppl, ATermAppl>() {
	//
	// @Override
	// public Set<ATermAppl> getEquivs(ATermAppl equivG) {
	// return kb.getTaxonomy().getEquivalents(equivG);
	// }
	//
	// @Override
	// public Set<ATermAppl> getSubs(ATermAppl superG, boolean direct) {
	// return kb.getTaxonomy().getFlattenedSubs(superG, direct);
	// }
	//
	// @Override
	// public Set<ATermAppl> getSupers(ATermAppl superG, boolean direct) {
	// return kb.getTaxonomy().getFlattenedSupers(superG, direct);
	// }
	//
	// @Override
	// public Set<ATermAppl> getTops() {
	// return Collections.singleton(kb.getTaxonomy().getTop().getName());
	// }
	//
	// @Override
	// public Set<ATermAppl> getBottoms() {
	// return Collections
	// .singleton(kb.getTaxonomy().getBottom().getName());
	// }
	// };
	//
	// @Override
	// public Hierarchy<ATermAppl, ATermAppl> getClassHierarchy() {
	// return classHierarchy;
	// }
	//
	// private final Hierarchy<ATermAppl, ATermAppl> propertyHierarchy = new
	// Hierarchy<ATermAppl, ATermAppl>() {
	//
	// @Override
	// public Set<ATermAppl> getEquivs(ATermAppl equivG) {
	// return kb.getRoleTaxonomy(kb.isObjectProperty(equivG))
	// .getAllEquivalents(equivG);
	// }
	//
	// @Override
	// public Set<ATermAppl> getSubs(ATermAppl superG, boolean direct) {
	// return kb.getRoleTaxonomy(kb.isObjectProperty(superG))
	// .getFlattenedSubs(superG, direct);
	// }
	//
	// @Override
	// public Set<ATermAppl> getSupers(ATermAppl superG, boolean direct) {
	// return kb.getRoleTaxonomy(kb.isObjectProperty(superG))
	// .getFlattenedSupers(superG, direct);
	// }
	//
	// @Override
	// public Set<ATermAppl> getTops() {
	// return Collections.singleton(kb.getTaxonomy().getTop().getName());
	// }
	//
	// @Override
	// public Set<ATermAppl> getBottoms() {
	// return Collections
	// .singleton(kb.getTaxonomy().getBottom().getName());
	// }
	// };
	//
	// @Override
	// public Hierarchy<ATermAppl, ATermAppl> getPropertyHierarchy() {
	// return propertyHierarchy;
	// }
	//
	// private final Hierarchy<ATermAppl, ATermAppl> toldClassHierarchy = new
	// Hierarchy<ATermAppl, ATermAppl>() {
	//
	// @Override
	// public Set<ATermAppl> getEquivs(ATermAppl equivG) {
	// return kb.getToldTaxonomy().getEquivalents(equivG);
	// }
	//
	// @Override
	// public Set<ATermAppl> getSubs(ATermAppl superG, boolean direct) {
	// return kb.getToldTaxonomy().getFlattenedSubs(superG, direct);
	// }
	//
	// @Override
	// public Set<ATermAppl> getSupers(ATermAppl superG, boolean direct) {
	// return kb.getToldTaxonomy().getFlattenedSupers(superG, direct);
	// }
	//
	// @Override
	// public Set<ATermAppl> getTops() {
	// return Collections.singleton(kb.getToldTaxonomy().getTop()
	// .getName());
	// }
	//
	// @Override
	// public Set<ATermAppl> getBottoms() {
	// return Collections.singleton(kb.getToldTaxonomy().getBottom()
	// .getName());
	// }
	// };
	//
	// @Override
	// public Hierarchy<ATermAppl, ATermAppl> getToldClassHierarchy() {
	// return toldClassHierarchy;
	// }
	//
	// @Override
	// public Set<? extends ATermAppl> getAsymmetricProperties() {
	// return kb.getAsymmetricProperties();
	// }
	//
	// @Override
	// public Set<? extends ATermAppl> getFunctionalProperties() {
	// return kb.getFunctionalProperties();
	// }
	//
	// @Override
	// public Set<? extends ATermAppl> getInverseFunctionalProperties() {
	// return kb.getInverseFunctionalProperties();
	// }
	//
	// @Override
	// public Set<? extends ATermAppl> getIrreflexiveProperties() {
	// return kb.getIrreflexiveProperties();
	// }
	//
	// @Override
	// public Set<? extends ATermAppl> getReflexiveProperties() {
	// return kb.getReflexiveProperties();
	// }
	//
	// @Override
	// public Set<? extends ATermAppl> getSymmetricProperties() {
	// return kb.getSymmetricProperties();
	// }
	//
	// @Override
	// public Set<? extends ATermAppl> getTransitiveProperties() {
	// return kb.getTransitiveProperties();
	// }
	//
	// @Override
	// public boolean isAsymmetricProperty(ATermAppl Term) {
	// return isAsymmetricProperty(Term);
	// }
	//
	// @Override
	// public boolean isFunctionalProperty(ATermAppl Term) {
	// return isFunctionalProperty(Term);
	// }
	//
	// @Override
	// public boolean isInverseFunctionalProperty(ATermAppl Term) {
	// return isInverseFunctionalProperty(Term);
	// }
	//
	// @Override
	// public boolean isIrreflexiveProperty(ATermAppl Term) {
	// return isIrreflexiveProperty(Term);
	// }
	//
	// @Override
	// public boolean isReflexiveProperty(ATermAppl Term) {
	// return isReflexiveProperty(Term);
	// }
	//
	// @Override
	// public boolean isSymmetricProperty(ATermAppl Term) {
	// return isSymmetricProperty(Term);
	// }
	//
	// @Override
	// public boolean isTransitiveProperty(ATermAppl Term) {
	// return isTransitiveProperty(Term);
	// }
}
