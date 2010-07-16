package net.fhtagn.zoobeditor.tools;

import android.content.Context;
import net.fhtagn.zoobeditor.cell.GridCell;
import net.fhtagn.zoobeditor.cell.TankCell;
import net.fhtagn.zoobeditor.utils.Types;

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
