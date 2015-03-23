package com.fourelements.roomescape;


import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;

public class LogoSplash extends Activity {

	 // Splash screen timer
    private static int SPLASH_TIME_OUT = 3000;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(LogoSplash.this, MenuActivity.class);
                startActivity(i);
        		((BitmapDrawable) findViewById(R.id.logo).getBackground()).getBitmap().recycle();

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    	//((BitmapDrawable) findViewById(R.id.logo).getBackground()).getBitmap().recycle();
    }

	@Override
	protected void onStart() {
		super.onStart();
	}

}
