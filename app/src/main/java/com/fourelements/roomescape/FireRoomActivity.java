package com.fourelements.roomescape;

import java.io.IOException;


import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ChartboostDelegate;
import com.millennialmedia.android.MMAdView;
import com.millennialmedia.android.MMRequest;
import com.millennialmedia.android.MMSDK;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class FireRoomActivity extends Activity implements OnClickListener,
		AnimationListener, AudioManager.OnAudioFocusChangeListener {
	int wall;
	ViewFlipper vf;
	private Handler mHandler = new Handler();
	boolean wall1, wall2, wall3, wall4 = false;
	TextView tview;
	RelativeLayout parentLayout;
	CustomView customView;
	int bagClickCount = 1;
	int selectedItem = -1;
	MediaPlayer backgroundAudio;
	SoundPool sounds;
	AudioManager am;
	private long mLastClickTime = 0;
	private long mLastZoomClickTime = 0;
	boolean loaded = false;
	private static final String TAG = "Chartboost";
	int storeSoundID, bookSoundID, chestOpenSoundID, chestLockedSoundID,
			gooeyBrainSoundID, feelEmptySoundID, spiritSoundID,
			fullyPaintedSoundID, semipaintedSoundID, breakSoundID,
			drawerSoundID, fountainSoundID;
	public static final String FIRE_ROOM = "FireRoom";
	SharedPreferences settings;
	SharedPreferences.Editor editor;
	boolean red_found, blue_found, yellow_found, white_found, jug_found,
			brain_found, key_found, hammer_found, skull_found,
			paperball_found = false;
	boolean red_faded, blue_faded, key_faded, jug_faded, hammer_faded,
			brain_faded, skull_faded, yellow_faded, white_faded = false;
	int fireCount, earthCount = 0;
	boolean fireFound, earthFound, waterFound, windFound = false;
	int result;
	private Chartboost cb;
	ImageStorage storage;
	String appId = "52b0329b2d42da5580551e71";
	String appSignature = "202885a0818e2c1bff1552af968352b245e2a48e";
    LinearLayout mBanner;
    private String adSpace="MediatedBannerBottom";
	private MMAdView adViewFromXml;
	static final String SOUND = "sound";
	static final String ADS = "ads";
	boolean mSound, mAds;
	//RefreshHandler handler;
GestureListener gListener;
GestureDetector gestureDetector;
    //private StartAppAd startAppAd = new StartAppAd(this);
	public static final String PREFS_NAME = "Settings";
	public static final String ACTIVITY = "activity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fire_room);
		 MMSDK.setLogLevel(MMSDK.LOG_LEVEL_DEBUG);
		gListener = new GestureListener();
		gestureDetector = new GestureDetector(this, gListener);
		SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);

		SharedPreferences.Editor editor = preferences.edit();
	    editor.putString(ACTIVITY, "FireRoom");
	    editor.commit();
	    mAds = (Boolean)IntentHelper.getObjectForKey(ADS);
		if(mAds){
			this.cb = Chartboost.sharedChartboost();
			this.cb.onCreate(this, appId, appSignature, this.chartBoostDelegate);
		this.cb.onStart(this);
		this.cb.startSession();
		this.cb.showInterstitial();
		}
	}
	private void setMMediaAd() {
		adViewFromXml = (MMAdView) findViewById(R.id.adView);
		MMRequest request = new MMRequest();
		adViewFromXml.setMMRequest(request);
		adViewFromXml.setListener(new AdListener());
		adViewFromXml.getAd();
	}

	public void nextLevel(View view) {
		view.setVisibility(View.GONE);
		new LoadTask().execute();
	}

	private void loadImages() {
		RelativeLayout wall1 = getParentLayout(1);
		BitmapDrawable bck = storage.getDrawable(this, "wall_nobrainjar");
		wall1.setBackgroundDrawable(bck);

		RelativeLayout wall2 = getParentLayout(2);
		wall2.setBackgroundDrawable(storage.getDrawable(this,
				"wall2_cushion_down"));

		getParentLayout(3).setBackgroundDrawable(
				storage.getDrawable(this, "wall3_paperball"));
		findViewById(R.id.cabinet_zoomed).setBackgroundDrawable(
				storage.getDrawable(this, "alchemist_cabinet"));

		getParentLayout(4).setBackgroundDrawable(
				storage.getDrawable(this, "wall4_cabinet_closed"));
		findViewById(R.id.frames_zoomed_image).setBackgroundDrawable(
				storage.getDrawable(this, "frames_zoomed"));
	}

	public void temp() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		storage = new ImageStorage(getResources(), metrics);
		customView = (CustomView) findViewById(R.id.customView);
		tview = (TextView) customView.findViewById(R.id.closeup_text);
		vf = (ViewFlipper) findViewById(R.id.fire_room_flipper);
		wall = 1;
		wall1 = true;
		parentLayout = getParentLayout(wall);
	}

	private void initMediaPlayer() {
		AssetFileDescriptor descriptor = null;
		try {
			descriptor = getFileDescriptor(this,
					"expansion/audio_files/fire_background.ogg");
			backgroundAudio = new MediaPlayer();
			backgroundAudio.setDataSource(descriptor.getFileDescriptor(),
					descriptor.getStartOffset(), descriptor.getLength());
			backgroundAudio.prepare();
		} catch (Exception e) {
		} finally {
			if (descriptor != null)
				try {
					descriptor.close();
				} catch (IOException e) {
				}
		}

		backgroundAudio = MediaPlayer.create(FireRoomActivity.this,
				R.raw.fire_background);
		backgroundAudio.setLooping(true);
		backgroundAudio.setVolume(0.1f, 0.1f);

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

	private void loadSounds() throws IOException {
		String folder = "expansion/audio_files/";
		AssetFileDescriptor store_fd = getFileDescriptor(this, folder
				+ "store.ogg");
		storeSoundID = sounds.load(store_fd.getFileDescriptor(),
				store_fd.getStartOffset(), store_fd.getLength(), 1);
		if (store_fd != null)
			store_fd.close();

		AssetFileDescriptor semipainted_fd = getFileDescriptor(this, folder
				+ "semipainted.ogg");
		semipaintedSoundID = sounds.load(semipainted_fd.getFileDescriptor(),
				semipainted_fd.getStartOffset(), semipainted_fd.getLength(), 1);
		if (semipainted_fd != null)
			semipainted_fd.close();

		AssetFileDescriptor fullyPainted_fd = getFileDescriptor(this, folder
				+ "fully_painted.ogg");
		fullyPaintedSoundID = sounds.load(fullyPainted_fd.getFileDescriptor(),
				fullyPainted_fd.getStartOffset(), fullyPainted_fd.getLength(),
				1);
		if (fullyPainted_fd != null)
			fullyPainted_fd.close();

		AssetFileDescriptor book_fd = getFileDescriptor(this, folder
				+ "book.ogg");
		bookSoundID = sounds.load(book_fd.getFileDescriptor(),
				book_fd.getStartOffset(), book_fd.getLength(), 1);
		if (book_fd != null)
			book_fd.close();

		AssetFileDescriptor feelEmpty_fd = getFileDescriptor(this, folder
				+ "i_feel_empty_tonight.ogg");
		feelEmptySoundID = sounds.load(feelEmpty_fd.getFileDescriptor(),
				feelEmpty_fd.getStartOffset(), feelEmpty_fd.getLength(), 1);
		if (feelEmpty_fd != null)
			feelEmpty_fd.close();

		AssetFileDescriptor ceramicBreak_fd = getFileDescriptor(this, folder
				+ "ceramic_break.ogg");
		breakSoundID = sounds.load(ceramicBreak_fd.getFileDescriptor(),
				ceramicBreak_fd.getStartOffset(), ceramicBreak_fd.getLength(),
				1);
		if (ceramicBreak_fd != null)
			ceramicBreak_fd.close();

		AssetFileDescriptor chestOpen_fd = getFileDescriptor(this, folder
				+ "chest_open.ogg");
		chestOpenSoundID = sounds.load(chestOpen_fd.getFileDescriptor(),
				chestOpen_fd.getStartOffset(), chestOpen_fd.getLength(), 1);
		if (chestOpen_fd != null)
			chestOpen_fd.close();

		AssetFileDescriptor chestLocked_fd = getFileDescriptor(this, folder
				+ "chest_locked.ogg");
		chestLockedSoundID = sounds.load(chestLocked_fd.getFileDescriptor(),
				chestLocked_fd.getStartOffset(), chestLocked_fd.getLength(), 1);
		if (chestLocked_fd != null)
			chestLocked_fd.close();

		AssetFileDescriptor spirit_fd = getFileDescriptor(this, folder
				+ "spirit.ogg");
		spiritSoundID = sounds.load(spirit_fd.getFileDescriptor(),
				spirit_fd.getStartOffset(), spirit_fd.getLength(), 1);
		if (spirit_fd != null)
			spirit_fd.close();

		AssetFileDescriptor gooey_fd = getFileDescriptor(this, folder
				+ "gooey_brain.ogg");
		gooeyBrainSoundID = sounds.load(gooey_fd.getFileDescriptor(),
				gooey_fd.getStartOffset(), gooey_fd.getLength(), 1);
		if (gooey_fd != null)
			gooey_fd.close();

		AssetFileDescriptor fountain_fd = getFileDescriptor(this, folder
				+ "fountain.ogg");
		fountainSoundID = sounds.load(fountain_fd.getFileDescriptor(),
				fountain_fd.getStartOffset(), fountain_fd.getLength(), 1);
		if (fountain_fd != null)
			fountain_fd.close();

		AssetFileDescriptor drawer_fd = getFileDescriptor(this, folder
				+ "drawer.ogg");
		drawerSoundID = sounds.load(drawer_fd.getFileDescriptor(),
				drawer_fd.getStartOffset(), drawer_fd.getLength(), 1);
		if (drawer_fd != null)
			drawer_fd.close();

		/*
		 * storeSoundID = sounds.load(this, R.raw.store, 1); semipaintedSoundID
		 * = sounds.load(this, R.raw.semipainted, 1); fullyPaintedSoundID =
		 * sounds.load(this, R.raw.fully_painted, 1); bookSoundID =
		 * sounds.load(this, R.raw.book, 1); feelEmptySoundID =
		 * sounds.load(this, R.raw.i_feel_empty_tonight, 1); breakSoundID =
		 * sounds.load(this, R.raw.ceramic_break, 1); chestOpenSoundID =
		 * sounds.load(this, R.raw.chest_open, 1); chestLockedSoundID =
		 * sounds.load(this, R.raw.chest_locked, 1); fountainSoundID =
		 * sounds.load(this, R.raw.fountain, 1); gooeyBrainSoundID =
		 * sounds.load(this, R.raw.gooey_brain, 1); spiritSoundID =
		 * sounds.load(this, R.raw.spirit, 1); drawerSoundID = sounds.load(this,
		 * R.raw.drawer, 1);
		 */
	}

	int streamID;

	private void playSound(int soundID) {
		if (loaded) {
			streamID = sounds.play(soundID, 1, 1, 1, 0, 1);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_fire_room, menu);
		return true;
	}

	boolean roomIsGlanced = false;

	public void nextWall(View view) {
		if (wall == 1) {
			vf.showNext();
			wall = 2;
			parentLayout = getParentLayout(wall);
			wall2 = true;
		} else if (wall == 2) {
			vf.showNext();
			wall = 3;
			parentLayout = getParentLayout(wall);
			wall3 = true;
		} else if (wall == 3) {
			vf.showNext();
			wall = 4;
			parentLayout = getParentLayout(wall);
			wall4 = true;
		} else if (wall == 4) {
			vf.showNext();
			wall = 1;
			parentLayout = getParentLayout(wall);
		}
		if (roomIsGlanced == false) {
			if (wall1 && wall2 && wall3 && wall4) {
				tview.setText(getString(R.string.fire_room));
				roomIsGlanced = true;
				// fade out view nicely after 5 seconds
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						tview.setText(null);
					}
				}, 4000);
			}
		}
	    bagClickCount =2;
		bagClicked(null);
	}

	public void previousWall(View v) {
		 bagClickCount =2;
			bagClicked(null);
		if (wall == 1) {
			vf.showPrevious();
			wall = 4;
			parentLayout = getParentLayout(wall);
			wall4 = true;
		} else if (wall == 2) {
			vf.showPrevious();
			wall = 1;
			parentLayout = getParentLayout(wall);
		} else if (wall == 3) {
			vf.showPrevious();
			wall = 2;
			parentLayout = getParentLayout(wall);
			wall2 = true;
		} else if (wall == 4) {
			vf.showPrevious();
			wall = 3;
			parentLayout = getParentLayout(wall);
			wall3 = true;
		}
		if (roomIsGlanced == false) {
			if (wall1 && wall2 && wall3 && wall4) {
				tview.setText(getString(R.string.fire_room));
				roomIsGlanced = true;
				// fade out view nicely after 5 seconds
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						tview.setText(null);
					}
				}, 4000);
			}
		}
	}

	private RelativeLayout getParentLayout(int wall) {
		int id = wall == 1 ? R.id.first : wall == 2 ? R.id.second
				: wall == 3 ? R.id.third : R.id.fourth;
		return (RelativeLayout) findViewById(id);
	}

	private boolean hammerFound = false;
	boolean paperballFound = false;
