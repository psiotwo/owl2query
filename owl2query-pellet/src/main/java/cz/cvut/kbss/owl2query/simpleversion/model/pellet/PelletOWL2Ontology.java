package cz.cvut.kbss.owl2query.model.pellet;

import static com.clarkparsia.pellet.utils.TermFactory.TOP_OBJECT_PROPERTY;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mindswap.pellet.ABox;
import org.mindswap.pellet.DependencySet;
import org.mindswap.pellet.PelletOptions;
import org.mindswap.pellet.exceptions.InternalReasonerException;
import org.mindswap.pellet.utils.ATermUtils;
import org.mindswap.pellet.utils.Bool;
import org.mindswap.pellet.utils.CandidateSet;

import aterm.ATermAppl;
import cz.cvut.kbss.owl2query.model.Hierarchy;
import cz.cvut.kbss.owl2query.model.OWL2Ontology;
import cz.cvut.kbss.owl2query.model.OWL2QueryFactory;
import cz.cvut.kbss.owl2query.model.OWLObjectType;
import cz.cvut.kbss.owl2query.model.SizeEstimate;
import cz.cvut.kbss.owl2query.model.SizeEstimateImpl;

public class PelletOWL2Ontology implements OWL2Ontology<ATermAppl> {

	private org.mindswap.pellet.KnowledgeBase kb;

	private SizeEstimate<ATermAppl> estimate;

	private OWL2QueryFactory<ATermAppl> factory = new PelletQueryFactory();

	public PelletOWL2Ontology(final org.mindswap.pellet.KnowledgeBase kb) {
		this.kb = kb;
		estimate = new SizeEstimateImpl<ATermAppl>(this);
	}

	org.mindswap.pellet.KnowledgeBase getKnowledgeBase() {
		return kb;
	}

	@Override
	public Set<? extends ATermAppl> getClasses() {
		return kb.getClasses();
	}

	@Override
	public Set<ATermAppl> getObjectProperties() {
		return kb.getObjectProperties();
	}

	@Override
	public Set<ATermAppl> getDataProperties() {
		return kb.getDataProperties();
	}

	@Override
	public Set<ATermAppl> getDifferents(ATermAppl i) {
		return kb.getDifferents(i);
	}

	@Override
	public Set<ATermAppl> getDomains(ATermAppl pred) {
		return kb.getDomains(pred);
	}

	@Override
	public Set<ATermAppl> getIndividuals() {
		return kb.getIndividuals();
	}

	@Override
	public Set<ATermAppl> getInstances(ATermAppl ic, boolean direct) {
		return kb.getInstances(ic, direct);
	}

	@Override
	public Set<ATermAppl> getInverses(ATermAppl ope) {
		return kb.getInverses(ope);
	}

	@Override
	public Set<ATermAppl> getRanges(ATermAppl pred) {
		return kb.getRanges(pred);
	}

	@Override
	public Set<ATermAppl> getSames(ATermAppl i) {
		return kb.getSames(i);
	}

	@Override
	public Set<ATermAppl> getTypes(ATermAppl i, boolean direct) {
		final Set<ATermAppl> set = new HashSet<ATermAppl>();

		for (final Set<ATermAppl> s : kb.getTypes(i, direct)) {
			set.addAll(s);
		}

		return set;
	}

