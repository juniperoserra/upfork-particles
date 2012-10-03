/* -*- mode: jde; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/**
 * Particle - part of Simon Greenwold's ParticleSystem library
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

public class Particle implements ParticleConstants {

	public ParticleSystem system;

	public int age;

	public int index;

	public boolean fixed[] = new boolean[3];

	private double fixedPos[] = new double[3];

	public boolean visible = true;

	CollisionForce collisionForce = null;

	public float mass;

	public float collisionRadius;

	public float minCollisionForce;

	public float maxCollisionForce;

	public float hitRadius;

	public float force[] = new float[DIMENSIONS]; // x

	public float pos[] = new float[DIMENSIONS]; // x

	public float velocity[] = new float[DIMENSIONS]; // v

	public float screenX;
	public float screenY;

	boolean markedForRemoval;

	public Particle(Particle copy) {
		this(copy.system, (float) copy.pos[0], (float) copy.pos[1],
				(float) copy.pos[2]);
	}

	public Particle(ParticleSystem system) {
		this(system, 0, 0, 0);
	}

	public Particle(ParticleSystem system, float x, float y, float z) {
		unfix();

		this.system = system;

		for (int i = 0; i < DIMENSIONS; i++) {
			velocity[i] = 0.0f;
		}
		pos[0] = x;
		pos[1] = y;
		pos[2] = z;

		system.addParticle(this);
		init();
	}

	public void init() {
		if (system == null)
			return;

		hitRadius = system.defaultHitRadius;
		mass = system.defaultMass;
		if (collisionRadius == 0)
			collisionRadius = system.defaultCollisionRadius;
		if (minCollisionForce == 0)
			minCollisionForce = system.defaultMinCollisionForce;
		if (maxCollisionForce == 0)
			maxCollisionForce = system.defaultMaxCollisionForce;
		age = 0;
	}

	public boolean isHit(float x, float y) {
		return Util.abs(x - screenX) + Util.abs(y - screenY) <= hitRadius;
	}

	public boolean fixed() {
		return fixed[X] && fixed[Y] && fixed[Z];
	}

	public void fix() {
		fix(pos[X], pos[Y], pos[Z]);
	}

	public void fix(boolean fixed) {
		if (fixed) {
			fix();
		} else {
			unfix();
		}
	}

	public void fix(double x, double y, double z) {
		fix(X, x);
		fix(Y, y);
		fix(Z, z);
	}

	public void fix(int axis, double value) {
		if (axis > Z)
			return;
		fixedPos[axis] = value;
		fixed[axis] = true;
	}

	public void unfix() {
		fixed[X] = false;
		fixed[Y] = false;
		fixed[Z] = false;
	}

	public void unfix(int axis) {
		if (axis >= Z)
			return;
		fixed[axis] = false;
	}

	public void draw() {
		if (system == null)
			return;

		PApplet applet = system.getParent();

		applet.pushMatrix();
		applet.translate((float) pos[0], (float) pos[1], (float) pos[2]);
		applet.box(5, 5, 5);
		applet.popMatrix();
	}

	public void die() {
		markedForRemoval = true;
	}

	public Particle[] particlesWithin(float radius) {
		// float radSq = Util.sq(radius);
		int count = 0;
		Particle particles[] = system.particles;
		for (int i = 0; i < system.nParticles; i++) {
			if (particles[i] != this) {
				// if (distSq(pos, particles[i].pos) <= radSq) {
				// count++;
				// }
			}
		}
		Particle result[] = new Particle[count];
		count = 0;
		for (int i = 0; i < system.nParticles; i++) {
			if (particles[i] != this) {
				// if (distSq(pos, particles[i].pos) <= radSq) {
				// result[count] = particles[i];
				// count++;
				// }
			}
		}
		return result;
	}

	public void enableCollision() {
		if (collisionForce == null) {
			collisionForce = new CollisionForce(this);
		}
	}

	public void nuffle() {
	}

	public void enableCollision(float radius) {
		collisionRadius = radius;
		enableCollision();
	}

	public void disableCollision() {
		if (collisionForce != null) {
			collisionForce.active = false;
		}
	}

	public void setPos(float x, float y, float z) {
		pos[0] = x;
		pos[1] = y;
		pos[2] = z;
	}

	public void setVelocity(float dx, float dy, float dz) {
		velocity[0] = dx;
		velocity[1] = dy;
		velocity[2] = dz;
	}

}