boolean removed = false;
	public void zoomImage(View view) {
		if (SystemClock.elapsedRealtime() - mLastZoomClickTime < 1500){
            return;
        }
        mLastZoomClickTime = SystemClock.elapsedRealtime();
		bagClickCount = 2;
		bagClicked(null);
		if (!removed) {
			LinearLayout transition = (LinearLayout) findViewById(R.id.transition);
			((BitmapDrawable) transition.getBackground()).getBitmap().recycle();
			transition.setBackgroundDrawable(null);
			transition.setVisibility(View.GONE);
			removed = true;
		}
		ImageView zoomedImage = null;
		if (view.getId() == R.id.brainjar) {
			if (selectedItem == R.id.jug_item) {
				playSound(gooeyBrainSoundID);
				((BitmapDrawable) view.getBackground()).getBitmap().recycle();
				view.setBackgroundResource(R.drawable.jar_noliquid);
				// view.setBackgroundDrawable(null);
				// view.setBackgroundDrawable(storage.getDrawable(this,
				// "jar_noliquid"));
				useItem();
				selectedItem = -1;
				jug_faded = true;

			} else {
				if (jug_faded == false) {
					zoomedImage = (ImageView) parentLayout
							.findViewById(R.id.brainjar_zoomed_image);
					zoomedImage.setVisibility(View.VISIBLE);
					zoomedImage.setTag(R.id.brainjar);
					tview.setText(getString(R.string.brain_jar_with_liquid));
				} else {
					zoomedImage = (ImageView) parentLayout
							.findViewById(R.id.zoomed_image);
					// zoomedImage.setBackgroundResource(R.drawable.brain_zoomed);
					zoomedImage.setBackgroundDrawable(storage.getDrawable(this,
							"brain_zoomed"));
					view.setClickable(false);
					((BitmapDrawable) view.getBackground()).getBitmap()
							.recycle();
					view.setBackgroundResource(R.drawable.jar_empty);
					// view.setBackgroundDrawable(storage.getDrawable(this,
					// "jar_empty"));
					zoomedImage.setTag(R.id.brain);
				}
			}
		} else if (view.getId() == R.id.red_paint) {
			view.setVisibility(View.GONE);
			zoomedImage = (ImageView) parentLayout
					.findViewById(R.id.zoomed_image);
			zoomedImage.setBackgroundDrawable(storage.getDrawable(this,
					"red_zoomed"));
			// zoomedImage.setBackgroundResource(R.drawable.red_zoomed);
			zoomedImage.setTag(R.id.red_paint);
			tview.setText(getString(R.string.red_paint));
			red_found = true;
		} else if (view.getId() == R.id.blue) {
			zoomedImage = (ImageView) parentLayout
					.findViewById(R.id.zoomed_image);
			// zoomedImage.setBackgroundResource(R.drawable.blue_zoomed);
			zoomedImage.setBackgroundDrawable(storage.getDrawable(this,
					"blue_zoomed"));
			zoomedImage.setTag(R.id.blue);
			view.setVisibility(View.INVISIBLE);
		} else if (view.getId() == R.id.white) {
			zoomedImage = (ImageView) parentLayout
					.findViewById(R.id.zoomed_image);
			zoomedImage.setBackgroundDrawable(storage.getDrawable(this,
					"white_zoomed"));
			// zoomedImage.setBackgroundResource(R.drawable.white_zoomed);
			zoomedImage.setTag(R.id.white);
			view.setVisibility(View.INVISIBLE);
		}else if (view.getId() == R.id.riddle_book) {
			playSound(bookSoundID);
			zoomedImage = (ImageView) parentLayout
					.findViewById(R.id.riddleBook_zoomed_image);
			//zoomedImage.setVisibility(View.VISIBLE);
			zoomedImage.setTag(R.id.riddle_book);
			zoomedImage.setVisibility(View.VISIBLE);
		} else if (view.getId() == R.id.skull_inchest) {
			playSound(spiritSoundID);
			tview.setText(getString(R.string.let_me_rest));
			zoomedImage = (ImageView) parentLayout
					.findViewById(R.id.zoomed_image);
			// zoomedImage.setBackgroundResource(R.drawable.skull_zoomed);
			zoomedImage.setBackgroundDrawable(storage.getDrawable(this,
					"skull_zoomed"));
			zoomedImage.setTag(R.id.skull_inchest);
			view.setVisibility(View.INVISIBLE);
			parentLayout.removeView(view);
			ImageView chest = (ImageView) parentLayout.findViewById(R.id.chest);
			/*
			 * ((BitmapDrawable) chest.getBackground()).getBitmap().recycle();
			 * chest.setBackgroundDrawable(null);
			 * chest.setBackgroundDrawable(storage
			 * .getDrawable(this,"chest_empty"));
			 */
			chest.setBackgroundResource(R.drawable.chest_empty);
		} else if (view.getId() == R.id.hammer) {
			zoomedImage = (ImageView) parentLayout
					.findViewById(R.id.zoomed_image);
			// zoomedImage.setBackgroundResource(R.drawable.hammer_zoomed);
			zoomedImage.setBackgroundDrawable(storage.getDrawable(this,
					"hammer_zoomed"));
			zoomedImage.setTag(R.id.hammer);
			view.setVisibility(View.INVISIBLE);
			parentLayout.removeView(view);
			hammerFound = true;
			cushionDown();
			tview.setText(getString(R.string.hammer));
		} else if (view.getId() == R.id.paper_ball) {
			zoomedImage = (ImageView) parentLayout
					.findViewById(R.id.zoomed_image);
			// zoomedImage.setBackgroundResource(R.drawable.hint_zoomed);
			zoomedImage.setBackgroundDrawable(storage.getDrawable(this,
					"hint_zoomed"));
			zoomedImage.setTag(R.id.paper_ball);
			view.setVisibility(View.INVISIBLE);
			((BitmapDrawable) parentLayout.getBackground()).getBitmap().recycle();
			// parentLayout.setBackgroundResource(R.drawable.wall3_nopaperball);
			parentLayout.setBackgroundDrawable(storage.getDrawable(this,
					"wall3_nopaperball"));
			paperballFound = true;
			tview.setText(getString(R.string.colour_code));
		} else if (view.getId() == R.id.jug) {
			zoomedImage = (ImageView) parentLayout
					.findViewById(R.id.zoomed_image);
			// zoomedImage.setBackgroundResource(R.drawable.jug_zoomed);
			zoomedImage.setBackgroundDrawable(storage.getDrawable(this,
					"jug_zoomed"));
			zoomedImage.setTag(R.id.jug);
			view.setBackgroundResource(0);
		} else if (view.getId() == R.id.key) {
			zoomedImage = (ImageView) parentLayout
					.findViewById(R.id.zoomed_image);
			// zoomedImage.setBackgroundResource(R.drawable.key_zoomed_fr);
			zoomedImage.setBackgroundDrawable(storage.getDrawable(this,
					"key_zoomed_fr"));
			zoomedImage.bringToFront();
			zoomedImage.setTag(R.id.key);
			view.setClickable(false);
view.setVisibility(View.GONE);
LinearLayout cabinet = (LinearLayout) parentLayout
					.findViewById(R.id.cabinet_zoomed);
			((BitmapDrawable) cabinet.getBackground()).getBitmap().recycle();
			cabinet.setBackgroundDrawable(null);
			// cabinet.setBackgroundResource(R.drawable.alchemist_cabinet_nokey);
			cabinet.setBackgroundDrawable(storage.getDrawable(this,
					"alchemist_cabinet_nokey"));
		} else if (view.getId() == R.id.yellow) {
			zoomedImage = (ImageView) parentLayout
					.findViewById(R.id.zoomed_image);
			// zoomedImage.setBackgroundResource(R.drawable.yellow_zoomed);
			zoomedImage.setBackgroundDrawable(storage.getDrawable(this,
					"yellow_zoomed"));
			zoomedImage.setTag(R.id.yellow);
			view.setVisibility(View.INVISIBLE);
			parentLayout.removeView(view);
		}
		if (zoomedImage != null&&view.getId() != R.id.riddle_book) {
			zoomedImage.setOnClickListener(this);
			zoomedImage.setVisibility(View.VISIBLE);
		}
	}
