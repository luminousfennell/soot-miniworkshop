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

	private static class LiveTracker {
		private final Set<String> last = new HashSet<String>();

		public LiveTracker(Collection<String> init) {
			last.addAll(init);
		}

		public Set<String> update(Set<String> current) {
			Set<String> diff = new HashSet<String>(current);
			diff.removeAll(last);

			last.clear();
			last.addAll(current);

			return diff;
		}
	}

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
						LiveTracker deads = new LiveTracker(interestingLocals);
						LiveTracker lives = new LiveTracker(
								Collections.EMPTY_SET);
						for (Unit unit : b.getUnits()) {
//							System.err.println(unit.toString());
							Stmt s = (Stmt) unit;
							// System.err.println(analysis.getFlowBefore(s));
							System.err.println("(before line " + getLine(s) + ")" + mapGetName(analysis.getFlowBefore(s)).toString());
							System.err.println("(after line " + getLine(s) + ")" + mapGetName(analysis.getFlowAfter(s)).toString());
							Set<String> currentLive = mapGetName(analysis.getFlowAfter(s));
							Set<String> born = lives.update(currentLive);
							if (! born.isEmpty()) System.err.println(formatChange(born, "born", s));

							Set<String> currentDead = new HashSet(
									interestingLocals);
							currentDead.removeAll(mapGetName(analysis
									.getFlowBefore(s)));
							Set<String> killed = deads.update(currentDead);
							if (! killed.isEmpty()) System.err.println(formatChange(killed, "dying", s));
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

	private static String formatChange(Collection<String> change,
			String activity, Stmt s) {
		String lineDesc = getLine(s);
		StringBuilder result = new StringBuilder(activity + "(line " + lineDesc
				+ "):");
		for (String l : change) {
			result.append(l);
		}

		return result.toString();
	}
}
