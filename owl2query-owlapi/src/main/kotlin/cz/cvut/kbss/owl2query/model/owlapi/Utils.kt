package cz.cvut.kbss.owl2query.model.owlapi

import cz.cvut.kbss.owl2query.model.OWLObjectType
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.model.parameters.Imports

val f: OWLDataFactory = OWLManager.getOWLDataFactory()

fun OWLObject?.asOWLClassExpression(): OWLClassExpression {
    if (this is OWLClassExpression) {
        return this
    } else if (this is OWLEntity) {
        // if (o.containsClassInSignature(ee.getIRI())) {
        return f.getOWLClass(this.iri)
        // }
    }
    throw IllegalArgumentException()
}

fun OWLObject?.asOWLPropertyExpression(o: OWLOntology): OWLPropertyExpression {
    if (this is OWLEntity) {
        return if (`is`(o, this, setOf(OWLObjectType.OWLDataProperty))) {
            f.getOWLDataProperty(this.iri)
        } else {
            f.getOWLObjectProperty(this.iri)
        }
    } else if (this is OWLPropertyExpression) {
        return this
    }
    throw IllegalArgumentException()
}

fun `is`(o: OWLOntology, e: OWLObject, tt: Set<OWLObjectType>): Boolean {
    var result = false
    for (t in tt) {
        when (t) {
            OWLObjectType.OWLLiteral -> result = e is OWLLiteral
            OWLObjectType.OWLAnnotationProperty -> if (e is OWLEntity) {
                result = o.containsAnnotationPropertyInSignature(e.iri, Imports.INCLUDED)
            }

            OWLObjectType.OWLDataProperty -> if (e is OWLEntity) {
                result = o.containsDataPropertyInSignature(
                    e.iri,
                    Imports.INCLUDED
                ) || e == f.owlTopDataProperty || e == f.owlBottomDataProperty
            }

            OWLObjectType.OWLObjectProperty -> if (e is OWLEntity) {
                result = o.containsObjectPropertyInSignature(
                    e.iri,
                    Imports.INCLUDED
                ) || e == f.owlTopObjectProperty || (e
                        == f.owlBottomObjectProperty)
            }

            OWLObjectType.OWLClass -> if (e is OWLEntity) {
                result = o.containsClassInSignature(e.iri, Imports.INCLUDED) || e == f.owlThing || e == f.owlNothing
            }

            OWLObjectType.OWLNamedIndividual -> if (e is OWLEntity) {
                result = o.containsIndividualInSignature(e.iri, Imports.INCLUDED)
            }

            else -> {}
        }
        if (result) {
            break
        }
    }
    return result
}


