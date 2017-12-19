package cz.cvut.kbss.owl2query.model.pellet;

import java.util.Set;

import aterm.ATermAppl;
import cz.cvut.kbss.owl2query.model.KBOperation;
import cz.cvut.kbss.owl2query.model.SizeEstimate;

class PelletSizeEstimate implements SizeEstimate<ATermAppl> {

	final org.mindswap.pellet.utils.SizeEstimate se;
	final org.mindswap.pellet.KnowledgeBase kb;

	PelletSizeEstimate(final PelletOWL2Ontology o) {
		this.kb = o.getKnowledgeBase();
		this.se = kb.getSizeEstimate();
	}

	@Override
	public double avg(ATermAppl pred) {
		return se.avg(pred);
	}

	@Override
	public double avgClassesPerInstance(boolean direct) {
		return se.avgClassesPerInstance(direct);
	}

	@Override
	public double avgDifferentsPerInstance() {
		return se.avgDifferentsPerInstance();
	}

	@Override
	public double avgInstancesPerClass(boolean direct) {
		return se.avgInstancesPerClass(direct);
	}

	@Override
	public double avgPairsPerProperty() {
		return se.avgPairsPerProperty();
	}

	@Override
	public double avgSamesPerInstance() {
		return se.avgSamesPerInstance();
	}

	@Override
	public double avgSubjectsPerProperty() {
		return se.avgSubjectsPerProperty();
	}

	@Override
	public int classesPerInstance(ATermAppl instance, boolean direct) {
		return se.classesPerInstance(instance, direct);
	}

	@Override
	public void compute(Set<ATermAppl> concepts, Set<ATermAppl> properties) {
		se.compute(concepts, properties);
	}

	@Override
	public void computeAll() {
		se.computeAll();
	}

	@Override
	public double differents(ATermAppl dfLHS) {
		return se.differents(dfLHS);
	}

	@Override
	public int getClassCount() {
		return se.getClassCount();
	}

	@Override
	public long getCost(KBOperation operation) {
		return se.getCost(org.mindswap.pellet.utils.KBOperation
				.valueOf(operation.name()));
	}

	@Override
	public int getInstanceCount() {
		return se.getInstanceCount();
	}

	@Override
	public boolean isComputed(ATermAppl predicate) {
		return se.isComputed(predicate);
	}

	@Override
	public double sames(ATermAppl saLHS) {
		return se.sames(saLHS);
	}

	@Override
	public int size(ATermAppl pred) {
		return se.size(pred);
	}

	@Override
	public double avgEquivProperties() {
		return se.avgEquivProperties();
	}

	@Override
	public double avgSubProperties(boolean direct) {
		return se.avgSubProperties(direct);
	}

	@Override
	public double avgSuperProperties(boolean direct) {
		return se.avgSuperProperties(direct);
	}

	@Override
	public double equivProperties(ATermAppl spLHS) {
		return se.equivProperties(spLHS);
	}

	@Override
	public double superProperties(ATermAppl spLHS, boolean direct) {
		return se.superProperties(spLHS, direct);
	}

	@Override
	public double avgEquivClasses() {
		return se.avgEquivClasses();
	}

	@Override
	public double avgSubClasses(boolean direct) {
		return se.avgSubClasses(direct);
	}

	@Override
	public double avgSuperClasses(boolean direct) {
		return se.avgSuperClasses(direct);
	}

	@Override
	public double equivClasses(ATermAppl clazzLHS) {
		return se.equivClasses(clazzLHS);
	}

	@Override
	public double superClasses(ATermAppl clazzLHS, boolean direct) {
		return se.superClasses(clazzLHS, direct);
	}

	@Override
	public int getDataPropertyCount() {
		return se.getDataPropertyCount();
	}

	@Override
	public int getObjectPropertyCount() {
		return se.getObjectPropertyCount();
	}
}
