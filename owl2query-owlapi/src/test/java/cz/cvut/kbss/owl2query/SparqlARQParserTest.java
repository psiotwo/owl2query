package cz.cvut.kbss.owl2query;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import cz.cvut.kbss.owl2query.model.OWL2Query;
import cz.cvut.kbss.owl2query.model.OWL2Rule;
import cz.cvut.kbss.owl2query.model.owlapi.OWLAPIv3OWL2Ontology;
import cz.cvut.kbss.owl2query.parser.arq.SparqlARQParser;

public class SparqlARQParserTest {
	private SparqlARQParser<OWLObject> parser;
	private OWLAPIv3OWL2Ontology o;
	
	private static final String p="PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX owl:  <http://www.w3.org/2002/07/owl#> ";
	
	@Before
 	public void initEach() throws Exception {
	   	parser = new SparqlARQParser<OWLObject>();
	   	final OWLOntologyManager m = OWLManager.createOWLOntologyManager();
	   	final OWLOntology ont = m.createOntology();
	   	final OWLReasoner r = new StructuralReasonerFactory().createReasoner(ont);	   	
	   	o = new OWLAPIv3OWL2Ontology(m, ont, r);	    
	}
	
    @Test
    public void testParseSimpleSelect() throws Exception {
    	parser.parse(p + "SELECT ?x { ?x ?y ?z .}", o);
    }

    @Test
    public void testParseSimpleConstruct() throws Exception {
    	System.out.println(parser.parseConstruct(p + "CONSTRUCT {?z rdfs:subClassOf owl:Thing} WHERE { ?x a ?z .}", o));
    	
    }

    @Test
    public void testBindKeyword() throws Exception {
    	System.out.println(parser.parse(p + "SELECT * WHERE { ?x a ?z . BIND(?z as ?q). }", o));
    	
    }

}
