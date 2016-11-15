package com.globalearthrover.djls.robotserver;

/**
 * Created by Alexandre on 11/01/2016.
 */

/**
 * Use this class to storage data in the database
 */
public class Coordonnees {
    private long id;
    private double Long;
    private double Lat;
    private int Pass;

    /**
     * public Constructor
     * @param id identifiant of the member( Auto increment in the database)
     * @param Lat a latitude
     * @param Long a longitude
     * @param pass if point had been passed
     */
    public Coordonnees(long id,double Lat,double Long,int pass){
        this.id = id;
        this.Lat = Lat;
        this.Long = Long;
        this.Pass = pass;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLong() {
        return Long;
    }

    public void setLong(double Longitude) {
        this.Long = Longitude;
    }

    public double getLat() {
        return Lat;
    }

    public void setLat(double Latitude) {
        this.Lat = Latitude;
    }
    public int getState() {
        return Pass;
    }

    /**
     *
     * @param iState 0 = not passed 1 = passed
     */
    public void setState(int iState) {
        this.Pass = iState;
    }



}
