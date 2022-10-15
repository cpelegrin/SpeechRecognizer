package br.com.cpelegrin.speechrecognizer;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    TextView speedValue;
    TextView pitchValue;
    private TextToSpeech ttsEngine;
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0: {
                    ttsEngine.speak("Oi Carlos", TextToSpeech.QUEUE_ADD, null, "UTTERANCE_ID");
                    break;
                }
                case 1: {
                    ttsEngine.speak("Tudo bem e você?", TextToSpeech.QUEUE_ADD, null, "UTTERANCE_ID");
                    break;
                }

                case 2: {
                    ttsEngine.speak("Que Bom! Boa noite alunos!", TextToSpeech.QUEUE_ADD, null, "UTTERANCE_ID");
                    break;
                }

                case 3: {
                    ttsEngine.speak("Esta é a aula 2.", TextToSpeech.QUEUE_ADD, null, "UTTERANCE_ID");
                    break;
                }

                default: {
                    ttsEngine.speak((CharSequence) msg.obj, TextToSpeech.QUEUE_ADD, null, "UTTERANCE_ID");
                    break;
                }
            }
        }
    };
    ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    List<String> resultado =
                            result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String spokenText = resultado.get(0);

                    Log.e(TAG, spokenText);

                    spokenText = spokenText.toLowerCase();

                    if (spokenText.contains("oi") || spokenText.contains("olá")) {
                        handler.sendEmptyMessage(0);
                    } else if (spokenText.contains("estou bem")) {
                        handler.sendEmptyMessage(2);
                    } else if (spokenText.contains(" bom") || spokenText.contains(" bem")) {
                        handler.sendEmptyMessageDelayed(1, 500);
                    } else if (spokenText.contains("aula")) {
                        handler.sendEmptyMessage(3);
                    } else {
                        Message message = new Message();
                        message.what = 800;
                        message.obj = spokenText;
                        handler.sendMessage(message);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSpeechToTextActivity();
            }
        });

        ttsEngine = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    ttsEngine.setLanguage(new Locale("pt", "BR"));
                    ttsEngine.setPitch(1f);
                    ttsEngine.setSpeechRate(1f);
                } else {
                    Log.w(TAG, "Could not open TTS Engine (onInit status=" + status + ")");
                    ttsEngine = null;
                }
            }
        });

        speedValue = findViewById(R.id.speedValue);
        SeekBar speedVoice = findViewById(R.id.setspeedBar);
        speedVoice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    speedValue.setText("0.1");
                } else {
                    speedValue.setText("" + progress);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getProgress() == 0) {
                    ttsEngine.setSpeechRate(0.1f);
                    speedValue.setText("0.1");
                } else {
                    ttsEngine.setSpeechRate(seekBar.getProgress());
                    speedValue.setText("" + seekBar.getProgress());
                }
            }
        });

        pitchValue = findViewById(R.id.pitchValue);
        SeekBar pitchSeekBar = findViewById(R.id.setPitchBar);
        pitchSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    pitchValue.setText("0.1");
                } else {
                    pitchValue.setText("" + progress / 3f);

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getProgress() == 0) {
                    ttsEngine.setPitch(0.1f);
                    pitchValue.setText("0.1");
                } else {
                    ttsEngine.setPitch(seekBar.getProgress() / 3f);
                    pitchValue.setText("" + seekBar.getProgress() / 3f);
                }
            }
        });
    }

    private void startSpeechToTextActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, new Locale("pt", "BR"));

        try {
//            startActivityForResult(intent, 1);
            activityResultLauncher.launch(intent);
        } catch (ActivityNotFoundException a) {
            Log.e(TAG, "Your device does not support Speech to Text");
        }
    }
}
