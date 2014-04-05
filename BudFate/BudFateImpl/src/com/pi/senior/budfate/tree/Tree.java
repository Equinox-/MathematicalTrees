package com.pi.senior.budfate.tree;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.pi.senior.budfate.api.BudEvolutionScheme;
import com.pi.senior.budfate.impl.SimplexBudEvolution;
import com.pi.senior.math.Vector3D;
import com.pi.senior.math.VoxelGrid;
import com.pi.senior.util.NodeIterator;

public class Tree {
	private PositionedMetamer rootMetamer;
	private Map<Integer, PositionedMetamer> metamers = new HashMap<Integer, PositionedMetamer>();
	private BudEvolutionScheme budEvolution;
	private VoxelGrid<TreeVoxel> voxelGrid;

	private float nutrition = 0;

	public Tree() {
		rootMetamer = new PositionedMetamer(MetamerType.TERMINAL, new Vector3D(
				0, 0, 0), new Vector3D(0, 0, 1));
		voxelGrid = new VoxelGrid<TreeVoxel>(1f, 5);
		budEvolution = new SimplexBudEvolution();
	}

	public void calculate() {
		rootMetamer.calculateRecursive();
	}

	public Iterator<Entry<Integer, PositionedMetamer>> getPositionedMetamers() {
		return metamers.entrySet().iterator();
	}

	public PositionedMetamer getRootMetamer() {
		return rootMetamer;
	}

	public PositionedMetamer getMetamer(int i) {
		return metamers.get(i);
	}

	public void evolve() {
		nutrition += 100000;
		List<PositionedMetamer> evolution = new ArrayList<PositionedMetamer>();
		NodeIterator itr = new NodeIterator(getRootMetamer());
		while (itr.hasNext()) {
			evolution.add(itr.next());
		}
		List<Entry<Float, PositionedMetamer>> possibleSteps = new ArrayList<Entry<Float, PositionedMetamer>>();
		for (PositionedMetamer mm : evolution) {
			PositionedMetamer mChild = budEvolution.getNextMetamer(mm);
			if (mChild != null) {
				float cost = budEvolution.getMetamerCost(mm, mChild);
				possibleSteps
						.add(new AbstractMap.SimpleEntry<Float, PositionedMetamer>(
								Float.valueOf(cost), mChild));
				mChild.calculate();
			}
		}
		Collections.sort(possibleSteps,
				new Comparator<Entry<Float, PositionedMetamer>>() {
					@Override
					public int compare(Entry<Float, PositionedMetamer> arg0,
							Entry<Float, PositionedMetamer> arg1) {
						return Float.compare(arg0.getKey(), arg1.getKey());
					}
				});
		System.out.println("Available nutrition: " + nutrition);
		for (int i = 0; i < possibleSteps.size() && nutrition > 0; i++) {
			if (nutrition > possibleSteps.get(i).getKey()) {
				PositionedMetamer metamer = possibleSteps.get(i).getValue();
				Vector3D voxelLocation = metamer.getNodeStart().clone()
						.subtract(metamer.getNodeEnd()).multiply(.5f)
						.add(metamer.getNodeEnd());
				TreeVoxel treeVoxel = voxelGrid.getVoxel(voxelLocation.x,
						voxelLocation.y, voxelLocation.z);
				if (treeVoxel == null || !treeVoxel.isOccupied) {
					if (treeVoxel != null) {
						treeVoxel.isOccupied = true;
					} else {
						treeVoxel = new TreeVoxel();
						treeVoxel.isOccupied = true;
						voxelGrid.putVoxel(voxelLocation.x, voxelLocation.y,
								voxelLocation.z, treeVoxel);
					}
					possibleSteps.get(i).getValue().getParent()
							.addChild(metamer);
					nutrition -= possibleSteps.get(i).getKey();
				}
			}
		}

		getRootMetamer().calculateRecursive();
	}
}
