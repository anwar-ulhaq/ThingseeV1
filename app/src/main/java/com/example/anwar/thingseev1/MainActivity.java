/*
 * MainActivity.java -- Simple demo application for the Thingsee cloud server agent
 *
 * Request 20 latest position measurements and displays them on the
 * listview wigdet.
 *
 * Note: you need to insert the following line before application -tag in
 * the AndroidManifest.xml file
 *  <uses-permission android:name="android.permission.INTERNET" />
 *
 * Author(s): Jarkko Vuori
 * Modification(s):
 *   First version created on 04.02.2017
 *   Clears the positions array before button pressed 15.02.2017
 *   Stores username and password to SharedPreferences 17.02.2017
 */
package com.example.anwar.thingseev1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;



//import com.example.jarkko.thingsee.R;
//import com.example.jarkko.thingsee.ThingSee;


import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int    MAXPOSITIONS = 20;
    private static final String PREFERENCEID = "Credentials";

    //private static final String DeviceProperties = "DeviceDetails";

    private String               username, password;
    private String[] ListReceivedData = new String[0];
    //private HashMap<Integer, String> ResievedData = new HashMap<Integer, String>();
    private SparseArray ReseivedData = new SparseArray();
    private String[]             positions = new String[29];//[MAXPOSITIONS];
    private String[]             NoOfDevices = new String[5];
    private int NofDevices = 0;
    private ArrayAdapter<String> myAdapter;
    private ArrayAdapter<String> DeviceAdapter;
    //private JSONArray            ThingseeDevices = new JSONArray();
    private boolean DeviceFound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize the array so that every position has an object (even it is empty string)
        /*
        for (int i = 0; i < positions.length; i++)
            positions[i] = "";
        */



        // setup the adapter for the array
        //myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, positions);
        //DeviceAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, MonthNames);

        // then connect it to the list in application's layout
        //ListView DeviceList = (ListView) findViewById(R.id.DeviceList);
        //DeviceList.setAdapter(DeviceAdapter);
        //Listener of List

        //DeviceList.setOnItemClickListener(this);

        // setup the button event listener to receive onClick events
        //((Button)findViewById(R.id.GetDevices)).setOnClickListener(this);

        //Click listener for Imgae
        ((ImageButton)findViewById(R.id.GetDevices)).setOnClickListener(this);

        // check that we know username and password for the Thingsee cloud
        SharedPreferences prefGet = getSharedPreferences(PREFERENCEID, Activity.MODE_PRIVATE);
        username = prefGet.getString("username", "");
        password = prefGet.getString("password", "");
        if (username.length() == 0 || password.length() == 0)
            // no, ask them from the user
            queryDialog(this, getResources().getString(R.string.prompt));
    }

    private void queryDialog(Context context, String msg) {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.credentials_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView dialogMsg      = (TextView) promptsView.findViewById(R.id.textViewDialogMsg);
        final EditText dialogUsername = (EditText) promptsView.findViewById(R.id.editTextDialogUsername);
        final EditText dialogPassword = (EditText) promptsView.findViewById(R.id.editTextDialogPassword);

        dialogMsg.setText(msg);
        dialogUsername.setText(username);
        dialogPassword.setText(password);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                username = dialogUsername.getText().toString();
                                password = dialogPassword.getText().toString();

                                SharedPreferences prefPut = getSharedPreferences(PREFERENCEID, Activity.MODE_PRIVATE);
                                SharedPreferences.Editor prefEditor = prefPut.edit();
                                prefEditor.putString("username", username);
                                prefEditor.putString("password", password);

                                prefEditor.apply();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void onClick(View v) {
        Log.d("USR", "Button pressed");

        // we make the request to the Thingsee cloud server in backgroud
        // (AsyncTask) so that we don't block the UI (to prevent ANR state, Android Not Responding)
        new TalkToThingsee().execute("QueryState");

        //Goto DeviceListActivity and create a list of devices.
        //Intent DeviceListActivity = new Intent(this, DeviceListActivity.class);
        //if(DeviceFound);
        //if(ReseivedData != null);

        //If devices are found? Start Devices activity
        if(DeviceFound)
        {
            int temp = 23;
            //Start activity
            Intent DeviceListActivity = new Intent(this, com.example.anwar.thingseev1.DeviceListActivity.class);
            startActivity(DeviceListActivity);
        }



    }

    /* This class communicates with the ThingSee client on a separate thread (background processing)
     * so that it does not slow down the user interface (UI)
     */
    private class TalkToThingsee extends AsyncTask<String, Integer, String> {
        ThingSee thingsee;



        //Create a JSONObject. Device =JSONArray.getJSONObject(index);

        //List<Location> coordinates = new ArrayList<Location>();

        //List<String> ResievedData = new ArrayList<String>();

        @Override
        protected String doInBackground(String ... params)
        {
            String result = "NOT OK";

            // here we make the request to the cloud server for MAXPOSITION number of coordinates
            try
            {
                thingsee = new ThingSee(username, password);

                //Create a JSONArray. Devices array. Should be global so that other activitess can access data too.
                JSONArray DeviceList = thingsee.Devices();

                //Length of DeviceList. to send it to DeviceListActivity
                int NuofDevices = DeviceList.length();
                // If device found?
                if (NuofDevices > 0)
                {
                    DeviceFound = true;
                }
                //Initilize Shared preference
                SharedPreferences DevPref = getSharedPreferences("DeviceDetails", Activity.MODE_PRIVATE);
                //Editor to edit and save the changes in shared preferences
                SharedPreferences.Editor DevputEditor = DevPref.edit();
                //Store Data in to Shared Preferences
                DevputEditor.putInt("NoOfDevices", NuofDevices);
                //DevputEditor.putStringSet("Long",S)
                //Svae Data
                DevputEditor.apply();

                // Get the event object of thingsee device with index Device[x].
                // 2nd plan. pass a complete device JSONobject from device array. .Event method Supports it.
                //JSONArray events = thingsee.Events(thingsee.Devices(0), MAXPOSITIONS);
                JSONArray events = thingsee.Events(DeviceList.getJSONObject(0), MAXPOSITIONS);
                //System.out.println(events);

                // Send the Device [x] event and get a hashmap.
                //ResievedData = thingsee.getPath(events);
                // Send the Device [x] event and get a SparasArray.
                ReseivedData = thingsee.getPath(events);
                //Initiliza Shared Preference for events
                SharedPreferences DeviceEvents = getSharedPreferences("EventString", Activity.MODE_PRIVATE);
                //Editor
                SharedPreferences.Editor DevEveEdit = DeviceEvents.edit();

                //Loop SpraceArray and save in shared pref
                for (int i = 0; i < ReseivedData.size(); i++ )
                {
                    // int key = ReseivedData.keyAt(i);
                    //Object Val = String.valueOf(ReseivedData.get(i));
                    DevEveEdit.putString( ( String.valueOf(ReseivedData.keyAt(i)) ) , ( (ReseivedData.get(i)).toString() ) );
                }
                DevEveEdit.apply();



//                for (Location coordinate: coordinates)
//                    System.out.println(coordinate);
                result = "OK";
            } catch(Exception e) {
                Log.d("NET", "Communication error: " + e.getMessage());
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            // check that the background communication with the client was succesfull
            if (result.equals("OK")) {
                // now the coordinates variable has those coordinates
                // elements of these coordinates is the Location object who has
                // fields for longitude, latitude and time when the position was fixed
                //myAdapter.notifyDataSetChanged();
                //Creat a TextView
                    //TextView CurrTemp = (TextView)findViewById(R.id.Hint);
                //Get the string from HashMap at index 1 that is Longitude;
                //CurrTemp.append(ResievedData.get(1) );
                    //CurrTemp.append( (ReseivedData.get(1)).toString() );
                    //CurrTemp.append("--");
                //CurrTemp.append(ResievedData.get(2) );
                    //CurrTemp.append( (ReseivedData.get(2)).toString() );
                //Iterator HashMapIter = ResievedData;

                /*
                //HashMap to Array conversion.
                //HashMap to Array conversion.
                Object[] appArray = ResievedData.values().toArray();
                String[] strArray = new String[appArray.length];
                //ListReceivedData = strArray;
                for (int i = 1; i < appArray.length; i++)
                {
                    strArray[i] = ( (String) appArray[i]);
                }

                positions = strArray;
                */

                /*
                for (int i = 0; i < ResievedData.size(); i++) {
                    Location loc = coordinates.get(i);

                    positions[i] = (new Date(loc.getTime())) +
                            " (" + loc.getLatitude() + "," +
                            loc.getLongitude() + "," + loc.getAltitude() + "," + loc.getAccuracy() + ")"; //coordinates.get(i).toString();
                }*/
            } else {
                // no, tell that to the user and ask a new username/password pair
                //positions[0] = getResources().getString(R.string.no_connection);
                queryDialog(MainActivity.this, getResources().getString(R.string.info_prompt));
            }
            //myAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPreExecute() {
            // first clear the previous entries (if they exist)
            /*
            for (int i = 0; i < positions.length; i++)
                positions[i] = "";
            */

            //myAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {}
    }
}
