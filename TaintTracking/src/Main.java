import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import java.util.List;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.PackManager;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.tagkit.SourceLnPosTag;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class Main {

	private static final MethodTyping mtyping = new MethodTyping() {

		@Override
		public TypeSpec getType(String methodName) {
			final TypeSpec<Sec> ignore = new TypeSpec<>(Collections.EMPTY_LIST,
					Sec.LOW);
			final TypeSpec<Sec> ignore1 = new TypeSpec<>(
					Collections.singletonList(Sec.LOW), Sec.LOW);
			final TypeSpec<Sec> ignore2 = new TypeSpec<>(
					Collections.unmodifiableList(Arrays.asList(new Sec[] {
							Sec.LOW, Sec.LOW })), Sec.LOW);
			switch (methodName) {
			case "println":
				return ignore1;
			case "<init>":
				return ignore1;
			case "append":
				return ignore1;
			case "toString":
				return ignore1;
			case "strAppend":
				return ignore2;
			case "secretSource":
				return new TypeSpec<Sec>(Collections.EMPTY_LIST, Sec.HIGH);
			case "publicSource":
				return new TypeSpec<Sec>(Collections.EMPTY_LIST, Sec.LOW);
			case "confidentialSink":
				return new TypeSpec<Sec>(Collections.singletonList(Sec.HIGH),
						Sec.LOW);
			case "publicSink":
				return new TypeSpec<Sec>(Collections.singletonList(Sec.LOW),
						Sec.LOW);
			default:
				throw new IllegalArgumentException("Type for method "
						+ methodName + " not found.");
			}
		}

	};

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PackManager.v().getPack("jtp")
				.add(new Transform("jtp.taintracking", new BodyTransformer() {

					@Override
					protected void internalTransform(Body b, String phaseName,
							Map options) {
						UnitGraph g = new BriefUnitGraph(b);
						TaintTracking analysis = new TaintTracking(mtyping, g);
						for (Unit u : b.getUnits()) {
							Stmt s = (Stmt) u;
							System.out.println("line " + getLine(s) + ": "
									+ analysis.getFlowBefore(s));
							typeCheck(s, analysis);
						}
					}
				}));
		// TODO Auto-generated method stub
		soot.Main.main(args);
	}

	private static void typeCheck(Stmt s, TaintTracking analysis) {
		// we only type check method calls here.
		if (s.containsInvokeExpr()) {
			InvokeExpr exp = s.getInvokeExpr();
			TypeSpec<Sec> methodType = mtyping.getType(exp.getMethod()
					.getName());
			List<Sec> argTypes = getArgTypes(exp, analysis.getFlowBefore(s));
			TypeSpec.ResultType<Sec> result = methodType.apply(Sec.CMP,
					argTypes);
			if (!result.success()) {
				System.err.println("Type error (line " + getLine(s) + "): "
						+ result.getError());
			}
		}
	}

	private static List<Sec> getArgTypes(InvokeExpr exp, Set<Local> analysis) {
		List<Sec> result = new ArrayList<Sec>();
		for (Value v : exp.getArgs()) {
			if (analysis.contains(v)) {
				result.add(Sec.HIGH);
			} else {
				result.add(Sec.LOW);
			}
		}
		return result;
	}

	private static String getLine(Stmt s) {
		SourceLnPosTag lineDesc = null;
		if (s.hasTag("SourceLnPosTag")) {
			lineDesc = ((SourceLnPosTag) s.getTag("SourceLnPosTag"));
		}
		return lineDesc != null ? "" + (lineDesc.startLn()) : "<unknown>";
	}

}
