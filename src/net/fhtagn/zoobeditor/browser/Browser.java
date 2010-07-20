package net.fhtagn.zoobeditor.browser;

import net.fhtagn.zoobeditor.R;
import net.fhtagn.zoobeditor.editor.Editor;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class Browser extends TabActivity {
	static final String TAG = "ZoobEditor";
	static final int DIALOG_NEWLVL_ID = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;
		
		//My levels
		intent = new Intent().setClass(this, MyLevelsActivity.class);
		spec = tabHost.newTabSpec("mylevels").setIndicator(res.getString(R.string.mylevels_tab))
								  .setContent(intent);
		tabHost.addTab(spec);
		
		//Online levels
		intent = new Intent().setClass(this, OnlineLevelsActivity.class);
		spec = tabHost.newTabSpec("online").setIndicator(res.getString(R.string.online_tab))
									.setContent(intent);
		tabHost.addTab(spec);
		
		tabHost.setCurrentTab(0);
	}
	
	protected void launchEditor (int xdim, int ydim) {
		Intent i = new Intent(getApplicationContext(), Editor.class);
  	i.putExtra("xdim", xdim);
  	i.putExtra("ydim", ydim);
  	this.startActivity(i);
	}
	
	protected Dialog onCreateDialog (int id) {
		switch (id) {
			case DIALOG_NEWLVL_ID: {
				final CharSequence[] items = {"8x8", "9x8"};
				final int[][] itemsDim = {{8,8},{9,8}};
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.newlvl_dlg_title)
							 .setItems(items,
							    new DialogInterface.OnClickListener() {
								    @Override
								    public void onClick(DialogInterface dialog, int item) {
								    	launchEditor(itemsDim[item][0], itemsDim[item][1]);
								    }
							    })
							  .setCancelable(true)
							  .setNegativeButton(android.R.string.cancel,
							    new DialogInterface.OnClickListener() {
								    public void onClick(DialogInterface dialog, int id) {
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