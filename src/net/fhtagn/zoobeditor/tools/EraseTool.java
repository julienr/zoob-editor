package net.fhtagn.zoobeditor.tools;

import net.fhtagn.zoobeditor.cell.EmptyCell;
import net.fhtagn.zoobeditor.cell.GridCell;

public class EraseTool extends EditorTool {
	@Override
  public GridCell apply(GridCell cell) {
	  return new EmptyCell(cell.getCoords());
  }
}
