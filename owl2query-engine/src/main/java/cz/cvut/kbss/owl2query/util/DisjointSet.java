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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Union-Find with ranking and path compression heuristics.
 */
public class DisjointSet<E> {
	private final Map<E, Integer> rank = new HashMap<E, Integer>();
	private final Map<E, E> parentMap = new HashMap<E, E>();
	private final Map<E, Set<E>> elements = new HashMap<E, Set<E>>();

	public void add(final E e) {
		if (elements.keySet().contains(e) || parentMap.keySet().contains(e)) {
			return;
		}
		elements.put(e, new HashSet<E>(Collections.singleton(e)));
	}

	public void union(final E e1, final E e2) {
		add(e1);
		add(e2);

		final E root1 = find(e1);
		final E root2 = find(e2);

		if (getRank(root1) < getRank(root2)) {
			parentMap.put(root1, root2);
			elements.get(root2).addAll(elements.get(root1));
			elements.remove(root1);
		} else if (getRank(root2) < getRank(root1)) {
			parentMap.put(root2, root1);
			elements.get(root1).addAll(elements.get(root2));
			elements.remove(root2);
		} else if (root1 != root2) {
			parentMap.put(root1, root2);
			elements.get(root2).addAll(elements.get(root1));
			elements.remove(root1);
			rank.put(root2, getRank(root2) + 1);
		}
	}

	private int getRank(final E e) {
		if (rank.containsKey(e)) {
			return rank.get(e);
		} else {
			return 0;
		}
	}

	/**
	 * Find with path-compression.
	 */
	public E find(E e) {
		if (!elements.containsKey(e) && !parentMap.containsKey(e)) {
			return null;
		} else if (!parentMap.containsKey(e)) {
			return e;
		} else {
			final E parent = parentMap.get(e);
			final E root = find(parent);

			parentMap.put(e, root);

			rank.put(root, 1);

			return root;
		}
	}

	public Collection<Set<E>> getEquivalenceSets() {
		return elements.values();
	}
}