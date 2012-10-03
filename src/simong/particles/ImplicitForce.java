/*
 * Created on Feb 20, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package simong.particles;

/**
 * @author simong
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class ImplicitForce extends Force {

	public double Jx[][]; // Jacobian wrt. x (position)

	public double Jv[][]; // Jacobian wrt. v (velocity)

	public double B[][]; // = - h * Jx - h^2 * Jv (cache result used in conj.

	// gad.)

	public ImplicitForce(ParticleSystem system) {
		super(system);
		Jx = new double[3][3];
		Jv = new double[3][3];
		B = new double[3][3];
	}

	public boolean isExplicit() {
		return false;
	}

	abstract public void calculateJacobians();

	abstract public void multiplyAPiece(double src[], double dst[]);

	abstract public void multiplyDfDx(double src[], double dst[]);

	// Not used by springs because the application of dfdv is simply adding in
	// damping.
	// but kept around as a reminder of its general necessity.
	abstract public void multiplyDfDv(double src[], double dst[]);
}
