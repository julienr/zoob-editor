package net.fhtagn.zoobeditor.cell;

import net.fhtagn.zoobeditor.utils.Coords;
import android.graphics.Canvas;

public abstract class GridCell {
	private final Coords coords;
	
	public GridCell (Coords c) {
		this.coords = c;
	}
	
	public Coords getCoords () {
		return coords;
	}
	
	public abstract void draw (Canvas canvas);
	
	//Should return true if this cell can be used as a waypoint for a path
	public abstract boolean isValidWaypoint ();
	
	//Should return true if the content represented by this cell can have a path associated to it
	public abstract boolean canHavePath ();
}
