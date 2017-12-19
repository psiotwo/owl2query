package cz.cvut.kbss.owl2query.model.pellet;

import java.util.Set;

import org.mindswap.pellet.utils.ATermUtils;

import aterm.ATermAppl;
import aterm.ATermList;
import cz.cvut.kbss.owl2query.engine.AbstractOWL2QueryFactory;

public class PelletQueryFactory extends AbstractOWL2QueryFactory<ATermAppl> {

	@Override
	public ATermAppl inverseObjectProperty(ATermAppl op) {
		return ATermUtils.makeInv(op);
	}

	@Override
	public ATermAppl literal(String s) {
		return ATermUtils.makePlainLiteral(s);
	}

	@Override
	public ATermAppl literal(String s, String lang) {
		return ATermUtils.makePlainLiteral(s, lang);
	}

	@Override
	public ATermAppl typedLiteral(String s, String dt) {
		return ATermUtils.makeTypedLiteral(s, dt);
	}

	@Override
	public ATermAppl namedClass(String uri) {
		return ATermUtils.makeTermAppl(uri);
	}

	@Override
	public ATermAppl namedDataProperty(String uri) {
		return ATermUtils.makeTermAppl(uri);
	}

	@Override
	public ATermAppl namedDataRange(String uri) {
		return ATermUtils.makeTermAppl(uri);
	}

	@Override
	public ATermAppl namedIndividual(String uri) {
		return ATermUtils.makeTermAppl(uri);
	}

	@Override
	public ATermAppl namedObjectProperty(String uri) {
		return ATermUtils.makeTermAppl(uri);
	}

	@Override
	public ATermAppl objectAllValuesFrom(ATermAppl ope, ATermAppl ce) {
		return ATermUtils.makeAllValues(ope, ce);
	}

	@Override
	public ATermAppl objectComplementOf(ATermAppl ce) {
		return ATermUtils.makeNot(ce);
	}

	@Override
	public ATermAppl objectExactCardinality(int card, ATermAppl ope,
			ATermAppl ce) {
		return ATermUtils.makeExactCard(ope, card, ce);
	}

	@Override
	public ATermAppl objectHasSelf(ATermAppl ope) {
		return ATermUtils.makeSelf(ope);
	}

	@Override
	public ATermAppl objectHasValue(ATermAppl ope, ATermAppl ni) {
		return ATermUtils.makeHasValue(ope, ni);
	}

	@Override
	public ATermAppl objectIntersectionOf(Set<ATermAppl> c) {
		return ATermUtils.makeAnd(ATermUtils.makeList(c));
	}

	@Override
	public ATermAppl objectMaxCardinality(int card, ATermAppl ope, ATermAppl ce) {
		return ATermUtils.makeMax(ope, card, ce);
	}

	@Override
	public ATermAppl objectMinCardinality(int card, ATermAppl ope, ATermAppl ce) {
		return ATermUtils.makeMin(ope, card, ce);
	}

	@Override
	public ATermAppl objectSomeValuesFrom(ATermAppl ope, ATermAppl ce) {
		return ATermUtils.makeSomeValues(ope, ce);
	}

	@Override
	public ATermAppl objectUnionOf(Set<ATermAppl> c) {
		return ATermUtils.makeOr(ATermUtils.makeList(c));
	}

	@Override
	public ATermAppl getNothing() {
		return ATermUtils.BOTTOM;
	}

	@Override
	public ATermAppl getThing() {
		return ATermUtils.TOP;
	}

	@Override
	public ATermAppl getTopDatatype() {
		return ATermUtils.TOP_LIT;
	}

	@Override
	public ATermAppl dataSomeValuesFrom(ATermAppl dpe, ATermAppl ce) {
		return ATermUtils.makeSomeValues(dpe, ce);
	}

	@Override
	public ATermAppl dataExactCardinality(int card, ATermAppl ope, ATermAppl dr) {
		return ATermUtils.makeExactCard(ope, card, dr);
	}

	@Override
	public ATermAppl dataMaxCardinality(int card, ATermAppl ope, ATermAppl dr) {
		return ATermUtils.makeMax(ope, card, dr);
	}

	@Override
	public ATermAppl dataMinCardinality(int card, ATermAppl ope, ATermAppl dr) {
		return ATermUtils.makeMin(ope, card, dr);
	}

	@Override
	public ATermAppl getBottomDataProperty() {
		return ATermUtils.BOTTOM;
	}

	@Override
	public ATermAppl getBottomObjectProperty() {
		return ATermUtils.BOTTOM;
	}

	@Override
	public ATermAppl getTopDataProperty() {
		return ATermUtils.TOP;
	}

	@Override
	public ATermAppl getTopObjectProperty() {
		return ATermUtils.TOP;
	}

	@Override
	public ATermAppl dataOneOf(Set<ATermAppl> nis) {
		ATermList list = ATermUtils.EMPTY_LIST;

		for (final ATermAppl a : nis) {
			list = list.insert(ATermUtils.makeValue(a));
		}

		return ATermUtils.makeOr(list);
	}

	@Override
	public ATermAppl objectOneOf(Set<ATermAppl> nis) {
		ATermList list = ATermUtils.EMPTY_LIST;

		for (final ATermAppl a : nis) {
			list = list.insert(ATermUtils.makeValue(a));
		}

		return ATermUtils.makeOr(list);
	}

	@Override
	public ATermAppl dataAllValuesFrom(ATermAppl ope, ATermAppl ce) {
		return ATermUtils.makeAllValues(ope, ce);
	}

	@Override
	public ATermAppl dataHasValue(ATermAppl ope, ATermAppl ni) {
		return ATermUtils.makeHasValue(ope, ni);
	}

	@Override
	public ATermAppl dataIntersectionOf(Set<ATermAppl> c) {
		return ATermUtils.makeAnd(ATermUtils.makeList(c));
	}

	@Override
	public ATermAppl dataUnionOf(Set<ATermAppl> c) {
		return ATermUtils.makeOr(ATermUtils.makeList(c));
	}
}
