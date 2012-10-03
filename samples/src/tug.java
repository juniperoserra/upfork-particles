import processing.core.PApplet;
import simong.particles.Particle;
import simong.particles.ParticleSystem;
import simong.particles.Spring;

/**
 * @author sgreenwo
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class tug extends PApplet {

	class NoDrawPart extends Particle {
		NoDrawPart(ParticleSystem ps) {
			super(ps);
		}

		NoDrawPart(ParticleSystem ps, float x, float y, float z) {
			super(ps, x, y, z);
		}

		public void draw() {

		}
	}

	int PARTICLES_Z = 1;
	int PARTICLES_Y = 8;
	int PARTICLES_X = 8;

	int X_OFFSET = 100;
	int Y_OFFSET = 100;
	int Z_OFFSET = 0;

	float unitLength = 30;

	ParticleSystem ps;
	Particle[][][] particles;

	public void setup() {
		size(500, 500, P3D);
		ps = new ParticleSystem(this);
		// ps.setGravity(1);
		// ps.drag = 0;
		ps.defaultMass = 1.0f;
		ps.defaultSpringRestLength = 30;
		ps.defaultSpringStrength = 20.0f;
		ps.defaultSpringDamping = 4.0f;

		ps.gY = 5.03f;

		makeGrid();

		particles[0][0][0].fix();
		// particles[PARTICLES_X-1][PARTICLES_Y-1][0].fix();
		particles[0][PARTICLES_Y - 1][0].fix();

		// ps.timeStep = 0.1f;
	}

	float rotZ = 0;

	public void draw() {
		// translate(width/4, height/2);
		// rotateZ(rotZ);
		rotZ += 0.1;
		// background(#99CCCC);
		background(255);

		particles[0][PARTICLES_Y - 1][0].pos[0] = mouseX;
		particles[0][PARTICLES_Y - 1][0].pos[1] = mouseY;
		particles[0][PARTICLES_Y - 1][0].pos[2] = 0;

		ps.draw();
	}

	void makeGrid() {
		particles = new Particle[PARTICLES_X][PARTICLES_Y][PARTICLES_Z];

		for (int x = 0; x < PARTICLES_X; x++) {
			for (int z = 0; z < PARTICLES_Z; z++) {
				for (int y = 0; y < PARTICLES_Y; y++) {
					particles[x][y][z] = new NoDrawPart(ps);
					particles[x][y][z].setPos(X_OFFSET + unitLength * x,
							Y_OFFSET + unitLength * y, Z_OFFSET + unitLength
									* z);

					// Don't add springs if x == 3
					// if (x != 3) {

					if (x > 0) {
						Spring s = new Spring(particles[x][y][z],
								particles[x - 1][y][z]);
						// s.strength = 50f;
						// s.damping = 40f;
					}
					if (y > 0) {
						Spring s = new Spring(particles[x][y][z],
								particles[x][y - 1][z]);
						// s.strength = 50f;
						// s.damping = 40f;
					}
					if (z > 0) {
						Spring s = new Spring(particles[x][y][z],
								particles[x][y][z - 1]);
						// s.strength = 50f;
						// s.damping = 40f;
					}
					// }
				}
			}
		}

		// Just add one spring for x == 3
		// ps.addForce(new Spring(particles[3][2][2], particles[2][2][2]));

	}

}
