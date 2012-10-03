/**
 * Magnet - part of Simon Greenwold's ParticleSystem library
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

import java.util.Hashtable;

import simong.Util;

public class Magnet extends Force {
	Particle a;

	public float minForce;
	public float maxForce;
	public float strength;
	public float falloffPower;

	private float falloffStrength;

	Hashtable ignoreTypes = new Hashtable();

	public Magnet(Particle a) {
		super(a.system);
		this.a = a;
		maxForce = 10;
		minForce = 0.001f;
		strength = 10;
		falloffPower = 0.5f;
	}

	void ignoreType(Particle particleType) {
		ignoreTypes.put(particleType.getClass(), particleType.getClass());
	}

	private float falloff(double dist) {
		if (falloffPower == 0.5f) {
			return (float) java.lang.Math.sqrt(dist); // just a speedup for the
														// default case
		}
		return (float) Math.pow(dist, falloffPower);
	}

	private double invFalloff(double mult) {
		if (falloffPower == 0.5f) {
			return mult * mult; // just a speedup for the default case
		}
		return (float) Math.pow(mult, 1.0 / falloffPower);
	}

	public Particle[] getDependencyList() {
		Particle[] dependencies = new Particle[1];
		dependencies[0] = a;
		return dependencies;
	}

	public void calculateForce(double F0[], double v[], double x[]) {
		Particle p;
		Particle particles[] = system.particles;

		double maxDistSq = invFalloff(strength / minForce);
		maxDistSq = maxDistSq * maxDistSq;

		for (int i = 0; i < system.nParticles; i++) {
			p = particles[i];
			if (ignoreTypes.contains(p.getClass())) {
				continue;
			}

			int ai = a.index * 3;
			int pi = p.index * 3;

			double Lx = x[ai] - x[pi];
			double Ly = x[ai + 1] - x[pi + 1];
			double Lz = x[ai + 2] - x[pi + 2];
			double distSq = Lx * Lx + Ly * Ly + Lz * Lz;

			// probably we should have a way to give a particle back
			// the same force we gave it last time if the distance is zero.
			if (distSq == 0 || distSq > maxDistSq) {
				continue;
			}

			double dist = java.lang.Math.sqrt(distSq);

			falloffStrength = Util.constrain(strength / (falloff(dist)), -Util
					.abs(maxForce), Util.abs(maxForce));
			F[0] = falloffStrength * (Lx / dist);
			F[1] = falloffStrength * (Ly / dist);
			F[2] = falloffStrength * (Lz / dist);

			F0[pi] += F[0];
			F0[pi + 1] += F[1];
			F0[pi + 2] += F[2];
		}
	}

}
