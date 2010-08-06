package net.fhtagn.zoobeditor.editor.utils;

import net.fhtagn.zoobeditor.editor.cell.TankCell;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;

public class TankView extends RadioButton {
	private TankCell cell;
	private Context context;
	private static final int MARGIN = 8;
	
	public TankView (Context context, Types.TankType type) {
		super(context);
		this.context = context;
		cell = new TankCell(new Coords(0,0), context, type);
	}
	
	public void setType (Types.TankType type) {
		cell = new TankCell(new Coords(0,0), context, type);
	}
	
	public Types.TankType getType () {
		return cell.getType();
	}
	
	@Override
	protected void onDraw (Canvas canvas) {
		final int width = getWidth();
		final int height = getHeight();
		int size = (width<height)?width:height;
		if (isChecked()) 
			canvas.drawARGB(255, 120, 120, 120);
		else
			canvas.drawARGB(255, 255, 255, 255);
		size -= 2*MARGIN;
		canvas.translate(MARGIN, MARGIN);
		canvas.scale(size, size);
		cell.draw(canvas);
	}
}
