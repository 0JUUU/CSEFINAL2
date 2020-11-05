package com.youngju.csefinal2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Locale;

public class FuncActivity extends Activity {
    RelativeLayout infomap;
    RelativeLayout infocam;

    private long btnPressTime = 0;
    TextView textView;
    TextView textView1;
    Context mContext;

    TextToSpeech tts;
    String text;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_func);

        infomap = (RelativeLayout)findViewById(R.id.RelativeLayout_info_map_simple);
        infocam = (RelativeLayout)findViewById(R.id.RelativeLayout_info_camera_simple);

        textView = (TextView)findViewById(R.id.textview_info_map);
        textView1 = (TextView)findViewById(R.id.textview_info_camera);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                    tts.setSpeechRate(0.9f);
                }
            }
        });

        infomap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(System.currentTimeMillis()>btnPressTime+1000){
                    btnPressTime = System.currentTimeMillis();
                    String text1 = textView.getText().toString();
                    Toast.makeText(getApplication(), text1, Toast.LENGTH_SHORT).show();

                    //http://stackoverflow.com/a/29777304
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ttsGreater21(text1);
                    } else {
                        ttsUnder20(text1);
                    }
                    return;
                }
                if(System.currentTimeMillis()<=btnPressTime+1000){
                    Intent it = new Intent(getApplicationContext(),MapInfoActivity.class);
                    startActivity(it);
                }
            }
        });

        infocam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(System.currentTimeMillis()>btnPressTime+1000){
                    btnPressTime = System.currentTimeMillis();
                    String text1 = textView1.getText().toString();
                    Toast.makeText(getApplication(), text1, Toast.LENGTH_SHORT).show();

                    //http://stackoverflow.com/a/29777304
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ttsGreater21(text1);
                    } else {
                        ttsUnder20(text1);
                    }
                    return;
                }
                if(System.currentTimeMillis()<=btnPressTime+1000){
                    Intent it = new Intent(getApplicationContext(),CameraInfoActivity.class);
                    startActivity(it);
                }
            }
        });
    }
    // TTS 관련
    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId=this.hashCode() + "";
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }
}

