package net.fhtagn.zoobeditor.editor;

import java.util.HashMap;

import net.fhtagn.zoobeditor.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

public class SerieOptionsActivity extends PreferenceActivity {
	private final static String TAG = "SerieOptionsActivity";
	private JSONObject serieObj;
	
	protected void onCreate (Bundle savedInstancestate) {
		super.onCreate(savedInstancestate);
		
		Intent intent = getIntent();
		if (!intent.hasExtra("json")) {
			Log.e(TAG, "no json in intent, aborting");
			finish();
		}
		
		addPreferencesFromResource(R.layout.serie_options);
		HashMap<String, ListPreference> prefs = new HashMap<String, ListPreference>();
		//All the items preferences are handled in the same way. 
		//When setting a preference, the user has to choose starting at which
		//level the item will become available. A list of level number is used for that
		String prefKeys[] = {"bombs", "shield", "improved_firing", "bounce" };
		
		int numLevels = 0;
		try {
			serieObj = new JSONObject(intent.getStringExtra("json"));
			if (serieObj.has("levels")) {
				numLevels = serieObj.getJSONArray("levels").length();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			finish();
		}
		
		//Create list of options
		final CharSequence[] entries = new CharSequence[numLevels];
		final CharSequence[] entriesValues = new CharSequence[numLevels];
		for (int i=0; i<numLevels; i++) {
			entries[i] = ""+i;
			if (i == 0)
				entriesValues[0] = getResources().getString(R.string.item_whole_serie);
			else
				entriesValues[i] = ""+i; 
		}
		
		//find all preferences
		for (final String key: prefKeys) {
			ListPreference p = (ListPreference)findPreference(key);
			if (p == null) {
				Log.e(TAG, "Couldn't find preference with key : " + key);
				finish();
			}
			
			if (numLevels == 0) {
				p.setEnabled(false);
			} else {
				p.setEntries(entries);
				p.setEntryValues(entriesValues);
				p.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
          public boolean onPreferenceChange(Preference pref, Object newValue) {
	          setPropertyOnAllLevels(key, Integer.parseInt((String)newValue));
	          return true;
          }
				});
			}
			prefs.put(key, p);
		}
	}
	
	
	//set the given property to the given value for all levels in the serie
	private void setPropertyOnAllLevels (String propKey, int value) {
		if (!serieObj.has("levels"))
			return;
		try {
			final JSONArray arr = serieObj.getJSONArray("levels");
			final int len = arr.length();
			for (int i=0; i<len; i++) {
				JSONObject levelObj = arr.getJSONObject(i);
				levelObj.put(propKey, value);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void finish () {
		Intent data = new Intent();
		data.putExtra("json", serieObj.toString());
		setResult(RESULT_OK, data);
		super.finish();
	}
}
