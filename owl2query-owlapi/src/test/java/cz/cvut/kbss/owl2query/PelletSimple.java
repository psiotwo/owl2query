package cz.cvut.kbss.owl2query;

import junit.framework.TestCase;

import org.mindswap.pellet.KnowledgeBase;
import org.mindswap.pellet.PelletOptions;
import org.mindswap.pellet.utils.ATermUtils;

import aterm.ATermAppl;
import cz.cvut.kbss.owl2query.engine.OWL2QueryEngine;
import cz.cvut.kbss.owl2query.model.OWL2Query;
import cz.cvut.kbss.owl2query.model.QueryResult;
import cz.cvut.kbss.owl2query.model.Variable;
//import cz.cvut.kbss.owl2query.model.pellet.PelletOWL2Ontology;

public class PelletSimple extends TestCase {

//	final String BASE_URI = "http://krizik.felk.cvut.cz/";
//
//	public void testPellet1() {
//		final KnowledgeBase kb = new KnowledgeBase();
//
//		PelletOptions.KEEP_ABOX_ASSERTIONS = true;
//		
//		final ATermAppl i1 = ATermUtils.makeTermAppl(BASE_URI + "i1");
//		final ATermAppl c1 = ATermUtils.makeTermAppl(BASE_URI + "c1");
//
//		kb.addIndividual(i1);
//		kb.addClass(c1);
//		kb.addType(i1, c1);
//
//		final PelletOWL2Ontology o = new PelletOWL2Ontology(kb);
//		final OWL2Query<ATermAppl> q = query1(o, c1);
//
//		final QueryResult<ATermAppl> qr = OWL2QueryEngine.exec(q);
//
//		System.out.println(qr);
//	}
//
//	private <T> OWL2Query<T> query1(
//			final cz.cvut.kbss.owl2query.model.OWL2Ontology<T> ont,
//			final T c1) {
//		final OWL2Query<T> q = ont.getFactory().createQuery(ont);
//		final Variable<T> varX = ont.getFactory().variable("x");
//		q.Type(ont.getFactory().wrap(c1), varX);
//		q.addDistVar(varX);
//		q.addResultVar(varX);
//		return q;
//	}
//
//	private String query1Sparql() {
//		return "SELECT ?x WHERE {?x a <" + BASE_URI + "c1> }";
//	}
//
//	// private String query2Sparql() {
//	// return "SELECT ?x WHERE {?x a <" + BASE_URI + "c1> . ?x <" + BASE_URI
//	// + "p> ?y}";
//	// }
//
//	public void testPellet2Sparql() {
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
}
