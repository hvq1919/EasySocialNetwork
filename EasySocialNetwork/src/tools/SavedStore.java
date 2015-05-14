package tools;

import java.io.IOException;
import java.io.Serializable;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.example.main.MainApplication;


public class SavedStore {

	private static final String PREFS_KEY = "account-session";
	private static final String PREFS_TOKEN = "prefsOauthToken";

	/* Using for facebook */
	private static final String PREFS_FB_USER_OBJECT = "fb_user_object";

	/* Using for Twitter */
	private static final String PREFS_Twitter_USER_OBJECT = "twitter_user_object";
	private static final String PREFS_Twitter_Access_Token = "twitter_access_token";
	private static final String PREFS_Twitter_Friend_List_Id = "Twitter_Friend_List_Id";

	/**
	 * Gets the token.
	 *
	 * @return the token
	 */
	public static String getToken() {
		Context context = MainApplication.getMainApplication().getApplicationContext();
		SharedPreferences mShared = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		return mShared.getString(PREFS_TOKEN, "");
	}

	/**
	 * Edits the access token.
	 *
	 * @param accessToken
	 *            the access token
	 * @return true, if successful
	 */
	public static boolean editAccessToken(String accessToken) {
		Context context = MainApplication.getMainApplication().getApplicationContext();
		Editor editor = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE).edit();
		editor.putString(PREFS_TOKEN, accessToken);
		return editor.commit();
	}

	/**
	 * Save object.
	 *
	 * @param objKey
	 *            the obj key
	 * @param dataObj
	 *            the data obj
	 * @return true, if successful
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static boolean saveObject(String objKey, Serializable dataObj) throws IOException {
		Context context = MainApplication.getMainApplication().getApplicationContext();
		Editor editor = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE).edit();
		editor.putString(objKey, ObjectSerializer.serialize(dataObj));
		return editor.commit();
	}

	/**
	 * Gets the object.
	 *
	 * @param objKey
	 *            the obj key
	 * @return the object
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static Object getObject(String objKey) throws IOException {
		Context context = MainApplication.getMainApplication().getApplicationContext();
		SharedPreferences savedSession = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		Object dataObj = (Object) ObjectSerializer.deserialize(savedSession.getString(objKey, null));
		return dataObj;
	}

	/**
	 * Save string.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return true, if successful
	 */
	public static boolean saveString(String key, String value) {
		Context context = MainApplication.getMainApplication().getApplicationContext();
		Editor editor = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE).edit();
		editor.putString(key, value);
		return editor.commit();
	}

	/**
	 * Gets the string.
	 *
	 * @param key
	 *            the key
	 * @param defValue
	 *            the default value
	 * @return the string
	 */
	public static String getString(String key, String defValue) {
		Context context = MainApplication.getMainApplication().getApplicationContext();
		SharedPreferences savedSession = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		return savedSession.getString(key, defValue);
	}

	/**
	 * Save boolean.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return true, if successful
	 */
	public static boolean saveBoolean(String key, boolean value) {
		Context context = MainApplication.getMainApplication().getApplicationContext();
		Editor editor = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE).edit();
		editor.putBoolean(key, value);
		return editor.commit();
	}

	/**
	 * Gets the boolean.
	 *
	 * @param key
	 *            the key
	 * @param defValue
	 *            the default value
	 * @return the boolean
	 */
	public static boolean getBoolean(String key, boolean defValue) {
		Context context = MainApplication.getMainApplication().getApplicationContext();
		SharedPreferences savedSession = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		return savedSession.getBoolean(key, defValue);
	}

	/**
	 * Clear.
	 *
	 * @param objKey
	 *            the obj key
	 */
	public static void clear(String objKey) {
		Context context = MainApplication.getMainApplication().getApplicationContext();
		Editor editor = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE).edit();
		editor.remove(objKey);
		editor.commit();
	}

	// --------------- FaceBook SharedPreferences ---------- //
	/**
	 * To save fb user object
	 * 
	 * @param dataObj
	 * @return
	 */
	public static boolean saveFbUserObject(Serializable dataObj) {
		try {
			return saveObject(PREFS_FB_USER_OBJECT, dataObj);
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * TO get fb user object
	 * 
	 * @return
	 */
	public static Object getFbUserObject() {
		try {
			return getObject(PREFS_FB_USER_OBJECT);
		} catch (IOException e) {
			return null;
		}
	} // -------- #End FaceBook ------------//

	
	
	// --------------- Twitter SharedPreferences ---------- //
	/**
	 * To save Twitter user object
	 * 
	 * @param dataObj
	 * @return
	 */
	public static boolean saveTwitterUserObject(Serializable dataObj) {
		try {
			return saveObject(PREFS_Twitter_USER_OBJECT, dataObj);
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * TO get Twitter user object
	 * 
	 * @return
	 */
	public static Object getTwitterUserObject() {
		try {
			return getObject(PREFS_Twitter_USER_OBJECT);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * To save Twitter user object
	 * 
	 * @param dataObj
	 * @return
	 */
	public static boolean saveTwitterAccessToken(Serializable dataObj) {
		try {
			return saveObject(PREFS_Twitter_Access_Token, dataObj);
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * TO get Twitter user object
	 * 
	 * @return
	 */
	public static Object getTwitterAccessToken() {
		try {
			return getObject(PREFS_Twitter_Access_Token);
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * To save Twitter Friend List
	 * 
	 * @param dataObj
	 * @return
	 */
	public static boolean saveTwitterFriendList(Serializable dataObj) {
		try {
			return saveObject(PREFS_Twitter_Friend_List_Id, dataObj);
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * TO get Twitter Friend List
	 * 
	 * @return Array of id(long)
	 */
	public static Object getTwitterFriendList() {
		try {
			return getObject(PREFS_Twitter_Friend_List_Id);
		} catch (IOException e) {
			return null;
		}
	}
	
	public static void clearTwitter(){
		clear(PREFS_Twitter_USER_OBJECT);
		clear(PREFS_Twitter_Access_Token);
		clear(PREFS_Twitter_Friend_List_Id);
	}

	// ----- #End Twitter --- //
	
}
