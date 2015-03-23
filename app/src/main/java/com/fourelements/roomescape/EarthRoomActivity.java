package com.fourelements.roomescape;

import java.io.IOException;


import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ChartboostDelegate;
import com.fourelements.roomescape.FireRoomActivity.GestureListener;
import com.millennialmedia.android.MMAdView;
import com.millennialmedia.android.MMRequest;
import com.millennialmedia.android.MMSDK;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.MediaPlayer.OnCompletionListener;
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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.LinearLayout;

import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class EarthRoomActivity extends Activity implements OnTouchListener, OnClickListener,AudioManager.OnAudioFocusChangeListener {
	int wall;
	ViewFlipper vf;
	TextView tview;
	RelativeLayout parentLayout;
	CustomView customView;
	int bagClickCount = 1;
	int selectedItem = -1;
	MediaPlayer backgroundAudio, end_credits;
	GestureListener gListener;
	GestureDetector gestureDetector;
	BitmapDrawable door_wall, door_wall_open;
	boolean king_rabbit_found = false;
	boolean queen_rabbit_found = false;
	AudioManager am;
	private SoundPool sounds;
	private MMAdView adViewFromXml;
	static final String SOUND = "sound";
	static final String ADS = "ads";
	boolean mSound, mAds;
	int chestSoundID, jokerSoundID, crownSoundID, sparkleSoundID, heartSoundID,
			doorSoundID, windSoundID, queenSoundID, kingSoundID;
	boolean loaded = false;
	private static final String TEST_DEVICE_ID = "CDE5C0CB2CDE51D7E16281E7CCDF48E6";
	ImageStorage storage;
	private Chartboost cb;
	private static final String TAG = "Chartboost";
	private Handler mHandler = new Handler();
	String appId = "52b0329b2d42da5580551e71";
	String appSignature = "202885a0818e2c1bff1552af968352b245e2a48e";
	int result;
	public static final String PREFS_NAME = "Settings";
	public static final String ACTIVITY = "activity";
	private long mLastClickTime = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);

		SharedPreferences.Editor editor = preferences.edit();
	    editor.putString(ACTIVITY, "EarthRoom");
	    editor.commit();
		setContentView(R.layout.activity_earth_room);
		findViewById(R.id.globe).setClickable(false);
	}

	private void loadImages() {
		getParentLayout(1).setBackgroundDrawable(storage.getDrawable(this, "globe_wall"));
		findViewById(R.id.castle_painting_layout).setBackgroundDrawable(storage.getDrawable(this, "castle_painting"));
		findViewById(R.id.frost_layout).setBackgroundDrawable(storage.getDrawable(this, "frost_painting"));
		findViewById(R.id.heart_painting_layout).setBackgroundDrawable(storage.getDrawable(this, "heart_painting"));
		findViewById(R.id.pumpkin_painting_zoomed).setBackgroundDrawable(storage.getDrawable(this, "pumpkin_painting"));
		findViewById(R.id.winter_painting_zoomed).setBackgroundDrawable(storage.getDrawable(this, "winter_painting"));
		findViewById(R.id.castle_painting_layout).setOnTouchListener(this);
		findViewById(R.id.frost_layout).setOnTouchListener(this);
		findViewById(R.id.heart_painting_layout).setOnTouchListener(this);
		findViewById(R.id.pumpkin_painting_zoomed).setOnTouchListener(this);
		findViewById(R.id.winter_painting_zoomed).setOnTouchListener(this);
		getParentLayout(2).setBackgroundDrawable(storage.getDrawable(this, "joker_wall"));
		getParentLayout(3).setBackgroundDrawable(storage.getDrawable(this, "mushroom_wall"));
		door_wall = storage.getDrawable(this, "door_wall");
		getParentLayout(4).setBackgroundDrawable(door_wall);
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
		/*
		 * crownSoundID = sounds.load(this, R.raw.princess_crown, 1);
		 * heartSoundID = sounds.load(this, R.raw.heart_beat, 1); sparkleSoundID
		 * = sounds.load(this, R.raw.magic_sparkle, 1); kingSoundID =
		 * sounds.load(this, R.raw.king_rabbit, 1); queenSoundID =
		 * sounds.load(this, R.raw.queen_rabbit, 1); jokerSoundID =
		 * sounds.load(this, R.raw.joker_laugh, 1); doorSoundID =
		 * sounds.load(this, R.raw.door_opening, 1); windSoundID =
		 * sounds.load(this, R.raw.strong_wind, 1);
		 */
		String folder = "expansion/audio_files/";

		AssetFileDescriptor crown_fd = getFileDescriptor(this, folder
				+ "princess_crown.ogg");
		crownSoundID = sounds.load(crown_fd.getFileDescriptor(),
				crown_fd.getStartOffset(), crown_fd.getLength(), 1);
		if (crown_fd != null)
			crown_fd.close();

		AssetFileDescriptor heart_fd = getFileDescriptor(this, folder
				+ "heart_beat.ogg");
		heartSoundID = sounds.load(heart_fd.getFileDescriptor(),
				heart_fd.getStartOffset(), heart_fd.getLength(), 1);
		if (heart_fd != null)
			heart_fd.close();

		AssetFileDescriptor sparkle_fd = getFileDescriptor(this, folder
				+ "magic_sparkle.ogg");
		sparkleSoundID = sounds.load(sparkle_fd.getFileDescriptor(),
				sparkle_fd.getStartOffset(), sparkle_fd.getLength(), 1);
		if (sparkle_fd != null)
			sparkle_fd.close();

		AssetFileDescriptor king_fd = getFileDescriptor(this, folder
				+ "king_rabbit.ogg");
		kingSoundID = sounds.load(king_fd.getFileDescriptor(),
				king_fd.getStartOffset(), king_fd.getLength(), 1);
		if (king_fd != null)
			king_fd.close();

		AssetFileDescriptor queen_fd = getFileDescriptor(this, folder
				+ "queen_rabbit.ogg");
		queenSoundID = sounds.load(queen_fd.getFileDescriptor(),
				queen_fd.getStartOffset(), queen_fd.getLength(), 1);
		if (queen_fd != null)
			queen_fd.close();

		AssetFileDescriptor joker_fd = getFileDescriptor(this, folder
				+ "joker_laugh.ogg");
		jokerSoundID = sounds.load(joker_fd.getFileDescriptor(),
				joker_fd.getStartOffset(), joker_fd.getLength(), 1);
		if (joker_fd != null)
			joker_fd.close();

		AssetFileDescriptor door_fd = getFileDescriptor(this, folder
				+ "door_opening.ogg");
		doorSoundID = sounds.load(door_fd.getFileDescriptor(),
				door_fd.getStartOffset(), door_fd.getLength(), 1);
		if (door_fd != null)
			door_fd.close();

		AssetFileDescriptor wind_fd = getFileDescriptor(this, folder
				+ "strong_wind.ogg");
		windSoundID = sounds.load(wind_fd.getFileDescriptor(),
				wind_fd.getStartOffset(), wind_fd.getLength(), 1);
		if (wind_fd != null)
			wind_fd.close();
	}

	int streamID;

	private void playSound(int soundID) {
		if (loaded) {
			streamID = sounds.play(soundID, 1, 1, 1, 0, 1);
		}
	}

	private void stopSound() {
		sounds.stop(streamID);
	}
