package com.openxc.challenge;

import org.achartengine.chartdemo.demo.chart.ScatterChart;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.openxc.VehicleManager;
import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.remote.VehicleServiceException;

public class MainActivity extends Activity {

	private VehicleManager mVehicleManager;
	static private TextView mVehicleSpeedView;
	ScatterChart sChart = new ScatterChart();
	private ProgressBar aClock;
	private final Handler mHandler = new Handler();
	

	private ServiceConnection mConnection = new ServiceConnection() {
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
	        MainActivity.this.runOnUiThread(new Runnable() {
	            public void run() {
	                mVehicleSpeedView.setText(
	                    "Vehicle speed (km/h): " + speed.getValue().doubleValue());
	                aClock.setProgress(speed.getValue().intValue());
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mVehicleSpeedView = (TextView)findViewById(R.id.textView1);
		aClock = (ProgressBar)findViewById(R.id.progressBar1);
		
		Intent intent = new Intent(this, VehicleManager.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);  
	}
	
	public void onPause() {
	    super.onPause();
	    Log.i("openxc", "Unbinding from vehicle service");
	    unbindService(mConnection);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
