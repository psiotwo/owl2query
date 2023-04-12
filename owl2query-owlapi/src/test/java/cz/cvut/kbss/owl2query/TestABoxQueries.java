package cz.cvut.kbss.owl2query;

import cz.cvut.kbss.owl2query.engine.OWL2QueryEngine;
import cz.cvut.kbss.owl2query.model.OWL2Query;
import cz.cvut.kbss.owl2query.model.QueryResult;
import cz.cvut.kbss.owl2query.model.Variable;
import cz.cvut.kbss.owl2query.model.owlapi.OWLAPIv3OWL2Ontology;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.net.URI;

public class TestABoxQueries {

    private static OWLReasonerFactory factory;

    private OWLClass c1;
    private OWLObjectProperty p1;
    private Variable<OWLObject> varX;
    private Variable<OWLObject> varY;

    private OWLAPIv3OWL2Ontology ont;

    @BeforeAll
    public static void init() {
        factory = TestConfiguration.get(TestConfiguration.PELLET).getFactory();
    }

    @BeforeEach
    public void setUp() {
        final OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology o;
        try {
            String BASE_URI = "http://krizik.felk.cvut.cz/";
            o = m.createOntology(IRI.create(URI.create(BASE_URI)));

            final OWLIndividual i1 = m.getOWLDataFactory()
                    .getOWLNamedIndividual(IRI.create(BASE_URI + "i1"));
            final OWLIndividual i2 = m.getOWLDataFactory()
                    .getOWLNamedIndividual(IRI.create(BASE_URI + "i2"));
            c1 = m.getOWLDataFactory().getOWLClass(
                    IRI.create(BASE_URI + "c1"));
            p1 = m.getOWLDataFactory().getOWLObjectProperty(
                    IRI.create(BASE_URI + "p1"));

            m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
                    .getOWLClassAssertionAxiom(c1, i1)));

            m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
                    .getOWLObjectPropertyAssertionAxiom(p1, i1, i2)));

            ont = new OWLAPIv3OWL2Ontology(m,
                    o, factory.createReasoner(o));

            varX = ont.getFactory().variable("x");
            varY = ont.getFactory().variable("y");

        } catch (OWLOntologyCreationException | OWLOntologyChangeException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testQueryType() {
        final Variable<OWLObject> varX = ont.getFactory().variable("x");

        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.Type(ont.getFactory().wrap(c1), varX);
        q.addDistVar(varX, true);

        runQuery(q, 1);
    }

    @Test
    public void testQueryPropertyValue() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.PropertyValue(ont.getFactory().wrap(p1), varX, varY);
        q.addDistVar(varX, true);
        q.addDistVar(varY, true);

        runQuery(q, 1);
    }

    @Test
    public void testQueryTypeAndPropertyValue() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.Type(ont.getFactory().wrap(c1), varX);
        q.PropertyValue(ont.getFactory().wrap(p1), varX, varY);
        q.addDistVar(varX, true);
        q.addDistVar(varY, true);

        runQuery(q, 1);
    }

    @Test
    public void testQueryTypeAndPropertyValue2() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.Type(ont.getFactory().wrap(c1), varY);
        q.PropertyValue(ont.getFactory().wrap(p1), varX, varY);
        q.addDistVar(varX, true);
        q.addDistVar(varY, true);

        runQuery(q, 0);
    }

    private void runQuery(final OWL2Query<OWLObject> q, int size) {
        final QueryResult<OWLObject> qr = OWL2QueryEngine.exec(q);
        System.out.println(qr);

        assertEquals(qr.size(), size);
    }
}
