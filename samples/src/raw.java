import processing.core.PApplet;
import simong.particles.Particle;
import simong.particles.ParticleSystem;
import simong.particles.Spring;

public class raw extends PApplet {// raw
	ParticleSystem ps;
	Particle a, b, c;

	public void setup() {
		size(200, 200);
		stroke(51);
		ps = new ParticleSystem(this);

		a = new Particle(ps, width / 2 - 50, 0, 0);
		a.fix();

		b = new Particle(ps, width / 2 + 50, 0, 0);

		c = new Particle(ps, width / 2, 70, 0);

		Spring s = new Spring(a, b);
		s.strength = 1;
		s.restLength = 60;

		s = new Spring(b, c);
		s.strength = 1;
		s.restLength = 60;

		s = new Spring(c, a);
		s.strength = 1;
		s.restLength = 60;

		b.fix(0, 30);
	}

	public void draw() {
		a.setPos(mouseX, mouseY, 0);
		background(204);
		fill(255);
		ps.draw();
	}
}