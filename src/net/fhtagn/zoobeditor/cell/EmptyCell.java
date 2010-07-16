package net.fhtagn.zoobeditor.cell;

import net.fhtagn.zoobeditor.utils.Coords;
import android.graphics.Canvas;

public class EmptyCell extends GridCell {

	public EmptyCell (Coords c) {
		super(c);
	}
	
	@Override
  public void draw(Canvas canvas) {
	  
  }

	@Override
  public boolean canHavePath() {
		return false;
  }

	@Override
  public boolean isValidWaypoint() {
	  return true;
  }
}
