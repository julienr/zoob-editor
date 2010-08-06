package net.fhtagn.zoobeditor.editor.utils;

import net.fhtagn.zoobeditor.editor.cell.WallCell;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;

public class WallView extends RadioButton {
	private WallCell cell;
	private static final int MARGIN = 8;
	
	public WallView (Context context, Types.WallType type) {
		super(context);
		cell = new WallCell(new Coords(0,0), type);
	}
	
	public void setType (Types.WallType type) {
		cell = new WallCell(new Coords(0,0), type);
	}
	
	public Types.WallType getType () {
		return cell.getType();
	}
	
	@Override
	protected void onDraw (Canvas canvas) {
		final int width = getWidth();
		final int height = getHeight();
		if (isChecked()) 
			canvas.drawARGB(255, 120, 120, 120);
		else
			canvas.drawARGB(255, 255, 255, 255);
		int size = (width<height)?width:height;
		size -= 2*MARGIN;
		canvas.translate(MARGIN, MARGIN);
		canvas.scale(size, size);
		
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(0.1f);
		canvas.drawRect(new Rect(0,0,1,1), paint);
		cell.draw(canvas);
	}
}