public void closeRiddleBook(View view){
	playSound(bookSoundID);
	view.setVisibility(View.GONE);
}
	@Override
	public void onClick(View view) {
		 if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
	            return;
	        }
	        mLastClickTime = SystemClock.elapsedRealtime();
			((BitmapDrawable) view.getBackground()).getBitmap().recycle();
			view.setVisibility(View.GONE);
		int tag = (Integer) view.getTag();
		if (tag == R.id.brainjar) {
			view.setVisibility(View.GONE);
			tview.setText(null);
		} else if (tag == R.id.jar_noliquid) {
			ImageButton newItem = (ImageButton) customView
					.findViewById(R.id.ImageButton09);
			newItem.setBackgroundResource(R.drawable.brain_stored);
			// newItem.setBackgroundDrawable(storage.getDrawable(this,
			// "brain_stored"));
			newItem.setId(R.id.brain_item);
			bagClickCount = 1;
			bagClicked(null);
		} else if (tag == R.id.red_paint) {
			storeRed(false);
			bagClickCount = 1;
			bagClicked(null);
			tview.setText(null);
			view.setTag(null);
		} else if (tag == R.id.blue) {
			storeBlue(false);
			bagClickCount = 1;
			bagClicked(null);
		} else if (tag == R.id.white) {
			storeWhite(false);
			bagClickCount = 1;
			bagClicked(null);
		} /*else if (tag == R.id.riddle_book) {
			playSound(bookSoundID);
			view.setVisibility(View.GONE);
		}*/ else if (tag == R.id.skull_inchest) {
			storeSkull(false);
			bagClickCount = 1;
			bagClicked(null);
			tview.setText(null);
			skull_found = true;
		} else if (tag == R.id.hammer) {
			storeHammer(false);
			bagClickCount = 1;
			bagClicked(null);
			parentLayout.findViewById(R.id.cushion).setVisibility(View.VISIBLE);
			tview.setText(null);
		} /*else if (tag == R.id.paper_ball) {
			if (!paperball_found) {
				storePaperball();
				paperball_found = true;
			}
			bagClickCount = 1;
			bagClicked(null);
			tview.setText(null);
		} */else if (tag == R.id.jug) {
			storeJug(false);
			bagClickCount = 1;
			bagClicked(null);
		} else if (tag == R.id.key) {
			storeKey(false);
			bagClickCount = 1;
			bagClicked(null);
		} else if (tag == R.id.brain) {
			storeBrain(false);
			bagClickCount = 1;
			bagClicked(null);
		} else if (tag == R.id.yellow) {
			storeYellow(false);
			bagClickCount = 1;
			bagClicked(null);
		}
		if (tag != R.id.brainjar && tag != R.id.riddle_book && !paperball_found) {
			playSound(storeSoundID);
		}
		
	}
