package com.pi.senior.world;

public class WorldProvider {
	private static World instance;

	public static void setInstance(World w) {
		instance = w;
	}

	public static World getInstance() {
		return instance;
	}

	public static long currentTimeMillis() {
		if (getInstance() == null) {
			return System.currentTimeMillis();
		} else {
			return getInstance().currentTimeMillis();
		}
	}


	public static double nextRandom() {
		if (getInstance() == null) {
			return Math.random();
		} else {
			return getInstance().nextRandom();
		}
	}
}
