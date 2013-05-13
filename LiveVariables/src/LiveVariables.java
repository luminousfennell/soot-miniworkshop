import java.util.HashSet;
import java.util.Set;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;


public class LiveVariables extends BackwardFlowAnalysis<Unit, Set<Local>> {

	public LiveVariables(DirectedGraph<Unit> graph) {
		super(graph);
		this.doAnalysis();
	}

	@Override
	protected void flowThrough(Set<Local> in, Unit d, Set<Local> out) {
		out.clear();
		out.addAll(in);
		for (ValueBox b : d.getDefBoxes()) {
			Value v = b.getValue();
			if (v instanceof Local) {
				out.remove(v);
			}
		}
		for (ValueBox b : d.getUseBoxes()) {
			Value v = b.getValue();
			if (v instanceof Local) {
				out.add((Local) v);
			}
		}
	}

	@Override
	protected Set<Local> newInitialFlow() {
		return new HashSet<Local>();
	}

	@Override
	protected Set<Local> entryInitialFlow() {
		return new HashSet<Local>();
	}

	@Override
	protected void merge(Set<Local> in1, Set<Local> in2, Set<Local> out) {
		out.clear();
		out.addAll(in1);
		out.addAll(in2);
	}

	@Override
	protected void copy(Set<Local> source, Set<Local> dest) {
		dest.clear();
		dest.addAll(source);
	}

}
