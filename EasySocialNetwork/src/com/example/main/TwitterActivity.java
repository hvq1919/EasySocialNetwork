package com.example.main;

import java.util.ArrayList;

import tools.SavedStore;
import twitter.TwitterHelper;
import twitter.TwitterHelper.OnGetFriendsListsSuccess;
import twitter.TwitterHelper.OnPostTwitterCallBack;
import twitter.TwitterHelper.OnSuccessLogin;
import twitter4j.User;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.easysocialnetwork.R;
import com.nostra13.universalimageloader.core.ImageLoader;

public class TwitterActivity extends Activity implements OnClickListener {

	private LinearLayout llContent, llFriendLists;
	private ImageView avatar;
	private TextView textContent;
	private EditText edtMessage;

	private TwitterHelper mTwitterHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_twitter);
		initObjects();

		mTwitterHelper = new TwitterHelper(this);

	
		
		updateUi();
	}

	/**
	 * You must override this method to use Twitter Helper
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mTwitterHelper.onActivitResult(requestCode, resultCode, data);
	}

	private void initObjects() {
		llContent = (LinearLayout) findViewById(R.id.llContent);
		llFriendLists = (LinearLayout) findViewById(R.id.llFriendLists);
		avatar = (ImageView) findViewById(R.id.avatar);
		textContent = (TextView) findViewById(R.id.textContent);
		edtMessage = (EditText) findViewById(R.id.edtMessage);

		/* register button click listeners */
		findViewById(R.id.btn_login).setOnClickListener(this);
		findViewById(R.id.btn_Share).setOnClickListener(this);
		findViewById(R.id.btn_get_friends).setOnClickListener(this);
		findViewById(R.id.btn_logout).setOnClickListener(this);
	}

	
	int current_page = 0;
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_login:
			mTwitterHelper.doLogin(new OnSuccessLogin() {
				@Override
				public void onSuccessLogin() {
					updateUi();
				}
			});
			break;
		case R.id.btn_Share:
			Bitmap icon = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_launcher);
			
			mTwitterHelper.doSharePost(edtMessage.getText().toString(),new OnPostTwitterCallBack() {
				
				@Override
				public void onCallBack(String message) {
					Toast.makeText(TwitterActivity.this, message, Toast.LENGTH_SHORT).show();
				}
			});
			
			
//			mTwitterHelper.doSharePostWithImage(edtMessage.getText().toString(), icon, new OnPostTwitterCallBack() {
//				@Override
//				public void onCallBack(String message) {
//					Toast.makeText(TwitterActivity.this, message, Toast.LENGTH_SHORT).show();
//				}
//			});
			break;
		case R.id.btn_get_friends:
			mTwitterHelper.doGetFriendLists(new OnGetFriendsListsSuccess() {
				@Override
				public void onGetFriends(ArrayList<User> users) {
					llContent.setVisibility(View.GONE);
					if (users == null || users.size() == 0)
						Toast.makeText(TwitterActivity.this, "Error get friend lists.", Toast.LENGTH_SHORT).show();
					else
						for (User user : users) {
							View child = getLayoutInflater().inflate(R.layout.item_twitter_profile, null);
							ImageView imgAvatar = (ImageView) child.findViewById(R.id.imgAvatar);
							TextView textId = (TextView) child.findViewById(R.id.textId);
							TextView textName = (TextView) child.findViewById(R.id.textName);
							TextView textContent = (TextView) child.findViewById(R.id.textContent);

							ImageLoader.getInstance().displayImage(user.getBiggerProfileImageURL(), imgAvatar);
							textId.setText("ID:" + user.getId() + "");
							textName.setText("Name:" + user.getName() + "");
							textContent.setText("User Object:" + user.toString() + "");

							llFriendLists.addView(child);
						}
				}
			}, current_page);
			current_page += 1;

			break;
		case R.id.btn_logout:
			mTwitterHelper.doLogout();
			updateUi();
			break;
		}
	}

	public void updateUi() {
		User user = (User) SavedStore.getTwitterUserObject();
		if (user == null)
			llContent.setVisibility(View.GONE);
		else {
			llContent.setVisibility(View.VISIBLE);
			ImageLoader.getInstance().displayImage(user.getBiggerProfileImageURL(), avatar);

			StringBuilder builder = new StringBuilder();

			builder.append("ID:" + user.getId() + "\n");
			builder.append("Name:" + user.getName() + "\n");
			builder.append("Description:" + user.getDescription() + "\n");
			builder.append("Location:" + user.getLocation() + "\n");
			builder.append("ScreenName:" + user.getScreenName() + "\n");
			builder.append("TimeZone:" + user.getTimeZone() + "\n");
			builder.append("URL:" + user.getURL() + "\n");

			builder.append("User content:" + user.toString() + "\n");
			textContent.setText(builder);
		}

	}

}
