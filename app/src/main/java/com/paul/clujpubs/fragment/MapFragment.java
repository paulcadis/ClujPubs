package com.paul.clujpubs.fragment;

        import android.app.Activity;
        import android.content.pm.ActivityInfo;
        import android.graphics.Bitmap;
        import android.graphics.drawable.BitmapDrawable;
        import android.hardware.Sensor;
        import android.hardware.SensorEvent;
        import android.hardware.SensorEventListener;
        import android.hardware.SensorManager;
        import android.os.Bundle;
        import android.support.annotation.Nullable;
        import android.support.v4.app.ActionBarDrawerToggle;
        import android.support.v4.app.Fragment;
        import android.support.v4.view.GravityCompat;
        import android.support.v4.widget.DrawerLayout;
        import android.view.LayoutInflater;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.AdapterView;
        import android.widget.ImageView;
        import android.widget.LinearLayout;
        import android.widget.ListView;
        import android.widget.Toast;

        import com.paul.clujpubs.MapActivity;
        import com.paul.clujpubs.R;
        import com.skobbler.ngx.SKCoordinate;
        import com.skobbler.ngx.map.SKAnimationSettings;
        import com.skobbler.ngx.map.SKAnnotation;
        import com.skobbler.ngx.map.SKBoundingBox;
        import com.skobbler.ngx.map.SKCoordinateRegion;
        import com.skobbler.ngx.map.SKMapCustomPOI;
        import com.skobbler.ngx.map.SKMapPOI;
        import com.skobbler.ngx.map.SKMapSurfaceListener;
        import com.skobbler.ngx.map.SKMapSurfaceView;
        import com.skobbler.ngx.map.SKMapViewHolder;
        import com.skobbler.ngx.map.SKPOICluster;
        import com.skobbler.ngx.map.SKScreenPoint;
        import com.skobbler.ngx.map.realreach.SKRealReachListener;
        import com.skobbler.ngx.navigation.SKNavigationListener;
        import com.skobbler.ngx.navigation.SKNavigationState;
        import com.skobbler.ngx.poitracker.SKDetectedPOI;
        import com.skobbler.ngx.poitracker.SKPOITrackerListener;
        import com.skobbler.ngx.poitracker.SKTrackablePOIType;
        import com.skobbler.ngx.positioner.SKCurrentPositionListener;
        import com.skobbler.ngx.positioner.SKCurrentPositionProvider;
        import com.skobbler.ngx.positioner.SKPosition;
        import com.skobbler.ngx.positioner.SKPositionerManager;
        import com.skobbler.ngx.routing.SKRouteInfo;
        import com.skobbler.ngx.routing.SKRouteJsonAnswer;
        import com.skobbler.ngx.routing.SKRouteListener;
        import com.skobbler.ngx.sdktools.navigationui.SKToolsNavigationListener;
        import com.skobbler.ngx.versioning.SKMapUpdateListener;
        import com.skobbler.ngx.versioning.SKVersioningManager;

        import java.util.ArrayList;
        import java.util.LinkedHashMap;
        import java.util.List;

/**
 * Created by Filip Tudic on 20-May-15.
 */
