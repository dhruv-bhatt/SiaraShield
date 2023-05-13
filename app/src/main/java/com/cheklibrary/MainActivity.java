package com.cheklibrary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent i = new Intent(this, com.cybersiara.app.MainActivity.class);
        i.putExtra("master_key", "kNKLUO9OyUNd4y1azl7TFFH8hI0zyzYh");
        i.putExtra("auth_key", "jwqIM6PQ8YVfy9HOgQuRjW6OCyX0dAGS");
        startActivity(i);
    }
}