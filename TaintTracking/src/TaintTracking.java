import java.util.HashSet;
import java.util.Set;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.Stmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;


/**
 * This analysis tracks the high-security variables.
 * @author fennell
 *
 */
public class TaintTracking extends ForwardFlowAnalysis<Unit, Set<Local>> {
	private final MethodTyping<Sec> typing;

	public TaintTracking(MethodTyping<Sec> typing, DirectedGraph<Unit> graph) {
		super(graph);
		this.typing = typing;
		doAnalysis();
	}

	@Override
	protected void flowThrough(Set<Local> in, Unit d, Set<Local> out) {
		Stmt s = (Stmt) d;
		Sec useLevel = Sec.LOW;
		
		if(s.containsInvokeExpr()) {
			// a method call; it will define the security level of an assignment
			String methodName = s.getInvokeExpr().getMethod().getName();
			if (typing.getType(methodName).getOuput() == Sec.HIGH) {
				useLevel = Sec.HIGH;
			}
		} else {
			// in other cases just lub the used values
			for (ValueBox b : d.getUseBoxes()) {
				Value v = b.getValue();
				if (v instanceof Local && in.contains(v)) {
					useLevel = Sec.HIGH;
				}
			}
		}
		
		// First, Assume that high-security information is overwritten
		for (ValueBox b : d.getDefBoxes()) {
			Value v = b.getValue();
			out.remove(v);
		}
		
		// If we have high dependencies, add all defined locals to the set
		if (useLevel == Sec.HIGH) {
			for (ValueBox b : d.getDefBoxes()) {
				Value v = b.getValue();
				if (v instanceof Local) {
					out.add((Local)v);
				}
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
		copy(in1, out);
		out.addAll(in2);
	}

	@Override
	protected void copy(Set<Local> source, Set<Local> dest) {
		dest.clear();
		dest.addAll(source);
		
	}

}
