package com.example.test;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import java.util.concurrent.TimeUnit;

public class SplashScreen extends AppCompatActivity {
    ImageView img;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        img=findViewById(R.id.loc_img);

        Animation animation = new AlphaAnimation(1,0);
        animation.setDuration(1000);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        img.setAnimation(animation);
        if(isNetworkConnected()) {
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(SplashScreen.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
                CountDownTimer countDownTimer = new CountDownTimer(5000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        img.clearAnimation();
                        Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                };
                countDownTimer.start();

            }
            else {
                CountDownTimer countDownTimer = new CountDownTimer(3000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        img.clearAnimation();
                        Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                };
                countDownTimer.start();
            }
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreen.this);
            builder.setMessage("No Internet Connection! \nPlease turn on your Internet")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }
    public boolean isNetworkConnected()
    {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo()!=null && cm.getActiveNetworkInfo().isConnected();
    }
}
