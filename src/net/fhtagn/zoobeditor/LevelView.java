package net.fhtagn.zoobeditor;

import net.fhtagn.zoobeditor.cell.EmptyCell;
import net.fhtagn.zoobeditor.cell.GridCell;
import net.fhtagn.zoobeditor.cell.WallCell;
import net.fhtagn.zoobeditor.cell.WallCell.WallType;
import net.fhtagn.zoobeditor.tools.EditorTool;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class LevelView extends View {
	static final int MARGIN = 4;
	static final int LEVEL_MAX_WIDTH = 12;
	static final int LEVEL_MAX_HEIGHT = 8;
	static final String TAG = "LevelView";
	
	//Size on x/y of a cell (calculated for resolution independence so a grid of 
	// at most LEVEL_MAX_WIDTH, LEVEL_MAX_HEIGHT will fit the screen
	private int sxy; 
	private int offsetX;
	private int offsetY;
	
	private final int xdim;
	private final int ydim;
	
	private int selectedCell[] = {-1,-1}; //contains coords of currently selected cell
	
	//The cells 
	private final GridCell grid[][];
	
	private EditorTool currentTool = null;
	
	public LevelView (Context context, int xdim, int ydim) {
		super(context);
		this.xdim = xdim;
		this.ydim = ydim;
		
		grid = new GridCell[xdim][ydim]; 
		for (int x=0; x<xdim; x++) {
			for (int y=0; y<ydim; y++) {
				if ((x == 0 && y == 0) ||
						(x == xdim-1 && y==ydim-1) ||
						(x == xdim-1 && y == 0) ||
						(x == 0 && y == ydim-1)) //corners
					grid[x][y] = new WallCell(WallType.W);
				else if (y == 0)
					grid[x][y] = new WallCell(WallType.T);
				else if (y == ydim-1)
					grid[x][y] = new WallCell(WallType.B);
				else if (x == 0)
					grid[x][y] = new WallCell(WallType.L);
				else if (x == xdim-1)
					grid[x][y] = new WallCell(WallType.R);
				else
					grid[x][y] = new EmptyCell();
			}
		}
		
		setFocusableInTouchMode(true); //necessary to get trackball events
	}
	
	public void setEditorTool (EditorTool tool) {
		currentTool = tool;
		Log.i(TAG, "new editor tool : " + tool);
	}
	
	@Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);

    int sx = (w - 2 * MARGIN) / LEVEL_MAX_WIDTH;
    int sy = (h - 2 * MARGIN) / LEVEL_MAX_HEIGHT;

    int size = sx < sy ? sx : sy;

    sxy = size;
    offsetX = (w - LEVEL_MAX_WIDTH * size) / 2;
    //offsetX = (w - LEVEL_MAX_WIDTH*size) - MARGIN; //Don't center the level. Put it on the right with a slight MARGIN
    offsetY = (h - LEVEL_MAX_HEIGHT * size) / 2;

    //mDstRect.set(MARGIN, MARGIN, size - MARGIN, size - MARGIN);
	}

	
	@Override
	protected void onDraw (Canvas canvas) {
		canvas.drawColor(Color.WHITE);
		
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setTextSize(sxy/2.0f);
		
		canvas.save();
		canvas.translate(offsetX, offsetY);
		canvas.scale(sxy, sxy);
		
		for (int x=0; x<=xdim; x++) {
			canvas.drawLine(x, 0, x, ydim, paint);
			for (int y=0; y<=ydim; y++) {
				canvas.drawLine(0, y, xdim, y, paint);
				
				if (x<xdim && y<ydim) {
					canvas.save();
					canvas.translate(x, y);
					grid[x][y].draw(canvas);
					canvas.restore();
				}
			}
		}
		
		if (selectedCell[0] != -1) {
			Log.v(TAG, "drawing selected");
			Rect r = new Rect(0, 0, 1, 1);
			r.offsetTo(selectedCell[0], selectedCell[1]);
			Paint rectPaint = new Paint();
			rectPaint.setColor(Color.argb(120, 255, 0, 0));
			rectPaint.setStyle(Paint.Style.FILL);
			canvas.drawRect(r, rectPaint);
		}
		
		canvas.restore();	
	}
	
	private boolean insideGrid (int x, int y) {
		return x >= 0 && x < xdim &&
					 y >= 0 && y < ydim;
	}
	
	private void selectCell (int x, int y) {
		if (!insideGrid(x,y))
			return;
		selectedCell[0] = x;
		selectedCell[1] = y;
		postInvalidate();
	}
	
	private void modifyCell () {
		if (selectedCell[0] == -1 || currentTool == null)
			return;

		final int x = selectedCell[0];
		final int y = selectedCell[1];
		Log.i(TAG, "modify Cell ("+x+","+y+")");
		grid[x][y] = currentTool.apply(grid[x][y]);
		postInvalidate();
	}
	
	@Override
	public boolean onTouchEvent (MotionEvent event) {
		int action = event.getAction();
		
		if (action == MotionEvent.ACTION_DOWN) {
      return true;
		} else if (action == MotionEvent.ACTION_UP) {
			int x = (int)event.getX();
			int y = (int)event.getY();
			
			//we subtract -sxy to recenter the click. looks like this is the best
			x = (x-MARGIN-sxy)/sxy;
			y = (y-MARGIN-sxy)/sxy;
			
			selectCell(x,y);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onTrackballEvent (final MotionEvent event) {
		int action = event.getAction();
		if (action == MotionEvent.ACTION_MOVE) { 
			//event.getX/Y return a float giving the move offset. This offset is 0.166 on the magic for example. 
			//but we're only interested in moving cell by cell (1 unit at a time), hence the conversion
			int xOff = 0;
			if (event.getX() < 0)
				xOff = -1;
			else if (event.getX() > 0)
				xOff = 1;

			int yOff = 0;
			if (event.getY() < 0)
				yOff = -1;
			else if (event.getY() > 0)
				yOff = 1;
			
			int newX = selectedCell[0]+xOff;
			int newY = selectedCell[1]+yOff;
			selectCell(newX, newY);
			return true;
		} else if (action == MotionEvent.ACTION_DOWN) {
			modifyCell();
		}
		return false;
	}
}
