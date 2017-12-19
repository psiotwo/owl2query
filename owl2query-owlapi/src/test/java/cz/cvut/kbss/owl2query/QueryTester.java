package cz.cvut.kbss.owl2query;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URI;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.OWLOntologyMerger;

import cz.cvut.kbss.owl2query.engine.OWL2QueryEngine;
import cz.cvut.kbss.owl2query.model.OWL2Ontology;
import cz.cvut.kbss.owl2query.model.OWL2Query;
import cz.cvut.kbss.owl2query.model.QueryResult;
import cz.cvut.kbss.owl2query.model.ResultBinding;
import cz.cvut.kbss.owl2query.model.Variable;
import cz.cvut.kbss.owl2query.model.owlapi.OWLAPIv3OWL2Ontology;
import cz.cvut.kbss.owl2query.parser.QueryParseException;
import cz.cvut.kbss.owl2query.parser.arq.SparqlARQParser;
import cz.cvut.kbss.owl2query.util.StatisticsUtils;

public class QueryTester implements GenericOWLAPITester {

	// public final static ReasonerPlugin<ATermAppl> pellet = new
	// ReasonerPlugin<ATermAppl>() {
	//
	// private OWL2Ontology<ATermAppl> o;
	// private OWL2Query<ATermAppl> q;
	//
	// public String getAbbr() {
	// return "pellet";
	// }
	//
	// public QueryResult<ATermAppl> exec() {
	// return OWL2QueryEngine.exec(q);
	// }
	//
	// public void loadOntology(Map<URI, URI> mapping, String... ontologyURIs) {
	// final JenaLoader l = new JenaLoader();
	// final KnowledgeBase kb = l.createKB(ontologyURIs);
	// o = new PelletOWL2Ontology(kb);
	// }
	//
	// public void loadQuery(String queryURI) {
	// try {
	// q = new cz.cvut.kbss.owl2query.parser.arq.SparqlARQParser<ATermAppl>()
	// .parse(new FileInputStream(new File(URI
	// .create(queryURI))), o);
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// } catch (QueryParseException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// };
	//
	// public final static ReasonerPlugin<Object> kaon2 = new
	// ReasonerPlugin<Object>() {
	//
	// private OWL2Ontology<Object> o;
	// private OWL2Query<Object> q;
	//
	// public String getAbbr() {
	// return "kaon2";
	// }
	//
	// public QueryResult<Object> exec() {
	// return OWL2QueryEngine.exec(q);
	// }
	//
	// public void loadOntology(Map<URI, URI> mapping, String... ontologyURIs) {
	// OntologyManager m;
	// try {
	// m = KAON2Manager.newOntologyManager();
	// Ontology ox = null;
	// for (final String s : ontologyURIs) {
	// ox = m.openOntology(s,
	// Collections.<String, Object> emptyMap());
	// }
	//
	// Reasoner r = ox.createReasoner();
	// o = new KaonOWL2Ontology(m, ox, r);
	// } catch (KAON2Exception e) {
	// e.printStackTrace();
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	//
	// }
	//
	// public void loadQuery(String queryURI) {
	// try {
	// q = new cz.cvut.kbss.owl2query.parser.arq.SparqlARQParser<Object>()
	// .parse(new FileInputStream(new File(URI
	// .create(queryURI))), o);
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// } catch (QueryParseException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// };

	static OWLOntologyManager m;
	static OWLOntology merged;

	static Map<OWLReasonerFactory, OWL2Ontology> map = new HashMap<OWLReasonerFactory, OWL2Ontology>();

