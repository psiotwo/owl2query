package cz.cvut.kbss.owl2query.model.factplusplus;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mindswap.pellet.exceptions.InternalReasonerException;

import uk.ac.manchester.cs.factplusplus.ClassPointer;
import uk.ac.manchester.cs.factplusplus.DataPropertyPointer;
import uk.ac.manchester.cs.factplusplus.DataValuePointer;
import uk.ac.manchester.cs.factplusplus.FaCTPlusPlus;
import uk.ac.manchester.cs.factplusplus.FaCTPlusPlusException;
import uk.ac.manchester.cs.factplusplus.IndividualPointer;
import uk.ac.manchester.cs.factplusplus.ObjectPropertyPointer;
import uk.ac.manchester.cs.factplusplus.Pointer;
import cz.cvut.kbss.owl2query.model.Hierarchy;
import cz.cvut.kbss.owl2query.model.OWL2Ontology;
import cz.cvut.kbss.owl2query.model.OWL2QueryFactory;
import cz.cvut.kbss.owl2query.model.OWLObjectType;
import cz.cvut.kbss.owl2query.model.SizeEstimate;
import cz.cvut.kbss.owl2query.model.SizeEstimateImpl;

public class FactPlusPlusOWL2Ontology implements OWL2Ontology<Pointer> {

	private FaCTPlusPlus fpp;

	private OWL2QueryFactory<Pointer> factory;

	public FactPlusPlusOWL2Ontology(final FaCTPlusPlus fpp) {

		this.fpp = fpp;

		factory = new FactPlusPlusQueryFactory(fpp);
	}

