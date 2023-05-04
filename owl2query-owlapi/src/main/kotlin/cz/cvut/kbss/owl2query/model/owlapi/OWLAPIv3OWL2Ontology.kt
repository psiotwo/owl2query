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
 * along with this program. If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.owl2query.model.owlapi

import cz.cvut.kbss.owl2query.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.model.parameters.Imports
import org.semanticweb.owlapi.reasoner.InferenceType
import org.semanticweb.owlapi.reasoner.OWLReasoner
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory
import org.semanticweb.owlapi.search.EntitySearcher
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import java.util.stream.Collectors

class OWLAPIv3OWL2Ontology(private val m: OWLOntologyManager, private val o: OWLOntology, private val r: OWLReasoner) :
    OWL2Ontology<OWLObject> {
    private val f: OWLDataFactory = OWLManager.getOWLDataFactory()
    private val factory: OWL2QueryFactory<OWLObject>
    private val sizeEstimate: SizeEstimate<OWLObject>
    private val structuralReasoner: OWLReasoner = StructuralReasonerFactory().createReasoner(o)
    private val propertyHierarchy: PropertyHierarchy
    private val classHierarchy: ClassHierarchy
    private val toldClassHierarchy: ToldClassHierarchy

    private fun asOWLNamedIndividual(e: OWLObject): OWLNamedIndividual {
        return when (e) {
            is OWLNamedIndividual -> e
            is OWLEntity -> f.getOWLNamedIndividual(e.iri)
            else -> throw InternalReasonerException()
        }
    }

    init {
        structuralReasoner.precomputeInferences(*InferenceType.values())
        classHierarchy = ClassHierarchy(r)
        toldClassHierarchy = ToldClassHierarchy(structuralReasoner)
        propertyHierarchy = PropertyHierarchy(o, r)
        factory = OWLAPIv3QueryFactory()
        sizeEstimate = SizeEstimateImpl(this)
    }

    private fun asOWLLiteral(e: OWLObject): OWLLiteral {
        return if (e is OWLLiteral) {
            e
        } else {
            throw InternalReasonerException()
        }
    }

    private fun asOWLObjectProperty(e: OWLObject): OWLObjectProperty {
        if (e is OWLObjectProperty) {
            return e
        } else if (e is OWLEntity) {
            if (`is`(e, OWLObjectType.OWLObjectProperty)) {
                return f.getOWLObjectProperty(e.iri)
            }
        }
        throw IllegalArgumentException()
    }

    override fun getClasses(): Set<OWLClass> {
        val set = o.classesInSignature(Imports.INCLUDED).collect(Collectors.toSet())
        set.add(f.owlThing)
        set.add(f.owlNothing)
        return set
    }

    override fun getObjectProperties(): Set<OWLObjectProperty> {
        val set = o.objectPropertiesInSignature(Imports.INCLUDED).collect(Collectors.toSet())
        set.add(f.owlBottomObjectProperty)
        set.add(f.owlTopObjectProperty)
        return set
    }

    override fun getDataProperties(): Set<OWLDataProperty> {
        val set = o.dataPropertiesInSignature(Imports.INCLUDED).collect(Collectors.toSet())
        set.add(f.owlBottomDataProperty)
        set.add(f.owlTopDataProperty)
        return set
    }

    override fun getIndividuals(): Set<OWLNamedIndividual> {
        return o.individualsInSignature(Imports.INCLUDED).collect(Collectors.toSet())
    }

    private val literals: Set<OWLLiteral>
        get() {
            val set: MutableSet<OWLLiteral> = HashSet()
            for (i in individuals) {
                o.dataPropertyAssertionAxioms(i).map { obj: OWLDataPropertyAssertionAxiom -> obj.getObject() }
                    .forEach { e: OWLLiteral ->
                        set.add(
                            e
                        )
                    }
            }
            return set
        }

    override fun getDifferents(i: OWLObject): Set<OWLObject> {
        if (i !is OWLEntity) {
            throw InternalReasonerException()
        }
        return r.getDifferentIndividuals(asOWLNamedIndividual(i)).entities().collect(Collectors.toSet())
    }

    override fun getDomains(pred: OWLObject): Set<OWLObject> {
        val ope = pred.asOWLPropertyExpression(o)
        if (ope.isAnonymous) {
            throw InternalReasonerException()
        } else if (ope.isObjectPropertyExpression) {
            r.getObjectPropertyDomains(ope.asOWLObjectProperty(), true)
        } else if (ope.isDataPropertyExpression) {
            r.getDataPropertyDomains(ope.asOWLDataProperty(), true)
        }
        throw InternalReasonerException()
    }

    override fun getInverses(ope: OWLObject): Set<OWLObject> {
        val opex = ope.asOWLPropertyExpression(o)
        if (opex.isObjectPropertyExpression) {
            return if (opex.isAnonymous) {
                r.getEquivalentObjectProperties(
                    (opex as OWLObjectPropertyExpression).namedProperty
                ).entities().collect(Collectors.toSet())
            } else {
                r.getInverseObjectProperties(
                    (opex as OWLObjectPropertyExpression).namedProperty
                ).entities().collect(Collectors.toSet())
            }
        }
        throw InternalReasonerException()
    }

    override fun getRanges(pred: OWLObject): Set<OWLObject> {
        val ope = pred.asOWLPropertyExpression(o)
        if (ope.isAnonymous) {
            throw InternalReasonerException()
        } else if (ope.isObjectPropertyExpression) {
            return r.getObjectPropertyRanges(ope.asOWLObjectProperty(), true).entities()
                .collect(Collectors.toSet())
        } else if (ope.isDataPropertyExpression) {
            return EntitySearcher.getRanges(ope.asOWLDataProperty(), o).collect(Collectors.toSet())
        }
        throw InternalReasonerException()
    }

    override fun getSames(i: OWLObject): Set<OWLObject> {
        if (i !is OWLEntity) {
            throw InternalReasonerException()
        }
        return r.getSameIndividuals(asOWLNamedIndividual(i)).entities().collect(Collectors.toSet())
    }

    override fun getTypes(i: OWLObject, direct: Boolean): Set<OWLClass> {
        if (i !is OWLEntity) {
            throw InternalReasonerException()
        }
        return r.getTypes(asOWLNamedIndividual(i), direct).entities().collect(Collectors.toSet())
    }

    override fun `is`(e: OWLObject, vararg tt: OWLObjectType): Boolean = `is`(o, e, tt.toSet())

    override fun isSameAs(i1: OWLObject, i2: OWLObject): Boolean {
        val ii1: OWLIndividual = asOWLNamedIndividual(i1)
        val ii2: OWLIndividual = asOWLNamedIndividual(i2)
        return if (i1 == i2) {
            true
        } else r.isEntailed(f.getOWLSameIndividualAxiom(ii1, ii2))
    }

    override fun isDifferentFrom(i1: OWLObject, i2: OWLObject): Boolean {
        if (i1 !is OWLEntity || i2 !is OWLEntity) {
            throw InternalReasonerException()
        }
        return r.isEntailed(
            f.getOWLDifferentIndividualsAxiom(
                asOWLNamedIndividual(i1),
                asOWLNamedIndividual(i2)
            )
        )
    }

    override fun isTypeOf(ce: OWLObject, i: OWLObject, direct: Boolean): Boolean {
        if (`is`(i, OWLObjectType.OWLLiteral)) {
            return false
        }
        val ii = asOWLNamedIndividual(i)
        val cce = ce.asOWLClassExpression()
        return if (direct) {
            r.getInstances(cce, true).containsEntity(ii)
        } else {
            r.isEntailed(f.getOWLClassAssertionAxiom(cce, ii))
        }
    }

    override fun ensureConsistency() {
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.config("Ensure consistency")
        }
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.config("	* isConsistent ?")
        }
        if (!r.isConsistent) {
            throw InternalReasonerException()
        }
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.config("	* true")
        }
    }

    override fun getIndividualsWithProperty(
        pvP: OWLObject,
        pvIL: OWLObject
    ): Set<OWLObject> {
        val pex = pvP.asOWLPropertyExpression(o)
        val set: MutableSet<OWLObject> = HashSet()
        if (pex.isObjectPropertyExpression) {
            if (!`is`(pvIL, OWLObjectType.OWLNamedIndividual)) {
                return set
            }
            val `object` = asOWLNamedIndividual(pvIL)
            for (i in individuals) {
                if (r.isEntailed(
                        f.getOWLObjectPropertyAssertionAxiom(
                            pex as OWLObjectPropertyExpression, i, `object`
                        )
                    )
                ) {
                    set.add(i)
                }
            }
        } else if (pex.isDataPropertyExpression) {
            if (!`is`(pvIL, OWLObjectType.OWLLiteral)) {
                return set
            }
            val `object` = asOWLLiteral(pvIL)
            for (i in individuals) {
                if (r.isEntailed(
                        f.getOWLDataPropertyAssertionAxiom(
                            pex as OWLDataPropertyExpression, i, `object`
                        )
                    )
                ) {
                    set.add(i)
                }
            }
        }
        return set
    }

    override fun getPropertyValues(
        pvP: OWLObject,
        pvI: OWLObject
    ): Set<OWLObject> {
        val pex = pvP.asOWLPropertyExpression(o)
        val ni = asOWLNamedIndividual(pvI)
        if (pex.isObjectPropertyExpression) {
            return if (pex.isOWLTopObjectProperty) {
                individuals
            } else {
                r.getObjectPropertyValues(
                    ni,
                    pex as OWLObjectPropertyExpression
                ).entities().collect(Collectors.toSet())
            }
        } else if (pex.isDataPropertyExpression) {
            return if (pex.isOWLTopDataProperty) {
                literals
            } else {
                r.getDataPropertyValues(ni, pex as OWLDataProperty)
            }
        }
        throw IllegalArgumentException()
    }

    override fun getSizeEstimate(): SizeEstimate<OWLObject> {
        return sizeEstimate
    }

    override fun hasPropertyValue(p: OWLObject, s: OWLObject, o: OWLObject): Boolean {
        val pex = p.asOWLPropertyExpression(this.o)
        if (pex.isObjectPropertyExpression) {
            return r.isEntailed(
                f.getOWLObjectPropertyAssertionAxiom(
                    pex as OWLObjectPropertyExpression, asOWLNamedIndividual(s),
                    asOWLNamedIndividual(o)
                )
            )
        } else if (pex.isDataPropertyExpression) {
            return r.isEntailed(
                f.getOWLDataPropertyAssertionAxiom(
                    pex as OWLDataPropertyExpression, asOWLNamedIndividual(s),
                    asOWLLiteral(o)
                )
            )
        }
        return false
    }

    override fun isClassAlwaysNonEmpty(sc: OWLObject): Boolean {
        val axiom: OWLAxiom = f.getOWLSubClassOfAxiom(sc.asOWLClassExpression(), f.owlNothing)
        return try {
            m.applyChange(AddAxiom(o, axiom))
            val classAlwaysNonEmpty = !r.isConsistent
            m.applyChange(RemoveAxiom(o, axiom))
            classAlwaysNonEmpty
        } catch (e: OWLOntologyChangeException) {
            throw InternalReasonerException()
        }
    }

    override fun isClassified(): Boolean {
        return false
    }

    override fun isSatisfiable(arg: OWLObject): Boolean {
        return r.isSatisfiable(arg.asOWLClassExpression())
    }

    override fun retrieveIndividualsWithProperty(
        odpe: OWLObject
    ): Set<OWLObject?> {
        val ope = odpe.asOWLPropertyExpression(o)
        val set: MutableSet<OWLObject?> = HashSet()
        try {
            if (ope.isObjectPropertyExpression) {
                for (i in individuals) {
                    if (!r.getObjectPropertyValues(
                            i,
                            ope as OWLObjectPropertyExpression
                        ).isEmpty
                    ) {
                        set.add(i)
                    }
                }
            } else if (ope.isObjectPropertyExpression) {
                for (i in individuals) {
                    if (!r.getObjectPropertyValues(
                            i,
                            ope as OWLObjectPropertyExpression
                        ).isEmpty
                    ) {
                        set.add(i)
                    }
                }
            }
        } catch (e: Exception) {
            throw InternalReasonerException(e)
        }
        return set
    }

    override fun getKnownInstances(ce: OWLObject): Map<OWLObject, Boolean> {
        val m: MutableMap<OWLObject, Boolean> = HashMap()
        val cex = ce.asOWLClassExpression()
        for (x in individuals) {
            m[x] = false
        }
        if (!cex.isAnonymous) {
            val owlClass = cex.asOWLClass()
            for (x in EntitySearcher.getIndividuals(
                owlClass,
                o
            ).collect(Collectors.toList())) {
                m[x] = true
            }
        }
        return m
    }

    override fun isKnownTypeOf(ce: OWLObject, i: OWLObject): Boolean {
        val ii: OWLIndividual = asOWLNamedIndividual(i)
        return EntitySearcher.getTypes(ii, o).collect(Collectors.toList()).contains(ce)
    }

    override fun hasKnownPropertyValue(p: OWLObject, s: OWLObject, ob: OWLObject): Boolean {
        val `is`: OWLIndividual = asOWLNamedIndividual(s)
        val pex = p.asOWLPropertyExpression(o)
        if (pex.isObjectPropertyExpression) {
            val ope = p as OWLObjectPropertyExpression
            return if (ope is OWLObjectInverseOf) {
                val opeInv = ope.getInverseProperty()
                o.axioms(AxiomType.OBJECT_PROPERTY_ASSERTION)
                    .anyMatch { ax: OWLObjectPropertyAssertionAxiom -> ax.getObject() == s && ax.property == opeInv && ax.subject == ob }
            } else {
                EntitySearcher.getObjectPropertyValues(
                    `is`, pex as OWLObjectPropertyExpression,
                    o
                ).collect(Collectors.toList()).contains(ob)
            }
        } else if (pex.isDataPropertyExpression) {
            return EntitySearcher.getDataPropertyValues(
                `is`, pex as OWLDataPropertyExpression,
                o
            ).collect(Collectors.toList()).contains(ob)
        }
        return false
    }

    override fun getInstances(ic: OWLObject, direct: Boolean): Set<OWLObject> {
        val c = ic.asOWLClassExpression()
        return r.getInstances(c, direct).entities().collect(Collectors.toSet())
    }

    override fun getFactory(): OWL2QueryFactory<OWLObject> {
        return factory
    }

    override fun isRealized(): Boolean {
        return false
    }

    override fun isComplexClass(c: OWLObject): Boolean {
        return c is OWLClassExpression
    }

    override fun getKnownPropertyValues(
        pvP: OWLObject, pvI: OWLObject
    ): Collection<OWLObject> {
        val p = pvP.asOWLPropertyExpression(o)
        val ni = asOWLNamedIndividual(pvI)
        val result: Collection<OWLObject> = if (p.isObjectPropertyExpression) {
            structuralReasoner.getObjectPropertyValues(
                ni,
                p as OWLObjectPropertyExpression
            ).flattened
        } else if (p.isDataPropertyExpression && !p.isAnonymous) {
            structuralReasoner.getDataPropertyValues(
                ni,
                p as OWLDataProperty
            )
        } else {
            throw IllegalArgumentException()
        }
        return result
    }

    override fun getClassHierarchy(): Hierarchy<OWLObject, out OWLObject?> {
        return classHierarchy
    }

    override fun getToldClassHierarchy(): Hierarchy<OWLObject, out OWLObject?> {
        return toldClassHierarchy
    }

    override fun getPropertyHierarchy(): Hierarchy<OWLObject, out OWLObject> {
        return propertyHierarchy
    }

    override fun getFunctionalProperties(): Set<OWLProperty> {
        val set: MutableSet<OWLProperty> = HashSet()
        for (p in objectProperties) {
            if (r.isEntailed(
                    f.getOWLFunctionalObjectPropertyAxiom(
                        p
                            .asOWLObjectProperty()
                    )
                )
            ) {
                set.add(p)
            }
        }
        for (p in dataProperties) {
            if (r.isEntailed(
                    f.getOWLFunctionalDataPropertyAxiom(
                        p
                            .asOWLDataProperty()
                    )
                )
            ) {
                set.add(p)
            }
        }
        return set
    }

    override fun getInverseFunctionalProperties(): Set<OWLObject> {
        val set: MutableSet<OWLObjectProperty> = HashSet()
        for (p in objectProperties) {
            if (r.isEntailed(f.getOWLInverseFunctionalObjectPropertyAxiom(p))) {
                set.add(p)
            }
        }
        return set
    }

    override fun getIrreflexiveProperties(): Set<OWLObject> {
        val set: MutableSet<OWLObjectProperty> = HashSet()
        for (p in objectProperties) {
            if (r.isEntailed(f.getOWLIrreflexiveObjectPropertyAxiom(p))) {
                set.add(p)
            }
        }
        return set
    }

    override fun getReflexiveProperties(): Set<OWLObject> {
        val set: MutableSet<OWLObjectProperty> = HashSet()
        for (p in objectProperties) {
            if (r.isEntailed(f.getOWLReflexiveObjectPropertyAxiom(p))) {
                set.add(p)
            }
        }
        return set
    }

    override fun getSymmetricProperties(): Set<OWLObject> {
        val set: MutableSet<OWLObjectProperty> = HashSet()
        for (p in objectProperties) {
            if (r.isEntailed(f.getOWLSymmetricObjectPropertyAxiom(p))) {
                set.add(p)
            }
        }
        return set
    }

    override fun getAsymmetricProperties(): Set<OWLObjectProperty> {
        val set: MutableSet<OWLObjectProperty> = HashSet()
        for (p in objectProperties) {
            if (r.isEntailed(f.getOWLAsymmetricObjectPropertyAxiom(p))) {
                set.add(p)
            }
        }
        return set
    }

    override fun getTransitiveProperties(): Set<OWLObject> {
        val set: MutableSet<OWLObjectProperty> = HashSet()
        for (p in objectProperties) {
            if (r.isEntailed(f.getOWLTransitiveObjectPropertyAxiom(p))) {
                set.add(p)
            }
        }
        return set
    }

    override fun isFunctionalProperty(term: OWLObject): Boolean {
        return when (term.asOWLPropertyExpression(o)) {
            is OWLObjectProperty -> r.isEntailed(
                f
                    .getOWLFunctionalObjectPropertyAxiom(asOWLObjectProperty(term))
            )

            is OWLDataProperty -> r.isEntailed(
                f
                    .getOWLFunctionalDataPropertyAxiom(term as OWLDataProperty)
            )

            else -> false
        }
    }

    override fun isInverseFunctionalProperty(term: OWLObject): Boolean {
        return r.isEntailed(
            f
                .getOWLInverseFunctionalObjectPropertyAxiom(Objects.requireNonNull(asOWLObjectProperty(term)))
        )
    }

    override fun isIrreflexiveProperty(term: OWLObject): Boolean {
        return r.isEntailed(
            f
                .getOWLIrreflexiveObjectPropertyAxiom(Objects.requireNonNull(asOWLObjectProperty(term)))
        )
    }

    override fun isReflexiveProperty(term: OWLObject): Boolean {
        return r.isEntailed(
            f
                .getOWLReflexiveObjectPropertyAxiom(Objects.requireNonNull(asOWLObjectProperty(term)))
        )
    }

    override fun isSymmetricProperty(term: OWLObject): Boolean {
        return r.isEntailed(
            f
                .getOWLSymmetricObjectPropertyAxiom(Objects.requireNonNull(asOWLObjectProperty(term)))
        )
    }

    override fun isAsymmetricProperty(term: OWLObject): Boolean {
        return r.isEntailed(
            f
                .getOWLAsymmetricObjectPropertyAxiom(asOWLObjectProperty(term))
        )
    }

    override fun isTransitiveProperty(term: OWLObject): Boolean {
        return r.isEntailed(
            f
                .getOWLTransitiveObjectPropertyAxiom(Objects.requireNonNull(asOWLObjectProperty(term)))
        )
    }

    override fun getDatatypeOfLiteral(literal: OWLObject): String {
        return if (literal is OWLLiteral) {
            literal.datatype.iri.toString()
        } else {
            throw OWL2QueryException("Expected literal, but got $literal")
        }
    }

    companion object {
        private val LOG = Logger
            .getLogger(OWLAPIv3OWL2Ontology::class.java.name)
    }
}
