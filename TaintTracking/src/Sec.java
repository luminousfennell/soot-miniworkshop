import java.util.Comparator;


public enum Sec {
	LOW,
	HIGH;
	
	public Sec lub(Sec other) {
		if (this == HIGH || other == HIGH) {
			return HIGH;
		} else {
			return LOW;
		}
	}
	
	public static final Comparator<Sec> CMP = new Comparator<Sec>() {

		@Override
		public int compare(Sec o1, Sec o2) {
			return Integer.compare(o1.ordinal(), o2.ordinal());
		}
	};
}
