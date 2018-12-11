package driver.dev.asliborneo.app.myridebah.Remote;

import driver.dev.asliborneo.app.myridebah.Common.Common;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RetrofitClient {
    private static Retrofit retrofit=null;
    private static Retrofit fcm_retrofit=null;
    public static Retrofit getClient(){
        if (fcm_retrofit==null){
            fcm_retrofit=new Retrofit.Builder()
                    .baseUrl("https://fcm.googleapis.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return fcm_retrofit;
    }
    public static Retrofit get_direction_client(){
        if (retrofit==null){
            retrofit=new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://maps.googleapis.com/")
                    .build();
        }
        return retrofit;
    }
    public static Retrofit getPath(String baseURL){
        if (retrofit==null){
            retrofit=new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(Common.baseURL)
                    .build();
        }
        return retrofit;
    }
}
