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
public class truss extends PApplet {
	int nodesZ = 10;
	float spreadZ = 40;

	int nodesX = 10;
	float spreadX = 40;

	ParticleSystem ps;
	// Arcball arcball;

	Particle TopGrid[][] = new Particle[nodesX][nodesZ];
	Particle BottomGrid[][] = new Particle[nodesX][nodesZ];
	Spring TopSpring[][][][] = new Spring[nodesX][nodesZ][nodesX][nodesZ];
	Spring BottomSpring[][][][] = new Spring[nodesX][nodesZ][nodesX][nodesZ];
	Spring CrossSpring[][][][] = new Spring[nodesX][nodesZ][nodesX][nodesZ];

	public void setup() {
		size(800, 450, P3D);
		// ps = (PSystem)loadPlugin("PSystem");
		// arcball = (Arcball)loadPlugin("Arcball");
		ps = new ParticleSystem(this);

		ps.defaultSpringRestLength = 40;
		ps.defaultSpringStrength = 30;
		ps.defaultSpringDamping = 3.8f;
		ps.timeStep = 0.125f;
		// ps.setGravity (0.1);
		ps.gY = 0.1f;

		// BOTTOM GRID PARTICLES AND SPRINGS
		for (int i = 0; i < nodesX; i++) {
			for (int j = 0; j < nodesZ; j++) {

				// ADD PARTICLES BOTTOM GRID
				BottomGrid[i][j] = new Particle(ps, width / 2 - (nodesX / 2)
						* spreadX + spreadX * i, height / 2 + sqrt(3) / 2
						* spreadZ, -(nodesX / 2) * spreadZ + spreadZ * j);

				BottomGrid[i][j].fix();
			}

			// ADD SPRING FOR BOTTOM GRID ALONG Z AXIS
			for (int j = 1; j < nodesZ; j++) {
				BottomSpring[i][j][i][j - 1] = new Spring(BottomGrid[i][j],
						BottomGrid[i][j - 1]);
			}
		}
		// ADD SPRING FOR BOTTOM GRID ALONG X AXIS
		for (int i = 1; i < nodesX; i++) {
			for (int j = 0; j < nodesZ; j++) {
				BottomSpring[i][j][i - 1][j] = new Spring(BottomGrid[i][j],
						BottomGrid[i - 1][j]);
			}
		}

		// TOP GRID PARTICLES AND SPRINGS
		for (int i = 0; i < nodesX; i++) {
			for (int j = 0; j < nodesZ; j++) {

				TopGrid[i][j] = new Particle(
						ps,
						(float) (width / 2 - (nodesX / 2) * spreadX + spreadX
								* 0.5 + spreadX * i),
						height / 2,
						(float) (-(nodesX / 2) * spreadZ + spreadZ * 0.5 + spreadZ
								* j));

				// TopGrid[i][j].fix();
			}

			// ADD SPRING FOR TOP GRID ALONG Z AXIS
			for (int j = 1; j < nodesZ; j++) {
				TopSpring[i][j][i][j - 1] = new Spring(TopGrid[i][j],
						TopGrid[i][j - 1]);
			}
		}

		// ADD SPRING FOR TOP GRID ALONG X AXIS
		for (int i = 1; i < nodesX; i++) {
			for (int j = 0; j < nodesZ; j++) {
				TopSpring[i][j][i - 1][j] = new Spring(TopGrid[i][j],
						TopGrid[i - 1][j]);
			}
		}

		// ADD CROSS SPRINGS
		for (int i = 0; i < nodesX; i++) {
			for (int j = 1; j < nodesZ; j++) {
				CrossSpring[i][j - 1][i][j] = new Spring(TopGrid[i][j - 1],
						BottomGrid[i][j]);
			}
		}

		for (int i = 0; i < nodesX; i++) {
			for (int j = 0; j < nodesZ; j++) {
				CrossSpring[i][j][i][j] = new Spring(TopGrid[i][j],
						BottomGrid[i][j]);
			}
		}

		for (int i = 1; i < nodesX; i++) {
			for (int j = 0; j < nodesZ; j++) {
				CrossSpring[i - 1][j][i][j] = new Spring(TopGrid[i - 1][j],
						BottomGrid[i][j]);
			}
		}

		for (int i = 1; i < nodesX; i++) {
			for (int j = 1; j < nodesZ; j++) {
				CrossSpring[i - 1][j - 1][i][j] = new Spring(
						TopGrid[i - 1][j - 1], BottomGrid[i][j]);
			}
		}

		// FIXING PARTICLES
		for (int j = 0; j < nodesZ; j++) {
			TopGrid[0][j].fix();
			BottomGrid[0][j].fix();
		}

	}

	public void draw() {
		background(255);
		// arcball.run();
		ps.draw();
	}
}
