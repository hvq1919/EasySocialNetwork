package facebook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequest.GraphJSONArrayCallback;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.Sharer.Result;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareLinkContent.Builder;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

public class FacebookHelper {
	private static final String TAG = "TAG";
	// private static final List<String> PERMISSIONS_PUBLIC =
	// Arrays.asList("publish_actions");
	private static final List<String> PERMISSIONS_READING = Arrays.asList("public_profile", "user_birthday", "email",
			"user_friends");

	// private final String PENDING_ACTION_BUNDLE_KEY = "PendingAction";
	//
	// private PendingAction pendingAction = PendingAction.NONE;
	//
	// private enum PendingAction {
	// NONE, POST_PHOTO, POST_STATUS_UPDATE
	// }

	private Activity mContext;

	private CallbackManager callbackManager;
	private OnLoginSuccess mOnLoginSuccess;
	private OnGetFriendListSuccess mOnGetFriendListSuccess;

	private AccessToken mAccessToken;
	private ShareDialog shareDialog;
	private FacebookCallback<Result> shareCallBack;
	private LoginButton mbtnLoginButton;
	private FacebookCallback<LoginResult> facebookCallBack = new FacebookCallback<LoginResult>() {
		@Override
		public void onSuccess(LoginResult result) {

			mAccessToken = result.getAccessToken();
			makeFacebookUserRequest(mAccessToken);
			makeUserFriendListsResquest(mAccessToken);
		}

		@Override
		public void onError(FacebookException error) {
			if (mOnLoginSuccess != null)
				mOnLoginSuccess.onLoginSuccess(null);
		}

		@Override
		public void onCancel() {
			if (mOnLoginSuccess != null)
				mOnLoginSuccess.onLoginSuccess(null);
		}
	};

	// ------------------ Interfaces ------------------- //
	public interface OnGetFriendListSuccess {
		public void onGetFriendList(JSONObject jsonObject);
	}

	public interface OnLoginSuccess {
		public void onLoginSuccess(FacebookUser facebookUser);
	}

	public FacebookHelper(Activity mContext) {
		super();
		this.mContext = mContext;
		init();
	}

	public FacebookHelper(Activity mContext, FacebookCallback<Result> callback) {
		super();
		this.mContext = mContext;
		shareCallBack = callback;
		init();
	}

	private void init() {
		FacebookSdk.sdkInitialize(mContext.getApplicationContext());
		callbackManager = CallbackManager.Factory.create();

		mbtnLoginButton = new LoginButton(mContext);
		mbtnLoginButton.setReadPermissions(PERMISSIONS_READING);
		// mbtnLoginButton.setPublishPermissions(PERMISSIONS_PUBLIC);

		mbtnLoginButton.registerCallback(callbackManager, facebookCallBack);

		/* Login init */
		// LoginManager.getInstance().registerCallback(callbackManager,
		// facebookCallBack);

		/* Share init */
		shareDialog = new ShareDialog(mContext);
		// this part is optional
		if (shareCallBack != null)
			shareDialog.registerCallback(callbackManager, shareCallBack);

	}

	private void makeUserFriendListsResquest(AccessToken accessToken) {
		GraphRequest request = GraphRequest.newMyFriendsRequest(accessToken, new GraphJSONArrayCallback() {

			@Override
			public void onCompleted(JSONArray objects, GraphResponse response) {

				if (mOnGetFriendListSuccess != null)
					mOnGetFriendListSuccess.onGetFriendList(response.getJSONObject());
				Log.d(TAG, "FriendListsArray:" + objects.toString());
				Log.d(TAG, "FriendListsObject:" + response.getJSONObject().toString());

			}
		});

		Bundle parameters = new Bundle();
		parameters.putString("fields", "id,name,link,picture");
		request.setParameters(parameters);
		request.executeAsync();
	}

	private void makeFacebookUserRequest(AccessToken accessToken) {
		GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
			@Override
			public void onCompleted(JSONObject object, GraphResponse response) {
				// Application code
				Log.d(TAG, "User object:" + response.getJSONObject().toString());

				if (mOnLoginSuccess != null)
					mOnLoginSuccess.onLoginSuccess(parseFacebookUser(response.getJSONObject()));

			}
		});
		Bundle parameters = new Bundle();
		parameters.putString("fields", "id,name,first_name,last_name,email,gender, birthday");
		request.setParameters(parameters);
		request.executeAsync();
	}

	/**
	 * TO parse info of facebook user
	 * 
	 * @param string
	 */
	private FacebookUser parseFacebookUser(JSONObject jsonObject) {
		if (jsonObject != null) {
			return new FacebookUser(jsonObject.optString("id"), jsonObject.optString("email"),
					jsonObject.optString("name"), jsonObject.optString("last_name"),
					jsonObject.optString("first_name"), jsonObject.optString("birthday"),
					jsonObject.optString("gender"), "http://graph.facebook.com/" + jsonObject.optString("id")
							+ "/picture?type=large");
		}
		return null;
	}

	// ----------------- Public Methods ------------------------ //
	private void doLogin() {
		// LoginManager.getInstance().logInWithPublishPermissions(mContext,
		// PERMISSIONS);

		// LoginManager.getInstance().logInWithReadPermissions(mContext,
		// PERMISSIONS);

		mbtnLoginButton.callOnClick();
	}

	public void doLogin(OnLoginSuccess onLoginSuccess) {
		Log.d(TAG, "doLogin");
		mOnLoginSuccess = onLoginSuccess;
		doLogin();
	}

	public void doLogout() {
		LoginManager.getInstance().logOut();
	}

	/**
	 * To post a message to wall
	 */
	public void doSharePost() {
		if (ShareDialog.canShow(ShareLinkContent.class)) {
			Builder builder = new ShareLinkContent.Builder();
			shareDialog.show(builder.build());
		} else
			doLogin();
	}

	/**
	 * To share post with link
	 * 
	 * @param url
	 * @param contentTitle
	 * @param contentDescription
	 */
	public void doShareLink(String url, String contentTitle, String contentDescription) {
		if (ShareDialog.canShow(ShareLinkContent.class)) {
			Builder builder = new ShareLinkContent.Builder();
			if (!TextUtils.isEmpty(contentTitle))
				builder.setContentTitle(contentTitle);
			if (!TextUtils.isEmpty(contentDescription))
				builder.setContentDescription(contentDescription);
			if (!TextUtils.isEmpty(url))
				builder.setContentUrl(Uri.parse(url));

			shareDialog.show(builder.build());
		} else
			doLogin();
	}

	/**
	 * To share post with image
	 * 
	 * @param bm
	 */
	public void doShareImage(Bitmap bm) {
		if (ShareDialog.canShow(SharePhotoContent.class)) {
			SharePhoto sharePhoto = new SharePhoto.Builder().setBitmap(bm).build();
			ArrayList<SharePhoto> photos = new ArrayList<>();
			photos.add(sharePhoto);

			SharePhotoContent sharePhotoContent = new SharePhotoContent.Builder().setPhotos(photos).build();
			shareDialog.show(sharePhotoContent);
		} else
			doLogin();
	}

	public void setShareCallBack(FacebookCallback<Result> callback) {
		shareCallBack = callback;
	}

	public CallbackManager getCallBackManager() {
		return callbackManager;
	}

	public void setOnGetFriendListSuccess(OnGetFriendListSuccess friendListSuccess) {
		mOnGetFriendListSuccess = friendListSuccess;
	}

	public AccessToken getAccessToken() {
		if (mAccessToken != null)
			return mAccessToken;
		else
			return AccessToken.getCurrentAccessToken();
	}
}
