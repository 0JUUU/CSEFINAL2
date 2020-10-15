package com.youngju.csefinal2;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HelpActivity extends Fragment {
    private String title;
    private int page;
    RelativeLayout relativehelp;
    RelativeLayout relativetutorial;

    public static HelpActivity newInstance(){
        HelpActivity helpfr=new HelpActivity();
        return helpfr;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_help,container,false);
        relativehelp = (RelativeLayout)view.findViewById(R.id.Relative_help);
        relativetutorial = (RelativeLayout)view.findViewById(R.id.RelativeLayout_tutorial_simple);

       relativetutorial.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),TutorialActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}
