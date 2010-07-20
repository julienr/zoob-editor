package net.fhtagn.zoobeditor.editor.tools;

import net.fhtagn.zoobeditor.editor.cell.GridCell;
import net.fhtagn.zoobeditor.editor.cell.TankCell;
import net.fhtagn.zoobeditor.editor.utils.Types;
import android.content.Context;

public class TankTool extends EditorTool {
	static final String TAG = "TankTool";
	
	private final Types.TankType type;
	private final Context context;
	
	public TankTool (Context context, Types.TankType type) {
		this.type = type;
		this.context = context;
	}
	
	@Override
	public GridCell apply(GridCell cell) {
		return new TankCell(cell.getCoords(), context, type);
	}
}
