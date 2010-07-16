package net.fhtagn.zoobeditor.tools;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import net.fhtagn.zoobeditor.LevelView;
import net.fhtagn.zoobeditor.cell.GridCell;
import net.fhtagn.zoobeditor.utils.Coords;

public class PathTool extends EditorTool {
	static final String TAG = "PathTool";
	/**
	 * This tool has 2 states. 
	 * The user first has to select the tank for which he will set a path (by clicking on it)
	 * Then, he can draw the path.
	 */
	static final int STATE_NO_TANK = 0;
	static final int STATE_DRAWING_PATH = 1;
	
	private int state;
	
	private Coords selectedTank = null;
	
	private final ArrayList<Coords> waypoints = new ArrayList<Coords>();
	
	public PathTool () {
		state = STATE_NO_TANK;
		resetPath();
	}
	
	public void resetPath () {
		waypoints.clear();
	}
	
	@Override
	public void draw (Canvas canvas) {
		if (state == STATE_DRAWING_PATH) {
			final int length = waypoints.size();
			for (int i=0; i<length; i++) {
				Coords wp = waypoints.get(i);
				canvas.save();
				canvas.translate(wp.getX(), wp.getY());
				Paint paint = new Paint();
				paint.setTextSize(0.5f);
				paint.setColor(Color.BLACK);
				paint.setTextAlign(Paint.Align.CENTER);
				canvas.drawText("" + i, 0.5f, 0.9f, paint);
				canvas.restore();
			}
			
			//Highlight selected tank
			canvas.save();
			canvas.translate(selectedTank.getX(), selectedTank.getY());
			Paint paint = new Paint();
			paint.setColor(Color.argb(120, 0,0,255));
			canvas.drawRect(0, 0, 1, 1, paint);
			canvas.restore();
		} else {
			Paint paint = new Paint();
			paint.setTextSize(2);
			paint.setColor(Color.argb(120, 120, 120, 120));
			canvas.drawRect(0, 0, 5, 1, paint);
			canvas.drawText("Select a tank",0, 1, paint);
		}
	}
	
	public void savePath () {
		//FIXME: todo
	}
	
	@Override
  public GridCell apply(GridCell cell) {
		if (state == STATE_NO_TANK && cell.canHavePath()) {
			Log.i(TAG, "Tank selected");
			state = STATE_DRAWING_PATH;
			selectedTank = cell.getCoords();
			return cell;
		} else if (state == STATE_DRAWING_PATH){ //STATE_DRAWING_PATH
			Log.i(TAG, "path drawing");
			if (cell.isValidWaypoint()) {
				Log.i(TAG, "adding waypoint");
				waypoints.add(cell.getCoords());
			} 
			return cell;
		} else
			return cell;
  }

}