public class MapFragment extends Fragment implements SKMapSurfaceListener, SKRouteListener, SKNavigationListener,
        SKRealReachListener, SKPOITrackerListener, SKCurrentPositionListener, SensorEventListener,
        SKMapUpdateListener, SKToolsNavigationListener {

    /**
     * time, in milliseconds, from the moment when the application receives new
     * GPS values
     */
    private static final int MINIMUM_TIME_UNTILL_MAP_CAN_BE_UPDATED = 30;

    /**
     * defines how smooth the movement will be (1 is no smoothing and 0 is never
     * updating).
     */
    private static final float SMOOTH_FACTOR_COMPASS = 0.1f;

    private static int runningInstances = 0;

    /**
     * the current value of the z axis ; at each new step it is updated with the
     * new value
     */
    private float currentCompassValue;

    /**
     * last time when received GPS signal
     */
    private long lastTimeWhenReceivedGpsSignal;

    /**
     * Current position provider
     */
    private SKCurrentPositionProvider currentPositionProvider;

    /**
     * Current position
     */
    private SKPosition currentPosition;

    /**
     * the view that holds the map view
     */
    private SKMapViewHolder mapViewGroup;

    /**
     * Surface view for displaying the map
     */
    private SKMapSurfaceView mapView;
    /**
     * menu items
     */
//    private LinkedHashMap<TestingOption, MenuDrawerItem> menuItems;
    /**
     * menu items values
     */
//    private ArrayList<MenuDrawerItem> list;

    /**
     * Drawer layout object
     */
    private DrawerLayout drawerLayout;

    /**
     * The navigation drawer
     */
    private ListView drawerList;

    /**
     * Action bar toggle
     */
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private LinearLayout debugBaseLayout;

    /**
     * the values returned by magnetic sensor
     */
    private float[] orientationValues;

    /**
     * the latest exact screen orientation (given by the
     * getExactScreenOrientation method) that was recorded
     */
    private int lastExactScreenOrientation = -1;

    private SKAnnotation testAnnotation;

    private MapActivity activity;

    private View view;

    public enum TestingOption {
        TESTING_OPTION, ANNOTATION_OPTION, MAP_VIEW_SETTINGS_OPTION, MAP_CACHE_OPTION, LAST_RENDERED_FRAME_OPTION, ANIMATION_CUSTOM_VIEW_OPTION, BOUNDING_BOX_OPTION, INTERNALIZATION_OPTION, ANIMATE_OPTION, MAP_STYLE_OPTION, SCALE_VIEW_OPTION, CALLOUT_VIEW_OPTION,
        ROUTING_OPTION, MAP_VERSION_OPTION, OVERLAYS_OPTION, POSITION_LOGGING_OPTION, POI_TRACKER
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_map,container);
        activity =(MapActivity) getActivity();
        //app = (DemoApplication) getApplication();

        currentPositionProvider = new SKCurrentPositionProvider(activity);
        currentPositionProvider.setCurrentPositionListener(this);
        currentPositionProvider.requestLocationUpdates(true, true, false);

        mapViewGroup = (SKMapViewHolder) view.findViewById(R.id.view_group_map);
        mapViewGroup.setMapSurfaceListener(this);
//        LayoutInflater inflater = (LayoutInflater) getService(Context.LAYOUT_INFLATER_SERVICE);
//        mapPopup = mapViewGroup.getCalloutView();
//        View view = inflater.inflate(R.layout.layout_popup, null);
//        popupTitleView = (TextView) view.findViewById(R.id.top_text);
//        popupDescriptionView = (TextView) view.findViewById(R.id.bottom_text);
//        mapPopup.setCustomView(view);

        SKVersioningManager.getInstance().setMapUpdateListener(this);
        return view;
    }



    public void addTestAnnotationAtPosition(SKCoordinate position) {
        if (testAnnotation == null) {
            testAnnotation = new SKAnnotation(155000);
            testAnnotation.setLocation(position);
            testAnnotation.setMininumZoomLevel(3);
            testAnnotation.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_GREEN);
            mapView.addAnnotation(testAnnotation, SKAnimationSettings.ANIMATION_NONE);
        } else {
            testAnnotation.setLocation(position);
            mapView.updateAnnotation(testAnnotation);
        }
    }

    private void updateTestAnnotation() {
        if (testAnnotation != null) {
            if (testAnnotation.getAnnotationType() == SKAnnotation.SK_ANNOTATION_TYPE_RED) {
                testAnnotation.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_GREEN);
            } else {
                testAnnotation.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_RED);
            }
            mapView.updateAnnotation(testAnnotation);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapViewGroup.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapViewGroup.onPause();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onCurrentPositionUpdate(SKPosition position) {
        this.currentPosition = position;
        SKPositionerManager.getInstance().reportNewGPSPosition(this.currentPosition);
    }

    @Override
    public void onActionPan() {

    }

    @Override
    public void onActionZoom() {

    }

    @Override
    public void onSurfaceCreated(SKMapViewHolder mapHolder) {
        View chessBackground = view.findViewById(R.id.chess_board_background);
        chessBackground.setVisibility(View.GONE);

        mapView = mapHolder.getMapSurfaceView();
//        applySettingsOnMapView();
        startOrientationSensor();
//        if(SplashActivity.newMapVersionDetected != 0){
//            showUpdateDialog(SplashActivity.newMapVersionDetected);
//        }

    }

    @Override
    public void onMapRegionChanged(SKCoordinateRegion skCoordinateRegion) {
    }

    @Override
    public void onMapRegionChangeStarted(SKCoordinateRegion skCoordinateRegion) {

    }

    @Override
    public void onMapRegionChangeEnded(SKCoordinateRegion skCoordinateRegion) {

    }

    @Override
    public void onDoubleTap(SKScreenPoint skScreenPoint) {

    }

    @Override
    public void onSingleTap(SKScreenPoint screenPoint) {
    }

    @Override
    public void onRotateMap() {

    }

    @Override
    public void onLongPress(SKScreenPoint skScreenPoint) {
    }


    @Override
    public void onInternetConnectionNeeded() {

    }

    @Override
    public void onMapActionDown(SKScreenPoint skScreenPoint) {

    }

    @Override
    public void onMapActionUp(SKScreenPoint skScreenPoint) {

    }

    @Override
    public void onPOIClusterSelected(SKPOICluster skpoiCluster) {

    }

    @Override
    public void onMapPOISelected(SKMapPOI skMapPOI) {

    }

    @Override
    public void onAnnotationSelected(SKAnnotation skAnnotation) {
        if (skAnnotation == testAnnotation) {
            updateTestAnnotation();
        }
    }

    @Override
    public void onCustomPOISelected(SKMapCustomPOI skMapCustomPOI) {

    }

    @Override
    public void onCompassSelected() {

    }

    @Override
    public void onCurrentPositionSelected() {

    }

    @Override
    public void onObjectSelected(int i) {

    }


    @Override
    public void onInternationalisationCalled(int i) {

    }

    @Override
    public void onBoundingBoxImageRendered(int i) {

    }

    @Override
    public void onGLInitializationError(String s) {

    }

    @Override
    public void onScreenshotReady(final Bitmap bitmap) {

    }

    @Override
    public void onDestinationReached() {

    }

    @Override
    public void onSignalNewAdviceWithInstruction(String s) {

    }

    @Override
    public void onSignalNewAdviceWithAudioFiles(String[] strings, boolean b) {

    }

    @Override
    public void onSpeedExceededWithAudioFiles(String[] strings, boolean b) {

    }

    @Override
    public void onSpeedExceededWithInstruction(String s, boolean b) {

    }

    @Override
    public void onUpdateNavigationState(SKNavigationState skNavigationState) {

    }

    @Override
    public void onReRoutingStarted() {

    }

    @Override
    public void onFreeDriveUpdated(String countryCode, String streetName, String referenceName, SKNavigationState.SKStreetType streetType,
                                   double currentSpeed, double speedLimit) {

    }

    @Override
    public void onViaPointReached(int i) {

    }

    @Override
    public void onVisualAdviceChanged(boolean b, boolean b1, SKNavigationState skNavigationState) {

    }

    @Override
    public void onTunnelEvent(boolean b) {

    }

    @Override
    public void onRealReachCalculationCompleted(SKBoundingBox skBoundingBox) {

    }

    @Override
    public void onRouteCalculationCompleted(SKRouteInfo skRouteInfo) {

    }

    @Override
    public void onRouteCalculationFailed(SKRoutingErrorCode skRoutingErrorCode) {

    }

    @Override
    public void onAllRoutesCompleted() {

    }

    @Override
    public void onServerLikeRouteCalculationCompleted(SKRouteJsonAnswer skRouteJsonAnswer) {

    }

    @Override
    public void onOnlineRouteComputationHanging(int i) {

    }

    @Override
    public void onNavigationEnded() {

    }

    @Override
    public void onRouteCalculationStarted() {

    }

    @Override
    public void onRouteCalculationCompleted() {

    }

    @Override
    public void onRouteCalculationCanceled() {

    }

    @Override
    public void onNavigationStarted() {

    }

    @Override
    public void onNewVersionDetected(int i) {

    }

    @Override
    public void onMapVersionSet(int i) {

    }

    @Override
    public void onVersionFileDownloadTimeout() {

    }

    @Override
    public void onNoNewVersionDetected() {

    }

    @Override
    public void onUpdatePOIsInRadius(double v, double v1, int i) {

    }

    @Override
    public void onReceivedPOIs(SKTrackablePOIType skTrackablePOIType, List<SKDetectedPOI> list) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle  other action bar items...
        return super.onOptionsItemSelected(item);
    }

    /**
     * Customize the map view
     */
    private void applySettingsOnMapView() {
        mapView.getMapSettings().setMapRotationEnabled(true);
        mapView.getMapSettings().setMapZoomingEnabled(true);
        mapView.getMapSettings().setMapPanningEnabled(true);
        mapView.getMapSettings().setZoomWithAnchorEnabled(true);
        mapView.getMapSettings().setInertiaRotatingEnabled(true);
        mapView.getMapSettings().setInertiaZoomingEnabled(true);
        mapView.getMapSettings().setInertiaPanningEnabled(true);
    }

    /**
     * list view click listener
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /**
     * handles the click on menu items
     *
     * @param position
     */
    public void selectItem(int position) {
        drawerList.setItemChecked(position, true);
        if (this.drawerLayout.isDrawerOpen(this.drawerList)) {
            drawerLayout.closeDrawer(drawerList);
        }
//        this.handleMenuItemClick(list.get(position).getMapOption());
    }

    public SKPosition getCurrentPosition() {
        return currentPosition;
    }

    public SKMapSurfaceView getMapView() {
        return mapView;
    }

    public SKMapViewHolder getMapHolder() {
        return mapViewGroup;
    }



    /**
     * Activates the orientation sensor
     */
    private void startOrientationSensor() {
        orientationValues = new float[3];
        SensorManager sensorManager = (SensorManager) activity.getSystemService(Activity.SENSOR_SERVICE);
        Sensor orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorManager.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_UI);
    }

    /**
     * Deactivates the orientation sensor
     */
    private void stopOrientationSensor() {
        orientationValues = null;
        SensorManager sensorManager = (SensorManager) activity.getSystemService(Activity.SENSOR_SERVICE);
        sensorManager.unregisterListener(this);
    }

    /**
     * @param newCompassValue new z value returned by the sensors
     */
    private void applySmoothAlgorithm(float newCompassValue) {
        if (Math.abs(newCompassValue - currentCompassValue) < 180) {
            currentCompassValue = currentCompassValue + SMOOTH_FACTOR_COMPASS * (newCompassValue - currentCompassValue);
        } else {
            if (currentCompassValue > newCompassValue) {
                currentCompassValue = (currentCompassValue + SMOOTH_FACTOR_COMPASS * ((360 + newCompassValue - currentCompassValue) % 360) + 360) % 360;
            } else {
                currentCompassValue = (currentCompassValue - SMOOTH_FACTOR_COMPASS * ((360 - newCompassValue + currentCompassValue) % 360) + 360) % 360;
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
//        //mapView.reportNewHeading(t.values[0]);
//        switch (event.sensor.getType()) {
//
//            case Sensor.TYPE_ORIENTATION:
//                if (orientationValues != null) {
//                    for (int i = 0; i < orientationValues.length; i++) {
//                        orientationValues[i] = event.values[i];
//
//                    }
//                    if (orientationValues[0] != 0) {
//                        if ((System.currentTimeMillis() - lastTimeWhenReceivedGpsSignal) > MINIMUM_TIME_UNTILL_MAP_CAN_BE_UPDATED) {
//                            applySmoothAlgorithm(orientationValues[0]);
//                            int currentExactScreenOrientation = DebugKitUtils.getExactScreenOrientation(this);
//                            if (lastExactScreenOrientation != currentExactScreenOrientation) {
//                                lastExactScreenOrientation = currentExactScreenOrientation;
//                                switch (lastExactScreenOrientation) {
//                                    case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
//                                        mapView.reportNewDeviceOrientation(SKMapSurfaceView.SKOrientationType.PORTRAIT);
//                                        break;
//                                    case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
//                                        mapView.reportNewDeviceOrientation(SKMapSurfaceView.SKOrientationType.PORTRAIT_UPSIDEDOWN);
//                                        break;
//                                    case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
//                                        mapView.reportNewDeviceOrientation(SKMapSurfaceView.SKOrientationType.LANDSCAPE_RIGHT);
//                                        break;
//                                    case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
//                                        mapView.reportNewDeviceOrientation(SKMapSurfaceView.SKOrientationType.LANDSCAPE_LEFT);
//                                        break;
//                                }
//                            }
//
//                            // report to NG the new value
//                            if (orientationValues[0] < 0) {
//                                mapView.reportNewHeading(-orientationValues[0]);
//                            } else {
//                                mapView.reportNewHeading(orientationValues[0]);
//                            }
//
//                            lastTimeWhenReceivedGpsSignal = System.currentTimeMillis();
//                        }
//                    }
//                }
//                break;
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
