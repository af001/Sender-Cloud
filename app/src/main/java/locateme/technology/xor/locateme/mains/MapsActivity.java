package locateme.technology.xor.locateme.mains;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import locateme.technology.xor.locateme.AppController;
import locateme.technology.xor.locateme.R;
import locateme.technology.xor.locateme.dialogs.QrcodeDialog;
import locateme.technology.xor.locateme.dialogs.VerifyDialog;
import locateme.technology.xor.locateme.parse.ParseSendPush;
import locateme.technology.xor.locateme.support.AppData;
import locateme.technology.xor.locateme.support.LetterTileProvider;
import locateme.technology.xor.locateme.support.LocationProvider;
import timber.log.Timber;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        LocationProvider.LocationCallback {

    private GoogleMap mMap;
    private LocationProvider mLocationProvider;
    private SupportMapFragment mapFragment;
    private FloatingActionButton myFab;
    private ProgressBar progressBar;
    private boolean isVerified;
    private double currentLatitude;
    private double currentLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mLocationProvider = new LocationProvider(this, this);
        progressBar = (ProgressBar) findViewById(R.id.pb_loading);
        myFab = (FloatingActionButton) findViewById(R.id.myFAB);

        setUpMapIfNeeded();

        ParseUser user = ParseUser.getCurrentUser();
        isVerified = user.getBoolean("emailVerified");

        if (isVerified) {
            myFab.setEnabled(true);
            myFab.setBackgroundTintList(getResources().getColorStateList(R.color.button_color_selector));
            myFab.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ParseSendPush mSendPush = new ParseSendPush();
                    mSendPush.LocateUser();
                    mMap.clear();

                    new BackgroundTask().execute();
                    progressBar.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void CallVerifyDialog() {
        VerifyDialog vDialog = new VerifyDialog();
        vDialog.AlertUser(MapsActivity.this);
        myFab.setEnabled(false);
    }

    private void GetUserData() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
        query.whereEqualTo("trackerId", ParseUser.getCurrentUser().getObjectId());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        int counter = 0;

                        for (ParseObject object : objects) {
                            Double lat = object.getParseGeoPoint("grid").getLatitude();
                            Double lon = object.getParseGeoPoint("grid").getLongitude();

                            LatLng latLng = new LatLng(lat, lon);

                            if (lat != 0 && lon != 0) {
                                builder.include(latLng);
                                counter++;
                            }

                            AddMarker(object.getString("trackedId"), latLng);
                        }
                        if (counter > 0) {
                            if (currentLatitude != 0.0) {
                                LatLng myLocation = new LatLng(currentLatitude, currentLongitude);
                                builder.include(myLocation);
                            }
                            LatLngBounds bounds = builder.build();
                            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 300));
                        }
                    } else {
                        Toast.makeText(getBaseContext(), "No informaiton reported by device(s). Try to refresh!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Timber.e("Error getting user location data.", e.getMessage());
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void AddMarker(final String trackedId, final LatLng point) {
        // TODO: Create a query from the local datastore
        ParseQuery<ParseObject> accessList = new ParseQuery<ParseObject>("AccessList");
        accessList.whereEqualTo("trackerId", ParseUser.getCurrentUser().getObjectId());
        accessList.whereEqualTo("trackedId", trackedId);
        accessList.fromLocalDatastore();
        accessList.setLimit(1);
        accessList.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject obj : objects) {
                        if (point.latitude == 0) {
                            Toast.makeText(MapsActivity.this, "Location services for " +
                                    obj.getString("nickname") + " are disabled!", Toast.LENGTH_SHORT).show();
                        } else {
                            int tileSize = getResources().getDimensionPixelSize(R.dimen.letter_tile_size);
                            LetterTileProvider tileProvider = new LetterTileProvider(getBaseContext());
                            Bitmap letterTile = tileProvider.getLetterTile(obj.getString("nickname"), trackedId, tileSize, tileSize);

                            mMap.addMarker(new MarkerOptions()
                                    .position(point)
                                    .title(obj.getString("nickname"))
                                    .icon(BitmapDescriptorFactory.fromBitmap(letterTile)));
                        }
                    }
                }
            }
        });
    }

    private void RequestPermissionReadPhone() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{
                                Manifest.permission.READ_PHONE_STATE},
                        AppData.MY_PERMISSIONS_REQUEST_READ_PHONE);

            } else {
                Log.d("Home", "Already granted access to location.");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case AppData.MY_PERMISSIONS_REQUEST_READ_PHONE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Uri packageURI = Uri.parse("package:" + AppController.class.getPackage().getName());
                    Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
                    startActivity(uninstallIntent);
                }
                return;
            default:
                Timber.e("MapsActivity", "Permission error onRequestPermissionsResult");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }

        UiSettings settings = mMap.getUiSettings();
        settings.setMyLocationButtonEnabled(true);
        settings.setCompassEnabled(false);
        settings.setZoomControlsEnabled(true);
        settings.setAllGesturesEnabled(true);
        settings.setMapToolbarEnabled(true);

        // Find ZoomControl view
        View zoomControls = findViewById(AppData.ZOOM_CONTROL);
        View myLocationBtn = findViewById(AppData.MY_LOCATION);
        // View otherControls = findViewById(AppData.MAP_CONTROL);

        if (zoomControls != null && zoomControls.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
            // ZoomControl is inside of RelativeLayout
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) zoomControls.getLayoutParams();
            RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) myLocationBtn.getLayoutParams();
            // RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) otherControls.getLayoutParams();

            // Align it to - parent top|left
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.addRule(RelativeLayout.ALIGN_PARENT_START);

            params1.addRule(RelativeLayout.ALIGN_PARENT_END);
            //params1.addRule(RelativeLayout.ALIGN_PARENT_TOP);

            //params2.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            //params2.addRule(RelativeLayout.ALIGN_PARENT_START);

            // Update margins, set to 10dp
            final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                    getResources().getDisplayMetrics());
            params.setMargins(margin, margin, margin, margin);

            final int margin1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                    getResources().getDisplayMetrics());
            params1.setMargins(margin1, margin1, margin1, margin1);

            /*
            final int margin2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                    getResources().getDisplayMetrics());
            params2.setMargins(margin2, margin2, margin2, margin2); */
        }

        // CENTER ON THE UNITED STATES
        LatLng unitedStates = new LatLng( 38.771,-95.757);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(unitedStates, 2));
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mLocationProvider.connect();

        ParseUser user = ParseUser.getCurrentUser();
        isVerified = user.getBoolean("emailVerified");

        if (!isVerified) {
            user.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (e == null) {
                        if (!object.getBoolean("emailVerified")) {
                            CallVerifyDialog();
                        }
                    } else {
                        Timber.e("MapsActivity", "Failed to update user data.");
                    }
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationProvider.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        ParseUser user = ParseUser.getCurrentUser();
        isVerified = user.getBoolean("emailVerified");

        // HANDLE ITEM SELECTION
        switch (item.getItemId()) {
            case R.id.system_logout:
                ParseUser.logOut();
                Intent logoutIntent = new Intent(this, AppController.class);
                startActivity(logoutIntent);
                mLocationProvider.disconnect();
                finish();
                return true;
            case R.id.system_exit:
                mLocationProvider.disconnect();
                finish();
                return true;
            case R.id.show_accounts:
                if (isVerified) {
                    Intent accountsIntent = new Intent(this, AccountsActivity.class);
                    startActivity(accountsIntent);
                } else {
                    Toast.makeText(MapsActivity.this, "Please verify your email address!", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.show_barcode:
                if (isVerified) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                            QrcodeDialog mQrCode = new QrcodeDialog();
                            mQrCode.AlertUser(this);
                        } else {
                            RequestPermissionReadPhone();
                        }
                    }
                } else {
                    Toast.makeText(MapsActivity.this, "Please verify your email address!", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.show_layers:
                return true;
            case R.id.view_hybrid:
                if (mMap != null)
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.view_normal:
                if (mMap != null)
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.view_satellite:
                if (mMap != null)
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.view_terrain:
                if (mMap != null)
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void handleNewLocation(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
    }

    public class BackgroundTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            myFab.setClickable(false);
            progressBar.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in));
        }

        @Override
        protected void onPostExecute(Void result) {
            myFab.setClickable(true);
            progressBar.setVisibility(View.GONE);
            progressBar.clearAnimation();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(6000);
                GetUserData();
            } catch (InterruptedException e) {
                Timber.e("MapsActivity", "Error querying map data.");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0]);
        }
    }
}
