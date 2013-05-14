package com.openxc.challenge;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.openxc.VehicleManager;
import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.remote.VehicleServiceException;
/**
 * @author cpark
 *
 */
public abstract class BaseActivity extends SherlockFragmentActivity implements OnItemClickListener  {

	DisplayMetrics _displayMetrics = new DisplayMetrics();
	
	private static final String[] FEATURE_LIST = new String[] {
        "Fuel Efficiency", "Map", "Speed", "Performance", "Breaks Used", "Oil Level", "BS",
        "Boobies"
    };
	
	public enum eSlideMode{
		eSlide,eNoSlide,eCustomSlide
	}
	
	ListView menuView;

	eSlideMode slidingMenuMode = eSlideMode.eSlide;

	SlidingMenu _slidingMenu;
	ListAdapter _customAdapter = null;
	int _menuWidth=0;
	LinearLayout contentLayout;


	static int ZOOM_MODE = 0, SCALE_MODE = 1, SLIDE_MODE = 2;
	int _slideMode = 0;

	private static Interpolator interp = new Interpolator() {
		@Override
		public float getInterpolation(float t) {
			t -= 1.0f;
			return t * t * t + 1.0f;
		}		
	};

	SlidingMenu.CanvasTransformer zoomTransform = new SlidingMenu.CanvasTransformer() {
		@Override
		public void transformCanvas(Canvas canvas, float percentOpen) {
			float scale = (float) (percentOpen*0.25 + 0.75);
			canvas.scale(scale, scale, canvas.getWidth()/2, canvas.getHeight()/2);
		}
	};

	SlidingMenu.CanvasTransformer scaleTransform = new SlidingMenu.CanvasTransformer() {
		@Override
		public void transformCanvas(Canvas canvas, float percentOpen) {
			canvas.scale(percentOpen, 1f, 0, 0);
		}			
	};

	SlidingMenu.CanvasTransformer slideTransform =new SlidingMenu.CanvasTransformer() {
		@Override
		public void transformCanvas(Canvas canvas, float percentOpen) {
			canvas.translate(0, canvas.getHeight()*(1-interp.getInterpolation(percentOpen)));
		}			
	};

	protected void onCreate(Bundle savedInstanceState, eSlideMode mode) {
		super.onCreate(savedInstanceState);
		slidingMenuMode = mode;
		((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(_displayMetrics);
		
		Intent intent = new Intent(this, VehicleManager.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);  
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		onCreate(savedInstanceState, eSlideMode.eSlide);
	}

	@Override
	public void setContentView(int layoutResId) {
		super.setContentView(layoutResId);
		if (isTablet()){
			_menuWidth = (int)(_displayMetrics.widthPixels * 0.3f);
		} else{
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
				_menuWidth = (int)(_displayMetrics.widthPixels * 0.5f);
			else
				_menuWidth = (int)(_displayMetrics.widthPixels * 0.8f);
		}
		setSlidingMenu();
	}
	
	public void toggleSlidingMenu(){
		_slidingMenu.toggle();
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		//if (slidingMenuMode != eSlideMode.eNoSlide)
			//_menuListFragment.getListView().setOnItemClickListener(this);
	}

	public void setSlidingMenu(){
		if (slidingMenuMode != eSlideMode.eNoSlide){
			_slidingMenu = new SlidingMenu(this);
			_slidingMenu.setMenu(R.layout.grid_menu);
			_slidingMenu.setMode(SlidingMenu.LEFT);
			_slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
			_slidingMenu.setBehindOffset(_menuWidth);
			_slidingMenu.setBehindScrollScale(0.5f);
			_slidingMenu.setFadeDegree(0.8f);
			_slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
			setSlideMode(ZOOM_MODE);
			menuView = (ListView) findViewById(R.id.menu_view);
			menuView.setAdapter(new ArrayAdapter<String>(this,
	                android.R.layout.simple_list_item_multiple_choice, FEATURE_LIST));
			menuView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			menuView.setOnItemClickListener(this);
		}
	}

	public void setSlideMode(int mode){
		_slideMode = mode;
		if (_slideMode == ZOOM_MODE){
			_slidingMenu.setBehindCanvasTransformer(zoomTransform);
		} else if (_slideMode == SLIDE_MODE){
			_slidingMenu.setBehindCanvasTransformer(slideTransform);
		} else if (_slideMode == SCALE_MODE){
			_slidingMenu.setBehindCanvasTransformer(scaleTransform);
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i("openxc", "Unbinding from vehicle service");
		unbindService(mConnection);
	}
	
	/**
	 * Might not need it but returns if the view is tablet or not.
	 */
	public boolean isTablet(){
		/*if (findViewById(R.id.tablet) == null)
			return false;*/
		return false;
	}

	public void setFragment(int res, Fragment fragment){
		getSupportFragmentManager().beginTransaction()
		.replace(res, fragment).commit();
	}
	
	private VehicleManager mVehicleManager;

	private final Handler mHandler = new Handler();


	protected ServiceConnection mConnection = new ServiceConnection() {
		// Called when the connection with the service is established
		public void onServiceConnected(ComponentName className,
				IBinder service) {
			Log.i("openxc", "Bound to VehicleManager");
			mVehicleManager = ((VehicleManager.VehicleBinder)service).getService();

			try {
				mVehicleManager.addListener(VehicleSpeed.class, mSpeedListener);
				mVehicleManager.addListener(Latitude.class,
						mLatitude);
				mVehicleManager.addListener(Longitude.class,
						mLongitude);
			} catch (VehicleServiceException e) {
				e.printStackTrace();
			} catch (UnrecognizedMeasurementTypeException e) {
				e.printStackTrace();
			}
		}

		// Called when the connection with the service disconnects unexpectedly
		public void onServiceDisconnected(ComponentName className) {
			Log.w("openxc", "VehicleService disconnected unexpectedly");
			mVehicleManager = null;
		}
	};


	VehicleSpeed.Listener mSpeedListener = new VehicleSpeed.Listener() {
		public void receive(Measurement measurement) {
			final VehicleSpeed speed = (VehicleSpeed) measurement;
			BaseActivity.this.runOnUiThread(new Runnable() {
				public void run() {
				
				}
			});
		}
	};

	Latitude.Listener mLatitude =
			new Latitude.Listener() {
		public void receive(Measurement measurement) {
			final Latitude lat = (Latitude) measurement;
			mHandler.post(new Runnable() {
				public void run() {
				}
			});
		}
	};

	Longitude.Listener mLongitude =
			new Longitude.Listener() {
		public void receive(Measurement measurement) {
			final Longitude lng = (Longitude) measurement;
			mHandler.post(new Runnable() {
				public void run() {
					//TODO : Add to Map
				}
			});
		}
	};


}