import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.PackManager;
import soot.Transform;
import soot.Unit;
import soot.jimple.Stmt;
import soot.tagkit.SourceLnPosTag;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.FlowAnalysis;

public class Main {

	private final static Set<String> interestingLocals = Collections
			.unmodifiableSet(new HashSet<String>(Arrays.asList(new String[] {
					"s1", "s2", "s3" })));

	private static Set<String> mapGetName(Set<Local> ls) {
		Set<String> result = new HashSet<String>();
		for (Local l : ls) {
			String varname = l.getName();
			if (interestingLocals.contains(varname)) {
				result.add(l.getName());
			}
		}
		return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PackManager.v().getPack("jtp")
				.add(new Transform("jtp.myAnalysis", new BodyTransformer() {

					@Override
					protected void internalTransform(Body b, String phaseName,
							Map options) {
						UnitGraph g = new BriefUnitGraph(b);
						FlowAnalysis<Unit, Set<Local>> analysis = new LiveVariables(
								g);
						for (Unit unit : b.getUnits()) {
							Stmt s = (Stmt) unit;
							System.err.println("(before line " + getLine(s) + ")" + mapGetName(analysis.getFlowBefore(s)).toString());
							System.err.println("(after line " + getLine(s) + ")" + mapGetName(analysis.getFlowAfter(s)).toString());
						}
					}
				}));
		System.err.println(System.getProperty("user.dir"));
		soot.Main.main(args);
	}
	
	private static String getLine(Stmt s) {
		String lineDesc = "<unknown>";
		if (s.hasTag("SourceLnPosTag")) {
			lineDesc = ""
					+ ((SourceLnPosTag) s.getTag("SourceLnPosTag")).startLn();
		}
		return lineDesc;
	}
}
