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

public class ExplicitSpring extends Force {

	ParticleSystem system;

	public Particle a;

	public Particle b;

	public float restLength; // Rest length

	public float strength; // Spring constant

	public float damping; // Damping constant

	double dist;

	double dx[] = new double[3];

	double nx[] = new double[3];

	public ExplicitSpring(ParticleSystem system) {
		super(system);
		strength = system.defaultSpringStrength;
		damping = system.defaultSpringDamping;
		restLength = system.defaultSpringRestLength;
	}

	public ExplicitSpring(Particle a, Particle b) {
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

	public void setRestToActualLength() {
		preCalcCache();
		restLength = (float) dist;
	}

	protected double effectiveStrength() {
		return strength;
	}

	public void draw() {
		if (system == null)
			return;
		PApplet parent = system.getParent();
		parent.line((float) a.pos[0], (float) a.pos[1], (float) a.pos[2],
				(float) b.pos[0], (float) b.pos[1], (float) b.pos[2]);
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

		// This isn't helping somewhat with the RK integration.
		dx[0] = x[ai] - x[bi];
		dx[1] = x[ai + 1] - x[bi + 1];
		dx[2] = x[ai + 2] - x[bi + 2];
		dist = java.lang.Math.sqrt(dx[0] * dx[0] + dx[1] * dx[1] + dx[2]
				* dx[2]);

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

}
