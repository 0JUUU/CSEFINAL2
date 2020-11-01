package com.youngju.csefinal2;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

public class PopupActivity extends Activity {

    private  Context context;
    private long btnPressTime = 0;

    public PopupActivity(Context context){
        this.context = context;
    }

    TextView textView;
    TextToSpeech tts;


    public void callFunction(){
        final Dialog dig = new Dialog(context);
        dig.setTitle("튜토리얼");
        dig.setContentView(R.layout.activity_pop_up);
        dig.show();

        LinearLayout layout = (LinearLayout)dig.findViewById(R.id.pop_up_txt);
        textView = (TextView)dig.findViewById(R.id.textView_pop_up);
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                    tts.setSpeechRate(0.9f);
                }
            }
        });

        layout.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(System.currentTimeMillis()>btnPressTime+1000){
                    btnPressTime = System.currentTimeMillis();

                    tts.speak(textView.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, null);

                    return;
                }
                if(System.currentTimeMillis()<=btnPressTime+1000){
                    tts.stop();
                    tts.shutdown();
                    dig.dismiss();
                }
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

