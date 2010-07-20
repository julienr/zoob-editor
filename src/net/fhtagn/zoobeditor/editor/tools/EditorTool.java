package net.fhtagn.zoobeditor.editor.tools;

import net.fhtagn.zoobeditor.editor.cell.GridCell;
import android.graphics.Canvas;

//And editor tool specify the action to take when the user click on a cell 
//(such as erasing its content, putting something in, etc...)
public abstract class EditorTool {
	//Apply the tool on the given cell, it should the new cell used to replace the old cell
	public abstract GridCell apply (GridCell cell);
	
	//Can be used to draw tool-specific informations
	//The canvas has its origin at level top-right corner and is scaled to sxy,sxy
	public void draw (Canvas canvas) {
	}
}
