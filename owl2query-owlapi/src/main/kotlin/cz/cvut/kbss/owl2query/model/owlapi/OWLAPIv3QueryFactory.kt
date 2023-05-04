package cz.cvut.kbss.owl2query.model.owlapi

import cz.cvut.kbss.owl2query.engine.AbstractOWL2QueryFactory
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.vocab.OWL2Datatype

class OWLAPIv3QueryFactory : AbstractOWL2QueryFactory<OWLObject>() {

    private val factory = OWLManager.getOWLDataFactory()

    override fun getNothing(): OWLClass = factory.owlNothing

    override fun getThing(): OWLClass = factory.owlThing

    override fun getTopDatatype(): OWLDatatype = factory.topDatatype

    override fun getBottomDataProperty(): OWLDataProperty = factory.owlBottomDataProperty

    override fun getBottomObjectProperty(): OWLObjectProperty = factory.owlBottomObjectProperty

    override fun getTopDataProperty(): OWLDataProperty = factory.owlTopDataProperty

    override fun getTopObjectProperty(): OWLObjectProperty = factory.owlTopObjectProperty

    override fun literal(s: String): OWLObject = factory.getOWLLiteral(s, OWL2Datatype.RDF_PLAIN_LITERAL)

    override fun literal(s: String, lang: String?): OWLObject = factory.getOWLLiteral(s, lang)

    override fun namedClass(iri: String): OWLObject = factory.getOWLClass(IRI.create(iri))

    override fun namedDataProperty(iri: String): OWLObject = factory.getOWLDataProperty(IRI.create(iri))

    override fun namedDataRange(iri: String): OWLObject = factory.getOWLDatatype(IRI.create(iri))

    override fun namedIndividual(iri: String): OWLObject = factory.getOWLNamedIndividual(IRI.create(iri))

    override fun namedObjectProperty(iri: String): OWLObject = factory.getOWLObjectProperty(IRI.create(iri))

    private fun getOWLClassExpression(ope: OWLObject): OWLClassExpression =
        when (ope) {
            is OWLClassExpression -> ope
            is OWLEntity -> factory.getOWLClass(ope.iri)
            else -> throw IllegalArgumentException("Illegal object type: $ope")
        }

    private fun getOWLClassExpressions(ce: Set<OWLObject>): Set<OWLClassExpression> =
        ce.map { getOWLClassExpression(it) }.toSet()

    private fun getOWLDataRanges(ce: Set<OWLObject>): Set<OWLDataRange> =
        ce.map { getOWLDataRange(it) }.toSet()

    private fun getOWLObjectPropertyExpression(ope: OWLObject): OWLObjectPropertyExpression =
        when (ope) {
            is OWLObjectPropertyExpression -> ope
            is OWLEntity -> factory.getOWLObjectProperty(ope.iri)
            else -> throw IllegalArgumentException("Illegal object type: $ope")
        }

    private fun getOWLDataPropertyExpression(dpe: OWLObject): OWLDataPropertyExpression =
        when (dpe) {
            is OWLDataPropertyExpression -> dpe
            is OWLEntity -> factory.getOWLDataProperty(dpe.iri)
            else -> throw IllegalArgumentException("Illegal object type: $dpe")
        }

    private fun getOWLNamedIndividual(ni: OWLObject): OWLNamedIndividual =
        when (ni) {
            is OWLNamedIndividual -> ni
            is OWLEntity -> factory.getOWLNamedIndividual(ni.iri)
            else -> throw IllegalArgumentException("Illegal object type: $ni")
        }

    private fun getOWLNamedIndividuals(nisO: Set<OWLObject>): Set<OWLNamedIndividual> =
        nisO.map { getOWLNamedIndividual(it) }.toSet()

    private fun getOWLLiteral(ni: OWLObject): OWLLiteral =
        if (ni is OWLLiteral) {
            ni
        } else {
            throw IllegalArgumentException("Illegal object type: $ni")
        }

    private fun getOWLLiterals(nisO: Set<OWLObject>): Set<OWLLiteral> =
        nisO.map { getOWLLiteral(it) }.toSet()

    private fun getOWLDataRange(ope: OWLObject): OWLDataRange = when (ope) {
        is OWLDataRange -> ope
        is OWLEntity -> factory.getOWLDatatype(ope.iri)
        else -> throw IllegalArgumentException("Illegal object type: $ope")
    }

