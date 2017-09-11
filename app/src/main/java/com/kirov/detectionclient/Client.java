package com.kirov.detectionclient;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by rzcc5 on 10-Sep-17.
 */

public class Client extends AsyncTask<Void, Void, String> {
    String address;
    String response ="";
    int portNum;
    TextView responseText;
    boolean isConnected = false;
    //private static Context mContext;

    Client(String add, int port, TextView responseText){
        address = add;
        portNum = port;
        this.responseText = responseText;
        //mContext.getApplicationContext();
    }


    @Override
    protected String doInBackground(Void... params) {
        Socket socket = null;
        //step = main.stepValue;
        try{
            //response = "Connecting...";
            Log.d("[CLIENT]:", "Connecting...");
            socket = new Socket(address, portNum);
            Log.d("[CLIENT]:", "Connected");
            isConnected = true;
            while (isConnected){
                //ToDo send step data to server
                try{
                    Log.d("[OUTPUT]:", "Sending data");
                    DataOutputStream DOS = new DataOutputStream(socket.getOutputStream());
                    DOS.writeUTF("Send Steps");
                    socket.close();
                } catch(IOException e){
                    Log.d("[OUTPUT]:", "IO Exception");
                    e.printStackTrace();
                    response = "IOException: " + e.toString();
                }
            }
        } catch(UnknownHostException e){
            e.printStackTrace();
            Log.d("[CLIENT]: ", "UnknownHostException");
            response = "Unknown Host Exception: "+ e.toString();
        } catch(IOException e){
            Log.d("[CLIENT]: ", "IO Exception");
            e.printStackTrace();
            response = "IOException: " + e.toString();
        } finally{
            if (socket != null){
                try{
                    socket.close();
                    isConnected = false;
                    response = "Socket Closed";
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        return response;
    }

    @Override
    protected void onPostExecute(String result){
        responseText.setText(response);
        super.onPostExecute(result);
    }
}