public void closePaperball(View view){
	if (!paperball_found) {
		storePaperball();
		paperball_found = true;
	}
	bagClickCount = 1;
	bagClicked(null);
	tview.setText("");
}
	private void storeWhite(boolean faded) {
		ImageButton newItem = (ImageButton) customView
				.findViewById(R.id.ImageButton08);

		if (faded == false) {
			newItem.setBackgroundResource(R.drawable.white_stored);
			// newItem.setBackgroundDrawable(storage.getDrawable(this,"white_stored"));
			newItem.setId(R.id.white_item);
			newItem.setTag("white");
		} else {
			newItem.setBackgroundResource(R.drawable.white_faded);
			// newItem.setBackgroundDrawable(storage.getDrawable(this,"white_faded"));
		}
	}

	private void storeBlue(boolean faded) {
		ImageButton newItem = (ImageButton) customView
				.findViewById(R.id.ImageButton07);

		if (faded == false) {
			newItem.setBackgroundResource(R.drawable.blue_stored);
			// newItem.setBackgroundDrawable(storage.getDrawable(this,"blue_stored"));
			newItem.setId(R.id.blue_item);
			newItem.setTag("blue");
		} else {
			newItem.setBackgroundResource(R.drawable.blue_faded);
			// newItem.setBackgroundDrawable(storage.getDrawable(this,"blue_faded"));
		}
	}

	private void storeRed(boolean faded) {
		ImageButton newItem = (ImageButton) customView
				.findViewById(R.id.ImageButton01);

		if (faded == false) {
			newItem.setBackgroundResource(R.drawable.red_stored);
			// newItem.setBackgroundDrawable(storage.getDrawable(this,"red_stored"));
			newItem.setId(R.id.red_item);
			newItem.setTag("red");
		} else {
			newItem.setBackgroundResource(R.drawable.red_faded);
			// newItem.setBackgroundDrawable(storage.getDrawable(this,"red_faded"));
		}
	}

	private void storeBrain(boolean faded) {
		ImageButton newItem = (ImageButton) customView
				.findViewById(R.id.ImageButton09);

		if (faded == false) {
			newItem.setBackgroundResource(R.drawable.brain_stored);
			// newItem.setBackgroundDrawable(storage.getDrawable(this,"brain_stored"));

			newItem.setId(R.id.brain_item);
			newItem.setTag("brain");

		} else {
			newItem.setBackgroundResource(R.drawable.brain_faded);
			// newItem.setBackgroundDrawable(storage.getDrawable(this,"brain_faded"));
		}
	}

	private void storeSkull(boolean faded) {
		ImageButton newItem = (ImageButton) customView
				.findViewById(R.id.ImageButton02);

		if (faded == false) {
			newItem.setBackgroundResource(R.drawable.skull_stored);
			// newItem.setBackgroundDrawable(storage.getDrawable(this,"skull_stored"));
			newItem.setId(R.id.skull_item);
			newItem.setTag("skull");

		} else {
			newItem.setBackgroundResource(R.drawable.skull_faded);
			// newItem.setBackgroundDrawable(storage.getDrawable(this,"skull_faded"));
		}
	}

	private void storeHammer(boolean faded) {
		ImageButton newItem = (ImageButton) customView
				.findViewById(R.id.ImageButton03);
		if (faded == false) {
			newItem.setBackgroundResource(R.drawable.hammer_stored);
			// newItem.setBackgroundDrawable(storage.getDrawable(this,"hammer_stored"));
			newItem.setId(R.id.hammer_item);
			newItem.setTag("hammer");
		} else {
			newItem.setBackgroundResource(R.drawable.hammer_faded);
			// newItem.setBackgroundDrawable(storage.getDrawable(this,"hammer_faded"));
		}
	}

	private void storeKey(boolean faded) {
		ImageButton newItem = (ImageButton) customView
				.findViewById(R.id.ImageButton10);
		if (faded == false) {
			// newItem.setBackgroundDrawable(storage.getDrawable(this,"key_fr_stored"));
			newItem.setBackgroundResource(R.drawable.key_fr_stored);
			newItem.setId(R.id.key_fr_item);
			newItem.setTag("key_fr");

		} else {
			newItem.setBackgroundResource(R.drawable.key_fr_faded);
			// newItem.setBackgroundDrawable(storage.getDrawable(this,"key_fr_faded"));
		}
	}

	private void storeYellow(boolean faded) {
		ImageButton newItem = (ImageButton) customView
				.findViewById(R.id.ImageButton06);

		if (faded == false) {
			newItem.setBackgroundResource(R.drawable.yellow_stored);
			// newItem.setBackgroundDrawable(storage.getDrawable(this,"yellow_stored"));
			newItem.setId(R.id.yellow_item);
			newItem.setTag("yellow");
		} else {
			newItem.setBackgroundResource(R.drawable.yellow_faded);
			// newItem.setBackgroundDrawable(storage.getDrawable(this,"yellow_faded"));
		}
	}

	private void storePaperball() {
		ImageButton newItem = (ImageButton) customView
				.findViewById(R.id.ImageButton04);
		newItem.setBackgroundResource(R.drawable.colourhint_stored);
		// newItem.setBackgroundDrawable(storage.getDrawable(this,"colourhint_stored"));
		newItem.setId(R.id.colourhint_item);
		newItem.setTag("colourhint_item");

	}

	private void storeJug(boolean faded) {
		ImageButton newItem = (ImageButton) customView
				.findViewById(R.id.ImageButton05);
		if (faded == false) {
			newItem.setBackgroundResource(R.drawable.jug_stored);
			// newItem.setBackgroundDrawable(storage.getDrawable(this,"jug_stored"));
			newItem.setId(R.id.jug_item);
			newItem.setTag("jug");

		} else {
			newItem.setBackgroundResource(R.drawable.jug_faded);
			// newItem.setBackgroundDrawable(storage.getDrawable(this,"jug_faded"));
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (backgroundAudio != null) {
			backgroundAudio.stop();
			backgroundAudio.release();
			backgroundAudio = null;}
		if (cb != null) {
			this.cb.onDestroy(this);
			this.cb = null;
			cb = null;
		}
		System.gc();
	}

	private void unbindDrawables(View view) {
		try {
			System.out.println("UNBINDING" + view);
			if (view.getBackground() != null) {
				if (view.getBackground() instanceof BitmapDrawable) {
					((BitmapDrawable) view.getBackground()).getBitmap()
							.recycle();
				}
			}
			if (view instanceof ViewGroup) {
				for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
					unbindDrawables(((ViewGroup) view).getChildAt(i));
				}
				((ViewGroup) view).removeAllViews();
			} else if (view instanceof View) {
				view.getBackground().setCallback(null);
				view = null;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
		if(sounds!=null){
		sounds.release();
		sounds = null;}
		super.onPause();
	}

	void destroyWebView(ViewGroup viewGroup) {
		for (int i = 0; i < viewGroup.getChildCount(); i++) {
			if (viewGroup.getChildAt(i) instanceof WebView) {
				WebView view = (WebView) viewGroup.getChildAt(i);
				viewGroup.removeView(view);
				view.destroy();
				view = null;
				return;
			}
		}
	}

	@Override
	protected void onRestart() {
		Log.d(TAG, "onRestart");
		super.onRestart();
	}

	@Override
	public void onResume(){ 
		super.onResume(); 
		//startAppAd.onResume();
	}


	@Override
	protected void onStart() {
		super.onStart();
		mSound = (Boolean)IntentHelper.getObjectForKey(SOUND);
		System.gc();
		if(!mAds){
			if(adViewFromXml!=null){
			adViewFromXml.removeAllViews();}
		}
		sounds = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		sounds.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool sp, int sid, int status) {
				Log.d(getClass().getSimpleName(), "Sound is now loaded");
				loaded = true;
			}
		});
		try {
			loadSounds();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(mSound){
		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		result = am.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN);	
		if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			initMediaPlayer();
			backgroundAudio.start();
		}
		}
	}

	@Override
	protected void onStop() {
		mHandler.removeCallbacks(null);
		IntentHelper.addObjectForKey(mSound, SOUND);
		IntentHelper.addObjectForKey(mAds, ADS);
		super.onStop();
		if (cb != null) {
			this.cb.onStop(this);
		}
		if(am!=null){
			am.abandonAudioFocus(this);
			if(backgroundAudio!=null){backgroundAudio.stop();
			backgroundAudio.release();
			backgroundAudio=null;}}
	}

	public void bagClicked(View view) {
		if (bagClickCount == 1) {
			openTheBag();
			bagClickCount++;
		} else {
			findViewById(R.id.storage).setVisibility(View.INVISIBLE);
			bagClickCount = 1;
		}
	}

	private void openTheBag() {
		findViewById(R.id.storage).setVisibility(View.VISIBLE);
		selectedItem = -1;
	}

	private boolean chest_opened = false;

	public void openTheLock(View view) {
		if (selectedItem == R.id.key_fr_item) {
			playSound(chestOpenSoundID);
			ImageView chest = (ImageView) parentLayout.findViewById(R.id.chest);
			((BitmapDrawable) chest.getBackground()).getBitmap().recycle();
			chest.setBackgroundResource(R.drawable.chest_opened);
			/*
			 * BitmapDrawable chestDrawable = (BitmapDrawable)
			 * chest.getBackground(); Bitmap chestBitmap =
			 * chestDrawable.getBitmap(); chestBitmap.recycle();
			 * view.setBackgroundDrawable(null);
			 * chest.setBackgroundDrawable(storage
			 * .getDrawable(this,"chest_opened"));
			 */
			parentLayout.findViewById(R.id.lock).setVisibility(View.GONE);
			parentLayout.findViewById(R.id.skull_inchest).setVisibility(
					View.VISIBLE);
			useItem();
			selectedItem = -1;
			chest_opened = true;
		} else if (chest_opened == false) {
			playSound(chestLockedSoundID);
			tview.setText(getString(R.string.locked));
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					tview.setText(null);
				}
			}, 2000);
		}
	}

	boolean up = false;

	public void liftCushion(View view) {
		if (selectedItem == R.id.skull_item) {
			tview.setText(getString(R.string.empty_tonight));
			playSound(feelEmptySoundID);
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					tview.setText(null);
				}
			}, 3000);
			parentLayout.findViewById(R.id.skull).setVisibility(View.VISIBLE);
			useItem();
			selectedItem = -1;
		} else {
			((BitmapDrawable) parentLayout.getBackground()).getBitmap()
					.recycle();
			parentLayout.setBackgroundDrawable(null);
			if (up == false) {
				if (hammerFound == false) {
					// parentLayout.setBackgroundResource(R.drawable.wall2_cushion_up);
					parentLayout.setBackgroundDrawable(storage.getDrawable(
							this, "wall2_cushion_up"));
					parentLayout.findViewById(R.id.hammer).setVisibility(
							View.VISIBLE);
					parentLayout.findViewById(R.id.hammer).setClickable(true);
				} else {
					// parentLayout.setBackgroundResource(R.drawable.wall2_cushion_up_empty);
					parentLayout.setBackgroundDrawable(storage.getDrawable(
							this, "wall2_cushion_up_empty"));
				}
				up = true;
			} else if (up == true) {
				cushionDown();
			}
		}
	}

	private void cushionDown() {
		// parentLayout.setBackgroundResource(R.drawable.wall2_cushion_down);
		parentLayout.setBackgroundDrawable(storage.getDrawable(this,
				"wall2_cushion_down"));
		up = false;
		Button hammer = (Button) parentLayout.findViewById(R.id.hammer);
		if (hammer != null)
			hammer.setClickable(false);
	}

	boolean skullIsBroken = false;

	public void skullTapped(View view) {
		if (selectedItem == R.id.hammer_item) {
			playSound(breakSoundID);
			skullIsBroken = true;
			((BitmapDrawable) view.getBackground()).getBitmap().recycle();
			view.setBackgroundResource(R.drawable.skull_empty);
			useItem();
			selectedItem = -1;
		} else if (skullIsBroken == true) {
			if (selectedItem == R.id.brain_item) {
				playSound(drawerSoundID);
				((BitmapDrawable) view.getBackground()).getBitmap().recycle();
				view.setBackgroundResource(R.drawable.skull_with_brain);
				useItem();
				selectedItem = -1;
				openTheDrawer();
				view.setClickable(false);
			}
		}
	}

	public void itemSelected(View view) {
		if (view.getId() == R.id.colourhint_item) {
			ImageView zoomedImage = (ImageView) parentLayout
					.findViewById(R.id.zoomed_image);
			// zoomedImage.setBackgroundResource(R.drawable.hint_zoomed);
			zoomedImage.setBackgroundDrawable(storage.getDrawable(this,
					"hint_zoomed"));
			zoomedImage.setTag(R.id.paper_ball);
			zoomedImage.setVisibility(View.VISIBLE);
			zoomedImage.setOnClickListener(this);
			tview.setText(getString(R.string.colour_code));
		}

		else {

			if (view.getBackground().getConstantState() != getResources()
					.getDrawable(R.drawable.single_square).getConstantState()) {
				String packageName = this.getPackageName();
				// deselect other images
				if (selectedItem != -1 && selectedItem != view.getId()) {
					String stored_name = getResources().getResourceEntryName(
							selectedItem);
					stored_name = stored_name.replace("item", "stored");
					int stored_background = getResources().getIdentifier(
							stored_name, "drawable", packageName);
					findViewById(selectedItem).setBackgroundResource(
							stored_background);
					// String stored_name = (String)view.getTag()+"_stored";
					// findViewById(selectedItem).setBackgroundDrawable(storage.getDrawable(this,stored_name+""));
				}
				selectedItem = view.getId();

				// select this image
				String selected_item = getResources().getResourceEntryName(
						view.getId());
				selected_item = selected_item.replace("item", "selected");
				int selected_background = getResources().getIdentifier(
						selected_item, "drawable", packageName);
				view.setBackgroundResource(selected_background);
				// String selected_item = (String)view.getTag()+"_selected";
				// view.setBackgroundDrawable(storage.getDrawable(this,
				// selected_item+""));
				selected_item = null;

			}
		}
	}

	private void useItem() {
		String stored_name = getResources().getResourceEntryName(selectedItem);
		stored_name = stored_name.replace("item", "faded");
		int stored_background = getResources().getIdentifier(stored_name,
				"drawable", this.getPackageName());
		ImageButton item = (ImageButton) findViewById(selectedItem);
		((BitmapDrawable) item.getBackground()).getBitmap().recycle();
		item.setBackgroundResource(stored_background);
		// item.setBackgroundDrawable(storage.getDrawable(this, stored_name));
		findViewById(selectedItem).setClickable(false);
	}

	public void openTheDrawer() {
		RelativeLayout wall4 = getParentLayout(4);
		((BitmapDrawable) wall4.getBackground()).getBitmap().recycle();
		wall4.setBackgroundDrawable(null);
		wall4.setBackgroundDrawable(storage.getDrawable(this,
				"wall4_paint_in_drawer"));
		wall4.findViewById(R.id.white).setVisibility(View.VISIBLE);
	}

	public void zoomFrames(View view) {
		findViewById(R.id.frames_zoomed_image).setVisibility(
				View.VISIBLE);
		findViewById(R.id.frameZoomedLayout).setVisibility(
				View.VISIBLE);

	}

	public void hideZoomedFrames(View view) {
		parentLayout.findViewById(R.id.frames_zoomed_image).setVisibility(
				View.INVISIBLE);
		parentLayout.findViewById(R.id.frameZoomedLayout).setVisibility(
				View.INVISIBLE);
	}

	int blueFound, yellowFound = 0;
	boolean yellowFire, redFire,yellowEarth, blueEarth, blueWater;
