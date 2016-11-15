package com.example.simplecamera;

class CNetCom extends CNetworkCommunication{
    boolean bRobotLoc = false;
    double latitude = 0;
    double longitude = 0;
    MainActivityAuto mMainActivityAuto;

    public CNetCom(MainActivityAuto activity){
        mMainActivityAuto = activity;
    }
    public void onReception(CDataReceived dataReceived){
        switch (dataReceived.getDataName()){
            case "LOCATION":
                if(dataReceived.getDataValue() == "OK"){
                    mMainActivityAuto.OnDataReceive();
                }
                break;
            case "LAT":
                this.latitude = Double.valueOf(dataReceived.getDataValue());
                break;
            case "LONG":
                this.longitude = Double.valueOf(dataReceived.getDataValue());
                break;
        }
    }

}
