package com.example.djls.socketserver;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity {

    networkCommunication mNetwork;
    TextView text;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (TextView) findViewById(R.id.text2);
        mNetwork = new networkCommunication();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.mNetwork.close();
    }


    class networkReception implements Runnable {
        networkCommunication.CDataReceived dataReceived;

        public networkReception(networkCommunication.CDataReceived dataReceived) {
            this.dataReceived = dataReceived;
        }

        @Override
        public void run() {
            //Enter here the programme to execute when there are a reception from on socket
            //Use the attributes strDataName and strDataValue of this.dataReceived to know more about data :
            //Show following exemple :
            Log.i("Socket", "Reception of " + dataReceived.getDataName() + " with value " + dataReceived.getDataValue());
        }
    }

    public class networkCommunication {

        private ServerSocket serverSocket;
        private int serverPort;
        Thread serverThread = null;
        Handler updateConversationHandler;

        public networkCommunication() {
            serverPort = 5000;
            updateConversationHandler = new Handler();
            this.serverThread = new Thread(new ServerThread());
            this.serverThread.start();
        }

        public void close() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        class ServerThread implements Runnable {
            public void run() {
                Socket socket = null;
                try {
                    serverSocket = new ServerSocket(serverPort);
                    Log.i("New socket", "Socket opened with port " + serverSocket.getLocalPort());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        socket = serverSocket.accept();
                        Log.i("New socket connection", socket.getInetAddress().getHostName());
                        CommunicationThread commThread = new CommunicationThread(socket);
                        new Thread(commThread).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        class CommunicationThread implements Runnable {
            private Socket clientSocket;
            private BufferedReader input;
            private CDataReceived dataReceived;

            public CommunicationThread(Socket cltSocket) {
                this.clientSocket = cltSocket;
                Log.i("Socket Client", "commThread constructor");
                dataReceived = new CDataReceived();
                try {
                    Log.i("Socket Client", "commThread constructor OK");
                    this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
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
                                updateConversationHandler.post(new networkReception(dataReceived));
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
}