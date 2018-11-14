package driver.dev.asliborneo.app.myridebah;


import android.graphics.Color;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import driver.dev.asliborneo.app.myridebah.Helper.DirectionJSONParser;


public class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>> {
    private GoogleMap mMap;
    ParserTask(GoogleMap map) {
        this.mMap=map;
    }

    // Parsing the data in non-ui thread
    @Override
    protected List<List<HashMap<String,String>>> doInBackground(String... jsonData) {

        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;

        try {
            jObject = new JSONObject(jsonData[0]);
            DirectionJSONParser parser = new DirectionJSONParser();

            routes = parser.parse(jObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return routes;
    }

    @Override
    protected void onPostExecute(List<List<HashMap<String,String>>> result) {
        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;
        MarkerOptions markerOptions = new MarkerOptions();

        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<>();
            lineOptions = new PolylineOptions();

            List<HashMap<String, String>> path = result.get(i);

            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble((String) Objects.requireNonNull(point.get("lat")));
                double lng = Double.parseDouble((String) Objects.requireNonNull(point.get("lng")));
                LatLng position = new LatLng(lat, lng);
                points.add(position);
            }

            lineOptions.addAll(points);
            lineOptions.width(12);
            lineOptions.color(Color.RED);
            lineOptions.geodesic(true);

        }

// Drawing polyline in the Google Map for the i-th route
        if (lineOptions != null)
       mMap.addPolyline(lineOptions);
    }
}

