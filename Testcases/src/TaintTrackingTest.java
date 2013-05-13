public class TaintTrackingTest {

	// type: void -> String^H
	public static String secretSource() {
		return "Secret!";
	}

	// type: void -> String^L
	public static String publicSource() {
		return "Blablabla!";
	}
	
	// type: String^H -> void
	public static void confidentialSink(String s) {
		System.out.println("XXXX");
	}
	
	// type: String^L -> void
	public static void publicSink(String s) {
		System.out.println(s);
	}
	
	// type: (String^B1, String^B2) -> String^B1+B2
	public static String strAppend(String s1, String s2) {
		return s1 + s2;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// type: String^H
		String h = secretSource();
		// type: String^L
		String l = publicSource();
		
		publicSink(l); // ok
		confidentialSink(h); // ok
		
		publicSink(h); // error
		confidentialSink(l); // ok
		
		publicSink(strAppend(l, h)); // error
	}

}
