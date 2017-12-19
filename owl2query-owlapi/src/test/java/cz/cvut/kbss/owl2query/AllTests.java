package cz.cvut.kbss.owl2query;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {
	
	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for cz.cvut.kbss.owl2query.engine");
		//$JUnit-BEGIN$
		suite.addTestSuite(GenericOWLAPIv3Simple.class);
//		suite.addTestSuite(NativeLUBMQueries.class);
		//$JUnit-END$
		return suite;
	}

}
