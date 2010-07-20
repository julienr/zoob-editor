package net.fhtagn.zoobeditor.editor.cell;

import net.fhtagn.zoobeditor.editor.utils.Coords;
import android.graphics.Canvas;

public class EmptyCell extends GridCell {

	public EmptyCell (Coords c) {
		super(c);
	}
	
	@Override
  public void draw(Canvas canvas) {
	  
  }
	
	public String toTileString () {
		return "E";
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
