package com.paul.clujpubs;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity {

    private static final String TAG = "MapActivity";

    public static final int MODE_IDLE = 0;
    public static final int MODE_SEARCH = 1;
    private FloatingActionButton mFabButton;
    private ImageView mSearchButton;
    private EditText mNewEditText;

    private TextView mTitleText;

    public int mCurrentMode = 0;

    private Fragment mapFragment;
    private GoogleMap mMap;
    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private Marker mCurrLocationMarker;
    private Location mLastLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapFragment = getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mFabButton = (FloatingActionButton) findViewById(R.id.fab_button);
        mSearchButton = (ImageView) findViewById(R.id.search_button);
        mNewEditText = (EditText) findViewById(R.id.name_edit_text);
        mTitleText = (TextView) findViewById(R.id.title_text);
    }

    @Override
    protected void onResume() {
        mapFragment.onResume();

        mSearchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (mCurrentMode) {
                    case MODE_IDLE:
                        mCurrentMode = MODE_SEARCH;
                        mSearchButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_close_white_24dp));
                        mNewEditText.setVisibility(View.VISIBLE);
                        mTitleText.setVisibility(View.GONE);
                        showKeyboard(mNewEditText);
                        break;
                    case MODE_SEARCH:
                        mCurrentMode = MODE_IDLE;
                        mNewEditText.setText("");
                        mSearchButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_search_white_24dp));
                        mTitleText.setVisibility(View.VISIBLE);
                        mNewEditText.setVisibility(View.GONE);
                        hideKeyboard();
                        break;
                }
            }
        });
        mFabButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onLocationChanged(mLastLocation);
            }


        });
    }

    public void showKeyboard(View view) {
        // TODO Auto-generated method stub
        InputMethodManager keyboard = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.showSoftInput(view, 0);
    }

    public void hideKeyboard() {

        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (LocationListener) this);
        }
    }


}
