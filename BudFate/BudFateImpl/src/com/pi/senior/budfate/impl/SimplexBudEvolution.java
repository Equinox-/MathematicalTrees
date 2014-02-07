package com.pi.senior.budfate.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.pi.senior.budfate.api.BudEvolutionScheme;
import com.pi.senior.budfate.tree.MetamerType;
import com.pi.senior.budfate.tree.PositionedMetamer;
import com.pi.senior.math.Vector3D;
import com.pi.senior.world.WorldProvider;

public class SimplexBudEvolution implements BudEvolutionScheme {
	@Override
	public void formBuds(PositionedMetamer base) {
		// The idea behind this algorithm is to place the lateral buds so they
		// lie on the surface of a cone projecting away from the end of the
		// previous node. If this is called on a branch that has already formed
		// buds it should try to distribute the buds so they are as far as
		// possible from the others.

		Vector3D planeNormal = base.getNodeEnd().clone()
				.subtract(base.getNodeStart()).normalize();
		boolean foundTerminalBud = false;
		List<Vector3D> projectedValues = new ArrayList<Vector3D>();
		projectedValues.add(Vector3D.projectOntoPlane(planeNormal,
				new Vector3D(0, 0, -1)));
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
			base.addChild(new PositionedMetamer(MetamerType.TERMINAL, base,
					new Vector3D(0, 0, 0), new Vector3D(0, 0, 1), true));
			System.out.println("Added terminal bud.");
			return;
		}

		// Clean projections
		Iterator<Vector3D> itr = projectedValues.iterator();
		while (itr.hasNext()) {
			if (itr.next().mag2() <= 0) {
				itr.remove();
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
			theoryBud = null; // TODO Multiple lateral buds? OPTIMIZIFY
			// Find the two spherically the furthest apart.
		}

		// Unproject theoretical bud
		if (theoryBud != null) {
			System.out.println("ADD LATERAL BUD: " + theoryBud);
			theoryBud.add(base.getNodeEnd());
			base.addChild(new PositionedMetamer(MetamerType.LATERAL, base,
					new Vector3D(0, 0, 0), theoryBud.normalize(), true));
		}
	}
}
