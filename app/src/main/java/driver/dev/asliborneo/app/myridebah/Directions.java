package driver.dev.asliborneo.app.myridebah;

import java.util.List;

public class Directions {
    public List<GeocodedWaypoint> geocoded_waypoints;
    public List<Route> routes;
    public String status;
}
class GeocodedWaypoint
{
    public String geocoder_status;
    public String place_id;
    public List<String> types;
}

class Northeast
{
    public double lat;
    public double lng;
}

class Southwest
{
    public double lat;
    public double lng;
}

class Bounds
{
    public Northeast northeast;
    public Southwest southwest;
}

class Distance
{
    public String text;
    public int value;
}

class Duration
{
    public String text;
    public int value;
}

class EndLocation
{
    public double lat;
    public double lng;
}

class StartLocation
{
    public double lat;
    public double lng;
}

class Polyline
{
    public String points;
}

class Step
{
    public Distance distance;
    public Duration duration;
    public EndLocation end_location;
    public String html_instructions;
    public Polyline polyline;
    public  StartLocation start_location;
    public String travel_mode;
    public String maneuver;
}

class Leg
{
    public Distance distance;
    public Duration duration;
    public String end_address;
    public EndLocation end_location;
    public String start_address;
    public StartLocation start_location;
    public List<Step> steps;
    public List<Object> traffic_speed_entry;
    public List<Object> via_waypoint;
}

class OverviewPolyline
{
    public String points;
}

class Route
{
    public Bounds bounds;
    public String copyrights;
    public List<Leg> legs;
    public OverviewPolyline overview_polyline;
    public String summary;
    public List<Object> warnings;
    public List<Object> waypoint_order;
}
