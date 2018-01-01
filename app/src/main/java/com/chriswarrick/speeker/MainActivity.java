/*
 * Copyright © 2016-2018, Chris Warrick.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions, and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions, and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the author of this software nor the names of
 *    contributors to this software may be used to endorse or promote
 *    products derived from this software without specific prior written
 *    consent.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.chriswarrick.speeker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.UtteranceProgressListener;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.speech.tts.TextToSpeech;
import android.app.AlertDialog;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public FloatingActionButton fab;
    public TextToSpeech tts;
    public CoordinatorLayout coordinatorLayout;
    public EditText editText;
    public boolean isSpeaking = false;
    public TextView languageLabel;
    public Spinner languageSpinner;
    public HashMap<String, Locale> locales;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        locales = new HashMap<>();
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.ERROR) {
                    // 1. Instantiate an AlertDialog.Builder with its constructor
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                    // 2. Chain together various setter methods to set the dialog characteristics
                    builder.setMessage(R.string.notts_message)
                            .setTitle(R.string.notts_title);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });

                    // 3. Get the AlertDialog from create()
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    Log.i("TTS", "Successfully initialized TTS");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        // Add languages to spinner.

                        // Get default locale (to be selected)
                        String defaultLocale = tts.getDefaultVoice().getLocale().getDisplayName();

                        // Create and populate language list
                        java.util.ArrayList<String> localeList = new java.util.ArrayList<>();
                        java.util.Set<Locale> availableLanguages = tts.getAvailableLanguages();
                        for (Locale l: availableLanguages) {
                            int available = tts.isLanguageAvailable(l);
                            if (available != TextToSpeech.LANG_COUNTRY_AVAILABLE && available != TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE) {
                                continue;
                            }
                            String lDisplayName = l.getDisplayName();
                            localeList.add(lDisplayName);
                            locales.put(lDisplayName, l);
                        }

                        // Sort list and set adapter
                        java.util.Collections.sort(localeList);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                MainActivity.this,
                                android.R.layout.simple_spinner_dropdown_item,
                                localeList);
                        languageSpinner.setAdapter(adapter);

                        // Set selection to default language
                        languageSpinner.setSelection(adapter.getPosition(defaultLocale));
                    }
                }
            }
        });

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String uid) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fab.setImageResource(R.drawable.ic_stop);
                        isSpeaking = true;
                        Log.i("TTS", "Started speaking");
                    }
                });
            }

            @Override
            public void onDone(String uid) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fab.setImageResource(R.drawable.ic_mic);
                        isSpeaking = false;
                        Log.i("TTS", "Stopped speaking");
                    }
                });
            }

            @Override
            public void onError(String uid, int errorCode) {
                int errorRID = R.string.tts_ERROR_UNKNOWN;
                switch (errorCode) {
                    case TextToSpeech.ERROR_INVALID_REQUEST:
                        errorRID = R.string.tts_ERROR_INVALID_REQUEST;
                        break;
                    case TextToSpeech.ERROR_NETWORK:
                        errorRID = R.string.tts_ERROR_NETWORK;
                        break;
                    case TextToSpeech.ERROR_NETWORK_TIMEOUT:
                        errorRID = R.string.tts_ERROR_NETWORK_TIMEOUT;
                        break;
                    case TextToSpeech.ERROR_NOT_INSTALLED_YET:
                        errorRID = R.string.tts_ERROR_NOT_INSTALLED_YET;
                        break;
                    case TextToSpeech.ERROR_OUTPUT:
                        errorRID = R.string.tts_ERROR_OUTPUT;
                        break;
                    case TextToSpeech.ERROR_SERVICE:
                        errorRID = R.string.tts_ERROR_SERVICE;
                        break;
                    case TextToSpeech.ERROR_SYNTHESIS:
                        errorRID = R.string.tts_ERROR_SYNTHESIS;
                        break;
                }

                Snackbar.make(coordinatorLayout, getString(R.string.tts_formattable_error, getString(errorRID)), Snackbar.LENGTH_LONG).show();
                Log.e("TTS", uid);
                onDone(uid);
            }

            @Override
            public void onError(String uid) {
                Snackbar.make(coordinatorLayout, R.string.tts_generic_error, Snackbar.LENGTH_LONG).show();
                Log.e("TTS", uid);
                onDone(uid);
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        editText = (EditText) findViewById(R.id.editText);
        languageLabel = (TextView) findViewById(R.id.languageLabel);
        languageSpinner = (Spinner) findViewById(R.id.languageSpinner);

        assert fab != null;
        assert coordinatorLayout != null;
        assert editText != null;
        assert languageLabel != null;
        assert languageSpinner != null;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // hide language options (no reasonable support)
            languageLabel.setEnabled(false);
            languageLabel.setText(R.string.language_label_unsupported);
            languageSpinner.setEnabled(false);
            languageSpinner.setVisibility(View.GONE);
        }

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String lname = (String) parent.getItemAtPosition(pos);
                Locale l = locales.get(lname);
                tts.setLanguage(l);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSpeaking) {
                    tts.stop();
                    isSpeaking = false;
                } else {
                    String text = editText.getText().toString().trim();
                    if (text.equals("")) {
                        Log.w("TTS", "Input is empty");
                        return;
                    }
                    if (text.length() > TextToSpeech.getMaxSpeechInputLength()) {
                        Log.e("TTS", "Input too long");
                        Snackbar.make(coordinatorLayout, R.string.tts_too_long_error, Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        // Use API 21+ speak method
                        tts.speak(
                                text,
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                UUID.randomUUID().toString()
                        );
                    } else {
                        // Use old speak method
                        HashMap<String, String> parmap = new HashMap<>();
                        parmap.put(
                                TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
                                UUID.randomUUID().toString());
                        tts.speak(
                                text,
                                TextToSpeech.QUEUE_FLUSH,
                                parmap);
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent i;
        switch (item.getItemId()) {
            case R.id.action_about:
                i = new Intent(this, AboutActivity.class);
                this.startActivity(i);
                return true;
            case R.id.action_tts_settings:
                // Intent name is hardcoded
                i = new Intent("com.android.settings.TTS_SETTINGS");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        // Stop speaking and shut down TTS when the activity is destroyed.
        if(tts != null) {
            tts.stop();
            tts.shutdown();
            Log.i("TTS", "Shutting down.");
        }
        super.onDestroy();
    }
}