public void changeIcon(View view){
int frame = view.getId();

	switch (selectedItem) {
	case R.id.yellow_item:
		if(frame == R.id.fire_icon && yellowFire == false){
			yellowFire = true;
			changeFireIcon(view);
			if(yellowEarth){
				useItem();
			}
			if(redFire){
				fireFound = true;
				checkCombination();
			}
		}
		else if(frame == R.id.earth_icon&&yellowEarth==false){
			yellowEarth = true;
			changeEarthIcon(view);
			if(yellowFire){
				useItem();
			}
			if(blueEarth){
				earthFound = true;
				checkCombination();
			}
	}
		break;
	case R.id.red_item:
		if(frame == R.id.fire_icon && redFire == false){
			redFire = true;
		changeFireIcon(view);
		useItem();
		if(yellowFire){
			fireFound = true;
		checkCombination();}
		}
		break;
	case R.id.blue_item:
		if(frame == R.id.water_icon && blueWater == false){
			blueWater = true;
			playSound(fullyPaintedSoundID);
			((BitmapDrawable) view.getBackground()).getBitmap().recycle();
			((BitmapDrawable) parentLayout.findViewById(R.id.water_frame)
					.getBackground()).getBitmap().recycle();
			view.setBackgroundResource(R.drawable.water_icon_coloured);
			parentLayout.findViewById(R.id.water_frame)
					.setBackgroundResource(
							R.drawable.small_water_icon_coloured);
			waterFound = true;
			checkCombination();
			if (blueEarth) {
				useItem();
			} 
		}
		else if(frame == R.id.earth_icon&&blueEarth == false){
			blueEarth = true;
			changeEarthIcon(view);
			if (blueWater) {
				useItem();
			} 	
			if(yellowEarth){
				earthFound = true;
				checkCombination();
			}
		}
		break;
	case R.id.white_item:
		if(frame == R.id.wind_icon&&windFound == false){
		playSound(fullyPaintedSoundID);
		((BitmapDrawable) view.getBackground()).getBitmap().recycle();
		((BitmapDrawable) parentLayout.findViewById(R.id.wind_frame)
				.getBackground()).getBitmap().recycle();
		view.setBackgroundResource(R.drawable.wind_icon_coloured);
		parentLayout.findViewById(R.id.wind_frame)
				.setBackgroundResource(
						R.drawable.small_wind_icon_coloured);
		windFound = true;
		checkCombination();
		useItem();}
		break;
	}
}private void changeFireIcon(View view) {
	if(fireCount == 0){
		fireCount++;
		view.setBackgroundResource(R.drawable.fire_icon_faded);
		parentLayout.findViewById(R.id.fire_frame).setBackgroundResource(R.drawable.small_fire_icon_faded);
		playSound(semipaintedSoundID);
	}
	else if(fireCount == 1){
		view.setBackgroundResource(R.drawable.fire_icon_coloured);
		parentLayout.findViewById(R.id.fire_frame).setBackgroundResource(R.drawable.small_fire_icon_coloured);
		fireFound = true;
		checkCombination();
		playSound(fullyPaintedSoundID);
	}	
}

