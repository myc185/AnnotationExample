package com.example.annotation;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.lucky.annotation.BindView;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tvText)
    TextView mTvText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyButterknife.bind(this); //一键实现findViewById功能
        mTvText.setText("Test");
    }
}