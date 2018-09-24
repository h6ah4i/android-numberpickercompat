package com.h6ah4i.android.example.numberpickercompat;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.h6ah4i.android.widget.numberpickercompat.NumberPicker;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NumberPicker numberPicker = (NumberPicker) findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(100);
        numberPicker.setMinValue(0);
    }
}
