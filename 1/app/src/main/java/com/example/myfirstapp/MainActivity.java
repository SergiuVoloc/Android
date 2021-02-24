package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        changeTextOnce();
    }

    public void changeTextOnce(){
        final TextView changingText = (TextView) findViewById(R.id.textView2);
        Button changeTextButton = (Button) findViewById(R.id.button);

        changeTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                changingText.setText("Hello!");
            }
        });
    }

}