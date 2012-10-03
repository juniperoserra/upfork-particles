import processing.core.PApplet;
import simong.particles.ExplicitSpring;
import simong.particles.Force;
import simong.particles.Magnet;
import simong.particles.Particle;
import simong.particles.ParticleSystem;
import simong.particles.Spring;

/**
 * @author sgreenwo
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class mixedTug extends PApplet {

	class NoDrawPart extends Particle {
		NoDrawPart(ParticleSystem system) {
			super(system);
		}

		NoDrawPart(ParticleSystem system, float x, float y, float z) {
			super(system, x, y, z);
		}

		public void draw() {

		}
	}

	class Slider {
		Slider(int x) {
			m_x = x;
			m_width = 10;
			m_height = 5;
		}

		int m_x;

		int m_y;

		int m_width;

		int m_height;

		int m_minY;

		int m_maxY;

		float m_minValue;

		float m_maxValue;

		int m_r, m_g, m_b;

		float m_value;

		void draw() {
			stroke(0);
			line(m_x, m_minY, m_x, m_maxY);
			line(m_x - 5, m_minY, m_x + 5, m_minY);
			line(m_x - 5, m_maxY, m_x + 5, m_maxY);

			fill(m_r, m_g, m_b);
			rectMode(CORNER);
			rect(m_x - m_width / 2, m_y - m_height / 2, m_width, m_height);

			// text(nf(m_value, 2, 2), m_x, m_maxY);

		}

		void setColor(int r, int g, int b) {
			m_r = r;
			m_g = g;
			m_b = b;
		}

		void setYRange(int minY, int maxY) {
			m_minY = minY;
			m_maxY = maxY;
		}

		void setValueRange(float min, float max) {
			m_minValue = min;
			m_maxValue = max;
		}

		void setY(int y) {
			m_y = y;
			if (m_y > m_maxY)
				m_y = m_maxY;
			if (m_y < m_minY)
				m_y = m_minY;
			m_value = m_maxValue - (m_maxValue - m_minValue)
					* (((float) (m_y - m_minY)) / ((float) (m_maxY - m_minY)));
		}

		boolean hit(int x, int y) {
			if (x < m_x - m_width / 2)
				return false;
			if (y < m_y - m_height / 2)
				return false;
			if (x > m_x + m_width / 2)
				return false;
			if (y > m_y + m_height / 2)
				return false;
			return true;
		}
	}

	int PARTICLES_Z = 1;

	int PARTICLES_Y = 8;

	int PARTICLES_X = 8;

	int X_OFFSET = 70;

	int Y_OFFSET = 70;

	int Z_OFFSET = 0;

	float unitLength = 25;

	ParticleSystem ps;

	Particle[][][] expParticles;

	Particle[][][] impParticles;

	Slider impSlider;

	Slider expSlider;

	int g_mouseButton;

	boolean g_trackingImpSlider = false;

	boolean g_trackingExpSlider = false;

	boolean g_mousePressed = false;

	boolean g_mouseReleased = false;

	Particle movingParticle;

	boolean isMovingParticle;

	boolean wasMovingParticleFixed;

	public void setup() {
		size(500, 500, P3D);
		ps = new ParticleSystem(this);
		// ps.setGravity(1);
		// ps.drag = 0;
		ps.defaultMass = 1.0f;
		ps.defaultSpringRestLength = 30;
		ps.defaultSpringStrength = 1.0f;
		ps.defaultSpringDamping = 0.02f;

		ps.gY = 5.03f;
		ps.forceRampUpSteps = 200;

		makeExpGrid();

		ps.defaultSpringStrength = 1.0f;
		ps.defaultSpringDamping = 0.02f;
		makeImpGrid();

		// ps.removeParticle(impParticles[4][5][0]);

		// ps.removeParticle(expParticles[2][2][0]);

		expParticles[0][0][0].fix();
		// particles[PARTICLES_X-1][PARTICLES_Y-1][0].fix();
		expParticles[0][PARTICLES_Y - 1][0].fix();
		expParticles[0][PARTICLES_Y - 1][0].pos[0] = 480;
		expParticles[0][PARTICLES_Y - 1][0].pos[1] = 40;

		impParticles[0][0][0].fix();
		// particles[PARTICLES_X-1][PARTICLES_Y-1][0].fix();
		impParticles[0][PARTICLES_Y - 1][0].fix();
		impParticles[0][PARTICLES_Y - 1][0].pos[0] = 480;
		impParticles[0][PARTICLES_Y - 1][0].pos[1] = 40;

		// ps.timeStep = 0.1f;

		Particle magPart = new Particle(ps, 250, 400, 0);
		magPart.fix();
		Magnet mag = new Magnet(magPart);
		mag.strength = 20;

		impSlider = new Slider(20);
		impSlider.setYRange(20, 420);
		impSlider.setValueRange(0.1f, 20);
		impSlider.setColor(0, 0, 255);
		impSlider.setY(385);

		expSlider = new Slider(40);
		expSlider.setYRange(20, 420);
		expSlider.setValueRange(0.1f, 20);
		expSlider.setColor(255, 0, 0);
		expSlider.setY(385);
	}

	float rotZ = 0;

	void expDraw() {
		for (int i = 0; i < ps.nExplicitForces; i++) {
			Force f = ps.explicitForces[i];
			if (f instanceof ExplicitSpring) {
				ExplicitSpring es = (ExplicitSpring) f;
				line((float) es.a.pos[0], (float) es.a.pos[1],
						(float) es.a.pos[2], (float) es.b.pos[0],
						(float) es.b.pos[1], (float) es.b.pos[2]);
			}
		}
	}

	void impDraw() {
		for (int i = 0; i < ps.nImplicitForces; i++) {
			Force f = ps.implicitForces[i];
			if (f instanceof Spring) {
				Spring s = (Spring) f;
				line((float) s.a.pos[0], (float) s.a.pos[1],
						(float) s.a.pos[2], (float) s.b.pos[0],
						(float) s.b.pos[1], (float) s.b.pos[2]);
			}
		}
	}

	void setExpSpringStrengths(float strength) {
		for (int i = 0; i < ps.nExplicitForces; i++) {
			Force f = ps.explicitForces[i];
			if (f instanceof ExplicitSpring) {
				ExplicitSpring es = (ExplicitSpring) f;
				es.strength = strength;
				es.damping = strength / 10;
			}
		}
	}

	void setImpSpringStrengths(float strength) {
		for (int i = 0; i < ps.nImplicitForces; i++) {
			Force f = ps.implicitForces[i];
			if (f instanceof Spring) {
				Spring s = (Spring) f;
				s.strength = strength;
				s.damping = strength / 10;
			}
		}
	}

	public void draw() {

		if (g_mousePressed) {
			if (impSlider.hit(mouseX, mouseY)) {
				g_trackingImpSlider = true;
			} else if (expSlider.hit(mouseX, mouseY)) {
				g_trackingExpSlider = true;
			}

			int hitIndex = -1;
			for (int i = 0; i < ps.nParticles; i++) {
				if (ps.particles[i].isHit(mouseX, mouseY)) {
					hitIndex = i;
				}
			}
			if (hitIndex >= 0) {
				if (g_mouseButton == LEFT) {
					isMovingParticle = true;
					movingParticle = ps.particles[hitIndex];
					wasMovingParticleFixed = ps.particles[hitIndex].fixed();
					movingParticle.fix();
				} else {
					isMovingParticle = false;
					ps.removeParticle(hitIndex);
				}
			}
			g_mousePressed = false;
		}

		if (g_mouseReleased) {
			g_trackingExpSlider = false;
			g_trackingImpSlider = false;
			if (isMovingParticle) {
				isMovingParticle = false;
				movingParticle.fix(wasMovingParticleFixed);
				movingParticle = null;
			}
			g_mouseReleased = false;
		}

		background(255);

		if (g_trackingExpSlider) {
			expSlider.setY(mouseY);
		}
		if (g_trackingImpSlider) {
			impSlider.setY(mouseY);
		}
		expSlider.draw();
		impSlider.draw();

		setExpSpringStrengths(expSlider.m_value);
		setImpSpringStrengths(impSlider.m_value);

		// impParticles[0][PARTICLES_Y-1][0].pos[0] = mouseX;
		// impParticles[0][PARTICLES_Y-1][0].pos[1] = mouseY;
		// impParticles[0][PARTICLES_Y-1][0].pos[2] = 0;

		// expParticles[0][PARTICLES_Y-1][0].pos[0] = mouseX;
		// expParticles[0][PARTICLES_Y-1][0].pos[1] = mouseY;
		// expParticles[0][PARTICLES_Y-1][0].pos[2] = 0;

		if (isMovingParticle) {
			movingParticle.pos[0] = mouseX;
			movingParticle.pos[1] = mouseY;
			movingParticle.pos[2] = 0;
		}

		ps.draw();

		stroke(255, 0, 0);
		expDraw();
		stroke(0, 0, 255);
		impDraw();
	}

	void makeExpGrid() {
		expParticles = new Particle[PARTICLES_X][PARTICLES_Y][PARTICLES_Z];

		for (int x = 0; x < PARTICLES_X; x++) {
			for (int z = 0; z < PARTICLES_Z; z++) {
				for (int y = 0; y < PARTICLES_Y; y++) {
					expParticles[x][y][z] = new NoDrawPart(ps, X_OFFSET
							+ unitLength * x, Y_OFFSET + unitLength * y,
							Z_OFFSET + unitLength * z);

					if (x > 0) {
						ExplicitSpring s = new ExplicitSpring(
								expParticles[x][y][z],
								expParticles[x - 1][y][z]);
					}
					if (y > 0) {
						ExplicitSpring s = new ExplicitSpring(
								expParticles[x][y][z],
								expParticles[x][y - 1][z]);
					}
					if (z > 0) {
						ExplicitSpring s = new ExplicitSpring(
								expParticles[x][y][z],
								expParticles[x][y][z - 1]);
					}
				}
			}
		}

	}

	void makeImpGrid() {
		impParticles = new Particle[PARTICLES_X][PARTICLES_Y][PARTICLES_Z];

		for (int x = 0; x < PARTICLES_X; x++) {
			for (int z = 0; z < PARTICLES_Z; z++) {
				for (int y = 0; y < PARTICLES_Y; y++) {
					impParticles[x][y][z] = new NoDrawPart(ps, X_OFFSET
							+ unitLength * x, Y_OFFSET + unitLength * y,
							Z_OFFSET + unitLength * z);

					if (x > 0) {
						Spring s = new Spring(impParticles[x][y][z],
								impParticles[x - 1][y][z]);
					}
					if (y > 0) {
						Spring s = new Spring(impParticles[x][y][z],
								impParticles[x][y - 1][z]);
					}
					if (z > 0) {
						Spring s = new Spring(impParticles[x][y][z],
								impParticles[x][y][z - 1]);
					}
				}
			}
		}

		// Just add one spring for x == 3
		// ps.addForce(new Spring(particles[3][2][2], particles[2][2][2]));

	}

	public void mousePressed() {
		g_mousePressed = true;
		g_mouseButton = mouseButton;
	}

	public void mouseReleased() {
		g_mouseReleased = true;
	}

}
