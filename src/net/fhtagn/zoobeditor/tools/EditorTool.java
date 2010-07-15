package net.fhtagn.zoobeditor.tools;

import net.fhtagn.zoobeditor.cell.GridCell;

//And editor tool specify the action to take when the user click on a cell 
//(such as erasing its content, putting something in, etc...)
public interface EditorTool {
	//Apply the tool on the given cell, it should the new cell used to replace the old cell
	public GridCell apply (GridCell cell);
}
