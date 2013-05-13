import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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

	private static class ResultsPerLine {
		private final Map<Integer, Set<String>> resultsBefore = new TreeMap<Integer, Set<String>>();
		private final Map<Integer, Set<String>> resultsAfter = new TreeMap<Integer, Set<String>>();

		
		public void addResultBefore(int lineNo, Set<String> livesBefore) {
			addResult(resultsBefore, lineNo, livesBefore);
		}
	
		public void addResultAfter(int lineNo, Set<String> livesAfter) {
			addResult(resultsAfter, lineNo, livesAfter);
		}

		private static void addResult(Map<Integer, Set<String>> rest,
				int lineNo, Set<String> live) {
			if (!rest.containsKey(lineNo)) {
				rest.put(lineNo, new TreeSet<String>(live));
			} else {
				rest.get(lineNo).addAll(live);
			}
		}

		public String formatResult(boolean before, boolean after) {
			StringBuilder result = new StringBuilder();
			for (int i : resultsBefore.keySet()) {
				String s = "line " + i + 
						(before? (" before: " + resultsBefore.get(i).toString()) : "") +
						(after? " (after: " + resultsAfter.get(i).toString() : "");
				result.append(s + "\n");
			}
			return result.toString();
		}
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
						ResultsPerLine results = new ResultsPerLine();
						for (Unit unit : b.getUnits()) {
							Stmt s = (Stmt) unit;
							SourceLnPosTag line = getLine(s);
							if (line != null) {
								results.addResultBefore(line.startLn(), mapGetName(analysis.getFlowBefore(s)));
								results.addResultAfter(line.endLn(), mapGetName(analysis.getFlowAfter(s)));
							}
						}
						System.err.println(results.formatResult(true, false));
					}
				}));
		System.err.println(System.getProperty("user.dir"));
		soot.Main.main(args);
	}

	private static SourceLnPosTag getLine(Stmt s) {
		SourceLnPosTag lineDesc = null;
		if (s.hasTag("SourceLnPosTag")) {
			lineDesc = ((SourceLnPosTag) s.getTag("SourceLnPosTag"));
		}
		return lineDesc;
	}
}
