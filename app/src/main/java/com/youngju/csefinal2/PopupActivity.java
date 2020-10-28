package com.youngju.csefinal2;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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

        layout.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(System.currentTimeMillis()>btnPressTime+1000){
                    btnPressTime = System.currentTimeMillis();

                    return;
                }
                if(System.currentTimeMillis()<=btnPressTime+1000){
                   dig.dismiss();
                }
            }
        });
    }

}

