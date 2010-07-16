package net.fhtagn.zoobeditor.utils;

import android.graphics.Color;
import android.util.Log;

public class Types {
	static final String TAG = "Types";
	/** WALL */
	public enum WallType {
		T, L, B, R, W, M 
	}

	static public WallType str2wall (String str) {
		if (str.equals("T"))
			return WallType.T;
		else if (str.equals("L"))
			return WallType.L;
		else if (str.equals("B"))
			return WallType.B;
		else if (str.equals("W"))
			return WallType.W;
		else if (str.equals("M"))
			return WallType.M;
		else {
			Log.e(TAG, "Unhandled wall type : " + str);
			return WallType.W;
		}
	}
	
	static public String wall2str (WallType t) {
		switch (t) {
			case T: return "T";
			case L: return "L";
			case B: return "B";
			case R: return "R";
			case W: return "W";
			case M: return "M";
			default:
				Log.e(TAG, "Unhandled wall type : " + t);
				return "";
		}
	}
	/** TANKS */
	public enum TankType {
		PLAYER, STATIC, SIMPLE, BOUNCE, BURST, SPLIT, SHIELD,
		BOSS_SIMPLE, BOSS_BOUNCE, BOSS_BURST, BOSS_SPLIT, BOSS_SHIELD
	}
	
	static public boolean isBoss (TankType t) {
		switch(t) {
			case BOSS_SIMPLE:
			case BOSS_BOUNCE:
			case BOSS_BURST:
			case BOSS_SPLIT:
			case BOSS_SHIELD:
				return true;
			default:
				return false;
		}
	}
	
	static public TankType str2tank (String str) {
		if (str.equals("player"))
			return TankType.PLAYER;
		else if (str.equals("simple"))
			return TankType.SIMPLE;
		else if (str.equals("static"))
			return TankType.STATIC;
		else if (str.equals("bounce"))
			return TankType.BOUNCE;
		else if (str.equals("burst"))
			return TankType.BURST;
		else if (str.equals("split"))
			return TankType.SPLIT;
		else if (str.equals("shield"))
			return TankType.SHIELD;
		else if (str.equals("boss_simple"))
			return TankType.BOSS_SIMPLE;
		else if (str.equals("boss_bounce"))
			return TankType.BOSS_BOUNCE;
		else if (str.equals("boss_burst"))
			return TankType.BOSS_BURST;
		else if (str.equals("boss_split"))
			return TankType.BOSS_SPLIT;
		else if (str.equals("boss_shield"))
			return TankType.BOSS_SHIELD;
		else {
			Log.e(TAG, "Unhandled tank type : " + str);
			return TankType.STATIC;
		}
	}
	
	static public String tank2str (TankType t) {
		switch (t) {
			case PLAYER: return "player";
			case SIMPLE: return "simple";
			case STATIC: return "static";
			case BOUNCE: return "bounce";
			case BURST: return "burst";
			case SPLIT: return "split";
			case SHIELD: return "shield";
			case BOSS_SIMPLE: return "boss_simple";
			case BOSS_BOUNCE: return "boss_bounce";
			case BOSS_BURST: return "boss_burst";
			case BOSS_SPLIT: return "boss_split";
			case BOSS_SHIELD: return "boss_shield";
			default:
				Log.e(TAG, "Unhandled tank type : " + t);
				return "";
		}
	}
	
	//Tank colors
	static public int GREEN = Color.rgb(102, 255, 61); 
	static public int RED = Color.rgb(255, 46, 46);
	static public int GREY = Color.rgb(160, 160, 160);
	static public int DARK_GREY = Color.rgb(102, 102, 102);
	static public int ORANGE = Color.rgb(255, 130, 26);
	static public int VIOLET = Color.rgb(247, 46, 255);
	static public int YELLOW = Color.rgb(255, 247, 46);
	static public int BLUE = Color.rgb(60, 76, 255);
	
	static public int tank2color (TankType t) {
		switch (t) {
      case PLAYER: return GREEN;
      case BOSS_SIMPLE:
      case SIMPLE: return RED;
      case BOSS_BOUNCE:
      case BOUNCE: return ORANGE;
      case STATIC: return GREY;
      case BOSS_BURST:
      case BURST: return VIOLET;
      case BOSS_SHIELD:
      case SHIELD: return YELLOW;
      case SPLIT:
      case BOSS_SPLIT: return BLUE;
      default:
      	Log.e(TAG, "Unhandled tank type : " + t);
      	return 0;
		}
	}
}
