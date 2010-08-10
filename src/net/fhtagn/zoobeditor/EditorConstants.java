package net.fhtagn.zoobeditor;

import java.net.URI;

import android.accounts.Account;
import android.app.Activity;
import android.graphics.Color;

public class EditorConstants {
	public static final String TAG  = "ZoobEditor";
	
	public static final String ACCOUNT_TYPE = "com.google";
	public static final String AUTH_TOKEN_TYPE = "ah";
	
	public static final int RESULT_ERROR = Activity.RESULT_FIRST_USER;
	
	/** 
	 * Request types
	 */
	public static final int REQUEST_LEVEL_EDITOR = 0;
	public static final int REQUEST_SERIE_OPTIONS = 1;
	public static final int REQUEST_UPLOAD = 2;
	public static final int REQUEST_LEVEL_OPTIONS = 3;
	public static final int REQUEST_DELETE = 4;
	public static final int REQUEST_RATE = 5;
	
	/**
	 * Colors
	 */
	
	public static final int COLOR_UPLOADED = Color.parseColor("#FF50AB86");
	public static final int COLOR_NOT_UPLOADED = Color.parseColor("#FFBC3300");
	
	/**
	 * Zoobweb
	 */
	private static final boolean production = false; 
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
	
	public static String getByAuthorListUrl (Account author) { 
		return getServerUrl()+"/?author="+getAuthorIdentification(author);
	}
	
	public static String getDetailsUrl (int serieId) {
		return getServerUrl()+"/show?id="+serieId;
	}

	public static String getRateUrl(long serieID, float rating) {
	  return getServerUrl()+"/rate?serie_id="+serieID+"&rating="+rating;
  }
}
