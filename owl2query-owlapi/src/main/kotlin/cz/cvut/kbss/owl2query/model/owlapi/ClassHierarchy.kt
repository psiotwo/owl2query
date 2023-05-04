package cz.cvut.kbss.owl2query.model.owlapi

import cz.cvut.kbss.owl2query.model.Hierarchy
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLClassExpression
import org.semanticweb.owlapi.model.OWLDataFactory
import org.semanticweb.owlapi.model.OWLObject
import org.semanticweb.owlapi.reasoner.OWLReasoner
import java.util.stream.Collectors

class ClassHierarchy(private val reasoner: OWLReasoner) : Hierarchy<OWLObject, OWLClass> {

    private val factory: OWLDataFactory = OWLManager.getOWLDataFactory()

    override fun getEquivs(ce: OWLObject): Set<OWLClass> =
        reasoner.getEquivalentClasses(ce.asOWLClassExpression()).entities().collect(Collectors.toSet())

    override fun getSubs(superCE: OWLObject, direct: Boolean): Set<OWLClass> {
        val set: MutableSet<OWLClass> =
            reasoner.getSubClasses(superCE.asOWLClassExpression(), direct).entities().collect(Collectors.toSet())
        if (!direct) {
            set.add(factory.owlNothing)
        }
        return set
    }

    override fun getSupers(superCE: OWLObject, direct: Boolean): Set<OWLClass> {
        val cex: OWLClassExpression = superCE.asOWLClassExpression()
        val set: MutableSet<OWLClass> = reasoner.getSuperClasses(cex, direct).entities().collect(Collectors.toSet())
        if (!direct) {
            set.add(factory.owlThing)
        }
        return set
    }

    override fun isEquiv(equivG1: OWLObject, equivG2: OWLObject): Boolean =
        reasoner.isEntailed(
            factory.getOWLEquivalentClassesAxiom(
                equivG1.asOWLClassExpression(),
                equivG2.asOWLClassExpression()
            )
        )

    override fun isSub(subG1: OWLObject, superG2: OWLObject, direct: Boolean): Boolean =
        reasoner.isEntailed(
            factory.getOWLSubClassOfAxiom(
                subG1.asOWLClassExpression(), superG2.asOWLClassExpression()
            )
        )
    // return getSubs(superG2,direct).contains(subG1);

    override fun isDisjointWith(disjointG1: OWLObject, disjointG2: OWLObject): Boolean =
        reasoner.isEntailed(
            factory.getOWLDisjointClassesAxiom(
                disjointG1.asOWLClassExpression(),
                disjointG2.asOWLClassExpression()
            )
        )

    override fun getComplements(complementG: OWLObject): Set<OWLClass> =
        reasoner.getEquivalentClasses(factory.getOWLObjectComplementOf(complementG.asOWLClassExpression())).entities()
            .collect(Collectors.toSet())

    override fun isComplementWith(complementG1: OWLObject, complementG2: OWLObject): Boolean =
        reasoner.isEntailed(
            factory.getOWLEquivalentClassesAxiom(
                factory.getOWLObjectComplementOf(complementG1.asOWLClassExpression()),
                complementG2.asOWLClassExpression()
            )
        )

    override fun getTops(): Set<OWLClass> = setOf<OWLClass>(factory.owlThing)

    override fun getBottoms(): Set<OWLClass> = setOf<OWLClass>(factory.owlNothing)

    override fun getDisjoints(disjointG: OWLObject): Set<OWLClass> =
        reasoner.getDisjointClasses(disjointG.asOWLClassExpression()).entities()
            .collect(Collectors.toSet())
}
