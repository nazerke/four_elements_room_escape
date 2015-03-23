package com.fourelements.roomescape;



import android.content.Context;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;


public class CustomView extends RelativeLayout{
	private LayoutInflater inflater;
	private AttributeSet attSet;
	public CustomView(Context context) {
		super(context);
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		addView(inflater.inflate(R.layout.common_views_scrollable, null));
	}
	 public CustomView(Context context, AttributeSet attrs) {
	        super(context, attrs);
	        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			addView(inflater.inflate(R.layout.common_views_scrollable, null));
			attSet = attrs;
	      
	    }

	    public CustomView(Context context, AttributeSet attrs, int defStyle) {
	        super(context, attrs, defStyle);
	        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			addView(inflater.inflate(R.layout.common_views_scrollable, null));
			attSet = attrs;
	        
	    }
	    public AttributeSet getAttributeSet()
	    {
	    	return attSet;
	    }


}
