/**
 * CollisionForce - part of Simon Greenwold's ParticleSystem library
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

public class CollisionForce extends Force {
	Particle a;

	public float minStrength;
	public float maxStrength;
	public float falloffPower;

	CollisionForce(Particle a) {
		super(a.system);
		this.a = a;
		falloffPower = 1.0f;
	}

	/*
	 * private float falloff(float dist) { if (falloffPower == 1.0f) { if (dist
	 * == 0) return 1; // just a speedup for the default case return 1.0f /
	 * dist; } return (float)Math.pow(dist, falloffPower); } private float
	 * invFalloff(float mult) { if (falloffPower == 1.0f) { return mult; // just
	 * a speedup for the default case } return (float)Math.pow(mult,
	 * 1.0/falloffPower); }
	 */

	public Particle[] getDependencyList() {
		Particle[] dependencies = new Particle[1];
		dependencies[0] = a;
		return dependencies;
	}

	public void calculateForce(double F0[], double v[], double x[]) {
		if (!active)
			return;
		Particle p;
		Particle particles[] = system.particles;
		float radius = a.collisionRadius;

		for (int i = 0; i < system.nParticles; i++) {
			p = particles[i];
			if (a == p)
				continue;

			if (p.fixed() || p.collisionForce == null
					|| !p.collisionForce.active
					|| a.collisionRadius + p.collisionRadius <= 0)
				continue;

			int ai = a.index * 3;
			int pi = p.index * 3;

			double Lx = x[ai] - x[pi];
			double Ly = x[ai + 1] - x[pi + 1];
			double Lz = x[ai + 2] - x[pi + 2];
			double distSq = Lx * Lx + Ly * Ly + Lz * Lz;
			float dist = (float) Math.sqrt(distSq);

			minStrength = a.minCollisionForce;
			maxStrength = a.maxCollisionForce;

			float distAdj = (dist - radius) - p.collisionRadius;

			if (distAdj > 0)
				continue;

			float ratio = -distAdj / (radius + p.collisionRadius);
			float finalStrength = minStrength
					+ (ratio * (maxStrength - minStrength));

			F[0] = -finalStrength * Lx / dist;
			F[1] = -finalStrength * Ly / dist;
			F[2] = -finalStrength * Lz / dist;

			F0[pi] += F[0];
			F0[pi + 1] += F[1];
			F0[pi + 2] += F[2];
		}
	}
}
