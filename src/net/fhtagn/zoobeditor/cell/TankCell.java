package net.fhtagn.zoobeditor.cell;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import net.fhtagn.zoobeditor.R;
import net.fhtagn.zoobeditor.types.Types;

public class TankCell implements GridCell {
	static final String TAG = "TankCell";
	
	private final Types.TankType type;
	private final Drawable tank;
	
	//Paint used to draw "boss" text overlay
	private final static Paint bossTextPaint;
	static {
		bossTextPaint = new Paint();
		bossTextPaint.setTextSize(0.5f);
		bossTextPaint.setColor(Color.WHITE);
		bossTextPaint.setTextAlign(Paint.Align.CENTER);
		bossTextPaint.setShadowLayer(0.05f, 0.05f, 0.05f, Color.BLACK);
	}

	
	public TankCell(Context context, Types.TankType type) {
		this.type = type;
		
		if (type == Types.TankType.BOUNCE || 
				type == Types.TankType.BOSS_BOUNCE)
			tank = context.getResources().getDrawable(R.drawable.tank_bounce);
		else
			tank = context.getResources().getDrawable(R.drawable.tank);
		
		tank.setBounds(0,0,1,1);
	}
	
	public ColorMatrixColorFilter createFilter (int targetColor) {
		ColorMatrix cm = new ColorMatrix();
		final float tr = Color.red(targetColor)/255.0f;
		final float tg = Color.green(targetColor)/255.0f;
		final float tb = Color.blue(targetColor)/255.0f;
		cm.set(new float[] {
				tr, 0, 0, 0, 0,
				0, tg, 0, 0, 0,
				0, 0, tb, 0, 0,
				0, 0, 0, 1, 0,
		});
		return new ColorMatrixColorFilter(cm);
	}
	
	public Types.TankType getType () {
		return type;
	}
	
	public void draw (Canvas canvas) {
		//Have to set the color filter here because all the tankcell 
		//share the same drawable
		tank.setColorFilter(Types.tank2color(type), PorterDuff.Mode.MULTIPLY);
		tank.draw(canvas);
		if (Types.isBoss(type))
			canvas.drawText("Boss", 0.5f, 0.9f, bossTextPaint);
	}
}
