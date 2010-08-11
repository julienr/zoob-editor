package net.fhtagn.zoobeditor.editor.tools;

import java.util.ArrayList;

import net.fhtagn.zoobeditor.editor.LevelView;
import net.fhtagn.zoobeditor.editor.cell.GridCell;
import net.fhtagn.zoobeditor.editor.utils.Coords;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

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
	
	private GridCell selectedCell = null;
	
	private final ArrayList<Coords> waypoints = new ArrayList<Coords>();
	
	/** Different paints */
	private static Paint selectTankPaint = new Paint();
	private static Paint linePaint = new Paint();
	private static Paint cellPaint = new Paint();
	private static Paint numberPaint = new Paint();	
	private static Paint highlightPaint = new Paint();

	static {
		selectTankPaint.setTextSize(2);
		selectTankPaint.setColor(Color.argb(120, 120, 120, 120));
		
		linePaint.setColor(Color.argb(120,0,0,255));
		linePaint.setStrokeWidth(0.2f);
		linePaint.setStyle(Paint.Style.STROKE);
		
		cellPaint.setColor(Color.argb(120,0,0,255));
		cellPaint.setStyle(Paint.Style.FILL);
		
		numberPaint.setTextSize(0.5f);
		numberPaint.setColor(Color.WHITE);
		numberPaint.setTextAlign(Paint.Align.CENTER);
		numberPaint.setTypeface(Typeface.DEFAULT_BOLD);
		
		highlightPaint.setColor(Color.argb(120, 0,0,255));
	}
	
	
	public PathTool () {
		state = STATE_NO_TANK;
		resetPath();
	}
	
	public void resetPath () {
		//tank is always first element in path (except if we don't have any path)
		if (waypoints.size() > 0) {
			Coords tankCoords = waypoints.get(0);
			waypoints.clear();
			waypoints.add(tankCoords);
		} else {
			waypoints.clear();
		}
	}
	
	@Override
	public void draw (LevelView view, Canvas canvas) {
		if (state == STATE_DRAWING_PATH) {			
			final int length = waypoints.size();
			for (int i=0; i<length; i++) {
				Coords wp = waypoints.get(i);
				//highlight cell
				canvas.save();
				canvas.translate(wp.getX(), wp.getY());
				canvas.drawRect(new RectF(0,0,1,1), cellPaint);
				canvas.restore();
				
				//draw line connecting path points
				Coords next = waypoints.get((i+1)%length);
				canvas.drawLine(wp.getX()+0.5f, wp.getY()+0.5f, next.getX()+0.5f, next.getY()+0.5f, linePaint);
				
				//cell number
				canvas.save();
				canvas.translate(wp.getX(), wp.getY());
				canvas.drawText("" + i, 0.5f, 0.7f, numberPaint);
				canvas.restore();
			}
			
			//Highlight selected tank
			canvas.save();
			canvas.translate(selectedCell.getCoords().getX(), 
											 selectedCell.getCoords().getY());
			canvas.drawRect(0, 0, 1, 1, highlightPaint);
			canvas.restore();
		} else {
			//canvas.drawRect(0, 0, 5, 1, paint);
			canvas.drawText("Select a tank", 0, view.getYDim()/2, selectTankPaint);
		}
	}
	
	public void savePath () {
		if (selectedCell != null)
			selectedCell.setPath(waypoints);
	}
	
	@Override
	public boolean supportHistory () {
		return false;
	}
	
	@Override
  public GridCell apply(GridCell cell) {
		if (state == STATE_NO_TANK && cell.canHavePath()) {
			state = STATE_DRAWING_PATH;
			selectedCell = cell;
			ArrayList<Coords> path = cell.getPath();
			if (path != null) {
				waypoints.clear();
				waypoints.addAll(path);
			} else {
				waypoints.add(cell.getCoords());
			}
			return cell;
		} else if (state == STATE_DRAWING_PATH){ //STATE_DRAWING_PATH
			if (cell.isValidWaypoint()) {
				waypoints.add(cell.getCoords());
			} 
			return cell;
		} else
			return cell;
  }

}
