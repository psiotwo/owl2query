package cz.cvut.kbss.owl2query.protege;

import org.semanticweb.owlapi.model.OWLObject;

import cz.cvut.kbss.owl2query.model.OWL2Rule;

public class SPARQLDLNOTRule {

	private OWL2Rule<OWLObject> rule;
	
	private String sparqlString;
	
	public OWL2Rule<OWLObject> getRule() {
		return rule;
	}
	
	public void setRule(OWL2Rule<OWLObject> rule) {
		this.rule = rule;
	}	
	
	public void setSparqlString(String sparqlString) {
		this.sparqlString = sparqlString;
	}
	
	public String getSparqlString() {
		return sparqlString;
	}	
}
