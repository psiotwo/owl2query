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

import java.util.HashSet;
import java.util.Set;

import cz.cvut.kbss.owl2query.model.OWLObjectType;
import cz.cvut.kbss.owl2query.model.SizeEstimate;
import cz.cvut.kbss.owl2query.model.Term;

class QuerySizeEstimator {

	public static <G> void computeSizeEstimate(final InternalQuery<G> query) {
		final SizeEstimate<G> sizeEstimate = query.getOntology()
				.getSizeEstimate();

		final Set<G> concepts = new HashSet<G>();
		final Set<G> properties = new HashSet<G>();
		// boolean fullDone = false;
		for (final QueryAtom<G> atom : query.getAtoms()) {
			// if (!fullDone) {
			// switch (atom.getPredicate()) {
			// case Type:
			// if (query.getDistVars()
			// .contains(atom.getArguments().get(1))) {
			// fullDone = true;
			// }
			// break;
			// case PropertyValue:
			// if (query.getDistVars()
			// .contains(atom.getArguments().get(1))) {
			// fullDone = true;
			// }
			// break;
			// case SameAs:
			// case DifferentFrom:
			// break;
			// default:
			// // fullDone = true;
			// ;
			// }
			// if (fullDone) {
			// sizeEstimate.computeAll();
			// }
			// }

			for (final Term<G> argument : atom.getArguments()) {
				if (argument.isGround()) {
					if ((query.getOntology().is(
							argument.asGroundTerm().getWrappedObject(),
							OWLObjectType.OWLClass) // ||
					// !argument.isURI()
					&& !sizeEstimate.isComputed(argument.asGroundTerm()
							.getWrappedObject()))) {
						concepts
								.add(argument.asGroundTerm().getWrappedObject());
					}

					if ((query.getOntology().is(argument.asGroundTerm()
							.getWrappedObject(),
							OWLObjectType.OWLObjectProperty,
							OWLObjectType.OWLDataProperty,
							OWLObjectType.OWLAnnotationProperty))
							&& !sizeEstimate.isComputed(argument.asGroundTerm()
									.getWrappedObject())) {
						properties.add(argument.asGroundTerm()
								.getWrappedObject());
					}
				}
			}
		}

		sizeEstimate.compute(concepts, properties);
	}
}
