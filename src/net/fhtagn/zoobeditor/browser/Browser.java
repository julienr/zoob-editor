package net.fhtagn.zoobeditor.browser;

import net.fhtagn.zoobeditor.R;
import android.app.TabActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class Browser extends TabActivity {
	static final String TAG = "ZoobEditor";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;
		
		//My levels
		intent = new Intent().setClass(this, MySeriesActivity.class);
		spec = tabHost.newTabSpec("myseries").setIndicator(res.getString(R.string.myseries_tab))
								  .setContent(intent);
		tabHost.addTab(spec);
		
		//Online levels
		intent = new Intent().setClass(this, OnlineSeriesActivity.class);
		spec = tabHost.newTabSpec("online").setIndicator(res.getString(R.string.online_tab))
									.setContent(intent);
		tabHost.addTab(spec);
		
		tabHost.setCurrentTab(0);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 
	}

	
	/*@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		
		showDialog(DIALOG_PROGRESS);
		authenticate(new Intent(), EditorConstants.SEND_TO_ZOOB_WEB);
	}*/
}