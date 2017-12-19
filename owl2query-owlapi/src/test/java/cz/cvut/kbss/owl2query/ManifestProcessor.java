package cz.cvut.kbss.owl2query;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

public class ManifestProcessor {
	public static void main(String[] args) {
		final Model mdl = ModelFactory.createDefaultModel();

		mdl
				.read(
						"file:///home/kremen/work/query/sparql-test-suite/test-manifest.n3",
						"N3");
		mdl
				.read(
						"file:///home/kremen/work/query/sparql-test-suite/test-dawg.n3",
						"N3");
		mdl
				.read(
						"file:///home/kremen/work/query/sparql-test-suite/test-query.n3",
						"N3");

		new ManifestProcessor()
				.loadManifest(
						mdl,
						"file:///home/kremen/work/query/sparql-test-suite/data-r2/manifest-syntax.ttl");

	}

	private void loadManifest(final Model mdlAll, final String manifest) {
		final String baseManifest = "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#";
		final Resource cManifest = mdlAll.getResource(baseManifest + "Manifest");
		final Property pInclude = mdlAll.getProperty(baseManifest + "include");

		final Model mdl = ModelFactory.createDefaultModel();
		mdl.read(manifest.toString(),"N3");
		
		final ResIterator i = mdl.listSubjectsWithProperty(RDF.type, cManifest);
		while (i.hasNext()) {
			final Resource r = i.nextResource();

			final Statement statement = r.getProperty(pInclude);
			
			if ( statement == null ) {
				continue;
			}
			
			final RDFList node = statement.getObject().as(RDFList.class);
			
			for (final RDFNode n : node.asJavaList()) {
				final Resource m = n.as(Resource.class);
				System.out.println("Loading " + m.getURI());
				loadManifest(mdl, m.getURI());
			}
		}
		
		mdlAll.add(mdl);
	}
	
}
