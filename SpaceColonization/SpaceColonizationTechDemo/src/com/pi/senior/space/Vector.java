package com.pi.senior.space;

public class Vector {
	public float x, y, z;

	public Vector(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector(float x, float y) {
		this(x, y, 0);
	}

	public Vector translate(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public Vector subtract(Vector p) {
		this.x -= p.x;
		this.y -= p.y;
		this.z -= p.z;
		return this;
	}

	public Vector add(Vector p) {
		this.x += p.x;
		this.y += p.y;
		this.z += p.z;
		return this;
	}

	public Vector multiply(float scalar) {
		this.x *= scalar;
		this.y *= scalar;
		this.z *= scalar;
		return this;
	}

	public double dist(Vector p) {
		float dX = p.x - x, dY = p.y - y, dZ = p.z - z;
		return Math.sqrt((dX * dX) + (dY * dY) + (dZ * dZ));
	}

	@Override
	public Vector clone() {
		return new Vector(x, y, z);
	}

	@Override
	public int hashCode() {
		return (int) x << 24 ^ (int) y << 12 ^ (int) z << 6;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Vector) {
			Vector p = (Vector) o;
			float xD = p.x - x, yD = p.y - y, zD = p.z - z;
			return xD == 0f && yD == 0f && zD == 0f;
		}
		return false;
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + "," + z + ")";
	}

	public Vector translate(Vector trans) {
		return translate(trans.x, trans.y, trans.z);
	}

	public Vector normalize() {
		double dist = dist(new Vector(0, 0, 0));
		if (dist != 0) {
			x /= dist;
			y /= dist;
			z /= dist;
		}
		return this;
	}

	public static Vector normalize(Vector p) {
		return p.clone().normalize();
	}

	public static float dotProduct(Vector u, Vector v) {
		return (u.x * v.x) + (u.y * v.y) + (u.z * v.z);
	}

	public static Vector crossProduct(Vector u, Vector v) {
		return new Vector((u.y * v.z) - (u.z * v.y), (u.z * v.x) - (u.x * v.z),
				(u.x * v.y) - (u.y * v.x));
	}

	public static Vector negative(Vector p) {
		return new Vector(-p.x, -p.y, -p.z);
	}

	public Vector reverse() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	public Vector abs() {
		x = Math.abs(x);
		y = Math.abs(y);
		z = Math.abs(z);
		return this;
	}
}