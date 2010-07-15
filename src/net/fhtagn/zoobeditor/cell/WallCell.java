package net.fhtagn.zoobeditor.cell;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

public class WallCell implements GridCell {
	static final String TAG = "WallCell";
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
	
	private final WallType type;
	
	private final RectF drawRect;
	
	private final static Paint drawPaint = new Paint();
	static {
		drawPaint.setColor(Color.BLACK);
	}
	
	public WallCell (WallType type) {
		this.type = type;
		
		switch (type) {
			case T: drawRect = new RectF(0, 0, 1, 0.5f); break;
			case B: drawRect = new RectF(0, 0.5f, 1, 1); break;
			case L: drawRect = new RectF(0, 0, 0.5f, 1); break;
			case R: drawRect = new RectF(0.5f, 0, 1, 1); break;
			case W: drawRect = new RectF(0, 0, 1, 1); break;
			case M: drawRect = new RectF(0.25f, 0.25f, 0.75f, 0.75f); break;
			default:
				Log.e(TAG, "Unhandled type : " + type);
				drawRect = null;
		}
	}
	
	public WallType getType () {
		return type;
	}
	
	public void draw (Canvas canvas) {
		//canvas.drawText(wall2str(type), 0, 0, paint);
		canvas.drawRect(drawRect, drawPaint);
	}
	
}