private boolean strong_wind_playing = false;
	@Override
	protected void onStart() {
		super.onStart();
		mSound = (Boolean)IntentHelper.getObjectForKey(SOUND);
		mAds = (Boolean)IntentHelper.getObjectForKey(ADS);

		if(mAds){
		if(this.cb == null){
			this.cb = Chartboost.sharedChartboost();
			this.cb.onCreate(this, appId, appSignature, this.chartBoostDelegate);
		}
		this.cb.onStart(this);
		this.cb.startSession();
		this.cb.showInterstitial();
		}
		if(mSound){
		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		 result = am.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN);
		if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			initMediaPlayer();
			backgroundAudio.start();
		}}
		if(strong_wind_playing){
			strong_wind_playing = false;
			AssetFileDescriptor descriptor = null;
			try {
				descriptor = getFileDescriptor(this,
						"expansion/audio_files/strong_wind.ogg");
				strong_wind = new MediaPlayer();
				strong_wind.setDataSource(descriptor.getFileDescriptor(),
						descriptor.getStartOffset(), descriptor.getLength());
				strong_wind.prepare();
			} catch (Exception e) {
			} finally {
				if (descriptor != null)
					try {
						descriptor.close();
					} catch (IOException e) {
					}
			}
			strong_wind.setVolume(0.1f, 0.1f);
			strong_wind.start();
			strong_wind.setLooping(true);
		}
		sounds = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		sounds.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool sp, int sid, int status) {
				loaded = true;
			}
		});
		try {
			loadSounds();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_earth_room, menu);
		return true;
	}

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
			if (doorOpen == true) {
				closeTheDoor();
			}
			vf.showNext();
			wall = 1;
			parentLayout = getParentLayout(wall);
		}
	}

	private void closeTheDoor() {
		parentLayout.setBackgroundDrawable(door_wall);
		parentLayout.findViewById(R.id.door_handle).setClickable(true);
		parentLayout.findViewById(R.id.peephole_doorwall).setClickable(false);
		doorOpen = false;
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
			if (doorOpen == true) {
				closeTheDoor();
			}
			vf.showPrevious();
			wall = 3;
			parentLayout = getParentLayout(wall);

		}
	}

	private RelativeLayout getParentLayout(int wall) {
		int id = wall == 1 ? R.id.first : wall == 2 ? R.id.second
				: wall == 3 ? R.id.third : R.id.fourth;
		return (RelativeLayout) findViewById(id);
	}
	boolean pumpkin_found = false;
	boolean eyehole_open = false;
