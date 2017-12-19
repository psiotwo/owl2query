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

public class ABoxQueries {

	final String BASE_URI = "http://krizik.felk.cvut.cz/";
    private OWLReasonerFactory factory;

    private OWLClass c1;
    private OWLObjectProperty p1;
    private Variable<OWLObject> varX;
    private Variable<OWLObject> varY;

    private OWLAPIv3OWL2Ontology ont;

    {
        try {
            factory = (OWLReasonerFactory) Class.forName("com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory").newInstance();
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

        } catch (OWLOntologyCreationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OWLOntologyChangeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
	public void testQueryType() {
        final Variable<OWLObject> varX = ont.getFactory().variable("x");

        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.Type(ont.getFactory().wrap(c1), varX);
        q.addDistVar(varX);
        q.addResultVar(varX);

        runQuery(q,1);
    }

    @Test
	public void testQueryPropertyValue() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.PropertyValue(ont.getFactory().wrap(p1), varX, varY);
        q.addDistVar(varX);
        q.addResultVar(varX);
        q.addDistVar(varY);
        q.addResultVar(varY);

        runQuery(q,1);
    }

    @Test
    public void testQueryTypeAndPropertyValue() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.Type(ont.getFactory().wrap(c1), varX);
        q.PropertyValue(ont.getFactory().wrap(p1), varX, varY);
        q.addDistVar(varX);
        q.addResultVar(varX);
        q.addDistVar(varY);
        q.addResultVar(varY);

        runQuery(q,1);
    }

    @Test
    public void testQueryTypeAndPropertyValue2() {
        final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
        q.Type(ont.getFactory().wrap(c1), varY);
        q.PropertyValue(ont.getFactory().wrap(p1), varX, varY);
        q.addDistVar(varX);
        q.addResultVar(varX);
        q.addDistVar(varY);
        q.addResultVar(varY);

        runQuery(q,0);
    }

    private void runQuery(final OWL2Query q, int size) {
        final QueryResult<OWLObject> qr = OWL2QueryEngine.exec(q);
        System.out.println(qr);

        Assert.assertEquals(qr.size(),size);
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
