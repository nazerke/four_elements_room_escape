package com.fourelements.roomescape;

import java.io.IOException;

import android.media.AudioManager;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics; 
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ChartboostDelegate;
import com.google.android.gms.ads.AdView;
import com.millennialmedia.android.MMAdView;
import com.millennialmedia.android.MMRequest;
import com.millennialmedia.android.MMSDK;

public class WaterRoomActivity extends Activity implements OnClickListener, AudioManager.OnAudioFocusChangeListener,OnTouchListener {
	int wall;
	ViewFlipper vf;
	RelativeLayout parentLayout;
	CustomView rl;
	TextView tview;
	private SoundPool sounds;
	private int sBook, sCassette, sMagicMirror, sSafeAppears, sSlidingDoor,
			sChestOpen, sBottle;
	boolean loaded;
	private MediaPlayer mermaid_song;
	MediaPlayer backgroundAudio;
	AudioManager am;
	ImageView zoomedImage;
	int selectedItem = -1;
	GestureDetector gestureDetector;
	GestureListener gListener;
	Resources res;
	int result;
	private static final String TAG = "Chartboost";
	MediaPlayer bottle;
	private MMAdView adViewFromXml;
	private int length = 0;

	private static final String TEST_DEVICE_ID = "CDE5C0CB2CDE51D7E16281E7CCDF48E6";
	private static final String MY_AD_UNIT_ID = "ca-app-pub-7246305362398013/5293549281";
	// "204492A704EAD4D9E21776F93804C016"; s3 mom
	// "4608458DD1D2D3F99AC92EA614FC0D8D";
	private Chartboost cb;
	ImageStorage storage;
	String appId = "52b0329b2d42da5580551e71";
	String appSignature = "202885a0818e2c1bff1552af968352b245e2a48e";
	public static final String PREFS_NAME = "Settings";
	public static final String ACTIVITY = "activity";
    private boolean mermaid_song_playing = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);

		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(ACTIVITY, "WaterRoom");
		editor.commit();
		setContentView(R.layout.water_room_view_flipper);

		gListener = new GestureListener();
		gestureDetector = new GestureDetector(WaterRoomActivity.this, gListener);
	}

	public void nextLevel(View view) {
		new LoadTask().execute();
		view.setVisibility(View.GONE);
	}

	static final String SOUND = "sound";
	static final String ADS = "ads";
	static final String ONBACK = "onback";
	boolean mSound, mAds;

	@Override
	protected void onStart() {
		super.onStart();
		mSound = (Boolean) IntentHelper.getObjectForKey(SOUND);
		mAds = (Boolean) IntentHelper.getObjectForKey(ADS);

		if (mAds) {
			if (this.cb == null) {
				this.cb = Chartboost.sharedChartboost();
				this.cb.onCreate(this, appId, appSignature,
						this.chartBoostDelegate);
			}
			this.cb.onStart(this);
			this.cb.startSession();
			this.cb.showInterstitial();
		} else {
			if (adViewFromXml != null) {
				adViewFromXml.removeAllViews();
			}
		}
		System.gc();
		if (mSound) {
			am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			result = am.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
					AudioManager.AUDIOFOCUS_GAIN);
			if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
				initMediaPlayer();
				backgroundAudio.start();
			}
		}
		mermaid_song = MediaPlayer.create(WaterRoomActivity.this,
				R.raw.mermaid_song);
		if(mermaid_song_playing){
			mermaid_song_playing = false;
			mermaid_song.seekTo(length);
			mermaid_song.start();
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
	}

	public void temp() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		storage = new ImageStorage(getResources(), metrics);
		// loadImages();
		res = this.getResources();
		wall = 1;
		parentLayout = getParentLayout(wall);
		rl = (CustomView) findViewById(R.id.customView);
		tview = (TextView) rl.findViewById(R.id.closeup_text);
		vf = (ViewFlipper) findViewById(R.id.water_room_flipper);

		getParentLayout(3).findViewById(R.id.second_layout).setOnTouchListener(
				this);
		getParentLayout(2).findViewById(R.id.safe_closeup_layout)
				.setOnTouchListener(this);

		AssetFileDescriptor descriptor = null;
		try {
			descriptor = getFileDescriptor(this,
					"expansion/audio_files/filling_bottle.ogg");
			bottle = new MediaPlayer();
			bottle.setDataSource(descriptor.getFileDescriptor(),
					descriptor.getStartOffset(), descriptor.getLength());
			bottle.prepare();
		} catch (Exception e) {
		} finally {
			if (descriptor != null)
				try {
					descriptor.close();
				} catch (IOException e) {
				}
		}
	}

	private void loadImages() {
		getParentLayout(1).setBackgroundDrawable(
				storage.getDrawable(this, "wall1withkey"));

		getParentLayout(2).setBackgroundDrawable(
				storage.getDrawable(this, "wall2"));

		getParentLayout(3).setBackgroundDrawable(
				storage.getDrawable(this, "wall3"));
		findViewById(R.id.album_zoomed).setBackgroundDrawable(
				storage.getDrawable(this, "strange_photograph"));

		getParentLayout(4).setBackgroundDrawable(
				storage.getDrawable(this, "wall4"));
	/*	findViewById(R.id.colour_code).setBackgroundDrawable(
				storage.getDrawable(this, "colour_code"));*/
	}

	/*
	 * private void loadSounds() { sMagicMirror = sounds.load(this,
	 * R.raw.magic_mirror, 1); sBook = sounds.load(this, R.raw.book, 1); sBottle
	 * = sounds.load(this, R.raw.bottle_from_safe, 1); sChestOpen =
	 * sounds.load(this, R.raw.chest_open, 1); sSlidingDoor = sounds.load(this,
	 * R.raw.wood_sliding_door, 1); sSafeAppears = sounds.load(this,
	 * R.raw.safe_appears, 1); }
	 */

	private void loadSounds() throws IOException {
		String folder = "expansion/audio_files/";

		AssetFileDescriptor mirror_fd = getFileDescriptor(this, folder
				+ "magic_mirror.ogg");
		sMagicMirror = sounds.load(mirror_fd.getFileDescriptor(),
				mirror_fd.getStartOffset(), mirror_fd.getLength(), 1);
		if (mirror_fd != null)
			mirror_fd.close();

		AssetFileDescriptor bottle_fd = getFileDescriptor(this, folder
				+ "bottle_from_safe.ogg");
		sBottle = sounds.load(bottle_fd.getFileDescriptor(),
				bottle_fd.getStartOffset(), bottle_fd.getLength(), 1);
		if (bottle_fd != null)
			bottle_fd.close();

		AssetFileDescriptor chestOpen_fd = getFileDescriptor(this, folder
				+ "chest_open.ogg");
		sChestOpen = sounds.load(chestOpen_fd.getFileDescriptor(),
				chestOpen_fd.getStartOffset(), chestOpen_fd.getLength(), 1);
		if (chestOpen_fd != null)
			chestOpen_fd.close();

		AssetFileDescriptor sliding_fd = getFileDescriptor(this, folder
				+ "wood_sliding_door.ogg");
		sSlidingDoor = sounds.load(sliding_fd.getFileDescriptor(),
				sliding_fd.getStartOffset(), sliding_fd.getLength(), 1);
		if (sliding_fd != null)
			sliding_fd.close();

		AssetFileDescriptor safe_fd = getFileDescriptor(this, folder
				+ "safe_appears.ogg");
		sSafeAppears = sounds.load(safe_fd.getFileDescriptor(),
				safe_fd.getStartOffset(), safe_fd.getLength(), 1);
		if (safe_fd != null)
			safe_fd.close();

		AssetFileDescriptor book_fd = getFileDescriptor(this, folder
				+ "book.ogg");
		sBook = sounds.load(book_fd.getFileDescriptor(),
				book_fd.getStartOffset(), book_fd.getLength(), 1);
		if (book_fd != null)
			book_fd.close();
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

	int streamID;

	private void playSound(int soundID) {
		if (loaded) {
			streamID = sounds.play(soundID, 1, 1, 1, 0, 1);
		}
	}

	AdView adView;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.activity_water_room, menu);
		return true;
	}

	private static final int NUM_OF_WALLS = 4;

	public void nextWall(View view) {
		bagClickCount = 0;
		bagClicked(null);
		if (wall == 1) {
			vf.showNext();
			wall = 2;
			parentLayout = getParentLayout(wall);
		} else if (wall == 2) {
			vf.showNext();
			wall = 3;
			parentLayout = getParentLayout(wall);
		} else if (wall == 3) {
			vf.showNext();
			wall = 4;
			parentLayout = getParentLayout(wall);
		} else if (wall == 4) {
			vf.showNext();
			wall = 1;
			parentLayout = getParentLayout(wall);
		}
	}

	public void previousWall(View v) {
		bagClickCount = 0;
		bagClicked(null);
		if (wall == 1) {
			vf.showPrevious();
			wall = 4;
			parentLayout = getParentLayout(wall);
		} else if (wall == 2) {
			vf.showPrevious();
			wall = 1;
			parentLayout = getParentLayout(wall);
		} else if (wall == 3) {
			vf.showPrevious();

			wall = 2;
			parentLayout = getParentLayout(wall);
		} else if (wall == 4) {
			vf.showPrevious();
			wall = 3;
			parentLayout = getParentLayout(wall);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (sounds != null) {
			sounds.release();
			sounds = null;
		}
		if (backgroundAudio != null) {
			backgroundAudio.stop();
			backgroundAudio.release();
			backgroundAudio = null;
		}
		if (cb != null) {
			this.cb.onDestroy(this);
			this.cb = null;
			cb = null;
		}
		System.gc();
		super.onPause();
	}

	boolean removed = false;
	private long mLastZoomClickTime = 0;

	@SuppressLint("NewApi")
	public void zoomImage(View view) {
		if (SystemClock.elapsedRealtime() - mLastZoomClickTime < 1000){
            return;
        }
        mLastZoomClickTime = SystemClock.elapsedRealtime();
		bagClickCount = 0;
		bagClicked(null);
		if (!removed) {
			LinearLayout transition = (LinearLayout) findViewById(R.id.transition);
			((BitmapDrawable) transition.getBackground()).getBitmap().recycle();
			transition.setBackgroundResource(0);
			transition.setVisibility(View.GONE);
			removed = true;
		}
		if (view.getId() == R.id.key) {
			view.setVisibility(View.GONE);

			parentLayout.removeView(findViewById(R.id.key_layout));
			tview.setText(getString(R.string.water_room_key));
			zoomedImage = (ImageView) parentLayout
					.findViewById(R.id.zoomed_image);
			// zoomedImage.setBackgroundResource(R.drawable.key_zoomed);
			zoomedImage.setBackgroundDrawable(storage.getDrawable(this,
					"key_zoomed"));
			zoomedImage.setTag(R.id.key);
			((BitmapDrawable) parentLayout.getBackground()).getBitmap()
					.recycle();
			// parentLayout.setBackgroundResource(R.drawable.wall1withoutkey);
			view.setBackgroundDrawable(null);
			parentLayout.setBackgroundDrawable(storage.getDrawable(this,
					"wall1withoutkey"));
		}

		else if (view.getId() == R.id.cassette) {
			tview.setText(getString(R.string.cassette));
			zoomedImage = (ImageView) parentLayout
					.findViewById(R.id.zoomed_image);
			// zoomedImage.setBackgroundResource(R.drawable.cassette_zoomed);
			zoomedImage.setBackgroundDrawable(storage.getDrawable(this,
					"cassette_zoomed"));
			zoomedImage.setTag(R.id.cassette);
			((BitmapDrawable) parentLayout.getBackground()).getBitmap()
					.recycle();
			// parentLayout.setBackgroundResource(R.drawable.wall3nocassette);
			parentLayout.setBackgroundDrawable(storage.getDrawable(this,
					"wall3nocassette"));
			parentLayout.findViewById(R.id.recorder)
					.setVisibility(View.VISIBLE);
			view.setVisibility(View.INVISIBLE);
		} else if (view.getId() == R.id.mirrorHole) {
			playSound(sMagicMirror);
			tview.setText(getString(R.string.mirror));
			zoomedImage = (ImageView) parentLayout
					.findViewById(R.id.zoomed_image);
			// zoomedImage.setBackgroundResource(R.drawable.mirror_zoomed);
			zoomedImage.setBackgroundDrawable(storage.getDrawable(this,
					"mirror_zoomed"));
			zoomedImage.setTag(R.id.mirrorHole);
			zoomedImage.setVisibility(View.VISIBLE);
			((BitmapDrawable) parentLayout.getBackground()).getBitmap()
					.recycle();
			// parentLayout.setBackgroundResource(R.drawable.wall1nomirror);
			parentLayout.setBackgroundDrawable(null);
			parentLayout.setBackgroundDrawable(storage.getDrawable(this,
					"wall1nomirror"));
			view.setVisibility(View.INVISIBLE);
		} else if (view.getId() == R.id.stool) {

			tview.setText(getString(R.string.stool));
			zoomedImage = (ImageView) parentLayout
					.findViewById(R.id.zoomed_image);
			// zoomedImage.setBackgroundResource(R.drawable.stool_zoomed);
			zoomedImage.setBackgroundDrawable(storage.getDrawable(this,
					"stool_zoomed"));
			zoomedImage.setTag(R.id.stool);
			((BitmapDrawable) parentLayout.getBackground()).getBitmap()
					.recycle();
			// parentLayout.setBackgroundResource(R.drawable.wall4emptywardrobe);
			parentLayout.setBackgroundDrawable(null);
			parentLayout.setBackgroundDrawable(storage.getDrawable(this,
					"wall4emptywardrobe"));
			view.setVisibility(View.INVISIBLE);
		} else if (view.getId() == R.id.album) {
			zoomedImage = (ImageView) parentLayout
					.findViewById(R.id.album_zoomed);
			playSound(sBook);
			zoomedImage.setVisibility(View.VISIBLE);
			view.setVisibility(View.INVISIBLE);
			Button strange_photo = (Button) parentLayout
					.findViewById(R.id.strange_photo);
			if (!peeled) {
				tview.setText(getString(R.string.fountain_photo));
				strange_photo.setVisibility(View.VISIBLE);
			}
		} else if (view.getId() == R.id.bottle) {
			
			tview.setText(getString(R.string.bottle));
			zoomedImage = (ImageView) parentLayout
					.findViewById(R.id.zoomed_image);
			// zoomedImage.setBackgroundResource(R.drawable.bottle_zoomed);
			zoomedImage.setBackgroundDrawable(storage.getDrawable(this,
					"bottle_zoomed"));
			zoomedImage.setTag(R.id.bottle);
			((BitmapDrawable) parentLayout.getBackground()).getBitmap()
					.recycle();
			// parentLayout.setBackgroundResource(R.drawable.wall2safe_empty);
			parentLayout.setBackgroundDrawable(null);
			parentLayout.setBackgroundDrawable(storage.getDrawable(this,
					"wall2safe_empty"));
			playSound(sBottle);
			view.setVisibility(View.INVISIBLE);
		}
		if (view.getId() != R.id.album) {
			zoomedImage.setOnClickListener(this);
			makeArrowsInvisible();
		}
	}

	private RelativeLayout getParentLayout(int wall) {
		int id = wall == 1 ? R.id.first : wall == 2 ? R.id.second
				: wall == 3 ? R.id.third : R.id.fourth;
		return (RelativeLayout) findViewById(id);
	}

	public void openLock(View view) {
		if (selectedItem == R.id.key_item) {
			playSound(sChestOpen);
			((BitmapDrawable) parentLayout.getBackground()).getBitmap()
					.recycle();
			parentLayout.setBackgroundDrawable(null);
			parentLayout.setBackgroundDrawable(storage.getDrawable(this,
					"wall3safeopen"));
			parentLayout.findViewById(R.id.cassette)
					.setVisibility(View.VISIBLE);
			ImageButton key = (ImageButton) findViewById(selectedItem);
			((BitmapDrawable) key.getBackground()).getBitmap().recycle();
			key.setBackgroundResource(R.drawable.key_faded);
			findViewById(selectedItem).setClickable(false);
			selectedItem = -1;
			view.setVisibility(View.INVISIBLE);
		}
	}

	public void itemSelected(View view) {
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
			}

			selectedItem = view.getId();

			// select this image
			String selected_item = getResources().getResourceEntryName(
					view.getId());
			selected_item = selected_item.replace("item", "selected");

			int selected_background = getResources().getIdentifier(
					selected_item, "drawable", packageName);
			view.setBackgroundResource(selected_background);
		}
	}
	private long mLastClickTime = 0;

	@Override
	public void onClick(View view) {
		 if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
	            return;
	        }
	        mLastClickTime = SystemClock.elapsedRealtime();
		int tag = (Integer) view.getTag();
		if (tag == R.id.key) {
			ImageButton newItem = (ImageButton) rl
					.findViewById(R.id.ImageButton01);
			if (newItem != null) {
				newItem.setBackgroundResource(R.drawable.key_stored);
				newItem.setId(R.id.key_item);
				bagClickCount = 1;
				bagClicked(null);
				((BitmapDrawable) view.getBackground()).getBitmap().recycle();
				view.setBackgroundDrawable(null);
				view.setVisibility(View.GONE);
				tview.setText(null);
			}

		} else if (tag == R.id.cassette) {
			ImageButton newItem = (ImageButton) rl
					.findViewById(R.id.ImageButton02);
			if (newItem != null) {
				tview.setText("");

				newItem.setBackgroundResource(R.drawable.cassette_stored);
				newItem.setId(R.id.cassette_item);
				bagClickCount = 1;
				bagClicked(null);
				((BitmapDrawable) view.getBackground()).getBitmap().recycle();
				view.setBackgroundDrawable(null);
				view.setVisibility(View.GONE);
			}
		} else if (tag == R.id.mirrorHole) {

			ImageButton newItem = (ImageButton) rl
					.findViewById(R.id.ImageButton03);
			if (newItem != null) {

				tview.setText(null);
				newItem.setBackgroundResource(R.drawable.mirror_stored);
				newItem.setId(R.id.mirror_item);
				view.setVisibility(View.GONE);
				bagClickCount = 1;
				bagClicked(null);
				parentLayout.findViewById(R.id.key).setVisibility(View.VISIBLE);
				Button anchorTip = (Button) parentLayout.findViewById(R.id.key);

				OnClickListener placeMirrorOnAnchorTip = new OnClickListener() {
					@Override
					public void onClick(final View v) {
						if (selectedItem == R.id.mirror_item) {
							((BitmapDrawable) parentLayout.getBackground())
									.getBitmap().recycle();
							// parentLayout.setBackgroundResource(R.drawable.wall1mirroronanchor);
							parentLayout.setBackgroundDrawable(null);
							parentLayout.setBackgroundDrawable(storage
									.getDrawable(WaterRoomActivity.this,
											"wall1mirroronanchor"));
							parentLayout.findViewById(R.id.mirror_on_anchor)
									.setVisibility(View.VISIBLE);
							ImageButton mirror = (ImageButton) findViewById(selectedItem);
							((BitmapDrawable) mirror.getBackground())
									.getBitmap().recycle();
							mirror.setBackgroundResource(R.drawable.mirror_faded);
							findViewById(selectedItem).setClickable(false);
							selectedItem = -1;
						}
					}
				};
				anchorTip.setOnClickListener(placeMirrorOnAnchorTip);
				bagClickCount = 1;
				bagClicked(null);
			}
		} else if (tag == R.id.stool) {

			ImageButton newItem = (ImageButton) rl
					.findViewById(R.id.ImageButton04);
			if (newItem != null) {
				tview.setText(null);

				newItem.setBackgroundResource(R.drawable.stool_stored);
				newItem.setId(R.id.stool_item);
				bagClickCount = 1;
				bagClicked(null);
				view.setVisibility(View.GONE);
			}
		} else if (tag == R.id.bottle) {
			ImageButton newItem = (ImageButton) rl
					.findViewById(R.id.ImageButton06);
			if (newItem != null) {
				tview.setText(null);

				newItem.setBackgroundResource(R.drawable.bottle_stored);
				newItem.setId(R.id.bottle_item);
				bagClickCount = 1;
				bagClicked(null);
				view.setVisibility(View.GONE);
			}
		}
		makeArrowsVisible();
	}

	public void playCassette(View view) {
		if (selectedItem == R.id.cassette_item) {
			parentLayout = getParentLayout(1);
			((BitmapDrawable) parentLayout.getBackground()).getBitmap()
					.recycle();
			parentLayout.setBackgroundDrawable(null);
			parentLayout.setBackgroundDrawable(storage.getDrawable(this,
					"wall1mermaid"));
			// parentLayout.setBackgroundResource(R.drawable.wall1mermaid);
			parentLayout.findViewById(R.id.redx).setVisibility(View.VISIBLE);
			ImageButton cassette = (ImageButton) findViewById(selectedItem);
			((BitmapDrawable) cassette.getBackground()).getBitmap().recycle();
			cassette.setBackgroundResource(R.drawable.cassette_faded);
			findViewById(selectedItem).setClickable(false);
			selectedItem = -1;
			mermaid_song.start();
		}
	}

	public void showMirror(View view) {
		((BitmapDrawable) parentLayout.getBackground()).getBitmap().recycle();
		view.setClickable(false);
		parentLayout.removeView(findViewById(R.id.redx));
		// parentLayout.setBackgroundResource(R.drawable.wall1mirror);
		parentLayout.setBackgroundDrawable(null);
		parentLayout.setBackgroundDrawable(storage.getDrawable(this,
				"wall1mirror"));
		parentLayout.findViewById(R.id.mirrorHole).setVisibility(View.VISIBLE);
		mermaid_song.stop();
		mermaid_song.release();
		mermaid_song = null;
	}

	ImageView year_in_mirror;
	boolean firstTime = false;

	public void mirrorClicked(View view) {
		year_in_mirror = (ImageView) parentLayout
				.findViewById(R.id.year_in_mirror);
		//if (!firstTime) {
			// year_in_mirror.setBackgroundResource(R.drawable.mirror_year);
			year_in_mirror.setBackgroundDrawable(storage.getDrawable(this,
					"mirror_year"));
		//}
		year_in_mirror.setVisibility(View.VISIBLE);
		year_in_mirror.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((BitmapDrawable) year_in_mirror.getBackground()).getBitmap()
				.recycle();
				year_in_mirror.setVisibility(View.GONE);
			}

		});
	}

	public void placeStool(View v) {
		if (selectedItem == R.id.stool_item) {
			parentLayout.removeView(v);
			parentLayout.findViewById(R.id.stool_top).setVisibility(
					View.VISIBLE);
			ImageButton stool = (ImageButton) findViewById(selectedItem);
			((BitmapDrawable) stool.getBackground()).getBitmap().recycle();
			stool.setBackgroundResource(R.drawable.stool_faded);
			findViewById(selectedItem).setClickable(false);
			selectedItem = -1;
		}
	}

	boolean safeFirstTime = true;

	public void zoomSafe(View v) {
		makeArrowsInvisible();
		if (safeFirstTime) {
			// parentLayout.findViewById(R.id.safe_closeup_layout).setBackgroundResource(R.drawable.zoomed_safe);
			parentLayout.findViewById(R.id.safe_closeup_layout)
					.setBackgroundDrawable(
							storage.getDrawable(this, "zoomed_safe"));
			safeFirstTime = false;
		}
		parentLayout.findViewById(R.id.safe_closeup_layout).setVisibility(
				View.VISIBLE);
		tv = (TextView) parentLayout.findViewById(R.id.passwordField);
		tv.setVisibility(View.VISIBLE);
		Typeface tf = Typeface.createFromAsset(getAssets(),
				"fonts/ds_digital/DS-DIGIB.TTF");
		tv.setTypeface(tf);
		parentLayout.findViewById(R.id.safe_closeup_layout).setVisibility(
				View.VISIBLE);
		//v.setClickable(false);
	}

	public void zoomWardrobe(View v) {
		findViewById(R.id.colour_code).setBackgroundDrawable(
				storage.getDrawable(this, "colour_code"));
		parentLayout.findViewById(R.id.colour_code).setVisibility(View.VISIBLE);
		makeArrowsInvisible();
	}

	private int code_one_click = 1;
	private int code_two_click = 1;
	private int code_three_click = 1;
	private int code_four_click = 1;

	public void colorCodeOneClicked(View v) {
		ImageView firstSquare = (ImageView) parentLayout
				.findViewById(R.id.white);
		if (code_one_click == 1) {
			firstSquare.setBackgroundResource(R.drawable.yellow);
			firstSquare.setTag(R.drawable.yellow);
			code_one_click++;
		} else if (code_one_click == 2) {
			firstSquare.setBackgroundResource(R.drawable.orange);
			code_one_click++;
			checkIfCorrectCode();
		} else if (code_one_click == 3) {
			firstSquare.setBackgroundResource(R.drawable.white);
			firstSquare.setTag(R.drawable.white);
			code_one_click = 1;
		}
	}

	private void checkIfCorrectCode() {

		if (code_one_click == 3 && code_two_click == 4 && code_three_click == 4
				&& code_four_click == 4) {
			((BitmapDrawable) parentLayout.getBackground()).getBitmap()
					.recycle();
			playSound(sSlidingDoor);
			// parentLayout.setBackgroundResource(R.drawable.wall4wardrobe_open);
			parentLayout.setBackgroundDrawable(null);
			parentLayout.setBackgroundDrawable(storage.getDrawable(this,
					"wall4wardrobe_open"));
			/*((BitmapDrawable) parentLayout.findViewById(R.id.colour_code)
					.getBackground()).getBitmap().recycle();
			parentLayout.findViewById(R.id.colour_code).setBackgroundDrawable(
					null);*/
			parentLayout.findViewById(R.id.wardrobe).setVisibility(View.GONE);
			((BitmapDrawable) parentLayout.findViewById(R.id.red)
					.getBackground()).getBitmap().recycle();
			((BitmapDrawable) parentLayout.findViewById(R.id.white)
					.getBackground()).getBitmap().recycle();
			((BitmapDrawable) parentLayout.findViewById(R.id.dark_blue)
					.getBackground()).getBitmap().recycle();
			((BitmapDrawable) parentLayout.findViewById(R.id.purple)
					.getBackground()).getBitmap().recycle();
			makeColourCodeInvisible(null);
			makeArrowsVisible();
			parentLayout.removeView(findViewById(R.id.colour_code));
			((Button) parentLayout.findViewById(R.id.stool))
					.setVisibility(View.VISIBLE);
		}

	}

	public void colorCodeTwoClicked(View v) {
		ImageView secondSquare = (ImageView) parentLayout
				.findViewById(R.id.red);

		if (code_two_click == 1) {
			secondSquare.setBackgroundResource(R.drawable.neon_green);
			secondSquare.setTag(R.drawable.neon_green);
			code_two_click++;
		} else if (code_two_click == 2) {
			secondSquare.setBackgroundResource(R.drawable.grey);
			secondSquare.setTag(R.drawable.grey);
			code_two_click++;
		} else if (code_two_click == 3) {
			secondSquare.setBackgroundResource(R.drawable.blue);
			secondSquare.setTag(R.drawable.blue);
			code_two_click++;
			checkIfCorrectCode();

		} else if (code_two_click == 4) {
			secondSquare.setBackgroundResource(R.drawable.red_square);
			secondSquare.setTag(R.drawable.red_square);
			code_two_click = 1;
		}

	}

	public void colorCodeThreeClicked(View v) {
		if (code_three_click == 1) {
			parentLayout.findViewById(R.id.dark_blue).setBackgroundResource(
					R.drawable.pink);
			code_three_click++;
		} else if (code_three_click == 2) {
			parentLayout.findViewById(R.id.dark_blue).setBackgroundResource(
					R.drawable.neon_green);
			code_three_click++;
		} else if (code_three_click == 3) {
			parentLayout.findViewById(R.id.dark_blue).setBackgroundResource(
					R.drawable.earth_green);
			code_three_click++;
			checkIfCorrectCode();

		} else if (code_three_click == 4) {
			parentLayout.findViewById(R.id.dark_blue).setBackgroundResource(
					R.drawable.pink);
			code_three_click = 1;
		}
	}

	public void colorCodeFourClicked(View v) {
		if (code_four_click == 1) {
			parentLayout.findViewById(R.id.purple).setBackgroundResource(
					R.drawable.brown);
			code_four_click++;
		} else if (code_four_click == 2) {
			parentLayout.findViewById(R.id.purple).setBackgroundResource(
					R.drawable.dark_blue);
			code_four_click++;
		}

		else if (code_four_click == 3) {
			parentLayout.findViewById(R.id.purple).setBackgroundResource(
					R.drawable.white);
			code_four_click++;
			checkIfCorrectCode();
		} else if (code_four_click == 4) {
			parentLayout.findViewById(R.id.purple).setBackgroundResource(
					R.drawable.purple);
			code_four_click = 1;
		}
	}

	TextView tv;

	public void typeOne(View v) {
		tv.append("1   ");
		String password = tv.getText().toString().replaceAll(" ", "");
		if (password.length() == 4)
			tv.setText("");
	}

	public void typeTwo(View v) {
		tv.append("2   ");
		String password = tv.getText().toString().replaceAll(" ", "");
		if (password.length() == 4)
			tv.setText("");
	}

	public void typeThree(View v) {
		tv.append("3   ");
		String password = tv.getText().toString().replaceAll(" ", "");
		if (password.length() == 4)
			tv.setText("");
	}

	public void typeFour(View v) {
		tv.append("4   ");
		String password = tv.getText().toString().replaceAll(" ", "");
		if (password.length() == 4)
			tv.setText("");
	}

	public void typeFive(View v) {
		tv.append("5   ");
		String password = tv.getText().toString().replaceAll(" ", "");
		if (password.length() == 4)
			tv.setText("");
	}

	public void typeSix(View v) {
		tv.append("6");
		String password = tv.getText().toString().replaceAll(" ", "");
		if (password.length() == 4) {
			if (password.equals("1856")) {
				openTheSafe();
			} else
				tv.setText("");
		}
	}

	private void openTheSafe() {
	/*	if (year_in_mirror != null) {
			((BitmapDrawable) year_in_mirror.getBackground()).getBitmap()
					.recycle();
			year_in_mirror = null;
		}*/
		tv = null;
		((BitmapDrawable) findViewById(R.id.safe_closeup_layout)
				.getBackground()).getBitmap().recycle();
		parentLayout.removeView(findViewById(R.id.safe_closeup_layout));
		parentLayout.findViewById(R.id.safe).setVisibility(View.GONE);
		((BitmapDrawable) parentLayout.getBackground()).getBitmap().recycle();
		// parentLayout.setBackgroundResource(R.drawable.wall2safe_open);
		parentLayout.setBackgroundDrawable(null);
		parentLayout.setBackgroundDrawable(storage.getDrawable(this,
				"wall2safe_open"));
		findViewById(R.id.bottle).setVisibility(View.VISIBLE);
	}

	public void typeSeven(View v) {
		tv.append("7   ");
		String password = tv.getText().toString().replaceAll(" ", "");
		if (password.length() == 4)
			tv.setText("");
	}

	public void typeEight(View v) {
		tv.append("8   ");
		String password = tv.getText().toString().replaceAll(" ", "");
		if (password.length() == 4)
			tv.setText("");
	}

	public void typeNine(View v) {
		tv.append("9   ");
		String password = tv.getText().toString().replaceAll(" ", "");
		if (password.length() == 4)
			tv.setText("");
	}

	public void typeHashTag(View v) {
		tv.append("#   ");
		String password = tv.getText().toString().replaceAll(" ", "");
		if (password.length() == 4)
			tv.setText("");
	}

	public void typeZero(View v) {
		tv.append("0   ");
		String password = tv.getText().toString().replaceAll(" ", "");
		if (password.length() == 4)
			tv.setText("");
	}

	public void typeStar(View v) {
		tv.append("*   ");
		String password = tv.getText().toString().replaceAll(" ", "");
		if (password.length() == 4)
			tv.setText("");
	}

	boolean zoomShelfFirstTime = false;

	public void climbStool(View v) {
		//if (!zoomShelfFirstTime) {
			// parentLayout.findViewById(R.id.second_layout).setBackgroundResource(R.drawable.shelf_zoomed);
			parentLayout.findViewById(R.id.second_layout)
					.setBackgroundDrawable(
							storage.getDrawable(this, "shelf_zoomed"));
			//zoomShelfFirstTime = true;
		//}

		findViewById(R.id.second_layout).setVisibility(View.VISIBLE);
		makeArrowsInvisible();
	}

	boolean peeled = false;

	public void peelOffPhoto(View view) {
		peeled = true;
		ImageView album_zoomed = (ImageView) parentLayout
				.findViewById(R.id.album_zoomed);
		((BitmapDrawable) album_zoomed.getBackground()).getBitmap().recycle();
		album_zoomed.setBackgroundDrawable(null);
		album_zoomed.setBackgroundDrawable(storage.getDrawable(this,
				"album_empty"));
		view.setClickable(false);
		parentLayout.removeView(view);
		ImageButton newItem = (ImageButton) rl.findViewById(R.id.ImageButton05);
		newItem.setBackgroundResource(R.drawable.photo_stored);
		newItem.setId(R.id.photo_item);
		bagClickCount = 1;
		bagClicked(null);
		tview.setText(null);
	}

	public void makeColourCodeInvisible(View v) {
		((BitmapDrawable) parentLayout.findViewById(R.id.colour_code)
				.getBackground()).getBitmap().recycle();
		parentLayout.findViewById(R.id.colour_code).setVisibility(
				View.INVISIBLE);
		makeArrowsVisible();
	}

	private void makeArrowsInvisible() {
		findViewById(R.id.left).setVisibility(View.INVISIBLE);
		findViewById(R.id.right).setVisibility(View.INVISIBLE);
	}

	private void makeArrowsVisible() {
		findViewById(R.id.left).setVisibility(View.VISIBLE);
		findViewById(R.id.right).setVisibility(View.VISIBLE);
	}

	public void frameClicked(View view) {
		if (selectedItem == R.id.photo_item) {
			playSound(sSafeAppears);
			ImageButton photo_frame = (ImageButton) parentLayout
					.findViewById(R.id.photo_frame);
			photo_frame.setBackgroundResource(R.drawable.fountain_photo);
			RelativeLayout secondLayout = getParentLayout(2);
			((BitmapDrawable) secondLayout.getBackground()).getBitmap()
					.recycle();
			secondLayout.setBackgroundDrawable(null);
			secondLayout.setBackgroundDrawable(storage.getDrawable(this,
					"wall2safe"));
			secondLayout.findViewById(R.id.safe).setVisibility(View.VISIBLE);
			ImageButton photo = (ImageButton) findViewById(selectedItem);
			((BitmapDrawable) photo.getBackground()).getBitmap().recycle();
			photo.setBackgroundResource(R.drawable.photo_faded);
			findViewById(selectedItem).setClickable(false);
			selectedItem = -1;
		}
	}

	public void fillBottle(View v) {
		if (this.cb != null) {
			this.cb.onDestroy(this);
			this.cb.clearCache();
			this.cb = null;
			cb = null;
		}
		if (selectedItem == R.id.bottle_item) {
			bottle.start();
			bottle.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer arg0) {
					findViewById(selectedItem).setBackgroundResource(
							R.drawable.bottle_full);
					findViewById(selectedItem).setId(R.id.bottle_full_item);
					findViewById(R.id.fishtank).setClickable(false);
					findViewById(R.id.flowerpot).setClickable(true);
					selectedItem = -1;
				}
			});

			/*
			 * MediaPlayer filling_bottle = MediaPlayer.create(
			 * WaterRoomActivity.this, R.raw.filling_bottle);
			 * filling_bottle.start();
			 * filling_bottle.setOnCompletionListener(new OnCompletionListener()
			 * {
			 * 
			 * @Override public void onCompletion(MediaPlayer arg0) {
			 * findViewById(selectedItem).setBackgroundResource(
			 * R.drawable.bottle_full);
			 * findViewById(R.id.fishtank).setClickable(false);
			 * findViewById(R.id.flowerpot).setClickable(true); } });
			 */
		}
	}

	public void waterFlower(View v) {
		if (selectedItem == R.id.bottle_full_item) {
			IntentHelper.addObjectForKey(mSound, SOUND);
			IntentHelper.addObjectForKey(mAds, ADS);
			ImageButton bottle = (ImageButton) findViewById(selectedItem);
			((BitmapDrawable) bottle.getBackground()).getBitmap().recycle();
			bottle.setBackgroundResource(R.drawable.bottle_full_faded);
			findViewById(selectedItem).setClickable(false);
			selectedItem = -1;
			Intent intent = new Intent(this, EarthRoomBeginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			cleanUp();
			System.gc();
			finish();
			startActivity(intent);
		}
	}

	public void cleanUp() {
		unbindDrawables(findViewById(R.id.water_room_layout));

		year_in_mirror = null;
		sounds = null;

		if (backgroundAudio != null) {
			backgroundAudio.stop();
			backgroundAudio.release();
			backgroundAudio = null;
			am.abandonAudioFocus(this);
			am = null;
		}
		if (mermaid_song != null && mermaid_song.isPlaying()) {
			mermaid_song.stop();
			mermaid_song.release();
			mermaid_song = null;
		}

		tview = null;
		if (adViewFromXml != null) {
			adViewFromXml.removeAllViews();
			adViewFromXml = null;
		}
	}

	public void closeTheAlbum(View v) {
		parentLayout.findViewById(R.id.album_zoomed).setVisibility(View.GONE);
		parentLayout.findViewById(R.id.album).setVisibility(View.VISIBLE);
		Button photo = (Button) parentLayout.findViewById(R.id.strange_photo);
		if (photo != null) {
			photo.setVisibility(View.GONE);
		}
		makeArrowsVisible();
	}

	int bagClickCount = 1;

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
			if (backgroundAudio != null) {
				if (backgroundAudio.isPlaying()) {
					backgroundAudio.stop();
					backgroundAudio.release();
					backgroundAudio = null;
				}
			}
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

	private void initMediaPlayer() {
		// backgroundAudio =
		// MediaPlayer.create(WaterRoomActivity.this,R.raw.ocean);
		AssetFileDescriptor descriptor = null;
		try {
			descriptor = getFileDescriptor(this,
					"expansion/audio_files/ocean.ogg");
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
		backgroundAudio.setLooping(true);
		backgroundAudio.setVolume(0.1f, 0.1f);

		mermaid_song = MediaPlayer.create(WaterRoomActivity.this,
				R.raw.mermaid_song);
	
		AssetFileDescriptor mermaid_fd = null;
		try {
			mermaid_fd = getFileDescriptor(this,
					"expansion/audio_files/mermaid_song.ogg");
			mermaid_song = new MediaPlayer();
			mermaid_song.setDataSource(mermaid_fd.getFileDescriptor(),
					mermaid_fd.getStartOffset(), mermaid_fd.getLength());
			mermaid_song.prepare();
		} catch (Exception e) {
		} finally {
			if (mermaid_fd != null)
				try {
					mermaid_fd.close();
				} catch (IOException e) {
				}
		}
		mermaid_song.setLooping(true);
		mermaid_song.setVolume(1.0f, 1.0f);
		
	}

	@Override
	protected void onStop() {
		super.onStop();
		IntentHelper.addObjectForKey(mSound, SOUND);
		IntentHelper.addObjectForKey(mAds, ADS);
		if (cb != null) {
			this.cb.onStop(this);
		}
		if(am!=null){
			am.abandonAudioFocus(this);
			if(backgroundAudio!=null){backgroundAudio.stop();
			backgroundAudio.release();
			backgroundAudio=null;}}
		
			if(mermaid_song!=null){
				
				if(mermaid_song.isPlaying()){
					mermaid_song.pause();
				mermaid_song_playing = true;

				length=mermaid_song.getCurrentPosition();}
				/*mermaid_song.stop();
			mermaid_song.release();
			mermaid_song=null;*/}
		// The activity is no longer visible (it is now "stopped")
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (cb != null) {
			this.cb.onDestroy(this);
			this.cb = null;
			cb = null;
		}
		if (adViewFromXml != null) {
			adViewFromXml.removeAllViews();
			adViewFromXml = null;
		}
		unbindDrawables(findViewById(R.id.water_room_flipper));
		System.gc();
	}

	static void unbindDrawables(View view) {
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

	class GestureListener extends GestureDetector.SimpleOnGestureListener {
		RelativeLayout parentLayout;
		View view;

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
			if (view.getId() == R.id.second_layout) {
				parentLayout.findViewById(R.id.second_layout).setVisibility(
						View.GONE);
			} else if (view.getId() == R.id.safe_closeup_layout) {
				LinearLayout safe = (LinearLayout) parentLayout
						.findViewById(R.id.safe_closeup_layout);
				safe.setVisibility(View.GONE);
			}
			return true;
		}

		public void setView(View view) {
			this.view = view;
		}
	}
public void zoomDownBookshelf(View view){
	makeArrowsVisible();
	((BitmapDrawable) findViewById(R.id.second_layout).getBackground())
	.getBitmap().recycle();
		parentLayout.findViewById(R.id.second_layout).setVisibility(View.GONE);
}
public void zoomDownSafe(View view){
		LinearLayout safe = (LinearLayout) parentLayout
				.findViewById(R.id.safe_closeup_layout);
		safe.setVisibility(View.GONE);
}
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		gListener.setView(view);
		if (view.getId() == R.id.second_layout)
			gListener.setLayout(getParentLayout(3));
		else if (view.getId() == R.id.safe_closeup_layout)
			gListener.setLayout(getParentLayout(2));
		return gestureDetector.onTouchEvent(event);
	}

	@Override
	public void onBackPressed() {
		if (this.cb != null && this.cb.onBackPressed())
			// If a Chartboost view exists, close it and return
			return;
		else {

			IntentHelper.addObjectForKey("onBack", ONBACK);
			Intent inMain = new Intent(this, MenuActivity.class);
			inMain.putExtra("activity", "WaterRoom");
			startActivityForResult(inMain, 0);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	final public int RESULT_CLOSE_ALL = 1;
	final public int NEW_GAME = 2;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case NEW_GAME:
			Intent intent = new Intent(this, FireRoomActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			cleanUp();
			System.gc();
			finish();
			startActivity(intent);
			//cleanUp();

		/*	Intent intent = new Intent(this, FireRoomActivity.class);
			intent.putExtra("ACTIVITY_NAME", "Fire");
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);*/
			//finish();
			//startActivity(intent);

		case RESULT_CLOSE_ALL:
			setResult(RESULT_CLOSE_ALL);
			finish();
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

	public void onLoadButtonClick(View view) {
		this.cb.showInterstitial();
		Log.i(TAG, "showInterstitial");
	}

	public void onMoreButtonClick(View view) {
		this.cb.showMoreApps();
		Log.i(TAG, "showMoreApps");
	}

	public void onPreloadClick(View v) {
		this.cb.cacheInterstitial();
		Log.i(TAG, "cacheInterstitial");
	}

	public void onPreloadMoreAppsClick(View v) {
		cb.cacheMoreApps();
	}

	public void onPreloadClearClick(View v) {
		cb.clearCache();
	}

	private void setMMediaAd() {
		adViewFromXml = (MMAdView) findViewById(R.id.adView);
		MMRequest request = new MMRequest();
		adViewFromXml.setMMRequest(request);
		adViewFromXml.setListener(new AdListener());
		adViewFromXml.getAd();
	}

	public void removeLoading() {
		if (mAds) {
			setMMediaAd();
		}

		findViewById(R.id.transition).setVisibility(View.INVISIBLE);
		 findViewById(R.id.water_room_layout).setVisibility(View.VISIBLE);
	}

	private class LoadTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			((BitmapDrawable) findViewById(R.id.enter_layout).getBackground())
					.getBitmap().recycle();
			findViewById(R.id.enter_layout).setBackgroundResource(0);
		}

		@Override
		protected Void doInBackground(Void... params) {
			temp();
			return null;
		}

		protected void onProgressUpdate(Integer... progress) {
			// setProgressPercent(progress[0]);
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			loadImages();
			removeLoading();
		}

	}

}