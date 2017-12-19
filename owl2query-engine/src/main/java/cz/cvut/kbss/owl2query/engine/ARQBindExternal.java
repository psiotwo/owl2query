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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionEnv;

import cz.cvut.kbss.owl2query.model.GroundTerm;
import cz.cvut.kbss.owl2query.model.OWL2Ontology;
import cz.cvut.kbss.owl2query.model.OWLObjectType;
import cz.cvut.kbss.owl2query.model.ResultBinding;
import cz.cvut.kbss.owl2query.model.Term;
import cz.cvut.kbss.owl2query.model.Variable;

public class ARQBindExternal<G> extends External<G> {

	private List<Term<G>> terms;

	final Map<Variable<G>, GroundTerm<G>> binding;

	final Var var;

	final Expr expr;

	public ARQBindExternal(final Var var, final Expr expr) {
		super("arq-expr");
		this.var = var;
		this.expr = expr;
		this.binding = new HashMap<Variable<G>, GroundTerm<G>>();
	}

	private Binding toJena(Map<? extends Term<G>,? extends Term<G>> binding, final OWL2Ontology<G> ont) {
		Binding b = BindingFactory.binding();

		for (Term<G> v : binding.keySet()) {
			Term<G> g = binding.get(v);

			if (!v.isVariable()) {
				continue;
			}

			Variable<G> vv = v.asVariable();

			if (g.isGround()) {
				GroundTerm<G> gg = g.asGroundTerm();
				String strRep = gg.getWrappedObject().toString();

				if (ont.is(gg.getWrappedObject(), OWLObjectType.OWLLiteral)) {
					b = BindingFactory.binding(b, Var.alloc(vv.getName()),
							NodeFactory.createLiteral(strRep));
				} else {
					b = BindingFactory.binding(b, Var.alloc(vv.getName()),
							NodeFactory.createURI(strRep));
				}
			} else if (g.isVariable()) {
				b = BindingFactory.binding(b, Var.alloc(vv.getName()),
						NodeFactory.createVariable(g.asVariable().getName()));
			}
		}

		return b;
	}

	@Override
	public QueryAtom<G> apply(
			Map<? extends Term<G>, ? extends Term<G>> binding,
			OWL2Ontology<G> ont) {

		return new ARQBindExternal<G>(var, expr.copySubstitute(toJena(binding,
				ont)));
	}

	@Override
	public Iterator<ResultBinding<G>> eval(ResultBinding<G> binding,
			final OWL2Ontology<G> o) {
		final FunctionEnv env = new ExecutionContext(ARQ.getContext(), null,
				null, null);
		// TODO
		final List<ResultBinding<G>> newBinding = new ArrayList<ResultBinding<G>>();
		final ResultBinding<G> b = new ResultBindingImpl<G>(binding);
		newBinding.add(b);

		final Binding jenaBinding = toJena(binding, o);
		final Variable<G> v = o.getFactory().variable(var.getName());
		final NodeValue value = expr.eval(jenaBinding, env);
		final GroundTerm<G> g;
		if (value.isLiteral()) {
			g = o.getFactory().wrap(o.getFactory().literal(value.asString()));
		} else if (value.isIRI()) {
			// TODO any URI, not just individuals
			g = o.getFactory().wrap(
					o.getFactory().namedIndividual(value.asString()));
		} else {
			throw new IllegalArgumentException();
		}

		b.put(v, g);

		return newBinding.iterator();
	}

	@Override
	public boolean isGround() {
		return false;
	}

	@Override
	public QueryPredicate getPredicate() {
		return QueryPredicate.Bind;
	}

	@Override
	public List<Term<G>> getArguments() {
		return terms;
	}
}
