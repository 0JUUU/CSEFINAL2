package com.youngju.csefinal2;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.Locale;

public class CameraInfoActivity extends Activity {
    TextView textView;
    TextToSpeech tts;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_camera);

        textView = (TextView)findViewById(R.id.textView_info_camera_content);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                tts.setSpeechRate(0.9f);
                tts.speak(textView.getText().toString(),TextToSpeech.QUEUE_FLUSH, null,null);
            }
        });
    }
    protected void onDestroy() {
        super.onDestroy();
        // TTS 객체가 남아있다면 실행을 중지하고 메모리에서 제거한다.
        if(tts != null){
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }
}