    override fun dataExactCardinality(card: Int, dpe: OWLObject, dr: OWLObject): OWLObject =
        factory.getOWLDataExactCardinality(
            card,
            getOWLDataPropertyExpression(dpe), getOWLDataRange(dr)
        )

    override fun dataMaxCardinality(card: Int, dpe: OWLObject, dr: OWLObject): OWLObject =
        factory.getOWLDataMaxCardinality(
            card,
            getOWLDataPropertyExpression(dpe), getOWLDataRange(dr)
        )

    override fun dataMinCardinality(card: Int, dpe: OWLObject, dr: OWLObject): OWLObject =
        factory.getOWLDataMinCardinality(
            card,
            getOWLDataPropertyExpression(dpe), getOWLDataRange(dr)
        )

    override fun dataOneOf(nis: Set<OWLObject>): OWLObject = factory.getOWLDataOneOf(getOWLLiterals(nis))

    override fun dataSomeValuesFrom(dpe: OWLObject, ce: OWLObject): OWLObject = factory.getOWLDataSomeValuesFrom(
        getOWLDataPropertyExpression(dpe),
        getOWLDataRange(ce)
    )

    override fun inverseObjectProperty(op: OWLObject): OWLObject =
        factory.getOWLObjectInverseOf(getOWLObjectPropertyExpression(op).asOWLObjectProperty())

    override fun objectAllValuesFrom(ope: OWLObject, ce: OWLObject): OWLObject = factory.getOWLObjectAllValuesFrom(
        getOWLObjectPropertyExpression(ope),
        getOWLClassExpression(ce)
    )

    override fun objectComplementOf(c: OWLObject): OWLObject =
        factory.getOWLObjectComplementOf(getOWLClassExpression(c))

    override fun objectExactCardinality(card: Int, ope: OWLObject, ce: OWLObject): OWLObject =
        factory.getOWLObjectExactCardinality(
            card,
            getOWLObjectPropertyExpression(ope), getOWLClassExpression(ce)
        )

    override fun objectHasSelf(ope: OWLObject): OWLObject =
        factory.getOWLObjectHasSelf(getOWLObjectPropertyExpression(ope))

    override fun objectHasValue(ope: OWLObject, ni: OWLObject): OWLObject =
        factory.getOWLObjectHasValue(
            getOWLObjectPropertyExpression(ope),
            getOWLNamedIndividual(ni)
        )

    override fun objectIntersectionOf(c: Set<OWLObject>): OWLObject =
        factory.getOWLObjectIntersectionOf(getOWLClassExpressions(c))

    override fun objectMaxCardinality(card: Int, ope: OWLObject, ce: OWLObject): OWLObject =
        factory.getOWLObjectMaxCardinality(
            card,
            getOWLObjectPropertyExpression(ope), getOWLClassExpression(ce)
        )

    override fun objectMinCardinality(card: Int, ope: OWLObject, ce: OWLObject): OWLObject =
        factory.getOWLObjectMinCardinality(
            card,
            getOWLObjectPropertyExpression(ope), getOWLClassExpression(ce)
        )

    override fun objectOneOf(nis: Set<OWLObject>): OWLObject = factory.getOWLObjectOneOf(getOWLNamedIndividuals(nis))

    override fun objectSomeValuesFrom(ope: OWLObject, ce: OWLObject): OWLObject =
        factory.getOWLObjectSomeValuesFrom(
            getOWLObjectPropertyExpression(ope), getOWLClassExpression(ce)
        )

    override fun objectUnionOf(set: Set<OWLObject>): OWLObject =
        factory.getOWLObjectUnionOf(getOWLClassExpressions(set))

    override fun typedLiteral(s: String, datatype: String): OWLObject =
        factory.getOWLLiteral(s, factory.getOWLDatatype(IRI.create(datatype)))

    override fun dataAllValuesFrom(ope: OWLObject, ce: OWLObject): OWLObject =
        factory.getOWLDataAllValuesFrom(
            getOWLDataPropertyExpression(ope),
            getOWLDataRange(ce)
        )

    override fun dataHasValue(ope: OWLObject, ni: OWLObject): OWLObject =
        factory.getOWLDataHasValue(
            getOWLDataPropertyExpression(ope),
            getOWLLiteral(ni)
        )

    override fun dataIntersectionOf(c: Set<OWLObject>): OWLObject =
        factory.getOWLDataIntersectionOf(getOWLDataRanges(c))

    override fun dataUnionOf(c: Set<OWLObject>): OWLObject =
        factory.getOWLDataUnionOf(getOWLDataRanges(c))
}