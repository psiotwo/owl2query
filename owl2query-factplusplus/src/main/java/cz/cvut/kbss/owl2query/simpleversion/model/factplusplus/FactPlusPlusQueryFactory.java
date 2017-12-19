package cz.cvut.kbss.owl2query.model.factplusplus;

import java.util.Set;

import uk.ac.manchester.cs.factplusplus.ClassPointer;
import uk.ac.manchester.cs.factplusplus.DataPropertyPointer;
import uk.ac.manchester.cs.factplusplus.DataTypeExpressionPointer;
import uk.ac.manchester.cs.factplusplus.DataValuePointer;
import uk.ac.manchester.cs.factplusplus.FaCTPlusPlus;
import uk.ac.manchester.cs.factplusplus.FaCTPlusPlusException;
import uk.ac.manchester.cs.factplusplus.IndividualPointer;
import uk.ac.manchester.cs.factplusplus.ObjectPropertyPointer;
import uk.ac.manchester.cs.factplusplus.Pointer;
import cz.cvut.kbss.owl2query.engine.AbstractOWL2QueryFactory;
import cz.cvut.kbss.owl2query.model.InternalReasonerException;

class FactPlusPlusQueryFactory extends AbstractOWL2QueryFactory<Pointer> {

	FaCTPlusPlus fpp;

	FactPlusPlusQueryFactory(final FaCTPlusPlus fpp) {
		this.fpp = fpp;
	}

	private IndividualPointer asOWLNamedIndividual(final Pointer e) {
		return (IndividualPointer) e;
	}

	private DataValuePointer asDataValue(final Pointer e) {
		return (DataValuePointer) e;
	}

	private ClassPointer asClass(final Pointer e) {
		return (ClassPointer) e;
	}

	private ObjectPropertyPointer asObjectProperty(final Pointer e) {
		return (ObjectPropertyPointer) e;
	}

	private DataPropertyPointer asDataProperty(final Pointer e) {
		return (DataPropertyPointer) e;
	}

	private DataTypeExpressionPointer asDatatypeExpression(final Pointer e) {
		return (DataTypeExpressionPointer) e;
	}

