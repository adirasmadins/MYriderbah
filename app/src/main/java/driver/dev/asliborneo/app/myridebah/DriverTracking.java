package driver.dev.asliborneo.app.myridebah;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

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

import static driver.dev.asliborneo.app.myridebah.Driver_Home.mlastlocation;

public class DriverTracking extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks,LocationListener {
    private GoogleMap mMap;
    double rider_lat,rider_lng;
    LocationRequest location_request;
    GoogleApiClient mGoogleapiclient;
    private Circle rider_marker;
    private Marker driver_marker;
    private com.google.android.gms.maps.model.Polyline direction;
    GeoFire geoFire;
    Button start_trip_btn;
    Location pick_up_location;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        start_trip_btn=(Button) findViewById(R.id.start_trip);
        if (getIntent()!=null){
            rider_lat=getIntent().getDoubleExtra("lat",-1.0);
            rider_lng=getIntent().getDoubleExtra("lng",-1.0);
        }
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
        Call<Directions> call=service.getPath("driving","less_driving" ,mlastlocation.getLatitude()+","+mlastlocation.getLongitude(),pick_up_location.getLatitude()+","+pick_up_location.getLongitude(),getResources().getString(R.string.google_direction_api));
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
        if (ActivityCompat.checkSelfPermission(DriverTracking.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // locationManager.removeUpdates(this);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleapiclient,this);
    }
    private void display_location() {
        if (ActivityCompat.checkSelfPermission(DriverTracking.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mlastlocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleapiclient);
        if (mlastlocation != null) {

            final double longitude = mlastlocation.getLongitude();
            final double latitude = mlastlocation.getLatitude();
            if(driver_marker!=null)
                driver_marker.remove();
            driver_marker=mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),17.0f));
            if(direction!=null)
                direction.remove();
            getDirection();
        }
    }

    private void getDirection() {
        new DownloadTask().execute(getDirectionsUrl());
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
    private void start_location_update() {
        if (ActivityCompat.checkSelfPermission(DriverTracking.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleapiclient,location_request,this);
        // locationManager.requestLocationUpdates(Provider,20000,0,this);
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap=googleMap;
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
        geoFire=new GeoFire(FirebaseDatabase.getInstance().getReference("Drivers"));
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
        Notification notification=new Notification(String.format("Arrived","Usman"),"Driver has arrived at your door");

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
        String api_key="key=AIzaSyCKslRY-6A406ZWayqKbNDO_t5FLXRAHns";
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
    class DownloadTask extends AsyncTask<String,Void,String> {
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