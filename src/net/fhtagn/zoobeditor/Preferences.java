package net.fhtagn.zoobeditor;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;

public class Preferences extends PreferenceActivity {
	static private String TAG = "Preferences";
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.preferences);
	}
	
	@Override
	protected void onResume () {
		super.onResume();
		ListPreference accounts = (ListPreference)findPreference("account");
		fillAccountsList(accounts);		
		accounts.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
      public boolean onPreferenceChange(Preference pref, Object newValue) {
				EditorApplication app = (EditorApplication)getApplication();
				app.syncMySeries();
	      return true;
      }
		});
	}
	
	protected void fillAccountsList (final ListPreference accountsList) {
		final Account[] accounts = AccountManager.get(this).getAccountsByType(EditorConstants.ACCOUNT_TYPE);
		if (accounts.length == 0) {
			accountsList.setSummary(R.string.no_account_defined_summary);
			accountsList.setEntries(new CharSequence[]{});
			accountsList.setEntryValues(new CharSequence[]{});
			accountsList.setEnabled(false);
		} else {
			final String[] accountsNames = new String[accounts.length];
			for (int i=0; i<accounts.length; i++)
				accountsNames[i] = accounts[i].name;
			accountsList.setEntries(accountsNames);
			accountsList.setEntryValues(accountsNames);
		}
	}
}
