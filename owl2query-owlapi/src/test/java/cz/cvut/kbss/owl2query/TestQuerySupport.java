package cz.cvut.kbss.owl2query;

import cz.cvut.kbss.owl2query.engine.OWL2QueryEngine;
import cz.cvut.kbss.owl2query.model.OWL2Query;
import cz.cvut.kbss.owl2query.model.QueryResult;
import cz.cvut.kbss.owl2query.model.Variable;
import cz.cvut.kbss.owl2query.model.owlapi.OWLAPIv3OWL2Ontology;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.net.URI;

public class TestQuerySupport {

	final String BASE_URI = "http://krizik.felk.cvut.cz/";
    private OWLReasonerFactory factory;

    private OWLClass c1;
    private OWLObjectProperty p1;
    private Variable<OWLObject> varX;
    private Variable<OWLObject> varY;
    private Variable<OWLObject> varZ;

    private OWLAPIv3OWL2Ontology ont;

    {
        try {
            factory = (OWLReasonerFactory) Class.forName("openllet.owlapi.OpenlletReasonerFactory").newInstance();
        } catch (ClassNotFoundException e) {
            factory = null;
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setUp() {
        final OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology o;
        try {
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
            varZ = ont.getFactory().variable("z");

        } catch (OWLOntologyCreationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OWLOntologyChangeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
	public void testQueryTy() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addResultVar(varX).addResultVar(varY).Type(varY, varX);
        runQuery(q,3);
    }

    @Test
	public void testQueryPV() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addResultVar(varX).addResultVar(varY).addResultVar(varZ).PropertyValue(varX, varY, varZ);
        runQuery(q,5);
    }

    @Test
    public void testQuerySA() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addResultVar(varX).addResultVar(varY).SameAs(varX, varY);
        runQuery(q,2);
    }

    @Test
    public void testQueryDF() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addResultVar(varX).addResultVar(varY).DifferentFrom(varX, varY);
        runQuery(q,0);
    }


    @Test
    public void testQueryFun() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addResultVar(varX).Functional(varX);
        runQuery(q,2);    // <bottomData> , <bottomObject>
    }


    @Test
    public void testQueryIFun() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addResultVar(varX).InverseFunctional(varX);
        runQuery(q,1);   // <bottomObject>
    }


    @Test
    public void testQueryTrans() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addResultVar(varX).Transitive(varX);
        runQuery(q,2);   // <bottomObject> ; <topObject>
    }

    @Test
    public void testQueryRef() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addResultVar(varX).Reflexive(varX);
        runQuery(q,1);  // <topObject>
    }

    @Test
    public void testQueryIRef() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addResultVar(varX).Irreflexive(varX);
        runQuery(q,1);  // <bottomObject>
    }


    @Test
    public void testQuerySym() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addResultVar(varX).Symmetric(varX);
        runQuery(q,2);  // <bottomObject> ; <topObject>
    }


    @Test
    public void testQueryASym() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addResultVar(varX).Asymmetric(varX);
        runQuery(q,1);  // <bottomObject>
    }

    @Test
    public void testQuerySC() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addResultVar(varX).addResultVar(varY).SubClassOf(varX,varY);
        runQuery(q,4);
    }


    @Test
    public void testQueryEC() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addResultVar(varX).addResultVar(varY).EquivalentClass(varX,varY);
        runQuery(q,3);
    }

    @Test
    public void testQueryDW() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addResultVar(varX).addResultVar(varY).DisjointWith(varX,varY);
        runQuery(q,5);
    }


    @Test
    public void testQueryCO() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addResultVar(varX).addResultVar(varY).ComplementOf(varX,varY);
        runQuery(q,2);
    }


    @Test
    public void testQueryOP() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addResultVar(varX).ObjectProperty(varX);
        runQuery(q,3);
    }


    @Test
    public void testQueryDP() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addResultVar(varX).DatatypeProperty(varX);
        runQuery(q,2);
    }

    @Test
    public void testQuerySPO() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addResultVar(varX).addResultVar(varY).SubPropertyOf(varX,varY);
        runQuery(q,9);
    }

    @Test
    public void testQueryEP() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addResultVar(varX).addResultVar(varY).EquivalentProperty(varX,varY);
        runQuery(q,5);
    }

    @Test
    public void testQueryIO() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addResultVar(varX).addResultVar(varY).InverseOf(varX,varY);
        runQuery(q,3); // <bottomObject,bottomObject> ; <topObject,topObject> ; <p,inv(p)>
    }


    @Test
    public void testQueryDSPO() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addResultVar(varX).addResultVar(varY).DirectSubPropertyOf(varX,varY);
        runQuery(q,3); // <bottomData,topData> ; <bottomObject,p> ; <p,topObject>
    }

    @Test
    public void testQueryDSCO() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addResultVar(varX).addResultVar(varY).DirectSubClassOf(varX,varY);
        runQuery(q,2); // <nothing,c> ; <c,thing>
    }

    @Test
    public void testQuerySSPO() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addResultVar(varY).addResultVar(varY).StrictSubPropertyOf(varX,varY);
        runQuery(q,4);    // <bottomData,topData> ; <bottomObject,topObject> ; <bottomObject,p> ; <p,topObject>
    }

    @Test
    public void testQuerySSCO() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.addResultVar(varX).addResultVar(varY).StrictSubClassOf(varX,varY);
        runQuery(q,3);    // <nothing,thing> ; <nothing,c> ; <c,thing>
    }

    private void runQuery(final OWL2Query q, int size) {
        final QueryResult<OWLObject> qr = OWL2QueryEngine.exec(q);
        System.out.println(qr);

        Assert.assertEquals(size,qr.size());
    }

//	public void test1Sparql() {
//		final KnowledgeBase kb = new KnowledgeBase();
//
//		final ATermAppl i1 = ATermUtils.makeTermAppl(BASE_URI + "i1");
//		final ATermAppl c1 = ATermUtils.makeTermAppl(BASE_URI + "c1");
//
//		kb.addIndividual(i1);
//		kb.addClass(c1);
//		kb.addType(i1, c1);
//
//		final PelletOWL2Ontology o = new PelletOWL2Ontology(kb);
//		final QueryResult<ATermAppl> qr = OWL2QueryEngine.exec(query1Sparql(),
//				o);
//
//		System.out.println(qr);
//	}
//
//	public void test2Sparql() {
//		final OWLOntologyManager m = OWLManager.createOWLOntologyManager();
//		try {
//			OWLOntology o = m.createOntology(IRI.create(URI.create(BASE_URI)));
//
//			final OWLNamedIndividual i1 = m.getOWLDataFactory()
//					.getOWLNamedIndividual(IRI.create(BASE_URI + "i1"));
//			final OWLClass c1 = m.getOWLDataFactory().getOWLClass(
//					IRI.create(BASE_URI + "c1"));
//
//			m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
//					.getOWLDeclarationAxiom(c1)));
//
//			m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
//					.getOWLClassAssertionAxiom(c1, i1)));
//
//			final OWL2Ontology<OWLObject> ont = new OWLAPIv3OWL2Ontology(m,
//					o, new ReasonerFactory().createReasoner(o));
//			final QueryResult<OWLObject> qr = OWL2QueryEngine.exec(
//					query1Sparql(), ont);
//
//			System.out.println(qr);
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//	}
}
