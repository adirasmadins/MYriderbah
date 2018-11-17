package driver.dev.asliborneo.app.myridebah;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import driver.dev.asliborneo.app.myridebah.Common.commons;
import driver.dev.asliborneo.app.myridebah.Remote.FCMService;
import driver.dev.asliborneo.app.myridebah.Remote.IGoogleAPI;
import driver.dev.asliborneo.app.myridebah.Remote.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import driver.dev.asliborneo.app.myridebah.Model.Notification;
import driver.dev.asliborneo.app.myridebah.Model.Token;
import driver.dev.asliborneo.app.myridebah.Model.fcm_response;
import driver.dev.asliborneo.app.myridebah.Model.sender;

import static driver.dev.asliborneo.app.myridebah.DriverHome.mlastlocation;

public class DriverTracking extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks,LocationListener,
        GoogleMap.OnMyLocationButtonClickListener,GoogleMap.OnMyLocationClickListener {
    private static final int MY_PERMISSION_REQUEST_CODE = 1;
    private static GoogleMap mMap;
    double rider_lat,rider_lng;
    LocationRequest location_request;
    GoogleApiClient mGoogleapiclient;
    Circle rider_marker;
    private Marker driver_marker;
    private com.google.android.gms.maps.model.Polyline direction;
    GeoFire geoFire;
    Button start_trip_btn;
    boolean mPermissionDenied = false;
    Location pick_up_location;
    private LatLng startPosition,endPosition,currentPosition;
    private int index,next;
    private PolylineOptions polylineOptions,blackPolylineOptions;
    private com.google.android.gms.maps.model.Polyline blackPolyline;
    private com.google.android.gms.maps.model.Polyline greyPolyline;
    private IGoogleAPI mService;
    private List<LatLng> polyLineList;
    private float v;
    Handler handler;
    private double lat,lng;


    Runnable drawPathRunnable = new Runnable() {
        @Override
        public void run() {
            if (index < polyLineList.size() - 1) {
                index++;
                next = index+1;
            }
            if(index < polyLineList.size()-1)
            {
                startPosition = polyLineList.get(index);
                endPosition  = polyLineList.get(next);
            }

            final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0,1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    v = valueAnimator.getAnimatedFraction();
                    lng = v*endPosition.longitude+(1-v)*startPosition.longitude;
                    lat = v*endPosition.latitude+(1-v)*startPosition.latitude;

                    LatLng newPos = new LatLng(lat,lng);
                    driver_marker.setAnchor(0.5f,0.5f);
                    driver_marker.setRotation(getBearing(startPosition,newPos));
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(newPos)
                                    .zoom(15.5f)
                                    .build()
                    ));
                }
            });

        }
    };

    private float getBearing(LatLng startPosition, LatLng endPosition) {
        double lat = Math.abs(startPosition.latitude - endPosition.latitude);
        double lng = Math.abs(startPosition.longitude - endPosition.longitude);

        if(startPosition.latitude < endPosition.latitude && startPosition.longitude<endPosition.longitude)
            return (float)(Math.toDegrees(Math.atan(lng/lat)));
        else if(startPosition.latitude >= endPosition.latitude && startPosition.longitude < endPosition.longitude)
            return (float)((90-Math.toDegrees(Math.atan(lng/lat)))+90);
        else if(startPosition.latitude >= endPosition.latitude && startPosition.longitude >= endPosition.longitude)
            return (float)(Math.toDegrees(Math.atan(lng/lat))+180);
        else if(startPosition.latitude < endPosition.latitude && startPosition.longitude >= endPosition.longitude)
            return (float)((90-Math.toDegrees(Math.atan(lng/lat)))+270);
        return -1;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(DriverTracking.this);
        start_trip_btn=(Button) findViewById(R.id.start_trip);
        if (getIntent()!=null){
            rider_lat=getIntent().getDoubleExtra("lat",-1.0);
            rider_lng=getIntent().getDoubleExtra("lng",-1.0);
        }

        mService = commons.getGoogleAPI();
        init_googleapiclient();
        init_location_request();
        start_trip_btn=(Button) findViewById(R.id.start_trip);
        start_trip_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(start_trip_btn.getText().toString().equals("Start Trip")){
                    pick_up_location=mlastlocation;
                    start_trip_btn.setText("Drop off Here");
                }else  if(start_trip_btn.getText().toString().equals("Drop off Here")){
                    Calculate_cash_fee(pick_up_location,mlastlocation);
                }
            }
        });
        // geoFire=new GeoFire();
    }

    private void Calculate_cash_fee(final Location pick_up_location, final Location mlastlocation) {
        IGoogleAPI service=RetrofitClient.get_direction_client().create(IGoogleAPI.class);
        Call<Directions> call=service.getPath("driving","less_driving" ,mlastlocation.getLatitude()+","+mlastlocation.getLongitude(),rider_lat + "," + rider_lng,"AIzaSyCw0musjvyG7sFkZf0QeVCCeUPK3TEztIE");
        call.enqueue(new Callback<Directions>() {
            @Override
            public void onResponse(Call<Directions> call, Response<Directions> response) {
                if(response.body()!=null){
                    if( response.body().routes.size()>0)
                        if(response.body().routes.get(0).legs.size()>0) {
                            String distance_text=response.body().routes.get(0).legs.get(0).distance.text;
                            Double distance_value=Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+",""));
                            //  response.body().routes.get(0).legs.get(0).start_address;
                            String time_text= response.body().routes.get(0).legs.get(0).duration.text;
                            Double time_value=Double.parseDouble(time_text.replaceAll("[^0-9\\\\.]+",""));
                            send_dropoff_notification(getIntent().getStringExtra("customer"));
                            Intent intent=new Intent(DriverTracking.this,TripDetail.class);
                            intent.putExtra("start_address",response.body().routes.get(0).legs.get(0).start_address);
                            intent.putExtra("Time",String.valueOf(time_value));
                            intent.putExtra("Distance",String.valueOf(distance_value));
                            intent.putExtra("end_address",response.body().routes.get(0).legs.get(0).end_address);
                            intent.putExtra("Total",commons.price_formula(distance_value,time_value));
                            intent.putExtra("Location_start",String.format("%f,%f",pick_up_location.getLatitude(),pick_up_location.getLongitude()));
                            intent.putExtra("Location_end",String.format("%f,%f",mlastlocation.getLatitude(),mlastlocation.getLongitude()));
                            startActivity(intent);
                            finish();

                        }
                }else{
                }
            }

            @Override
            public void onFailure(Call<Directions> call, Throwable t) {
                Log.e("direction_error",t.getMessage());
            }
        });
    }

    private void stop_location_updates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // locationManager.removeUpdates(this);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleapiclient,DriverTracking.this);
    }
    private void display_location() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mlastlocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleapiclient);
        if (mlastlocation != null) {
            final double longitude = mlastlocation.getLongitude();
            final double latitude = mlastlocation.getLatitude();
            LatLng center=new LatLng(mlastlocation.getLatitude(),mlastlocation.getLongitude());
            LatLng northside= SphericalUtil.computeOffset(center,100000,0);
            LatLng southside= SphericalUtil.computeOffset(center,100000,180);
            LatLngBounds bounds=LatLngBounds.builder()
                    .include(northside)
                    .include(southside)
                    .build();

            if(driver_marker!=null)
                driver_marker.remove();
            driver_marker=mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(rider_lat,rider_lng),17.0f));
            if(direction!=null)
                direction.remove();
            getDirection();
        }
    }

    private void getDirection() {
        new DownloadTask().execute(getDirectionsUrl());
        currentPosition = new LatLng(mlastlocation.getLatitude(), mlastlocation.getLongitude());


        mService.getPath("driving", "less_driving", mlastlocation.getLatitude() + "," + mlastlocation.getLongitude(), rider_lat + "," + rider_lng, "AIzaSyCz--LqThFx6f-C2KBVleSOv0J9-vcc-nY")
                .enqueue(new Callback<Directions>() {
                    @Override
                    public void onResponse(Call<Directions> call, Response<Directions> response) {

                        {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray jsonArray = jsonObject.getJSONArray("routes");

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject route = jsonArray.getJSONObject(i);
                                    JSONObject poly = route.getJSONObject("overview_polyline");
                                    String polyline = poly.getString("points");
                                    polyLineList = decodePoly(polyline);
                                }
                                if (response.body() != null) {
                                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                    for (LatLng latLng : polyLineList)
                                        builder.include(latLng);
                                    LatLngBounds bounds = builder.build();
                                    CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
                                    mMap.animateCamera(mCameraUpdate);
                                }


                                polylineOptions = new PolylineOptions();
                                polylineOptions.color(Color.BLUE);
                                polylineOptions.width(5);
                                polylineOptions.startCap(new SquareCap());
                                polylineOptions.endCap(new SquareCap());
                                polylineOptions.jointType(JointType.ROUND);
                                polylineOptions.addAll(polyLineList);
                                greyPolyline = mMap.addPolyline(polylineOptions);

                                blackPolylineOptions = new PolylineOptions();
                                blackPolylineOptions.color(Color.BLACK);
                                blackPolylineOptions.width(5);
                                blackPolylineOptions.startCap(new SquareCap());
                                blackPolylineOptions.endCap(new SquareCap());
                                blackPolylineOptions.jointType(JointType.ROUND);
                                greyPolyline = mMap.addPolyline(blackPolylineOptions);

                                mMap.addMarker(new MarkerOptions()
                                        .position(polyLineList.get(polyLineList.size() - 1))
                                        .title("Pickup Location"));

                                //Animation
                                ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0, 100);
                                polyLineAnimator.setDuration(2000);
                                polyLineAnimator.setInterpolator(new LinearInterpolator());
                                polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                        List<LatLng> points = greyPolyline.getPoints();
                                        int percentValue = (int) valueAnimator.getAnimatedValue();
                                        int size = points.size();
                                        int newPoints = (int) (size * (percentValue / 100.0f));
                                        List<LatLng> p = points.subList(0, newPoints);
                                        blackPolyline.setPoints(p);
                                    }
                                });
                                polyLineAnimator.start();

                                driver_marker = mMap.addMarker(new MarkerOptions().position(currentPosition)
                                        .flat(true)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));


                                handler = new Handler();
                                index = -1;
                                next = 1;
                                handler.postDelayed(drawPathRunnable, 3000);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Directions> call, Throwable t) {

                    }
                });
    }

    private void rotate_marker(final Marker mcurrent, final float i, GoogleMap mMap) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float start_rotation = mcurrent.getRotation();
        final long duration = 1500;
        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elasped = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elasped / duration);
                float rot = t * i + (1 - t) * start_rotation;
                mcurrent.setRotation(-rot > 180 ? rot / 2 : rot);
                if (t < 1.0)
                    handler.postDelayed(this, 16);
            }
        });
    }
    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    private void init_location_request(){
        location_request=new LocationRequest();
        location_request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        location_request.setSmallestDisplacement(10);
        location_request.setFastestInterval(3000);
        location_request.setInterval(5000);

    }
    private void init_googleapiclient(){
        mGoogleapiclient=new GoogleApiClient.Builder(DriverTracking.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleapiclient.connect();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != MY_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }
    private void start_location_update() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleapiclient,location_request,this);
        // locationManager.requestLocationUpdates(Provider,20000,0,this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap=googleMap;
        enableMyLocation();
        try {
            boolean issucess = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(DriverTracking.this, R.raw.uber_style_map));
            if (!issucess)
                Toast.makeText(DriverTracking.this, "Error setting Map Style", Toast.LENGTH_LONG).show();
        }catch(Resources.NotFoundException ex){ex.printStackTrace();}
        rider_marker=mMap.addCircle(new CircleOptions()
                .center(new LatLng(rider_lat,rider_lng))
                .radius(50)
                .strokeColor(Color.BLUE).fillColor(0x220000FF)
                .strokeWidth(5.0f));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
                .position(new LatLng(rider_lat,rider_lng))
                .title("Pick up here"));
        geoFire=new GeoFire(FirebaseDatabase.getInstance().getReference(commons.driver_location));
        GeoQuery geoQuery=geoFire.queryAtLocation(new GeoLocation(rider_lat,rider_lng),0.05f);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                send_arrived_notification(getIntent().getStringExtra("customer"));
                start_trip_btn.setEnabled(true);
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void send_arrived_notification(String customer_id) {
        Token token=new Token(customer_id);
        Notification notification=new Notification(String.format("Arrived",direction),"Driver has arrived at your door");

        sender sender=new sender(notification,token.getToken());
        FCMService service=RetrofitClient.getClient().create(FCMService.class);
        Call<fcm_response> call=service.send_message(sender);
        call.enqueue(new Callback<fcm_response>() {
            @Override
            public void onResponse(Call<fcm_response> call, Response<fcm_response> response) {
                if(response.body() !=null)
                    if(response.body().success!=1){
                        Toast.makeText(DriverTracking.this,"Failed",Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(DriverTracking.this,"Success",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(DriverTracking.this,TripDetail.class);
                        startActivity(intent);
                        finish();
                    }
                Log.e("arrival_notification",response.toString());
            }

            @Override
            public void onFailure(Call<fcm_response> call, Throwable t) {
                Log.e("fcm_problem",t.toString());
            }
        });
    }
    private void send_dropoff_notification(String customer_id) {
        Token token=new Token(customer_id);
        Notification notification=new Notification("Drop Off",customer_id);

        sender sender=new sender(notification,token.getToken());
        FCMService service=RetrofitClient.getClient().create(FCMService.class);
        Call<fcm_response> call=service.send_message(sender);
        call.enqueue(new Callback<fcm_response>() {
            @Override
            public void onResponse(Call<fcm_response> call, Response<fcm_response> response) {
                if(response.body() !=null)
                    if(response.body().success!=1){
                        Toast.makeText(DriverTracking.this,"Failed",Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(DriverTracking.this,"Success",Toast.LENGTH_LONG).show();
                    }
                Log.e("arrival_notification",response.toString());
            }

            @Override
            public void onFailure(Call<fcm_response> call, Throwable t) {
                Log.e("fcm_problem",t.toString());
            }
        });
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        display_location();
        start_location_update();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleapiclient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("location_error",connectionResult.getErrorMessage());
    }

    @Override
    public void onLocationChanged(Location location) {
        mlastlocation=location;
        display_location();
    }
    private String getDirectionsUrl() {

        // Origin of route
        String str_origin = "origin=" + mlastlocation.getLatitude() + "," + mlastlocation.getLongitude();

        // Destination of route
        String str_dest = "destination=" + rider_lat + "," + rider_lng;

        // Sensor enabled
        String api_key="key=AIzaSyCw0musjvyG7sFkZf0QeVCCeUPK3TEztIE";
        String transit_routing_preference="transit_routing_preference=less_driving";
        String mode = "mode=driving";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + transit_routing_preference + "&" + mode+ "&" +api_key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/json?"+parameters;

        return url;


    }
    static class DownloadTask extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask(mMap);
            parserTask.execute(result);

        }

        private String downloadUrl(String strUrl) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            if (mlastlocation !=null)
                try {
                    URL url = new URL(strUrl);

                    urlConnection = (HttpURLConnection) url.openConnection();

                    urlConnection.connect();

                    iStream = urlConnection.getInputStream();

                    BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                    StringBuffer sb = new StringBuffer();

                    String line = "";
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }

                    data = sb.toString();

                    br.close();

                } catch (Exception e) {
                    Log.d("Exception", e.toString());
                } finally {
                    iStream.close();
                    urlConnection.disconnect();
                }
            return data;
        }
    }
}

