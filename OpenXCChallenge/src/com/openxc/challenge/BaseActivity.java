package com.openxc.challenge;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

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
public abstract class BaseActivity extends FragmentActivity implements OnItemClickListener  {

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

	ListAdapter _customAdapter = null;
	int _menuWidth=0;
	LinearLayout contentLayout;


	static int ZOOM_MODE = 0, SCALE_MODE = 1, SLIDE_MODE = 2;
	int _slideMode = 0;

	
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
	}
	

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		//if (slidingMenuMode != eSlideMode.eNoSlide)
			//_menuListFragment.getListView().setOnItemClickListener(this);
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