	public static ReasonerPlugin getGenericOWLAPIv3(final TestConfiguration f) {

		return new ReasonerPlugin() {
			public String getAbbr() {
				return "OWLAPI-"
						+ f.getFactory().getReasonerName().substring(0, 1);
			}

			private OWL2Ontology<OWLObject> o;

			private OWL2Query<OWLObject> q;

			public long exec() {
				return OWL2QueryEngine.exec(q).size();
			}

			public void loadOntology(final Map<URI, URI> mapping,
					String... ontologyURIs) {
				if (m == null) {
					m = OWLManager.createOWLOntologyManager();
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
					OWLOntologyMerger merger = new OWLOntologyMerger(m);
					try {
						merged = merger.createMergedOntology(m,
								IRI.create("http://temp"));
					} catch (OWLOntologyCreationException e) {
						e.printStackTrace();
					}
				}
				o = map.get(f);

				if (o == null) {
					OWLReasoner r;
					if (f.getConfiguration() != null) {

						r = f.getFactory().createReasoner(merged,
								f.getConfiguration());
					} else {

						r = f.getFactory().createReasoner(merged);
					}
					r.isConsistent();

					// r.precomputeInferences(InferenceType.CLASS_HIERARCHY,
					// InferenceType.OBJECT_PROPERTY_HIERARCHY);

					o = new OWLAPIv3OWL2Ontology(m, merged, r);
					map.put(f.getFactory(), o);
				}
			}

			public void loadQuery(String queryURI) {
				try {
					q = new SparqlARQParser<OWLObject>()
							.parse(new FileInputStream(new File(URI
									.create(queryURI))), o);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (QueryParseException e) {
					e.printStackTrace();
				}
			}
		};
	}

	// public static ReasonerPlugin<OWLObject> getGenericOWLAPIv3(
	// final OWLReasonerFactory f) {
	//
	// return new ReasonerPlugin<OWLObject>() {
	// public String getAbbr() {
	// return "OWLAPI-" + f.getReasonerName().substring(0, 1);
	// }
	//
	// private OWL2Ontology<OWLObject> o;
	//
	// private OWL2Query<OWLObject> q;
	//
	// @Override
	// public QueryResult<OWLObject> exec() {
	// return OWL2QueryEngine.exec(q);
	// }
	//
	// @Override
	// public void loadOntology(final Map<URI, URI> mapping,
	// String... ontologyURIs) {
	// final OWLOntologyManager m = OWLManager
	// .createOWLOntologyManager();
	// m.addIRIMapper(new OWLOntologyIRIMapper() {
	//
	// @Override
	// public URI getPhysicalURI(IRI arg0) {
	// final URI mm = mapping.get(arg0.toURI());
	//
	// if (mm != null) {
	// return mm;
	// } else {
	// return arg0.toURI();
	// }
	// }
	// });
	// try {
	// for (final String uri : ontologyURIs) {
	// if (uri.startsWith("file:")) {
	// m.loadOntologyFromPhysicalURI(URI.create(uri));
	// } else {
	// m.loadOntology(IRI.create(uri));
	// }
	// }
	// OWLOntologyMerger merger = new OWLOntologyMerger(m);
	// OWLOntology merged = merger.createMergedOntology(m, IRI
	// .create("http://temp"));
	//
	// OWLReasoner r = f.createReasoner(m, m.getOntologies());
	// r.classify();
	//
	// o = new OWLAPIv3OWL2Ontology(m, merged, r);
	// } catch (OWLOntologyCreationException e) {
	// e.printStackTrace();
	// } catch (OWLOntologyChangeException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (OWLReasonerException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	//
	// @Override
	// public void loadQuery(String queryURI) {
	// try {
	// q = new
	// cz.cvut.kbss.owl2query.parser.arq.SparqlARQParser<OWLObject>()
	// .parse(new FileInputStream(new File(URI
	// .create(queryURI))), o);
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// } catch (QueryParseException e) {
	// e.printStackTrace();
	// }
	// }
	// };
	// }

	public final long run(final ReasonerPlugin plugin, final String queryURI,
			final String mappingFile, int runs, final String... ontologyURIs) {
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

	private <T> void printResults(final QueryResult<T> res, final PrintWriter w) {
		byte colWidth = 50;
		w.write(Character.LINE_SEPARATOR);
		for (final Variable<T> var : res.getResultVars()) {
			for (int i = 0; i < colWidth / 2; i++) {
				w.write(" ");
			}

			w.write(var.getName());

			for (int i = colWidth / 2; i < colWidth; i++) {
				w.write(" ");
			}
		}
		w.write(Character.LINE_SEPARATOR);
		for (int i = 0; i < colWidth * res.getResultVars().size(); i++) {
			w.write("=");
		}

		w.write(Character.LINE_SEPARATOR);
		for (Iterator<ResultBinding<T>> rb = res.iterator(); rb.hasNext();) {
			ResultBinding<T> r = rb.next();
			for (final Variable<T> var : res.getResultVars()) {
				w.printf("%-" + colWidth + "s", r.get(var).asGroundTerm()
						.toString());
			}
			w.write(Character.LINE_SEPARATOR);
		}
		w.flush();
	}
}
