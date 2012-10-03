/* -*- mode: jde; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/**
 * Spring - part of Simon Greenwold's ParticleSystem library
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

import processing.core.PApplet;
import simong.Util;

public class Spring extends ImplicitForce {

	public Particle a;

	public Particle b;

	public float restLength; // Rest length

	public float strength; // Spring constant

	public float damping; // Damping constant

	double dist;

	double dx[] = new double[3];

	double nx[] = new double[3];

	private double ds[] = new double[3];

	private double addJ[] = new double[3];

	public Spring(ParticleSystem system) {
		super(system);
		strength = system.defaultSpringStrength;
		damping = system.defaultSpringDamping;
		restLength = system.defaultSpringRestLength;
	}

	public Spring(Particle a, Particle b) {
		this(a.system);
		this.a = a;
		this.b = b;
	}

	public Particle[] getDependencyList() {
		Particle[] dependencies = new Particle[2];
		dependencies[0] = a;
		dependencies[1] = b;
		return dependencies;
	}

	public void draw() {
		if (system == null)
			return;
		PApplet parent = system.getParent();
		parent.line((float) a.pos[0], (float) a.pos[1], (float) a.pos[2],
				(float) b.pos[0], (float) b.pos[1], (float) b.pos[2]);
	}

	public void setRestToActualLength() {
		preCalcCache();
		restLength = (float) dist;
	}

	protected double effectiveStrength() {
		return strength;
	}

	public void preCalcCache() {
		dx[0] = a.pos[0] - b.pos[0];
		dx[1] = a.pos[1] - b.pos[1];
		dx[2] = a.pos[2] - b.pos[2];
		dist = java.lang.Math.sqrt(dx[0] * dx[0] + dx[1] * dx[1] + dx[2]
				* dx[2]);

	}

	public void calculateForce(double F0[], double v[], double x[]) {
		int ai = a.index * 3;
		int bi = b.index * 3;

		// Not necessary in any case. Only evaluated once per step.
		// dx[0] = x[ai] - x[bi];
		// dx[1] = x[ai+1] - x[bi+1];
		// dx[2] = x[ai+2] - x[bi+2];
		// dist = java.lang.Math.sqrt(dx[0] * dx[0] + dx[1] * dx[1] + dx[2] *
		// dx[2]);

		if (dist != 0) {
			nx[0] = dx[0] / dist;
			nx[1] = dx[1] / dist;
			nx[2] = dx[2] / dist;
		} else {
			nx[0] = 0;
			nx[1] = 0;
			nx[2] = 0;
		}

		if (dist == 0) {
			Util.zero(Jx);
			Util.zero(Jv);
			Util.zero(B);
			F[0] = 0;
			F[1] = 0;
			F[2] = 0;
		} else {
			F[0] = -(effectiveStrength() * (dist - restLength) + damping
					* (v[ai] - v[bi]) * nx[0])
					* nx[0];
			F[1] = -(effectiveStrength() * (dist - restLength) + damping
					* (v[ai + 1] - v[bi + 1]) * nx[1])
					* nx[1];
			F[2] = -(effectiveStrength() * (dist - restLength) + damping
					* (v[ai + 2] - v[bi + 2]) * nx[2])
					* nx[2];

			F0[ai] += F[0];
			F0[ai + 1] += F[1];
			F0[ai + 2] += F[2];

			F0[bi] -= F[0];
			F0[bi + 1] -= F[1];
			F0[bi + 2] -= F[2];
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see simong.particles.Force#calculateJacobians() h = timeStep
	 */
	public void calculateJacobians() {
		/*
		 * We calculate spring Jx = | ? ? ? | | ? ? ? | | ? ? ? |
		 */

		if (dist == 0)
			return;

		// begin with outer product of nx.
		Jx[0][0] = nx[0] * nx[0];
		Jx[0][1] = nx[0] * nx[1];
		Jx[0][2] = nx[0] * nx[2];
		Jx[1][0] = nx[1] * nx[0];
		Jx[1][1] = nx[1] * nx[1];
		Jx[1][2] = nx[1] * nx[2];
		Jx[2][0] = nx[2] * nx[0];
		Jx[2][1] = nx[2] * nx[1];
		Jx[2][2] = nx[2] * nx[2];

		// Matrix dxtdx = new DenseMatrix(3,3);
		// dxtdx.rank1(dx); // Outer product dx*dt
		// double len = dx.norm(Vector.Norm.Two);
		// if (len != 0.0) {
		// len = 1.0/len;
		// }
		// dxtdx.scale(len*len);

		double len;
		if (dist != 0.0) {
			len = 1.0 / dist;
		} else {
			len = 0;
		}

		// Jx = (nxtnx + (I - nxtnx)*(1 - R / L) ) * ks
		double mul = restLength * len - 1;
		Jx[0][0] = (Jx[0][0] + (Jx[0][0] - 1) * mul) * effectiveStrength();
		Jx[0][1] = (Jx[0][1] + Jx[0][1] * mul) * effectiveStrength();
		Jx[0][2] = (Jx[0][2] + Jx[0][2] * mul) * effectiveStrength();
		Jx[1][0] = (Jx[1][0] + Jx[1][0] * mul) * effectiveStrength();
		Jx[1][1] = (Jx[1][1] + (Jx[1][1] - 1) * mul) * effectiveStrength();
		Jx[1][2] = (Jx[1][2] + Jx[1][2] * mul) * effectiveStrength();
		Jx[2][0] = (Jx[2][0] + Jx[2][0] * mul) * effectiveStrength();
		Jx[2][1] = (Jx[2][1] + Jx[2][1] * mul) * effectiveStrength();
		Jx[2][2] = (Jx[2][2] + (Jx[2][2] - 1) * mul) * effectiveStrength();

		// Jx.addDiagonal(1.0);
		// Jx.add(-1.0, dxtdx);
		// Jx.scale(1 - restLength*len);

		// Jx.add(dxtdx);
		// Jx.scale(strength);

		Jv[0][0] = damping;
		Jv[1][1] = damping;
		Jv[2][2] = damping;
	}

	public void preStepCache(double h) {
		// B = - h * Jv - h^2 * Jx (cache result used in conj. gad.)
		double hsq = h * h;
		B[0][0] = -hsq * Jx[0][0] - h * Jv[0][0];
		B[0][1] = -hsq * Jx[0][1];
		B[0][2] = -hsq * Jx[0][2];
		B[1][0] = -hsq * Jx[1][0];
		B[1][1] = -hsq * Jx[1][1] - h * Jv[1][1];
		B[1][2] = -hsq * Jx[1][2];
		B[2][0] = -hsq * Jx[2][0];
		B[2][1] = -hsq * Jx[2][1];
		B[2][2] = -hsq * Jx[2][2] - h * Jv[2][2];
	}

	public void multiplyAPiece(double src[], double dst[]) {
		int ai = 0;
		int bi;

		ai = a.index * 3;
		bi = b.index * 3;
		ds[0] = src[ai] - src[bi];
		ds[1] = src[ai + 1] - src[bi + 1];
		ds[2] = src[ai + 2] - src[bi + 2];

		addJ[0] = B[0][0] * ds[0] + B[0][1] * ds[1] + B[0][2] * ds[2];
		addJ[1] = B[1][0] * ds[0] + B[1][1] * ds[1] + B[1][2] * ds[2];
		addJ[2] = B[2][0] * ds[0] + B[2][1] * ds[1] + B[2][2] * ds[2];

		dst[ai] -= addJ[0];
		dst[ai + 1] -= addJ[1];
		dst[ai + 2] -= addJ[2];

		dst[bi] += addJ[0];
		dst[bi + 1] += addJ[1];
		dst[bi + 2] += addJ[2];
	}

	public void multiplyDfDx(double src[], double dst[]) {
		int ai, bi;

		ai = a.index * 3;
		bi = b.index * 3;
		ds[0] = src[ai] - src[bi];
		ds[1] = src[ai + 1] - src[bi + 1];
		ds[2] = src[ai + 2] - src[bi + 2];

		addJ[0] = Jx[0][0] * ds[0] + Jx[0][1] * ds[1] + Jx[0][2] * ds[2];
		addJ[1] = Jx[1][0] * ds[0] + Jx[1][1] * ds[1] + Jx[1][2] * ds[2];
		addJ[2] = Jx[2][0] * ds[0] + Jx[2][1] * ds[1] + Jx[2][2] * ds[2];

		dst[ai] -= addJ[0];
		dst[ai + 1] -= addJ[1];
		dst[ai + 2] -= addJ[2];

		dst[bi] += addJ[0];
		dst[bi + 1] += addJ[1];
		dst[bi + 2] += addJ[2];
	}

	public void multiplyDfDv(double src[], double dst[]) {
		// should be just adding damping. Applied as part of calculation of B.
	}

}