boolean removed = false;
private long mLastZoomClickTime = 0;

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
			transition.setBackgroundDrawable(null);
			transition.setVisibility(View.GONE);
			removed = true;
		}
		ImageView zoomedImage = null;
		if (view.getId() == R.id.key) {
			zoomedImage = (ImageView) parentLayout
					.findViewById(R.id.zoomed_image);
			// zoomedImage.setBackgroundResource(R.drawable.key_zoomed_er);
			zoomedImage.setBackgroundDrawable(storage.getDrawable(this,
					"key_zoomed_er"));
			((BitmapDrawable) parentLayout.getBackground()).getBitmap()
					.recycle();
			parentLayout.setBackgroundDrawable(null);
			parentLayout.setBackgroundDrawable(storage.getDrawable(this,
					"joker_wall_nokey"));

			// parentLayout.setBackgroundResource(R.drawable.joker_wall_nokey);
			view.setClickable(false);
			zoomedImage.setTag(R.id.key);
		} else if (view.getId() == R.id.globe_crown) {
			playSound(crownSoundID);
			zoomedImage = (ImageView) parentLayout
					.findViewById(R.id.zoomed_image);
			// zoomedImage.setBackgroundResource(R.drawable.crown_zoomed);
			zoomedImage.setBackgroundDrawable(storage.getDrawable(this,
					"crown_zoomed"));
			view.setClickable(false);
			((LinearLayout) parentLayout
					.findViewById(R.id.castle_painting_layout))
					.removeView(view);
			zoomedImage.setTag(R.id.globe_crown);
			tview.setText(getString(R.string.crown));
		} else if (view.getId() == R.id.heart) {
			view.setVisibility(View.GONE);
			playSound(heartSoundID);
			zoomedImage = (ImageView) parentLayout
					.findViewById(R.id.zoomed_image);
			// zoomedImage.setBackgroundResource(R.drawable.heart_zoomed);
			zoomedImage.setBackgroundDrawable(storage.getDrawable(this,
					"heart_zoomed"));
			LinearLayout heart_painting_layout = (LinearLayout) parentLayout
					.findViewById(R.id.heart_painting_layout);
			((BitmapDrawable) heart_painting_layout.getBackground())
					.getBitmap().recycle();
			// heart_painting_layout.setBackgroundResource(R.drawable.heart_painting_noheart);
			heart_painting_layout.setBackgroundDrawable(storage.getDrawable(
					this, "heart_painting_noheart"));
			zoomedImage.setTag(R.id.heart);
			tview.setText(getString(R.string.heart));
		} else if (view.getId() == R.id.frost) {
			playSound(sparkleSoundID);
			zoomedImage = (ImageView) parentLayout
					.findViewById(R.id.zoomed_image);
			// zoomedImage.setBackgroundResource(R.drawable.snowflake_zoomed);
			zoomedImage.setBackgroundDrawable(storage.getDrawable(this,
					"snowflake_zoomed"));
			LinearLayout snowflake_painting_layout = (LinearLayout) parentLayout
					.findViewById(R.id.frost_layout);
			((BitmapDrawable) snowflake_painting_layout.getBackground())
					.getBitmap().recycle();
			snowflake_painting_layout.setBackgroundDrawable(null);
			// snowflake_painting_layout.setBackgroundResource(R.drawable.snowflake_painting_empty);
			snowflake_painting_layout.setBackgroundDrawable(storage
					.getDrawable(this, "snowflake_painting_empty"));

			view.setClickable(false);
			snowflake_painting_layout.removeView(view);
			zoomedImage.setTag(R.id.frost);
			tview.setText(getString(R.string.snowflake));
		} else if (view.getId() == R.id.pumpkin) {
			pumpkin_found = true;
			zoomedImage = (ImageView) parentLayout
					.findViewById(R.id.zoomed_image);
			// zoomedImage.setBackgroundResource(R.drawable.pumpkin_zoomed);
			zoomedImage.setBackgroundDrawable(storage.getDrawable(this,
					"pumpkin_zoomed"));
			BitmapDrawable wall3new = null;
			if (!eyehole_open) {
				wall3new = storage.getDrawable(this,
						"mushroom_wall_nopumpkin");
			} else {
				wall3new = storage.getDrawable(this,
						"mushroom_wall_nopumpkin_open_peephole");
			}
			parentLayout.setBackgroundDrawable(wall3new);
			view.setClickable(false);
			parentLayout.removeView(view);
			zoomedImage.setTag(R.id.pumpkin);
			tview.setText(getString(R.string.pumpkin));
		} else if (view.getId() == R.id.eyehole) {
			eyehole_open = true;
			((BitmapDrawable) parentLayout.getBackground()).getBitmap()
					.recycle();
			parentLayout.setBackgroundDrawable(null);
			if (!pumpkin_found) {
				parentLayout.setBackgroundDrawable(storage.getDrawable(this,
						"mushroom_wall_with_pumpkin_open_peephole"));
			} else {
				parentLayout.setBackgroundDrawable(storage.getDrawable(this,
						"mushroom_wall_nopumpkin_open_peephole"));
			}
			view.setId(R.id.eyehole_open);
		} else if (view.getId() == R.id.eyehole_open) {
			playSound(kingSoundID);
			zoomedImage = (ImageView) parentLayout
					.findViewById(R.id.rabbit_mushroomwall);
			zoomedImage.setBackgroundDrawable(storage.getDrawable(this,
					"king_rabbit_small"));
			// zoomedImage.setBackgroundResource(R.drawable.king_rabbit_small);
			makeArrowsInvisible();
			zoomedImage.setTag(R.id.rabbit_mushroomwall);
			king_rabbit_found = true;
		} /*else if (view.getId() == R.id.peephole_doorwall) {
			zoomedImage = (ImageView) parentLayout
					.findViewById(R.id.queen_rabbit);
			// zoomedImage.setBackgroundResource(R.drawable.queen_rabbit_small);
			zoomedImage.setBackgroundDrawable(storage.getDrawable(this,
					"queen_rabbit_small"));
			playSound(queenSoundID);

			makeArrowsInvisible();
			zoomedImage.setTag(R.id.queen_rabbit);
		}*/ else if (view.getId() == R.id.pin) {
			view.setId(-1);
			LinearLayout globe = (LinearLayout) parentLayout
					.findViewById(R.id.globe);
			((BitmapDrawable) globe.getBackground()).getBitmap().recycle();
			globe.setBackgroundResource(R.drawable.globe_america_nopin);
			zoomedImage = (ImageView) parentLayout
					.findViewById(R.id.zoomed_image);
			zoomedImage.setBackgroundResource(R.drawable.pin_zoomed);
			// zoomedImage.setBackgroundDrawable(storage.getDrawable(this,
			// "pin_zoomed"));
			findViewById(R.id.pinLayout).setVisibility(View.GONE);
			view.setClickable(false);
zoomedImage.setTag(R.id.pin);
			tview.setText(getString(R.string.pin));
		}
		if(zoomedImage!=null){
			zoomedImage.setVisibility(View.VISIBLE);
			zoomedImage.setClickable(true);
			zoomedImage.setOnClickListener(this);}
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
			if (tag == R.id.key) {
				ImageButton newItem = (ImageButton) customView
						.findViewById(R.id.ImageButton01);
				newItem.setBackgroundResource(R.drawable.key_er_stored);
				newItem.setId(R.id.key_er_item);
				bagClickCount = 1;
				bagClicked(null);
			} else if (tag == R.id.globe_crown) {
				tview.setText(null);
				ImageButton newItem = (ImageButton) customView
						.findViewById(R.id.ImageButton02);
				newItem.setBackgroundResource(R.drawable.crown_stored);
				newItem.setId(R.id.crown_item);
				bagClickCount = 1;
				bagClicked(null);
			}

			else if (tag == R.id.heart) {
				tview.setText(null);
				ImageButton newItem = (ImageButton) customView
						.findViewById(R.id.ImageButton03);
				newItem.setBackgroundResource(R.drawable.heart_stored);
				newItem.setId(R.id.heart_item);
				bagClickCount = 1;
				bagClicked(null);
			} else if (tag == R.id.frost) {
				tview.setText(null);
				ImageButton newItem = (ImageButton) customView
						.findViewById(R.id.ImageButton04);
				newItem.setBackgroundResource(R.drawable.snowflake_stored);
				newItem.setId(R.id.snowflake_item);
				((BitmapDrawable) parentLayout.getBackground()).getBitmap()
						.recycle();
				// parentLayout.setBackgroundResource(R.drawable.globe_wall_empty_frame);
				parentLayout.setBackgroundDrawable(null);
				parentLayout.setBackgroundDrawable(storage.getDrawable(this,
						"globe_wall_empty_frame"));
				bagClickCount = 1;
				bagClicked(null);
				// view.setBackgroundResource(0);
				BitmapDrawable bitmapDrawable = (BitmapDrawable) view
						.getBackground();
			} else if (tag == R.id.pumpkin) {
				tview.setText(null);
				ImageButton newItem = (ImageButton) customView
						.findViewById(R.id.ImageButton05);
				newItem.setBackgroundResource(R.drawable.pumpkin_stored);
				newItem.setId(R.id.pumpkin_item);
				bagClickCount = 1;
				bagClicked(null);
			} else if (tag == R.id.pin) {
				tview.setText(null);
				ImageButton newItem = (ImageButton) customView
						.findViewById(R.id.ImageButton06);
				newItem.setBackgroundResource(R.drawable.pin_stored);
				newItem.setId(R.id.pin_item);
				bagClickCount = 1;
				bagClicked(null);
			} else if (tag == R.id.rabbit_mushroomwall) {
				stopSound();
				makeArrowsVisible();
				if (queen_rabbit_found) {
					getParentLayout(1).findViewById(R.id.globe).setClickable(
							true);
					king_rabbit_found = true;
				}
			} /*else if (tag == R.id.queen_rabbit) {
				stopSound();
				queen_rabbit_found = true;
				closeTheDoor();
				view.setVisibility(View.GONE);
				if (king_rabbit_found) {
					getParentLayout(1).findViewById(R.id.globe).setClickable(
							true);
				}
				makeArrowsVisible();
			}*/
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

	private boolean keyUsed = false;
	MediaPlayer jokerAudio;
	public void openTheLock(View v) {

		if (selectedItem == R.id.key_er_item) {
			useItem();
			selectedItem = -1;
			keyUsed = true;
		}
		if (selectedItem == R.id.key_er_item || keyUsed == true) {
			final ImageView joker = (ImageView)findViewById(R.id.joker);
			joker.setBackgroundDrawable(storage.getDrawable(this, "jokerbox_small"));
			joker.setVisibility(View.VISIBLE);
		 jokerAudio = new MediaPlayer();
			AssetFileDescriptor descriptor = null;
			try {
				descriptor = getFileDescriptor(this,
						"expansion/audio_files/joker_laugh.ogg");
				jokerAudio.setDataSource(descriptor.getFileDescriptor(),
						descriptor.getStartOffset(), descriptor.getLength());
				jokerAudio.prepare();
			} catch (Exception e) {
			} finally {
				if (descriptor != null)
					try {
						descriptor.close();
					} catch (IOException e) {
					}
			}
			jokerAudio.start();
			
			joker.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if(jokerAudio !=null){
					jokerAudio.stop();
					jokerAudio.reset();
					jokerAudio.release();
					jokerAudio = null;}
					((BitmapDrawable) joker.getBackground()).getBitmap().recycle();
					view.setVisibility(View.INVISIBLE);
					makeArrowsVisible();
				}
			});
			makeArrowsInvisible();
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

	int globeClick = 1;
	int globe_america_with_pin = R.drawable.globe_america_with_pin;
	int globe_america_no_pin = R.drawable.globe_america_nopin;
	int globe_aus_nopin = R.drawable.globe_aus_nopin;
	int globe_aus_with_pin = R.drawable.globe_aus_with_pin;
	int globe_without_crown = R.drawable.globe_without_crown;

	public void globeTapped(View view) {
		LinearLayout globe = (LinearLayout) parentLayout.findViewById(R.id.globe);
		findViewById(R.id.brisbane).setClickable(false);
		if(findViewById(R.id.pin)!=null){
		findViewById(R.id.pin).setClickable(false);
		}
		if (globeClick == 1) {
			if (parentLayout.findViewById(R.id.pin) != null) {
				globe.setBackgroundResource(globe_america_with_pin);
				parentLayout.findViewById(R.id.pin).setClickable(true);
			} else {
				parentLayout.findViewById(R.id.globe).setBackgroundResource(
						globe_america_no_pin);
			}
			globeClick++;
		} else {
			if (globeClick == 2) {
				if (pinned == false) {
					parentLayout.findViewById(R.id.globe).setBackgroundResource(globe_aus_nopin);
					parentLayout.findViewById(R.id.brisbane).setClickable(true);
				} else {
					parentLayout.findViewById(R.id.globe)
							.setBackgroundResource(globe_aus_with_pin);
				}
				globeClick++;
			} else if (globeClick == 3) {
				parentLayout.findViewById(R.id.globe).setBackgroundResource(
						globe_without_crown);
				globeClick = 1;
			}
			if (parentLayout.findViewById(R.id.pin) != null) {
				parentLayout.findViewById(R.id.pin).setClickable(false);
			}

		}
	}

	int swap = 0;
	boolean swapped = false;
	View selectedCard = null;
	int order[] = new int[4];

	public void placeCard(View view) {
		if (selectedItem != -1 && view.getTag() == null) {
			useItem();
			String placed_name = getResources().getResourceEntryName(
					selectedItem);
			placed_name = placed_name.replace("item", "placed");
			String packageName = this.getPackageName();
			int placed_background = getResources().getIdentifier(placed_name,
					"drawable", packageName);
			// set background image of the button
			view.setBackgroundResource(placed_background);
			view.setTag("taken");
			/*
			 * float scale = this.getResources().getDisplayMetrics().density;
			 * float width = (view.getWidth() - 0.5f)/scale; float height =
			 * (view.getHeight()-0.5f)/scale;
			 */

			order[getCardOrder(view)] = placed_background;
			checkTheOrder(order);
			selectedItem = -1;
		}

		else {
			swapped = false;
			if ((selectedCard != null || selectedCard == view) && swap == 1) {
				Drawable selectedCardFace = selectedCard.getBackground();
				selectedCard.setBackgroundDrawable(view.getBackground());
				view.setBackgroundDrawable(selectedCardFace);
				swapped = true;
				swap = 0;
				int view1 = getCardOrder(selectedCard);
				int view2 = getCardOrder(view);
				int temp = order[view1];
				order[view1] = order[view2];
				order[view2] = temp;
				checkTheOrder(order);
			}
			selectedCard = view;
			if (!swapped) {
				swap++;
			}
		}
	}

	private int getCardOrder(View view) {
		if (view.getId() == R.id.card1) {
			return 0;
		} else if (view.getId() == R.id.card2) {
			return 1;
		} else if (view.getId() == R.id.card3) {
			return 2;
		} else if (view.getId() == R.id.card4) {
			return 3;
		}

		return -1;
	}

	private void useItem() {
		String stored_name = getResources().getResourceEntryName(selectedItem);
		stored_name = stored_name.replace("item", "faded");
		int stored_background = getResources().getIdentifier(stored_name,
				"drawable", this.getPackageName());
		ImageButton item = (ImageButton) findViewById(selectedItem);
		((BitmapDrawable) item.getBackground()).getBitmap().recycle();
		item.setBackgroundResource(stored_background);
		findViewById(selectedItem).setClickable(false);
	}

	private void checkTheOrder(int[] order) {
		boolean orderIsRight = true;
		int correctOrder[] = {R.drawable.snowflake_placed, R.drawable.crown_placed,
				 R.drawable.heart_placed,
				R.drawable.pumpkin_placed };
		for (int i = 0; i < correctOrder.length; i++) {
			if (correctOrder[i] != order[i]) {
				orderIsRight = false;
			}
		}
		if (orderIsRight == true) {
			//findViewById(R.id.queen_rabbit).setBackgroundDrawable(storage.getDrawable(this, "queen_rabbit_small"));
			door_wall_open = storage.getDrawable(this, "door_open");
			playSound(doorSoundID);
			RelativeLayout layout_wall4 = getParentLayout(4);
			layout_wall4.setBackgroundDrawable(door_wall_open);
			layout_wall4.findViewById(R.id.peephole_doorwall)
					.setClickable(true);
			layout_wall4.findViewById(R.id.door_handle).setVisibility(
					View.VISIBLE);
			doorOpen = true;
		}
	}

	boolean pinned = false;

	public void placePin(View view) {
		if (selectedItem == R.id.pin_item) {
			playSound(sparkleSoundID);
			parentLayout.findViewById(R.id.brisbane).setVisibility(View.GONE);
			LinearLayout globe = (LinearLayout) parentLayout
					.findViewById(R.id.globe);
			((BitmapDrawable) globe.getBackground()).getBitmap().recycle();
			globe.setBackgroundResource(R.drawable.globe_aus_with_pin);
			pinned = true;
			door_wall_open.getBitmap().recycle();

	/*		((BitmapDrawable) findViewById(R.id.castle_painting_layout)
					.getBackground()).getBitmap().recycle();
			((BitmapDrawable) findViewById(R.id.heart_painting_layout)
					.getBackground()).getBitmap().recycle();
			((BitmapDrawable) findViewById(R.id.pumpkin_painting_zoomed)
					.getBackground()).getBitmap().recycle();
			((BitmapDrawable) findViewById(R.id.winter_painting_zoomed)
					.getBackground()).getBitmap().recycle();
			((BitmapDrawable) findViewById(R.id.joker).getBackground())
					.getBitmap().recycle();
			((BitmapDrawable) findViewById(R.id.queen_rabbit).getBackground())
					.getBitmap().recycle();
			((BitmapDrawable) findViewById(R.id.rabbit_mushroomwall)
					.getBackground()).getBitmap().recycle();*/
		}
	}

	boolean doorOpen = false;
	public static MediaPlayer strong_wind;

	public void openTheDoor(View view) {
		if (pinned) {
			playSound(windSoundID);
			if (backgroundAudio != null) {
				backgroundAudio.stop();
				backgroundAudio.release();
				backgroundAudio = null;
			}
			AssetFileDescriptor descriptor = null;
			try {
				descriptor = getFileDescriptor(this,
						"expansion/audio_files/strong_wind.ogg");
				strong_wind = new MediaPlayer();
				strong_wind.setDataSource(descriptor.getFileDescriptor(),
						descriptor.getStartOffset(), descriptor.getLength());
				strong_wind.prepare();
			} catch (Exception e) {
			} finally {
				if (descriptor != null)
					try {
						descriptor.close();
					} catch (IOException e) {
					}
			}
			strong_wind.setVolume(0.1f, 0.1f);
			strong_wind.start();
			strong_wind.setLooping(true);
			((BitmapDrawable) parentLayout.getBackground()).getBitmap().recycle();
			parentLayout.setBackgroundDrawable(null);
			parentLayout.setBackgroundDrawable(storage.getDrawable(this,
					"door_open_hanging_walk"));
			parentLayout.findViewById(R.id.hanging_walk).setClickable(true);
			((BitmapDrawable)findViewById(R.id.castle_painting_layout).getBackground()).getBitmap().recycle();
			((BitmapDrawable)findViewById(R.id.frost_layout).getBackground()).getBitmap().recycle();
			((BitmapDrawable)findViewById(R.id.heart_painting_layout).getBackground()).getBitmap().recycle();
			((BitmapDrawable)findViewById(R.id.pumpkin_painting_zoomed).getBackground()).getBitmap().recycle();
			((BitmapDrawable)findViewById(R.id.winter_painting_zoomed).getBackground()).getBitmap().recycle();
			System.gc();
			/*((BitmapDrawable)findViewById(R.id.queen_rabbit).getBackground()).getBitmap().recycle();*/
		} else {
			parentLayout.setBackgroundDrawable(door_wall_open);
			parentLayout.findViewById(R.id.peephole_doorwall)
					.setClickable(true);
			doorOpen = true;
		}
	}

	MediaPlayer lightning;

	public void strike(View view) {
		IntentHelper.addObjectForKey(mSound, SOUND);
		IntentHelper.addObjectForKey(mAds, ADS);
		view.setClickable(false);
		strong_wind.stop();
		strong_wind.release();
		strong_wind = null;
		((BitmapDrawable) parentLayout.getBackground()).getBitmap().recycle();
		lightning.start();
		final ImageView letter_image = (ImageView) parentLayout.findViewById(R.id.letter_image);
		((BitmapDrawable) letter_image.getBackground()).getBitmap().recycle();
		System.gc();
		lightning.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer arg0) {
				final ImageView success = (ImageView) parentLayout
						.findViewById(R.id.success);
				success.setBackgroundResource(R.drawable.success);
				// fade out view nicely after 10 seconds
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						((BitmapDrawable) success.getBackground()).getBitmap()
								.recycle();
						System.gc();
						startEndCredits();
					}
				}, 8500);
			}
		});
		 parentLayout.setBackgroundResource(R.drawable.coil_twice);
		//parentLayout.setBackgroundDrawable(storage.getDrawable(this,"coil_twice"));
	}
	protected void startEndCredits() {
		
		if (adViewFromXml != null) {
			adViewFromXml.removeAllViews();
			adViewFromXml = null;
		}
		Intent intent = new Intent(this, EndCreditsActivity.class);
		intent.putExtra(ACTIVITY,"EARTH");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		unbindDrawables(findViewById(R.id.earth_room_layout));
		startActivity(intent);
		this.finish();

	}

	//ScrollView letter;

	public void goOut(View view) {

		sounds.release();
		sounds = null;

		unbindDrawables(findViewById(R.id.earth_room_layout));

		final ViewGroup viewGroup = (ViewGroup) findViewById(R.id.earth_room_layout);
		viewGroup.removeAllViews();
		viewGroup.addView(View.inflate(this,R.layout.end_coil_scene, null));
		if(am!=null){
		am.abandonAudioFocus(this);}
		if(backgroundAudio!=null){backgroundAudio.stop();
		backgroundAudio.release();
		backgroundAudio=null;}
		parentLayout = (RelativeLayout) findViewById(R.id.end_scene);
		//parentLayout.setBackgroundDrawable(storage.getDrawable(this, "coil"));
		//parentLayout.findViewById(R.id.letter_image).setBackgroundDrawable(storage.getDrawable(this, "tesla_letter"));
		//letter = (ScrollView) parentLayout.findViewById(R.id.letter);
		//letter.setOnTouchListener(this);

		AssetFileDescriptor lightning_fd = null;
		lightning = new MediaPlayer();

		try {
			lightning_fd = getFileDescriptor(this,"expansion/audio_files/lightning_strike.ogg");
			lightning.setDataSource(lightning_fd.getFileDescriptor(),lightning_fd.getStartOffset(), lightning_fd.getLength());
			lightning.prepare();
			lightning.setVolume(0.1f, 0.1f);} 
		catch (Exception e) {} 
		finally {
			if (lightning_fd != null)
				try {
					lightning_fd.close();
				} catch (IOException e) {}
		}
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

	public void zoomPainting(View view) {
		int id = view.getId();
		if (id == R.id.frost_painting) {
			//findViewById(R.id.frost_layout).setBackgroundDrawable(storage.getDrawable(this, "frost_painting"));
			parentLayout.findViewById(R.id.frost_layout).setVisibility(
					View.VISIBLE);
		} else if (id == R.id.heart_painting) {
			//findViewById(R.id.heart_painting_layout).setBackgroundDrawable(storage.getDrawable(this, "heart_painting"));
			parentLayout.findViewById(R.id.heart_painting_layout)
					.setVisibility(View.VISIBLE);
		} else if (id == R.id.pumpkin_painting) {
			//findViewById(R.id.pumpkin_painting_zoomed).setBackgroundDrawable(storage.getDrawable(this, "pumpkin_painting"));
			parentLayout.findViewById(R.id.pumpkin_painting_zoomed)
					.setVisibility(View.VISIBLE);
		} else if (id == R.id.winter_painting) {	
			//findViewById(R.id.winter_painting_zoomed).setBackgroundDrawable(storage.getDrawable(this, "winter_painting"));
			parentLayout.findViewById(R.id.winter_painting_zoomed)
					.setVisibility(View.VISIBLE);
		} else if (id == R.id.castle_painting) {
			//findViewById(R.id.castle_painting_layout).setBackgroundDrawable(storage.getDrawable(this, "castle_painting"));
          findViewById(R.id.castle_painting_layout)
					.setVisibility(View.VISIBLE);
		} else if (id == R.id.peephole_doorwall) {
		findViewById(R.id.queen_rabbit).setBackgroundDrawable(storage.getDrawable(this,
					"queen_rabbit_small"));
			findViewById(R.id.queen_rabbit).setVisibility(View.VISIBLE);
			if (king_rabbit_found) {
				getParentLayout(1).findViewById(R.id.globe).setClickable(true);
			}
			makeArrowsVisible();

			playSound(queenSoundID);
			queen_rabbit_found = true;
		}
		makeArrowsInvisible();
	}

	class GestureListener extends GestureDetector.SimpleOnGestureListener {
		RelativeLayout parentLayout;
		View view;

		public void setView(View view) {
			this.view = view;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		// event when double tap occurs
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if(view.getId()!=R.id.letter){
				makeArrowsVisible();	}
				view.setVisibility(View.GONE);
				return true;
		}
	}
public void closeLetter(View view){
	adViewFromXml = null;
view.setVisibility(View.GONE);
}
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		gListener.setView(view);
		return gestureDetector.onTouchEvent(event);
	}

	private void initMediaPlayer() {
		AssetFileDescriptor descriptor = null;
		try {
			descriptor = getFileDescriptor(this,
					"expansion/audio_files/forest_ambiance.ogg");
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
		// backgroundAudio = MediaPlayer.create(EarthRoomActivity.this,
		// R.raw.forest_ambiance);
		backgroundAudio.setLooping(true);
		backgroundAudio.setVolume(0.1f, 0.1f);
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
			if(backgroundAudio!=null){{
			if (backgroundAudio.isPlaying())
				backgroundAudio.stop();
			backgroundAudio.release();
			backgroundAudio = null;}}
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
	static final String ONBACK = "onback";

	@Override
	public void onBackPressed() {
		if (this.cb!=null && this.cb.onBackPressed())
			// If a Chartboost view exists, close it and return
			return;
		else {
			IntentHelper.addObjectForKey("onBack", ONBACK);

		Intent inMain = new Intent(this, MenuActivity.class);
		inMain.putExtra("activity", "EarthRoom");
		startActivityForResult(inMain, 0);}
	}

	final public int RESULT_CLOSE_ALL = 1;
	final public int NEW_GAME = 2;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case NEW_GAME:
			if (adViewFromXml != null) {
				adViewFromXml.removeAllViews();
				adViewFromXml = null;
			}
			Intent intent = new Intent(this, FireRoomActivity.class);
			intent.putExtra(ACTIVITY,"EARTH");
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			unbindDrawables(findViewById(R.id.earth_room_layout));
			startActivity(intent);
			this.finish();
			//setResult(RESULT_CLOSE_ALL);
			
			//adViewFromXml = null;
			//unbindDrawables(findViewById(R.id.earth_room_layout));
			//this.finish();
		/*	Intent intent = new Intent(this, FireRoomActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);*/

		case RESULT_CLOSE_ALL:
			setResult(RESULT_CLOSE_ALL);
			finish();
			System.gc();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG,"onStop");

		IntentHelper.addObjectForKey(mSound, SOUND);
		IntentHelper.addObjectForKey(mAds, ADS);
		mHandler.removeCallbacks(null);
		if(jokerAudio!=null){
			jokerAudio.stop();
			jokerAudio.release();
			jokerAudio = null;
		}
		if (cb != null) {
			this.cb.onStop(this);
		}
		if(am!=null){
		am.abandonAudioFocus(this);
		if(backgroundAudio!=null){backgroundAudio.stop();
		backgroundAudio.release();
		backgroundAudio=null;}}
		if(strong_wind!=null){
			strong_wind_playing = true;
			strong_wind.stop();
			strong_wind.release();
			strong_wind = null;
		}
	}

	@Override
	protected void onDestroy() {
		if (cb != null) {
			this.cb.onDestroy(this);
			this.cb = null;
			cb = null;
		}
		if (adViewFromXml != null) {
			adViewFromXml.removeAllViews();
			adViewFromXml = null;
		}
		System.gc();
		super.onDestroy();

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

	public void temp() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		storage = new ImageStorage(getResources(), metrics);
		customView = (CustomView) findViewById(R.id.customView);
		tview = (TextView) customView.findViewById(R.id.closeup_text);
		vf = (ViewFlipper) findViewById(R.id.earth_room_flipper);
		wall = 1;
		parentLayout = getParentLayout(wall);
	}
