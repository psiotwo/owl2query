package cz.cvut.kbss.owl2query;

import java.util.Arrays;
import java.util.Collection;

public class UTAM {
	public static void main(String[] args) {
		final QueryTester q = new QueryTester();
		// Collection<Integer> queries = Arrays.asList(1, /* ,2 */3, 4, 5, 6, 7,
		// /*
		// * ,8
		// * ,
		// * 9
		// */
		// 10 /* ,11, 12, 13 */, 14, 15, 16);
		Collection<Integer> queries = Arrays.asList(2);

		// final Collection<ReasonerPlugin> rs = Arrays.asList((ReasonerPlugin)
		// q
		// .getGenericOWLAPIv3(new PelletReasonerFactory())

		final Collection<GenericOWLAPITester.ReasonerPlugin> rs = Arrays.asList((GenericOWLAPITester.ReasonerPlugin) q
				.getGenericOWLAPIv3(TestConfiguration.FACTORY));

		// ,q
		// .getGenericOWLAPIv3(new HermiTReasonerFactory())

		System.out.println("QUERY \t\t|\t1\t|\t2\t|\t3\t|\t# of results\t");
		System.out
				.println("==============================================================================");

		// for (int i : queries) {
		for (final GenericOWLAPITester.ReasonerPlugin rp : rs) {

			System.out.print("\n" + 1 + "-" + rp.getAbbr() + "\t|\t");
			System.out
					.println(q
							.run(rp,
									"file:///home/kremen/fel/projects/utam/internal-svn/utam-failures/impl/runtime-queries/Xquery-defects-of-objects-and-parts.sparql",
									"/home/kremen/fel/projects/utam/internal-svn/utam-failures/impl/runtime/mapping",1,
									"file:///home/kremen/fel/projects/utam/internal-svn/utam-failures/impl/runtime/utam-failures.owl"
							// "file:///home/kremen/work/java/datasets/lubm/university0-0.owl",
							// "file:///home/kremen/work/java/datasets/lubm/university0-1.owl",
							// "file:///home/kremen/work/java/datasets/lubm/university0-2.owl",
							// "file:///home/kremen/work/java/datasets/lubm/university0-3.owl",
							// "file:///home/kremen/work/java/datasets/lubm/university0-4.owl",
							// "file:///home/kremen/work/java/datasets/lubm/university0-5.owl",
							// "file:///home/kremen/work/java/datasets/lubm/university0-6.owl",
							// "file:///home/kremen/work/java/datasets/lubm/university0-7.owl",
							// "file:///home/kremen/work/java/datasets/lubm/university0-8.owl",
							// "file:///home/kremen/work/java/datasets/lubm/university0-9.owl",
							// "file:///home/kremen/work/java/datasets/lubm/university0-10.owl",
							// "file:///home/kremen/work/java/datasets/lubm/university0-11.owl",
							// "file:///home/kremen/work/java/datasets/lubm/university0-12.owl",
							// "file:///home/kremen/work/java/datasets/lubm/university0-13.owl",
							// "file:///home/kremen/work/java/datasets/lubm/university0-14.owl"
							));
		}
		System.out
				.println("\n---------------------------------------------------------------------------------");
	}
	// }
}
