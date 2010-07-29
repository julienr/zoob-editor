package net.fhtagn.zoobeditor;

import android.app.Activity;

public class EditorConstants {
	public static final String TAG  = "ZoobEditor";
	
	public static final String ACCOUNT_TYPE = "com.google";
	
	/**
	 * onActivityResult request code
	 */
	public static final int GET_LOGIN = 1;
	public static final int SEND_TO_ZOOB_WEB = 2;
	
	public static final int RESULT_ERROR = Activity.RESULT_FIRST_USER;
	
	/**
	 * Zoobweb
	 */
	private static final boolean production = true; 
	private static final String SERVER_URL = "http://zoobweb.appspot.com";
	private static final String LOCAL_URL = "http://192.168.0.2:8080";
	
	public static boolean isProd () {
		return production;
	}
	
	public static String getServerUrl () {
		if (production)
			return SERVER_URL;
		else
			return LOCAL_URL;
	}
	
	public static String getCreateUrl () {
		return getServerUrl()+"/create";
	}
	
	public static String getListUrl () {
		return getServerUrl()+"/";
	}
}
