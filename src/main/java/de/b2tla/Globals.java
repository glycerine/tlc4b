package de.b2tla;

import de.b2tla.tlc.TLCOutput;

public class Globals {
	public static int DEFERRED_SET_SIZE = 3;
	public static boolean GOAL = true;
	public static boolean deadlockCheck = true;
	public static boolean runTLC = true;
	public static boolean translate = true;
	public static boolean invariant = true;
	public static boolean tool = false;
	public static boolean setupConstants = false;
	public static boolean deleteOnExit = false;
	
	
	public static TLCOutput tlcOutput = null;
}