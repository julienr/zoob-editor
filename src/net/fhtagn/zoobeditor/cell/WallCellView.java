package net.fhtagn.zoobeditor.cell;

import net.fhtagn.zoobeditor.types.Types;
import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

public class WallCellView extends View {
	private WallCell cell;
	int width, height;
	
	public WallCellView (Context context, Types.WallType type) {
		super(context);
		cell = new WallCell(type);
	}
	
	public void setType (Types.WallType type) {
		cell = new WallCell(type);
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
