package net.fhtagn.zoobeditor.editor.tools;

import net.fhtagn.zoobeditor.editor.cell.GridCell;
import net.fhtagn.zoobeditor.editor.cell.WallCell;
import net.fhtagn.zoobeditor.editor.utils.Types;

public class WallTool extends EditorTool {
	static final String TAG = "WallTool";
	
	private final Types.WallType type;
	
	public WallTool (Types.WallType type) {
		this.type = type;
	}
	
	@Override
  public GridCell apply(GridCell cell) {
		return new WallCell(cell.getCoords(), type);
  }
}
