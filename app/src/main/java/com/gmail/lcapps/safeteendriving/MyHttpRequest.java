package com.gmail.lcapps.safeteendriving;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyHttpRequest extends AsyncTask<String, Void, String> {

    private Context appContext = null;
    DataDownloadListener dataDownloadListener;

    public MyHttpRequest(Context context) {

        this.appContext = context;
    }

    public void setDataDownloadListener(DataDownloadListener dataDownloadListener) {
        this.dataDownloadListener = dataDownloadListener;
    }

    @Override
    protected String doInBackground(String... params) {

        try {

            return MakeRequest(params[0], params[1]);

        } catch (Exception e) {

            return "Unable to retrieve web page. URL may be invalid.";
        }
    }

    @Override
    protected void onPostExecute(String result) {

        //Toast.makeText(appContext, "Result:, " + result, Toast.LENGTH_LONG).show();
        if(result != null)
        {
            dataDownloadListener.dataDownloadedSuccessfully(result);
        }
        else
            dataDownloadListener.dataDownloadFailed();
    }

    public interface DataDownloadListener {
        void dataDownloadedSuccessfully(Object data);
        void dataDownloadFailed();
    }

    /*public void SubmitRequest(String url, String msg) {

        ConnectivityManager connMgr = (ConnectivityManager)this.appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {

            this.message = msg;
            new WebRequestTask().execute(url);

        } else {

            Toast.makeText(this.appContext, "No network detected", Toast.LENGTH_SHORT).show();
        }
    }

    public class WebRequestTask extends AsyncTask<String, Void, String> {

        DataDownloadListener dataDownloadListener;

        public void setDataDownloadListener(DataDownloadListener dataDownloadListener) {
            this.dataDownloadListener = dataDownloadListener;
        }

        @Override
        protected String doInBackground(String... urls) {

            try {

                return MakeRequest(urls[0]);

            } catch (Exception e) {

                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        @Override
        protected void onPostExecute(String result) {

            Toast.makeText(appContext, "Result:, " + result, Toast.LENGTH_LONG).show();
            if(result != null)
            {
                dataDownloadListener.dataDownloadedSuccessfully(result);
            }
            else
                dataDownloadListener.dataDownloadFailed();
        }

        public interface DataDownloadListener {
            void dataDownloadedSuccessfully(Object data);
            void dataDownloadFailed();
        }
    }*/

    public String MakeRequest(String myUrl, String message) throws Exception
    {
        OutputStream os = null;
        InputStream is = null;
        HttpURLConnection conn = null;

        try {

            URL url = new URL(myUrl);

            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(20000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setFixedLengthStreamingMode(message.getBytes().length);
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            conn.connect();

            os = new BufferedOutputStream(conn.getOutputStream());
            os.write(message.getBytes());
            os.flush();

            is = conn.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            String contentAsString = total.toString();
            Log.v("HTTP Response: ", contentAsString);

            return contentAsString;

        } finally {

            if (os != null && is != null && conn != null) {

                //clean up
                os.close();
                is.close();
                conn.disconnect();
            }
        }
    }
}
