package driver.dev.asliborneo.app.myridebah.Remote;


import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;


public interface IGoogleAPIS {



    @GET
    Call<String> getPath(@Url String url);
}
