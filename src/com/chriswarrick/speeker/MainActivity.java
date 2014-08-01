package com.chriswarrick.speeker;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.EditText;
import android.content.Context;
import android.widget.Toast;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import java.util.HashMap;
import android.view.Menu;
import android.view.MenuItem;
import android.speech.tts.UtteranceProgressListener;

@SuppressLint("NewApi")
public class MainActivity extends Activity {
	EditText TextTS;
	TextToSpeech Engine;
	boolean speak_button = true;
	String SPEEKER_UID = "SpeekerTTS";

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		class InitListener implements TextToSpeech.OnInitListener {
			@Override
			public void onInit(int status) {
				ttsStatus(status);

				if (Build.VERSION.SDK_INT >= 15) {
					Engine.setOnUtteranceProgressListener(new UtteranceProgressListener() {
						@Override
						public void onDone(String utteranceId) {
							MainActivity.this.speak_button = true;
							MainActivity.this.invalidateOptionsMenu();
						}

						@Override
						public void onError(String utteranceId) {
							MainActivity.this.speak_button = true;
							MainActivity.this.invalidateOptionsMenu();

							Context context = getApplicationContext();
							int duration = Toast.LENGTH_SHORT;

							Toast toast = Toast.makeText(context,
									"An error occured.", duration);
							toast.show();
						}

						@Override
						public void onStart(String utteranceId) {
							MainActivity.this.speak_button = false;
							MainActivity.this.invalidateOptionsMenu();
						}
					});
				}
			}
		}
		TextToSpeech.OnInitListener IL = new InitListener();

		Context context = getApplicationContext();
		Engine = new TextToSpeech(context, IL);

		setContentView(R.layout.activity_main);
		TextTS = (EditText) findViewById(R.id.TextTS);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		if (Build.VERSION.SDK_INT >= 15) {
			// button switcheroo (and thus, stopping) supported only on modern
			// android
			MenuItem speakItem = menu.findItem(R.id.action_speak);
			MenuItem stopItem = menu.findItem(R.id.action_stop);
			speakItem.setVisible(false);
			stopItem.setVisible(false);
			if (speak_button) {
				speakItem.setVisible(true);
			} else {
				stopItem.setVisible(true);
			}
		}
		return true;
	}

	public void ttsStatus(int status) {
		if (status == TextToSpeech.ERROR) {
			// 1. Instantiate an AlertDialog.Builder with its constructor
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			// 2. Chain together various setter methods to set the dialog
			// characteristics
			builder.setMessage(R.string.notts_message)
					.setTitle(R.string.notts_title)
					.setPositiveButton(R.string.close,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.dismiss();
									MainActivity.this.finish();
								}
							});

			// 3. Get the AlertDialog from create()
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}

	public void notImplemented() {
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, "Not implemented.", duration);
		toast.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			notImplemented();
		} else if (id == R.id.action_speak) {
			String t = TextTS.getText().toString();
			int length = 4000; // in API 18, backporting for backwards
								// compatibility
			if (t.length() > length) {
				Context context = getApplicationContext();
				int duration = Toast.LENGTH_SHORT;

				Toast toast = Toast.makeText(context, R.string.too_long,
						duration);
				toast.show();
			} else {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
						MainActivity.this.getPackageName());
				Engine.speak(t, TextToSpeech.QUEUE_ADD, map);
				MainActivity.this.speak_button = false;
				MainActivity.this.invalidateOptionsMenu();
			}
		} else if (id == R.id.action_stop) {
			Engine.stop();
		} else if (id == R.id.action_about) {
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
		} else if (id == R.id.action_clear) {
			TextTS.setText("");
		} else {
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
}
