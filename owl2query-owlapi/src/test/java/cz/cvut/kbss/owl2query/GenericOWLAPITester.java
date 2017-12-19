package cz.cvut.kbss.owl2query;

import java.net.URI;
import java.util.Map;

public interface GenericOWLAPITester {

	public interface ReasonerPlugin {
		public void loadOntology(final Map<URI, URI> mapping,
				final String... ontologyURIs);

		public void loadQuery(final String queryURI);

		public String getAbbr();

		public long exec();
	}
	
	public long run(ReasonerPlugin rp, String queryFileURI, String mappingFile,
			int runs, String... ontoURIs);
}
