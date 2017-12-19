package cz.cvut.kbss.owl2query.util;

public class StatisticsUtils {

	public static double avg(long[] vector) {
		int size = vector.length;
		double result = 0;
		for (int i = 0; i < size; i++)
			result = (i * result + vector[i]) / (i + 1);
		return result;
	}

	public static double var(long[] vector) {
		int size = vector.length;
		double result = 0;
		for (int i = 0; i < size; i++)
			result = (i * result + vector[i]*vector[i]) / (i + 1);
		double resAvg = avg(vector);
		return result - resAvg*resAvg;
	}
	
	public static void main(String[] args) {
		System.out.println(StatisticsUtils.avg(new long[]{1,2,3}));
		System.out.println(StatisticsUtils.var(new long[]{1,2,3,4,5,6,7,8,9,10}));
	}
}
