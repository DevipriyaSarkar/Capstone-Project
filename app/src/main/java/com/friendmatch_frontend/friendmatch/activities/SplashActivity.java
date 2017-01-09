package com.friendmatch_frontend.friendmatch.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.friendmatch_frontend.friendmatch.R;

import static com.friendmatch_frontend.friendmatch.application.AppController.LOCAL_IP_ADDRESS;
import static com.friendmatch_frontend.friendmatch.application.AppController.MY_SERVER;
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
                    e.putString("SERVER_URL", MY_SERVER);
                    e.apply();
                    SERVER_URL = MY_SERVER;
                    finish();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
            });

            Button enableLocalHostButton = (Button) findViewById(R.id.enableLocalHostButton);
            enableLocalHostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startButton.setVisibility(View.GONE);
                    LinearLayout localHostLayout = (LinearLayout) findViewById(R.id.localHostLayout);
                    localHostLayout.setVisibility(View.VISIBLE);

                    final EditText localHostAddress = (EditText) findViewById(R.id.localHostAddress);

                    Button localHostStartButton = (Button) findViewById(R.id.localHostStartButton);
                    localHostStartButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String address = localHostAddress.getText().toString();
                            if (isEmpty(address)) {
                                Toast.makeText(getApplicationContext(), R.string.local_host_address_empty_error, Toast.LENGTH_SHORT).show();
                            } else if (isInvalid(address)) {
                                Toast.makeText(getApplicationContext(), R.string.local_host_address_incorrect_error, Toast.LENGTH_SHORT).show();
                            } else {
                                LOCAL_IP_ADDRESS = "http://" + address;
                                SharedPreferences.Editor e = sp.edit();
                                e.putString("SERVER_URL", LOCAL_IP_ADDRESS);
                                e.apply();
                                SERVER_URL = LOCAL_IP_ADDRESS;
                                finish();
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                            }
                        }
                    });
                }
            });

        } else {
            SERVER_URL = sp.getString("SERVER_URL", MY_SERVER);
            finish();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }
    }

    private boolean isEmpty(String string) {
        return string == null || string.equals("") || string.equals(" ");
    }

    private boolean isInvalid(String string) {
        return !string.matches(".+[.].+[.].+[.].+");
    }
}
