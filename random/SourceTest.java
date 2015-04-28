public class SourceTest {
	int sneakyGlobal = 54;
	String sneakyGlobalString = "adsf";
	public SourceTest() {
	}
	// doesn't use string global
	static int[] calculateValue(String str, int[] x, int y, boolean doPlus) {
		int r = 0;
		int j;
		int l, m, n, o, p;
		int q, d = 93, w = 10;
		String q1, d1 = "93", w2 = "10";
		Point q2 = new Point(q, d), d2;
		if (doPlus) {
			r = x + y;
		} else {
			r = x - y;
		}
		return r + sneakyGlobal;
	}
	// this uses the string global
	public int calculateValue(int x, int y, boolean doPlus, boolean parameterIWIllNotUSe) {
		int r = 0;
		if (doPlus) {
			r = x + y;
		} else {
			r = x - y;
		}
		sneakyGlobalString = "asdfadsf";
		String w = new String();
		w = new String("asdf");
		w = String.valueOf(2);
		return r + sneakyGlobal;
	}
	
	public int calc1(){}
	public int calc2(){}

}

