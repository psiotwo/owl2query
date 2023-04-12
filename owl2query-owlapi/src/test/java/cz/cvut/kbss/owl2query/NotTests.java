package cz.cvut.kbss.owl2query;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

import cz.cvut.kbss.owl2query.model.*;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import cz.cvut.kbss.owl2query.engine.OWL2QueryEngine;
import cz.cvut.kbss.owl2query.model.owlapi.OWLAPIv3OWL2Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotTests {

	private static final Logger LOG = LoggerFactory.getLogger(NotTests.class);

	final String BASE_URI = "http://krizik.felk.cvut.cz/";

	private final TestConfiguration f = TestConfiguration.FACTORY;

	/**
	 * SPO(?x,isMemberOf),NOT(SPO(?x,?y),SPO(?y,isMemberOf))
	 */
	@Test
	public void testExample1() {
		LOG.info(" ===========  " + getClass().getName() + " : testExample1");
		final OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology o;
		try {
			o = m.createOntology(IRI.create(URI.create(BASE_URI)));

			// data
			final OWLObjectProperty pIsMemberOf = m.getOWLDataFactory()
					.getOWLObjectProperty(IRI.create(BASE_URI + "isMemberOf"));
			final OWLObjectProperty pWorksFor = m.getOWLDataFactory()
					.getOWLObjectProperty(IRI.create(BASE_URI + "worksFor"));
			final OWLObjectProperty pIsStudentOf = m.getOWLDataFactory()
					.getOWLObjectProperty(IRI.create(BASE_URI + "isStudentOf"));
			final OWLObjectProperty pIsHeadOf = m.getOWLDataFactory()
					.getOWLObjectProperty(IRI.create(BASE_URI + "isHeadOf"));

			m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
					.getOWLSubObjectPropertyOfAxiom(pWorksFor, pIsMemberOf)));
			m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
					.getOWLSubObjectPropertyOfAxiom(pIsHeadOf, pWorksFor)));
			m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
					.getOWLSubObjectPropertyOfAxiom(pIsStudentOf, pIsMemberOf)));

			final OWLAPIv3OWL2Ontology ont = new OWLAPIv3OWL2Ontology(m, o,
					f.getFactory().createReasoner(o,f.getConfiguration()));

			// query
			final OWL2QueryFactory<OWLObject> f = ont.getFactory();

			final Variable<OWLObject> vX = f.variable("x");
			final Variable<OWLObject> vY = f.variable("y");
			
			final OWL2Query<OWLObject> queryNot = f.createQuery(ont)
					.SubPropertyOf(vX, vY).SubPropertyOf(vY, f.wrap(pIsMemberOf))
					.addDistVar(vX,true).addDistVar(vY,true);


			final Term<OWLObject> top = f.wrap(m.getOWLDataFactory().getOWLTopObjectProperty());
			final Term<OWLObject> bottom = f.wrap(m.getOWLDataFactory().getOWLBottomObjectProperty());

			final OWL2Query<OWLObject> q = f.createQuery(ont)
					.StrictSubPropertyOf(vX, top).StrictSubPropertyOf(bottom,vX)
					.StrictSubPropertyOf(vY, top).StrictSubPropertyOf(bottom,vY)
					.SubPropertyOf(vX, f.wrap(pIsMemberOf)).Not(queryNot)
					.addDistVar(vX,true).addDistVar(vY,true);

			// evaluation
			final QueryResult<OWLObject> qr = OWL2QueryEngine.exec(q);

			LOG.info(qr.toString());
			assertEquals(8, qr.size());
		} catch (OWLOntologyCreationException | OWLOntologyChangeException e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * reflexive strict sub properties
	 * 
	 * SPO(?x,isMemberOf),NOT(EQ(?x,isMemberOf))
	 */
	@Test
	public void testExample2() {
		LOG.info(" ===========  " + getClass().getName() + " : testExample2");
		final OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology o;
		try {
			o = m.createOntology(IRI.create(URI.create(BASE_URI)));

			// data
			final OWLObjectProperty pIsMemberOf = m.getOWLDataFactory()
					.getOWLObjectProperty(IRI.create(BASE_URI + "isMemberOf"));
			final OWLObjectProperty pWorksFor = m.getOWLDataFactory()
					.getOWLObjectProperty(IRI.create(BASE_URI + "worksFor"));
			final OWLObjectProperty pIsStudentOf = m.getOWLDataFactory()
					.getOWLObjectProperty(IRI.create(BASE_URI + "isStudentOf"));
			final OWLObjectProperty pIsHeadOf = m.getOWLDataFactory()
					.getOWLObjectProperty(IRI.create(BASE_URI + "isHeadOf"));

			m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
					.getOWLAsymmetricObjectPropertyAxiom(pWorksFor)));
			m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
					.getOWLSubObjectPropertyOfAxiom(pWorksFor, pIsMemberOf)));
			m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
					.getOWLSubObjectPropertyOfAxiom(pIsHeadOf, pWorksFor)));
			m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
					.getOWLSubObjectPropertyOfAxiom(pIsStudentOf, pIsMemberOf)));

			final OWLAPIv3OWL2Ontology ont = new OWLAPIv3OWL2Ontology(m, o,
					f.getFactory().createReasoner(o,f.getConfiguration()));

			// query
			final OWL2QueryFactory<OWLObject> f = ont.getFactory();

			final Variable<OWLObject> vX = f.variable("x");

			final Term<OWLObject> top = f.wrap(m.getOWLDataFactory().getOWLTopObjectProperty());
			final Term<OWLObject> bottom = f.wrap(m.getOWLDataFactory().getOWLBottomObjectProperty());

			final OWL2Query<OWLObject> queryNot = f.createQuery(ont)
					.EquivalentProperty(vX, f.wrap(pIsMemberOf));
			queryNot.addDistVar(vX, true);
			final OWL2Query<OWLObject> q = f.createQuery(ont)
					.StrictSubPropertyOf(vX, top).StrictSubPropertyOf(bottom,vX)
					.SubPropertyOf(vX, f.wrap(pIsMemberOf)).Asymmetric(vX).Not(queryNot);
			q.addDistVar(vX, true);

			// evaluation
			final QueryResult<OWLObject> qr = OWL2QueryEngine.exec(q);

			LOG.info("RESULT: " + qr);
			assertEquals(2, qr.size());
		} catch (OWLOntologyCreationException | OWLOntologyChangeException e) {
			e.printStackTrace();
			fail();
		}
	}
		
	
	//
	// public void test2() {
	// final OWLOntologyManager m = OWLManager.createOWLOntologyManager();
	// OWLOntology o;
	// try {
	// o = m.createOntology(IRI.create(URI.create(BASE_URI)));
	//
	// // data
	// final OWLIndividual i1 = m.getOWLDataFactory()
	// .getOWLNamedIndividual(IRI.create(BASE_URI + "i1"));
	// final OWLIndividual i2 = m.getOWLDataFactory()
	// .getOWLNamedIndividual(IRI.create(BASE_URI + "i2"));
	// final OWLClass c1 = m.getOWLDataFactory().getOWLClass(
	// IRI.create(BASE_URI + "c1"));
	// final OWLObjectProperty p1 = m.getOWLDataFactory()
	// .getOWLObjectProperty(IRI.create(BASE_URI + "p1"));
	//
	// m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
	// .getOWLClassAssertionAxiom(c1, i1)));
	// m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
	// .getOWLObjectPropertyAssertionAxiom(p1, i1, i2)));
	//
	// final OWLAPIv3OWL2Ontology ont = new OWLAPIv3OWL2Ontology(m, o,
	// f.createReasoner(o));
	//
	// // query
	// final OWL2QueryFactory<OWLObject> f = ont.getFactory();
	//
	// final Variable<OWLObject> vX = f.variable("x");
	// final OWL2Query<OWLObject> q = f
	// .createQuery(ont)
	// .Type(f.wrap(c1), vX)
	// .Not(f.createQuery(ont).PropertyValue(f.wrap(p1),
	// f.wrap(i1), f.wrap(i2)));
	// q.addDistVar(vX);
	// q.addResultVar(vX);
	//
	// // evaluation
	// final QueryResult<OWLObject> qr = OWL2QueryEngine.exec(q);
	//
	// LOG.info(qr);
	// assertEquals(0, qr.size());
	// } catch (OWLOntologyCreationException e) {
	// e.printStackTrace();
	// fail();
	// } catch (OWLOntologyChangeException e) {
	// e.printStackTrace();
	// fail();
	// }
	// }
}
