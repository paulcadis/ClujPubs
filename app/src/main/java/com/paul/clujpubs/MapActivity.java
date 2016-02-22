package com.paul.clujpubs;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.paul.clujpubs.fragment.MapFragment;
import com.skobbler.ngx.SKCoordinate;

public class MapActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MapActivity";

    public static final int MODE_IDLE = 0;
    public static final int MODE_SEARCH = 1;
    private FloatingActionButton mFabButton;
    private ImageView mSearchButton;
    private EditText mNewEditText;

    private TextView mTitleText;

    public int mCurrentMode = 0;

    private MapFragment mapFragment;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private boolean mIsConnected = false;

    private CoordinatorLayout mCoordinatorLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mFabButton = (FloatingActionButton) findViewById(R.id.fab_button);
        mSearchButton = (ImageView) findViewById(R.id.search_button);
        mNewEditText = (EditText) findViewById(R.id.name_edit_text);
        mTitleText = (TextView) findViewById(R.id.title_text);

    }

    @Override
    protected void onResume() {
        super.onResume();
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
                if (mIsConnected) {
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                            mGoogleApiClient);
                    if (mapFragment!=null && mLastLocation != null) {
                        mapFragment.onLocationChanged(new SKCoordinate(mLastLocation.getLongitude(),mLastLocation.getLatitude()));
                    } else {
                        Snackbar.make(mCoordinatorLayout,"No locatoin available", Snackbar.LENGTH_LONG).show();
                    }
                }

            }


        });
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();

    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
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

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: ");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        mIsConnected = true;
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: ");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: ");
    }
}
