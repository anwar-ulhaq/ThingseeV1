package com.example.anwar.thingseev1;

/**
 * Created by Anwar on 11/04/2018.
 */



/*
 * ThingSee.java -- ThingSee Cloud server client
 *
 * API documentation:
 *  http://api.thingsee.com/doc/rest
 *  https://thingsee.zendesk.com/hc/en-us/articles/205188962-Thingsee-Property-API-
 *  https://thingsee.zendesk.com/hc/en-us/articles/205188982-Thingsee-Events-API-
 * Web interface:
 *  http://app.thingsee.com
 *
 * Copyright (C) 2017 by ZyMIX Oy. All rights reserved.
 * Author(s): Jarkko Vuori
 * Modification(s):
 *   First version created on 04.02.2017
 */
//package com.example.jarkko.thingsee;

        import android.app.Activity;
        import android.content.SharedPreferences;
        import android.location.Location;
        import android.util.Log;
        import android.util.SparseArray;

        import java.io.*;
        import java.net.*;
        import java.util.ArrayList;
        import java.util.Date;
        import java.util.HashMap;
        import java.util.List;
        import org.json.*;

public class ThingSee {
    private final static String charset = "UTF-8";
    private final static String url     = "http://api.thingsee.com/v2";


    private URLConnection connection;
    private String        accountAuthUuid;
    private String        accountAuthToken;
    private Boolean       fConnection;

    /**
     * Authenticates the user
     * <p>
     * Credentials returned from the cloud server are used in the following method calls
     * (so that it is not needed to send email/password information every time to the server).
     *
     * @param email      User's email address used for the authentication
     * @param passwd     User's password
     * @throws Exception Gives an exception with text information if there was an error
     */
    //ThingSee(String email, String passwd) throws Exception

    ThingSee(String email, String passwd) throws Exception {
        JSONObject param = new JSONObject();

        param.put("email", email);
        param.put("password", passwd);

        JSONObject resp = getThingSeeObject(param, "/accounts/login");
        accountAuthUuid = resp.getString("accountAuthUuid");
        accountAuthToken = resp.getString("accountAuthToken");
    }

    /**
     * Send a request to the ThingSee server at the subpath
     * <p>
     * Authentication is supposed to have been done before (using a constructor)
     *
     * @param  request   Request parameter (optional, null if not needed)
     * @param  path      URI-name for the object to be requested
     * @return           Requested object in JSON-format
     * @throws Exception Gives an exception with text information if there was an error
     */
    private JSONObject getThingSeeObject(JSONObject request, String path) throws Exception {
        JSONObject     resp     = null;
        InputStream    response = null;
        BufferedReader reader   = null;


        fConnection = false;
        try {
            connection = new URL(url + path).openConnection();
                connection.setConnectTimeout(5000); connection.setReadTimeout(5000);
            connection.setRequestProperty("Accept-Charset", charset);
            connection.setRequestProperty("Content-Type", "application/json;charset=" + charset);
            if (accountAuthToken != null)
                connection.setRequestProperty("Authorization", "Bearer " + accountAuthToken);

            // send a request (if needed)
            if (request != null) {
                connection.setDoOutput(true);   // Triggers HTTP POST request

                OutputStream output = connection.getOutputStream();
                output.write(request.toString().getBytes(charset));
            }

            // wait for the reply
            response = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(response));
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }

