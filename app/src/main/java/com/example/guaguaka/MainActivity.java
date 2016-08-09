package com.example.guaguaka;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.guaguaka.utils.Util;
import com.example.guaguaka.view.GuaGuaKa;

public class MainActivity extends AppCompatActivity {

    GuaGuaKa mGuaGuaKa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGuaGuaKa = (GuaGuaKa) findViewById(R.id.id_guaguaka);
        mGuaGuaKa.setOnGuaGuaKaCompleteListener(new GuaGuaKa.OnGuaGuaKaCompleteListener() {
            @Override
            public void complete() {
//                Toast.makeText(getApplicationContext(), "用户已经刮得差不多了", Toast.LENGTH_SHORT).show();
                Util.showToast(MainActivity.this, "用户已经刮得差不多了");
            }
        });

//        mGuaGuaKa.setText("");

    }

}
