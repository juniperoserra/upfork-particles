/**
 * ParticleSystem - part of Simon Greenwold's ParticleSystem library
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

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import processing.core.PApplet;
import processing.core.PConstants;

public class ParticleSystem implements ParticleConstants, PConstants {

	public static final int START_PARTICLES = 1000;

	public static final int START_FORCES = 1000;

	public static final boolean VARIABLE_STEP_SIZE = true;

	public static final boolean VARIABLE_STEP_SPEED = false;

	private static final float DEFAULT_GX = 0.0f;

	private static final float DEFAULT_GY = 0.5f;

	private static final float DEFAULT_GZ = 0.0f;

	private static final float DEFAULT_DRAG = 0.03f;

	private static final float DEFAULT_SPRING_DAMPING = 0.01f;

	private static final float DEFAULT_SPRING_STRENGTH = 0.005f;

	private static final float DEFAULT_SPRING_REST_LENGTH = 18.0f;

	private static final float DEFAULT_TIME_STEP = 0.5f;

	private static final float MIN_TIME_STEP = 0.005f;

	private static final float DEFAULT_MASS = 1.0f;

	private static final float FIXED_MASS = 1000000.0f;

	private static final float DEFAULT_COLLISION_RADIUS = 10.0f;

	private static final float DEFAULT_HIT_RADIUS = 4.0f;

	private static final float DEFAULT_MIN_COLLISION_FORCE = 1.0f;

	private static final float DEFAULT_MAX_COLLISION_FORCE = 2.0f;

	public float time; // accumulated system time

	public boolean useVariableStepSize = VARIABLE_STEP_SIZE;

	public int nForces;

	public int nParticles;

	private int previousNParticles;

	public boolean drawForces = true;

	public Particle particles[] = null;

	public Force forces[] = null;

	public int nImplicitForces;

	public int nExplicitForces;

	public ImplicitForce implicitForces[] = null;

	public Force explicitForces[] = null;

	public Surface surfaces[] = null;

	private double zeros[];

	private int nVectorElements;

	private double x[];

	private double v[];

	private double testX[];

	private double testV[];

	private double dv[];

	private double tmpV[];

	private double tmpX[];

	private double F0[];

	private double m[];

	private double b[];

	private double k1[];

	private double r[];

	private double k2[];

	private double d[];

	private double k3[];

	private double q[];

	private double k4[];

	private double p[];

	private double s[];

	private double t[];

	private double rtilde[];

	private boolean firstImplicitForce = false;

	private boolean implicitGravity;

	public float gX, gY, gZ;

	public float drag, timeStep;

	public float defaultMass;

	public float defaultSpringDamping;

	public float defaultSpringRestLength;

	public float defaultSpringStrength;

	public float defaultCollisionRadius;

	public float defaultHitRadius;

	public float defaultMinCollisionForce;

	public float defaultMaxCollisionForce;

	public int forceRampUpSteps = 0;

	public int stepNum = 0;

	PApplet parent;

	public PApplet getParent() {
		return parent;
	}

	/**
	 * Used when an app uses Class.forName() to invoke this feller. If so, the
	 * subsequent newInstance() call will need to be followed by setup(PApplet
	 * parent) or PApplet.attach(). in order to properly connect this guy.
	 */
	public ParticleSystem() {
	}

	public ParticleSystem(PApplet parent) {
		this.parent = parent;

		// This is to prevent a nasty hang that occurs on Windows sometimes.
		// I don't know why it works or if it's the only solution
		// or if it always works.
		/*
		 * if (!parent.online) { Frame frame = new Frame();
		 * frame.setVisible(true); frame.setVisible(false); }
		 */

		parent.registerPre(this);
		parent.registerPost(this);

		nParticles = 0;
		nForces = 0;

		// void initGlobals() {
		gX = DEFAULT_GX;
		gY = DEFAULT_GY;
		gZ = DEFAULT_GZ;
		drag = DEFAULT_DRAG;
		timeStep = DEFAULT_TIME_STEP;

		defaultMass = DEFAULT_MASS;
		defaultSpringDamping = DEFAULT_SPRING_DAMPING;
		defaultSpringStrength = DEFAULT_SPRING_STRENGTH;
		defaultSpringRestLength = DEFAULT_SPRING_REST_LENGTH;
		defaultCollisionRadius = DEFAULT_COLLISION_RADIUS;
		defaultHitRadius = DEFAULT_HIT_RADIUS;
		defaultMinCollisionForce = DEFAULT_MIN_COLLISION_FORCE;
		defaultMaxCollisionForce = DEFAULT_MAX_COLLISION_FORCE;
	}

	public void pre() {
		if (nParticles > 0) {

			sweepParticlesMarkedForDeletion();

			implicitGravity = (nExplicitForces == 0 && nForces != 0);

			resizeTempVectorsIfNecessary();
			prepareMassArray();
			collectState();
			clearForces();
			preCalcCache();
			if (nImplicitForces > 0) {
				calculateImplicitForces(implicitGravity);
				calculateJacobians();
				implicitForceRampUp();
				preStepCache(timeStep);
				impEulerStep(timeStep);
			}

			if (nExplicitForces > 0 || !implicitGravity) {
				clearForces();
				calculateExplicitForces(!implicitGravity);
				RKStep(timeStep);
			}

			updatePositions(timeStep);

			scatterState();

			// zeroForces();
			// applyForces();
			// calcNextState();
			// updateState();

			// Vector xout = new DenseVector(3*nParticles);
			// Vector vout = new DenseVector(3*nParticles);
			// solver.step(xout, vout, this, allX, allV, timeStep);

		}

		if (surfaces != null) {
			for (int i = 0; i < surfaces.length; i++) {
				surfaces[i].update();
			}
		}

		stepNum++;
	}

	private void sweepParticlesMarkedForDeletion() {
		for (int i = 0; i < nParticles; i++) {
			if (particles[i].markedForRemoval) {
				removeParticle(i);
				i--;
			}
		}
	}

	public Particle getParticleHit(float x, float y) {
		for (int i = nParticles - 1; i >= 0; i--) {
			if (particles[i].isHit(x, y)) {
				return particles[i];
			}
		}
		return null;
	}

	private void RKStep(float step) {
		// Runge-Kutta solver

		// Why does updating position inter-step destroy stability for springs?
		// It helps tons for magnets...
		// double dragMult = (1.0 - drag);

		System.arraycopy(v, 0, tmpV, 0, nVectorElements);
		System.arraycopy(x, 0, tmpX, 0, nVectorElements);

		for (int i = 0; i < nVectorElements; ++i) {
			double tmpK1 = step * F0[i] / m[i];
			k1[i] = tmpK1;
			v[i] = tmpK1 / 2;
		}

		clearForces();
		calculateExplicitForces(!implicitGravity);

		for (int i = 0; i < nVectorElements; ++i) {
			double tmpK2 = step * F0[i] / m[i];
			k2[i] = tmpK2;
			x[i] = tmpX[i] + (step / 2) * v[i];
			v[i] = tmpV[i] + tmpK2 / 2;
		}

		clearForces();
		calculateExplicitForces(!implicitGravity);

		for (int i = 0; i < nVectorElements; ++i) {
			double tmpK3 = step * F0[i] / m[i];
			k3[i] = tmpK3;
			// x[i] already updated
			// x[i] = tmpX[i] + (step/2) * v[i];
			v[i] = tmpV[i] + tmpK3;
		}

		clearForces();
		calculateExplicitForces(!implicitGravity);

		for (int i = 0; i < nVectorElements; ++i) {
			double tmpK4 = step * F0[i] / m[i];
			k4[i] = tmpK4;
			v[i] = tmpV[i] + (k1[i] + 2 * k2[i] + 2 * k3[i] + k4[i]) / 6;
		}
		System.arraycopy(tmpX, 0, x, 0, nVectorElements);
	}

	private void updatePositions(double step) {
		double dragMult = (1.0 - drag);
		for (int i = 0; i < nVectorElements; ++i) {
			v[i] *= dragMult;
			x[i] += step * v[i];
		}
	}

	private void clearForces() {
		System.arraycopy(zeros, 0, F0, 0, nVectorElements);
	}

	private void preCalcCache() {
		for (int i = 0; i < nForces; i++) {
			forces[i].preCalcCache();
		}
	}

	private void calculateImplicitForces(boolean applyGravity) {
		for (int i = 0; i < nImplicitForces; i++) {
			implicitForces[i].calculateForce(F0, v, x);
		}

		if (applyGravity) {
			int ai = 0;
			for (int i = 0; i < nParticles; i++) {
				// Application of gravity.
				if (!particles[i].fixed())
					F0[ai] += particles[i].mass * gX;
				particles[i].force[0] += F0[ai];
				ai++;
				if (!particles[i].fixed())
					F0[ai] += particles[i].mass * gY;
				particles[i].force[1] += F0[ai];
				ai++;
				if (!particles[i].fixed())
					F0[ai] += particles[i].mass * gZ;
				particles[i].force[1] += F0[ai];
				ai++;
			}
		}
	}

	private void implicitForceRampUp() {
		if (stepNum < forceRampUpSteps) {
			double proportion = (stepNum + 1)
					/ ((double) (forceRampUpSteps + 1));
			for (int i = 0; i < nVectorElements; i++) {
				F0[i] *= proportion;
			}
			for (int i = 0; i < nImplicitForces; i++) {
				ImplicitForce f = implicitForces[i];
				for (int x = 0; x < 3; x++) {
					for (int y = 0; y < 3; y++) {
						f.Jx[x][y] *= proportion;
						f.Jv[x][y] *= proportion;
					}
				}
			}
		}
	}

	private void calculateExplicitForces(boolean applyGravity) {
		for (int i = 0; i < nExplicitForces; i++) {
			explicitForces[i].calculateForce(F0, v, x);
		}
		int ai = 0;
		for (int i = 0; i < nParticles; i++) {
			// Application of gravity.
			if (!particles[i].fixed())
				F0[ai] += particles[i].mass * gX;
			particles[i].force[0] += F0[ai];
			ai++;
			if (!particles[i].fixed())
				F0[ai] += particles[i].mass * gY;
			particles[i].force[0] += F0[ai];
			ai++;
			if (!particles[i].fixed())
				F0[ai] += particles[i].mass * gZ;
			particles[i].force[0] += F0[ai];
			ai++;
		}

		if (stepNum < forceRampUpSteps) {
			double proportion = (stepNum + 1)
					/ ((double) (forceRampUpSteps + 1));
			for (int i = 0; i < nVectorElements; i++) {
				F0[i] *= proportion;
			}
		}
	}

	private void calculateJacobians() {
		for (int i = 0; i < nImplicitForces; i++) {
			implicitForces[i].calculateJacobians();
		}
	}

	private void preStepCache(double h) {
		for (int i = 0; i < nForces; i++) {
			forces[i].preStepCache(h);
		}
	}

	public void draw() {
		drawParticles();
		drawForces();
	}

	public void size(int w, int h) {
	}

	public void post() {
	}

	public void mouse(MouseEvent event) {
	}

	public void key(KeyEvent e) {
	}

	public void dispose() {
	}

	// ..................................................................

	public void drawParticles() {
		for (int i = 0; i < nParticles; i++) {
			particles[i].screenX = parent.screenX((float) particles[i].pos[0],
					(float) particles[i].pos[1], (float) particles[i].pos[2]);
			particles[i].screenY = parent.screenY((float) particles[i].pos[0],
					(float) particles[i].pos[1], (float) particles[i].pos[2]);
			if (particles[i].visible)
				particles[i].draw();
		}
	}

	public void drawForces() {
		if (!drawForces)
			return;
		for (int i = 0; i < nForces; i++) {
			if (forces[i].visible)
				forces[i].draw();
		}
	}

	void drawSprings() {
		for (int i = 0; i < nForces; i++) {
			if (forces[i] instanceof Spring) {
				((Spring) forces[i]).draw();
			}
		}
	}

	public void addParticle(Particle part) {
		part.system = this;
		if (particles == null) {
			particles = new Particle[START_PARTICLES];
		}

		if (nParticles >= particles.length) {
			Particle[] newParticles = new Particle[nParticles + START_PARTICLES];
			System.arraycopy(particles, 0, newParticles, 0, nParticles);
			particles = newParticles;
		}

		particles[nParticles] = part;
		part.index = nParticles;

		// if (p.collisionForce != null)
		// addForce(p.collisionForce);
		nParticles++;

		nVectorElements = 3 * nParticles;
	}

	void renumberParticles() {
		for (int i = 0; i < nParticles; i++) {
			particles[i].index = i;
		}
	}

	void resizeTempVectorsIfNecessary() {
		nVectorElements = 3 * nParticles;
		if (nParticles != previousNParticles) {
			k1 = new double[nVectorElements];
			k2 = new double[nVectorElements];
			k3 = new double[nVectorElements];
			k4 = new double[nVectorElements];
			tmpV = new double[nVectorElements];
			tmpX = new double[nVectorElements];
			m = new double[nVectorElements];
			x = new double[nVectorElements];
			v = new double[nVectorElements];
			F0 = new double[nVectorElements];
			zeros = new double[nVectorElements];
		}

		if (nParticles != previousNParticles || firstImplicitForce) {
			if (nImplicitForces > 0) {
				b = k1; // k1 and b are exclusive (RK / Imp eul)
				r = new double[nVectorElements];
				d = new double[nVectorElements];
				q = new double[nVectorElements];
				p = new double[nVectorElements];
				s = new double[nVectorElements];
				t = new double[nVectorElements];
				rtilde = new double[nVectorElements];

				dv = new double[nVectorElements];
				testV = new double[nVectorElements];
				testX = new double[nVectorElements];

				firstImplicitForce = false;
			}
		}

		previousNParticles = nParticles;
	}

	private void removeParticleNoSideEffect(int i) {
		System.arraycopy(particles, i + 1, particles, i, (nParticles - i) - 1);
		particles[nParticles] = null;
		nParticles--;
		renumberParticles();
	}

	public void removeParticle(int i) {
		Particle p = particles[i];
		for (int f = 0; f < nForces; f++) {
			Particle[] dependencies = forces[f].getDependencyList();
			for (int d = 0; d < dependencies.length; d++) {
				if (dependencies[d] == p) {
					removeForce(forces[f]);
					f--;
				}
			}
		}
		removeParticleNoSideEffect(i);
	}

	public void removeParticle(Particle p) {
		for (int i = 0; i < nParticles; i++) {
			if (particles[i] == p) {
				removeParticle(i);
				break;
			}
		}
	}

	// public Magnet addMagnet(Particle a) {
	// Magnet m = new Magnet(a);
	// addForce(m);
	// return m;
	// }
	//
	// public Magnet addMagnet(Particle a, float strength) {
	// Magnet m = new Magnet(a);
	// m.strength = strength;
	// addForce(m);
	// return m;
	// }

	public void addForce(Force f) {
		if (forces == null) {
			forces = new Force[START_FORCES];
			implicitForces = new ImplicitForce[START_FORCES];
			explicitForces = new Force[START_FORCES];
		}

		if (nForces >= forces.length) {
			Force[] newForces = new Force[nForces * 2];
			System.arraycopy(forces, 0, newForces, 0, nForces);
			forces = newForces;
		}

		if (!f.isExplicit() && nImplicitForces >= implicitForces.length) {
			ImplicitForce[] newForces = new ImplicitForce[nImplicitForces * 2];
			System.arraycopy(implicitForces, 0, newForces, 0, nImplicitForces);
			implicitForces = newForces;
			if (!firstImplicitForce) {
				firstImplicitForce = true;
			}
		}

		if (f.isExplicit() && nExplicitForces >= explicitForces.length) {
			Force[] newForces = new Force[nExplicitForces * 2];
			System.arraycopy(explicitForces, 0, newForces, 0, nExplicitForces);
			explicitForces = newForces;
		}

		forces[nForces] = f;
		nForces++;

		if (f.isExplicit()) {
			explicitForces[nExplicitForces] = f;
			nExplicitForces++;
		} else {
			implicitForces[nImplicitForces] = (ImplicitForce) f;
			nImplicitForces++;
		}
	}

	public void removeForce(int i) {
		System.arraycopy(forces, i + 1, forces, i, (nForces - i) - 1);
		forces[nForces] = null;
		nForces--;
	}

	private void removeImplicitForceRecord(ImplicitForce f) {
		int found = -1;
		for (int i = 0; i < nImplicitForces; i++) {
			if (implicitForces[i] == f) {
				found = i;
				break;
			}
		}
		if (found < 0)
			return;

		System.arraycopy(implicitForces, found + 1, implicitForces, found,
				(nImplicitForces - found) - 1);
		implicitForces[nImplicitForces] = null;
		nImplicitForces--;
	}

	private void removeExplicitForceRecord(Force f) {
		int found = -1;
		for (int i = 0; i < nExplicitForces; i++) {
			if (explicitForces[i] == f) {
				found = i;
				break;
			}
		}
		if (found < 0)
			return;

		System.arraycopy(explicitForces, found + 1, explicitForces, found,
				(nExplicitForces - found) - 1);
		explicitForces[nExplicitForces] = null;
		nExplicitForces--;
	}

	public void removeForce(Force f) {
		for (int i = 0; i < nForces; i++) {
			if (forces[i] == f) {
				removeForce(i);
				if (f.isExplicit()) {
					removeExplicitForceRecord(f);
				} else {
					removeImplicitForceRecord((ImplicitForce) f);
				}
				return;
			}
		}
	}

	/*
	 * void applyForces() { Force force = null; for (int i = 0; i < nForces;
	 * i++) { force = forces[i]; if (force.active) force.applyForce(); }
	 * Particle p = null; for (int i = 0; i < nParticles; i++) { p =
	 * particles[i]; if (! p.fixed()) { p.addForce(-drag * p.velocity[0], -drag
	 * * p.velocity[1], -drag * p.velocity[2]); p.addForce(gX * p.mass, gY *
	 * p.mass, gZ * p.mass); } } }
	 * 
	 * 
	 * void applyForces(Particle p) { if (p.fixed()) return;
	 * 
	 * Force force = null; for (int i = 0; i < nForces; i++) { force =
	 * forces[i]; if (force.active) force.applyForce(p); }
	 * 
	 * p.addForce(-drag * p.velocity[0], -drag * p.velocity[1], -drag *
	 * p.velocity[2]); p.addForce(gX, gY, gZ); }
	 * 
	 * 
	 * void calcNextState() { calcNextState(timeStep); }
	 * 
	 * void calcNextState(float step) { Particle p = null; for (int i = 0; i <
	 * nParticles; i++) { p = particles[i]; if (! p.fixed())
	 * p.calculateNext(step); } }
	 * 
	 * 
	 * void updateState() { Particle p = null; for (int i = 0; i < nParticles;
	 * i++) { p = particles[i]; if (!p.fixed()) p.updateNext(); } }
	 */

	public void setGravity(float gY) {
		this.gY = gY;
	}

	public void setGravity(float gX, float gY, float gZ) {
		this.gX = gX;
		this.gY = gY;
		this.gZ = gZ;
	}

	private void addSurface(Surface s) {
		s.system = this;
		if (surfaces == null) {
			surfaces = new Surface[1];

		} else {
			Surface[] newSurfaces = new Surface[surfaces.length + 1];
			System.arraycopy(surfaces, 0, newSurfaces, 0, surfaces.length);
			surfaces = newSurfaces;
		}
		surfaces[surfaces.length - 1] = s;
	}

	public Surface loadSurface(String surfaceName) {
		Surface s = new Surface(parent, this, surfaceName);
		addSurface(s);
		return s;
	}

	private void collectState() {
		int allIndex = 0;
		for (int i = 0; i < nParticles; i++) {
			if (particles[i].fixed[0]) {
				v[allIndex] = 0;// x[allIndex] - particles[i].pos[0];
			} else {
				v[allIndex] = particles[i].velocity[0];
			}
			x[allIndex] = particles[i].pos[0];
			allIndex++;
			if (particles[i].fixed[1]) {
				v[allIndex] = 0;// x[allIndex] - particles[i].pos[1];
			} else {
				v[allIndex] = particles[i].velocity[1];
			}
			x[allIndex] = particles[i].pos[1];
			allIndex++;
			if (particles[i].fixed[2]) {
				v[allIndex] = 0;// x[allIndex] - particles[i].pos[2];
			} else {
				v[allIndex] = particles[i].velocity[2];
			}
			x[allIndex] = particles[i].pos[2];
			allIndex++;
		}
	}

	private void scatterState() {
		int allIndex = 0;
		for (int i = 0; i < nParticles; i++) {
			if (!particles[i].fixed[0]) {
				particles[i].pos[0] = (float) x[allIndex];
				particles[i].velocity[0] = (float) v[allIndex];
			}
			allIndex++;
			if (!particles[i].fixed[1]) {
				particles[i].pos[1] = (float) x[allIndex];
				particles[i].velocity[1] = (float) v[allIndex];
			}
			allIndex++;
			if (!particles[i].fixed[2]) {
				particles[i].pos[2] = (float) x[allIndex];
				particles[i].velocity[2] = (float) v[allIndex];
			}
			allIndex++;

			particles[i].age++;
		}
	}

	public void impEulerStepTry(double h) {

		// Vecd b = h*( fxv + h*(fdx*v) );
		multiplyDfDx(v, tmpV);
		for (int i = 0; i < nVectorElements; i++) {
			b[i] = h * (F0[i] + h * tmpV[i]);
		}

		// conjGrad();
		BiCGStab();
	}

	public void impEulerStep(double h) {
		impEulerStep_RAW(h);
	}

	public void impEulerStep_REPORT(double h) {
		boolean goodStep = false;
		double currentStep = h;
		float saveDrag = drag;

		while (!goodStep) {
			impEulerStepTry(currentStep);
			for (int i = 0; i < nVectorElements; i++) {
				testV[i] = v[i] + dv[i];
				testV[i] *= (1.0 - drag);
				testX[i] = x[i] + h * testV[i];
			}

			goodStep = true;
			if (currentStep > MIN_TIME_STEP) {
				for (int f = 0; f < nForces; f++) {
					Spring s;
					double newLen, dx, dy, dz, lenRat;
					int ai, bi;
					if (forces[f] instanceof Spring) {
						s = (Spring) forces[f];

						if (!s.a.fixed() && !s.b.fixed() && s.dist != 0) {
							ai = s.a.index * 3;
							bi = s.b.index * 3;
							dx = testX[ai] - testX[bi];
							ai++;
							bi++;
							dy = testX[ai] - testX[bi];
							ai++;
							bi++;
							dz = testX[ai] - testX[bi];
							newLen = Math.sqrt(dx * dx + dy * dy + dz * dz);
							lenRat = newLen / s.dist;

							if (lenRat > 5) {
								goodStep = false;

								// if (!s.a.fixed() && !s.b.fixed()) {
								System.out.println("Bang!: " + s.dist);
								// }
								goodStep = true;
								// currentStep /= 1.1;
								// drag *= 1.2;
								// System.out.println("Bad step. " + lenRat);
								break;
							}
						}
					}
				}
			}

		}
		if (goodStep) {
			drag = saveDrag;
			System.arraycopy(testV, 0, v, 0, nVectorElements);
			System.arraycopy(testX, 0, x, 0, nVectorElements);
		}
	}

	public void impEulerStep_RAW(double h) {

		boolean goodStep = false;
		double currentStep = h;
		float saveDrag = drag;

		while (!goodStep) {
			impEulerStepTry(currentStep);
			for (int i = 0; i < nVectorElements; i++) {
				testV[i] = v[i] + dv[i];
				// testV[i] *= (1.0 - drag);
				// testX[i] = x[i] + h*testV[i];
			}

			goodStep = true;
		}
		if (goodStep) {
			drag = saveDrag;
			System.arraycopy(testV, 0, v, 0, nVectorElements);
			// System.arraycopy(testX, 0, x, 0, nVectorElements);
		}

	}

	private double dot(double a[], double b[]) {
		double result = 0;
		for (int i = 0; i < a.length; i++) {
			result += a[i] * b[i];
		}
		return result;
	}

	int IT_MAX = 100;

	double EPS = 0.00001;

	/*
	 * private void conjGrad() { System.arraycopy(zeros, 0, dv, 0,
	 * nVectorElements); int i = 0; multiplyA(dv, r); for (int vi = 0; vi <
	 * nVectorElements; vi++) { r[vi] = b[vi] - r[vi]; } System.arraycopy(r, 0,
	 * d, 0, nVectorElements); double epsNew = dot(r, r); double eps0 = epsNew;
	 * while (i < IT_MAX && epsNew > EPS * eps0) { multiplyA(d, q); double alpha
	 * = epsNew / dot(d, q); for (int vi = 0; vi < nVectorElements; vi++) {
	 * dv[vi] += alpha * d[vi]; r[vi] -= alpha * q[vi]; } double epsOld =
	 * epsNew; epsNew = dot(r, r); double beta = epsNew / epsOld; for (int vi =
	 * 0; vi < nVectorElements; vi++) { d[vi] = r[vi] + beta * d[vi]; } i++; } }
	 */

	void add(double alpha, double x[], double beta, double y[], double z[]) {
		for (int i = 0; i < x.length; i++) {
			z[i] = alpha * x[i] + beta * y[i];
		}
	}

	double initR = 0;

	int maxIter = 100000;

	double rtol = 1e-5;

	double atol = 1e-50;

	double dtol = 1e+5;

	boolean converged(int iter, double rv[]) {
		double r = java.lang.Math.sqrt(dot(rv, rv));
		// Store initial residual
		if (iter == 0)
			initR = r;

		// Check for convergence
		if (r < Math.max(rtol * initR, atol))
			return true;

		// Check for divergence
		if (r > dtol * initR)
			return true; // Not really. Diverged.

		if (iter >= maxIter)
			return true; // Not really. Timed out.

		if (Double.isNaN(r))
			return true; // Not really. Diverged.

		// Neither convergence nor divergence
		return false;
	}

	private boolean BiCGStab() {
		double phat[] = p;
		double shat[] = s;
		double rho_1 = 1., rho_2 = 1., alpha = 1., beta = 1., omega = 1.;
		// r = b - A*dv
		multiplyA(dv, q);
		add(1.0, b, -1.0, q, r);

		// rtilde = r
		System.arraycopy(r, 0, rtilde, 0, nVectorElements);

		int it = 0;
		// for (iter.setFirst(); !iter.converged(r, dv); iter.next()) {
		while (!converged(it, r)) {
			// rho_1 = rtilde . r
			rho_1 = dot(rtilde, r);
			if (rho_1 == 0.)
				// rho breakdown. Non-convergence
				return false;
			if (omega == 0.)
				// omega breakdown. Non-convergence
				return false;

			if (it == 0)
				// p = r
				System.arraycopy(r, 0, p, 0, nVectorElements);
			else {
				beta = (rho_1 / rho_2) * (alpha / omega);
				// temp = -omega * d + p;
				// p = beta * temp + r
				add(-omega, d, 1.0, p, tmpV);
				add(beta, tmpV, 1.0, r, p);
			}
			// Apply preconditioner: M.apply(p, phat);
			// d = A*phat
			multiplyA(phat, d);

			// alpha = rho_1 / (rtilde . d)
			alpha = rho_1 / dot(rtilde, d);

			// s = -alpha * d + r
			add(-alpha, d, 1.0, r, s);

			if (converged(it, s)) {
				// dv += alpha * phat
				add(alpha, phat, 1.0, dv, dv);
				return true;
			}
			// Apply preconditioner: M.apply(s, shat);
			// t = A * shat
			multiplyA(shat, t);

			// omega = (t . s) / (t . t)
			omega = dot(t, s) / dot(t, t);

			// dv += alpha * phat + omega * shat
			add(alpha, phat, 1.0, dv, dv);
			add(omega, shat, 1.0, dv, dv);
			// r = -omega * t + s
			add(-omega, t, 1.0, s, r);

			rho_2 = rho_1;
			it++;
		}
		return true;
	}

	public void multiplyA(double src[], double dst[]) {
		System.arraycopy(zeros, 0, dst, 0, nVectorElements); // Zero dest

		for (int i = 0; i < nVectorElements; ++i) {
			dst[i] = src[i] * m[i];
		}

		for (int i = 0; i < nImplicitForces; i++) {
			implicitForces[i].multiplyAPiece(src, dst);
		}
	}

	private void prepareMassArray() {
		int ai = 0;
		for (int i = 0; i < nParticles; i++) {
			if (!particles[i].fixed[0]) {
				m[ai] = particles[i].mass;
			} else {
				m[ai] = FIXED_MASS;
			}
			ai++;
			if (!particles[i].fixed[1]) {
				m[ai] = particles[i].mass;
			} else {
				m[ai] = FIXED_MASS;
			}
			ai++;
			if (!particles[i].fixed[2]) {
				m[ai] = particles[i].mass;
			} else {
				m[ai] = FIXED_MASS;
			}
			ai++;
		}
	}

	public void multiplyDfDx(double src[], double dst[]) {
		System.arraycopy(zeros, 0, dst, 0, nVectorElements); // Zero dest
		for (int i = 0; i < nImplicitForces; i++) {
			implicitForces[i].multiplyDfDx(src, dst);
		}
	}

	public void multiplyDfDv(double src[], double dst[]) {
		System.arraycopy(zeros, 0, dst, 0, nVectorElements); // Zero dest
		for (int i = 0; i < nImplicitForces; i++) {
			implicitForces[i].multiplyDfDv(src, dst);
		}
	}

}
