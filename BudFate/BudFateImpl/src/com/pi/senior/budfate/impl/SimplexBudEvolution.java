package com.pi.senior.budfate.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.pi.senior.budfate.api.BudEvolutionScheme;
import com.pi.senior.budfate.tree.MetamerType;
import com.pi.senior.budfate.tree.PositionedMetamer;
import com.pi.senior.math.Vector3D;
import com.pi.senior.world.WorldProvider;

public class SimplexBudEvolution implements BudEvolutionScheme {
	private Vector3D UP_VECTOR = new Vector3D(0, 0, 1);

	@Override
	public PositionedMetamer getNextMetamer(PositionedMetamer base) {
		// The idea behind this algorithm is to place the lateral buds so they
		// lie on the surface of a cone projecting away from the end of the
		// previous node. If this is called on a branch that has already formed
		// buds it should try to distribute the buds so they are as far as
		// possible from the others.

		Vector3D planeNormal = base.getDirection().normalize();
		boolean foundTerminalBud = false;
		List<Vector3D> projectedValues = new ArrayList<Vector3D>();

		projectedValues.add(Vector3D.projectOntoPlane(planeNormal,
				new Vector3D(0, 0, -1)));
		projectedValues
				.add(Vector3D.projectOntoPlane(planeNormal, new Vector3D(
						(float) Math.random(), (float) Math.random(), -1)));

		for (int i = 0; i < base.getChildren().size(); i++) {
			if (base.getChildren().get(i).getStateInfo().getBudType() == MetamerType.TERMINAL) {
				foundTerminalBud = true;
			} else {
				Vector3D relativePos = base.getChildren().get(i).getNodeEnd()
						.clone()
						.subtract(base.getChildren().get(i).getNodeStart());
				projectedValues.add(Vector3D.projectOntoPlane(planeNormal,
						relativePos));
			}
		}
		if (!foundTerminalBud) {
			// Add a terminal bud if one doesn't exist
			Vector3D dir = base.getDirection().normalize();
			if (!dir.equals(UP_VECTOR)) {
				//dir = Vector3D.slerp(dir, UP_VECTOR, 0.1f);
			}
			return new PositionedMetamer(MetamerType.TERMINAL, base, base
					.getNodeEnd().clone(), base.getNodeEnd().clone().add(dir),
					false);
		}

		// Clean projections
		Iterator<Vector3D> itr = projectedValues.iterator();
		while (itr.hasNext()) {
			Vector3D v = itr.next();
			if (v.mag2() <= 0) {
				itr.remove();
			} else {
				v.normalize();
			}
		}

		// Flattened theoretical bud
		Vector3D theoryBud = null;
		if (projectedValues.size() == 1 && projectedValues.get(0).mag2() > 0) {
			theoryBud = new Vector3D(-projectedValues.get(0).x,
					-projectedValues.get(0).y, -projectedValues.get(0).z);
		} else if (projectedValues.size() <= 1) {
			theoryBud = new Vector3D((float) WorldProvider.nextRandom(),
					(float) WorldProvider.nextRandom(), 0);
		} else {
			theoryBud = null;
			// TODO OPTIMIZIFY
			// Find the two spherically the farthest apart.
			int[] uses = new int[projectedValues.size()];
			float bestAngle = Float.MIN_VALUE;
			Vector3D pairA = null;
			Vector3D pairB = null;
			for (int i = 0; i < projectedValues.size(); i++) {
				if (uses[i] < 2) {
					float bestPair = Float.MAX_VALUE;
					int pairNum = -1;
					for (int j = 0; j < projectedValues.size(); j++) {
						if (uses[j] < 2 && j != i) {
							float angle = (float) Math.acos(Vector3D
									.dotProduct(projectedValues.get(i),
											projectedValues.get(j)));
							if (angle < bestPair) {
								bestPair = angle;
								pairNum = j;
							}
						}
					}
					if (pairNum > 0) {
						uses[pairNum]++;
						uses[i]++;
						float angle = (float) Math.acos(Vector3D.dotProduct(
								projectedValues.get(i),
								projectedValues.get(pairNum)));
						if (angle > bestAngle) {
							pairA = projectedValues.get(i);
							pairB = projectedValues.get(pairNum);
							bestAngle = angle;
						}
					}
				}
			}

			// Find an unused pair
			{
				int i = -1;
				int j = -1;
				for (int v = 0; v < projectedValues.size(); v++) {
					if (uses[v] < 2) {
						if (i == -1) {
							i = v;
						} else {
							j = v;
							break;
						}
					}
				}
				if (i > -1 && j > -1) {
					uses[j]++;
					uses[i]++;
					float angle = (float) Math.acos(Vector3D.dotProduct(
							projectedValues.get(i), projectedValues.get(j)));
					if (angle > bestAngle) {
						pairA = projectedValues.get(i);
						pairB = projectedValues.get(j);
						bestAngle = angle;
					}
				}
			}

			if (pairA != null && pairB != null) {
				theoryBud = Vector3D.slerp(pairA, pairB, .5f).normalize();
				// What is better? Original theory or negated theory.
				{
					double aGood = Double.MAX_VALUE;
					double bGood = Double.MAX_VALUE;
					Vector3D b = Vector3D.negative(theoryBud);
					for (Vector3D proj : projectedValues) {
						double dotA = Math.abs(Math.acos(Vector3D.dotProduct(
								proj, theoryBud)));
						double dotB = Math.abs(Math.acos(Vector3D.dotProduct(
								proj, b)));
						if (dotA < aGood) {
							aGood = dotA;
						}
						if (dotB < bGood) {
							bGood = dotB;
						}
					}
					if (aGood < 1e-2 || bGood < 1e-2) {
						theoryBud = null;
						// Can't evolve; too close to another one
					} else if (bGood > aGood) {
						theoryBud = b;
					}
				}
			}
		}

		// Unproject theoretical bud
		if (theoryBud != null) {
			theoryBud.add(base.getNodeEnd());
			return new PositionedMetamer(MetamerType.LATERAL, base,
					new Vector3D(0, 0, 0), theoryBud.normalize(), true);
		}
		return null;
	}

	@Override
	public float getMetamerCost(PositionedMetamer base, PositionedMetamer child) {
		float divergence = (float) Math
				.abs(Math.acos(Vector3D.dotProduct(base.getDirection()
						.normalize(), child.getDirection().normalize())));
		float minSeparation = (float) (Math.PI);
		for (PositionedMetamer s : base.getChildren()) {
			float angle = (float) Math.abs(Math.acos(Vector3D.dotProduct(s
					.getDirection().normalize(), child.getDirection()
					.normalize())));
			if (s == child) {
				return Float.MAX_VALUE; // Already exists
			}
			if (angle < minSeparation) {
				minSeparation = angle;
			}
		}
		return (float) (divergence
				+ (base.getChildren().size() / minSeparation) + (base
				.getDepth() * base.getDepth() / 2.0));
	}
}
