/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.kbss.owl2query;

import cz.cvut.kbss.owl2query.engine.OWL2QueryEngine;
import cz.cvut.kbss.owl2query.model.AllValuesFrom;
import cz.cvut.kbss.owl2query.model.OWL2Query;
import cz.cvut.kbss.owl2query.model.QueryResult;
import cz.cvut.kbss.owl2query.model.Term;
import cz.cvut.kbss.owl2query.model.Variable;
import cz.cvut.kbss.owl2query.model.owlapi.OWLAPIv3OWL2Ontology;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

/**
 *
 * @author user
 */
public class VariableInClassExpressionTest {

    @BeforeAll
    public static void initKBandReasoner() {
    }

    @AfterAll
    public static void closeKBandReasoner() {
    }
    final String BASE_URI = "http://krizik.felk.cvut.cz/";
    private final OWLReasonerFactory factory = TestConfiguration.get(TestConfiguration.PELLET).getFactory();
    private OWLIndividual i1;
    private OWLIndividual i3;
    private OWLClass c1;
    private OWLObjectProperty p1;
    private OWLObjectProperty p2;
    private Variable<OWLObject> varX;
    private Variable<OWLObject> varY;
    private OWLAPIv3OWL2Ontology ont;

    @BeforeEach
    public void setUp() throws OWLOntologyCreationException {
        final OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology o;
        try {
            o = m.createOntology(IRI.create(URI.create(BASE_URI)));

            i1 = m.getOWLDataFactory()
                    .getOWLNamedIndividual(IRI.create(BASE_URI + "i1"));
            OWLIndividual i2 = m.getOWLDataFactory()
                    .getOWLNamedIndividual(IRI.create(BASE_URI + "i2"));
            i3 = m.getOWLDataFactory()
                    .getOWLNamedIndividual(IRI.create(BASE_URI + "i3"));
            c1 = m.getOWLDataFactory().getOWLClass(
                    IRI.create(BASE_URI + "c1"));
            OWLClass c2 = m.getOWLDataFactory().getOWLClass(
                    IRI.create(BASE_URI + "c2"));
            OWLClass c3 = m.getOWLDataFactory().getOWLClass(
                    IRI.create(BASE_URI + "c3"));
            p1 = m.getOWLDataFactory().getOWLObjectProperty(
                    IRI.create(BASE_URI + "p1"));
            p2 = m.getOWLDataFactory().getOWLObjectProperty(
                    IRI.create(BASE_URI + "p2"));

            m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
                    .getOWLClassAssertionAxiom(c1, i1)));
            
