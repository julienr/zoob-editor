package net.fhtagn.zoobeditor.browser;

import net.fhtagn.zoobeditor.Common;
import net.fhtagn.zoobeditor.EditorConstants;
import net.fhtagn.zoobeditor.editor.LevelView;
import net.fhtagn.zoobeditor.editor.cell.WallCell;
import net.fhtagn.zoobeditor.editor.utils.Coords;
import net.fhtagn.zoobeditor.editor.utils.Types;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class SeriePreviewGrid extends View {
	static final String TAG = "SeriePreviewGrid";
	
	private JSONObject serieObj = null;
	private Bitmap bitmap = null;
	
	//Level-specific infos
	private int sxy;
	private int offsetX;
	private int offsetY;
	
	//Some params for the grid (they are all in DP)
	private final int cellSize = 90;
	private final int verticalMargin = 10;
	private final int horizontalMargin = 10;
	
	//number of levels we can put on the same row
	private int numHoriz;
	
	private int numLevels;
	
	private final static Paint blackPaint = new Paint();
	private final static Paint whitePaint = new Paint();
	static {
		blackPaint.setColor(Color.BLACK);
		whitePaint.setColor(Color.WHITE);
	}
	
	public SeriePreviewGrid(Context context, AttributeSet attrs) {
	  super(context, attrs);
  }
	
	@Override
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);
    
    if (widthMode == MeasureSpec.UNSPECIFIED || heightMode != MeasureSpec.UNSPECIFIED) {
    	Log.e(TAG, "Need SPECIFIED width and UNSPECIFIED height");
    	return;
    }

    determineColumns (widthSize);

    heightSize = 0;
    for (int i = 0; i < numLevels; i += numHoriz) {
        heightSize += cellSize;
        if (i + numHoriz < numLevels) {
            heightSize += verticalMargin;
        }
    }
    Log.i(TAG, "height size : " + heightSize);
    setMeasuredDimension(widthSize, heightSize);
	}
	
	private void determineColumns (int availableSpace) {
		Log.i(TAG, "availableSpace : " + availableSpace);
		numHoriz = availableSpace/(Common.dp2pixels(getContext(), cellSize) + Common.dp2pixels(getContext(), horizontalMargin));
		Log.i(TAG, "num horiz series : " + numHoriz);
		
		//Limit the number of levels shown
		try {
			numLevels = Math.min(serieObj.getJSONArray("levels").length(), numHoriz*EditorConstants.PREVIEW_NUMROWS);
		} catch (JSONException e) {
			numLevels = 0;
		}
	}
	
	@Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    
    if (w == 0 || h == 0) {
    	Log.e(TAG, "size changed to ["+w+","+h+"]");
    	return;
    }
    
    int levelWidthPx = Common.dp2pixels(getContext(), cellSize);
    int levelHeightPx = (int)(levelWidthPx*(LevelView.LEVEL_MAX_HEIGHT/(float)LevelView.LEVEL_MAX_WIDTH));
    
    int sx = levelWidthPx / LevelView.LEVEL_MAX_WIDTH;
    int sy = levelHeightPx / LevelView.LEVEL_MAX_HEIGHT;

    int size = sx < sy ? sx : sy;

    sxy = size;
    offsetX = (levelWidthPx - LevelView.LEVEL_MAX_WIDTH * size) / 2;
    offsetY = (levelHeightPx - LevelView.LEVEL_MAX_HEIGHT * size) / 2;

    bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    createBitmap();
	}
	
	public void setSerie (JSONObject serieObj) {
		this.serieObj = serieObj;
	}
	
	private void drawLevel (Canvas canvas, JSONObject levelObj) throws JSONException {
    int xdim = levelObj.getInt("xdim");
    int ydim = levelObj.getInt("ydim");
    
    RectF square = new RectF(0,0,1,1);
    //Tiles
    JSONArray tiles = levelObj.getJSONArray("tiles");
    for (int y=0; y<ydim; y++) {
    	JSONArray row = tiles.getJSONArray(y);
    	for (int x=0; x<xdim; x++) {
    		String v = row.getString(x);
    		canvas.save();
  			canvas.translate(x,y);
    		if (Types.isEmpty(v)) {
    			canvas.drawRect(square, whitePaint);
    		} else {
    			Types.WallType t = Types.str2wall(v);
    			RectF rect = WallCell.getRectFor(t);
    			canvas.drawRect(rect, blackPaint);
    		}
    		canvas.restore();
    	}
    }
    //Tanks, draw simply a colored rect
		if (levelObj.has("tanks")) {
			JSONArray tanksArr = levelObj.getJSONArray("tanks");
			for (int i=0; i<tanksArr.length(); i++) {
				JSONObject tank = tanksArr.getJSONObject(i);
				Types.TankType type = Types.str2tank(tank.getString("type"));
				JSONArray cArr = tank.getJSONArray("coords");
				Coords c = new Coords(cArr.getInt(0), cArr.getInt(1));
				
				canvas.save();
				canvas.translate(c.getX(), c.getY());
				Paint paint = new Paint();
				paint.setColor(Types.tank2color(type));
				canvas.drawRect(square, paint);
				canvas.restore();
			}
		}
	}
	
	protected void createBitmap () {
		if (serieObj == null || bitmap == null) 
			return;
		try {
			Canvas canvas = new Canvas(bitmap);
			canvas.drawARGB(255,255,255,255);
			JSONArray levels = serieObj.getJSONArray("levels");
			int numCurrentRow = 0;
			
			final int colWidth = Common.dp2pixels(getContext(), cellSize);
			final int vertMargin = Common.dp2pixels(getContext(), verticalMargin);
			final int horizMargin = Common.dp2pixels(getContext(), horizontalMargin);
			
			int currentX = horizMargin;
			int currentY = vertMargin;
			
			for (int i=0; i<numLevels; i++) {
				JSONObject levelObj = levels.getJSONObject(i);
				
				canvas.save();
				canvas.translate(currentX + offsetX, currentY + offsetY);
				canvas.scale(sxy, sxy);
				drawLevel(canvas, levelObj);
				canvas.restore();
				
				numCurrentRow++;
				if (numCurrentRow == numHoriz) { 
					currentX = horizMargin;
					currentY += colWidth + vertMargin;
					numCurrentRow = 0;
				} else {
					currentX += colWidth + horizMargin;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onDraw (Canvas canvas) {
		canvas.drawARGB(255,255,255,255);
		if (bitmap == null)
			return;
		canvas.drawBitmap(bitmap, null, new Rect(0, 0, getWidth(), getHeight()), null);
	}
}
