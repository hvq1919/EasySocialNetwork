package twitter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import tools.SavedStore;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;
import android.webkit.CookieManager;
import android.widget.Toast;

public class TwitterHelper {
	private static final String TAG = "TAG";

	/* The limit that Twitter request */
	private int FRIEND_LIMIT = 14;

	private Activity mContext;

	private static Twitter twitter;
	private static RequestToken requestToken;

	private OnSuccessLogin mOnSuccessLogin;

	public interface OnSuccessLogin {
		public void onSuccessLogin();
	}

	public interface OnPostTwitterCallBack {
		public void onCallBack(String message);
	}

	public interface OnGetFriendsListsSuccess {
		public void onGetFriends(ArrayList<User> user);
	}

	public TwitterHelper(Activity mContext) {
		super();
		this.mContext = mContext;

		init();
	}

	private void init() {
		/* Enabling strict mode */
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
	}

	/**
	 * To show Toast message
	 * 
	 * @param mess
	 */
	private void showToast(String mess) {
		Toast.makeText(mContext, mess, Toast.LENGTH_SHORT).show();
	}

	@SuppressWarnings("deprecation")
	public void doLogout() {
		Log.d(TAG, "Do logout");

		CookieManager.getInstance().removeAllCookie();

		SavedStore.clearTwitter();
	}

	public void doLogin(OnSuccessLogin onSuccessLogin) {
		mOnSuccessLogin = onSuccessLogin;
		final ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(Define.TWITTER_CONSUMER_KEY);
		builder.setOAuthConsumerSecret(Define.TWITTER_CONSUMER_SECRET);

		final Configuration configuration = builder.build();
		final TwitterFactory factory = new TwitterFactory(configuration);
		twitter = factory.getInstance();

		try {
			requestToken = twitter.getOAuthRequestToken(Define.TWITTER_CALLBACK_URL);
			/**
			 * Loading twitter login page on webview for authorization Once
			 * authorized, results are received at onActivityResult
			 * */
			final Intent intent = new Intent(mContext, WebViewActivity.class);
			intent.putExtra(WebViewActivity.EXTRA_URL, requestToken.getAuthenticationURL());
			mContext.startActivityForResult(intent, Define.WEBVIEW_REQUEST_CODE);

		} catch (TwitterException e) {
			showToast("Error Request Token");
		}
	}

	public void doSharePostWithImage(String message, Bitmap bm, OnPostTwitterCallBack onTwitterCallBack) {
		AccessToken accessToken = (AccessToken) SavedStore.getTwitterAccessToken();
		if (accessToken == null)
			onTwitterCallBack.onCallBack("AccessToken invalid");
		else
			new updateTwitterStatus(bm, message, onTwitterCallBack, accessToken).execute();
	}

	public void doSharePostWithImage(String message, Bitmap bm) {
		AccessToken accessToken = (AccessToken) SavedStore.getTwitterAccessToken();
		if (accessToken != null)
			new updateTwitterStatus(bm, message, accessToken).execute();
	}

	public void doSharePost(String message) {
		AccessToken accessToken = (AccessToken) SavedStore.getTwitterAccessToken();
		if (accessToken != null)
			new updateTwitterStatus(message, accessToken).execute();
	}

	public void doSharePost(String message, OnPostTwitterCallBack onTwitterCallBack) {
		AccessToken accessToken = (AccessToken) SavedStore.getTwitterAccessToken();
		if (accessToken == null)
			onTwitterCallBack.onCallBack("AccessToken invalid");
		else
			new updateTwitterStatus(message, onTwitterCallBack, accessToken).execute();
	}

	/**
	 * To get friend lists.
	 * 
	 * Notes: You only call this method in each >=15 mins. You only get 15
	 * friends in each calling this method.
	 * 
	 * @param onGetFriendsListsSuccess
	 * @param page
	 *            begin from 0
	 */
	public void doGetFriendLists(OnGetFriendsListsSuccess onGetFriendsListsSuccess, int page) {
		long[] ids = (long[]) SavedStore.getTwitterFriendList();

		if (ids == null) {
			AccessToken accessToken = (AccessToken) SavedStore.getTwitterAccessToken();
			if (accessToken == null) {
				onGetFriendsListsSuccess.onGetFriends(null);
				return;
			} else {
				if (twitter == null) {
					ConfigurationBuilder builder = new ConfigurationBuilder();
					builder.setOAuthConsumerKey(Define.TWITTER_CONSUMER_KEY);
					builder.setOAuthConsumerSecret(Define.TWITTER_CONSUMER_SECRET);
					builder.setOAuthAccessToken(accessToken.getToken());
					builder.setOAuthAccessTokenSecret(accessToken.getTokenSecret());
					Configuration conf = builder.build();
					twitter = new TwitterFactory(conf).getInstance();
				}
				try {
					long lCursor = -1;
					ids = twitter.getFriendsIDs(twitter.getId(), lCursor).getIDs();
					SavedStore.saveTwitterFriendList(ids);
				} catch (Exception e) {
					onGetFriendsListsSuccess.onGetFriends(null);
					e.printStackTrace();
					return;
				}
			}
		}

		/* Get user friends details */
		new GetFriendsList(onGetFriendsListsSuccess, ids, page * FRIEND_LIMIT).execute();
	}