	@Override
	public Pointer dataExactCardinality(int card, Pointer ope, Pointer dr) {
		try {
			return fpp.getDataExact(card, asDataProperty(ope),
					asDatatypeExpression(dr));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public Pointer dataMaxCardinality(int card, Pointer ope, Pointer dr) {
		try {
			return fpp.getDataAtMost(card, asDataProperty(ope),
					asDatatypeExpression(dr));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public Pointer dataMinCardinality(int card, Pointer ope, Pointer dr) {
		try {
			return fpp.getDataAtLeast(card, asDataProperty(ope),
					asDatatypeExpression(dr));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public Pointer dataOneOf(Set<Pointer> nis) {
		try {
			fpp.initArgList();
			for (final Pointer p : nis) {
				fpp.addArg(p);
			}
			fpp.closeArgList();
			return fpp.getDataUnionOf();
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public Pointer dataSomeValuesFrom(Pointer dpe, Pointer c) {
		try {
			return fpp
					.getDataSome(asDataProperty(dpe), asDatatypeExpression(c));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public ClassPointer getNothing() {
		try {
			return fpp.getNothing();
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException();
		}
	}

	@Override
	public ClassPointer getThing() {
		try {
			return fpp.getThing();
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException();
		}
	}

	@Override
	public Pointer getTopDatatype() {
		try {
			return fpp.getDataTop();
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException();
		}
	}

	@Override
	public Pointer inverseObjectProperty(Pointer op) {
		try {
			return fpp.getInverseProperty(asObjectProperty(op));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException();
		}

	}

	@Override
	public Pointer literal(String s) {
		try {
			return fpp
					.getDataValue(
							s,
							fpp
									.getBuiltInDataType("http://www.w3.org/1999/02/22-rdf-syntax-ns#PlainLiteral"));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException();
		}
	}

	@Override
	public Pointer literal(String s, String lang) {
		return literal(s);
	}

	@Override
	public Pointer namedClass(String iri) {
		try {
			return fpp.getNamedClass(iri);
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException();
		}
	}

	@Override
	public Pointer namedDataProperty(String iri) {
		try {
			return fpp.getDataProperty(iri);
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException();
		}
	}

	@Override
	public Pointer namedDataRange(String iri) {
		try {
			return fpp.getBuiltInDataType(iri);
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException();
		}
	}

	@Override
	public Pointer namedIndividual(String iri) {
		try {
			return fpp.getIndividual(iri);
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException();
		}
	}

	@Override
	public Pointer namedObjectProperty(String iri) {
		try {
			return fpp.getObjectProperty(iri);
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException();
		}
	}

	@Override
	public Pointer objectAllValuesFrom(Pointer ope, Pointer ce) {
		try {
			return fpp.getObjectAll(asObjectProperty(ope), asClass(ce));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public Pointer objectComplementOf(Pointer ce) {
		try {
			return fpp.getConceptNot(asClass(ce));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException();
		}
	}

	@Override
	public Pointer objectExactCardinality(int card, Pointer ope, Pointer ce) {
		try {
			return fpp.getObjectExact(card, asObjectProperty(ope), asClass(ce));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public Pointer objectHasSelf(Pointer ope) {
		try {
			return fpp.getSelf(asObjectProperty(ope));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public Pointer objectHasValue(Pointer ope, Pointer ni) {
		try {
			return fpp.getObjectValue(asObjectProperty(ope),
					asOWLNamedIndividual(ni));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public Pointer objectIntersectionOf(Set<Pointer> c) {
		try {
			fpp.initArgList();
			for (final Pointer p : c) {
				fpp.addArg(p);
			}
			fpp.closeArgList();
			return fpp.getConceptAnd();
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public Pointer objectMaxCardinality(int card, Pointer ope, Pointer ce) {
		try {
			return fpp
					.getObjectAtMost(card, asObjectProperty(ope), asClass(ce));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public Pointer objectMinCardinality(int card, Pointer ope, Pointer ce) {
		try {
			return fpp.getObjectAtLeast(card, asObjectProperty(ope),
					asClass(ce));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public Pointer objectOneOf(Set<Pointer> nis) {
		try {
			fpp.initArgList();
			for (final Pointer p : nis) {
				fpp.addArg(p);
			}
			fpp.closeArgList();
			return fpp.getConceptOr();
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	// private IndividualPointer getOWLNamedIndividual(final Pointer ni) {
	// if (ni instanceof IndividualPointer) {
	// return (IndividualPointer) ni;
	// } else {
	// return fpp.getIndividual();
	// }
	// }

	@Override
	public Pointer objectSomeValuesFrom(Pointer ope, Pointer ce) {
		try {
			return fpp.getObjectSome(asObjectProperty(ope), asClass(ce));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public Pointer objectUnionOf(Set<Pointer> c) {
		try {
			fpp.initArgList();
			for (final Pointer p : c) {
				fpp.addArg(p);
			}
			fpp.closeArgList();
			return fpp.getConceptOr();
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public Pointer typedLiteral(String s, String dt) {
		try {
			return fpp.getDataValue(s, fpp.getBuiltInDataType(dt));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public DataPropertyPointer getBottomDataProperty() {
		try {
			return fpp.getBottomDataProperty();
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public ObjectPropertyPointer getBottomObjectProperty() {
		try {
			return fpp.getBottomObjectProperty();
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public Pointer getTopDataProperty() {
		try {
			return fpp.getTopDataProperty();
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public Pointer getTopObjectProperty() {
		try {
			return fpp.getTopObjectProperty();
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public Pointer dataAllValuesFrom(Pointer ope, Pointer ce) {
		try {
			return fpp
					.getDataAll(asDataProperty(ope), asDatatypeExpression(ce));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public Pointer dataHasValue(Pointer ope, Pointer ni) {
		try {
			return fpp.getDataValue(asDataProperty(ope), asDataValue(ni));
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public Pointer dataIntersectionOf(Set<Pointer> c) {
		try {
			fpp.initArgList();
			for (final Pointer p : c) {
				fpp.addArg(p);
			}
			fpp.closeArgList();
			return fpp.getDataIntersectionOf();
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}

	@Override
	public Pointer dataUnionOf(Set<Pointer> c) {
		try {
			fpp.initArgList();
			for (final Pointer p : c) {
				fpp.addArg(p);
			}
			fpp.closeArgList();
			return fpp.getDataUnionOf();
		} catch (FaCTPlusPlusException e) {
			throw new InternalReasonerException(e);
		}
	}
}
