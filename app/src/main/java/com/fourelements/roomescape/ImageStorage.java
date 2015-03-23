package com.fourelements.roomescape;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;

import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;


public class ImageStorage {
	Resources res;
	String folder;
	DisplayMetrics metrics;
	public ImageStorage(Resources res,DisplayMetrics metrics)
	{
		this.res = res;
		this.metrics = metrics;
		folder = getFolder();
	}
	public String getFolder()
	{
		String folder = "";
		int dpi = res.getDisplayMetrics().densityDpi;

		int widthPixels = metrics.widthPixels;
		int heightPixels = metrics.heightPixels;

		float scaleFactor = metrics.density;
		float widthDp = widthPixels / scaleFactor;
		float heightDp = heightPixels / scaleFactor;

		float smallestWidth = Math.min(widthDp, heightDp);

		if (smallestWidth > 720 && dpi == DisplayMetrics.DENSITY_MEDIUM) {
			folder = "drawable-xhdpi/";
		} 
		else if (dpi == DisplayMetrics.DENSITY_XHIGH||dpi == DisplayMetrics.DENSITY_TV||dpi == DisplayMetrics.DENSITY_XXHIGH) {
			//xhdpi
			folder = "drawable-xhdpi/";
		}
		else if((smallestWidth > 480 && dpi == DisplayMetrics.DENSITY_MEDIUM)|| dpi == DisplayMetrics.DENSITY_HIGH)
		{
			//hdpi
			folder = "drawable-hdpi/";

		}
		else if(dpi == DisplayMetrics.DENSITY_MEDIUM)
		{
			//mdpi
			folder = "drawable-mdpi/";
		}
		else if(dpi == DisplayMetrics.DENSITY_LOW){
			//ldpi
			folder = "drawable-ldpi/";
		}
		return folder;
	}
public BitmapDrawable getDrawable(Context ctx, String imageName){
	String path ="expansion/"+folder+imageName+".png";
	ZipResourceFile zip;
	BitmapFactory.Options bfo = new BitmapFactory.Options();
	//bfo.inDensity = DisplayMetrics.DENSITY_XHIGH;
	//bfo.inTargetDensity =res.getDisplayMetrics().densityDpi;
	bfo.inPreferredConfig = Bitmap.Config.ARGB_8888;

	Bitmap b = null;
	try {
		zip = APKExpansionSupport.getAPKExpansionZipFile(ctx,7, -1);
		InputStream is = zip.getInputStream(path);
		if(is == null){
			path ="expansion/"+folder+imageName+".jpg";
			is = zip.getInputStream(path);
		}
		b = BitmapFactory.decodeStream(is, null, bfo);
		}
	catch(IOException e){
		e.printStackTrace();}
	 final BitmapDrawable d = new BitmapDrawable(b);
     d.setTargetDensity(res.getDisplayMetrics());
	 return d;
	}
}