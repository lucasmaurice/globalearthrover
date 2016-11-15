package com.globalearthrover.djls.robotserver;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Alexandre on 08/01/2016.
 */
public class CalculCap {
    protected MainActivity mActivity;

    public CalculCap(MainActivity Activity){
        mActivity = Activity;
    }

    /**
     * Calculate the angle between the robot and his destination
     * @param Robot actual Position of the robot
     * @param Dest where the robot should go
     * @return return the azimut necessary to follow the road
     */
    protected float getAngle(LatLng Robot,LatLng Dest) {
        double longDelta = Dest.longitude - Robot.longitude;
        double y = Math.sin(longDelta) * Math.cos(Dest.latitude);
        double x = Math.cos(Robot.latitude)*Math.sin(Dest.latitude) -
                Math.sin(Robot.latitude)*Math.cos(Dest.latitude)*Math.cos(longDelta);
        double angle = Math.toDegrees(Math.atan2(y, x));
        while (angle < 0) {
            angle += 360;
        }
        return (float) angle % 360;
    }

    /**
     * Used to Calculate the cap
     * @param fAngleRobot Actual angle of the robot
     * @param fAngleDest Angle of the destination
     * @return what the robot should do
     */
    protected int CalCap(float fAngleRobot, float fAngleDest){

        //Ordres
        final int AVANCER = 1;
        final int TOURNERD = 3;
        final int TOURNERG = 4;

        float fAngle = 0;
        int iOrdreRet = AVANCER;

        fAngle = (fAngleDest - fAngleRobot)%360;

        if((-30 < fAngle) && (fAngle < 30)){
            iOrdreRet = AVANCER;
        }
        else if(((fAngle < 0) && (fAngle > -180)) || (fAngle > 180) ){
            iOrdreRet = TOURNERG;
        }
        else if(((fAngle > 0) && (fAngle < 180)) || (fAngle < -180)){
            iOrdreRet = TOURNERD;
        }
        return iOrdreRet;
    }
}
