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

import java.util.Collection;

public enum VarType {
	CLASS(new OWLObjectType[] { OWLObjectType.OWLClass }),

	OBJECT_OR_DATA_PROPERTY(new OWLObjectType[] {
			OWLObjectType.OWLObjectProperty, OWLObjectType.OWLDataProperty }),

	OBJECT_PROPERTY(new OWLObjectType[] { OWLObjectType.OWLObjectProperty }),

	DATA_PROPERTY(new OWLObjectType[] { OWLObjectType.OWLDataProperty }),

	INDIVIDUAL(new OWLObjectType[] { OWLObjectType.OWLNamedIndividual }),

	LITERAL(new OWLObjectType[] { OWLObjectType.OWLLiteral }),

	INDIVIDUAL_OR_LITERAL(new OWLObjectType[] {
			OWLObjectType.OWLNamedIndividual, OWLObjectType.OWLLiteral }),

	ANY(new OWLObjectType[] { OWLObjectType.OWLAnnotationProperty,
			OWLObjectType.OWLDataProperty, OWLObjectType.OWLObjectProperty,
			OWLObjectType.OWLClass, OWLObjectType.OWLNamedIndividual,
			OWLObjectType.OWLLiteral });

	private OWLObjectType[] allowed;

	private VarType(OWLObjectType[] allowed) {
		this.allowed = allowed;
	}

	public OWLObjectType[] getAllowedTypes() {
		return allowed;
	}

	public boolean updateIfValid(Collection<VarType> vars) {
		if (vars.contains(this)) {
			return true;
		}

		switch (this) {
		case CLASS:
			if (vars.contains(LITERAL)) {
				return false;
			} else if (vars.contains(INDIVIDUAL_OR_LITERAL)) {
				vars.remove(INDIVIDUAL_OR_LITERAL);
				vars.add(INDIVIDUAL);
			}
			vars.add(CLASS);
			break;
		case INDIVIDUAL:
			if (vars.contains(LITERAL)) {
				return false;
			} else if (vars.contains(INDIVIDUAL_OR_LITERAL)) {
				vars.remove(INDIVIDUAL_OR_LITERAL);
			}
			vars.add(INDIVIDUAL);
			break;
		case DATA_PROPERTY:
			if (vars.contains(LITERAL)) {
				return false;
			} else if (vars.contains(INDIVIDUAL_OR_LITERAL)) {
				vars.remove(INDIVIDUAL_OR_LITERAL);
				vars.add(INDIVIDUAL);
			}

			if (vars.contains(OBJECT_OR_DATA_PROPERTY)) {
				vars.add(OBJECT_OR_DATA_PROPERTY);
			}
			vars.add(DATA_PROPERTY);
			break;
		case OBJECT_OR_DATA_PROPERTY:
			if (vars.contains(LITERAL)) {
				return false;
			} else if (vars.contains(INDIVIDUAL_OR_LITERAL)) {
				vars.remove(INDIVIDUAL_OR_LITERAL);
				vars.add(INDIVIDUAL);
			}

			if (!vars.contains(OBJECT_PROPERTY)
					&& !vars.contains(DATA_PROPERTY)) {
				vars.add(OBJECT_OR_DATA_PROPERTY);
			}
			break;
		case OBJECT_PROPERTY:
			if (vars.contains(LITERAL)) {
				return false;
			} else if (vars.contains(INDIVIDUAL_OR_LITERAL)) {
				vars.remove(INDIVIDUAL_OR_LITERAL);
				vars.add(INDIVIDUAL);
			}

			if (vars.contains(OBJECT_OR_DATA_PROPERTY)) {
				vars.add(OBJECT_OR_DATA_PROPERTY);
			}
			vars.add(OBJECT_PROPERTY);
			break;
		case INDIVIDUAL_OR_LITERAL:
			if (vars.contains(INDIVIDUAL)) {
				return true;
			} else if (vars.contains(CLASS) || vars.contains(OBJECT_PROPERTY)
					|| vars.contains(OBJECT_OR_DATA_PROPERTY)
					|| vars.contains(DATA_PROPERTY)) {
				vars.add(INDIVIDUAL);
			} else if (!vars.contains(VarType.INDIVIDUAL)
					&& !vars.contains(VarType.LITERAL)) {
				vars.add(INDIVIDUAL_OR_LITERAL);
			}
			break;
		case LITERAL:
			if (vars.contains(vars.contains(CLASS)
					|| vars.contains(OBJECT_PROPERTY)
					|| vars.contains(OBJECT_OR_DATA_PROPERTY)
					|| vars.contains(DATA_PROPERTY)
					|| vars.contains(INDIVIDUAL))) {
				return false;
			} else if (vars.contains(INDIVIDUAL_OR_LITERAL)) {
				vars.remove(INDIVIDUAL_OR_LITERAL);
				vars.add(LITERAL);
			}
			break;
		default:
			;

		}
		return true;
	}
}
