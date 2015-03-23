package com.fourelements.roomescape;

import java.io.IOException;



import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ChartboostDelegate;
import com.millennialmedia.android.MMAdView;
import com.millennialmedia.android.MMRequest;
import com.millennialmedia.android.MMSDK;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class EarthRoomBeginActivity extends Activity implements AudioManager.OnAudioFocusChangeListener{
	private static final String TEST_DEVICE_ID = "4608458DD1D2D3F99AC92EA614FC0D8D";
    String appId = "52b0329b2d42da5580551e71";
    String appSignature = "202885a0818e2c1bff1552af968352b245e2a48e";
	AudioManager am;
	MediaPlayer backgroundAudio;
	int  result;
	    private Chartboost cb;
	    private static final String TAG = "Chartboost";
		private MMAdView adViewFromXml;
		public static final String PREFS_NAME = "Settings";
		public static final String ACTIVITY = "activity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);

		SharedPreferences.Editor editor = preferences.edit();
	    editor.putString(ACTIVITY, "EarthRoomBegin");
	    editor.commit();
		setContentView(R.layout.activity_earth_room_begin);
	}
	   
		private void setMMediaAd() {
			adViewFromXml = (MMAdView) findViewById(R.id.adView);
			MMRequest request = new MMRequest();
			adViewFromXml.setMMRequest(request);
			adViewFromXml.setListener(new AdListener());
			adViewFromXml.getAd();
		}
		static final String SOUND = "sound";
		static final String ADS = "ads";
		boolean mSound, mAds; 
	@Override
	protected void onStart() {
		mSound = (Boolean)IntentHelper.getObjectForKey(SOUND);
		mAds = (Boolean)IntentHelper.getObjectForKey(ADS);
		boolean sound = mSound;
		boolean ads = mAds;
		if(mAds){
		setMMediaAd();  
		if(this.cb==null){
		this.cb = Chartboost.sharedChartboost();
	    this.cb.onCreate(this, appId, appSignature, this.chartBoostDelegate);}
		
		this.cb.onStart(this);
	    this.cb.startSession();
	    this.cb.showInterstitial(); }
		System.gc();
		if(mSound){
		am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		result = am.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		if(result== AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
			initMediaPlayer();
			backgroundAudio.start();
			}}
	super.onStart();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_earth_room_begin, menu);
		return true;
	}
	int spin = 1;
public void spin(View view){
	if(spin == 1){
	findViewById(R.id.dark1).setBackgroundResource(R.drawable.dark2);
	spin++;}
	else if(spin == 2){
		findViewById(R.id.dark1).setBackgroundResource(R.drawable.dark3);
		spin++;
	}
	else if(spin == 3){
		view.setClickable(false);
		IntentHelper.addObjectForKey(mSound, SOUND);
		IntentHelper.addObjectForKey(mAds, ADS);
		adViewFromXml = null;
		if(am!=null){
		 am.abandonAudioFocus(this);
		 am = null;}
		findViewById(R.id.dark1).setBackgroundResource(R.drawable.dark1);
		Intent intent = new Intent(this, EarthRoomActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		unbindDrawables(findViewById(R.id.beginning));
		startActivity(intent);
		finish();
		System.gc();
	}
}
static void unbindDrawables(View view) {
	try{
	    if (view.getBackground() != null) {
	    	if(view.getBackground() instanceof BitmapDrawable){
		        ((BitmapDrawable)view.getBackground()).getBitmap().recycle();}
	    }
	    if (view instanceof ViewGroup) {
	        for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
	        unbindDrawables(((ViewGroup) view).getChildAt(i));
	        }
	    ((ViewGroup) view).removeAllViews();
	    }
	    else if(view instanceof View){
	    	    view.getBackground().setCallback(null);
	    	    view = null;
	    }

	}catch (Exception e) {
	e.printStackTrace();
	}
	}

@Override
protected void onDestroy() {
	super.onDestroy();
	System.gc();
	if(cb!=null){
	this.cb.onDestroy(this);
	this.cb = null;
	cb = null;
	}
}
void destroyWebView(ViewGroup viewGroup) {
    for (int i = 0; i < viewGroup.getChildCount(); i++) {
        if (viewGroup.getChildAt(i) instanceof WebView) {
            WebView view = (WebView)viewGroup.getChildAt(i);
            viewGroup.removeView(view);
            view.destroy();                
            return ;
        }
    }
}
@Override
protected void onPause() {
	super.onPause();
	if(mSound){
	backgroundAudio.stop();
    backgroundAudio.release();
    backgroundAudio = null;}
    System.gc();
}

