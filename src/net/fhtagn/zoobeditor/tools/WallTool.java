package net.fhtagn.zoobeditor.tools;

import net.fhtagn.zoobeditor.cell.GridCell;
import net.fhtagn.zoobeditor.cell.WallCell;
import net.fhtagn.zoobeditor.types.Types;

public class WallTool implements EditorTool {
	static final String TAG = "WallTool";
	
	private final Types.WallType type;
	
	public WallTool (Types.WallType type) {
		this.type = type;
	}
	
	@Override
  public GridCell apply(GridCell cell) {
		return new WallCell(type);
  }
}
