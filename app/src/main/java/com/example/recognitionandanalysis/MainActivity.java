package com.example.recognitionandanalysis;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.jetbrains.annotations.Nullable;

import kotlin.jvm.internal.Intrinsics;

public class MainActivity extends AppCompatActivity {

    private Button savedImgButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        Button setImgButton = findViewById(R.id.savedImg_btn);


        setImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), setImage.class);
                startActivity(intent);
            }
        });

    }
}
