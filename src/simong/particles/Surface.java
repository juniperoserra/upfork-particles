/**
 * Surface - part of Simon Greenwold's ParticleSystem library
 * An extension for the Processing project - http://processing.org
 * <p/>
 * Copyright (c) 2004-2006 Simon Greenwold, Created: Feb 11, 2004
 * Updated for Processing 0070 by Ben Fry in September 2004
 * Updated for Processing 0107 by Simon Greenwold in March 2006
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

import java.io.BufferedReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

public class Surface {

	private final static int A = 0;

	private final static int B = 1;

	private final static int C = 2;

	private final static int D = 3;

	float _plane[] = new float[4];

	PApplet parent;

	ParticleSystem system;

	public Surface(PApplet parent, ParticleSystem system, String filename) {
		this.parent = parent;
		this.system = system;
		load(filename);
	}

	public Surface(PApplet parent, String filename) {
		this.parent = parent;
		load(filename);
	}

	static class Vec3 {
		float x, y, z;

		Vec3() {
		}

		Vec3(float v[]) {
			this.x = v[0];
			this.y = v[1];
			this.z = v[2];
		}

		Vec3(Vec3 v) {
			this.x = v.x;
			this.y = v.y;
			this.z = v.z;
		}

		Vec3(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		void normalize() {
			float length = length();
			x /= length;
			y /= length;
			z /= length;
		}

		float length() {
			return (float) Math.sqrt(x * x + y * y + z * z);
		}

		static Vec3 cross(Vec3 v1, Vec3 v2) {
			Vec3 res = new Vec3();
			res.x = v1.y * v2.z - v1.z * v2.y;
			res.y = v1.z * v2.x - v1.x * v2.z;
			res.z = v1.x * v2.y - v1.y * v2.x;
			return res;
		}

		static float dot(Vec3 v1, Vec3 v2) {
			return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
		}

		static Vec3 mul(Vec3 v, float d) {
			Vec3 res = new Vec3();
			res.x = v.x * d;
			res.y = v.y * d;
			res.z = v.z * d;
			return res;
		}

		public void sub(Vec3 v1, Vec3 v2) {
			x = v1.x - v2.x;
			y = v1.y - v2.y;
			z = v1.z - v2.z;
		}

		public void add(Vec3 v1, Vec3 v2) {
			x = v1.x + v2.x;
			y = v1.y + v2.y;
			z = v1.z + v2.z;
		}
	}

	/**
	 * Description of the Method
	 * 
	 * @param x
	 *            Description of the Parameter
	 * @param y
	 *            Description of the Parameter
	 * @param z
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	Vec3 pointHit(float x, float y, float z) {
		return pointHit(new Vec3(x, y, z));
	}

	/**
	 * Description of the Method
	 * 
	 * @param p
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	Vec3 pointHit(float p[]) {
		return pointHit(p[0], p[1], p[2]);
	}

	/**
	 * Method movingPointHit: In moving from p0 -> p1, did we move to below the
	 * surface? If yes, return the hit point. If no, return null. The assumption
	 * is that p0 is ABOVE the surface to begin with.
	 * 
	 * @param p
	 * @return the hit point if we've moved below, and null if we haven't
	 */
	Vec3 pointHit(Vec3 p) {
		Vec3 result = null;
		for (int tri = 0; tri < nTris; tri++) {
			result = pointHitTriangle(p, tri);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Description of the Method
	 * 
	 * @param p
	 *            Description of the Parameter
	 * @param tri
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	Vec3 pointHitTriangle(Vec3 p, int tri) {

		planeFromTriangle(tri, _plane);
		float d = signedDistance(p, _plane);
		if (d < 0) {
			return null;
		}
		// If p1 is still above, no hit
		// Otherwise we have to test for the segment-triangle intersection

		Vec3 planePt = perpToPlane(p, _plane);

		if (ptInTri(planePt, tri)) {
			return planePt;
		}
		return null;
	}

	/**
	 * Description of the Method
	 * 
	 * @param pt
	 *            Description of the Parameter
	 * @param plane
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	Vec3 perpToPlane(Vec3 pt, float plane[]) {
		Vec3 result = new Vec3();

		Vec3 n = new Vec3(plane[0], plane[1], plane[2]);
		result.sub(pt, Vec3.mul(n, (Vec3.dot(n, pt) + plane[3])
				/ Vec3.dot(n, n)));
		return result;
	}

	/**
	 * Description of the Method
	 * 
	 * @param pt
	 *            Description of the Parameter
	 * @param T
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	boolean ptInTri(Vec3 pt, int T) {
		Vec3 u;
		Vec3 v;
		Vec3 w;

		Vec3 V0 = new Vec3(verts[tris[T][0]]);
		Vec3 V1 = new Vec3(verts[tris[T][1]]);
		Vec3 V2 = new Vec3(verts[tris[T][2]]);
		// get triangle edge vectors and plane normal
		u = new Vec3();
		u.sub(V1, V0);
		v = new Vec3();
		v.sub(V2, V0);

		// is pt inside T?
		float uu;

		// is pt inside T?
		float uv;

		// is pt inside T?
		float vv;

		// is pt inside T?
		float wu;

		// is pt inside T?
		float wv;

		// is pt inside T?
		float D;
		uu = Vec3.dot(u, u);
		uv = Vec3.dot(u, v);
		vv = Vec3.dot(v, v);
		w = new Vec3();
		w.sub(pt, V0);
		wu = Vec3.dot(w, u);
		wv = Vec3.dot(w, v);
		D = uv * uv - uu * vv;

		// get and test parametric coords
		float s;

		// get and test parametric coords
		float t;
		s = (uv * wv - vv * wu) / D;
		if (s < 0.0 || s > 1.0) {
			// pt is outside T
			return false;
		}
		t = (uv * wu - uu * wv) / D;
		if (t < 0.0 || (s + t) > 1.0) {
			// pt is outside T
			return false;
		}

		return true;
		// pt is in T
	}

	/**
	 * Method movingPointHit: In moving from (x0, y0, z0) -> (x1, y1, z1), did
	 * we move to below the surface? If yes, return the hit point. If no, return
	 * null. The assumption is that (x0, y0, z0) is ABOVE the surface to begin
	 * with.
	 * 
	 * @param x0
	 * @param y0
	 * @param z0
	 * @param x1
	 * @param y1
	 * @param z1
	 * @return the hit point if we've moved below, and null if we haven't
	 */
	Vec3 movingPointHit(float x0, float y0, float z0, float x1, float y1,
			float z1) {
		return movingPointHit(new Vec3(x0, y0, z0), new Vec3(x1, y1, z1));
	}

	/**
	 * Description of the Method
	 * 
	 * @param p0
	 *            Description of the Parameter
	 * @param p1
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	Vec3 movingPointHit(float p0[], float p1[]) {
		return movingPointHit(p0[0], p0[1], p0[2], p1[0], p1[1], p1[2]);
	}

	/**
	 * Method movingPointHit: In moving from p0 -> p1, did we move to below the
	 * surface? If yes, return the hit point. If no, return null. The assumption
	 * is that p0 is ABOVE the surface to begin with.
	 * 
	 * @param p0
	 * @param p1
	 * @return the hit point if we've moved below, and null if we haven't
	 */
	Vec3 movingPointHit(Vec3 p0, Vec3 p1) {
		Vec3 result = null;
		for (int tri = 0; tri < nTris; tri++) {
			result = movingPointHitTriangle(p0, p1, tri);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Description of the Method
	 * 
	 * @param p0
	 *            Description of the Parameter
	 * @param p1
	 *            Description of the Parameter
	 * @param tri
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	Vec3 movingPointHitTriangle(Vec3 p0, Vec3 p1, int tri) {

		planeFromTriangle(tri, _plane);
		float d = signedDistance(p1, _plane);
		if (d < 0) {
			return null;
		}
		// If p1 is still above, no hit
		// Otherwise we have to test for the segment-triangle intersection

		if (Math.abs(signedDistance(p0, _plane)) < SMALL_NUM) {
			return p0;
		}
		if (Math.abs(signedDistance(p1, _plane)) < SMALL_NUM) {
			return p1;
		}

		Vec3 hitPt = new Vec3();
		if (intersect_RayTriangle(p0, p1, tri, hitPt) == 1
				&& intersect_RayTriangle(p1, p0, tri, hitPt) == 1) {
			return hitPt;
		}

		/*
		 * if (intersect_RayTriangle(p0, p1, tri, hitPt) == 1) { return hitPt; }
		 * if (intersect_RayTriangle(p1, p0, tri, hitPt) == 1) { return hitPt; }
		 */
		return null;
	}

	// Copyright 2001, softSurfer (www.softsurfer.com)
	// This code may be freely used and modified for any purpose
	// providing that this copyright notice is included with it.
	// SoftSurfer makes no warranty for this code, and cannot be held
	// liable for any real or imagined damage resulting from its use.
	// Users of this code must verify correctness for their application.

	// Assume that classes are already given for the objects:
	// Point and Vector with
	// coordinates {float x, y, z;}
	// operators for:
	// == to test equality
	// != to test inequality
	// (Vector)0 = (0,0,0) (null vector)
	// Point = Point ± Vector
	// Vector = Point - Point
	// Vector = Scalar * Vector (scalar product)
	// Vector = Vector * Vector (cross product)
	// Line and Ray and Segment with defining points {Point P0, P1;}
	// (a Line is infinite, Rays and Segments start at P0)
	// (a Ray extends beyond P1, but a Segment ends at P1)
	// Plane with a point and a normal {Point V0; Vector n;}
	// Triangle with defining vertices {Point V0, V1, V2;}
	// Polyline and Polygon with n vertices {int n; Point *V;}
	// (a Polygon has V[n]=V[0])
	// ===================================================================

	private final static float SMALL_NUM = 0.00000001f;

	// anything that avoids division overflow

	// dot product (3D) which allows vector operations in arguments

	// intersect_RayTriangle(): intersect a ray with a 3D triangle
	// Input: a ray R, and a triangle T
	// Output: *I = intersection point (when it exists)
	// Return: -1 = triangle is degenerate (a segment or point)
	// 0 = disjoint (no intersect)
	// 1 = intersect in unique point I1
	// 2 = are in the same plane
	/**
	 * Description of the Method
	 * 
	 * @param R0
	 *            Description of the Parameter
	 * @param R1
	 *            Description of the Parameter
	 * @param T
	 *            Description of the Parameter
	 * @param I
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	int intersect_RayTriangle(Vec3 R0, Vec3 R1, int T, Vec3 I) {
		Vec3 u;
		Vec3 v;
		Vec3 n;
		// triangle vectors
		Vec3 dir;
		// triangle vectors
		Vec3 w0;
		// triangle vectors
		Vec3 w;
		// ray vectors
		float r;
		// ray vectors
		float a;
		// ray vectors
		float b;
		// params to calc ray-plane intersect

		Vec3 V0 = new Vec3(verts[tris[T][0]]);
		Vec3 V1 = new Vec3(verts[tris[T][1]]);
		Vec3 V2 = new Vec3(verts[tris[T][2]]);

		// get triangle edge vectors and plane normal
		u = new Vec3();
		u.sub(V1, V0);
		v = new Vec3();
		v.sub(V2, V0);
		n = Vec3.cross(u, v);
		// cross product
		if (Vec3.dot(n, n) == 0.0f) {
			// triangle is degenerate
			return -1;
		}
		// do not deal with this case

		dir = new Vec3();
		dir.sub(R1, R0);
		// ray direction vector
		w0 = new Vec3();
		w0.sub(R0, V0);
		a = -Vec3.dot(n, w0);
		b = Vec3.dot(n, dir);
		if (Math.abs(b) < SMALL_NUM) {
			// ray is parallel to triangle plane
			if (a == 0) {
				// ray lies in triangle plane
				return 2;
			} else {
				return 0;
			}
			// ray disjoint from plane
		}

		// get intersect point of ray with triangle plane
		r = a / b;
		if (r < 0.0) {
			// ray goes away from triangle
			return 0;
		}
		// => no intersect
		// for a segment, also test if (r > 1.0) => no intersect

		I.add(R0, Vec3.mul(dir, r));
		// intersect point of ray and plane

		// is I inside T?
		float uu;
		// intersect point of ray and plane

		// is I inside T?
		float uv;
		// intersect point of ray and plane

		// is I inside T?
		float vv;
		// intersect point of ray and plane

		// is I inside T?
		float wu;
		// intersect point of ray and plane

		// is I inside T?
		float wv;
		// intersect point of ray and plane

		// is I inside T?
		float D;
		uu = Vec3.dot(u, u);
		uv = Vec3.dot(u, v);
		vv = Vec3.dot(v, v);
		w = new Vec3();
		w.sub(I, V0);
		wu = Vec3.dot(w, u);
		wv = Vec3.dot(w, v);
		D = uv * uv - uu * vv;

		// get and test parametric coords
		float s;

		// get and test parametric coords
		float t;
		s = (uv * wv - vv * wu) / D;
		if (s < 0.0 || s > 1.0) {
			// I is outside T
			return 0;
		}
		t = (uv * wu - uu * wv) / D;
		if (t < 0.0 || (s + t) > 1.0) {
			// I is outside T
			return 0;
		}

		return 1;
		// I is in T
	}

	/**
	 * Description of the Method
	 * 
	 * @param pt
	 *            Description of the Parameter
	 * @param plane
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	private float signedDistance(Vec3 pt, float plane[]) {
		return Vec3.dot(pt, new Vec3(plane)) + plane[D];
	}

	/**
	 * Description of the Method
	 * 
	 * @param tri
	 *            Description of the Parameter
	 * @param plane
	 *            Description of the Parameter
	 */
	private void planeFromTriangle(int tri, float plane[]) {
		Vec3 V0 = new Vec3(verts[tris[tri][0]]);
		Vec3 V1 = new Vec3(verts[tris[tri][1]]);
		Vec3 V2 = new Vec3(verts[tris[tri][2]]);

		Vec3 d0 = new Vec3();
		d0.sub(V1, V0);
		Vec3 d1 = new Vec3();
		d1.sub(V2, V0);

		Vec3 n = Vec3.cross(d0, d1);
		n.normalize();

		plane[A] = n.x;
		plane[B] = n.y;
		plane[C] = n.z;
		plane[D] = -Vec3.dot(n, V0);
	}

	PImage texture = null;

	float verts[][];

	// [vertNum][xyzNum]
	float normals[][];

	// [normNum][xyzNum]
	float texCoords[][];

	// [texNum][uvNum]
	int tris[][];

	// [triNum][ptNum] -> vertexIndex
	int triNorms[][];

	// [triNum][ptNum] -> normIndex
	int triTex[][];

	// [triNum][ptNum] -> texIndex
	int quads[][];

	// [quadNum][ptNum] -> vertexIndex
	int quadNorms[][];

	// [quadNum][ptNum] -> normIndex
	int quadTex[][];

	// [quadNum][ptNum] -> texIndex

	int vertLayers[];

	Particle vertexParticles[];

	float centroids[][];

	int layerVertCounts[];

	Particle centroidParticles[];

	int nTris;

	int nQuads;

	int nVerts;

	int nNorms;

	int nTexCoords;

	int nLayers;

	boolean hasNorms = true;

	boolean hasParticles = false;

	boolean hasTexCoords = true;

	Vector layers = new Vector();

	/**
	 * Sets the texture attribute of the Surface object
	 * 
	 * @param image
	 *            The new texture value
	 */
	void setTexture(PImage image) {
		texture = image;
	}

	/**
	 * Sets the texture attribute of the Surface object
	 * 
	 * @param image
	 *            The new texture value
	 */
	void setTexture(String image) {
		texture = parent.loadImage(image);
	}

	/**
	 * Description of the Method
	 * 
	 * @param maxVerts
	 *            Description of the Parameter
	 * @param maxTris
	 *            Description of the Parameter
	 * @param maxQuads
	 *            Description of the Parameter
	 */
	void allocateArrays(int maxVerts, int maxTris, int maxQuads) {
		verts = new float[maxVerts][3];
		texCoords = new float[maxVerts][2];
		vertLayers = new int[maxVerts];
		normals = new float[maxVerts][3];
		tris = new int[maxTris][3];
		triNorms = new int[maxTris][3];
		triTex = new int[maxTris][3];
		quads = new int[maxQuads][4];
		quadNorms = new int[maxQuads][4];
		quadTex = new int[maxQuads][4];
		vertexParticles = new Particle[maxVerts];
		for (int i = 0; i < maxVerts; i++) {
			vertexParticles[i] = null;
		}
	}

	/**
	 * Adds a feature to the SpringsToOriginalShape attribute of the Surface
	 * object
	 * 
	 * @param layerName
	 *            The feature to be added to the SpringsToOriginalShape
	 *            attribute
	 * @return Description of the Return Value
	 */
	boolean addSpringsToOriginalShape(String layerName) {
		if (system == null) {
			System.err
					.println("Cannot add springs to surface without specifying PSystem first.");
			return false;
		}

		return addSpringsToOriginalShape(layerName,
				system.defaultSpringStrength, 0.0f, system.defaultSpringDamping);
	}

	/**
	 * Adds a feature to the SpringsToOriginalShape attribute of the Surface
	 * object
	 * 
	 * @param layerName
	 *            The feature to be added to the SpringsToOriginalShape
	 *            attribute
	 * @param strength
	 *            The feature to be added to the SpringsToOriginalShape
	 *            attribute
	 * @return Description of the Return Value
	 */
	boolean addSpringsToOriginalShape(String layerName, float strength) {
		if (system == null) {
			System.err
					.println("Cannot add springs to surface without specifying PSystem first.");
			return false;
		}
		return addSpringsToOriginalShape(layerName, strength, 0.0f,
				system.defaultSpringDamping);
	}

	/**
	 * Adds a feature to the SpringsToOriginalShape attribute of the Surface
	 * object
	 * 
	 * @param layerName
	 *            The feature to be added to the SpringsToOriginalShape
	 *            attribute
	 * @param strength
	 *            The feature to be added to the SpringsToOriginalShape
	 *            attribute
	 * @param restLength
	 *            The feature to be added to the SpringsToOriginalShape
	 *            attribute
	 * @param damping
	 *            The feature to be added to the SpringsToOriginalShape
	 *            attribute
	 * @return Description of the Return Value
	 */
	boolean addSpringsToOriginalShape(String layerName, float strength,
			float restLength, float damping) {
		if (system == null) {
			System.err
					.println("Cannot add springs to surface without specifying PSystem first.");
			return false;
		}
		int layerNum = layers.indexOf(layerName);
		if (layerNum < 0) {
			return false;
		}

		for (int v = 0; v < nVerts; v++) {
			if (vertLayers[v] == layerNum && vertexParticles[v] != null) {
				Particle p = new Particle(system);
				p.visible = false;
				p.init();

				p.fix();
				p.pos[0] = vertexParticles[v].pos[0];
				p.pos[1] = vertexParticles[v].pos[1];
				p.pos[2] = vertexParticles[v].pos[2];

				Spring s = new Spring(vertexParticles[v], p);
				s.strength = strength;
				s.restLength = restLength;
				s.damping = damping;
				s.visible = false;
			}
		}

		return true;
	}

	/**
	 * Description of the Method
	 * 
	 * @param layerName
	 *            Description of the Parameter
	 * @param particleType
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	boolean applyParticles(String layerName, Particle particleType) {
		if (system == null) {
			System.err
					.println("Cannot apply particles to surface without specifying PSystem first.");
			return false;
		}
		int layerNum = layers.indexOf(layerName);
		if (layerNum < 0) {
			return false;
		}

		for (int v = 0; v < nVerts; v++) {
			if (vertLayers[v] == layerNum) {
				Particle p = null;
				try {
					p = (Particle) particleType.getClass().newInstance();

				} catch (InstantiationException e) { // This will happen if
					// particle type is
					// nested

					Class[] argTypes = new Class[] { parent.getClass() };
					Constructor constructor;
					try {
						constructor = particleType.getClass().getConstructor(
								argTypes);
					} catch (NoSuchMethodException e1) {
						System.err.println("Particle type "
								+ particleType.getClass()
								+ " must have a default constructor.");
						return false;
					}

					Object[] args = new Object[] { parent };
					try {
						p = (Particle) constructor.newInstance(args);
					} catch (InstantiationException e1) {
						e1.printStackTrace();
						return false;
					} catch (IllegalAccessException e1) {
						e1.printStackTrace();
						return false;
					} catch (InvocationTargetException e1) {
						e1.printStackTrace();
						return false;
					}

				} catch (IllegalAccessException e) {
					e.printStackTrace();
					return false;
				}

				applyParticle(v, p);
			}
		}

		return true;
	}

	/**
	 * Description of the Method
	 * 
	 * @param layerName
	 *            Description of the Parameter
	 * @param particle
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	boolean attachParticleToCentroid(String layerName, Particle particle) {
		if (system == null) {
			System.err
					.println("Cannot attach particle to surface without specifying PSystem first.");
			return false;
		}

		int layerNum = layers.indexOf(layerName);
		if (layerNum < 0) {
			return false;
		}
		centroidParticles[layerNum] = particle;
		particle.fix();
		system.addParticle(particle);

		hasParticles = true;
		return true;
	}

	/**
	 * Description of the Method
	 * 
	 * @param vertex
	 *            Description of the Parameter
	 * @param particle
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	boolean applyParticle(int vertex, Particle particle) {
		if (system == null) {
			System.err
					.println("Cannot apply particle to surface without specifying PSystem first.");
			return false;
		}

		if (vertex >= nVerts) {
			return false;
		}
		vertexParticles[vertex] = particle;
		system.addParticle(particle);
		particle.pos[0] = verts[vertex][0];
		particle.pos[1] = verts[vertex][1];
		particle.pos[2] = verts[vertex][2];
		hasParticles = true;
		return true;
	}

	/**
	 * Description of the Method
	 * 
	 * @param fileName
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	boolean allocateForObject(String filename) {
		int maxTris = 0;
		int maxQuads = 0;
		int maxVerts = 0;

		try {
			BufferedReader reader = parent.createReader(filename);
			String line = reader.readLine();
			String[] words = null;
			maxVerts = 1;
			boolean process;
			while (line != null) {
				process = true;
				try {
					words = PApplet.split(line, ' ');
				} catch (Exception e) {
					process = false;
				}
				if (process) {
					if (words[0].equals("v")) {
						maxVerts++;
					} else if (words[0].equals("f")) {
						if (words.length > 4) {
							maxQuads++;
						} else {
							maxTris++;
						}
					}
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (Exception e) {
			return false;
		}
		allocateArrays(maxVerts, maxTris, maxQuads);
		return true;
	}

	/**
	 * Description of the Method
	 * 
	 * @param fileName
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	boolean load(String filename) {
		allocateForObject(filename);

		nTris = 0;
		nQuads = 0;
		nLayers = 0;
		int activeLayer = 0;

		try {
			BufferedReader reader = parent.createReader(filename);
			String line = reader.readLine();
			String[] words = null;
			nVerts = 1;
			nNorms = 1;
			nTexCoords = 1;
			boolean process;
			while (line != null) {
				process = true;
				try {
					words = PApplet.split(line, ' ');
				} catch (Exception e) {
					process = false;
				}
				if (process) {
					if (words[0].equals("g")) {
						// New layer
						if (!layers.contains(words[1])) {
							// Not a new layer
							layers.addElement(words[1]);
							nLayers++;
						}
						activeLayer = layers.indexOf(words[1]);
					} else if (words[0].equals("v")) {
						verts[nVerts][0] = PApplet.parseFloat(words[1]);
						verts[nVerts][1] = PApplet.parseFloat(words[2]);
						verts[nVerts][2] = PApplet.parseFloat(words[3]);
						vertLayers[nVerts] = activeLayer;
						nVerts++;
					} else if (words[0].equals("vn")) {
						normals[nNorms][0] = PApplet.parseFloat(words[1]);
						normals[nNorms][1] = PApplet.parseFloat(words[2]);
						normals[nNorms][2] = PApplet.parseFloat(words[3]);
						nNorms++;
					} else if (words[0].equals("vt")) {
						texCoords[nTexCoords][0] = PApplet.parseFloat(words[1]);
						texCoords[nTexCoords][1] = PApplet.parseFloat(words[2]);
						nTexCoords++;
					} else if (words[0].equals("f")) {
						if (words.length > 4) {
							for (int i = 0; i < 4; i++) {

								String vertString[] = PApplet.split(
										words[i + 1], '/');
								quads[nQuads][i] = Integer
										.parseInt(vertString[0]);
								if (hasTexCoords) {
									try {
										quadTex[nQuads][i] = Integer
												.parseInt(vertString[1]);
									} catch (Exception e) {
										hasTexCoords = false;
										System.err
												.println("Bad texture coords.");
									}
								}

								if (hasNorms) {
									try {
										quadNorms[nQuads][i] = Integer
												.parseInt(vertString[2]);
									} catch (Exception e) {
										hasNorms = false;
										System.err.println("Bad normals.");
										// e.printStackTrace();
									}
								}
							}

							nQuads++;
						} else {
							for (int i = 0; i < 3; i++) {

								String vertString[] = PApplet.split(
										words[i + 1], '/');
								tris[nTris][i] = Integer
										.parseInt(vertString[0]);
								if (hasTexCoords) {
									try {
										triTex[nTris][i] = Integer
												.parseInt(vertString[1]);
									} catch (Exception e) {
										hasTexCoords = false;
										System.err
												.println("Bad texture coords.");
									}
								}
								if (hasNorms) {
									try {
										triNorms[nTris][i] = Integer
												.parseInt(vertString[2]);
									} catch (Exception e) {
										hasNorms = false;
										System.err.println("Bad normals.");
										// e.printStackTrace();
									}
								}
							}

							nTris++;
						}
					}
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (Exception e) {
			return false;
		}

		centroids = new float[3][nLayers];
		layerVertCounts = new int[nLayers];
		centroidParticles = new Particle[nLayers];
		for (int i = 0; i < nLayers; i++) {
			centroidParticles[i] = null;
		}
		// scale(10);
		return true;
	}

	void update() {
		if (!hasParticles) {
			return;
		}

		for (int i = 0; i < nLayers; i++) {
			centroids[0][i] = 0;
			centroids[1][i] = 0;
			centroids[2][i] = 0;

			layerVertCounts[i] = 0;
		}

		for (int i = 0; i < nVerts; i++) {
			if (vertexParticles[i] != null) {
				// verts[i][0] = vertexParticles[i].pos[0];
				// verts[i][1] = vertexParticles[i].pos[1];
				// verts[i][2] = vertexParticles[i].pos[2];

				centroids[0][vertLayers[i]] += vertexParticles[i].pos[0];
				centroids[1][vertLayers[i]] += vertexParticles[i].pos[1];
				centroids[2][vertLayers[i]] += vertexParticles[i].pos[2];
			}

			layerVertCounts[vertLayers[i]]++;
		}

		for (int i = 0; i < nLayers; i++) {
			if (layerVertCounts[i] > 0) {
				centroids[0][i] /= layerVertCounts[i];
				centroids[1][i] /= layerVertCounts[i];
				centroids[2][i] /= layerVertCounts[i];

				if (centroidParticles[i] != null) {
					centroidParticles[i].setPos(centroids[0][i],
							centroids[1][i], centroids[2][i]);
				}
			}
		}

		/*
		 * float vec1[] = new float[3]; float vec2[] = new float [3]; float
		 * first[] = new float[3]; float norm[] = new float[3]; float div; for
		 * (int i = 0; i < nTris; i++) { first[0] = verts[0][tris[0][i]];
		 * first[1] = verts[1][tris[0][i]]; first[2] = verts[2][tris[0][i]];
		 * vec1[0] = verts[0][tris[1][i]]; vec1[1] = verts[1][tris[1][i]];
		 * vec1[2] = verts[2][tris[1][i]]; vec2[0] = verts[0][tris[2][i]];
		 * vec2[1] = verts[1][tris[2][i]]; vec2[2] = verts[2][tris[2][i]];
		 * vec1[0] -= first[0]; vec1[1] -= first[1]; vec1[2] -= first[2];
		 * vec2[0] -= first[0]; vec2[1] -= first[1]; vec2[2] -= first[2];
		 * norm[0] = vec1[1]*vec2[2] - vec1[2]*vec2[1]; norm[1] =
		 * vec1[2]*vec2[0] - vec1[0]*vec2[2]; norm[2] = vec1[0]*vec2[1] -
		 * vec1[1]*vec2[0]; div = mag(norm); if (div != 0) { norm[0] /= div;
		 * norm[1] /= div; norm[2] /= div; } normals[0][triNorms[0][i]] =
		 * norm[0]; normals[0][triNorms[1][i]] = norm[0];
		 * normals[0][triNorms[2][i]] = norm[0]; normals[1][triNorms[0][i]] =
		 * norm[1]; normals[1][triNorms[1][i]] = norm[1];
		 * normals[1][triNorms[2][i]] = norm[1]; normals[2][triNorms[0][i]] =
		 * norm[2]; normals[2][triNorms[1][i]] = norm[2];
		 * normals[2][triNorms[2][i]] = norm[2]; }
		 */
	}

	/**
	 * Description of the Method
	 * 
	 *@param vec
	 *            Description of the Parameter
	 *@return Description of the Return Value
	 */
	float mag(float vec[]) {
		return (float) Math.sqrt(vec[0] * vec[0] + vec[1] * vec[1] + vec[2]
				* vec[2]);
	}

	void scale(float s) {
		for (int i = 0; i < nVerts; i++) {
			verts[i][0] *= s;
			verts[i][1] *= s;
			verts[i][2] *= s;
		}
	}

	void draw() {
		boolean useTexture = hasTexCoords && texture != null;

		parent.beginShape(PConstants.TRIANGLES);

		if (useTexture) {
			parent.g.texture(texture);
			parent.g.textureMode(PConstants.NORMALIZED);
		}

		for (int i = 0; i < nTris; i++) {
			for (int v = 0; v < 3; v++) {
				if (hasNorms) {
					parent.normal(normals[triNorms[i][v]][0],
							normals[triNorms[i][v]][1],
							normals[triNorms[i][v]][2]);
				}
				if (useTexture) {
					parent.vertex(verts[tris[i][v]][0], verts[tris[i][v]][1],
							verts[tris[i][v]][2], texCoords[triTex[i][v]][0],
							texCoords[triTex[i][v]][1]);
					// parent.g.vertex_texture(texCoords[triTex[i][v]][0],
					// texCoords[triTex[i][v]][1]);
				} else {
					parent.vertex(verts[tris[i][v]][0], verts[tris[i][v]][1],
							verts[tris[i][v]][2]);
				}
			}
		}
		parent.endShape();
		parent.beginShape(PConstants.QUADS);

		if (useTexture) {
			parent.g.texture(texture);
			parent.g.textureMode(PConstants.NORMALIZED);
		}

		for (int i = 0; i < nQuads; i++) {
			for (int v = 0; v < 4; v++) {
				if (hasNorms) {
					parent.normal(normals[quadNorms[i][v]][0],
							normals[quadNorms[i][v]][1],
							normals[quadNorms[i][v]][2]);
				}
				if (useTexture) {
					parent.vertex(verts[quads[i][v]][0], verts[quads[i][v]][1],
							verts[quads[i][v]][2], texCoords[quadTex[i][v]][0],
							texCoords[quadTex[i][v]][1]);
					// parent.g.vertex_texture(texCoords[quadTex[i][v]][0],
					// texCoords[quadTex[i][v]][1]);
				} else {
					parent.vertex(verts[quads[i][v]][0], verts[quads[i][v]][1],
							verts[quads[i][v]][2]);
				}
			}
		}
		parent.endShape();
	}
}
