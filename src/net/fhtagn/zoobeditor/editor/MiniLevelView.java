package net.fhtagn.zoobeditor.editor;

import java.util.ArrayList;

import net.fhtagn.zoobeditor.editor.cell.TankCell;
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

public class MiniLevelView extends View {
	public MiniLevelView(Context context, AttributeSet attrs) {
	  super(context, attrs);
  }

	private Bitmap bitmap = null;
	private JSONObject levelObj = null;
	
	private int sxy;
	private int offsetX;
	private int offsetY;
	
	private final static Paint blackPaint = new Paint();
	private final static Paint whitePaint = new Paint();
	static {
		blackPaint.setColor(Color.BLACK);
		whitePaint.setColor(Color.WHITE);
	}
	
	public void setLevel (JSONObject levelObj) {
		this.levelObj = levelObj;
		drawBitmap();
	}
	
	@Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    
    if (w == 0 || h == 0) {
    	Log.e("MiniLevelView", "size changed to ["+w+","+h+"]");
    	return;
    }
    //Log.e("MiniLevelView", "onSizeChanged to ["+w+","+h+"]");

    int sx = w / LevelView.LEVEL_MAX_WIDTH;
    int sy = h / LevelView.LEVEL_MAX_HEIGHT;

    int size = sx < sy ? sx : sy;

    sxy = size;
    offsetX = (w - LevelView.LEVEL_MAX_WIDTH * size) / 2;
    offsetY = (h - LevelView.LEVEL_MAX_HEIGHT * size) / 2;

    bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    drawBitmap();
	}
	
	protected void drawBitmap () {
		if (levelObj == null || bitmap == null)
			return;
		try {
	    int xdim = levelObj.getInt("xdim");
	    int ydim = levelObj.getInt("ydim");
	    
	    Canvas canvas = new Canvas(bitmap);
	    canvas.drawARGB(255,255,255,255);
	    canvas.translate(offsetX, offsetY);
	    canvas.scale(sxy, sxy);
	    
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
