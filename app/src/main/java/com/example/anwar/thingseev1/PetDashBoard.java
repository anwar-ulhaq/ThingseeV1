package com.example.anwar.thingseev1;

/**
 * Created by Anwar on 04/05/2018.
 */

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PetDashBoard extends AppCompatActivity {

    private SparseArray DeviceEvents = new SparseArray();

    private String Altitude;
    private String Accuracy;
    private String Temperature;
    private String Humidity;
    private String Pressure;
    private String Luminance;
    private String Battery;
    private String Speed;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_dash_board);

        //Get shared preferance

        //Toast.setGravity(Gravity.TOP|Gravity.LEFT, 0, 0);

        LinearLayout LocationLayout = (LinearLayout)findViewById(R.id.L1);
        LocationLayout.setOnClickListener(LinearLayoutListener);

        LinearLayout AltitudeLayout = (LinearLayout)findViewById(R.id.L2);
        AltitudeLayout.setOnClickListener(LinearLayoutListener);

        LinearLayout AccuracyLayout = (LinearLayout)findViewById(R.id.L3);
        AccuracyLayout.setOnClickListener(LinearLayoutListener);

        LinearLayout TemperatureLayout = (LinearLayout)findViewById(R.id.L4);
        TemperatureLayout.setOnClickListener(LinearLayoutListener);

        LinearLayout HumidityLayout = (LinearLayout)findViewById(R.id.L5);
        HumidityLayout.setOnClickListener(LinearLayoutListener);

        LinearLayout PressureLayout = (LinearLayout)findViewById(R.id.L6);
        PressureLayout.setOnClickListener(LinearLayoutListener);

        LinearLayout LuminanceLayout = (LinearLayout)findViewById(R.id.L7);
        LuminanceLayout.setOnClickListener(LinearLayoutListener);

        LinearLayout BatteryLayout = (LinearLayout)findViewById(R.id.L8);
        BatteryLayout.setOnClickListener(LinearLayoutListener);

        LinearLayout SpeedLayout = (LinearLayout)findViewById(R.id.L9);
        SpeedLayout.setOnClickListener(LinearLayoutListener);

        SharedPreferences EventPref = getSharedPreferences("EventString", Activity.MODE_PRIVATE);
        //username = prefGet.getString("username", "");
        //password = prefGet.getString("password", "");

        Altitude = EventPref.getString("3", "Null");
        Accuracy = EventPref.getString("4", "Null");
        Temperature = EventPref.getString("7", "Null");
        Humidity = EventPref.getString("8", "Null");
        Pressure = EventPref.getString("10", "Null");
        Luminance = EventPref.getString("9", "Null");
        Battery = EventPref.getString("6", "Null");
        Speed = EventPref.getString("5", "Null");

        /*

        //Loop SpraceArray and save in shared pref
                for (int i = 0; i < ReseivedData.size(); i++ )
                {
                    int key = ReseivedData.keyAt(i);
                    Object Val = String.valueOf(ReseivedData.keyAt(i));
                    DevEveEdit.putString( ( String.valueOf(ReseivedData.keyAt(i)) ) , ( (ReseivedData.get(i)).toString() ) );
                }
                DevEveEdit.apply();

        */



        TextView T2 = (TextView)findViewById(R.id.T2);

        TextView T3 = (TextView)findViewById(R.id.T3);
        TextView T4 = (TextView)findViewById(R.id.T4);
        TextView T5 = (TextView)findViewById(R.id.T5);
        TextView T6 = (TextView)findViewById(R.id.T6);
        TextView T7 = (TextView)findViewById(R.id.T7);
        TextView T8 = (TextView)findViewById(R.id.T8);
        TextView T9 = (TextView)findViewById(R.id.T9);

        T2.setText(Altitude);
        T3.setText(Accuracy);
        T4.setText(Temperature);
        T5.setText(Humidity);
        T6.setText(Pressure);
        T7.setText(Luminance);
        T8.setText(Battery);
        T9.setText(Speed);




        //Toast.makeText(mContext, "Picture Changed", Toast.LENGTH_LONG).show();
    }


    private View.OnClickListener LinearLayoutListener = new View.OnClickListener() {
        @Override
        public void onClick(View LayoutView) {

            //TextView Display = (TextView)findViewById(R.id.Title);

            LinearLayout LayoutListener = (LinearLayout)LayoutView;

            int temp = 23;

            //Distinguish each click by the string of TextView. So,
            //Creat a TextView that corresponds to the third element of Linear Layout ( Whenever Element is added
            // in Linear Layout, its index is added and assigned to element.)
            TextView LayoutText = (TextView)( ((LinearLayout) LayoutView).getChildAt(2) );
            //Get the text of the TextView for comparision purpose.
            String TextViewText = LayoutText.getText().toString();

            //String LayoutIdentifier = ( String.valueOf( LayoutListener.getId() ) );

            //Display.setText( TextViewText );

            //Text view for

            //Stand Alone case for Location Linear view listner

            switch (TextViewText)
            {
                case "LOCATION":
                    int temp1 = 23;

                    Intent MapActivityIntent = new Intent(getApplicationContext(), com.example.anwar.thingseev1.MapsActivity.class);
                    startActivity(MapActivityIntent);
                    break;

                    default:
                        break;
            }


            // ON click Toast
            /*
            switch (TextViewText) {

                case "ALTITUDE":
                    Toast.makeText(getApplicationContext(), Altitude, Toast.LENGTH_LONG).show();
                    break;

                case "ACCURACY":
                    Toast.makeText(getApplicationContext(), Accuracy, Toast.LENGTH_LONG).show();
                    break;

                case "TEMPERATURE":
                    Toast.makeText(getApplicationContext(), Temperature, Toast.LENGTH_LONG).show();
                    break;

                case "HUMIDITY":
                    Toast.makeText(getApplicationContext(), Humidity, Toast.LENGTH_LONG).show();
                    break;

                case "PRESSURE":
                    Toast.makeText(getApplicationContext(), Pressure, Toast.LENGTH_LONG).show();
                    break;

                case "LUMINANCE":
                    Toast.makeText(getApplicationContext(), Luminance, Toast.LENGTH_LONG).show();
                    break;

                case "BATTERY":
                    Toast.makeText(getApplicationContext(), Battery, Toast.LENGTH_LONG).show();
                    break;

                case "SPEED":
                    Toast.makeText(getApplicationContext(), Speed, Toast.LENGTH_LONG).show();
                    break;



            }
            */
            //Toast.makeText(getApplicationContext(), TextViewText, Toast.LENGTH_LONG).show();


        }
    };

}
