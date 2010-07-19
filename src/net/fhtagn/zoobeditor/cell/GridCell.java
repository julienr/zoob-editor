package net.fhtagn.zoobeditor.cell;

import java.util.ArrayList;

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
	
	//Set the path associated to this cell. Will most likely have no effect if canHavePath returns false
	public void setPath (ArrayList<Coords> waypoints) {
	}
	
	//Return the value that should be put in the tiles array of a level for this cell
	public abstract String toTileString ();
	
	public ArrayList<Coords> getPath () {
		return null;
	}
}
