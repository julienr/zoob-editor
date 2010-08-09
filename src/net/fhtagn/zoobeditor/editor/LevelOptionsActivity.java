package net.fhtagn.zoobeditor.editor;

import java.util.HashMap;

import net.fhtagn.zoobeditor.R;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

public class LevelOptionsActivity extends PreferenceActivity {
	private final static String TAG = "LevelOptionsActivity";
	
	private JSONObject levelObj;
	
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		if (!intent.hasExtra("json")) {
			Log.e(TAG, "no json in intent, aborting");
			finish();
		}
		
		addPreferencesFromResource(R.layout.level_options);
		HashMap<String, CheckBoxPreference> prefs = new HashMap<String, CheckBoxPreference>();
		//These are the keys of the options. For each preference, the key match the key used in level_options.xml
		//but also correspond to the associated JSON entry 
		//When one of this checkbox is checked/unchecked, the corresponding value in the JSON will be set to 0 or 1 accordingly
		String prefKeys[] = {"shadows", "boss", "bombs", "shield", "improved_firing", "bounce"};
		
		//find all preferences
		for (String key: prefKeys) {
			CheckBoxPreference p = (CheckBoxPreference)findPreference(key);
			if (p == null) {
				Log.e(TAG, "Couldn't find preference with key : " + key);
				finish();
			}
			prefs.put(key, p);
		}
		
		//set their default values based on level json
		try {
			levelObj = new JSONObject(intent.getStringExtra("json"));
			for (final String key: prefs.keySet()) {
				CheckBoxPreference preference = prefs.get(key);
				preference.setChecked(levelObj.optBoolean(key, false));
				preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
			    public boolean onPreferenceChange(Preference pref, Object newValue) {
						try {
							Log.i(TAG, key + " = " + (Boolean)newValue);
				      levelObj.put(key, (Boolean)newValue);
			      } catch (JSONException e) {
				      e.printStackTrace();
			      }
						return true;
			    }
				});
			}
		} catch (JSONException e) {
			e.printStackTrace();
			finish();
		}
	}
	
	@Override
	public void finish () {
		Intent data = new Intent();
		data.putExtra("json", levelObj.toString());
		setResult(RESULT_OK, data);
		super.finish();
	}
}
