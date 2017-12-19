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
package cz.cvut.kbss.owl2query.model;

import java.util.Set;

/**
 * An abstract access to all hierarchic information: - TBOX - told TBOX - RBOX
 * 
 * Each implementation of this class operates over two types of elements: -
 * named elements (classes, properties) - general elements (class expressions,
 * property expressions).
 * 
 * @author Petr Kremen
 */
public interface Hierarchy<G, T extends G> {

	/**
	 * Returns a set of all top elements (for hierarchies where no common root
	 * is defined). E.g. OWL property hierarchy can have two roots -
	 * owl:topObjectProperty and owl:topDataProperty.
	 * 
	 * @return a set of named elements comprising the top (uncomparable) layer
	 *         of this hierarchy
	 */
	public Set<T> getTops();

    /**
     * Returns true if subG1 is a subelement of subG2.
     *
     * @return a set of named elements that are (direct) subelements of the
     *         general element superG
     */
    public boolean isSub(final G subG1, final G superG2, boolean direct);

    /**
	 * Returns a set of direct/all subs, NOT including getEquivs.
	 * 
	 * The set might include elements from getBottoms() if applicable.
	 * 
	 * @return a set of named elements that are (direct) subelements of the
	 *         general element superG
	 */
	public Set<T> getSubs(final G superG, boolean direct);

	/**
	 * Returns a set of direct/all supers, NOT including getEquivs
	 * 
	 * The set might include elements from getTops() if applicable.
	 * 
	 * @return a set of named elements that are (direct) superelements of the
	 *         general element superG
	 */
	public Set<T> getSupers(final G subG, boolean direct);

    /**
     * Returns true if equivG1 is equivalent to equivG2.
     *
     * @return a set of named elements that are equivalent to the general
     *         element superG
     */
    public boolean isEquiv(final G equivG1,final G equivG2);

    /**
	 * Returns a set of all equivalents subs, NOT including equivG.
	 * 
	 * @return a set of named elements that are equivalent to the general
	 *         element superG
	 */
	public Set<T> getEquivs(final G equivG);

	/**
	 * Returns a set of all disjoint classes.
	 * 
	 * @return a set of named elements that are disjoint with disjointG
	 */
	public Set<T> getDisjoints(final G disjointG);

    /**
     * Returns true if disjointG1 is disjoint with disjointG2.
     *
     * @return a set of named elements that are disjoint with disjointG
     */
    public boolean isDisjointWith(final G disjointG1,final G disjointG2);

    /**
     * Returns a set of all complements.
     *
     * @return a set of named elements that are complement to complementG
     */
    public Set<T> getComplements(final G complementG);

    /**
     * Returns true if complementG1 is disjoint with complementG2.
     *
     * @return a set of named elements that are disjoint with disjointG
     */
    public boolean isComplementWith(final G complementG1,final G complementG2);

    /**
	 * Returns a set of all bottom elements (in case no common sink is defined).
	 * E.g. OWL property hierarchy can have two sinks - owl:bottomObjectProperty
	 * and owl:bottomDataProperty.
	 * 
	 * @return a set of named elements comprising the bottom (uncomparable)
	 *         layer of this hierarchy
	 */
	public Set<T> getBottoms();
}