	@Override
	public boolean is(ATermAppl e, final OWLObjectType... tt) {
		boolean result = false;

		for (final OWLObjectType t : tt) {
			switch (t) {
			case OWLLiteral:
				result = ATermUtils.isLiteral(e);
				break;
			case OWLAnnotationProperty:
				result = kb.isAnnotationProperty(e);
				break;
			case OWLDataProperty:
				result = kb.isDatatypeProperty(e);
				break;
			case OWLObjectProperty:
				result = kb.isObjectProperty(e);
				break;
			case OWLClass:
				result = kb.isClass(e);
				break;
			case OWLNamedIndividual:
				result = kb.isIndividual(e);
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

	@Override
	public boolean isDifferentFrom(ATermAppl i1, ATermAppl i2) {
		return kb.isDifferentFrom(i1, i2);
	}

	@Override
	public boolean isSameAs(ATermAppl i1, ATermAppl i2) {
		return kb.isSameAs(i1, i2);
	}

	@Override
	public boolean isTypeOf(ATermAppl ce, ATermAppl i, boolean direct) {
		boolean result;

		if (PelletOptions.USE_CACHING) {
			kb.isSatisfiable(ce);
			kb.isSatisfiable(ATermUtils.makeNot(ce));
		}

		if (direct) {
			result = kb.getTypes(i, true).contains(ce);
		} else {
			result = kb.isType(i, ce);
		}

		return result;
	}

	static int cc = 0;

	@Override
	public void ensureConsistency() {
		try {
			kb.ensureConsistency();
		} catch (Exception e) {
			throw new InternalReasonerException(e);
		}
	}

	// @Override
	// public long getConsistencyCount() {
	// return kb.getABox().consistencyCount;
	// }

	@Override
	public Collection<ATermAppl> getIndividualsWithProperty(ATermAppl pvP,
			ATermAppl pvIL) {
		return kb.getIndividualsWithProperty(pvP, pvIL);
	}

	@Override
	public Collection<ATermAppl> getPropertyValues(ATermAppl pvP, ATermAppl pvI) {
		return kb.getPropertyValues(pvP, pvI);
	}

	// @Override
	// public long getSatisfiabilityCount() {
	// return kb.getABox().satisfiabilityCount;
	// }

	@Override
	public SizeEstimate<ATermAppl> getSizeEstimate() {
		return estimate;
	}

	@Override
	public boolean hasPropertyValue(ATermAppl p, ATermAppl s, ATermAppl o) {
		return kb.hasPropertyValue(s, p, o);
	}

	@Override
	public boolean isClassAlwaysNonEmpty(ATermAppl sc) {
		if (PelletOptions.USE_CACHING) {
			if (!kb.isSatisfiable(sc)) {
				return false;
			}

			kb.isSatisfiable(ATermUtils.makeNot(sc));
		}

		ATermAppl newUC = ATermUtils.normalize(ATermUtils.makeNot(sc));

		kb.getRBox().addDomain( TOP_OBJECT_PROPERTY, newUC );
				
		ABox copy = kb.getABox().copy();
		copy.setInitialized(false);
		boolean classAlwaysNonEmpty = !copy.isConsistent();

		kb.getRole( TOP_OBJECT_PROPERTY ).removeDomain( newUC, DependencySet.INDEPENDENT );
				
		return classAlwaysNonEmpty;
	}

	@Override
	public boolean isClassified() {
		return kb.isClassified();
	}

	@Override
	public boolean isSatisfiable(ATermAppl arg) {
		return kb.isSatisfiable(arg);
	}

	@Override
	public Collection<ATermAppl> retrieveIndividualsWithProperty(ATermAppl p) {
		return kb.retrieveIndividualsWithProperty(p);
	}

	@Override
	public Map<ATermAppl, Boolean> getKnownInstances(ATermAppl ce) {
		final CandidateSet<ATermAppl> cs = kb.getABox().getObviousInstances(ce);
		final Map<ATermAppl, Boolean> map = new HashMap<ATermAppl, Boolean>();

		for (final ATermAppl a : cs.getKnowns()) {
			map.put(a, true);
		}

		for (final ATermAppl a : cs.getUnknowns()) {
			map.put(a, false);
		}

		return map;
	}

	@Override
	public Boolean hasKnownPropertyValue(ATermAppl p, ATermAppl s, ATermAppl o) {
		return evaluate(kb.hasKnownPropertyValue(s, p, o));
	}

	private Boolean evaluate(final Bool b) {
		if (b.isKnown()) {
			return b.isTrue();
		} else {
			return null;
		}
	}

	@Override
	public Boolean isKnownTypeOf(ATermAppl ce, ATermAppl i) {
		return evaluate(kb.isKnownType(i, ce));
	}

	@Override
	public OWL2QueryFactory<ATermAppl> getFactory() {
		return factory;
	}

	@Override
	public boolean isRealized() {
		return kb.isRealized();
	}

	@Override
	public boolean isComplexClass(ATermAppl o) {
		return ATermUtils.isComplexClass(o);
	}

	@Override
	public Collection<? extends ATermAppl> getKnownPropertyValues(
			ATermAppl pvP, ATermAppl pvI) {
		if (kb.isObjectProperty(pvP)) {
			final Set<ATermAppl> knowns = new HashSet<ATermAppl>();

			kb.getABox().getObjectPropertyValues(pvI, kb.getRole(pvP), knowns,
					new HashSet<ATermAppl>(), true);

			return knowns;
		} else if (kb.isDatatypeProperty(pvP)) {
			return kb.getABox().getObviousDataPropertyValues(pvI,
					kb.getRole(pvP), null);
		} else {
			throw new InternalReasonerException();
		}
	}

	private final Hierarchy<ATermAppl, ATermAppl> classHierarchy = new Hierarchy<ATermAppl, ATermAppl>() {

		@Override
		public Set<ATermAppl> getEquivs(ATermAppl equivG) {
			return kb.getEquivalentClasses(equivG);
		}

		@Override
		public Set<ATermAppl> getSubs(ATermAppl superG, boolean direct) {
			return flatten(kb.getSubClasses(superG,direct));
		}

		@Override
		public Set<ATermAppl> getSupers(ATermAppl superG, boolean direct) {
			return flatten(kb.getSuperClasses(superG, direct));
		}

		@Override
		public Set<ATermAppl> getTops() {
			return Collections.singleton(kb.getTaxonomy().getTop().getName());
		}

		@Override
		public Set<ATermAppl> getBottoms() {
			return Collections
					.singleton(kb.getTaxonomy().getBottom().getName());
		}
		
		private Set<ATermAppl> flatten(final Set<Set<ATermAppl>> toFlatten) {
			Set<ATermAppl> set = new HashSet<ATermAppl>();
			for( final Set<ATermAppl> s : toFlatten) {
				set.addAll(s);
			}
			
			return set;
		}
	};

	@Override
	public Hierarchy<ATermAppl, ATermAppl> getClassHierarchy() {
		return classHierarchy;
	}

	private final Hierarchy<ATermAppl, ATermAppl> propertyHierarchy = new Hierarchy<ATermAppl, ATermAppl>() {

		@Override
		public Set<ATermAppl> getEquivs(ATermAppl equivG) {
			return kb.getRoleTaxonomy(kb.isObjectProperty(equivG))
					.getAllEquivalents(equivG);
		}

		@Override
		public Set<ATermAppl> getSubs(ATermAppl superG, boolean direct) {
			return kb.getRoleTaxonomy(kb.isObjectProperty(superG))
					.getFlattenedSubs(superG, direct);
		}

		@Override
		public Set<ATermAppl> getSupers(ATermAppl superG, boolean direct) {
			return kb.getRoleTaxonomy(kb.isObjectProperty(superG))
					.getFlattenedSupers(superG, direct);
		}

		@Override
		public Set<ATermAppl> getTops() {
			return Collections.singleton(kb.getTaxonomy().getTop().getName());
		}

		@Override
		public Set<ATermAppl> getBottoms() {
			return Collections
					.singleton(kb.getTaxonomy().getBottom().getName());
		}
	};

	@Override
	public Hierarchy<ATermAppl, ATermAppl> getPropertyHierarchy() {
		return propertyHierarchy;
	}

	private final Hierarchy<ATermAppl, ATermAppl> toldClassHierarchy = new Hierarchy<ATermAppl, ATermAppl>() {

		@Override
		public Set<ATermAppl> getEquivs(ATermAppl equivG) {
			if (kb.isClass(equivG)) {
				return kb.getToldTaxonomy().getEquivalents(equivG);
			} else {
				return Collections.emptySet();
			}
		}

		@Override
		public Set<ATermAppl> getSubs(ATermAppl superG, boolean direct) {
			if (ATermUtils.isComplexClass(superG)) {
				return Collections.emptySet();
			} else {
				return kb.getToldTaxonomy().getFlattenedSubs(superG, direct);
			}
		}

		@Override
		public Set<ATermAppl> getSupers(ATermAppl superG, boolean direct) {
			if (ATermUtils.isComplexClass(superG)) {
				return Collections.emptySet();
			} else {
				return kb.getToldTaxonomy().getFlattenedSupers(superG, direct);
			}
		}

		@Override
		public Set<ATermAppl> getTops() {
			return Collections.singleton(kb.getToldTaxonomy().getTop()
					.getName());
		}

		@Override
		public Set<ATermAppl> getBottoms() {
			return Collections.singleton(kb.getToldTaxonomy().getBottom()
					.getName());
		}
	};

	@Override
	public Hierarchy<ATermAppl, ATermAppl> getToldClassHierarchy() {
		return toldClassHierarchy;
	}

	@Override
	public Set<? extends ATermAppl> getAsymmetricProperties() {
		return kb.getAsymmetricProperties();
	}

	@Override
	public Set<? extends ATermAppl> getFunctionalProperties() {
		return kb.getFunctionalProperties();
	}

	@Override
	public Set<? extends ATermAppl> getInverseFunctionalProperties() {
		return kb.getInverseFunctionalProperties();
	}

	@Override
	public Set<? extends ATermAppl> getIrreflexiveProperties() {
		return kb.getIrreflexiveProperties();
	}

	@Override
	public Set<? extends ATermAppl> getReflexiveProperties() {
		return kb.getReflexiveProperties();
	}

	@Override
	public Set<? extends ATermAppl> getSymmetricProperties() {
		return kb.getSymmetricProperties();
	}

	@Override
	public Set<? extends ATermAppl> getTransitiveProperties() {
		return kb.getTransitiveProperties();
	}

	@Override
	public boolean isAsymmetricProperty(ATermAppl Term) {
		return isAsymmetricProperty(Term);
	}

	@Override
	public boolean isFunctionalProperty(ATermAppl Term) {
		return isFunctionalProperty(Term);
	}

	@Override
	public boolean isInverseFunctionalProperty(ATermAppl Term) {
		return isInverseFunctionalProperty(Term);
	}

	@Override
	public boolean isIrreflexiveProperty(ATermAppl Term) {
		return isIrreflexiveProperty(Term);
	}

	@Override
	public boolean isReflexiveProperty(ATermAppl Term) {
		return isReflexiveProperty(Term);
	}

	@Override
	public boolean isSymmetricProperty(ATermAppl Term) {
		return isSymmetricProperty(Term);
	}

	@Override
	public boolean isTransitiveProperty(ATermAppl Term) {
		return isTransitiveProperty(Term);
	}
}
