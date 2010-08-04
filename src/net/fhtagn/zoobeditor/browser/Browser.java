package net.fhtagn.zoobeditor.browser;

import net.fhtagn.zoobeditor.Preferences;
import net.fhtagn.zoobeditor.R;
import android.app.TabActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabWidget;

public class Browser extends TabActivity {
	static final String TAG = "ZoobEditor";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;
		
		//My levels
		intent = new Intent().setClass(this, MySeriesActivity.class);
		spec = tabHost.newTabSpec("myseries").setIndicator(res.getString(R.string.myseries_tab)/*, res.getDrawable(android.R.drawable.ic_menu_myplaces)*/)
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
	}
	
	@Override
	protected void onResume () {
		super.onResume();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}
	
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.base_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		switch (item.getItemId()) {
			case R.id.prefs:
				Intent i = new Intent(getApplicationContext(), Preferences.class);
				startActivity(i);
				return true;
		}
		return false;
	}
}