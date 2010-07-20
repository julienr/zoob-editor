package net.fhtagn.zoobeditor.editor.tools;

import net.fhtagn.zoobeditor.editor.cell.EmptyCell;
import net.fhtagn.zoobeditor.editor.cell.GridCell;

public class EraseTool extends EditorTool {
	@Override
  public GridCell apply(GridCell cell) {
	  return new EmptyCell(cell.getCoords());
  }
}
