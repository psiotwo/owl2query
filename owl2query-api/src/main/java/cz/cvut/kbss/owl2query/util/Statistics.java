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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Statistics<R, C> {
	private static final Logger log = Logger.getLogger(Statistics.class
			.getName());

	private Map<C, Map<R, Number>> statistics = new HashMap<C, Map<R, Number>>();

	private List<C> cols = new ArrayList<C>();
	private List<R> rows = new ArrayList<R>();

	private int firstColumnSize = 10;

	public void add(final R row, final C col, final Number stat) {
		Map<R, Number> getCol = statistics.get(col);

		if (getCol == null) {
			getCol = new HashMap<R, Number>();
			statistics.put(col, getCol);
			cols.add(col);
		}

		Number getStat = getCol.get(row);

		if (getStat != null) {
			log.warning("Overwriting [" + row + " : " + col + "].");
		} else {
			if (!rows.contains(row)) {
				if (firstColumnSize < row.toString().length()) {
					firstColumnSize = row.toString().length();
				}
				rows.add(row);
			}
		}

		getCol.put(row, stat);
	}

	public void add(final C col, final Map<R, ? extends Number> stat) {
		for (R row : stat.keySet()) {
			add(row, col, stat.get(row));
		}
	}

	@Override
	public String toString() {
		String s = "";
		List<Integer> colSizes = new ArrayList<Integer>();

		for (C col : cols) {
			colSizes.add(col.toString().length() + 2);
		}

		// format of first column
		String firstCol = "| %1$-" + (firstColumnSize + 2) + "s ";

		// format of one line
		String lineFormat = "";

		for (int i = 1; i < colSizes.size() + 1; i++) {
			lineFormat += "| %" + i + "$-10.10s ";
		}

		lineFormat += "|\n";

		// separator
		final char[] a = new char[String.format(lineFormat, cols.toArray())
				.length()
				+ String.format(firstCol, "").length()];

		Arrays.fill(a, '=');
		final String separator = new String(a);

		s += separator + "\n";
		s += String.format(firstCol, "")
				+ String.format(lineFormat, cols.toArray());
		s += separator + "\n";

		for (final R row : rows) {
			final List<Number> rowData = new ArrayList<Number>();
			for (final C col : cols) {
				final Map<R, Number> map = statistics.get(col);
				Number stat = map.get(row);

				if (stat == null) {
					rowData.add(Double.POSITIVE_INFINITY);
				} else {
					rowData.add(stat);
				}
			}

			String rowName;

			// // TODO
			// try {
			// rowName = URI.create(row.toString()).getFragment();
			// } catch (Exception e) {
			rowName = row.toString();
			// }

			s += String.format(firstCol, new Object[] { rowName })
					+ String.format(lineFormat, rowData.toArray());
		}

		s += separator + "\n";

		return s;
	}
}
