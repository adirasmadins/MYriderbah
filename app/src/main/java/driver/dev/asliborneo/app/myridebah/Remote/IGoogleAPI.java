package driver.dev.asliborneo.app.myridebah.Remote;

import com.google.android.gms.common.api.GoogleApiClient;

import driver.dev.asliborneo.app.myridebah.Directions;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;


public interface IGoogleAPI {
    @GET("maps/api/directions/json")
 Call<Directions> getPath(@Query("mode") String mode, @Query("transit_routing_preference")String transit_routing_preference, @Query("origin")String origin, @Query("destination")String destination, @Query("key")String key );




    @GET
    Call<String> getPath(@Url String url);
}
