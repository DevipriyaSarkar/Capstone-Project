package com.friendmatch_frontend.friendmatch.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.friendmatch_frontend.friendmatch.R;

import static com.friendmatch_frontend.friendmatch.application.AppController.SERVER_URL;

public class SplashActivity extends AppCompatActivity {


    // private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check if it's first launch
        final SharedPreferences sp = getSharedPreferences("FIRST_LAUNCH", Context.MODE_PRIVATE);
        final int first_launch = sp.getInt("first_launch", 1);

        if (first_launch == 1) {
            setContentView(R.layout.activity_splash);

            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("first_launch", 0);
            editor.commit();

            final Button startButton = (Button) findViewById(R.id.startButton);
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor e = sp.edit();
                    e.putString("SERVER_URL", SERVER_URL);
                    e.apply();
                    finish();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}