	@Override
	public void ensureConsistency() {
		try {
			fpp.isKBConsistent();
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public Set<? extends Pointer> getClasses() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Pointer> getDataProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Pointer> getDifferents(Pointer i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Pointer> getDomains(Pointer pred) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWL2QueryFactory<Pointer> getFactory() {
		return factory;
	}

	@Override
	public Set<? extends Pointer> getIndividuals() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends Pointer> getIndividualsWithProperty(
			Pointer pvP, Pointer pvIL) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<IndividualPointer> getInstances(Pointer ic, boolean direct) {
		try {
			return asCollection(fpp.askInstances(asClass(ic), direct));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public Set<? extends Pointer> getInverses(Pointer ope) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Pointer, Boolean> getKnownInstances(Pointer ic) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends Pointer> getKnownPropertyValues(Pointer pvP,
			Pointer pvI) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ObjectPropertyPointer> getObjectProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends Pointer> getPropertyValues(Pointer pvP,
			Pointer pvI) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Pointer> getRanges(Pointer pred) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Pointer> getSames(Pointer i) {
		try {
			return asCollection(fpp.askSameAs(asIndividual(i)));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public SizeEstimate<Pointer> getSizeEstimate() {
		return new SizeEstimateImpl<Pointer>(this);
	}

	@Override
	public Set<ClassPointer> getTypes(Pointer i, boolean direct) {
		try {
			return asCollection(fpp.askIndividualTypes(asIndividual(i), direct));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public Boolean hasKnownPropertyValue(Pointer Term, Pointer Term2,
			Pointer Term3) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasPropertyValue(Pointer p, Pointer s, Pointer o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isClassAlwaysNonEmpty(Pointer sc) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isClassified() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isComplexClass(Pointer o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean is(Pointer e, final OWLObjectType... tt) {
		boolean result = false;

		for (final OWLObjectType t : tt) {
			switch (t) {
			case OWLLiteral:
				result = e instanceof DataValuePointer;
				break;
			case OWLAnnotationProperty:
				// TODO;
				break;
			case OWLDataProperty:
				result = e instanceof DataPropertyPointer;
				break;
			case OWLObjectProperty:
				result = e instanceof ObjectPropertyPointer;
				break;
			case OWLClass:
				result = e instanceof ClassPointer;
				break;
			case OWLNamedIndividual:
				result = e instanceof IndividualPointer;
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
	public boolean isDifferentFrom(Pointer i1, Pointer i2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Boolean isKnownTypeOf(Pointer ce, Pointer i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRealized() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSameAs(Pointer i1, Pointer i2) {
		try {
			return fpp.isSameAs(asIndividual(i1), asIndividual(i2));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public boolean isSatisfiable(Pointer arg) {
		try {
			return fpp.isClassSatisfiable(asClass(arg));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public boolean isTypeOf(Pointer ce, Pointer i, boolean direct) {
		try {
			if (direct) {
				return asCollection(
						fpp.askIndividualTypes(asIndividual(i), direct))
						.contains(asClass(ce));
			} else {
				return fpp.isInstanceOf(asIndividual(i), asClass(ce));
			}
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public Collection<?> retrieveIndividualsWithProperty(Pointer Term) {
		// TODO Auto-generated method stub
		return null;
	}

	private <T> Set<T> asCollection(T[][] clsPointers) {
		Set<T> clsSets = new HashSet<T>();
		for (T[] clsPointArray : clsPointers) {
			for (T clsPointer : clsPointArray) {
				clsSets.add(clsPointer);
			}
		}
		return clsSets;
	}

	private <T> Set<T> asCollection(T[] clsPointers) {
		Set<T> clsSets = new HashSet<T>();
		for (T clsPointer : clsPointers) {
			clsSets.add(clsPointer);
		}
		return clsSets;
	}

	private IndividualPointer asIndividual(final Pointer arg) {
		return ((IndividualPointer) arg);
	}

	private ClassPointer asClass(final Pointer arg) {
		return ((ClassPointer) arg);
	}

	private ObjectPropertyPointer asObjectProperty(Pointer arg) {
		return (ObjectPropertyPointer) arg;
	}

	private final Hierarchy<Pointer, ClassPointer> classHierarchy = new Hierarchy<Pointer, ClassPointer>() {

		@Override
		public Set<ClassPointer> getEquivs(Pointer equivG) {
			try {
				return asCollection(fpp.askEquivalentClasses(asClass(equivG)));
			} catch (FaCTPlusPlusException e) {
				throw new InternalReasonerException(e);
			}
		}

		@Override
		public Set<ClassPointer> getSubs(Pointer superG, boolean direct) {
			try {
				return asCollection(fpp.askSubClasses(asClass(superG), direct));
			} catch (FaCTPlusPlusException e) {
				throw new InternalReasonerException(e);
			}
		}

		@Override
		public Set<ClassPointer> getSupers(Pointer superG, boolean direct) {
			try {
				return asCollection(fpp
						.askSuperClasses(asClass(superG), direct));
			} catch (FaCTPlusPlusException e) {
				throw new InternalReasonerException(e);
			}
		}

		@Override
		public Set<ClassPointer> getTops() {
			try {
				return Collections.singleton(fpp.getThing());
			} catch (FaCTPlusPlusException e) {
				throw new InternalReasonerException(e);
			}
		}

		@Override
		public Set<ClassPointer> getBottoms() {
			try {
				return Collections.singleton(fpp.getNothing());
			} catch (FaCTPlusPlusException e) {
				throw new InternalReasonerException(e);
			}
		}
	};

	@Override
	public Hierarchy<Pointer, ClassPointer> getClassHierarchy() {
		return classHierarchy;
	}

	@Override
	public Hierarchy<Pointer, Pointer> getPropertyHierarchy() {
		// TODO Auto-generated method stub
		return null;
	}

	public Hierarchy<Pointer, ClassPointer> getToldClassHierarchy() {
		// TODO
		return classHierarchy;
	}

	@Override
	public Set<ObjectPropertyPointer> getAsymmetricProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Pointer> getFunctionalProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Pointer> getInverseFunctionalProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Pointer> getIrreflexiveProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Pointer> getReflexiveProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends Pointer> getSymmetricProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ObjectPropertyPointer> getTransitiveProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAsymmetricProperty(Pointer Term) {
		try {
			return fpp.isObjectPropertyAntiSymmetric(asObjectProperty(Term));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public boolean isFunctionalProperty(Pointer Term) {
		try {
			return fpp.isObjectPropertyFunctional(asObjectProperty(Term));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public boolean isInverseFunctionalProperty(Pointer Term) {
		try {
			return fpp
					.isObjectPropertyInverseFunctional(asObjectProperty(Term));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public boolean isIrreflexiveProperty(Pointer Term) {
		try {
			return fpp.isObjectPropertyIrreflexive(asObjectProperty(Term));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public boolean isReflexiveProperty(Pointer Term) {
		try {
			return fpp.isObjectPropertyReflexive(asObjectProperty(Term));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public boolean isSymmetricProperty(Pointer Term) {
		try {
			return fpp.isObjectPropertySymmetric(asObjectProperty(Term));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public boolean isTransitiveProperty(Pointer Term) {
		try {
			return fpp.isObjectPropertyTransitive(asObjectProperty(Term));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}
}
