package driver.dev.asliborneo.app.myridebah.Common;

import driver.dev.asliborneo.app.myridebah.Remote.IGoogleAPIS;
import driver.dev.asliborneo.app.myridebah.Remote.RetrofitClient;

public class Common{
    public static final String baseURL = "https://maps.googleapis.com";
    public static IGoogleAPIS getGoogleAPI()
    {
        return RetrofitClient.getClient().create(IGoogleAPIS.class);
    }
}