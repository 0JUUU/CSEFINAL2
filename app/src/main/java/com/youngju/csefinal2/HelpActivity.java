package com.youngju.csefinal2;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.Locale;

public class HelpActivity extends Fragment {

    TextView textView;
    TextView textView1;
    Context mContext;

    TextToSpeech tts;
    String text;

    private long btnPressTime = 0;
    RelativeLayout relativehelp;
    RelativeLayout relativetutorial;

    public static HelpActivity newInstance(){
        HelpActivity helpfr=new HelpActivity();
        return helpfr;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_help, container, false);
        relativehelp = (RelativeLayout) view.findViewById(R.id.RelativeLayout_info_func_simple);
        relativetutorial = (RelativeLayout) view.findViewById(R.id.RelativeLayout_tutorial_simple);
        textView = (TextView)view.findViewById(R.id.textView_tutorial_simple);
        tts = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        relativetutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(System.currentTimeMillis()>btnPressTime+1000){
                    btnPressTime = System.currentTimeMillis();
                    String text1 = textView.getText().toString();
                    Toast.makeText(getContext(), text1, Toast.LENGTH_SHORT).show();

                    //http://stackoverflow.com/a/29777304
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ttsGreater21(text1);
                    } else {
                        ttsUnder20(text1);
                    }
                    return;
                }
                if(System.currentTimeMillis()<=btnPressTime+1000){
                    Intent it = new Intent(getActivity(),TutorialActivity.class);
                    startActivity(it);
                }
            }
        });

        textView1 = (TextView)view.findViewById(R.id.textView_info_func_simple);
        relativehelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(System.currentTimeMillis()>btnPressTime+1000){
                    btnPressTime = System.currentTimeMillis();
                    String text1 = textView1.getText().toString();
                    Toast.makeText(getContext(), text1, Toast.LENGTH_SHORT).show();

                    //http://stackoverflow.com/a/29777304
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ttsGreater21(text1);
                    } else {
                        ttsUnder20(text1);
                    }
                    return;
                }
                if(System.currentTimeMillis()<=btnPressTime+1000){
                    Intent it = new Intent(getActivity(),FuncActivity.class);

                    startActivity(it);
                }
            }
        });

        return view;
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
