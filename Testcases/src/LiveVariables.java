
public class LiveVariables {
	
	static public boolean c1;
	
	static public void m1(String s1, String s2, String s3) {
		System.out.println(s1);
		if (c1) {
			s3 = s2 + s1;
			// s1 dies here
			System.out.println(s2);
			// s2 dies here
		}
		System.out.println(s3);
		// s3 dies here
		s3 = "Hello World";
	}

}
