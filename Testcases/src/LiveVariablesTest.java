
public class LiveVariablesTest {
	
	
	static public void m1(boolean c1, String s1, String s2, String s3) {
		System.out.println(s1);
		if (c1) {
			s3 = s2 + s1;
			// s1 dies here
			System.out.println(s2);
			// s2 dies here
		}
		System.out.println(s3);
		// s3 dies here
		System.out.println("Hello World");
		s3 = "Hello World";
	}

	static public void main(String[] args) {
		m1(args.length == 1, "Hello", "World", "!");
	}
	
}
