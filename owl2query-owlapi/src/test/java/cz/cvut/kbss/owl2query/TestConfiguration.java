package cz.cvut.kbss.owl2query;

import java.util.HashMap;
import java.util.Map;

//import org.semanticweb.HermiT.Configuration;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class TestConfiguration {

	public static Map<String, TestConfiguration> map = new HashMap<String, TestConfiguration>();

	public static final String PELLET = "pellet";
	public static final String HERMIT = "hermit";
	public static final String FACT = "fact";
	public static final String JFACT = "jfact";

	private OWLReasonerFactory f;
	private OWLReasonerConfiguration c;

	public OWLReasonerConfiguration getConfiguration() {
		return c;
	}

	public OWLReasonerFactory getFactory() {
		return f;
	}

	private TestConfiguration(OWLReasonerFactory f, OWLReasonerConfiguration c) {
		this.f = f;
		this.c = c;
	}

	public static TestConfiguration get(String s) {
		return map.get(s);
	}

	private static void add(String r, String rf, String rc) {
		try {
			map.put(r, new TestConfiguration((OWLReasonerFactory) Class
					.forName(rf).newInstance(), rc != null ?

			(OWLReasonerConfiguration) Class.forName(rc).newInstance() : null));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	static {
		add(PELLET, "com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory",
				"org.semanticweb.owlapi.reasoner.SimpleConfiguration");
		add(HERMIT, "org.semanticweb.HermiT.Reasoner$ReasonerFactory",
				"org.semanticweb.HermiT.Configuration");

		//((Configuration) map.get(HERMIT).getConfiguration()).prepareReasonerInferences = new Configuration.PrepareReasonerInferences();

//		add(FACT,
//				"uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory",
//				null);
		add(JFACT, "uk.ac.manchester.cs.jfact.JFactFactory", "uk.ac.manchester.cs.jfact.kernel.options.JFactReasonerConfiguration");
	}

	public static TestConfiguration FACTORY = map.get(PELLET);
}
