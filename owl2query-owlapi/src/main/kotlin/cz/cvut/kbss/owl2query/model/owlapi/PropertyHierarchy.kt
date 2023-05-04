package cz.cvut.kbss.owl2query.model.owlapi

import cz.cvut.kbss.owl2query.model.Hierarchy
import cz.cvut.kbss.owl2query.model.InternalReasonerException
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.reasoner.OWLReasoner
import java.util.*
import java.util.stream.Collectors
class PropertyHierarchy(val o: OWLOntology, val r: OWLReasoner): Hierarchy<OWLObject, OWLProperty> {

    private val factory : OWLDataFactory = OWLManager.getOWLDataFactory()
    override fun getEquivs(ce: OWLObject): Set<OWLProperty> {
        val cex = ce.asOWLPropertyExpression(o)
        return if (cex.isDataPropertyExpression) {
            r.getEquivalentDataProperties(cex as OWLDataProperty).entities().collect(Collectors.toSet())
        } else if (cex.isObjectPropertyExpression) {
            r.getEquivalentObjectProperties(cex as OWLObjectProperty).entities()
                .filter { ex: OWLObjectPropertyExpression -> !ex.isAnonymous }
                .map { obj: OWLObjectPropertyExpression -> obj.asOWLObjectProperty() }
                .collect(
                    Collectors.toSet()
                )
        } else {
            throw InternalReasonerException()
        }
    }

    override fun getSubs(superCE: OWLObject, direct: Boolean): Set<OWLProperty> {
        val cex = superCE.asOWLPropertyExpression(o)
        val set: MutableSet<OWLProperty> = HashSet()
        if (cex.isDataPropertyExpression) {
            set.addAll(
                r
                    .getSubDataProperties(cex as OWLDataProperty, direct).entities().collect(Collectors.toSet())
            )
            if (!direct) {
                set.add(factory.owlBottomDataProperty)
            }
        } else if (cex.isObjectPropertyExpression) {
            r.getSubObjectProperties(cex as OWLObjectProperty, direct).entities()
                .filter { ex: OWLObjectPropertyExpression -> !ex.isAnonymous }
                .forEach { ex: OWLObjectPropertyExpression ->
                    set.add(
                        ex.asOWLObjectProperty()
                    )
                }
            if (!direct) {
                set.add(factory.owlBottomObjectProperty)
            }
        } else {
            throw InternalReasonerException()
        }
        return set
    }

    override fun getSupers(superCE: OWLObject, direct: Boolean): Set<OWLProperty> {
        val cex = superCE.asOWLPropertyExpression(o)
        //
        // if (cex.equals(f.getOWLTopObjectProperty())) {
        // return Collections.emptySet();
        // } else if (cex.equals(f.getOWLBottomObjectProperty())) {
        // final Set<OWLProperty> set = new HashSet<OWLProperty>();
        // set.addAll(getObjectProperties());
        //
        // if (direct) {
        // for (OWLProperty op : new HashSet<OWLProperty>(set)) {
        // if (!getSubs(op, true).contains(
        // f.getOWLBottomObjectProperty())) {
        // set.remove(op);
        // }
        // }
        // } else {
        // set.add(f.getOWLTopObjectProperty());
        // }
        //
        // return set;
        // } else if (cex.equals(f.getOWLBottomDataProperty())) {
        // final Set<OWLProperty> set = new HashSet<OWLProperty>();
        // set.addAll(getDataProperties());
        //
        // if (direct) {
        // for (OWLProperty op : new HashSet<OWLProperty>(set)) {
        // if (!getSubs(op, true).contains(
        // f.getOWLBottomDataProperty())) {
        // set.remove(op);
        // }
        // }
        // } else {
        // set.add(f.getOWLTopDataProperty());
        // }
        // return set;
        // }
        val set: MutableSet<OWLProperty> = HashSet()
        if (cex.isDataPropertyExpression) {
            set.addAll(
                r.getSuperDataProperties(
                    cex as OWLDataProperty,
                    direct
                ).entities().collect(Collectors.toSet())
            )
            if (!direct) {
                set.add(factory.owlTopDataProperty)
            }
        } else if (cex.isObjectPropertyExpression) {
            r.getSuperObjectProperties(cex as OWLObjectPropertyExpression, direct).entities()
                .filter { ex: OWLObjectPropertyExpression -> !ex.isAnonymous }
                .forEach { ex: OWLObjectPropertyExpression ->
                    set.add(
                        ex.asOWLObjectProperty()
                    )
                }
            if (!direct) {
                set.add(factory.owlTopObjectProperty)
            }
        } else {
            throw InternalReasonerException()
        }
        return set
    }

    override fun getTops(): Set<OWLProperty> {
        return HashSet<OWLProperty>(
            Arrays.asList(
                factory.owlTopObjectProperty, factory.owlTopDataProperty
            )
        )
    }

    override fun getBottoms(): Set<OWLProperty> {
        return HashSet<OWLProperty>(
            Arrays.asList(
                factory.owlBottomObjectProperty,
                factory.owlBottomDataProperty
            )
        )
    }

    override fun getDisjoints(disjointG: OWLObject): Set<OWLProperty> {
        val cex = disjointG.asOWLPropertyExpression(o)
        return if (cex.isDataPropertyExpression) {
            r.getDisjointDataProperties(cex as OWLDataProperty).entities().collect(Collectors.toSet())
        } else if (cex.isObjectPropertyExpression) {
            r.getDisjointObjectProperties(cex as OWLObjectProperty).entities()
                .filter { ex: OWLObjectPropertyExpression -> !ex.isAnonymous }
                .map { obj: OWLObjectPropertyExpression -> obj.asOWLObjectProperty() }
                .collect(
                    Collectors.toSet()
                )
        } else {
            throw InternalReasonerException()
        }
    }

    override fun isEquiv(equivG1: OWLObject, equivG2: OWLObject): Boolean {
        val cex1 = equivG1.asOWLPropertyExpression(o)
        val cex2 = equivG2.asOWLPropertyExpression(o)
        return if (cex1.isDataPropertyExpression) {
            (cex2.isDataPropertyExpression
                    && r.isEntailed(
                factory.getOWLEquivalentDataPropertiesAxiom(
                    cex1 as OWLDataPropertyExpression,
                    cex2 as OWLDataPropertyExpression
                )
            ))
        } else {
            (cex2.isObjectPropertyExpression
                    && r.isEntailed(
                factory
                    .getOWLEquivalentObjectPropertiesAxiom(
                        cex1 as OWLObjectPropertyExpression,
                        cex2 as OWLObjectPropertyExpression
                    )
            ))
        }
    }

    override fun isSub(subG1: OWLObject, superG2: OWLObject, direct: Boolean): Boolean {
        return getSubs(superG2, direct).contains(subG1)
    }

    override fun isDisjointWith(disjointG1: OWLObject, disjointG2: OWLObject): Boolean {
        return getDisjoints(disjointG1).contains(disjointG2) // TODO
        // reasoner
        // directly
    }

    override fun getComplements(complementG: OWLObject): Set<OWLProperty> {
        throw UnsupportedOperationException("NOT supported yet.")
    }

    override fun isComplementWith(
        complementG1: OWLObject,
        complementG2: OWLObject
    ): Boolean {
        throw UnsupportedOperationException("NOT supported yet.")
    }
}