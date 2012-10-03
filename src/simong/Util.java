/*
 * Created on Feb 20, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package simong;

/**
 * @author simong
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class Util {

	/**
     *
     */
	public Util() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static final float abs(float n) {
		return (n < 0) ? -n : n;
	}

	public static final int abs(int n) {
		return (n < 0) ? -n : n;
	}

	public static final float sq(float a) {
		return a * a;
	}

	public static final float sqrt(float a) {
		return (float) Math.sqrt(a);
	}

	public static final float pow(float a, float b) {
		return (float) Math.pow(a, b);
	}

	public static final float max(float a, float b) {
		return Math.max(a, b);
	}

	public static final float max(float a, float b, float c) {
		return Math.max(a, Math.max(b, c));
	}

	public static final float min(float a, float b) {
		return Math.min(a, b);
	}

	public static final float min(float a, float b, float c) {
		return Math.min(a, Math.min(b, c));
	}

	public static final float constrain(float amt, float low, float high) {
		return (amt < low) ? low : ((amt > high) ? high : amt);
	}

	public static final int max(int a, int b) {
		return (a > b) ? a : b;
	}

	public static final int max(int a, int b, int c) {
		return (a > b) ? ((a > c) ? a : c) : b;
	}

	public static final int min(int a, int b) {
		return (a < b) ? a : b;
	}

	public static final int min(int a, int b, int c) {
		return (a < b) ? ((a < c) ? a : c) : b;
	}

	public static final int constrain(int amt, int low, int high) {
		return (amt < low) ? low : ((amt > high) ? high : amt);
	}

	public static final float sin(float angle) {
		return (float) Math.sin(angle);
	}

	public static final float cos(float angle) {
		return (float) Math.cos(angle);
	}

	public static final float tan(float angle) {
		return (float) Math.tan(angle);
	}

	public static final float atan2(float a, float b) {
		return (float) Math.atan2(a, b);
	}

	public static final float ceil(float what) {
		return (float) Math.ceil(what);
	}

	public static final float floor(float what) {
		return (float) Math.floor(what);
	}

	public static final float round(float what) {
		return Math.round(what);
	}

	public static final float distSq(float x1, float y1, float x2, float y2) {
		return sq(x2 - x1) + sq(y2 - y1);
	}

	public static final float distSq(float p1[], float p2[]) {
		return sq(p2[0] - p1[0]) + sq(p2[1] - p1[1]) + sq(p2[2] - p1[2]);
	}

	public static final float distSq(float x1, float y1, float z1, float x2,
			float y2, float z2) {
		return sq(x2 - x1) + sq(y2 - y1) + sq(z2 - z1);
	}

	public static final float dist(float p1[], float p2[]) {
		return sqrt(sq(p2[0] - p1[0]) + sq(p2[1] - p1[1]) + sq(p2[2] - p1[2]));
	}

	public static final float dist(float x1, float y1, float x2, float y2) {
		return sqrt(sq(x2 - x1) + sq(y2 - y1));
	}

	public static final float dist(float x1, float y1, float z1, float x2,
			float y2, float z2) {
		return sqrt(sq(x2 - x1) + sq(y2 - y1) + sq(z2 - z1));
	}

	public static final float random(float howbig) {
		return (float) Math.random() * howbig;
	}

	public static final float random(float howsmall, float howbig) {
		float diff = howbig - howsmall;
		return howsmall + (float) Math.random() * diff;
	}

	public static final void zero(double mat[][]) {
		mat[0][0] = 0.0;
		mat[0][1] = 0.0;
		mat[0][2] = 0.0;
		mat[1][0] = 0.0;
		mat[1][1] = 0.0;
		mat[1][2] = 0.0;
		mat[2][0] = 0.0;
		mat[2][1] = 0.0;
		mat[2][2] = 0.0;
	}

	public static final void identity(double mat[][]) {
		mat[0][0] = 1.0;
		mat[0][1] = 0.0;
		mat[0][2] = 0.0;
		mat[1][0] = 0.0;
		mat[1][1] = 1.0;
		mat[1][2] = 0.0;
		mat[2][0] = 0.0;
		mat[2][1] = 0.0;
		mat[2][2] = 1.0;
	}

}
