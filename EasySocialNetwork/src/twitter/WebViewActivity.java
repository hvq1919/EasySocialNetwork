package twitter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends Activity {
	private WebView webView;
	public static String EXTRA_URL = "extra_url";

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		final String url = this.getIntent().getStringExtra(EXTRA_URL);
		if (null == url) {
			Log.d("TAG", "URL cannot be null");
			finish();
		}

		webView = new WebView(this);

		webView.getSettings().setJavaScriptEnabled(true); // enable javascript
		webView.setWebViewClient(new MyWebViewClient());
		webView.loadUrl(url);

		setContentView(webView);

	}

	class MyWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {

			if (url.contains(Define.TWITTER_CALLBACK_URL)) {
				Uri uri = Uri.parse(url);

				/* Sending results back */
				String verifier = uri.getQueryParameter(Define.URL_TWITTER_OAUTH_VERIFIER);
				Intent resultIntent = new Intent();
				resultIntent.putExtra(Define.URL_TWITTER_OAUTH_VERIFIER, verifier);
				setResult(RESULT_OK, resultIntent);

				/* closing webview */
				finish();
				return true;
			}
			return false;
		}
	}

}
