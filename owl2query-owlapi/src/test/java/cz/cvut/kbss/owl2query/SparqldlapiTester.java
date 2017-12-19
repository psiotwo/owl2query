package cz.cvut.kbss.owl2query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.OWLOntologyMerger;

import cz.cvut.kbss.owl2query.util.StatisticsUtils;

//import de.derivo.sparqldlapi.Query;
//import de.derivo.sparqldlapi.QueryArgument;
//import de.derivo.sparqldlapi.QueryBinding;
//import de.derivo.sparqldlapi.QueryEngine;
//import de.derivo.sparqldlapi.QueryResult;
//import de.derivo.sparqldlapi.exceptions.QueryEngineException;
//import de.derivo.sparqldlapi.exceptions.QueryParserException;
//import de.derivo.sparqldlapi.impl.QueryParserImpl;
//import de.derivo.sparqldlapi.impl.QueryTokenizerImpl;

public class SparqldlapiTester implements GenericOWLAPITester {

	static OWLOntologyManager m;
	static OWLOntology merged;

	static Map<OWLReasonerFactory, OWLOntology> map = new HashMap<OWLReasonerFactory, OWLOntology>();

//	public static ReasonerPlugin getGenericOWLAPIv3(final TestConfiguration f) {

//		return new ReasonerPlugin() {
//			public String getAbbr() {
//				return "OWLAPI-"
//						+ f.getFactory().getReasonerName().substring(0, 1);
//			}
//
//			private OWLOntology o;
//			private OWLReasoner r;
//
////			private Query q;
////
////			public long exec() {
////				try {
////					return QueryEngine.create(m, r).execute(q).size();
////				} catch (QueryEngineException e) {
////					e.printStackTrace();
////					return -1;
////				}
////			}
//
//			public void loadOntology(final Map<URI, URI> mapping,
//					String... ontologyURIs) {
//				if (m == null) {
//					m = OWLManager.createOWLOntologyManager();
//					m.addIRIMapper(new OWLOntologyIRIMapper() {
//
//						public IRI getDocumentIRI(IRI arg0) {
//							final URI mm = mapping.get(arg0.toURI());
//
//							if (mm != null) {
//								return IRI.create(mm);
//							} else {
//								return arg0;
//							}
//						}
//					});
//					try {
//						for (final String uri : ontologyURIs) {
//							if (uri.startsWith("file:")) {
//								m.loadOntologyFromOntologyDocument(new File(URI
//										.create(uri)));
//							} else {
//								m.loadOntology(IRI.create(uri));
//							}
//						}
//					} catch (OWLOntologyCreationException e) {
//						e.printStackTrace();
//					}
//					OWLOntologyMerger merger = new OWLOntologyMerger(m);
//					try {
//						merged = merger.createMergedOntology(m,
//								IRI.create("http://temp"));
//					} catch (OWLOntologyCreationException e) {
//						e.printStackTrace();
//					}
//				}
//				o = map.get(f);
//
//				if (o == null) {
//					OWLReasoner r;
//					if (f.getConfiguration() != null) {
//
//						r = f.getFactory().createReasoner(merged,
//								f.getConfiguration());
//					} else {
//
//						r = f.getFactory().createReasoner(merged);
//					}
//					r.isConsistent();
//
//					// r.precomputeInferences(InferenceType.CLASS_HIERARCHY,
//					// InferenceType.OBJECT_PROPERTY_HIERARCHY);
//
//					o = merged;
//					map.put(f.getFactory(), o);
//				}
//			}
////
////			public void loadQuery(String queryURI) {
////				try {
////					String s = "";
////					BufferedReader reader = new BufferedReader(new FileReader(
////							new File(URI.create(queryURI))));
////					String line = "";
////					while ((line = reader.readLine()) != null) {
////						s += (line + Character.LINE_SEPARATOR);
////					}
////
////					q = new QueryParserImpl().parse(new QueryTokenizerImpl()
////							.tokenize(s));
////				} catch (FileNotFoundException e) {
////					e.printStackTrace();
////				} catch (IOException e) {
////					e.printStackTrace();
////				} catch (QueryParserException e) {
////					e.printStackTrace();
////				}
////			}
//		};
//	}

	public final long run(final GenericOWLAPITester.ReasonerPlugin plugin,
			final String queryURI, final String mappingFile, int runs,
			final String... ontologyURIs) {
		long now = System.currentTimeMillis();
		plugin.loadOntology(
				MappingFileParser.getMappings(new File(mappingFile)),
				ontologyURIs);
		System.out.print((System.currentTimeMillis() - now) + "\t");
		now = System.currentTimeMillis();
		plugin.loadQuery(queryURI);
		System.out.print((System.currentTimeMillis() - now) + "\t");

		long[] executionTimes = new long[runs];
		long size = 0;
		for (int i = 0; i < runs; i++) {
			now = System.currentTimeMillis();
			final long qr = plugin.exec();
			executionTimes[i] = (System.currentTimeMillis() - now);
			if (i == 0) {
				size = qr;
			} else if (size != qr) {
				throw new RuntimeException("INCORRECT NUMBER OF RESULTS: was "
						+ qr + ", expected " + size);
			}
		}
		
		System.out.print(MessageFormat.format(
				"{0,number,#.##}\t\t{1,number,#.##}\t\t{2,number}",
				StatisticsUtils.avg(executionTimes),
				StatisticsUtils.var(executionTimes), size));

		// printResults(qr, new PrintWriter(System.out));

		return size;
	}

//	private <T> void printResults(final QueryResult res, final PrintWriter w) {
//		byte colWidth = 50;
//		w.write(Character.LINE_SEPARATOR);
//		for (final QueryArgument var : res.getQuery().getResultVars()) {
//			for (int i = 0; i < colWidth / 2; i++) {
//				w.write(" ");
//			}
//
//			w.write(var.toString());
//
//			for (int i = colWidth / 2; i < colWidth; i++) {
//				w.write(" ");
//			}
//		}
//		w.write(Character.LINE_SEPARATOR);
//		for (int i = 0; i < colWidth * res.getQuery().getResultVars().size(); i++) {
//			w.write("=");
//		}
//
//		w.write(Character.LINE_SEPARATOR);
//		for (Iterator<QueryBinding> rb = res.iterator(); rb.hasNext();) {
//			QueryBinding r = rb.next();
//			for (final QueryArgument var : res.getQuery().getResultVars()) {
//				w.printf("%-" + colWidth + "s", r.get(var).toString());
//			}
//			w.write(Character.LINE_SEPARATOR);
//		}
//		w.flush();
//	}
}
