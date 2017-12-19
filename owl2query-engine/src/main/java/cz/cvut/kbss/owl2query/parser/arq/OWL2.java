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
package cz.cvut.kbss.owl2query.parser.arq;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.OWL;

public class OWL2 extends OWL {
	final public static Resource minQualifiedCardinality = ResourceFactory
			.createResource(NS + "minQualifiedCardinality");

	final public static Resource maxQualifiedCardinality = ResourceFactory
			.createResource(NS + "maxQualifiedCardinality");

	final public static Resource qualifiedCardinality = ResourceFactory
			.createResource(NS + "qualifiedCardinality");

	final public static Resource AllDisjointClasses = ResourceFactory
			.createResource(NS + "AllDisjointClasses");

	final public static Resource AllDisjointProperties = ResourceFactory
			.createResource(NS + "AllDisjointProperties");

	final public static Resource ReflexiveProperty = ResourceFactory
			.createResource(NS + "ReflexiveProperty");

	final public static Resource IrreflexiveProperty = ResourceFactory
			.createResource(NS + "IrreflexiveProperty");

	final public static Resource AsymmetricProperty = ResourceFactory
			.createResource(NS + "AsymmetricProperty");

	final public static Resource SelfRestriction = ResourceFactory
			.createResource(NS + "SelfRestriction");

	final public static Resource NegativePropertyAssertion = ResourceFactory
			.createResource(NS + "NegativePropertyAssertion");

	final public static Property disjointUnionOf = ResourceFactory
			.createProperty(NS + "disjointUnionOf");

	final public static Property propertyDisjointWith = ResourceFactory
			.createProperty(NS + "propertyDisjointWith");

	final public static Property topObjectProperty = ResourceFactory
			.createProperty(NS + "topObjectProperty");

	final public static Property bottomObjectProperty = ResourceFactory
			.createProperty(NS + "bottomObjectProperty");

	final public static Property topDataProperty = ResourceFactory
			.createProperty(NS + "topDataProperty");

	final public static Property bottomDataProperty = ResourceFactory
			.createProperty(NS + "bottomDataProperty");

	final public static Property onClass = ResourceFactory.createProperty(NS
			+ "onClass");

	final public static Property onDataRange = ResourceFactory
			.createProperty(NS + "onDataRange");

	final public static Property datatypeComplementOf = ResourceFactory
			.createProperty(NS + "datatypeComplementOf");

	final public static Property length = ResourceFactory.createProperty(NS
			+ "length");

	final public static Property maxLength = ResourceFactory.createProperty(NS
			+ "maxLength");

	final public static Property minLength = ResourceFactory.createProperty(NS
			+ "minLength");

	final public static Property totalDigits = ResourceFactory
			.createProperty(NS + "totalDigits");

	final public static Property fractionDigits = ResourceFactory
			.createProperty(NS + "fractionDigits");

	final public static Property minInclusive = ResourceFactory
			.createProperty(NS + "minInclusive");

	final public static Property minExclusive = ResourceFactory
			.createProperty(NS + "minExclusive");

	final public static Property maxInclusive = ResourceFactory
			.createProperty(NS + "maxInclusive");

	final public static Property maxExclusive = ResourceFactory
			.createProperty(NS + "maxExclusive");

	final public static Property hasSelf = ResourceFactory.createProperty(NS
			+ "hasSelf");

	final public static Property pattern = ResourceFactory.createProperty(NS
			+ "pattern");

	final public static Property propertyChain = ResourceFactory
			.createProperty(NS + "propertyChain");

	final public static Property members = ResourceFactory.createProperty(NS
			+ "members");
}