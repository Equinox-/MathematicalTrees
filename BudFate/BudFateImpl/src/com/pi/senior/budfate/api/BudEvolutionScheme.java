package com.pi.senior.budfate.api;

import com.pi.senior.budfate.tree.PositionedMetamer;

public interface BudEvolutionScheme {
	public PositionedMetamer getNextMetamer(PositionedMetamer base);
	
	public float getMetamerCost(PositionedMetamer base, PositionedMetamer child);
}
