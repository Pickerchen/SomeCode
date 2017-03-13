package com.sen5.ocup.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import com.sen5.ocup.R;
import com.sen5.ocup.yili.LoginActivity;

public class ChooseActivity extends AppCompatActivity {
    private LinearLayout ll_master,ll_gueste;
    private static String TAG = ChooseActivity.class.getSimpleName();
    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.ll_isMaster:
                    Intent intent = new Intent(ChooseActivity.this,BlueTooth3Activity.class);
                    startActivity(intent);
                break;
                case R.id.ll_gueste:
                    Intent intent2 = new Intent(ChooseActivity.this,MainActivity.class);
                        startActivity(intent2);
                    if (LoginActivity.instance != null){
                        LoginActivity.instance.finish();
                    }
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_choose);
        Transition fade = TransitionInflater.from(this).inflateTransition(R.transition.fade);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getWindow().setExitTransition(fade);
            getWindow().setEnterTransition(fade);
        }
        initView();
    }

    private void initView() {
        ll_master = (LinearLayout) findViewById(R.id.ll_isMaster);
        ll_gueste = (LinearLayout) findViewById(R.id.ll_gueste);
        ll_gueste.setOnClickListener(listener);
        ll_master.setOnClickListener(listener);
    }

}