            //System.out.println("Responce: " + out.toString());
            resp = new JSONObject(out.toString());
            fConnection = true;
        } finally {
            // ensure that streams are closed in all situations
            try {
                if (response != null)
                    response.close();
                if (reader != null)
                    reader.close();
            } catch (IOException ioe) {}
        }

        return (resp);
    }

    /**
     * Request the handle to the first ThingSee device at the cloud account
     * <p>
     * Cloud account may have multiple devices, this function selects always the first device
     *
     * @return Return JSON decription of the selected device
     */
    public JSONArray Devices() {
        JSONObject item, resp;
        JSONArray ListDevices;

        try {
            resp = getThingSeeObject(null, "/devices");
            //JSONArray devices = (JSONArray) resp.get("devices");
            ListDevices = (JSONArray) resp.get("devices");

            //int NoOfDevices = devices.length();

            //item = devices.getJSONObject(index);

        } catch (Exception e) {
            Log.d("THINGSEE", "No Thingsee device");
            //item = null;
            ListDevices = null;
        }
        //Returns device object
        return (ListDevices);
    }

    /**
     * Request all device events
     * <p>
     * Every device has an event log. This method read the given devices event log.
     *
     * @param  device    Device JSON description (given by Devices() method
     * @param  limit     Maximum number of events retrieved
     * @return           Events in JSON format
     * @throws Exception Gives an exception with text information if there was an error
     */
    public JSONArray Events(JSONObject device, int limit) throws Exception {
        JSONObject resp;
        JSONArray  events;

        //JSONObject DeviceUUID;

        try {
            resp = getThingSeeObject(null, "/events/" + device.getString("uuid") + "?limit=" + limit);
            events  = (JSONArray)resp.get("events");
        } catch (Exception e) {
            Log.d("THINGSEE", "ThingseeEvents error " + e);
            throw new Exception("No events");
        }

        return (events);
    }

    /* senseID groupID field */
    private static final int GROUP_LOCATION     = 0x01 << 16;   
    private static final int GROUP_SPEED        = 0x02 << 16;
    private static final int GROUP_ENERGY       = 0x03 << 16;
    private static final int GROUP_ORIENTATION  = 0x04 << 16;
    private static final int GROUP_ACCELERATION = 0x05 << 16;
    private static final int GROUP_ENVIRONMENT  = 0x06 << 16;
    private static final int GROUP_HW_KEYS      = 0x07 << 16;

    /* senseID propertyID field */
    private static final int PROPERTY1          = 0x01 << 8;    //256
    private static final int PROPERTY2          = 0x02 << 8;    //512
    private static final int PROPERTY3          = 0x03 << 8;    //768
    private static final int PROPERTY4          = 0x04 << 8;    //1024

    /**
     * Obtain Location objects from the events array
     * <p>
     * Collects all location events and construct Location object
     *
     * @param  events Device JSON description (given by Devices() method)
     * @return        List of Location objects (coordinates), empty if there are no coordinates available
     * @throws        Exception Gives an exception with text information if there was an error
     */
    public SparseArray getPath(JSONArray events) throws Exception {

        //HashMap<Integer, String> ExtractedVAlues = new HashMap<Integer, String>();
        SparseArray ExtractedValues = new SparseArray();

        //List   coordinates = new ArrayList();
        //int    k;

        try {
            for (int i = 0; i < events.length(); i++) {
                JSONObject event = events.getJSONObject(i);
                //Location   loc   = new Location("S1");



                //loc.setTime(event.getLong("timestamp"));
                //Write Timestamp at index 0 of list
                ExtractedValues.put(0, (event.getString ("timestamp") ) );
                //k = 0;
                JSONArray senses = event.getJSONObject("cause").getJSONArray("senses");

                //Loop through all MAXOSITIONS samples. it can be one also.
                for (int j = 0; j < senses.length(); j++) {

                    JSONObject sense = senses.getJSONObject(j);

                    //Loop through every cause and check SIDs
                    //for (int AllSidChecker = 0; AllSidChecker < sense.length(); AllSidChecker++)

                    //{


                        int senseID = Integer.decode(sense.getString("sId"));
                        //double value = sense.getDouble("val");

                        //String      TempVAl = "24";

                        //if (GROUP_LOCATION | PROPERTY1 == 0x000100)
                        int size = ExtractedValues.size();

                        switch (senseID) {

                            //Lat
                            case GROUP_LOCATION | PROPERTY1:
                                ExtractedValues.put(1, (sense.getString("val")));
                                break;

                            //Lon
                            case GROUP_LOCATION | PROPERTY2:
                                ExtractedValues.put(2, (sense.getString("val")));
                                break;

                            //Altitude
                            case GROUP_LOCATION | PROPERTY3:
                                ExtractedValues.put(3, (sense.getString("val")));
                                //ExtractedValues.put(3,"some");
                                break;

                            //Accuracy
                            case GROUP_LOCATION | PROPERTY4:
                                ExtractedValues.put(4, (sense.getString("val")));
                                break;

                            //Speed
                            case GROUP_SPEED | PROPERTY1:
                                ExtractedValues.put(5, (sense.getString("val")));
                                break;
                            /*
                            //
                            case GROUP_SPEED | PROPERTY2:
                                ExtractedValues.put(6, (sense.getString("val")));
                                break;

                            case GROUP_SPEED | PROPERTY3:
                                ExtractedValues.put(7, (sense.getString("val")));
                                break;

                            case GROUP_SPEED | PROPERTY4:
                                ExtractedValues.put(8, (sense.getString("val")));
                                break;

                            case GROUP_ENERGY | PROPERTY1:
                                ExtractedValues.put(9, (sense.getString("val")));
                                break;

                             */
                            //Battery
                            case GROUP_ENERGY | PROPERTY2:
                                //ExtractedValues.put(10, (sense.getString("val")));
                                ExtractedValues.put(6, (sense.getString("val")));
                                break;

                            /*
                            //
                            case GROUP_ENERGY | PROPERTY3:
                                ExtractedValues.put(11, (sense.getString("val")));
                                break;

                            case GROUP_ENERGY | PROPERTY4:
                                ExtractedValues.put(12, (sense.getString("val")));
                                break;

                            case GROUP_ORIENTATION | PROPERTY1:
                                ExtractedValues.put(13, (sense.getString("val")));
                                break;

                            case GROUP_ORIENTATION | PROPERTY2:
                                ExtractedValues.put(14, (sense.getString("val")));
                                break;

                            case GROUP_ORIENTATION | PROPERTY3:
                                ExtractedValues.put(15, (sense.getString("val")));
                                break;

                            case GROUP_ORIENTATION | PROPERTY4:
                                ExtractedValues.put(16, (sense.getString("val")));
                                break;

                            case GROUP_ACCELERATION | PROPERTY1:
                                ExtractedValues.put(17, (sense.getString("val")));
                                break;

                            case GROUP_ACCELERATION | PROPERTY2:
                                ExtractedValues.put(18, (sense.getString("val")));
                                break;

                            case GROUP_ACCELERATION | PROPERTY3:
                                ExtractedValues.put(19, (sense.getString("val")));
                                break;

                            case GROUP_ACCELERATION | PROPERTY4:
                                ExtractedValues.put(20, (sense.getString("val")));
                                break;
                            */

                            //Temp
                            case GROUP_ENVIRONMENT | PROPERTY1:
                                //ExtractedValues.put(21, (sense.getString("val")));
                                ExtractedValues.put(7, (sense.getString("val")));
                                break;

                            //Humid
                            case GROUP_ENVIRONMENT | PROPERTY2:
                                //ExtractedValues.put(22, (sense.getString("val")));
                                ExtractedValues.put(8, (sense.getString("val")));
                                break;

                            //Luminance
                            case GROUP_ENVIRONMENT | PROPERTY3:
                                //ExtractedValues.put(23, (sense.getString("val")));
                                ExtractedValues.put(9, (sense.getString("val")));
                                break;

                            //Pressure
                            case GROUP_ENVIRONMENT | PROPERTY4:
                                //ExtractedValues.put(24, (sense.getString("val")));
                                ExtractedValues.put(10, (sense.getString("val")));
                                break;
                            /*
                            case GROUP_HW_KEYS | PROPERTY1:
                                ExtractedValues.put(25, (sense.getString("val")));
                                break;

                            case GROUP_HW_KEYS | PROPERTY2:
                                ExtractedValues.put(26, (sense.getString("val")));
                                break;

                            case GROUP_HW_KEYS | PROPERTY3:
                                ExtractedValues.put(27, (sense.getString("val")));
                                break;

                            case GROUP_HW_KEYS | PROPERTY4:
                                ExtractedValues.put(28, (sense.getString("val")));
                                break;
                            */
                            default:
                                break;


                        }


                    //}
                }
            }
        } catch (Exception e) {
            throw new Exception("No coordinates");
        }

        return ExtractedValues;
    }

    @Override
    public String toString() {
        String s;

        if (fConnection)
            s = "Uuid: " + accountAuthUuid + "\nToken: " + accountAuthToken;
        else
            s = "Not authenticated";

        return (s);
    }

    /**
     * Convert events to string
     * <p>
     * Converts timestamp and senses information of the event to string
     *
     * @param  events Events in JSON format
     * @return        Events in string format
     */
    public String toString(JSONArray events) {
        StringBuilder s = new StringBuilder();
        String        ss;

        try {
            for (int i = 0; i < events.length(); i++) {
                JSONObject event = events.getJSONObject(i);

                s.append(new Date(event.getLong("timestamp")) + ": ");
                //System.out.println("    type: " + event.getString("type"));
                JSONArray senses = event.getJSONObject("cause").getJSONArray("senses");
                for (int j = 0; j < senses.length(); j++) {
                    JSONObject sense = senses.getJSONObject(j);

                    s.append("sId " + sense.getString("sId") + ": " + sense.getDouble("val") + ",");
                }
                s.append("\n");
            }
            ss = s.toString();
        } catch (Exception e) {
            ss = null;
        }

        return ss;
    }
}
