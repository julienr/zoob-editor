package net.fhtagn.zoobeditor.types;

import net.fhtagn.zoobeditor.R;
import net.fhtagn.zoobeditor.cell.TankCell;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;

public class TankView extends View {
	private TankCell cell;
	private int width, height;
	private Context context;
	
	public TankView (Context context, Types.TankType type) {
		super(context);
		this.context = context;
		cell = new TankCell(context, type);
	}
	
	public void setType (Types.TankType type) {
		cell = new TankCell(context, type);
	}
	
	@Override
	public void onSizeChanged (int w, int h, int oldW, int oldH) {
		width = w;
		height = h;
	}
	
	public Types.TankType getType () {
		return cell.getType();
	}
	
	@Override
	protected void onDraw (Canvas canvas) {
		int size = (width<height)?width:height;
		canvas.scale(size, size);
		cell.draw(canvas);
	}
}
