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

public enum KBOperation {
	IS_DIRECT_TYPE, IS_TYPE, GET_DIRECT_INSTANCES, GET_INSTANCES, GET_TYPES, HAS_PROPERTY_VALUE, GET_PROPERTY_VALUE, IS_SAME_AS, GET_SAMES, IS_DIFFERENT_FROM, GET_DIFFERENTS,

	GET_DIRECT_SUB_OR_SUPERCLASSES, GET_EQUIVALENT_CLASSES, IS_SUBCLASS_OF, GET_SUB_OR_SUPERCLASSES, IS_EQUIVALENT_CLASS, IS_DISJOINTCLASS_WITH, GET_DISJOINT_CLASSES, IS_DISJOINTPROPERTY_WITH, GET_DISJOINT_PROPERTIES, IS_COMPLEMENTCLASS_OF, GET_COMPLEMENT_CLASSES, GET_DIRECT_SUB_OR_SUPERPROPERTIES, IS_SUBPROPERTY_OF, GET_EQUIVALENT_PROPERTIES, GET_SUB_OR_SUPERPROPERTIES, IS_EQUIVALENT_PROPERTY,
	
	
	IS_INVERSE_OF, 
	GET_INVERSES, 
	//	IS_OBJECT_PROPERTY,
	// GET_OBJECT_PROPERTIES, IS_DATATYPE_PROPERTY, GET_DATATYPE_PROPERTIES,
	
	IS_FUNCTIONAL_PROPERTY, 
	GET_FUNCTIONAL_PROPERTIES,
	
	IS_INVERSE_FUNCTIONAL_PROPERTY, 
	GET_INVERSE_FUNCTIONAL_PROPERTIES,

	IS_REFLEXIVE_PROPERTY, 
	GET_REFLEXIVE_PROPERTIES,
	
	IS_IRREFLEXIVE_PROPERTY, 
	GET_IRREFLEXIVE_PROPERTIES,

	IS_TRANSITIVE_PROPERTY, 
	GET_TRANSITIVE_PROPERTIES, 
	
	IS_SYMMETRIC_PROPERTY,
	GET_SYMMETRIC_PROPERTIES,
	
	IS_ASYMMETRIC_PROPERTY,
	GET_ASYMMETRIC_PROPERTIES

}
