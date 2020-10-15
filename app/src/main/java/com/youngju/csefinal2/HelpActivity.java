package com.youngju.csefinal2;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HelpActivity extends Fragment {
    private String title;
    private int page;

    private long btnPressTime = 0;
    RelativeLayout relativehelp;
    RelativeLayout relativetutorial;

    public static HelpActivity newInstance() {
        HelpActivity helpfr = new HelpActivity();
        return helpfr;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_help, container, false);
        relativehelp = (RelativeLayout) view.findViewById(R.id.RelativeLayout_info_func_simple);
        relativetutorial = (RelativeLayout) view.findViewById(R.id.RelativeLayout_tutorial_simple);

        relativetutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(System.currentTimeMillis()>btnPressTime+1000){
                    btnPressTime = System.currentTimeMillis();
                    return;
                }
                if(System.currentTimeMillis()<=btnPressTime+1000){
                    Intent it = new Intent(getActivity(),TutorialActivity.class);
                    startActivity(it);
                }
            }
        });

        relativehelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(System.currentTimeMillis()>btnPressTime+1000){
                    btnPressTime = System.currentTimeMillis();
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


}