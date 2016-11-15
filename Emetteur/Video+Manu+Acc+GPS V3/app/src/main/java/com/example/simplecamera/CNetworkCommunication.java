package com.example.simplecamera;

import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Lucas Maurice on 3/01/2016.
 *
 * Use this class for basic socket data transfert
 * If you want to be able to receive data, you'll need to extend the class and override the method onReception(CDataReceived){}
 */

public class CNetworkCommunication {

    private ServerSocket serverSocket;
    private Socket mSocket;
    private String strServerAddress;
    private int iServerPort;
    Thread connexionThread;
    Thread commThread;


    public CNetworkCommunication() {
        this.iServerPort = 5000;
        this.mSocket = new Socket();
        this.connexionThread = new Thread();
        this.commThread = new Thread();
    }

    /**
     * Start a socket server in the object.
     * Use this when you need to start a SERVER.
     * Do not use the other surcharge in the same object.
     */
    public void startConnection(){
        this.connexionThread = new Thread(new ServerThread());
        this.connexionThread.start();
    }

    /**
     * Initialise a socket connexion on the chosen server in the object.
     * @param strServerAddress
     * Do not use the other surcharge in the same object.
     */
    public void startConnection(String strServerAddress){
        this.strServerAddress = strServerAddress;
        this.connexionThread = new Thread(new ClientThread());
        this.connexionThread.start();
        while(!this.mSocket.isConnected()){

        }
    }

    /**
     * Close all the connexions, and the server.
     */
    public void close() {
        connexionThread.interrupt();
        try {
            if(this.mSocket.isConnected()){
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.mSocket.getOutputStream())), true);
                out.println("close");
            }
            this.mSocket.close();
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method ran on data reception event on the socket connexion.
     * Override this method and insert your own code.
     */
    public void onReception(CDataReceived dataReceived){
        //Enter here the programme to execute when there are a reception from on socket
        //Use the attributes strDataName and strDataValue of dataReceived to know more about data
    }

    /**
     * Send data over socket connexion.
     * @param strName index of the data
     * @param strValue value of the data
     */
    public void sendData(String strName, String strValue){
        if(mSocket.isConnected()){
            try {
                String str = strName + '\0' + strValue;
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())), true);
                out.println(str);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Used to know if the server is running or not.
     * @return state of the server as boolean(true = running ; false = stopped)
     */
    public boolean isServerRunning(){
        boolean bRetIsRunning;
        if(serverSocket==null){
            bRetIsRunning = false;
        }else{
            bRetIsRunning = serverSocket.isBound();
        }
        return bRetIsRunning;
    }

    /**
     * Class of the server thread. Wait for socket client connexion.
     * When a client is logged, it launch the commmunication thread.
     */
    class ServerThread implements Runnable {
        public void run() {
            try {
                serverSocket = new ServerSocket(iServerPort);
                Log.i("New socket", "Socket opened with port " + serverSocket.getLocalPort());
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if(!mSocket.isConnected()) {
                        Log.i("New socket","waiting for client");
                        mSocket = serverSocket.accept();
                        Log.i("New socket connection", mSocket.getInetAddress().getHostName());
                        CommunicationThread commThread = new CommunicationThread();
                        new Thread(commThread).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Class of the client thread. Try to connect to the server, @ the IP address chosen.
     * If the connexion success, it launch the commmunication thread.
     */
    class ClientThread implements Runnable {
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(strServerAddress);
                Log.i("Socket client", "Try to connect");
                mSocket = new Socket(serverAddr,5000);
                CommunicationThread commThread = new CommunicationThread();
                new Thread(commThread).start();
                Log.i("Socket client", "is connected");
            } catch (UnknownHostException e) {
                e.printStackTrace();
                Log.e("Socket client", e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Socket client", e.getMessage());
            }
        }
    }

    /**
     * Class of the communication thread. Wait for data reception.
     * Analyse received data. Send data to the data received object.
     * If data format is correct, it run onReception method.
     */
    class CommunicationThread implements Runnable {
        private BufferedReader input;
        private CDataReceived dataReceived;

        public CommunicationThread() {
            Log.i("Socket Client", "commThread constructor");
            dataReceived = new CDataReceived();
            try {
                Log.i("Socket Client", "commThread constructor OK");
                this.input = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    Log.i("Socket Client","Data reception");
                    if(read == "close"){
                        Log.w("Socket Client","Device disconnected.");
                    }
                    if (read != null) {
                        Log.i("Socket Client","Not empty");
                        if(dataReceived.setData(read)){
                            onReception(dataReceived);
                        }else{
                            Log.e("Socket Client","Bad reception");
                        }
                    } else {
                        Log.e("Socket Client","Missing Connection (data empty).");
                        Thread.currentThread().interrupt();
                        connexionThread.run();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class CDataReceived{
        String strDataName;
        String strDataValue;
        public CDataReceived(){
            strDataName="";
            strDataValue="";
        }

        public boolean setData(String strData){
            boolean bRetDataOK;
            String strTab[]=strData.split("\0");
            int iLength = strTab.length;
            if(iLength==2){
                strDataName=strTab[0];
                strDataValue=strTab[1];
                bRetDataOK=true;
            }
            else{
                bRetDataOK=false;
            }
            return bRetDataOK;
        }
        public String getDataName(){
            return strDataName;
        }
        public String getDataValue(){
            return strDataValue;
        }
    }
}