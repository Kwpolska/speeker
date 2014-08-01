/**
 * 
 */
package com.chriswarrick.speeker;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.text.method.LinkMovementMethod;
import android.net.Uri;

public class AboutActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		((TextView) findViewById(R.id.textCopyright))
				.setMovementMethod(LinkMovementMethod.getInstance());
		((TextView) findViewById(R.id.textCopyright)).setText(Html
				.fromHtml(getResources().getString(R.string.copyright)));
		Context context = getApplicationContext();
		String versionName = "v?";
		try {
			versionName = "v"
					+ context.getPackageManager().getPackageInfo(
							context.getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
		}
		((TextView) findViewById(R.id.textVersion)).setText(versionName);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.about, menu);
		return true;
	}

	public void notImplemented() {
		Context context = getApplicationContext();
		CharSequence text = "Not implemented.";
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_web_site) {
			String website = "http://chriswarrick.com/";
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(website));
			startActivity(i);
		} else if (id == R.id.action_licenses) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Licenses");

			WebView wv = new WebView(this);
			wv.loadUrl("file:///android_asset/licenses.html");
			wv.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					view.loadUrl(url);

					return true;
				}
			});
			wv.setBackgroundColor(0x00000000);
			wv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			alert.setView(wv);
			alert.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			alert.show();
		} else {
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
}
