package net.fhtagn.zoobeditor;

import net.fhtagn.zoobeditor.tools.EraseTool;
import net.fhtagn.zoobeditor.tools.WallTool;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

public class Editor extends Activity {
	static final String TAG = "Editor";
	static final int DIALOG_WALL_ID = 0;
	
	private LevelView levelView;
	private Button wallButton;
	private Button eraseButton;
	
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
		wallButton = (Button)findViewById(R.id.btn_wall);
		wallButton.setOnClickListener(new OnClickListener() {
			@Override
      public void onClick(View view) {
				showDialog(DIALOG_WALL_ID);
      }
		});
		
		eraseButton = (Button)findViewById(R.id.btn_erase);
		eraseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				levelView.setEditorTool(new EraseTool());
			}
		});
	} 
	
	protected Dialog onCreateDialog (int id) {
		switch (id) {
			case DIALOG_WALL_ID: {
				final CharSequence[] items = {"L", "T", "B", "R", "M", "W"};
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.editor_wall_dlg_title)
							 .setItems(items,
									 new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int item) {
											levelView.setEditorTool(new WallTool(items[item].toString()));
										}
									 })
							 .setCancelable(true)
							 .setNegativeButton(R.string.cancel,
									 new DialogInterface.OnClickListener() {
								 	   public void onClick(DialogInterface dialog, int item) {
								 	  	 dialog.cancel();
								 	   }
							 		 });
				return builder.create();
				
			}
			default:
				return null;
		}
	}
}
