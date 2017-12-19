package cz.cvut.kbss.owl2query;

import java.net.URI;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import cz.cvut.kbss.owl2query.engine.OWL2QueryEngine;
import cz.cvut.kbss.owl2query.model.OWL2Ontology;
import cz.cvut.kbss.owl2query.model.OWL2Query;
import cz.cvut.kbss.owl2query.model.QueryResult;
import cz.cvut.kbss.owl2query.model.Variable;
import cz.cvut.kbss.owl2query.model.owlapi.OWLAPIv3OWL2Ontology;

public class GenericOWLAPIv3Simple extends TestCase {

	private static final Logger LOG = Logger
			.getLogger(TestCase.class.getName());

	final String BASE_URI = "http://krizik.felk.cvut.cz/";

	private final TestConfiguration f = TestConfiguration.FACTORY;

	public void test2() {
		final OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology o;
		try {
			o = m.createOntology(IRI.create(URI.create(BASE_URI)));

			// data
			final OWLIndividual i1 = m.getOWLDataFactory()
					.getOWLNamedIndividual(IRI.create(BASE_URI + "i1"));
			final OWLClass c1 = m.getOWLDataFactory().getOWLClass(
					IRI.create(BASE_URI + "c1"));

			m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
					.getOWLClassAssertionAxiom(c1, i1)));

			final OWLAPIv3OWL2Ontology ont = new OWLAPIv3OWL2Ontology(m, o, f
					.getFactory().createReasoner(o, f.getConfiguration()));

			// query
			final OWL2Query<OWLObject> q = ont.getFactory().createQuery(ont);
			final Variable<OWLObject> vX = ont.getFactory().variable("x");
			q.Type(ont.getFactory().wrap(c1), vX);
			q.addDistVar(vX);
			q.addResultVar(vX);

			// evaluation
			final QueryResult<OWLObject> qr = OWL2QueryEngine.exec(q);

			LOG.info(qr.toString());
			assertEquals(1, qr.size());
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			fail();
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
			fail();
		}
	}

	public void test2Sparql() {
		final OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		try {
			// data
			OWLOntology o = m.createOntology(IRI.create(URI.create(BASE_URI)));

			final OWLNamedIndividual i1 = m.getOWLDataFactory()
					.getOWLNamedIndividual(IRI.create(BASE_URI + "i1"));
			final OWLClass c1 = m.getOWLDataFactory().getOWLClass(
					IRI.create(BASE_URI + "c1"));

			m.applyChange(new AddAxiom(o, m.getOWLDataFactory()
					.getOWLClassAssertionAxiom(c1, i1)));

			final OWL2Ontology<OWLObject> ont = new OWLAPIv3OWL2Ontology(m, o,
					f.getFactory().createReasoner(o, f.getConfiguration()));

			// evaluation
			final QueryResult<OWLObject> qr = OWL2QueryEngine.exec(
					"SELECT ?x WHERE {?x a <" + BASE_URI + "c1> }", ont);

			LOG.info(qr.toString());

			assertEquals(1, qr.size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
