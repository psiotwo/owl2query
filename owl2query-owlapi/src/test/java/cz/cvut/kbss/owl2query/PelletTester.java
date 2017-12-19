package cz.cvut.kbss.owl2query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;

import org.mindswap.pellet.KnowledgeBase;
import org.semanticweb.owlapi.model.*;

import aterm.ATermAppl;

import com.clarkparsia.pellet.owlapiv3.OWLAPILoader;
import com.clarkparsia.pellet.sparqldl.engine.QueryEngine;
import com.clarkparsia.pellet.sparqldl.model.Query;
import com.clarkparsia.pellet.sparqldl.model.QueryResult;
import com.clarkparsia.pellet.sparqldl.model.ResultBinding;
import com.clarkparsia.pellet.sparqldl.parser.ARQParser;

import cz.cvut.kbss.owl2query.util.StatisticsUtils;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class PelletTester implements GenericOWLAPITester {

	static KnowledgeBase kb;
	static OWLOntologyManager m;

	public static ReasonerPlugin getGenericOWLAPIv3(final TestConfiguration f) {

		return new ReasonerPlugin() {
			public String getAbbr() {
				return "Pellet-"
						+ f.getFactory().getReasonerName().substring(0, 1);
			}

			private Query q;

			public long exec() {
				return QueryEngine.exec(q, kb).size();
			}

			public void loadOntology(final Map<URI, URI> mapping,
					String... ontologyURIs) {
				OWLAPILoader l = new OWLAPILoader();
				kb = l.getKB();
				
				if (m == null) {
					m = l.getManager();
					m.addIRIMapper(new OWLOntologyIRIMapper() {
						public IRI getDocumentIRI(IRI arg0) {
							final URI mm = mapping.get(arg0.toURI());

							if (mm != null) {
								return IRI.create(mm);
							} else {
								return arg0;
							}
						}
					});
										
					try {
						for (final String uri : ontologyURIs) {
							if (uri.startsWith("file:")) {
								m.loadOntologyFromOntologyDocument(new File(URI
										.create(uri)));
							} else {
								m.loadOntology(IRI.create(uri));
							}
						}
					} catch (OWLOntologyCreationException e) {
						e.printStackTrace();
					}
				}

//                OWLOntology o = map.get(f);
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
			}

			public void loadQuery(String queryURI) {
				try {
					String s = "";
					BufferedReader reader = new BufferedReader(new FileReader(
							new File(URI.create(queryURI))));
					String line = "";
					while ((line = reader.readLine()) != null) {
						s += (line + Character.LINE_SEPARATOR);
					}

					q = new ARQParser().parse(s, kb);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}

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

	private <T> void printResults(final QueryResult res, final PrintWriter w) {
		byte colWidth = 50;
		w.write(Character.LINE_SEPARATOR);
		for (final ATermAppl var : res.getResultVars()) {
			for (int i = 0; i < colWidth / 2; i++) {
				w.write(" ");
			}

			w.write(var.toString());

			for (int i = colWidth / 2; i < colWidth; i++) {
				w.write(" ");
			}
		}
		w.write(Character.LINE_SEPARATOR);
		for (int i = 0; i < colWidth * res.getResultVars().size(); i++) {
			w.write("=");
		}

		w.write(Character.LINE_SEPARATOR);
		for (Iterator<ResultBinding> rb = res.iterator(); rb.hasNext();) {
			ResultBinding r = rb.next();
			for (final ATermAppl var : res.getResultVars()) {
				w.printf("%-" + colWidth + "s", r.getValue(var).toString());
			}
			w.write(Character.LINE_SEPARATOR);
		}
		w.flush();
	}
}
