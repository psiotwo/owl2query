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
package cz.cvut.kbss.owl2query.engine;

public enum QueryPredicate {
	Type("T"), PropertyValue("PV"), SameAs("SA"), DifferentFrom("DF"), ObjectProperty("OP"), DatatypeProperty("DP"), SubClassOf("SCO"), EquivalentClass("EC"), DisjointWith("DW"), ComplementOf("CO"), EquivalentProperty("EP"), SubPropertyOf("SPO"), InverseOf("IO"), Annotation("A"),

	Functional("Fun"), InverseFunctional("IFun"), Transitive("Tr"), Symmetric("Sym"), Asymmetric("Asym"), Reflexive("Ref"), Irreflexive("Irr"),

	// SPARQL-DL non-monotonic extensions
	DirectType("DT"), StrictSubClassOf("SSCO"), DirectSubClassOf("DSCO"), DirectSubPropertyOf("DSPO"), StrictSubPropertyOf("SSPO"),

	// Negation as failure
	Not("N"),

	// Nested query support
	Core("C"),
	
	// Bind keyword
	Bind("B");

	private QueryPredicate() {
	}

	private String shortForm;
	
	private QueryPredicate(String shortForm) {
		this.shortForm = shortForm;
	}
	
	
	public String shortForm() {
		return shortForm;
	}
	
	@Override
	public String toString() {
		return name();
	}
}