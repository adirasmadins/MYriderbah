package driver.dev.asliborneo.app.myridebah.Common;

import android.location.Location;

import driver.dev.asliborneo.app.myridebah.Model.User;
import driver.dev.asliborneo.app.myridebah.Remote.IGoogleAPI;
import driver.dev.asliborneo.app.myridebah.Remote.RetrofitClient;


public class commons {
    public static String Current_Token;
    public static final String driver_location="Drivers";
    public static final String Registered_driver="DriverInformation";
    public static final String Registered_Riders="RidersInformation";
    public static final String Pickup_Request="PickUpRequest";
    public static double base_fare=2.55;
    private static double time_rate=0.35;
    private static double distance_rate=1.75;
    public static User current_user=null;
    public static final java.lang.String user_field="usr";
    public static final String password_field="pwd";

    public static double price_formula(double km,double min){
        return base_fare+(distance_rate*km)+(time_rate*min);
    }

    public static IGoogleAPI getGoogleAPI()
    {
        return RetrofitClient.get_direction_client().create(IGoogleAPI.class);
    }
}