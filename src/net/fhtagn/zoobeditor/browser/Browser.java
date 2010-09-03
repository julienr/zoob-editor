package net.fhtagn.zoobeditor.browser;

import net.fhtagn.zoobeditor.Common;
import net.fhtagn.zoobeditor.Preferences;
import net.fhtagn.zoobeditor.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.TabHost;
import android.widget.TabWidget;
import net.fhtagn.zoobeditor.EditorApplication;

public class Browser extends TabActivity {
	static final String TAG = "ZoobEditor";
	
	static final int DIALOG_HELP = 1;
	static final String HELP_SHOWN_PREF = "help_shown";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (!((EditorApplication)getApplication()).checkZoob(this)) {
			finish();
			return;
		}
		
		//Show Help on first run
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if (!prefs.getBoolean(HELP_SHOWN_PREF, false)) {
			Log.i(TAG, "Showing first time help");
			Common.showHelp(this);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean(HELP_SHOWN_PREF, true);
			editor.commit();
		}
		
		Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;
		
		//My levels
		intent = new Intent().setClass(this, MySeriesActivity.class);
		spec = tabHost.newTabSpec("myseries").setIndicator(res.getString(R.string.myseries_tab)/*, res.getDrawable(android.R.drawable.ic_menu_myplaces)*/)
								  .setContent(intent);
		tabHost.addTab(spec);
		
		//Downloaded
		intent = new Intent().setClass(this, DownloadedActivity.class);
		spec = tabHost.newTabSpec("downloaded").setIndicator(res.getString(R.string.downloaded_tab)/*, res.getDrawable(android.R.drawable.ic_menu_myplaces)*/)
								  .setContent(intent);
		tabHost.addTab(spec);
		
		//Online levels
		intent = new Intent().setClass(this, OnlineSeriesActivity.class);
		spec = tabHost.newTabSpec("online").setIndicator(res.getString(R.string.online_tab)/*, res.getDrawable(android.R.drawable.ic_menu_mapmode)*/)
									.setContent(intent);
		tabHost.addTab(spec);
		
		tabHost.setCurrentTab(0);
		
		//FIXME: big hack, but found no other way to achieve that.
		//Since we don't us icons in our tabs, reduce tab height. 
		TabWidget tw = getTabHost().getTabWidget();
		for (int i=0; i<tw.getChildCount(); i++) {
			final float scale = res.getDisplayMetrics().density;
			tw.getChildAt(i).getLayoutParams().height = (int)(scale*40); //40 dip for tab height (converted to pixels by using scale 
		}
		
		// light theme support
		tabHost.setBackgroundColor(Color.WHITE);
		tabHost.getTabWidget().setBackgroundColor(Color.BLACK);
	}
	
	@Override
	protected void onResume () {
		super.onResume();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}
	
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		Common.createCommonOptionsMenu(this, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		return Common.commonOnOptionsItemSelected(this, item);
	}
}