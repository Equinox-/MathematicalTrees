package com.pi.senior.budfate.tree;

import java.util.ArrayList;
import java.util.List;

import com.pi.senior.math.Quaternion;
import com.pi.senior.math.TransMatrix;
import com.pi.senior.math.Vector3D;

public class PositionedMetamer {
	// Memater State
	private MetamerInfo stateInfo;

	// Position Parameters
	private PositionedMetamer parent;
	private List<PositionedMetamer> children = new ArrayList<PositionedMetamer>();
	private Quaternion baseRotation = new Quaternion();
	private Quaternion bonusRotation = new Quaternion();
	private float length;
	private Vector3D base;

	// Calculated Position
	private Quaternion calcRotation = new Quaternion();
	private Vector3D metamerStart = new Vector3D();
	private Vector3D metamerEnd = new Vector3D();
	private TransMatrix localToWorld = new TransMatrix();

	private int depth = 0;

	public PositionedMetamer(MetamerType type, Vector3D pos) {
		this(type, new Vector3D(), pos);
	}

	public int getDepth() {
		return depth;
	}

	public PositionedMetamer(MetamerType type, Vector3D base, Vector3D pos) {
		this.base = base;

		Vector3D u = new Vector3D(0, 0, 1);
		Vector3D v = pos.subtract(base).clone().normalize();

		this.length = pos.magnitude();

		if (u.equals(Vector3D.negative(v))) {
			baseRotation.setRaw(0, 0, 1, 0);
		} else {
			Vector3D half = u.clone().add(v).normalize();
			baseRotation.setRaw(Vector3D.dotProduct(u, half),
					Vector3D.crossProduct(u, half));
		}
		stateInfo = new MetamerInfo(type);
	}

	public PositionedMetamer(MetamerType type, PositionedMetamer parent,
			Vector3D base, Vector3D pos, boolean relative) {
		this(type, (relative ? base : parent.getLocalToWorld().inverse()
				.multiply(base).subtract(new Vector3D(0, 0, parent.length))),
				(relative ? pos : parent.getLocalToWorld().inverse()
						.multiply(pos)
						.subtract(new Vector3D(0, 0, parent.length))));
		this.parent = parent;
		// this.parent.children.add(this); TODO Strange error brah
		this.depth = parent.depth + 1;
		calculate();
	}

	public PositionedMetamer getParent() {
		return parent;
	}
	
	public float getLength() {
		return length;
	}

	public void addChild(PositionedMetamer b) {
		if (b.parent != this && b.parent != null) {
			throw new RuntimeException("This child is already claimed.");
		}
		if (!children.contains(b)) {
			children.add(b);
		}
		b.parent = this;
	}

	public void calculate() {
		calcRotation = baseRotation.clone().multiply(bonusRotation);
		if (parent != null) {
			Vector3D parentOffset = new Vector3D(0, 0, parent.length).add(base);
			metamerStart.set(parent.localToWorld.multiply(parentOffset));
			// Parent matrix * local matrix
			TransMatrix me = new TransMatrix(calcRotation, parentOffset.x,
					parentOffset.y, parentOffset.z);
			localToWorld.copy(parent.localToWorld).multiply(me);
		} else {
			metamerStart.set(base);
			localToWorld.setQuaternion(calcRotation, metamerStart.x,
					metamerStart.y, metamerStart.z);
		}
		metamerEnd = localToWorld.multiply(new Vector3D(0, 0, length));
	}

	public void calculateRecursive() {
		calculate();
		for (PositionedMetamer b : children) {
			b.calculateRecursive();
		}
	}

	public List<PositionedMetamer> getChildren() {
		return children;
	}

	public Vector3D getNodeStart() {
		return metamerStart;
	}

	public Vector3D getNodeEnd() {
		return metamerEnd;
	}

	public Vector3D getDirection() {
		return metamerEnd.clone().subtract(metamerStart);
	}

	public TransMatrix getLocalToWorld() {
		return localToWorld;
	}

	public void slerp(Quaternion from, Quaternion to, float time) {
		bonusRotation.slerp(from, to, time);
	}

	public MetamerInfo getStateInfo() {
		return stateInfo;
	}

	public boolean intersects(PositionedMetamer value) {
		float distA = value.metamerEnd.dist(metamerEnd);
		float distB = value.metamerStart.dist(metamerStart);
		return false;// distA < 0.5 && distB < 0.5;
	}
}
