package cz.cvut.kbss.owl2query;

import cz.cvut.kbss.owl2query.engine.OWL2QueryEngine;
import cz.cvut.kbss.owl2query.model.OWL2Query;
import cz.cvut.kbss.owl2query.model.QueryResult;
import cz.cvut.kbss.owl2query.model.Variable;
import cz.cvut.kbss.owl2query.model.owlapi.OWLAPIv3OWL2Ontology;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestQuerySupport {

	final String BASE_URI = "http://krizik.felk.cvut.cz/";
    private final OWLReasonerFactory factory = TestConfiguration.get(TestConfiguration.PELLET).getFactory();

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

            final OWLIndividual i1 = m.getOWLDataFactory()
                    .getOWLNamedIndividual(IRI.create(BASE_URI + "i1"));
            final OWLIndividual i2 = m.getOWLDataFactory()
                    .getOWLNamedIndividual(IRI.create(BASE_URI + "i2"));
            OWLClass c1 = m.getOWLDataFactory().getOWLClass(
                    IRI.create(BASE_URI + "c1"));
            OWLObjectProperty p1 = m.getOWLDataFactory().getOWLObjectProperty(
                    IRI.create(BASE_URI + "p1"));

            m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
                    .getOWLClassAssertionAxiom(c1, i1)));

            m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
                    .getOWLObjectPropertyAssertionAxiom(p1, i1, i2)));

            ont = new OWLAPIv3OWL2Ontology(m,
                    o, factory.createReasoner(o));

            varX = ont.getFactory().variable("x");
            varY = ont.getFactory().variable("y");
            varZ = ont.getFactory().variable("z");

        } catch (OWLOntologyCreationException | OWLOntologyChangeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
	public void testQueryTy() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX, true).addDistVar(varY, true).Type(varY, varX);
        runQuery(q,3);
    }

    @Test
	public void testQueryPV() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX, true).addDistVar(varY, true).addDistVar(varZ, true).PropertyValue(varX, varY, varZ);
        runQuery(q,5);
    }

    @Test
    public void testQuerySA() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX,true).addDistVar(varY,true).SameAs(varX, varY);
        runQuery(q,2);
    }

    @Test
    public void testQueryDF() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX,true).addDistVar(varY,true).DifferentFrom(varX, varY);
        runQuery(q,0);
    }


    @Test
    public void testQueryFun() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX,true).Functional(varX);
        runQuery(q,2);    // <bottomData> , <bottomObject>
    }


    @Test
    public void testQueryIFun() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX,true).InverseFunctional(varX);
        runQuery(q,1);   // <bottomObject>
    }


    @Test
    public void testQueryTrans() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX,true).Transitive(varX);
        runQuery(q,2);   // <bottomObject> ; <topObject>
    }

    @Test
    public void testQueryRef() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX,true).Reflexive(varX);
        runQuery(q,1);  // <topObject>
    }

    @Test
    public void testQueryIRef() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX,true).Irreflexive(varX);
        runQuery(q,1);  // <bottomObject>
    }


    @Test
    public void testQuerySym() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX,true).Symmetric(varX);
        runQuery(q,2);  // <bottomObject> ; <topObject>
    }


    @Test
    public void testQueryASym() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX,true).Asymmetric(varX);
        runQuery(q,1);  // <bottomObject>
    }

    @Test
    public void testQuerySC() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX,true).addDistVar(varY,true).SubClassOf(varX,varY);
        runQuery(q,4);
    }


    @Test
    public void testQueryEC() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX,true).addDistVar(varY,true).EquivalentClass(varX,varY);
        runQuery(q,3);
    }

    @Test
    public void testQueryDW() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX,true).addDistVar(varY,true).DisjointWith(varX,varY);
        runQuery(q,5);
    }


    @Test
    public void testQueryCO() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX,true).addDistVar(varY,true).ComplementOf(varX,varY);
        runQuery(q,2);
    }


    @Test
    public void testQueryOP() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX,true).ObjectProperty(varX);
        runQuery(q,3);
    }


    @Test
    public void testQueryDP() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX,true).DatatypeProperty(varX);
        runQuery(q,2);
    }

    @Test
    public void testQuerySPO() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX,true).addDistVar(varY,true).SubPropertyOf(varX,varY);
        runQuery(q,9);
    }

    @Test
    public void testQueryEP() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX,true).addDistVar(varY,true).EquivalentProperty(varX,varY);
        runQuery(q,5);
    }

    @Test
    public void testQueryIO() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX,true).addDistVar(varY,true).InverseOf(varX,varY);
        runQuery(q,3); // <bottomObject,bottomObject> ; <topObject,topObject> ; <p,inv(p)>
    }


    @Test
    public void testQueryDSPO() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX,true).addDistVar(varY,true).DirectSubPropertyOf(varX,varY);
        runQuery(q,3); // <bottomData,topData> ; <bottomObject,p> ; <p,topObject>
    }

    @Test
    public void testQueryDSCO() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX,true).addDistVar(varY,true).DirectSubClassOf(varX,varY);
        runQuery(q,2); // <nothing,c> ; <c,thing>
    }

    @Test
    public void testQuerySSPO() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varY,true).addDistVar(varY,true).StrictSubPropertyOf(varX,varY);
        runQuery(q,4);    // <bottomData,topData> ; <bottomObject,topObject> ; <bottomObject,p> ; <p,topObject>
    }

    @Test
    public void testQuerySSCO() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addDistVar(varX,true).addDistVar(varY,true).StrictSubClassOf(varX,varY);
        runQuery(q,3);    // <nothing,thing> ; <nothing,c> ; <c,thing>
    }

    private void runQuery(final OWL2Query<OWLObject> q, int size) {
        final QueryResult<OWLObject> qr = OWL2QueryEngine.exec(q);
        System.out.println(qr);

        assertEquals(size,qr.size());
    }
}
