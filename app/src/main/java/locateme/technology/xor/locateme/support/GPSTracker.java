package locateme.technology.xor.locateme.support;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class GPSTracker extends Service implements LocationListener {

    private final Context mContext;

    // FLAG FOR GPS STATUS
    boolean isGPSEnabled = false;

    // FLAG FOR NETWORK STATUS
    boolean isNetworkEnabled = false;

    // FLAG FOR GPS STATUS
    boolean canGetLocation = false;

    Location location;
    double latitude;
    double longitude;

    // THE MINIMUM DISTANCE TO CHANGE UPDATES - IN METERS
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;

    // THE MINIMUM TIME BETWEEN UPDATES IN MILLISECONDS
    private static final long MIN_TIME_BW_UPDATES = 10000;

    // DECLARING A LOCATION MANAGER
    protected LocationManager locationManager;

    /**
     * CONSTRUCTOR
     * @param context
     */
    public GPSTracker(Context context) {
        this.mContext = context;
        getLocation();
    }

    /**
     * METHOD TO CHECK THE STATUS OF LOCATION SERVICES
     * @return
     */
    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // GET GPS STATUS
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // GET NETWORK STATUS
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // NO NETWORK PROVIDERS AVAILABLE
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // IF GPS IS ENABLED GET THE LAT/LONG USING GPS SERVICES
                if (isGPSEnabled) {
                    if (location != null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    /**
     * METHOD TO STOP RECEIVING GPS UPDATES - SAVES BATTERY!!
     */
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * METHOD TO GET THE LATITUDE OF THE PHONE
     * @return
     */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }

        return latitude;
    }

    /**
     * METHOD TO GET THE LONGITUDE OF THE PHONE
     * @return
     */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        return longitude;
    }

    /**
     * METHOD TO CHECK IF THE GPS OR NETWORK PROVIDE ARE ENABLED FOR GETTING LOCATION DATA
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    @Override
    public void onLocationChanged(Location loc) {
        latitude = loc.getLatitude();
        longitude = loc.getLongitude();
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}