/*	public void zoomDown(View view){
		makeArrowsVisible();	
		//((BitmapDrawable) view.getBackground()).getBitmap().recycle();		
		view.setVisibility(View.GONE);
	}*/
	public void zoomDownQueenRabbit(View view){
		makeArrowsVisible();	
		((BitmapDrawable) view.getBackground()).getBitmap().recycle();		
		view.setVisibility(View.GONE);
	}

	public void nextLevel(View view) {
		view.setVisibility(View.GONE);
		new LoadTask().execute();
	}
	private void setMMediaAd() {
		adViewFromXml = (MMAdView) findViewById(R.id.adView);
		MMRequest request = new MMRequest();
		adViewFromXml.setMMRequest(request);
		adViewFromXml.setListener(new AdListener());
		adViewFromXml.getAd();
	}
	public void removeLoading() {
		if(mAds){
		setMMediaAd();
		}
		findViewById(R.id.transition).setVisibility(View.INVISIBLE);
		findViewById(R.id.earth_room_layout).setVisibility(View.VISIBLE);
	}

	private class LoadTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			((BitmapDrawable)findViewById(R.id.enter_layout).getBackground()).getBitmap().recycle();	
			findViewById(R.id.enter_layout).setBackgroundResource(0);
		}

		@Override
		protected Void doInBackground(Void... params) {
			temp();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			loadImages();
			gListener = new GestureListener();
			gestureDetector = new GestureDetector(EarthRoomActivity.this,
					gListener);
			removeLoading();
		}

	}
}
