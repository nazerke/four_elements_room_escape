package com.fourelements.roomescape;

import java.io.IOException;
import com.android.vending.billing.IInAppBillingService;
import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.parse.Parse;
import com.parse.ParseObject;

import util.Purchase;
import util.IabHelper;
import util.IabResult;
import util.Inventory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MenuActivity extends Activity implements
		AudioManager.OnAudioFocusChangeListener {
	Intent intent;
	Typeface tf;
	TextView newgame, resume, settings, walkthroughs, about;
	int walkthrough_board = R.drawable.walkthrough;
	int settings_board = R.drawable.settings;
	int about_board = R.drawable.about;
	RelativeLayout boardLayout;
	int on = R.drawable.on;
	int off = R.drawable.off;
	int walkthrough_clicked, about_clicked, settings_clicked = 0;
	private final String TAG = "Lifecycle ";
	public static final String PREFS_NAME = "Settings";
	public static final String ACTIVITY = "activity";

	// Does the user have an access to previews?
	boolean have_fire_preview_access = false;
	boolean have_water_preview_access = false;
	boolean have_earth_preview_access = false;

	// SKUs for our products
	static final String SKU_PREMIUM = "premium";
	public static final String SKU_FIRE = "fire_room_walkthrough";
	public static final String SKU_WATER = "water_room_walkthrough";
	public static final String SKU_EARTH = "earth_room_walkthrough";

	// (arbitrary) request code for the purchase flow
	static final int RC_REQUEST = 10001;

	static final String SOUND = "sound";
	static final String ADS = "ads";
	static final String FIRE_PREVIEW = "fire_preview";
	static final String WATER_PREVIEW = "water_preview";
	static final String EARTH_PREVIEW = "earth_preview";

	boolean mSound = true;
	boolean mAds = true;
	final public int RESULT_CLOSE_ALL = 1;
	final public int NEW_GAME = 2;
	AudioManager am;
	MediaPlayer backgroundAudio;
	IabHelper mHelper;
	SharedPreferences preferences;
static Context context;
	public void clear() {
		SharedPreferences.Editor editor = preferences.edit();
		editor.clear();
		editor.commit();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		context = this.getApplicationContext();
		// compute your public key and store it in base64EncodedPublicKey
		String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlACCvna5m+eU2ajiHO0yTIdLfazmCoQJBgnIFDnvt4oz0dl4qH7ZLIa58VmKF64AOZsEp7EF9tfpkJUJkxGHJ/J3BL4/OPVWlcYKE/ApiG0uP1ayAIylYnH6rvlf000wxKZwfkUNCEbhZln6BYTGZJJnv/2Pr7PmEj+Q3a0ZjA+kursDRA4nhCV1CoW0iF4n3qpnCMWkMHIcLrWg0z5pGPXfEka+fYpJ8whCiRW2gAPFIB9eS9Be48CjrDg9QabuvCEGwyZqzdz66UDdnvWl0mCqh9xhvOtluQFkiIvH8SnuyN+D2pN42h7L/A4o5h6/K47I3TBZsX9OoQbDutuogwIDAQAB";
		mHelper = new IabHelper(this, base64EncodedPublicKey);
		super.onCreate(savedInstanceState);
		Log.d("Key", "Message");

		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			@Override
			public void onIabSetupFinished(IabResult result) {
				if (!result.isSuccess()) {
					// Oh noes, there was a problem.
					Log.d(TAG, "Problem setting up In-app Billing: " + result);
				}
				// Have we been disposed of in the meantime? If so, quit.
				if (mHelper == null)
					return;
				else {
					// IAB is fully set up. Now, let's get an inventory of stuff
					// we own.
					Log.d(TAG, "Setup successful. Querying inventory.");
					mHelper.queryInventoryAsync(mGotInventoryListener);
				}
			}
		});
		setContentView(R.layout.activity_menu);
		getSoundPreferences();
		setTypeface();
		boardLayout = (RelativeLayout) this.findViewById(R.id.board_layout);	 
		settings(null);
	}

	IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
		@Override
		public void onQueryInventoryFinished(IabResult result,
				Inventory inventory) {
 			Log.d(TAG, "Query inventory finished.");

			// Have we been disposed of in the meantime? If so, quit.
			if (mHelper == null)
				return;

			if(inventory==null){
				Log.d(TAG, "inventory null.");

			}
			// Is it a failure?
			if (result.isFailure()) {
	 			Log.d(TAG, "IAB result failure.");
				have_fire_preview_access = preferences.getBoolean(FIRE_PREVIEW, false);
				have_water_preview_access = preferences.getBoolean(WATER_PREVIEW, false);
				have_earth_preview_access = preferences.getBoolean(EARTH_PREVIEW, false);
				mAds = preferences.getBoolean(ADS, true);
				IntentHelper.addObjectForKey(mAds, ADS);
			    if (mAds == false) {
					findViewById(R.id.ad).setTag("off");
					findViewById(R.id.ad).setBackgroundResource(off);
				}
				return;
			}
			Log.d(TAG, "Query inventory was successful.");

			/*
			 * Check for items we own. Notice that for each purchase, we check
			 * the developer payload to see if it's correct! See
			 * verifyDeveloperPayload().
			 */

			// Do we have the premium upgrade?
			Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
			mAds = !(premiumPurchase != null);
			Log.d(TAG, "User is " + (!mAds ? "PREMIUM" : "NOT PREMIUM"));

			// Do we have the access to the fire room preview?
			Purchase firePreviewPurchase = inventory.getPurchase(SKU_FIRE);
			have_fire_preview_access = (firePreviewPurchase != null);
			Log.d(TAG, "User "
					+ (have_fire_preview_access ? "HAS" : "DOES NOT HAVE")
					+ " access to the fire room preview.");

			// Do we have the access to the water room preview?
			Purchase waterPreviewPurchase = inventory.getPurchase(SKU_WATER);
			have_water_preview_access = (waterPreviewPurchase != null);
			Log.d(TAG, "User "
					+ (have_water_preview_access ? "HAS" : "DOES NOT HAVE")
					+ " access to the water room preview.");

			// Do we have the access to the earth room preview?
			Purchase earthPreviewPurchase = inventory.getPurchase(SKU_EARTH);
			have_earth_preview_access = (earthPreviewPurchase != null);
			Log.d(TAG, "User "
					+ (have_earth_preview_access ? "HAS" : "DOES NOT HAVE")
					+ " access to the earth room preview.");
			Log.d(TAG, "Initial inventory query finished;");
			
			//String firePreviewPrice = inventory.getSkuDetails(SKU_FIRE).getPrice();
			
			IntentHelper.addObjectForKey(mAds, ADS);
		    if (mAds == false) {
				findViewById(R.id.ad).setTag("off");
				findViewById(R.id.ad).setBackgroundResource(off);
			}
		}
	};
	private void setTypeface() {
		newgame = (TextView) this.findViewById(R.id.new_game);
		resume = (TextView) this.findViewById(R.id.resume);
		settings = (TextView) this.findViewById(R.id.settings);
		walkthroughs = (TextView) this.findViewById(R.id.walkthroughs);
		about = (TextView) this.findViewById(R.id.about);

		tf = Typeface.createFromAsset(getAssets(), "fonts/Livingst.ttf");
		newgame.setTypeface(tf);
		resume.setTypeface(tf);
		settings.setTypeface(tf);
		walkthroughs.setTypeface(tf);
		about.setTypeface(tf);
		about.setTypeface(tf);
	}

	private void getSoundPreferences() {
		preferences = getSharedPreferences(PREFS_NAME, 0);
		if (preferences != null) {
			mSound = preferences.getBoolean(SOUND, true);
			//mAds = preferences.getBoolean(ADS, true);

			IntentHelper.addObjectForKey(mSound, SOUND);
			//IntentHelper.addObjectForKey(mAds, ADS);

			if (mSound == false) {
				findViewById(R.id.sound).setTag("off");
				findViewById(R.id.sound).setBackgroundResource(off);
			}
			/*
			 * mAds = preferences.getBoolean(ADS, true);
			 * IntentHelper.addObjectForKey(mAds, ADS);
			 * if (mAds == false) {
				findViewById(R.id.ad).setTag("off");
				findViewById(R.id.ad).setBackgroundResource(off);
			}*/
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

	private void initMediaPlayer() {
		backgroundAudio = MediaPlayer.create(MenuActivity.this,
				R.raw.menu);
		backgroundAudio.setLooping(true);
		backgroundAudio.setVolume(0.5f, 0.5f);
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
			if (backgroundAudio.isPlaying())
				backgroundAudio.stop();
			backgroundAudio.release();
			backgroundAudio = null;
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_menu, menu);
		return true;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		/*
		 * boolean sound = mSound; boolean ads = mAds; // Save the user's
		 * current game state savedInstanceState.putBoolean(SOUND, mSound);
		 * savedInstanceState.putBoolean(ADS, mAds);
		 */
		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Always call the superclass so it can restore the view hierarchy
		super.onRestoreInstanceState(savedInstanceState);
		/*
		 * // Restore state members from saved instance mSound =
		 * savedInstanceState.getBoolean(SOUND); mAds =
		 * savedInstanceState.getBoolean(ADS);
		 * IntentHelper.addObjectForKey(mSound, SOUND);
		 * IntentHelper.addObjectForKey(mAds, ADS);
		 * 
		 * if(mSound == false){ findViewById(R.id.sound).setTag("off");
		 * findViewById(R.id.sound).setBackgroundResource(off); } if(mAds =
		 * false){ findViewById(R.id.ad).setTag("off");
		 * findViewById(R.id.ad).setBackgroundResource(off); }
		 */
	}

	public void start(View v) {
		intent = new Intent(this, FireRoomActivity.class);
		startActivity(intent);
	}

	public void newGame(View view) {
		view.setClickable(false);
		changeColor(view);
		Intent oldintent = getIntent();
		String activity = oldintent.getStringExtra("activity");
		if (am != null) {
			am.abandonAudioFocus(this);
		}

		if (activity != null) {
			setResult(NEW_GAME);
			/*Intent intent = new Intent(this,FireRoomActivity.class);
			intent.putExtra("ACTIVITY_NAME", "Fire");
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			Log.d("new game", "new game");

			startActivity(intent);	*/
		} else {
			Intent intent = new Intent(MenuActivity.this, FireRoomActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
		finish();
	}

	private void nullify() {
		newgame = null;
		resume = null;
		settings = null;
		walkthroughs = null;
		about = null;

	}

	static void unbindDrawables(View view) {
		try {
			Log.d("unbinding", view.toString());
			if (view.getBackground() != null) {
				((BitmapDrawable) view.getBackground()).getBitmap().recycle();
				view.getBackground().setCallback(null);
				view = null;
			}
			if (view instanceof ViewGroup) {
				for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
					unbindDrawables(((ViewGroup) view).getChildAt(i));
				}
				((ViewGroup) view).removeAllViews();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
	}

	@Override
	protected void onPause() {
		if (backgroundAudio != null) {
			backgroundAudio.stop();
			backgroundAudio.release();
			backgroundAudio = null;
		}
		if (am != null) {
			am.abandonAudioFocus(this);
		}
		System.gc();
		Log.d(TAG, "onPause");
		super.onPause();
	}

	@Override
	protected void onRestart() {
		Log.d(TAG, "onRestart");
		super.onRestart();
	}

	@Override
	protected void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();
		if (mSound) {
			startBackgroundAudio();
		}
	}

	private void startBackgroundAudio() {
		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int result = am.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN);
		if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			initMediaPlayer();
			backgroundAudio.start();
		}

	}

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		 //IntentHelper.getObjectForKey(ONBACK);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mService != null) {
			unbindService(mServiceConn);
		}
		Log.d(TAG, "onDestroy");
		if (mHelper != null)
			mHelper.dispose();
		mHelper = null;
		 String onback = (String) IntentHelper.getObjectForKey(ONBACK);


	}

	static final String ONBACK = "onback";
	public void resume(View view) {
		// startGame();
		view.setClickable(false);
		changeColor(view);
		if (backgroundAudio != null) {
			backgroundAudio.stop();
			backgroundAudio.release();
			backgroundAudio = null;
		}
		if (am != null) {
			am.abandonAudioFocus(this);
			am = null;
		}
		nullify();
		 String onback = (String) IntentHelper.getObjectForKey(ONBACK);
	     //IntentHelper.addObjectForKey(ONBACK,"onback");

		if (onback != null) {
			Log.d("onback", "onback");
			String returning_activity = getIntent().getStringExtra("activity");
			if (returning_activity != null
					&& returning_activity.equals("Credits")) {
				startGame();
			}
		} else if (onback == null) {
			Log.d("onback", "onback is null");
			preferences = getSharedPreferences(PREFS_NAME, 0);
			if (preferences != null) {
				Log.d("preferences", "preferences");
				String activity = preferences.getString(ACTIVITY, null);
				if (activity != null) {
					Log.d("activity", "activity");
					if (activity.equals("WaterRoom")) {
						Intent intent = new Intent(this,
								WaterRoomActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					} else if (activity.equals("EarthRoom")) {
						Intent intent = new Intent(this,
								EarthRoomActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);

					} else if (activity.equals("EarthRoomBegin")) {
						Intent intent = new Intent(this,
								EarthRoomBeginActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					} else {
						startGame();
					}
				} else {
					Log.d("activity", "activity is null");
					startGame();
				}
			} else {
				Log.d("preferences", "preferences is null");
				startGame();

			}
		}

		finish();
	}

	private void startGame() {
		Intent intent = new Intent(this, FireRoomActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	public void settings(View view) {
		if (settings_clicked == 0) {
			settings_clicked++;
			if (walkthrough_clicked == 1) {
				changeColorBack(findViewById(R.id.walkthroughs));
				closeWalkthroughBoard();
				walkthrough_clicked = 0;

			}
			if (about_clicked == 1) {
				changeColorBack(findViewById(R.id.about));
				closeAboutBoard();
				about_clicked = 0;
			}
			if(view!=null){
			changeColor(view);}
			boardLayout.findViewById(R.id.settings_ll).setVisibility(
					View.VISIBLE);
			boardLayout.setBackgroundResource(settings_board);

		} else if (settings_clicked == 1) {
			if(view!=null){
			changeColorBack(view);}
			closeSettingsBoard();
			settings_clicked = 0;
			boardLayout.setBackgroundResource(R.drawable.plain_board);

		}
	}

	public void walkthroughs(View view) {
		if (walkthrough_clicked == 0) {
			if (settings_clicked == 1) {
				closeSettingsBoard();
				changeColorBack(findViewById(R.id.settings));
				settings_clicked = 0;
			}
			if (about_clicked == 1) {
				closeAboutBoard();
				changeColorBack(findViewById(R.id.about));
				about_clicked = 0;
			}
			changeColor(view);
			boardLayout.setBackgroundResource(walkthrough_board);
			boardLayout.findViewById(R.id.walkthroughs_ll).setVisibility(
					View.VISIBLE);

			walkthrough_clicked++;
		} else {
			changeColorBack(view);
			walkthrough_clicked = 0;
			closeWalkthroughBoard();
			boardLayout.setBackgroundResource(R.drawable.plain_board);

		}

	}

	public void about(View view) {
		if (about_clicked == 0) {
			if (settings_clicked == 1) {
				closeSettingsBoard();
				changeColorBack(findViewById(R.id.settings));
				settings_clicked = 0;
			}
			if (walkthrough_clicked == 1) {
				closeWalkthroughBoard();
				changeColorBack(findViewById(R.id.walkthroughs));
				walkthrough_clicked = 0;
			}
			changeColor(view);
			about_clicked++;
			boardLayout.setBackgroundResource(about_board);
			boardLayout.findViewById(R.id.about_ll).setVisibility(View.VISIBLE);
		} else if (about_clicked == 1) {
			changeColorBack(view);
			about_clicked = 0;
			closeAboutBoard();
			boardLayout.setBackgroundResource(R.drawable.plain_board);
		}

	}

	private void changeColorBack(View view) {
		((TextView) view).setTextColor(Color.parseColor("#FFFFF0"));

	}

	private void changeColor(View view) {
		((TextView) view).setTextColor(Color.parseColor("#FFDF00"));
	}

	public void change(View view) {
		String state = (String) view.getTag();

		if (view.getId() == R.id.sound) {
			if (state.equals("off")) {
				mSound = true;
				view.setTag("on");
				view.setBackgroundResource(on);
				startBackgroundAudio();
			} else if (state.equals("on")) {
				mSound = false;
				view.setTag("off");
				view.setBackgroundResource(off);
				backgroundAudio.stop();
				backgroundAudio.release();
				backgroundAudio = null;
				am.abandonAudioFocus(this);
				am = null;
			}
			IntentHelper.addObjectForKey(mSound, SOUND);
		}

		else if (view.getId() == R.id.ad && mAds == true) {
			Log.d(TAG, "Launching purchase flow for ads off.");
			String payload = "";

			mHelper.launchPurchaseFlow(this, SKU_PREMIUM , RC_REQUEST,
					mPurchaseFinishedListener, payload);
		}
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(SOUND, mSound);
		editor.commit();
	}

	private void closeAboutBoard() {
		boardLayout.findViewById(R.id.about_ll).setVisibility(View.INVISIBLE);
	}

	private void closeSettingsBoard() {
		boardLayout.findViewById(R.id.settings_ll)
				.setVisibility(View.INVISIBLE);
	}

	private void closeWalkthroughBoard() {
		boardLayout.findViewById(R.id.walkthroughs_ll).setVisibility(
				View.INVISIBLE);
	}

	@Override
	public void onBackPressed() {
		/*
		 * Intent intent = getIntent(); String activity =
		 * intent.getStringExtra("activity"); if(activity.equals("FireRoom")){
		 * FireRoomActivity }
		 */
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage("Are you sure you want to quit the game?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								setResult(RESULT_CLOSE_ALL);
								finish();
							}

						}).setNegativeButton("No", null).show();
	}

	public final static boolean isValidEmail(CharSequence target) {
		if (target == null) {
			return false;
		} else {
			return android.util.Patterns.EMAIL_ADDRESS.matcher(target)
					.matches();
		}
	}

	public void playCredits(View view) {

		Intent intent = new Intent(this, EndCreditsActivity.class);
		startActivity(intent);
	}

	private void goToUrl(String url) {
		Uri uriUrl = Uri.parse(url);
		Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
		startActivity(launchBrowser);
	}
public static final String test = "android.test.purchased";
	public void previewFireRoom(View view) {
		if (have_fire_preview_access) {
			goToUrl("http://y2u.be/QrsoxupKGF0 ");
			return;
		}

		// launch the fire preview purchase UI flow.
		// We will be notified of completion via mPurchaseFinishedListener
		Log.d(TAG, "Launching purchase flow for fire preview.");

		/*
		 * TODO: for security, generate your payload here for verification. See
		 * the comments on verifyDeveloperPayload() for more info. Since this is
		 * a SAMPLE, we just use an empty string, but on a production app you
		 * should carefully generate this.
		 */
		String payload = "";

		mHelper.launchPurchaseFlow(this, SKU_FIRE , RC_REQUEST,
				mPurchaseFinishedListener, payload);
	}

	IInAppBillingService mService;
	ServiceConnection mServiceConn;
	public void previewWaterRoom(View view) {
		if (have_water_preview_access) {
			goToUrl("http://y2u.be/B44lUfEWoes");
			return;
		}

		Log.d(TAG, "Launching purchase flow for water preview.");
		String payload = "";

		mHelper.launchPurchaseFlow(this, SKU_WATER , RC_REQUEST,
				mPurchaseFinishedListener, payload);
	}

	public void previewEarthRoom(View view) {
		if (have_earth_preview_access) {
			goToUrl("http://y2u.be/8n8Ug5TCeEE");
			return;
		}

		Log.d(TAG, "Launching purchase flow for earth preview.");
		String payload = "";

		mHelper.launchPurchaseFlow(this, SKU_EARTH , RC_REQUEST,
				mPurchaseFinishedListener, payload);
	}

	// Callback for when a purchase is finished
	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
		@Override
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
			Log.d(TAG, "Purchase finished: " + result + ", purchase: "
					+ purchase);
			Log.d(TAG, "Code: " + result.getResponse() + ", response: "
					+ IabHelper.getResponseDesc(result.getResponse()));
			// if we were disposed of in the meantime, quit.
			if (mHelper == null)
				return;

			if (result.isFailure()) {
				complain("Error purchasing: " + result);
				return;
			}

			Log.d(TAG, "Purchase successful.");
			SharedPreferences.Editor editor = preferences.edit();

			if (purchase.getSku().equals(SKU_FIRE)) {
				Log.d(TAG, "Purchase is fire preview.");
				have_fire_preview_access = true;
    			goToUrl("http://y2u.be/ufEkapSgGO8");
				editor.putBoolean(FIRE_PREVIEW, have_fire_preview_access);

			} else if (purchase.getSku().equals(SKU_PREMIUM)) {
				// bought the premium upgrade!
				Log.d(TAG, "Purchase is premium upgrade. Congratulating user.");
				alert("Thank you for upgrading to premium!");
				mAds = false;
				editor.putBoolean(ADS, mAds);
				updateAdButtonUi();
				IntentHelper.addObjectForKey(mAds, ADS);			
						} 
			
			else if (purchase.getSku().equals(SKU_WATER)) {
				// bought access to water preview
				Log.d(TAG, "Purchase is water preview.");
				have_water_preview_access = true;
    			goToUrl("http://y2u.be/ufEkapSgGO8");
				editor.putBoolean(WATER_PREVIEW, have_water_preview_access);
				// updateUi();
			} else if (purchase.getSku().equals(SKU_EARTH)) {
				// bought access to water preview
				Log.d(TAG, "Purchase is earth preview.");
				have_earth_preview_access = true;
    			goToUrl("http://y2u.be/ufEkapSgGO8");
				editor.putBoolean(EARTH_PREVIEW, have_earth_preview_access);
				// updateUi();
			}
			else if(purchase.getSku().equals(test)){
                alert("Purchase successful!You will be redirected to the preview");
				goToUrl("http://y2u.be/ufEkapSgGO8");
			}
			editor.commit();
		}

		private void updateAdButtonUi() {
			ImageButton ads = (ImageButton)findViewById(R.id.ad);
			ads.setTag("off");
			ads.setBackgroundResource(off);			
		}

	
	};

	void complain(String message) {
		Log.e(TAG, "**** TrivialDrive Error: " + message);
		alert("Error: " + message);
	}

	void alert(String message) {
		AlertDialog.Builder bld = new AlertDialog.Builder(this);
		bld.setMessage(message);
		bld.setNeutralButton("OK", null);
		Log.d(TAG, "Showing alert dialog: " + message);
		bld.create().show();
	}

	public void subscribe(String email, String name) {
		Parse.initialize(this, "g2urSEEGj6llstN6I727LdZQ6mAfogSf27gQBxgj", "q8gGKpNYv9k8rHJP8xgxu8j62i2SV3giJevaXpeO");
		ParseObject subscriber = new ParseObject("Subscriber");
  		subscriber.put("Name", name);
		subscriber.put("Email", email);
		subscriber.saveEventually();
		alert("Thank you for subscription!");
	}

	public void createForm(View view) {
		//Preparing views
	    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
	    View layout = inflater.inflate(R.layout.dialog_layout, (ViewGroup) findViewById(R.id.layout_root));
	//layout_root should be the name of the "top-level" layout node in the dialog_layout.xml file.
	    final EditText nameBox = (EditText) layout.findViewById(R.id.name_box);
	    final EditText emailBox = (EditText) layout.findViewById(R.id.email_box);

	    //Building dialog
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setView(layout);
	    builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	        	String name =  nameBox.getText().toString();
	        	String email =  emailBox.getText().toString();

	        	if(!(name.equals("")||email.equals(""))){
		        	if (isValidEmail(email)) {
		        		subscribe(email, name);
			       		}
		        	else{
		        		alert("Email you entered is not correct");
		        	}
	        	
	        	}
	        }
	    });
	    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            dialog.dismiss();
	        }
	    });
	    AlertDialog dialog = builder.create();		
	    dialog.show();
	}

	public void changebackground() {
		findViewById(R.id.transition).setVisibility(View.VISIBLE);
	}

	public void startTheGame() {
		Intent intent = new Intent(MenuActivity.this, FireRoomActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		 Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
	        if (mHelper == null) return;

	        // Pass on the activity result to the helper for handling
	        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
	            // not handled, so handle it ourselves (here's where you'd
	            // perform any handling of activity results not related to in-app
	            // billing...
	            super.onActivityResult(requestCode, resultCode, data);
	        }
	        else {
	            Log.d(TAG, "onActivityResult handled by IABUtil.");
	        }


	}
	
}
