package cz.cvut.kbss.owl2query.model.owlapi

import cz.cvut.kbss.owl2query.model.Hierarchy
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLClassExpression
import org.semanticweb.owlapi.model.OWLDataFactory
import org.semanticweb.owlapi.model.OWLObject
import org.semanticweb.owlapi.reasoner.OWLReasoner
import java.util.stream.Collectors

class ToldClassHierarchy(
    private val reasoner: OWLReasoner
) : Hierarchy<OWLObject, OWLClass> {

    val factory: OWLDataFactory = OWLManager.getOWLDataFactory()

    override fun getEquivs(ce: OWLObject): Set<OWLClass> {
        val cex: OWLClassExpression = ce.asOWLClassExpression()
        return if (cex.isAnonymous) {
            emptySet()
        } else {
            reasoner.getEquivalentClasses(cex).entities().collect(Collectors.toSet())
        }
    }

    override fun getSubs(superCE: OWLObject, direct: Boolean): Set<OWLClass> {
        val cex: OWLClassExpression = superCE.asOWLClassExpression()
        return if (cex.isAnonymous) {
            emptySet()
        } else {
            reasoner.getSubClasses(cex, direct).entities().collect(Collectors.toSet())
        }
    }

    override fun getSupers(superCE: OWLObject?, direct: Boolean): Set<OWLClass> {
        val cex: OWLClassExpression = superCE.asOWLClassExpression()
        return if (cex.isAnonymous) {
            emptySet()
        } else {
            reasoner.getSuperClasses(cex, direct).entities().collect(Collectors.toSet())
        }
    }

    override fun getTops(): Set<OWLClass> {
        return setOf<OWLClass>(factory.owlThing)
    }

    override fun getBottoms(): Set<OWLClass> {
        return setOf<OWLClass>(factory.owlNothing)
    }

    override fun getDisjoints(disjointG: OWLObject?): Set<OWLClass> {
        val cex: OWLClassExpression = disjointG.asOWLClassExpression()
        return if (cex.isAnonymous) {
            emptySet()
        } else {
            reasoner.getDisjointClasses(cex).entities().collect(Collectors.toSet())
        }
    }

    override fun isEquiv(equivG1: OWLObject, equivG2: OWLObject): Boolean {
        return reasoner.isEntailed(
            factory
                .getOWLEquivalentClassesAxiom(
                    equivG1.asOWLClassExpression(),
                    equivG2.asOWLClassExpression()
                )
        )
    }

    override fun isSub(subG1: OWLObject, superG2: OWLObject, direct: Boolean): Boolean {
        return getSubs(superG2, direct).contains(subG1)
    }

    override fun isDisjointWith(disjointG1: OWLObject, disjointG2: OWLObject): Boolean {
        return reasoner.isEntailed(
            factory.getOWLDisjointClassesAxiom(
                disjointG1.asOWLClassExpression(),
                disjointG2.asOWLClassExpression()
            )
        )
    }

    override fun getComplements(complementG: OWLObject?): Set<OWLClass> {
        return reasoner
            .getEquivalentClasses(
                factory.getOWLObjectComplementOf(complementG.asOWLClassExpression())
            ).entities().collect(
                Collectors.toSet()
            )
    }

    override fun isComplementWith(
        complementG1: OWLObject?,
        complementG2: OWLObject?
    ): Boolean {
        return reasoner
            .isEntailed(
                factory.getOWLEquivalentClassesAxiom(
                    factory.getOWLObjectComplementOf(complementG1.asOWLClassExpression()),
                    complementG2.asOWLClassExpression()
                )
            )
    }
}