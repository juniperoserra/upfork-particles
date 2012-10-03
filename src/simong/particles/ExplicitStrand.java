/*
 * Created on Nov 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package simong.particles;

/**
 * @author sgreenwo
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class ExplicitStrand extends ExplicitSpring {

	/**
     * 
     */
	public ExplicitStrand(ParticleSystem ps) {
		super(ps);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param a
	 * @param b
	 */
	public ExplicitStrand(Particle a, Particle b) {
		super(a, b);
		// TODO Auto-generated constructor stub
	}

	protected double effectiveStrength() {
		if (dist < restLength) {
			return 0;
		}
		return strength;
	}
}