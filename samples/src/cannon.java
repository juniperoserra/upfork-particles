import processing.core.PApplet;
import simong.particles.Particle;
import simong.particles.ParticleSystem;

/**
 * @author sgreenwo
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class cannon extends PApplet {
	// CannonBall cannonball = new CannonBall();

	class CannonBall extends Particle {
		int ballR;
		int ballG;
		int ballB;
		float radius;

		public CannonBall(ParticleSystem ps) {
			super(ps);
			ellipseMode(CENTER);
			ballR = (int) random(255);
			ballG = (int) random(255);
			ballB = (int) random(255);
			radius = 5 + random(30);
			minCollisionForce = radius;
			maxCollisionForce = radius * 2;
			enableCollision(radius + 2);
                        mass = PI * radius / 20;
		}

		public void draw() {
			pushMatrix();
			translate(pos[0], pos[1], pos[2]);
			noStroke();
			fill(ballR, ballG, ballB);
			ellipse(0, 0, radius * 2, radius * 2);
			popMatrix();

			if (pos[1] > height + radius * 2)
				die();
		}
	}

	ParticleSystem ps;

	public void setup() {
		size(500, 800, P3D);
		ps = new ParticleSystem(this);
		ps.setGravity(0.5f);
		ps.drag = 0.02f;
	}

	void fireLeft() {
		Particle p = new CannonBall(ps);
		p.pos[0] = 0;
		p.pos[1] = height;
		p.pos[2] = 0;

		p.velocity[0] = random(30);
		p.velocity[1] = random(-15) - 20;
	}

	void fireRight() {
		Particle p = new CannonBall(ps);
		p.pos[0] = width;
		p.pos[1] = height;
		p.pos[2] = 0;
		p.velocity[0] = random(-30);
		p.velocity[1] = random(-15) - 20;
	}

	int loopCounter = 0;
        int skipLoops = 6;

	public void draw() {
		background(255);

		if (loopCounter % skipLoops == 0) {
			fireLeft();
			fireRight();
		}

		ps.drawParticles();

		loopCounter++;
	}
}