@Override
protected void onStop() {
	// TODO Auto-generated method stub
	super.onStop();
	if(this.cb!=null){
	this.cb.onStop(this);}
}    
private void initMediaPlayer(){
	backgroundAudio = MediaPlayer.create(EarthRoomBeginActivity.this, R.raw.intro);
	backgroundAudio.setLooping(true);
	backgroundAudio.setVolume(0.1f, 0.1f);
}
public static AssetFileDescriptor getFileDescriptor(Context ctx, String path) {
	AssetFileDescriptor descriptor = null;
	try {
		ZipResourceFile zip = APKExpansionSupport.getAPKExpansionZipFile(ctx, 7, -1);
		descriptor = zip.getAssetFileDescriptor(path);
	} catch (IOException e) {
		e.printStackTrace();
	}
	return descriptor;
}
@Override
public void onAudioFocusChange(int focusChange) {
    switch (focusChange) {
    case AudioManager.AUDIOFOCUS_GAIN:
        // resume playback
        if (backgroundAudio == null) initMediaPlayer();
        else if (!backgroundAudio.isPlaying())backgroundAudio.start();
        break;

    case AudioManager.AUDIOFOCUS_LOSS:
        // Lost focus for an unbounded amount of time: stop playback and release media player
    	if(backgroundAudio!=null){
        if (backgroundAudio.isPlaying()) backgroundAudio.stop();
        backgroundAudio.release();
        backgroundAudio = null;}
        break;

    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
        // Lost focus for a short time, but we have to stop
        // playback. We don't release the media player because playback
        // is likely to resume
    	if(backgroundAudio!=null){
        if (backgroundAudio.isPlaying()) backgroundAudio.pause();
    	}
        break;

    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
        // Lost focus for a short time, but it's ok to keep playing
        // at an attenuated level
        if (backgroundAudio.isPlaying()) backgroundAudio.pause();
        break;
        
}}
@Override
public void onBackPressed() {
	if (this.cb!=null && this.cb.onBackPressed())
		// If a Chartboost view exists, close it and return
		return;
	else {
	Intent inMain=new Intent(this, MenuActivity.class);
	inMain.putExtra("activity","EarthRoomBegin");
	startActivityForResult(inMain,0);}
}

final public int RESULT_CLOSE_ALL = 1;
final public int NEW_GAME = 2;

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  switch(resultCode)
  {
  case NEW_GAME:
	  adViewFromXml = null;
      setResult(RESULT_CLOSE_ALL);
      this.finish();
      System.gc();
     Intent intent = new Intent(this,FireRoomActivity.class);
	   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);

  
  case RESULT_CLOSE_ALL:
      setResult(RESULT_CLOSE_ALL);
       finish();
      System.gc();
  }
  super.onActivityResult(requestCode, resultCode, data);
}

private ChartboostDelegate chartBoostDelegate = new ChartboostDelegate() {

	@Override
	public boolean shouldDisplayInterstitial(String location) {
		Log.i(TAG, "SHOULD DISPLAY INTERSTITIAL '"+location+ "'?");
		return true;
	}
	
	@Override
	public boolean shouldRequestInterstitial(String location) {
		Log.i(TAG, "SHOULD REQUEST INSTERSTITIAL '"+location+ "'?");
		return true;
	}
	
	@Override
	public void didCacheInterstitial(String location) {
		Log.i(TAG, "INTERSTITIAL '"+location+"' CACHED");
	}

	@Override
	public void didFailToLoadInterstitial(String location) {
		Log.i(TAG, "INTERSTITIAL '"+location+"' REQUEST FAILED");
	
	}

	@Override
	public void didDismissInterstitial(String location) {
				cb.cacheInterstitial(location);
		
		Log.i(TAG, "INTERSTITIAL '"+location+"' DISMISSED");

	}

	@Override
	public void didCloseInterstitial(String location) {
		Log.i(TAG, "INSTERSTITIAL '"+location+"' CLOSED");
	}

	@Override
	public void didClickInterstitial(String location) {
		Log.i(TAG, "DID CLICK INTERSTITIAL '"+location+"'");
	}

	@Override
	public void didShowInterstitial(String location) {
		Log.i(TAG, "INTERSTITIAL '" + location + "' SHOWN");
	}

	@Override
	public void didFailToLoadUrl(String url) {

		Log.i(TAG, "URL '"+url+"' REQUEST FAILED");
	}
	@Override
	public boolean shouldDisplayLoadingViewForMoreApps() {
		return true;
	}

	@Override
	public boolean shouldRequestMoreApps() {

		return true;
	}

	@Override
	public boolean shouldDisplayMoreApps() {
		Log.i(TAG, "SHOULD DISPLAY MORE APPS?");
		return true;
	}

	@Override
	public void didFailToLoadMoreApps() {
		Log.i(TAG, "MORE APPS REQUEST FAILED");
	}

	@Override
	public void didCacheMoreApps() {
		Log.i(TAG, "MORE APPS CACHED");
	}

	@Override
	public void didDismissMoreApps() {
		Log.i(TAG, "MORE APPS DISMISSED");
	}
	@Override
	public void didCloseMoreApps() {
		Log.i(TAG, "MORE APPS CLOSED");
	}
	@Override
	public void didClickMoreApps() {
		Log.i(TAG, "MORE APPS CLICKED");
	}

	@Override
	public void didShowMoreApps() {
		Log.i(TAG, "MORE APPS SHOWED");
	}

	@Override
	public boolean shouldRequestInterstitialsInFirstSession() {
		return true;
	}
};



public void onLoadButtonClick(View view) {
	this.cb.showInterstitial();
	Log.i(TAG, "showInterstitial");
}

public void onMoreButtonClick(View view) {
	this.cb.showMoreApps();	
	Log.i(TAG, "showMoreApps");
}

public void onPreloadClick(View v){
	this.cb.cacheInterstitial();	
	Log.i(TAG, "cacheInterstitial");
}


public void onPreloadMoreAppsClick(View v){
	cb.cacheMoreApps();
}


public void onPreloadClearClick(View v){
		cb.clearCache();
}
public void startEarthRoom(){
	Intent intent = new Intent(EarthRoomBeginActivity.this,EarthRoomActivity.class);
	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	startActivity(intent);
}
public void changebackground(){
	findViewById(R.id.transition).setVisibility(View.VISIBLE);
	findViewById(R.id.progress).setVisibility(View.VISIBLE);
}
private class LoadTask extends AsyncTask<Void, Void, Void> {

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		changebackground();
	}
	@Override
	protected Void doInBackground(Void... params) {	
		startEarthRoom();
		return null;
	}
}

}
