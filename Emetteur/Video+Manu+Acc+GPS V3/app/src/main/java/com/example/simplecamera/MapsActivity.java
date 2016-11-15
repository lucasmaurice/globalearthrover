package com.example.simplecamera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Random;

/**
 * Maps activity require Google play store API and a network connexions
 */

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Double Longitude = 6.11667;//
    private Double Latitude = 45.90002;//By default coordonne of Annecy
    private Random Generator = new Random();// random generator for colors of markers
    protected int NbrClick=0;
    protected ArrayList<LatLng> Road = new ArrayList<LatLng>();//use to memory the points give by user
    protected Polyline lines;
    protected LatLng YourPosition;
    protected ArrayList<LatLng> PointRoad = new ArrayList<LatLng>();//memory the road trace by phone
    protected Circle RadCircle;
    protected boolean bType;
    protected MapsActivity mActivity = this;
    protected ArrayList<Marker> ListMarker = new ArrayList<>();//Array list of markers use to permit to move marker.
    protected float fRmax;
    private int iIndex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle RBundle = this.getIntent().getExtras();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        NbrClick = 0;
        bType = RBundle.getBoolean("TYPE"); // Represent the mode of the map(Destination or Road)
        //if there are no position passed trough the bundle, by default you are on ANNECY
        if((RBundle.getDouble("LATITUDE") == 0.0)&&(RBundle.getDouble("LONGITUDE") == 0.0)){
            YourPosition = new LatLng(Latitude,Longitude);
        }
        else {
            Latitude = RBundle.getDouble("LATITUDE");
            Longitude = RBundle.getDouble("LONGITUDE");
            YourPosition = new LatLng(Latitude, Longitude);
        }
        PointRoad.add(YourPosition);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        // Getting Google Play availability status
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if(mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }
    @Override
    public void onBackPressed(){//When the user press BackButton he can end the activity and return the road
        new AlertDialog.Builder(this).setTitle("Have you finished?")
                .setMessage("Do you want really exit?")
                .setNegativeButton(android.R.string.cancel,null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent ReturnIntent = new Intent();
                        Bundle ReturnBundle = new Bundle();
                        ReturnBundle.putInt("Size",Road.size());
                        ReturnBundle.putFloat("Radius", fRmax);
                        ReturnBundle.putParcelableArrayList("Road", Road);
                        ReturnIntent.putExtra("bundle",ReturnBundle);
                        setResult(Activity.RESULT_OK,ReturnIntent);
                        finish();
                    }
                }).show();
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
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        UiSettings GMapSet = mMap.getUiSettings();
        GMapSet.setZoomControlsEnabled(true);
        //Add the marker at you position to ArrayList markers
        ListMarker.add(mMap.addMarker(new MarkerOptions().position(YourPosition).title("Marker at your position")));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(YourPosition, 20));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if ((bType == false) && (NbrClick >= 1)) {
                    /**
                     * Mode define a destination, you can only add one marker
                     */
                    new AlertDialog.Builder(mActivity).setTitle("You can only define one marker")
                            .setMessage("Do you want to change your choice?")
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    NbrClick = 0;
                                    ListMarker.get(1).remove();
                                    ListMarker.remove(1);
                                    PointRoad.remove(1);
                                    lines.remove();
                                    RadCircle.remove();
                                }
                            }).show();
                } else {
                    /**
                     * Mode define a road
                     */
                    NbrClick++;
                    PointRoad.add(latLng);
                    ListMarker.add(mMap.addMarker(new MarkerOptions().position(latLng).title("Point number" + " " + Integer.toString(NbrClick))
                            .icon(BitmapDescriptorFactory.defaultMarker(Generator.nextFloat() * 360/*generate a float between 0 and 360*/)).draggable(true)));
                    fRmax = TraceRoad(PointRoad, Road);
                }
            }
        });
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener(){// To move markers
            @Override
            public void onMarkerDragStart(Marker mark){
                iIndex = ListMarker.indexOf(mark);
            }// Move Start
            @Override
            public void onMarkerDragEnd(Marker mark){// Road is remake at each move
                //ListMarker.get(iIndex).setPosition(Tampon);
                PointRoad.set(iIndex,mark.getPosition());
                fRmax = TraceRoad(PointRoad, Road);
            }
            @Override
            public void onMarkerDrag(Marker mark){// End of the move
                mMap.animateCamera(CameraUpdateFactory.newLatLng(mark.getPosition()));
                PointRoad.set(iIndex,mark.getPosition());
                fRmax = TraceRoad(PointRoad, Road);
            }
        });

    }


    /** This method take all the point give by user in desorder and try to trace an optimal road.
     *
     * @param Croad The points give by user
     * @param Try The road calculate by method
     * @return a float representing the maximale distance betweeen origin and a point
     */
    protected float TraceRoad(final ArrayList<LatLng> Croad, ArrayList<LatLng> Try){// Algorithm to determinate a road
        int iBcl;
        float[] result = new float[1];
        float CourtRoad = 0;
        int iCourtRoad = 0;
        PolylineOptions ArrayOptions = new PolylineOptions();
        ArrayList<LatLng> Treatement = new ArrayList<LatLng>();
        float fRadius;

        if(!Try.isEmpty()){// if List is not empty we clear it
            lines.remove();
            Try.clear();
            Try.add(YourPosition);
        }
        else{
            Try.add(YourPosition);
        }
        if(RadCircle != null){//remove circle
            RadCircle.remove();
        }

        Treatement.addAll(Croad);

        /**
         * The algorithm determine the shortest longer since the origin, next move to the next point and restart the process
         */

        do{//
            result[0] = 0;//To storage the distance between 2 points
            for(iBcl = 0;iBcl < Treatement.size();iBcl++){
                Location.distanceBetween(Treatement.get(0).latitude, Treatement.get(0).longitude, Treatement.get(iBcl).latitude, Treatement.get(iBcl).longitude, result);
                if(Treatement.size() == Croad.size()){

                }
                if(CourtRoad != 0){
                    if(CourtRoad >= result[0]){
                        CourtRoad = result[0];
                        iCourtRoad = iBcl;
                    }
                }
                else{
                    CourtRoad = result[0];
                    iCourtRoad = iBcl;
                }
            }
            Try.add(Treatement.get(iCourtRoad));
            Treatement.set(0, Treatement.get(iCourtRoad));
            Treatement.remove(iCourtRoad);

        }while(!Treatement.isEmpty());
        Location.distanceBetween(Try.get(0).latitude,Try.get(0).longitude,Try.get(Try.size()-1).latitude,Try.get(Try.size()-1).longitude,result);
        fRadius = result[0];
        ArrayOptions.addAll(Try).width(5).color(Color.BLUE).geodesic(true);
        lines=mMap.addPolyline(ArrayOptions);
        RadCircle = mMap.addCircle(new CircleOptions().center(Try.get(0)).radius(fRadius).strokeColor(Color.BLACK));
        return fRadius;
    }
}