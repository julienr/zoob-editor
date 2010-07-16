package net.fhtagn.zoobeditor;

import net.fhtagn.zoobeditor.cell.WallCellView;
import net.fhtagn.zoobeditor.tools.EraseTool;
import net.fhtagn.zoobeditor.tools.WallTool;
import net.fhtagn.zoobeditor.types.Types;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

public class Editor extends Activity {
	static final String TAG = "Editor";
	static final int DIALOG_WALL_ID = 0;
	static final int DIALOG_TANK_ID = 1;
	
	private LevelView levelView;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Intent i = getIntent();
		int xdim = i.getIntExtra("xdim", 12);
		int ydim = i.getIntExtra("ydim", 8);
		Log.i(TAG, "xdim="+xdim+", ydim="+ydim);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);    

		//setContentView(new LevelView(getApplicationContext(), xdim, ydim));
		setContentView(R.layout.editor);
		FrameLayout levelFrame = (FrameLayout)findViewById(R.id.levelframe);
		levelView = new LevelView(getApplicationContext(), xdim, ydim);
		levelFrame.addView(levelView);
		
		levelView.requestFocus(); //force trackball focus on level view
		
		setupCallbacks();
	}
	
	private void setupCallbacks () {
		Button wallButton = (Button)findViewById(R.id.btn_wall);
		wallButton.setOnClickListener(new OnClickListener() {
			@Override
      public void onClick(View view) {
				showDialog(DIALOG_WALL_ID);
      }
		});
		
		Button eraseButton = (Button)findViewById(R.id.btn_erase);
		eraseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				levelView.setEditorTool(new EraseTool());
			}
		});
		
		Button tankButton = (Button)findViewById(R.id.btn_tank);
		tankButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showDialog(DIALOG_TANK_ID);
			}
		});
	} 
	
	private class WallTypeAdapter extends BaseAdapter {
		private final Context context;
		public WallTypeAdapter (Context c) {
			context = c;
		}
		
		public int getCount () {
			return Types.WallType.values().length;
		}
		
		public Object getItem (int position) {
			return Types.WallType.values()[position];
		}
		
		@Override
    public long getItemId(int position) {
	    return position;
    }
		
		public View getView (int position, View convertView, ViewGroup parent) {
			Types.WallType type = Types.WallType.values()[position];
			WallCellView view;
			if (convertView != null) {
				view = (WallCellView)convertView;
				view.setType(type);
			} else {
				view = new WallCellView(context, type);
				view.setLayoutParams(new GridView.LayoutParams(50,50));
				view.setPadding(8,8,8,8);
			}
			return view;
		}
	}
	
	protected Dialog onCreateDialog (int id) {
		switch (id) {
			case DIALOG_WALL_ID: {
				GridView gridView = (GridView)getLayoutInflater().inflate(R.layout.dlg_gridview, null).findViewById(R.id.gridview);
				gridView.setAdapter(new WallTypeAdapter(getApplicationContext()));
				gridView.setOnItemClickListener(new OnItemClickListener() {
					@Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						 WallCellView cellView = (WallCellView)view;
						 levelView.setEditorTool(new WallTool(cellView.getType()));
						 dismissDialog(DIALOG_WALL_ID);
          }
				});
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.editor_wall_dlg_title)
				 			 .setView(gridView)
							 .setCancelable(true)
							 .setNegativeButton(android.R.string.cancel,
									 new DialogInterface.OnClickListener() {
								 	   public void onClick(DialogInterface dialog, int item) {
								 	  	 dismissDialog(DIALOG_WALL_ID);
								 	   }
							 		 });
				return builder.create();
			}
			case DIALOG_TANK_ID: {
				return null;
			}
			default:
				return null;
		}
	}
}
