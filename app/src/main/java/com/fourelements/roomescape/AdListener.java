package com.fourelements.roomescape;

import android.util.Log;

import com.millennialmedia.android.MMAd;
import com.millennialmedia.android.MMException;
import com.millennialmedia.android.MMSDK;
import com.millennialmedia.android.RequestListener;

/** Demonstrates methods to implement as part of the MMAdListener interface */
public class AdListener implements RequestListener
{
	@Override
	public void MMAdOverlayLaunched(MMAd mmAd)
	{
		

		Log.i(MMSDK.SDKLOG, "Millennial Media Ad (" + mmAd.getApid() + ") overlay launched");
	}

	@Override
	public void MMAdRequestIsCaching(MMAd mmAd)
	{
		Log.i(MMSDK.SDKLOG, "Millennial Media Ad (" + mmAd.getApid() + ") caching started");
	}

	@Override
	public void requestCompleted(MMAd mmAd)
	{
		Log.i(MMSDK.SDKLOG,"Millennial Media Ad (" + mmAd.getApid() + ") request succeeded");
	}

	@Override
	public void requestFailed(MMAd mmAd, MMException exception)
	{
		Log.i(MMSDK.SDKLOG,String.format("Millennial Media Ad (" + mmAd.getApid() + ") request failed with error: %d %s.", exception.getCode(), exception.getMessage()));
	}

	@Override
	public void MMAdOverlayClosed(MMAd mmAd)
	{
		Log.i(MMSDK.SDKLOG, "Millennial Media Ad (" + mmAd.getApid() + ") overlay closed");

	}
	@Override
	public void onSingleTap(MMAd mmAd)
	{
		Log.i(MMSDK.SDKLOG, "Millennial Media Ad (" + mmAd.getApid() + ") single tap");
	}
}
