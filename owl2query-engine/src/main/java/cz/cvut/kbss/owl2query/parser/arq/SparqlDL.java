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
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class SparqlDL {

	public static final String sdleBase = "http://pellet.owldl.com/ns/sdle#";

	public static final String sdleNS = "sdle";

	// SPARQL-DL extensions
	public static final Property strictSubClassOf = ResourceFactory
			.createProperty(sdleBase + "strictSubClassOf");

	public static final Property directSubClassOf = ResourceFactory
			.createProperty(sdleBase + "directSubClassOf");

	public static final Property directSubPropertyOf = ResourceFactory
			.createProperty(sdleBase + "directSubPropertyOf");

	public static final Property strictSubPropertyOf = ResourceFactory
			.createProperty(sdleBase + "strictSubPropertyOf");

	public static final Property directType = ResourceFactory
			.createProperty(sdleBase + "directType");
}
