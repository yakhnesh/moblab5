package com.uoit.noteme.activites;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;

import com.uoit.noteme.R;
import com.uoit.noteme.activites.views.CustomViews;

public class MyCanvas extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_canvas);

        CustomViews customViews = new CustomViews(this);

    }
}