package net.fhtagn.zoobeditor.browser;

import net.fhtagn.zoobeditor.R;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

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
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		//FIXME: big hack, but found no other way to achieve that.
		//Since we don't us icons in our tabs, reduce tab height. 
		TabWidget tw = getTabHost().getTabWidget();
		for (int i=0; i<tw.getChildCount(); i++) {
			final float scale = res.getDisplayMetrics().density;
			tw.getChildAt(i).getLayoutParams().height = (int)(scale*40); //40 dip for tab height (converted to pixels by using scale 
		}
	}
}