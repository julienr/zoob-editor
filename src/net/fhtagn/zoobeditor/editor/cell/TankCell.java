package net.fhtagn.zoobeditor.editor.cell;

import java.util.ArrayList;

import net.fhtagn.zoobeditor.editor.utils.Coords;
import net.fhtagn.zoobeditor.editor.utils.Types;
import net.fhtagn.zoobeditor.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

public class TankCell extends GridCell {
	static final String TAG = "TankCell";
	
	private final Types.TankType type;
	private final Drawable tank;
	private Drawable shield = null;
	private ArrayList<Coords> path = null;
	
	//Paint used to draw "boss" text overlay
	private final static Paint bossTextPaint;
	private final static Paint playerTextPaint;
	static {
		bossTextPaint = new Paint();
		bossTextPaint.setTextSize(0.5f);
		bossTextPaint.setColor(Color.WHITE);
		bossTextPaint.setTextAlign(Paint.Align.CENTER);
		bossTextPaint.setShadowLayer(0.05f, 0.05f, 0.05f, Color.BLACK);
		
		playerTextPaint = new Paint();
		playerTextPaint.setTextSize(0.35f);
		playerTextPaint.setColor(Color.WHITE);
		playerTextPaint.setTextAlign(Paint.Align.CENTER);
		playerTextPaint.setShadowLayer(0.05f, 0.05f, 0.05f, Color.BLACK);
	}

	
	public TankCell(Coords c, Context context, Types.TankType type) {
		super(c);
		this.type = type;
		
		if (type == Types.TankType.BOUNCE || 
				type == Types.TankType.BOSS_BOUNCE) {
			tank = context.getResources().getDrawable(R.drawable.tank_bounce);
		} else {
			tank = context.getResources().getDrawable(R.drawable.tank);
			if (type == Types.TankType.SHIELD ||
					type == Types.TankType.BOSS_SHIELD) {
				shield = context.getResources().getDrawable(R.drawable.shield);
				shield.setBounds(0,0,1,1);
			}
		}
		
		tank.setBounds(0,0,1,1);
	}
	
	public String toTileString () {
		return "E";
	}
	
	public void setPath (ArrayList<Coords> waypoints) {
		path = new ArrayList<Coords>(waypoints);
	}
	
	public ArrayList<Coords> getPath () {
		return path;
	}
	
	public Types.TankType getType () {
		return type;
	}
	
	public void draw (Canvas canvas) {
		//Have to set the color filter here because all the tankcell 
		//share the same drawable
		tank.setColorFilter(Types.tank2color(type), PorterDuff.Mode.MULTIPLY);
		tank.draw(canvas);
		if (shield != null) {
			shield.draw(canvas);
		}
		if (Types.isBoss(type))
			canvas.drawText("Boss", 0.5f, 0.9f, bossTextPaint);
		else if (type == Types.TankType.PLAYER)
			canvas.drawText("Player", 0.5f, 0.9f, playerTextPaint);
	}

	@Override
  public boolean canHavePath() {
	  return true;
  }

	@Override
  public boolean isValidWaypoint() {
	  return true;
  }
}