	/**
	 * To get Result after login to Twitter
	 * 
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	public void onActivitResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			String verifier = data.getExtras().getString(Define.URL_TWITTER_OAUTH_VERIFIER);
			try {
				AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);

				long userID = accessToken.getUserId();
				User user = twitter.showUser(userID);

				SavedStore.saveTwitterUserObject(user);
				SavedStore.saveTwitterAccessToken(accessToken);

				if (mOnSuccessLogin != null)
					mOnSuccessLogin.onSuccessLogin();

				Log.d(TAG, "onActivitResult - username:" + user.getName());
				Log.d(TAG, "onActivitResult - profile:" + user.getBiggerProfileImageURL());

			} catch (Exception e) {
				showToast("Twitter Login Failed");
			}
		}
	}

	/**
	 * The asyncTask to post wall with image
	 * 
	 * @author quanhv
	 *
	 */
	private class updateTwitterStatus extends AsyncTask<Void, Void, Void> {
		private ProgressDialog pDialog;

		private String messCallBack;

		private Bitmap bm;
		private String message;
		private OnPostTwitterCallBack mOnPostTwitterCallBack;
		private AccessToken mAccessToken;

		public updateTwitterStatus(Bitmap bm, String message, OnPostTwitterCallBack onPostTwitterCallBack,
				AccessToken accessToken) {
			super();
			this.bm = bm;
			this.message = message;
			this.mOnPostTwitterCallBack = onPostTwitterCallBack;
			mAccessToken = accessToken;
		}

		public updateTwitterStatus(Bitmap bm, String message, AccessToken mAccessToken) {
			super();
			this.bm = bm;
			this.message = message;
			this.mAccessToken = mAccessToken;
		}

		public updateTwitterStatus(String message, OnPostTwitterCallBack onPostTwitterCallBack, AccessToken accessToken) {
			super();
			this.message = message;
			this.mOnPostTwitterCallBack = onPostTwitterCallBack;
			mAccessToken = accessToken;
		}

		public updateTwitterStatus(String message, AccessToken mAccessToken) {
			super();
			this.message = message;
			this.mAccessToken = mAccessToken;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			pDialog = new ProgressDialog(mContext);
			pDialog.setMessage("Posting to twitter...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		protected Void doInBackground(Void... args) {
			try {
				if (twitter == null) {
					ConfigurationBuilder builder = new ConfigurationBuilder();
					builder.setOAuthConsumerKey(Define.TWITTER_CONSUMER_KEY);
					builder.setOAuthConsumerSecret(Define.TWITTER_CONSUMER_SECRET);
					builder.setOAuthAccessToken(mAccessToken.getToken());
					builder.setOAuthAccessTokenSecret(mAccessToken.getTokenSecret());
					Configuration conf = builder.build();
					twitter = new TwitterFactory(conf).getInstance();
				}

				// Update status
				StatusUpdate statusUpdate = new StatusUpdate(message);
				//
				if (bm != null) {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					bm.compress(CompressFormat.PNG, 0 /* ignored for PNG */, bos);
					byte[] bitmapdata = bos.toByteArray();
					ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);

					statusUpdate.setMedia("message", bs);
				}

				twitter4j.Status response = twitter.updateStatus(statusUpdate);

				Log.d(TAG, response.getText());
				// TODO call back interface
				messCallBack = "Posted to Twitter!";
			} catch (Exception e) {
				Log.d(TAG, e.getMessage());
				messCallBack = e.getMessage();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			/* Dismiss the progress dialog after sharing */
			pDialog.dismiss();

			if (mOnPostTwitterCallBack != null)
				mOnPostTwitterCallBack.onCallBack(messCallBack);

		}

	}

	/**
	 * TO get list friends
	 * 
	 * @author quanhv
	 *
	 */
	private class GetFriendsList extends AsyncTask<Void, Void, Void> {
		private ProgressDialog pDialog;

		private long[] mIds;
		private int mStart;

		private OnGetFriendsListsSuccess mOnGetFriendsListsSuccess;

		ArrayList<User> mUsers;

		public GetFriendsList(OnGetFriendsListsSuccess onGetFriendsListsSuccess, long[] ids, int start) {
			super();
			this.mOnGetFriendsListsSuccess = onGetFriendsListsSuccess;
			mIds = ids;
			mStart = start;
			mUsers = new ArrayList<User>();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			pDialog = new ProgressDialog(mContext);
			pDialog.setMessage("Loading...");
			pDialog.setIndeterminate(false);
			pDialog.show();

		}

		@Override
		protected Void doInBackground(Void... params) {
			for (int i = mStart; i < mStart + FRIEND_LIMIT; i++) {
				if (i > mIds.length - 1)
					return null;
				try {
					User user = twitter.showUser(mIds[i]);
					mUsers.add(user);
				} catch (Exception e) {
					Log.d(TAG, "Exception Message:" + e.toString());
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			/* Dismiss the progress dialog after sharing */
			pDialog.dismiss();

			if (mOnGetFriendsListsSuccess != null)
				mOnGetFriendsListsSuccess.onGetFriends(mUsers);
		}
	}
}
