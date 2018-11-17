package driver.dev.asliborneo.app.myridebah;

import android.*;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.android.SphericalUtil;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import driver.dev.asliborneo.app.myridebah.Common.commons;
import driver.dev.asliborneo.app.myridebah.Remote.IGoogleAPI;
import io.paperdb.Paper;
import driver.dev.asliborneo.app.myridebah.Model.Token;
import retrofit2.Call;
import retrofit2.Response;

public class DriverHome extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,OnMapReadyCallback, com.google.android.gms.location.LocationListener,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static GoogleMap mMap;
    private LocationRequest location_request;
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    public static Location mlastlocation;
    DatabaseReference drivers;
    GeoFire geoFire;
    boolean mPermissionDenied = false;
    Marker mcurrent;
    MaterialAnimatedSwitch location_switch;
    SupportMapFragment mapFragment;
    int PICK_IMAGE_REQUEST = 9999;
    private PlaceAutocompleteFragment autocompleteFragment;
    AutocompleteFilter typefilter;
    PlaceAutocompleteFragment places;
    LatLng pickup_location;
    Marker carMarker;
    String pick_up_location, destination_location;
    Marker pick_up_location_marker, destination_location_marker;
    private String destination;
    private GoogleApiClient mGoogleapiclient;
    private DatabaseReference onlineref, currentuserref;
    FirebaseStorage storage;
    StorageReference storageReference;
    private Handler handler;
    private LatLng startPosition, endPosition, currentPosition;
    private int index, next;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private com.google.android.gms.maps.model.Polyline blackPolyline;
    private com.google.android.gms.maps.model.Polyline greyPolyline;
    private IGoogleAPI mService;
    private List<LatLng> polyLineList;
    private float v;
    private double lat, lng;


    Runnable drawPathRunnable = new Runnable() {
        @Override
        public void run() {
            if (index < polyLineList.size() - 1) {
                index++;
                next = index + 1;
            }
            if (index < polyLineList.size() - 1) {
                startPosition = polyLineList.get(index);
                endPosition = polyLineList.get(next);
            }

            final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    v = valueAnimator.getAnimatedFraction();
                    lng = v * endPosition.longitude + (1 - v) * startPosition.longitude;
                    lat = v * endPosition.latitude + (1 - v) * startPosition.latitude;

                    LatLng newPos = new LatLng(lat, lng);
                    mcurrent.setAnchor(0.5f, 0.5f);
                    mcurrent.setRotation(getBearing(startPosition, newPos));
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

        if (startPosition.latitude < endPosition.latitude && startPosition.longitude < endPosition.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (startPosition.latitude >= endPosition.latitude && startPosition.longitude < endPosition.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (startPosition.latitude >= endPosition.latitude && startPosition.longitude >= endPosition.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (startPosition.latitude < endPosition.latitude && startPosition.longitude >= endPosition.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        View navigation_header_view = navigationView.getHeaderView(0);
        CircleImageView avatar = (CircleImageView) navigation_header_view.findViewById(R.id.avatar);
        TextView name = (TextView) navigation_header_view.findViewById(R.id.driver_name);
        TextView email = (TextView) navigation_header_view.findViewById(R.id.emailtxt);
        TextView rating = (TextView) navigation_header_view.findViewById(R.id.rating);
        if (commons.current_user != null) {
            rating.setText(commons.current_user.getRates());
            name.setText(commons.current_user.getName());
            if (!TextUtils.isEmpty(commons.current_user.getAvatarurl())) {
                Picasso.with(DriverHome.this).load(commons.current_user.getAvatarurl()).into(avatar);
            }
        }

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        onlineref = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        currentuserref = FirebaseDatabase.getInstance().getReference("Drivers").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        onlineref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentuserref.onDisconnect().removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mService = commons.getGoogleAPI();

        handler = new Handler();
        polyLineList = new ArrayList<>();
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.placetxt);
        location_switch = findViewById(R.id.location_switch);
        typefilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(3)
                .build();
        drivers = FirebaseDatabase.getInstance().getReference("Drivers");
        init_googleapiclient();
        init_location_request();
        location_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline) {
                if (isOnline) {
                    FirebaseDatabase.getInstance().goOnline();
                    start_location_update();
                    display_location();
                    mMap.setMyLocationEnabled(true);

                    Snackbar.make(mapFragment.getView(), "You are Online", Snackbar.LENGTH_SHORT).show();
                } else {
                    FirebaseDatabase.getInstance().goOffline();
                    if (mcurrent != null)
                        mcurrent.remove();

                    mMap.setMyLocationEnabled(false);
                    handler.removeCallbacks(drawPathRunnable);
                    stop_location_updates();
                    Snackbar.make(mapFragment.getView(), "You are Offline", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        geoFire = new GeoFire(drivers);
        // setuplocation();
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mMap.clear();
                if (location_switch.isChecked()) {

                    destination = place.getAddress().toString();
                    destination = destination.replace(" ", "+");
                    mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)).position(new LatLng(lat,lng)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));

              /*      pickup_location = place.getLatLng();
                    mlastlocation.setLatitude(place.getLatLng().latitude);
                    mlastlocation.setLongitude(place.getLatLng().longitude);
                    pick_up_location = place.getAddress().toString();
                    pick_up_location_marker = mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("search area..").icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));*/


                    Log.d("DESTINATION", destination);
                    getDirectionsUrl();

                } else {
                    Toast.makeText(DriverHome.this, "Please Change your status to Online", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onError(Status status) {

            }
        });
        /*places=(PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mMap.clear();
                pickup_location=place.getLatLng();
                mlastlocation.setLatitude(place.getLatLng().latitude);
                mlastlocation.setLongitude(place.getLatLng().longitude);
                pick_up_location=place.getAddress().toString();
                pick_up_location_marker=mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("Pick Up Here").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),15.0f));


            }

            @Override
            public void onError(Status status) {

            }
        });*/
        update_firebase_token();
        setuplocation();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void update_firebase_token() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
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


    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, MY_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }



    private String getDirectionsUrl() {

        // Origin of route
        String str_origin = "origin=" + mlastlocation.getLatitude() + "," + mlastlocation.getLongitude();

        // Destination of route
        String str_dest = "destination=" + lat + "," + lng;

        // Sensor enabled
        String api_key= getString(R.string.google_direction_api);
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
    private void getDirection() {
        currentPosition = new LatLng(mlastlocation.getLatitude(),mlastlocation.getLongitude());


        try {
            new DownloadTask().execute(getDirectionsUrl());
            mService = new IGoogleAPI() {
                @Override
                public Call<Directions> getPath(String mode, String transit_routing_preference, String origin, String destination, String key) {
                    return null;
                }

                @Override
                public Call<String> getDirection(String url) {
                    return null;
                }

                @Override
                        public void onResponse(Call<String> call, Response<String> response) {

                            {
                                try
                                {
                                    JSONObject jsonObject = new JSONObject(response.body());
                                    JSONArray jsonArray = jsonObject.getJSONArray("routes");

                                    for(int i=0;i<jsonArray.length();i++)
                                    {
                                        JSONObject route = jsonArray.getJSONObject(i);
                                        JSONObject poly = route.getJSONObject("overview_polyline");
                                        String polyline = poly.getString("points");
                                        polyLineList = decodePoly(polyline);
                                    }
                                    if (response.body() != null) {
                                        LatLngBounds.Builder builder =   new LatLngBounds.Builder();
                                        for (LatLng latLng:polyLineList)
                                            builder.include(latLng);
                                        LatLngBounds bounds = builder.build();
                                        CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,2);
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
                                            .position(polyLineList.get(polyLineList.size()-1))
                                            .title("Pickup Location"));

                                    //Animation
                                    ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0,100);
                                    polyLineAnimator.setDuration(2000);
                                    polyLineAnimator.setInterpolator(new LinearInterpolator());
                                    polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                        @Override
                                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                            List<LatLng>points = greyPolyline.getPoints();
                                            int percentValue = (int)valueAnimator.getAnimatedValue();
                                            int size = points.size();
                                            int newPoints = (int)(size*(percentValue/100.0f));
                                            List<LatLng> p = points.subList(0,newPoints);
                                            blackPolyline.setPoints(p);
                                        }
                                    });
                                    polyLineAnimator.start();

                                    carMarker = mMap.addMarker(new MarkerOptions().position(currentPosition)
                                            .flat(true)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));


                                    handler = new Handler();
                                    index = -1;
                                    next = 1;
                                    handler.postDelayed(drawPathRunnable, 3000);

                                }catch (JSONException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {

                        }
                    };

        }catch (Exception e)
        {
            e.printStackTrace();
        }
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


    private void setuplocation() {
        if (ActivityCompat.checkSelfPermission(DriverHome.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverHome.this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);

        } else {
            if (location_switch.isChecked())
                display_location();
        }
    }
    private void stop_location_updates() {
        if (ActivityCompat.checkSelfPermission(DriverHome.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // locationManager.removeUpdates(this);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleapiclient,this);
    }
    private void display_location() {
        if (ActivityCompat.checkSelfPermission(DriverHome.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mlastlocation= LocationServices.FusedLocationApi.getLastLocation(mGoogleapiclient);
        if (mlastlocation != null) {
            if (location_switch.isChecked()) {
                final double longitude = mlastlocation.getLongitude();
                final double latitude = mlastlocation.getLatitude();
                LatLng center=new LatLng(mlastlocation.getLatitude(),mlastlocation.getLongitude());
                LatLng northside= SphericalUtil.computeOffset(center,100000,0);
                LatLng southside= SphericalUtil.computeOffset(center,100000,180);
                LatLngBounds bounds=LatLngBounds.builder()
                        .include(northside)
                        .include(southside)
                        .build();
                autocompleteFragment.setBoundsBias(bounds);
                autocompleteFragment.setFilter(typefilter);

                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if (mcurrent != null) {
                            mcurrent.remove();
                        }
                        mcurrent=mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.img_login1)));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),15.0f));
                         rotate_marker(mcurrent, 360, mMap);
                    }
                });
            }
        }
    }
    private void init_location_request(){
        location_request=new LocationRequest();
        location_request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        location_request.setSmallestDisplacement(10);
        location_request.setFastestInterval(3000);
        location_request.setInterval(5000);

    }
    private void init_googleapiclient(){
        mGoogleapiclient=new GoogleApiClient.Builder(DriverHome.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleapiclient.connect();
    }
    private void start_location_update() {
        if (ActivityCompat.checkSelfPermission(DriverHome.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleapiclient,location_request,this);
        // locationManager.requestLocationUpdates(Provider,20000,0,this);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap=googleMap;
        try {
            boolean issucess = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(DriverHome.this, R.raw.uber_style_map));
            if (!issucess)
                Toast.makeText(DriverHome.this, "Error setting Map Style", Toast.LENGTH_LONG).show();
        }catch(Resources.NotFoundException ex){ex.printStackTrace();}
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(5.9804, 116.0735), 15));

    }
    @Override
    public void onLocationChanged(Location location) {
        mlastlocation=location;
        display_location();
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        init_location_request();
        display_location();
    }
    @Override
    public void onConnectionSuspended(int i) {
    mGoogleapiclient.connect();

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("location_error",connectionResult.getErrorMessage());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.driver_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_Trip_history) {

        } else if (id == R.id.nav_way_bill) {

        } else if (id == R.id.nav_help) {

        } else if (id == R.id.nav_setting) {

        } else if (id == R.id.nav_signout) {
            Sign_Out();

        }else if(id== R.id.nav_change_password){
            show_change_password_dialog();
        } else if (id==R.id.nav_update_profile) {
            show_update_profile_dialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void show_update_profile_dialog() {
        AlertDialog.Builder update_profile_dialog=new AlertDialog.Builder(DriverHome.this);
        update_profile_dialog.setTitle("Update Profile");
        update_profile_dialog.setMessage("Please Fill all Information");
        View v=LayoutInflater.from(DriverHome.this).inflate(R.layout.update_profile_layout,null);
        final MaterialEditText name=(MaterialEditText)v.findViewById(R.id.nametxt);
        final MaterialEditText phone=(MaterialEditText)v.findViewById(R.id.phonetxt);
        ImageView image_upload=(ImageView)v.findViewById(R.id.image_upload);
        image_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choose_image();
            }
        });
        update_profile_dialog.setView(v);
        update_profile_dialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final AlertDialog waiting_dialog=new SpotsDialog(DriverHome.this);
                waiting_dialog.show();
                Map<String,Object> namephoneupdate=new HashMap<>();
                if(!TextUtils.isEmpty(name.getText().toString())&&!TextUtils.isEmpty(phone.getText().toString())) {
                    namephoneupdate.put("name", name.getText().toString());
                    namephoneupdate.put("phone", phone.getText().toString());
                    DatabaseReference driver_information_reference=FirebaseDatabase.getInstance().getReference("DriverInformation");
                    driver_information_reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(namephoneupdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                waiting_dialog.dismiss();
                                Toast.makeText(DriverHome.this,"Name and Phone are updated",Toast.LENGTH_LONG).show();
                            }else{
                                waiting_dialog.dismiss();
                                Toast.makeText(DriverHome.this,"Update Failed",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(DriverHome.this,"Please Provide Required Information",Toast.LENGTH_LONG).show();
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();

    }

    private void choose_image() {
        Intent intent =new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture for Profile"),PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==PICK_IMAGE_REQUEST&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null) {
            Uri saveuri=data.getData();
            if(saveuri!=null){
                final ProgressDialog dialog=new ProgressDialog(DriverHome.this);
                dialog.setMessage("Uploading.....");
                dialog.setCancelable(false);
                dialog.show();
                String image_id= UUID.randomUUID().toString();
                final StorageReference image_folder=storageReference.child("images/"+image_id);
                image_folder.putFile(saveuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        dialog.dismiss();
                        Toast.makeText(DriverHome.this,"Uploaded!",Toast.LENGTH_LONG).show();
                        image_folder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Map<String,Object> avatar_update=new HashMap<>();
                                avatar_update.put("avatarurl",uri.toString());
                                DatabaseReference driver_information_reference=FirebaseDatabase.getInstance().getReference("DriverInformation");
                                driver_information_reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(avatar_update).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(DriverHome.this,"Uploaded!",Toast.LENGTH_LONG).show();
                                        }else{
                                            Toast.makeText(DriverHome.this,"Upload Failed",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress=(100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                        dialog.setMessage("Uploading "+progress+"%");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DriverHome.this,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }



    private void show_change_password_dialog() {
        AlertDialog.Builder change_password_dialog=new AlertDialog.Builder(DriverHome.this);
        change_password_dialog.setTitle("Change Password");
        change_password_dialog.setMessage("Please fill all information");
        View v=LayoutInflater.from(DriverHome.this).inflate(R.layout.change_password_layout,null);
        final MaterialEditText new_password=(MaterialEditText)v.findViewById(R.id.new_password_txt);
        final MaterialEditText old_password=(MaterialEditText)v.findViewById(R.id.old_password_txt);
        final MaterialEditText repeat_new_password=(MaterialEditText)v.findViewById(R.id.repeat_new_password_txt);
        change_password_dialog.setView(v);
        change_password_dialog.setPositiveButton("Change Password", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final AlertDialog waiting_dialog=new SpotsDialog(DriverHome.this);
                waiting_dialog.show();
                if(new_password.getText().toString().equals(repeat_new_password.getText().toString())){
                    String email=FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    AuthCredential credinal= EmailAuthProvider.getCredential(email,old_password.getText().toString());
                    FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credinal).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                FirebaseAuth.getInstance().getCurrentUser().updatePassword(repeat_new_password.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Map<String,Object> password=new HashMap<>();
                                            password.put("password",repeat_new_password.getText().toString());
                                            DatabaseReference driver_information_reference=FirebaseDatabase.getInstance().getReference("DriverInformation");
                                            driver_information_reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        waiting_dialog.dismiss();
                                                        Toast.makeText(DriverHome.this,"Password has changed",Toast.LENGTH_LONG).show();
                                                    }else{
                                                        waiting_dialog.dismiss();
                                                        Toast.makeText(DriverHome.this,"Password was cchanged but not updated in Database",Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });

                                        }else{
                                            waiting_dialog.dismiss();
                                            Toast.makeText(DriverHome.this,"Password has not Changed due to some Error",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }else{
                                waiting_dialog.dismiss();
                                Toast.makeText(DriverHome.this,"Old Password is incorrect",Toast.LENGTH_LONG).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

                }else{
                    waiting_dialog.dismiss();
                    Toast.makeText(DriverHome.this,"Passwords do not match",Toast.LENGTH_LONG).show();

                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    private void Sign_Out() {
        Paper.init(this);
        Paper.book().destroy();
        FirebaseAuth.getInstance().signOut();
        Intent intent=new Intent(DriverHome.this,MainActivity.class);
        startActivity(intent);
        finish();

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
}
