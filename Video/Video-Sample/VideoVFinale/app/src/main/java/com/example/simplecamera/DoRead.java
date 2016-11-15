package com.example.simplecamera;

import java.io.IOException;
import java.net.URI;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.os.AsyncTask;
import android.util.Log;

// AsyncTask<Params, Progress, Result>
//Params, the type of the parameters sent to the task upon execution.
//Progress, the type of the progress units published during the background computation.
//Result, the type of the result of the background computation.

public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {
    private static final String TAG = "DoRead";
    public FirstActivity mActivity;

    public DoRead(FirstActivity Activity){
        mActivity = Activity;
    }
    protected MjpegInputStream doInBackground(String... url) {
        //TODO: if camera has authentication deal with it and don't just not work
        HttpResponse response = null;
        //You retrieve and send data via the HttpClient class. An instance of this class can be created with new DefaultHttpClient();
        DefaultHttpClient httpclient = new DefaultHttpClient();
        Log.d(TAG, "1. Sending http request");
        try {
            //The HttpClient uses a HttpUriRequest to send and receive data. Important subclass of HttpUriRequest are HttpGet and HttpPost. You can get the response of the HttpClient as an InputStream.
            response = httpclient.execute(new HttpGet(URI.create(url[0])));
            Log.d(TAG, "2. Request finished, status = " + response.getStatusLine().getStatusCode());
            //If the reponse is 401 it means "unauthorised"
            if(response.getStatusLine().getStatusCode()==401){
                //You must turn off camera User Access Control before this will work
                return null;
            }
            //Return the object containing the response
            return new MjpegInputStream(response.getEntity().getContent());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            Log.d(TAG, "Request failed-ClientProtocolException", e);
            //Error connecting to camera
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Request failed-IOException", e);
            //Error connecting to camera
        }

        return null;
    }

    //AsyncTask must be subclassed to be used. The subclass will override at least one method (doInBackground(Params...)), and most often will override a second one (onPostExecute(Result).)
    protected void onPostExecute(MjpegInputStream result) {
        //Source of the video
        mActivity.mVideo.setSource(result);
        //Set  the mode of display
        mActivity.mVideo.setDisplayMode(MjpegView.SIZE_BEST_FIT);
        //Show the speed of the transmission (FPS)
        mActivity.mVideo.showFps(true);
    }
}