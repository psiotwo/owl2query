import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class Pellet210Test {

	public static void main(String[] args) {
		try {
			OWLOntology o = OWLManager
					.createOWLOntologyManager()
					.loadOntologyFromOntologyDocument(
							new File(
									"Ontology1270637993352.owl"));

			OWLReasoner r = new PelletReasonerFactory().createReasoner(o);
			OWLDataProperty dp = o.getDataPropertiesInSignature().iterator()
					.next();
			r.isEntailed(OWLManager.getOWLDataFactory()
					.getOWLDataPropertyRangeAxiom(
							dp,
							OWLManager.getOWLDataFactory().getOWLDatatype(
									OWL2Datatype.XSD_DATE_TIME.getIRI())));
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}
}
