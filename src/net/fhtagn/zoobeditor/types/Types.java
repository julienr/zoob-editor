package net.fhtagn.zoobeditor.types;

import android.util.Log;

public class Types {
	static final String TAG = "Wall";
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
}
