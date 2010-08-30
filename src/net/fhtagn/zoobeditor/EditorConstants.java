package net.fhtagn.zoobeditor;

import java.net.URI;

import net.fhtagn.zoobeditor.accounts.AuthManager;
import net.fhtagn.zoobeditor.accounts.ModernAuthManager;
import net.fhtagn.zoobeditor.accounts.OldAuthManager;

import com.google.android.accounts.Account;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;

public class EditorConstants {
	public static final String TAG  = "ZoobEditor";
	
	public static final String ACCOUNT_TYPE = "com.google";
	public static final String AUTH_TOKEN_TYPE = "ah";
	
	public static final int RESULT_ERROR = Activity.RESULT_FIRST_USER;
	//Used by level editor when returning a level to serieedit. The level should be inserted in the serie
	//and then the serie saved and the newly inserted level played
	public static final int RESULT_OK_PLAY = RESULT_ERROR+1;
	
	/** 
	 * Request types
	 */
	public static final int REQUEST_LEVEL_EDITOR = 0;
	public static final int REQUEST_SERIE_OPTIONS = 1;
	public static final int REQUEST_UPLOAD = 2;
	public static final int REQUEST_LEVEL_OPTIONS = 3;
	public static final int REQUEST_DELETE = 4;
	public static final int REQUEST_RATE = 5;
	
	//Used to control the behaviour of SerieEdit when a particular level of a serie is played
	public static final int REQUEST_PLAY_RETURN_TO_SERIE = 6; //return to serie editing 
	public static final int REQUEST_PLAY_RETURN_TO_EDITOR = 7; //return directly to level editing
	
	/**
	 * Colors
	 */
	
	public static final int COLOR_UPLOADED = Color.parseColor("#FF50AB86");
	public static final int COLOR_NOT_UPLOADED = Color.parseColor("#FFBC3300");
	
	/**
	 * Zoobweb
	 */
	private static final boolean production = true; 
	private static final String SERVER_URL = "http://zoobweb.appspot.com/rest";
	private static final String LOCAL_URL = "http://192.168.0.2:8080/rest";
	
	private static final String SERVER_LOGIN = "http://zoobweb.appspot.com"; 
	private static final String LOCAL_LOGIN = "http://192.168.0.2:8080";
	
	public static boolean isProd () {
		return production;
	}
	
	public static String getServerUrl () {
		if (production)
			return SERVER_URL;
		else
			return LOCAL_URL;
	}
	
	public static String getLoginUrl () {
		if (production)
			return SERVER_LOGIN;
		else
			return LOCAL_LOGIN;
	}
	
	public static String getPutUrl () {
		return getServerUrl()+"/put";
	}
	
	public static String getPutUrl (long id) {
		return getServerUrl()+"/put?id="+id;
	}
	
	public static String getListUrl () {
		return getServerUrl()+"/";
	}
	
	public static String getDeleteUrl (long id) {
		return getServerUrl()+"/delete?id="+id;
	}
	
	//Return a string that can be used to identify this author to the remote server (usually the email address)
	public static String getAuthorIdentification (Account author) {
		return author.name;
	}
	
	public static String getByAuthorListUrl (String author) { 
		return getServerUrl()+"/?author="+author;
	}
	
	public static String getDetailsUrl (long serieId) {
		return getServerUrl()+"/show?id="+serieId;
	}
	
	public static String getSummaryUrl (long serieId) {
		return getServerUrl()+"/show?id="+serieId+"&full=0";
	}

	public static String getRateUrl(long serieID, float rating) {
	  return getServerUrl()+"/rate?serie_id="+serieID+"&rating="+rating;
  }
}
