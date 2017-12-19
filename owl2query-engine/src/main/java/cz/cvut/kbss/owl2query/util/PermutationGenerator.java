/*******************************************************************************
 * Copyright (C) 2011 Czech Technical University in Prague                                                                                                                                                        
 *                                                                                                                                                                                                                
 * This program is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any 
 * later version. 
 *                                                                                                                                                                                                                
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
 * details. You should have received a copy of the GNU General Public License 
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package cz.cvut.kbss.owl2query.util;

import java.math.BigInteger;

public class PermutationGenerator {

	private int[] array;
	private BigInteger left;
	private BigInteger total;

	public PermutationGenerator(int n) {
		if (n < 1) {
			throw new IllegalArgumentException("Min 1");
		}
		array = new int[n];
		total = getFactorial(n);
		reset();
	}

	public void reset() {
		for (int i = 0; i < array.length; i++) {
			array[i] = i;
		}
		left = new BigInteger(total.toString());
	}

	public BigInteger getNumLeft() {
		return left;
	}

	public BigInteger getTotal() {
		return total;
	}

	public boolean hasMore() {
		return left.compareTo(BigInteger.ZERO) == 1;
	}

	private static BigInteger getFactorial(int n) {
		BigInteger fact = BigInteger.ONE;
		for (int i = n; i > 1; i--) {
			fact = fact.multiply(new BigInteger(Integer.toString(i)));
		}
		return fact;
	}

	public int[] getNext() {

		if (left.equals(total)) {
			left = left.subtract(BigInteger.ONE);
			return array;
		}

		int temp;

		int j = array.length - 2;
		while (array[j] > array[j + 1]) {
			j--;
		}

		int k = array.length - 1;
		while (array[j] > array[k]) {
			k--;
		}

		temp = array[k];
		array[k] = array[j];
		array[j] = temp;

		int r = array.length - 1;
		int s = j + 1;

		while (r > s) {
			temp = array[s];
			array[s] = array[r];
			array[r] = temp;
			r--;
			s++;
		}

		left = left.subtract(BigInteger.ONE);
		return array;

	}

}
