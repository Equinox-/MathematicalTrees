package com.pi.senior.math;

/**
 * A general class representing a vector in 3D space.
 * 
 * Note: Most of the functions in this class are chainable, and do the operation
 * on the vector they were called on.
 * 
 * @author "Westin Miller"
 * 
 */
public class Vector {
	public float x, y, z;

	/**
	 * Creates a 3D vector with three floating point coordinates.
	 * 
	 * @param x
	 *            the x coordinate
	 * @param y
	 *            the y coordinate
	 * @param z
	 *            the z coordinate
	 */
	public Vector(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Creates a 3D vector on the XY plane using two coordinates. The z
	 * coordinate is assumed to be zero.
	 * 
	 * @param x
	 *            the x coordinate
	 * @param y
	 *            the y coordinate
	 */
	public Vector(float x, float y) {
		this(x, y, 0);
	}

	/**
	 * Translates this 3D vector by adding each of the three provided components
	 * to the current vector's components. This function also returns the
	 * current vector so it supports method chaining.
	 * 
	 * @param x
	 *            the x translation
	 * @param y
	 *            the y translation
	 * @param z
	 *            the z translation
	 * @return the translated vector
	 */
	public Vector translate(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	/**
	 * Adds the provided vector to the current vector and returns the current
	 * vector.
	 * 
	 * @param p
	 *            the vector to add
	 * @return the resulting vector
	 * @see Vector:subtract(Vector)
	 * @see Vector:translate(float, float, float)
	 */
	public Vector add(Vector p) {
		this.x += p.x;
		this.y += p.y;
		this.z += p.z;
		return this;
	}

	/**
	 * Subtracts the provided vector from the current vector and returns the
	 * current vector.
	 * 
	 * @param p
	 *            the vector to subtract
	 * @return the resulting vector
	 * @see Vector:add(Vector)
	 */
	public Vector subtract(Vector p) {
		this.x -= p.x;
		this.y -= p.y;
		this.z -= p.z;
		return this;
	}

	/**
	 * Multiplies each component of the current vector by the given scalar value
	 * and returns the current vector.
	 * 
	 * @param scalar
	 *            the scaling value for each component
	 * @return the resulting vector
	 */
	public Vector multiply(float scalar) {
		this.x *= scalar;
		this.y *= scalar;
		this.z *= scalar;
		return this;
	}

	/**
	 * Returns the distance squared between two points in 3D space, represented
	 * by this vector and the provided vector.
	 * 
	 * @param p
	 *            the vector to compute the distance to
	 * @return the distance squared
	 */
	public double distSquared(Vector p) {
		float dX = p.x - x, dY = p.y - y, dZ = p.z - z;
		return (dX * dX) + (dY * dY) + (dZ * dZ);
	}

	/**
	 * Returns the distance between two points in 3D space, represented by this
	 * vector and the provided vector.
	 * 
	 * @param p
	 *            the vector to compute the distance to
	 * @return the distance
	 */
	public double dist(Vector p) {
		return Math.sqrt(distSquared(p));
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

	/**
	 * Normalizes this vector so it has a magnitude of exactly one. This simply
	 * divides each component of the vector by the current magnitude of the
	 * vector.
	 * 
	 * @return this vector, normalized
	 */
	public Vector normalize() {
		double dist = dist(new Vector(0, 0, 0));
		if (dist != 0) {
			x /= dist;
			y /= dist;
			z /= dist;
		}
		return this;
	}

	/**
	 * Takes the dot product of two vectors, and returns the result.
	 * 
	 * @param u
	 *            the first vector
	 * @param v
	 *            the second vector
	 * @return the dot product
	 */
	public static float dotProduct(Vector u, Vector v) {
		return (u.x * v.x) + (u.y * v.y) + (u.z * v.z);
	}

	/**
	 * Takes the cross product of the first vector times the second vector, and
	 * returns a new vector containing the result.
	 * 
	 * @param u
	 *            the first vector
	 * @param v
	 *            the second vector
	 * @return the resulting vector
	 */
	public static Vector crossProduct(Vector u, Vector v) {
		return new Vector((u.y * v.z) - (u.z * v.y), (u.z * v.x) - (u.x * v.z),
				(u.x * v.y) - (u.y * v.x));
	}
}