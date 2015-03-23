package com.fourelements.roomescape;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;

public class EndCreditsActivity extends Activity  implements AudioManager.OnAudioFocusChangeListener{
	ScrollView	letter;
MediaPlayer backgroundAudio;
AudioManager am;
ImageStorage storage;
static final String SOUND = "sound";
static final String ADS = "ads";
boolean mSound, mAds;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_credits);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics); 
		storage =  new ImageStorage(getResources(), metrics);
		loadImages();
	  	getScrollMaxAmount();
		startAutoScrolling();
	}
	static final String ACTIVITY = "activity";
	String activity;
	@Override
	protected void onStart() {
		super.onStart();
		mSound = (Boolean)IntentHelper.getObjectForKey(SOUND);
		if(mSound){
		am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		int result = am.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		if(result== AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
		initMediaPlayer();
		backgroundAudio.start();}}
		 activity = getIntent().getStringExtra(ACTIVITY);
	}

	private void loadImages() {
		findViewById(R.id.credits).setBackgroundDrawable(storage.getDrawable(this, "coil_transparent"));
		findViewById(R.id.letter_image).setBackgroundDrawable(storage.getDrawable(this, "end_credits_transparent"));
		ImageView letter_image =(ImageView)findViewById(R.id.letter_image);
		letter_image.setVisibility(View.VISIBLE);
		startAutoScrolling();
		letter = (ScrollView)findViewById(R.id.letter);
		letter.setVisibility(View.VISIBLE);

	}
	private Timer scrollTimer		=	null;
	private TimerTask scrollerSchedule;
	private int verticalScrollMax;
	private int scrollPos =	0;

	public void moveScrollView(){
		scrollPos	= 	(int) (letter.getScrollY() + 1.0);
		if(scrollPos >= verticalScrollMax){
			scrollPos	=	0;
		}
		letter.scrollTo(0,scrollPos);
		Log.e("moveScrollView","moveScrollView");		
	}   
	public void getScrollMaxAmount(){
		int actualWidth = (3795-(256*3));
		verticalScrollMax   = actualWidth;
	}


	  public void startAutoScrolling(){
			if (scrollTimer == null) {
				scrollTimer					=	new Timer();
				final Runnable Timer_Tick 	= 	new Runnable() {
				    @Override
					public void run() {
				    	moveScrollView();
				    }
				};
				
				if(scrollerSchedule != null){
					scrollerSchedule.cancel();
					scrollerSchedule = null;
				}
				scrollerSchedule = new TimerTask(){
					@Override
					public void run(){
						runOnUiThread(Timer_Tick);
					}
				};
				
				scrollTimer.schedule(scrollerSchedule, 30, 30);
			}
		}
		private void clearTimers(Timer timer){
		    if(timer != null) {
			    timer.cancel();
		        timer = null;
		    }
		}
		public static AssetFileDescriptor getFileDescriptor(Context ctx, String path) {
			
			AssetFileDescriptor descriptor = null;
			try {
				ZipResourceFile zip = APKExpansionSupport.getAPKExpansionZipFile(
						ctx, 7, -1);
				descriptor = zip.getAssetFileDescriptor(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return descriptor;
		}
	private void initMediaPlayer(){
		 AssetFileDescriptor descriptor = null; 
			try { 
					descriptor = getFileDescriptor(this, "expansion/audio_files/end_credits.ogg"); 
					backgroundAudio = new MediaPlayer();
					backgroundAudio.setDataSource(descriptor.getFileDescriptor(),
					  descriptor.getStartOffset(), descriptor.getLength());
					  backgroundAudio.prepare(); 
					  } 
			catch (Exception e) { } 
			finally { if (descriptor != null)
					  try{descriptor.close();} 
			catch (IOException e) {} }		
			backgroundAudio.setLooping(true);
		backgroundAudio.setVolume(0.1f, 0.1f);
		}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_delete_this_later, menu);
		return true;
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
	        if (backgroundAudio.isPlaying()) backgroundAudio.stop();
	        backgroundAudio.release();
	        backgroundAudio = null;
	        break;

	    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
	        // Lost focus for a short time, but we have to stop
	        // playback. We don't release the media player because playback
	        // is likely to resume
	        if (backgroundAudio.isPlaying()) backgroundAudio.pause();
	        break;

	    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
	        // Lost focus for a short time, but it's ok to keep playing
	        // at an attenuated level
	        if (backgroundAudio.isPlaying()) backgroundAudio.pause();
	        break;
	}}
	private void clearTimerTaks(TimerTask timerTask){
		if(timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}
	}

	@Override
	protected void onDestroy() {
 		clearTimers(scrollTimer);
		clearTimerTaks(scrollerSchedule);
		scrollTimer.cancel();
		scrollTimer = null;
		scrollerSchedule = null;		
		super.onDestroy();
	}
	@Override
	protected void onPause() {	
		
		super.onPause();
	}
	@Override
	protected void onStop() {
	
        super.onStop();
	}
	static final String ONBACK = "onback";

	@Override
	public void onBackPressed() {
		IntentHelper.addObjectForKey(mSound, SOUND);
		IntentHelper.addObjectForKey(mAds, ADS);

		if(activity!=null){
			IntentHelper.addObjectForKey("onBack", ONBACK);

				Intent inMain = new Intent(this, MenuActivity.class);
				inMain.putExtra("activity", "Credits");
				startActivityForResult(inMain, 0);}
				if(backgroundAudio!=null){
				backgroundAudio.stop();
				backgroundAudio = null;
		        am.abandonAudioFocus(this);
		        am = null;}
				//unbindDrawables(findViewById(R.id.credits));
				finish();
	}
	static void unbindDrawables(View view) {
		try{
		System.out.println("UNBINDING"+view);
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

}