private void changeEarthIcon(View view) {
	((BitmapDrawable) view.getBackground()).getBitmap().recycle();
	((BitmapDrawable) parentLayout.findViewById(R.id.earth_frame)
			.getBackground()).getBitmap().recycle();
	if (earthCount == 0) {
		playSound(semipaintedSoundID);
		view.setBackgroundResource(R.drawable.earth_icon_faded);
		parentLayout.findViewById(R.id.earth_frame)
				.setBackgroundResource(
						R.drawable.small_earth_icon_faded);
		earthCount++;
	} else if (earthCount == 1) {
		playSound(fullyPaintedSoundID);
		view.setBackgroundResource(R.drawable.earth_icon_coloured);
		parentLayout.findViewById(R.id.earth_frame)
				.setBackgroundResource(
						R.drawable.small_earth_icon_coloured);
		earthCount++;
		checkCombination();	
}
}
/*	public void changeIcon(View view) {
		switch (view.getId()) {
		case R.id.fire_icon:
			if (selectedItem == R.id.red_item
					|| selectedItem == R.id.yellow_item) {
				((BitmapDrawable) view.getBackground()).getBitmap().recycle();
				((BitmapDrawable) parentLayout.findViewById(R.id.fire_frame)
						.getBackground()).getBitmap().recycle();
				if (fireCount == 0) {
					view.setBackgroundResource(R.drawable.fire_icon_faded);
					// view.setBackgroundDrawable(storage.getDrawable(this,
					// "fire_icon_faded"));
					parentLayout.findViewById(R.id.fire_frame)
							.setBackgroundResource(
									R.drawable.small_fire_icon_faded);
					// parentLayout.findViewById(R.id.fire_frame).setBackgroundDrawable(storage.getDrawable(this,
					// "small_fire_icon_faded"));
					fireCount++;
					playSound(semipaintedSoundID);
				} else if (fireCount == 1) {
					view.setBackgroundResource(R.drawable.fire_icon_coloured);
					// view.setBackgroundDrawable(storage.getDrawable(this,
					// "fire_icon_coloured"));
					parentLayout.findViewById(R.id.fire_frame)
							.setBackgroundResource(
									R.drawable.small_fire_icon_coloured);
					// parentLayout.findViewById(R.id.fire_frame).setBackgroundDrawable(storage.getDrawable(this,
					// "small_fire_icon_coloured"));
					fireFound = true;
					fireCount++;
					checkCombination();
					playSound(fullyPaintedSoundID);
				}
				if (selectedItem == R.id.red_item) {
					red_faded = true;
					useItem();
				} else if (selectedItem == R.id.yellow_item) {
					if (yellowFound == 1) {
						useItem();
					} else {
						yellowFound++;
					}
				}
			}
			break;
		case R.id.water_icon:
			if (selectedItem == R.id.blue_item) {
				playSound(fullyPaintedSoundID);
				((BitmapDrawable) view.getBackground()).getBitmap().recycle();
				((BitmapDrawable) parentLayout.findViewById(R.id.water_frame)
						.getBackground()).getBitmap().recycle();
				view.setBackgroundResource(R.drawable.water_icon_coloured);
				// view.setBackgroundDrawable(storage.getDrawable(this,
				// "water_icon_coloured"));

				parentLayout.findViewById(R.id.water_frame)
						.setBackgroundResource(
								R.drawable.small_water_icon_coloured);
				// parentLayout.findViewById(R.id.water_frame).setBackgroundDrawable(storage.getDrawable(this,
				// "small_water_icon_coloured"));

				waterFound = true;
				checkCombination();
				if (blueFound == 1) {
					useItem();
				} else {
					blueFound++;
				}
			}
			break;
		case R.id.earth_icon:
			if (selectedItem == R.id.blue_item
					|| selectedItem == R.id.yellow_item) {
				playSound(semipaintedSoundID);
				((BitmapDrawable) view.getBackground()).getBitmap().recycle();
				((BitmapDrawable) parentLayout.findViewById(R.id.earth_frame)
						.getBackground()).getBitmap().recycle();
				if (earthCount == 0) {
					view.setBackgroundResource(R.drawable.earth_icon_faded);
					// view.setBackgroundDrawable(storage.getDrawable(this,
					// "earth_icon_faded"));
					parentLayout.findViewById(R.id.earth_frame)
							.setBackgroundResource(
									R.drawable.small_earth_icon_faded);
					// parentLayout.findViewById(R.id.earth_frame).setBackgroundDrawable(storage.getDrawable(this,
					// "small_earth_icon_faded"));
					earthCount++;
				} else if (earthCount == 1) {
					playSound(fullyPaintedSoundID);
					view.setBackgroundResource(R.drawable.earth_icon_coloured);
					// view.setBackgroundDrawable(storage.getDrawable(this,
					// "earth_icon_coloured"));
					parentLayout.findViewById(R.id.earth_frame)
							.setBackgroundResource(
									R.drawable.small_earth_icon_coloured);
					// parentLayout.findViewById(R.id.earth_frame).setBackgroundDrawable(storage.getDrawable(this,
					// "small_earth_icon_coloured"));
					earthFound = true;
					earthCount++;
					checkCombination();
				}
				if (selectedItem == R.id.blue_item) {
					if (blueFound == 1) {
						useItem();
					} else {
						blueFound++;
					}
				} else if (selectedItem == R.id.yellow_item) {
					if (yellowFound == 1) {
						useItem();
					} else {
						yellowFound++;
					}
				}
			}
			break;
		case R.id.wind_icon:
			if (selectedItem == R.id.white_item) {
				playSound(fullyPaintedSoundID);
				((BitmapDrawable) view.getBackground()).getBitmap().recycle();
				((BitmapDrawable) parentLayout.findViewById(R.id.wind_frame)
						.getBackground()).getBitmap().recycle();
				view.setBackgroundResource(R.drawable.wind_icon_coloured);
				// view.setBackgroundDrawable(storage.getDrawable(this,
				// "wind_icon_coloured"));
				parentLayout.findViewById(R.id.wind_frame)
						.setBackgroundResource(
								R.drawable.small_wind_icon_coloured);
				// parentLayout.findViewById(R.id.wind_frame).setBackgroundDrawable(storage.getDrawable(this,
				// "small_wind_icon_coloured"));
				windFound = true;
				checkCombination();
				white_faded = true;
				useItem();
			}
			break;
		}
	}*/

	private void checkCombination() {
		if (earthFound && waterFound & windFound && fireFound) {
			// final ImageButton water_room =
			// (ImageButton)parentLayout.findViewById(R.id.water_room);
			bagClickCount = 2;
			bagClicked(null);
			if(adViewFromXml!=null){
			adViewFromXml.removeAllViews();}
			adViewFromXml = null;
			if(this.cb!=null){
			this.cb.onDestroy(this);
			this.cb.clearCache();
			this.cb = null;
			cb = null;}
			hideZoomedFrames(null);
			tview.setText(R.string.four_complete);
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					tview.setText(null);
					ImageButton fireplace = (ImageButton)findViewById(R.id.fireplace);
					((BitmapDrawable) fireplace.getBackground()).getBitmap()
							.recycle();
					// fireplace.setBackgroundResource(R.drawable.view_water_room);
					fireplace.setBackgroundDrawable(storage.getDrawable(
							FireRoomActivity.this, "view_water_room"));
					fireplace.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							finishFireRoom(null);
						}

					});
					// parentLayout.findViewById(R.id.fireplace).setVisibility(View.GONE);
					// parentLayout.removeView(findViewById(R.id.fireplace));
					// water_room.setVisibility(View.VISIBLE);
					playSound(fountainSoundID);
				}
			}, 3000);
		}
	}
