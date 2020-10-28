package com.youngju.csefinal2;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.util.Locale;

public class PopupCheckDestActivity extends Activity {
    TextToSpeech tts;
    TextView txtText;
    final int PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_popupcheckdest);
        TTS();


    }
    public void TTS() {
        txtText = (TextView)findViewById(R.id.txtText);
        Intent intent = getIntent();
        final String data = intent.getStringExtra("data");


        tts = new TextToSpeech(getBaseContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    String s = data.concat(" 맞습니까? 맞으면 핸드폰 백버튼을 누르시고, 아니면 화면을 터치해주세요.");
                    tts.setLanguage(Locale.KOREAN);
                    tts.setSpeechRate(0.9f);

                    //http://stackoverflow.com/a/29777304
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, null);
                        // API 20
                    else
                        tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);

                    //UI 객체생성
                }
            }
        });
        txtText.setText(data);
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            if(tts.isSpeaking()) {
                tts.stop();
            }
            tts.shutdown();
        }
        super.onDestroy();
    }

    //확인 버튼 클릭
    public void mOnClose(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("result", "ㄱㄴㄱㄴ");
        setResult(RESULT_CANCELED, intent);

        //액티비티(팝업) 닫기
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            //데이터 전달하기
            Intent intent = new Intent();
            intent.putExtra("result", "ㄱㄴㄱㄴ");
            setResult(RESULT_CANCELED, intent);

            //액티비티(팝업) 닫기
            finish();
            return true;
        }
        return true;
    }



    @Override
    public void onBackPressed() {
        // 맞을 경우
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("result", txtText.getText().toString());
        setResult(RESULT_OK, intent);

        //액티비티(팝업) 닫기
        finish();

    }
}
