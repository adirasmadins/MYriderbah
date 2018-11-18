package driver.dev.asliborneo.app.myridebah;
import android.*;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import driver.dev.asliborneo.app.myridebah.Common.commons;
import driver.dev.asliborneo.app.myridebah.Model.Token;
import io.paperdb.Paper;
import driver.dev.asliborneo.app.myridebah.Model.Token;

public class Driver_Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,OnMapReadyCallback, com.google.android.gms.location.LocationListener,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {
    private GoogleMap mMap;
    private LocationRequest location_request;
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    public static Location mlastlocation;
    DatabaseReference drivers;
    GeoFire geoFire;
    Marker mcurrent;
    MaterialAnimatedSwitch location_switch;
    SupportMapFragment mapFragment;
    int PICK_IMAGE_REQUEST=9999;
    private PlaceAutocompleteFragment autocompleteFragment;
    AutocompleteFilter typefilter;
    private String destination;
    private GoogleApiClient mGoogleapiclient;
    private DatabaseReference onlineref,currentuserref;
    FirebaseStorage storage;
    StorageReference storageReference;
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
        View navigation_header_view=navigationView.getHeaderView(0);
        CircleImageView avatar=(CircleImageView) navigation_header_view.findViewById(R.id.avatar);
        TextView name=(TextView) navigation_header_view.findViewById(R.id.driver_name);
        TextView rating=(TextView) navigation_header_view.findViewById(R.id.rating);
        rating.setText(commons.current_user.getRates());
        name.setText(commons.current_user.getName());
        if (!TextUtils.isEmpty(commons.current_user.getAvatarurl())){
            Picasso.with(Driver_Home.this).load(commons.current_user.getAvatarurl()).into(avatar);
        }
        storage=FirebaseStorage.getInstance();
        storageReference=storage.getReference();
        onlineref= FirebaseDatabase.getInstance().getReference().child(".info/connected");
        currentuserref=FirebaseDatabase.getInstance().getReference("Drivers").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        onlineref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentuserref.onDisconnect().removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.placetxt);
        location_switch = (MaterialAnimatedSwitch) findViewById(R.id.location_switch);
        typefilter=new AutocompleteFilter.Builder()
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
                    Snackbar.make(mapFragment.getView(), "You are Online", Snackbar.LENGTH_SHORT).show();
                }else
                {
                    FirebaseDatabase.getInstance().goOffline();
                    if(mcurrent!=null)
                        mcurrent.remove();
                    // mMap.clear();
                    // handler.removeCallbacks(drawrunnable);
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
                if(location_switch.isChecked()){
                    destination=place.getAddress().toString();
                    destination=destination.replace(" ","+");
                }else{
                    Toast.makeText(Driver_Home.this,"Please Change your status to Online",Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onError(Status status) {

            }
        });
        update_firebase_token();
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
        FirebaseDatabase db=FirebaseDatabase.getInstance();
        DatabaseReference tokens=db.getReference("Tokens");
        Token token=new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (location_switch.isChecked())
                        display_location();
                }
        }
    }
    private void setuplocation() {
        if (ActivityCompat.checkSelfPermission(Driver_Home.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Driver_Home.this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);

        } else {
            if (location_switch.isChecked())
                display_location();
        }
    }
    private void stop_location_updates() {
        if (ActivityCompat.checkSelfPermission(Driver_Home.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // locationManager.removeUpdates(this);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleapiclient,this);
    }
    private void display_location() {
        if (ActivityCompat.checkSelfPermission(Driver_Home.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                        mcurrent=mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),15.0f));
                        // rotate_marker(mcurrent, -360, mMap);*/
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
        mGoogleapiclient=new GoogleApiClient.Builder(Driver_Home.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleapiclient.connect();
    }
    private void start_location_update() {
        if (ActivityCompat.checkSelfPermission(Driver_Home.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
            boolean issucess = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(Driver_Home.this, R.raw.uber_style_map));
            if (!issucess)
                Toast.makeText(Driver_Home.this, "Error setting Map Style", Toast.LENGTH_LONG).show();
        }catch(Resources.NotFoundException ex){ex.printStackTrace();}
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);

    }
    @Override
    public void onLocationChanged(Location location) {
        mlastlocation=location;
        display_location();
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }
    @Override
    public void onConnectionSuspended(int i) {

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
        AlertDialog.Builder update_profile_dialog=new AlertDialog.Builder(Driver_Home.this);
        update_profile_dialog.setTitle("Update Profile");
        update_profile_dialog.setMessage("Please Fill all Information");
        View v=LayoutInflater.from(Driver_Home.this).inflate(R.layout.update_profile_layout,null);
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
                final AlertDialog waiting_dialog=new SpotsDialog(Driver_Home.this);
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
                                Toast.makeText(Driver_Home.this,"Name and Phone are updated",Toast.LENGTH_LONG).show();
                            }else{
                                waiting_dialog.dismiss();
                                Toast.makeText(Driver_Home.this,"Update Failed",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(Driver_Home.this,"Please Provide Required Information",Toast.LENGTH_LONG).show();
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
                final ProgressDialog dialog=new ProgressDialog(Driver_Home.this);
                dialog.setMessage("Uploading.....");
                dialog.setCancelable(false);
                dialog.show();
                String image_id= UUID.randomUUID().toString();
                final StorageReference image_folder=storageReference.child("images/"+image_id);
                image_folder.putFile(saveuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        dialog.dismiss();
                        Toast.makeText(Driver_Home.this,"Uploaded!",Toast.LENGTH_LONG).show();
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
                                            Toast.makeText(Driver_Home.this,"Uploaded!",Toast.LENGTH_LONG).show();
                                        }else{
                                            Toast.makeText(Driver_Home.this,"Upload Failed",Toast.LENGTH_LONG).show();
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
                        Toast.makeText(Driver_Home.this,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    private void show_change_password_dialog() {
        AlertDialog.Builder change_password_dialog=new AlertDialog.Builder(Driver_Home.this);
        change_password_dialog.setTitle("Change Password");
        change_password_dialog.setMessage("Please fill all information");
        View v=LayoutInflater.from(Driver_Home.this).inflate(R.layout.change_password_layout,null);
        final MaterialEditText new_password=(MaterialEditText)v.findViewById(R.id.new_password_txt);
        final MaterialEditText old_password=(MaterialEditText)v.findViewById(R.id.old_password_txt);
        final MaterialEditText repeat_new_password=(MaterialEditText)v.findViewById(R.id.repeat_new_password_txt);
        change_password_dialog.setView(v);
        change_password_dialog.setPositiveButton("Change Password", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final AlertDialog waiting_dialog=new SpotsDialog(Driver_Home.this);
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
                                                        Toast.makeText(Driver_Home.this,"Password has changed",Toast.LENGTH_LONG).show();
                                                    }else{
                                                        waiting_dialog.dismiss();
                                                        Toast.makeText(Driver_Home.this,"Password was cchanged but not updated in Database",Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });

                                        }else{
                                            waiting_dialog.dismiss();
                                            Toast.makeText(Driver_Home.this,"Password has not Changed due to some Error",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }else{
                                waiting_dialog.dismiss();
                                Toast.makeText(Driver_Home.this,"Old Password is incorrect",Toast.LENGTH_LONG).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

                }else{
                    waiting_dialog.dismiss();
                    Toast.makeText(Driver_Home.this,"Passwords do not match",Toast.LENGTH_LONG).show();

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
        Intent intent=new Intent(Driver_Home.this,MainActivity.class);
        startActivity(intent);
        finish();

    }
}
