package com.example.alexandre.projetct_gps;

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

    //Initialise the network socket as a server, at the chosen port.
    public CNetworkCommunication() {
        this.iServerPort = 5000;
        this.mSocket = new Socket();
        this.connexionThread = new Thread();
        this.commThread = new Thread();
    }

    public void startConnection(){
        this.connexionThread = new Thread(new ServerThread());
        this.connexionThread.start();
    }

    public void startConnection(String strServerAddress){
        this.strServerAddress = strServerAddress;
        this.connexionThread = new Thread(new ClientThread());
        this.connexionThread.start();
    }

    public void close() {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.mSocket.getOutputStream())), true);
            out.println("close");
            this.mSocket.close();
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connexionThread.interrupt();
        commThread.interrupt();
    }

    public void onReception(CDataReceived dataReceived){
        //Enter here the programme to execute when there are a reception from on socket
        //Use the attributes strDataName and strDataValue of dataReceived to know more about data
    }

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