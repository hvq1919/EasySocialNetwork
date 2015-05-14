package com.example.main;

import org.json.JSONObject;

import tools.SavedStore;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.easysocialnetwork.R;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.Sharer.Result;
import com.nostra13.universalimageloader.core.ImageLoader;

import facebook.FacebookHelper;
import facebook.FacebookHelper.OnGetFriendListSuccess;
import facebook.FacebookHelper.OnLoginSuccess;
import facebook.FacebookUser;

public class FacebookActivity extends Activity {

	private ImageView imgAvatar;
	private TextView tvInfo;
	private TextView tvFriendslist;

	private FacebookHelper mFacebookHelper;

	private FacebookCallback<Result> callback = new FacebookCallback<Sharer.Result>() {

		@Override
		public void onSuccess(Result result) {
			Log.d("TAG", "Share onSuccess");
			showToast("Share onSuccess");
		}

		@Override
		public void onError(FacebookException error) {
			Log.d("TAG", "Share onError");
			showToast("Share onError");
		}

		@Override
		public void onCancel() {
			Log.d("TAG", "Share onCancel");
			showToast("Share onCancel");
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_facebook);

		imgAvatar = (ImageView) findViewById(R.id.imgAvatar);
		tvInfo = (TextView) findViewById(R.id.textInfo);
		tvFriendslist = (TextView) findViewById(R.id.textListFriend);

		/* Share post to facebook without handling callback */
		// mFacebookHelper = new FacebookHelper(this);

		/*
		 * if you want to handle call back after sharing on facebook, please use
		 * constructor below
		 */
		mFacebookHelper = new FacebookHelper(this, callback);

		/* If you want to get friend list */
		mFacebookHelper.setOnGetFriendListSuccess(new OnGetFriendListSuccess() {
			@Override
			public void onGetFriendList(JSONObject jsonObject) {
				if (jsonObject != null)
					tvFriendslist.setText(jsonObject.toString());
			}
		});

		findViewById(R.id.btnLogin).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mFacebookHelper.doLogin(new OnLoginSuccess() {
					@Override
					public void onLoginSuccess(FacebookUser facebookUser) {

						// TODO check and show message if you want to

						/* Save object into SharedPreferences storage */
						if (facebookUser != null)
							SavedStore.saveFbUserObject(facebookUser);
						updateUi();
					}

				});
			}
		});

		findViewById(R.id.btnShareLink).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mFacebookHelper.doShareLink("https://www.google.com.vn", "Title Test", "Descriptions test");
			}
		});
		findViewById(R.id.btnShareImage).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Bitmap image = BitmapFactory.decodeResource(FacebookActivity.this.getResources(),
						R.drawable.ic_launcher);
				mFacebookHelper.doShareImage(image);
			}
		});
	}

	/**
	 * Update ui
	 * 
	 * @param fbUserObject
	 */
	private void updateUi() {
		FacebookUser fbUserObject = (FacebookUser) SavedStore.getFbUserObject();
		if (fbUserObject != null) {
			// TODO update ui

			ImageLoader.getInstance().displayImage(fbUserObject.getAvatarURL(), imgAvatar);

			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(" id:" + fbUserObject.getId() + " \n");
			stringBuilder.append(" Email:" + fbUserObject.getEmail() + " \n");
			stringBuilder.append(" Name:" + fbUserObject.getName() + " \n");
			stringBuilder.append(" Birthday:" + fbUserObject.getBirthday() + " \n");
			stringBuilder.append(" Gender:" + fbUserObject.getGender() + " \n");
			tvInfo.setText(stringBuilder);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mFacebookHelper.getCallBackManager().onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mFacebookHelper.doLogout();
	}

	private void showToast(String mess) {
		Toast.makeText(FacebookActivity.this, mess, Toast.LENGTH_SHORT).show();
	}
}