public void cleanUp(){
	if (backgroundAudio != null) {
		backgroundAudio.stop();
		backgroundAudio.release();
		backgroundAudio = null;
		am.abandonAudioFocus(this);
		am = null;
	}

	tview = null;
	unbindDrawables(findViewById(R.id.fire_room_layout));
}
	public void finishFireRoom(View view) {
		IntentHelper.addObjectForKey(mSound, SOUND);
		IntentHelper.addObjectForKey(mAds, ADS);
		
		Intent intent = new Intent(this, WaterRoomActivity.class);
		intent.putExtra("ACTIVITY_NAME", "Fire");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		cleanUp();
		System.gc();
		finish();
		startActivity(intent);	
	}

	private boolean drawerClosed = false;

	public void openKeyDrawer(View view) {
		// ((BitmapDrawable)parentLayout.getBackground()).getBitmap().recycle();
		playSound(drawerSoundID);
		if (drawerClosed == false) {
			// parentLayout.setBackgroundResource(R.drawable.wall3_inverted_drawers);
			parentLayout.setBackgroundDrawable(storage.getDrawable(this,
					"wall3_inverted_drawers"));
			parentLayout.findViewById(R.id.drawer_two).setVisibility(
					View.VISIBLE);
			drawerClosed = true;
		} else {
			if (paperballFound == false) {
				// parentLayout.setBackgroundResource(R.drawable.wall3_paperball);
				parentLayout.setBackgroundDrawable(storage.getDrawable(this,
						"wall3_paperball"));
			} else
				// parentLayout.setBackgroundResource(R.drawable.wall3_nopaperball);
				parentLayout.setBackgroundDrawable(storage.getDrawable(this,
						"wall3_nopaperball"));

			parentLayout.findViewById(R.id.drawer_two).setVisibility(
					View.INVISIBLE);
			drawerClosed = false;
		}
	}

	public void zoomDrawer(View view) {
		makeArrowsInvisible();
		parentLayout.findViewById(R.id.cabinet_zoomed).setVisibility(
				View.VISIBLE);
		Button key = (Button) parentLayout.findViewById(R.id.key);
		if (key != null) {
			playSound(fullyPaintedSoundID);
			key.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_GAIN:
			// resume playback
			if (backgroundAudio == null)
				initMediaPlayer();
			else if (!backgroundAudio.isPlaying())
				backgroundAudio.start();
			break;

		case AudioManager.AUDIOFOCUS_LOSS:
			// Lost focus for an unbounded amount of time: stop playback and
			// release media player
			if (backgroundAudio!= null)
				{backgroundAudio.stop();
			backgroundAudio.release();
			backgroundAudio = null;}
			break;

		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			// Lost focus for a short time, but we have to stop
			// playback. We don't release the media player because playback
			// is likely to resume
			if (backgroundAudio.isPlaying())
				backgroundAudio.pause();
			break;

		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
			// Lost focus for a short time, but it's ok to keep playing
			// at an attenuated level
			if (backgroundAudio.isPlaying())
				backgroundAudio.pause();
			break;
		}
	}

	private void makeArrowsInvisible() {
		findViewById(R.id.left).setVisibility(View.INVISIBLE);
		findViewById(R.id.right).setVisibility(View.INVISIBLE);
	}

	private void makeArrowsVisible() {
		findViewById(R.id.left).setVisibility(View.VISIBLE);
		findViewById(R.id.right).setVisibility(View.VISIBLE);
	}

	@Override
	public void onAnimationEnd(Animation arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationRepeat(Animation arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationStart(Animation arg0) {
		// TODO Auto-generated method stub

	}

	class GestureListener extends GestureDetector.SimpleOnGestureListener {
		RelativeLayout parentLayout;

		public void setLayout(RelativeLayout layout) {
			parentLayout = layout;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		// event when double tap occurs
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			makeArrowsVisible();
			parentLayout.findViewById(R.id.cabinet_zoomed).setVisibility(
					View.INVISIBLE);
			Button key = (Button) parentLayout.findViewById(R.id.key);
			if (key != null) {
				key.setVisibility(View.INVISIBLE);
			}
			return true;
		}
	}
/*	public void closeCabinet(View view){
		makeArrowsVisible();
		parentLayout.findViewById(R.id.cabinet_zoomed).setVisibility(
				View.INVISIBLE);
		Button key = (Button) parentLayout.findViewById(R.id.key);
		if (key != null) {
			key.setVisibility(View.INVISIBLE);
		}
	}*/
	static final String ONBACK = "onback";

	@Override
	public void onBackPressed() {
		if (this.cb!=null && this.cb.onBackPressed())
			// If a Chartboost view exists, close it and return
			return;
		else {
			IntentHelper.addObjectForKey("onBack", ONBACK);

		Intent inMain = new Intent(this, MenuActivity.class);
		inMain.putExtra("activity", "FireRoom");
		startActivityForResult(inMain, 0);
		mHandler.removeCallbacks(null);}
	}

	final public int RESULT_CLOSE_ALL = 1;
	final public int NEW_GAME = 2;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case NEW_GAME:
			///setResult(RESULT_CLOSE_ALL);
			/*Intent intent = new Intent(this, FireRoomActivity.class);
			intent.putExtra("ACTIVITY_NAME", "Fire");
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);*/
			cleanUp();
			finish();
			//startActivity(intent);	

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
			Log.i(TAG, "SHOULD DISPLAY INTERSTITIAL '" + location + "'?");
			return true;
		}

		@Override
		public boolean shouldRequestInterstitial(String location) {
			Log.i(TAG, "SHOULD REQUEST INSTERSTITIAL '" + location + "'?");
			return true;
		}

		@Override
		public void didCacheInterstitial(String location) {
			Log.i(TAG, "INTERSTITIAL '" + location + "' CACHED");
		}

		@Override
		public void didFailToLoadInterstitial(String location) {
			Log.i(TAG, "INTERSTITIAL '" + location + "' REQUEST FAILED");

		}

		@Override
		public void didDismissInterstitial(String location) {
			cb.cacheInterstitial(location);
			Log.i(TAG, "INTERSTITIAL '" + location + "' DISMISSED");

		}

		@Override
		public void didCloseInterstitial(String location) {
			Log.i(TAG, "INSTERSTITIAL '" + location + "' CLOSED");
		}

		@Override
		public void didClickInterstitial(String location) {
			Log.i(TAG, "DID CLICK INTERSTITIAL '" + location + "'");
		}

		@Override
		public void didShowInterstitial(String location) {
			Log.i(TAG, "INTERSTITIAL '" + location + "' SHOWN");
		}

		@Override
		public void didFailToLoadUrl(String url) {

			Log.i(TAG, "URL '" + url + "' REQUEST FAILED");
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
	public void removeLoading() {
		if(mAds){
		setMMediaAd();}
		findViewById(R.id.transition).setVisibility(View.INVISIBLE);
		findViewById(R.id.fire_room_layout).setVisibility(View.VISIBLE);
	}
	public void createProgressBar(){
		((BitmapDrawable)findViewById(R.id.enter_layout).getBackground()).getBitmap().recycle();
		findViewById(R.id.progress).setVisibility(View.VISIBLE);
	}
	
	private class LoadTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			System.gc();
			((BitmapDrawable)findViewById(R.id.enter_layout).getBackground()).getBitmap().recycle();	
			findViewById(R.id.enter_layout).setBackgroundResource(0);
			//createProgressBar();
		}

		@Override
		protected Void doInBackground(Void... params) {
            //showDialog(0);
			temp();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			loadImages();
			RelativeLayout wall3 = getParentLayout(3);
			gListener.setLayout(wall3);
			wall3.findViewById(R.id.cabinet_zoomed).setOnTouchListener(
					new OnTouchListener() {
						@Override
						public boolean onTouch(View v, MotionEvent event) {
							return gestureDetector.onTouchEvent(event);
						}
					});
			removeLoading();
		}

	}

	
}
