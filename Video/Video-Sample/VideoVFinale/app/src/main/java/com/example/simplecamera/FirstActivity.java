package com.example.simplecamera;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;


public class FirstActivity extends Activity {
    public MjpegView mVideo;
    public DoRead mReadStream;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        mVideo = (MjpegView) findViewById(R.id.mVideo);
        //On affecte alors à une chaine de caractère l’URL d’accès au streaming pour l’envoi de la requête http
        //String URL = "http://192.168.0.101:8080/video";
        Intent miIntent = getIntent();
        Bundle mbData = miIntent.getBundleExtra("DataBundle");
        String IP = mbData.getString("saddIP");
        String URL = "http://"+IP+":8080/video";
        Log.i("IPPPPPPPPPPPPPPPP",URL);
        //On associe notre Classe DoRead avec notre FirstActivity pour que celle-ci puisse acceder à notre objet mVideo (MjpegView) afin d'en définir sa source
        //La classe DoRead dérive d’AsyncTask, et va permettre d’initier la connection en créant et exécutant un client http
        mReadStream = new DoRead(this);
        mReadStream.execute(URL);

    }
}