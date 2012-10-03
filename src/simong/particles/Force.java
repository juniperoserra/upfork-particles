/**
 * Force - part of Simon Greenwold's ParticleSystem library
 * An extension for the Processing project - http://processing.org
 * <p/>
 * Copyright (c) 2004 Simon Greenwold, Created: Feb 11, 2004
 * Updated for Processing 0070 by Ben Fry in September 2004
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General
 * Public License along with the Processing project; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA  02111-1307  USA
 */

package simong.particles;

public abstract class Force implements ParticleConstants {
	boolean active;

	public boolean visible = true;

	ParticleSystem system;

	public Force(ParticleSystem system) {
		this.system = system;
		active = true;
		F = new double[3];

		system.addForce(this);
	}

	// Explicit forces can be integrated into the system
	// with a simple ODE solver. RK45 in our case. All an
	// explicit force needs to define is a force calculation
	// that can be applied into a global force matrix.
	public boolean isExplicit() {
		return true;
	}

	public void draw() {
	}

	public double F[];

	abstract public void calculateForce(double F0[], double v[], double x[]);

	public void preCalcCache() {
	}

	public void preStepCache(double h) {
	}

	public Particle[] getDependencyList() {
		return new Particle[0];
	}

}
