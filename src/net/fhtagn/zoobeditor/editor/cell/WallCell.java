package net.fhtagn.zoobeditor.editor.cell;

import net.fhtagn.zoobeditor.editor.utils.Coords;
import net.fhtagn.zoobeditor.editor.utils.Types;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

public class WallCell extends GridCell {
	static final String TAG = "WallCell";
	
	private final Types.WallType type;
	
	private final RectF drawRect;
	
	private final static Paint drawPaint = new Paint();
	static {
		drawPaint.setColor(Color.BLACK);
	}
	
	public static RectF getRectFor (Types.WallType type) {
		switch (type) {
			case T: return new RectF(0, 0, 1, 0.5f);
			case B: return new RectF(0, 0.5f, 1, 1);
			case L: return new RectF(0, 0, 0.5f, 1);
			case R: return new RectF(0.5f, 0, 1, 1);
			case W: return new RectF(0, 0, 1, 1);
			case M: return new RectF(0.25f, 0.25f, 0.75f, 0.75f);
			default:
				Log.e(TAG, "Unhandled type : " + type);
				return null;
		}
	}
	
	public WallCell (Coords c, Types.WallType type) {
		super(c);
		this.type = type;
		
		drawRect = getRectFor(type);
	}
	
	public Types.WallType getType () {
		return type;
	}
	
	public String toTileString () {
		return Types.wall2str(type);
	}
	
	public void draw (Canvas canvas) {
		//canvas.drawText(wall2str(type), 0, 0, paint);
		canvas.drawRect(drawRect, drawPaint);
	}

	@Override
  public boolean canHavePath() {
	  return false;
  }

	@Override
  public boolean isValidWaypoint() {
	  return false;
  }
	
}
