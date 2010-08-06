package net.fhtagn.zoobeditor.editor.tools;

import net.fhtagn.zoobeditor.editor.cell.GridCell;
import net.fhtagn.zoobeditor.editor.cell.WallCell;
import net.fhtagn.zoobeditor.editor.utils.Types;

public class EraseTool extends EditorTool {
	@Override
  public GridCell apply(GridCell cell) {
	  return new WallCell(cell.getCoords(), Types.WallType.E);
  }
}