            m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
                    .getOWLClassAssertionAxiom(c1, i3)));

            m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
                    .getOWLObjectPropertyAssertionAxiom(p1, i1, i2)));
            
            m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
                    .getOWLObjectPropertyAssertionAxiom(p1, i1, i3)));
            
            m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
                    .getOWLObjectPropertyAssertionAxiom(p2, i3, i3)));
            
            m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
                    .getOWLSubClassOfAxiom(c3, m.getOWLDataFactory().getOWLObjectHasSelf(p2))));
            
            m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
                    .getOWLObjectPropertyDomainAxiom(p1, c1)));
            
            m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
                    .getOWLEquivalentClassesAxiom(c1, m.getOWLDataFactory().getOWLObjectComplementOf(c2))));
            
            ont = new OWLAPIv3OWL2Ontology(m,
                    o, factory.createReasoner(o));

            varX = ont.getFactory().variable("x");
            varY = ont.getFactory().variable("y");
        } catch (OWLOntologyChangeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * This test fails. Because implementation of complex expressions is not finished.
     * TODO - The creation of the expressions should be moved from the parser to
     *        the model Factory OWL2QueryFactory.
     *      - It seams that complex expressions don't work also without variables.
     *        The query evaluation boils down to the method OWL2Ontology.isSub 
     *        implemented by the OWLAPIv3OWL2Ontology which only checks whether 
     *        the expression is part of the hierarchy in the case of the SCO atom.
     */
    @Test
//    @Ignore
    public void testAllValuesFromWithVariableInConceptPlace() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        final Term<OWLObject> tp1 = ont.getFactory().wrap(p1);
        AllValuesFrom<OWLObject> allValuesFromExpression = ont.getFactory().allValuesFrom(tp1, varX);
            
        Term<OWLObject> owlThing = ont.getFactory().wrap(ont.getFactory().getThing());
        q.addDistVar(varX, true).SubClassOf(allValuesFromExpression, owlThing);

        System.out.println("#####################################################");
        System.out.println(q);
        System.out.println("#####################################################");

        // I am not sure if the returned result should be 2 (Ting, c1). What about Nothing?
        runQuery(q, 5);
    }
    
    @Test
    public void testAllValuesFromWithVariableInConceptPlace2() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        final Term<OWLObject> ti1 = ont.getFactory().wrap(i1);
        final Term<OWLObject> tp1 = ont.getFactory().wrap(p1);
        
        AllValuesFrom<OWLObject> allValuesFromExpression = ont.getFactory().allValuesFrom(tp1, varX);
            
        q.addDistVar(varX, true).Type(allValuesFromExpression, ti1);

        System.out.println("#####################################################");
        System.out.println(q);
        System.out.println("#####################################################");

        // I am not sure if the returned result should be 2 (Ting, c1). What about Nothing?
        runQuery(q, 1);
        
    }
    
    @Test
    public void testSomeValuesFromExpressionGround() {
        
        final Term<OWLObject> tp1 = ont.getFactory().wrap(p1);
        final Term<OWLObject> tc1 = ont.getFactory().wrap(c1);
        final Term<OWLObject> owlThing = ont.getFactory().wrap(ont.getFactory().getThing());
        
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        Term<OWLObject> expression = ont.getFactory().someValuesFrom(tp1, tc1);
        q.addDistVar(varX, true).Type(expression, varX);
        System.out.println("#####################################################");
        System.out.println(q);
        System.out.println("#####################################################");
        runQuery(q, 1);
        
        final OWL2Query<OWLObject> qq = ont.getFactory().createQuery(ont);
        expression = ont.getFactory().someValuesFrom(tp1, owlThing);
        qq.addDistVar(varX, true).Type(expression, varX);
        System.out.println("#####################################################");
        System.out.println(qq);
        System.out.println("#####################################################");
        runQuery(qq, 1);
    }
    
    @Test
    public void testSomeValuesFromExpressionClassVar() {
        
        final Term<OWLObject> ti1 = ont.getFactory().wrap(i1);
        final Term<OWLObject> tp1 = ont.getFactory().wrap(p1);

        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        Term<OWLObject> expression = ont.getFactory().someValuesFrom(tp1, varY);
        q.addDistVar(varY, true).Type(expression, ti1);
        System.out.println("#####################################################");
        System.out.println(q);
        System.out.println("#####################################################");
        runQuery(q, 2);
        
    }
    
    @Test
    public void testSomeValuesFromExpressionPropVar() {
        
        final Term<OWLObject> ti1 = ont.getFactory().wrap(i1);
        final Term<OWLObject> tc1 = ont.getFactory().wrap(c1);
        final Term<OWLObject> owlThing = ont.getFactory().wrap(ont.getFactory().getThing());
        
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        Term<OWLObject> expression = ont.getFactory().someValuesFrom(varX, owlThing);
        q.addDistVar(varX, true).Type(expression, ti1);
        System.out.println("#####################################################");
        System.out.println(q);
        System.out.println("#####################################################");
        runQuery(q, 2);
        
        OWL2Query<OWLObject> qq = ont.getFactory().createQuery(ont);
        expression = ont.getFactory().someValuesFrom(varX, tc1);
        qq.addDistVar(varX, true).Type(expression, ti1);
        System.out.println("#####################################################");
        System.out.println(qq);
        System.out.println("#####################################################");
        runQuery(qq, 2);
    }
    
    @Test
    public void testObjectComplementOf() {
        
        final Term<OWLObject> ti1 = ont.getFactory().wrap(i1);

        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        Term<OWLObject> expression = ont.getFactory().objectComplementOf(varX);
        q.addDistVar(varX, true).Type(expression, ti1);
        System.out.println("#####################################################");
        System.out.println(q);
        System.out.println("#####################################################");
        runQuery(q, 2);
        
        OWL2Query<OWLObject> qq = ont.getFactory().createQuery(ont);
        expression = ont.getFactory().objectComplementOf(varX);
        qq.addDistVar(varX, true).addDistVar(varY, true).SubClassOf(varY, expression);
        System.out.println("#####################################################");
        System.out.println(qq);
        System.out.println("#####################################################");
        runQuery(qq, 11);
    }
    
    @Test
    public void testObjectHasSelf() {
        
        final Term<OWLObject> ti3 = ont.getFactory().wrap(i3);
        final Term<OWLObject> tp2 = ont.getFactory().wrap(p2);

        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        Term<OWLObject> expression = ont.getFactory().objectHasSelf(tp2);
        q.addDistVar(varX, true).Type(expression, varX);
        System.out.println("#####################################################");
        System.out.println(q);
        System.out.println("#####################################################");
        runQuery(q, 1);
        
        OWL2Query<OWLObject> qq = ont.getFactory().createQuery(ont);
        expression = ont.getFactory().objectHasSelf(varX);
        qq.addDistVar(varX, true).Type(expression, ti3);
        System.out.println("#####################################################");
        System.out.println(qq);
        System.out.println("#####################################################");
        runQuery(qq, 2);
    }


    private void runQuery(final OWL2Query<OWLObject> q, int size) {
        final QueryResult<OWLObject> qr = OWL2QueryEngine.exec(q);
        System.out.println("The query result is : ");
        System.out.println(qr);
        assertEquals(size, qr.size());
    }
}
