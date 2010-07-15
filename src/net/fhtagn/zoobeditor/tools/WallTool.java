package net.fhtagn.zoobeditor.tools;

import net.fhtagn.zoobeditor.cell.WallCell.WallType;
import net.fhtagn.zoobeditor.cell.GridCell;
import net.fhtagn.zoobeditor.cell.WallCell;
import android.util.Log;

public class WallTool implements EditorTool {
	static final String TAG = "WallTool";
	
	private final WallType type;
	
	public WallTool (WallType type) {
		this.type = type;
	}
	
	@Override
  public GridCell apply(GridCell cell) {
		return new WallCell(type);
  }
}
