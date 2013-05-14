package com.openxc.challenge.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.openxc.challenge.R;


public class CheckableRelativeLayout extends RelativeLayout implements Checkable  {
	private CheckBox _checkbox;
	private View _firstImageOverlay;
	private ImageView _secondImageOverlay;
	
    public CheckableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
	}
    
    @Override
    protected void onFinishInflate() {
    	super.onFinishInflate();
    	// find checked text view
    	_checkbox = (CheckBox) findViewById(R.id.checkBox1);
    }
    
    @Override 
    public boolean isChecked() { 
        return _checkbox != null ? _checkbox.isChecked() : false; 
    }
    
    @Override 
    public void setChecked(boolean checked) {
    	if (_checkbox != null) {
    		_checkbox.setChecked(checked);
    		updatedCheck();
    	}
    }
    
    @Override 
    public void toggle() { 
    	if (_checkbox != null) {
    		_checkbox.toggle();
    		updatedCheck();
    	}
    }
    
    private void updatedCheck(){
    	if (_checkbox.isChecked()){
    		
		} else{
			
		}
    }
}