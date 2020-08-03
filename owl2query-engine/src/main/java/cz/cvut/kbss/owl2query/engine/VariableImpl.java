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

import cz.cvut.kbss.owl2query.model.*;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

class VariableImpl<T> implements Variable<T> {

    private final String id;

    public VariableImpl(final String string) {
        this.id = string;
    }


    public boolean isVariable() {
        return true;
    }


    public String getName() {
        return id;
    }


    public GroundTerm<T> asGroundTerm() {
        throw new IllegalArgumentException();
    }


    public Variable<T> asVariable() {
        return this;
    }


    public boolean isGround() {
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VariableImpl other = (VariableImpl) obj;
        if (id == null) {
            return other.id == null;
        } else return id.equals(other.id);
    }

    @Override
    public String toString() {
        return "?" + id;
    }


    public String shortForm() {
        if (id.startsWith("?")) {
            return "_:" + id.substring(1);
        } else {
            return "?" + id;
        }
    }


    public Term<T> apply(Map<Variable<T>, GroundTerm<T>> binding, OWL2Ontology<T> ont) {
        if (binding.containsKey(this)) {
            return binding.get(this);
        } else {
            return this;
        }
    }


    public Set<Variable<T>> getVariables() {
        return Collections.singleton(this);
    }


    public VarType getVariableType(Variable<T> var) {
        return null;
    }

}
