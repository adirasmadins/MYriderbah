package driver.dev.asliborneo.app.myridebah.Service;



        import android.content.Intent;

        import com.google.android.gms.maps.model.LatLng;
        import com.google.firebase.messaging.FirebaseMessagingService;
        import com.google.firebase.messaging.RemoteMessage;
        import com.google.gson.Gson;

        import driver.dev.asliborneo.app.myridebah.CustomerCall;


public class MyFirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        LatLng Customer_location=new Gson().fromJson(remoteMessage.getNotification().getBody(),LatLng.class);
        Intent intent=new Intent(getBaseContext(), CustomerCall.class);
        intent.putExtra("lat",Customer_location.latitude);
        intent.putExtra("lng",Customer_location.longitude);
        intent.putExtra("customer",remoteMessage.getNotification().getTitle());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
