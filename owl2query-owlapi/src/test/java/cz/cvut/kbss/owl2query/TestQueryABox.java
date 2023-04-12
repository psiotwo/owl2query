package cz.cvut.kbss.owl2query;

import cz.cvut.kbss.owl2query.engine.OWL2QueryEngine;
import cz.cvut.kbss.owl2query.model.OWL2Query;
import cz.cvut.kbss.owl2query.model.QueryResult;
import cz.cvut.kbss.owl2query.model.Variable;
import cz.cvut.kbss.owl2query.model.owlapi.OWLAPIv3OWL2Ontology;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.net.URI;

public class TestQueryABox {

    final String BASE_URI = "http://krizik.felk.cvut.cz/";
    private final OWLReasonerFactory factory = TestConfiguration.get(TestConfiguration.PELLET).getFactory();

    private OWLClass c1;
    private OWLObjectProperty op1;

    private Variable<OWLObject> varX;
    private Variable<OWLObject> varY;
    private Variable<OWLObject> varZ;

    private OWLAPIv3OWL2Ontology ont;

    @BeforeEach
    public void setUp() {
        final OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology o;
        try {
            o = m.createOntology(IRI.create(URI.create(BASE_URI)));

            OWLIndividual i1 = m.getOWLDataFactory()
                    .getOWLNamedIndividual(IRI.create(BASE_URI + "i1"));
            OWLIndividual i2 = m.getOWLDataFactory()
                    .getOWLNamedIndividual(IRI.create(BASE_URI + "i2"));
            OWLLiteral l = m.getOWLDataFactory()
                    .getOWLLiteral("l1");
            c1 = m.getOWLDataFactory().getOWLClass(
                    IRI.create(BASE_URI + "c1"));
            op1 = m.getOWLDataFactory().getOWLObjectProperty(
                    IRI.create(BASE_URI + "op1"));
            OWLDataProperty dp1 = m.getOWLDataFactory().getOWLDataProperty(
                    IRI.create(BASE_URI + "dp1"));

            m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
                    .getOWLClassAssertionAxiom(c1, i1)));

            m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
                    .getOWLObjectPropertyAssertionAxiom(op1, i1, i2)));

            m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
                    .getOWLDataPropertyAssertionAxiom(dp1, i1, l)));

            ont = new OWLAPIv3OWL2Ontology(m,
                    o, factory.createReasoner(o));

            varX = ont.getFactory().variable("x");
            varY = ont.getFactory().variable("y");
            varZ = ont.getFactory().variable("z");

        } catch (OWLOntologyCreationException | OWLOntologyChangeException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testQueryTyPV() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX, true).addDistVar(varY, true).Type(ont.getFactory().wrap(c1), varX).PropertyValue(varZ, varX, varY);
        runQuery(q, 5);    // <i1,i2,topObject>
    }

    @Test
    public void testQueryTyPVOP() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX, true).addDistVar(varY, true).addDistVar(varZ, true).Type(ont.getFactory().wrap(c1), varX).PropertyValue(varZ, varX, varY).Not(ont.getFactory().createQuery(ont).addDistVar(varZ, true).EquivalentProperty(varZ, ont.getFactory().wrap(ont.getFactory().getTopObjectProperty()))).Not(ont.getFactory().createQuery(ont).addDistVar(varZ, true).EquivalentProperty(varZ, ont.getFactory().wrap(ont.getFactory().getTopDataProperty())));
        runQuery(q, 2);   // <i1,i2,op1>  ;  <i1,i2,topObject>
    }

    @Test
    public void testQueryTyPVOPStrict() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX, true).addDistVar(varY, true).addDistVar(varZ, true).Type(ont.getFactory().wrap(c1), varX).PropertyValue(varZ, varX, varY).StrictSubPropertyOf(varZ, ont.getFactory().wrap(ont.getFactory().getTopObjectProperty()));
        runQuery(q, 1);    // <i1,i2,op1>
    }

    @Test
    public void testQueryTyPVOPGiven() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX, true).addDistVar(varY, true).Type(ont.getFactory().wrap(c1), varX).PropertyValue(ont.getFactory().wrap(op1), varX, varY);
        runQuery(q, 1);
    }

    //@Test
    public void testQueryTyPVWithTypeOfObject() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX, true).addDistVar(varY, true).addDistVar(varZ, true).PropertyValue(varZ, varX, varY).Type(ont.getFactory().wrap(c1), varY);
        runQuery(q, 3);    // <i2,i1,topObject> ; <i1,i1,topObject>
    }

    @Test
    public void testQueryTyPVWithTypeOfObject2() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        final OWL2Query<OWLObject> q2 = ont.getFactory().createQuery(ont).addDistVar(varY, true).Type(ont.getFactory().wrap(c1), varY);
        q.addDistVar(varX, true).addDistVar(varY, true).addDistVar(varZ, true).Type(ont.getFactory().wrap(c1), varX).PropertyValue(varZ, varX, varY).Not(q2);
        runQuery(q, 4);
    }

    private void runQuery(final OWL2Query<OWLObject> q, int size) {
        System.out.println("QUERY:  " + q);
        final QueryResult<OWLObject> qr = OWL2QueryEngine.exec(q);
        System.out.println("RESULT: " + qr);

        assertEquals(size, qr.size());
    }
}
