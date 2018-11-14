package driver.dev.asliborneo.app.myridebah.Remote;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import driver.dev.asliborneo.app.myridebah.Model.fcm_response;
import driver.dev.asliborneo.app.myridebah.Model.sender;



public interface FCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAWFxABLU:APA91bF3konUlV_RsgGXoBHR3zmWU80Qx1MYItypY9lanHTbLDWBBlYaMThLoIDkahNSWgGasoScuZm9JxsNIf6kQN6FW0hWDCBdIhud5c95IvbL3M3Je26z3TN4I2AOdAKNxP8wePWI"
    })
    @POST("fcm/send")
    Call<fcm_response> send_message(@Body sender body);
}
