package net.fhtagn.zoobeditor.cell;

import net.fhtagn.zoobeditor.utils.Coords;
import net.fhtagn.zoobeditor.utils.Types;
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
	
	public WallCell (Coords c, Types.WallType type) {
		super(c);
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
	
	public Types.WallType getType () {
		return type;
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
