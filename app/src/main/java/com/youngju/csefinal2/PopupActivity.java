package com.youngju.csefinal2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class PopupActivity  {

    private  Context context;
    private long btnPressTime = 0;
    public PopupActivity(Context context){
        this.context = context;
    }

    public void callFunction(){
        final Dialog dig = new Dialog(context);
        dig.setTitle("튜토리얼");
        dig.setContentView(R.layout.pop_up);
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

