package net.fhtagn.zoobeditor;

import java.util.Arrays;

import net.fhtagn.zoobeditor.accounts.AuthManager;

import com.google.android.accounts.Account;
import com.google.android.accounts.AccountManager;
import com.google.android.googlelogin.GoogleLoginServiceHelper;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

public class Preferences extends PreferenceActivity {
	static private String TAG = "Preferences";
	
	private static final int REQUEST_ACCOUNTS = 1;
	
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.preferences);
		
		ListPreference accounts = (ListPreference)findPreference("account");
		accounts.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
      public boolean onPreferenceChange(Preference pref, Object newValue) {
				EditorApplication app = (EditorApplication)getApplication();
				app.accountChanged((String)newValue);
				app.syncMySeries();
	      return true;
      }
		});
		
		//Cannot do it from onResume, this would cause an infinite loop of onResume/onActivityResult
		if (!AuthManager.hasAccountManager()) { // android <= 2.0
			accounts.setEnabled(false);
			accounts.setSummary(R.string.loading);
			GoogleLoginServiceHelper.getAccount(this, REQUEST_ACCOUNTS, true);
		}
	}
	
	@Override
	protected void onResume () {
		super.onResume();
		if (AuthManager.hasAccountManager()) { //android >= 2.0
			ListPreference accounts = (ListPreference)findPreference("account");
			fillAccountsList(accounts);		
		}
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ACCOUNTS && resultCode == RESULT_OK) {
			String accounts[] = data.getExtras().getStringArray("accounts");
			ListPreference accountPref = (ListPreference)findPreference("account");
			accountPref.setEntries(accounts);
			accountPref.setEntryValues(accounts);
			Log.i(TAG, "accounts : " + Arrays.toString(accounts));
			if (accounts.length == 0)
				accountPref.setSummary(R.string.no_account_defined_summary);
			else {
				accountPref.setSummary(R.string.summary_account_preference);
				accountPref.setEnabled(true);
			}
		}
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
