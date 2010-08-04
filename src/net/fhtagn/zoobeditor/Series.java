package net.fhtagn.zoobeditor;

import android.net.Uri;
import android.provider.BaseColumns;

public final class Series implements BaseColumns {
	private Series () {}
	public static final Uri CONTENT_URI = Uri.parse("content://net.fhtagn.zoobgame.SerieContentProvider/series");
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/net.fhtagn.zoobgame.serie";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/net.fhtagn.zoobgame.serie";
	
	public static final String DEFAULT_SORT_ORDER = "download_date DESC";
	
	//DB fields
	public static final String ID = _ID;
	public static final String JSON = "json";
	public static final String IS_MINE = "is_mine"; //indicate whether the level has been created on this phone
	public static final String PROGRESS = "progress";
	public static final String COMMUNITY_ID = "community_id";
	
	/** The following fields are only useful for series authored by the phone user */
	public static final String UPLOAD_DATE = "upload_date";
	public static final String LAST_MODIFICATION = "last_modification";
}

