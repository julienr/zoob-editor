package net.fhtagn.zoobeditor.editor.utils;

import net.fhtagn.zoobeditor.editor.cell.WallCell;
import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

public class WallView extends View {
	private WallCell cell;
	private int width, height;
	
	public WallView (Context context, Types.WallType type) {
		super(context);
		cell = new WallCell(new Coords(0,0), type);
	}
	
	public void setType (Types.WallType type) {
		cell = new WallCell(new Coords(0,0), type);
	}
	
	@Override
	public void onSizeChanged (int w, int h, int oldW, int oldH) {
		width = w;
		height = h;
	}
	
	public Types.WallType getType () {
		return cell.getType();
	}
	
	@Override
	protected void onDraw (Canvas canvas) {
		int size = (width<height)?width:height;
		canvas.scale(size, size);
		canvas.drawARGB(255, 255, 255, 255);
		cell.draw(canvas);
	}
}
