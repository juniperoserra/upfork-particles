import processing.core.PApplet;
import simong.particles.Particle;
import simong.particles.ParticleSystem;

/**
 * @author sgreenwo
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class simple extends PApplet {

	ParticleSystem ps;
	Particle p1, p2;

	public void setup() {
		size(500, 500, P3D);
		ps = new ParticleSystem(this);
		// ps.setGravity(1);
		// ps.drag = 0;
		// ps.defaultMass = 1.0f;
		// ps.defaultSpringRestLength = 30;
		// ps.defaultSpringStrength = 0.03f;
		// ps.defaultSpringDamping = 0.001f;

		p1 = new Particle(ps, width / 2, height / 2, 0);
		// p1.fix();
		// p2 = new Particle(ps);

		// Spring s = new Spring(p1, p2);

		// ps.gY = 5.03f;

	}

	public void draw() {
		background(255);

		ps.draw();
	}
}