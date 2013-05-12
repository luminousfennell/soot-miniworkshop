import java.util.Arrays;
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

	private final static Set<String> interestingLocals = 
			Collections.unmodifiableSet(
					new HashSet<String>(Arrays.asList(new String[] {
					"s1", "s2", "s3" 
					})));
	
	private static class DeadSet {
		private Set<String> deads = new HashSet<String>(interestingLocals);
		public Set<String> update(Set<String> ls) {
			Set<String> currentDead = new HashSet<String>(interestingLocals);
			currentDead.removeAll(ls);
			deads.addAll(currentDead);
			currentDead.retainAll(deads);
			return currentDead;
		}
	}

	private static Set<String> mapGetName(Set<Local> ls) {
		Set<String> result = new HashSet<String>();
		for (Local l : ls) {
			result.add(l.getName());
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
						FlowAnalysis<Unit, Set<Local>> analysis = new LiveVariables(g);
						DeadSet deads = new DeadSet();
						for (Unit unit : b.getUnits()) {
							Stmt s = (Stmt) unit;
						    Set<String> killed = deads.update(mapGetName(analysis.getFlowBefore(s)));
							for(String l : killed) {
								String lineDesc = "<unknown>";
								if(s.hasTag("SourceLnPosTag")) {
									lineDesc = 
									  "" + ((SourceLnPosTag) s.getTag("SourceLnPosTag")) .startLn();
								}
							    System.out.println("Variable " + l + "is dying in line " + lineDesc);
							}
						}
					}
				}));

	}

}
