package com.example.simplecamera;

import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;

public class MainActivity extends Activity implements View.OnClickListener {
    public EditText mAdressIP;

    Button mbuttonManualmode = null;
    Button mbuttonGyromode = null;
    Button mbuttonGPSmode = null;
    Button mbuttonClose = null;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.mbuttonManualmode = (Button) findViewById(R.id.Manualmode);
        this.mbuttonManualmode.setOnClickListener(this);

        this.mbuttonGyromode = (Button) findViewById(R.id.Gyro);
        this.mbuttonGyromode.setOnClickListener(this);

        this.mbuttonGPSmode = (Button) findViewById(R.id.GPS);
        this.mbuttonGPSmode.setOnClickListener(this);

        this.mbuttonClose = (Button) findViewById(R.id.Close);
        this.mbuttonClose.setOnClickListener(this);

        mAdressIP = (EditText) findViewById(R.id.editText);
    }

    public void onClick(View v) {
        switch (v.getId()) { // who click ?

            case R.id.Manualmode:
                Bundle mbData = new Bundle();
                mbData.putString("saddIP", mAdressIP.getText().toString());
                Intent myIntent = new Intent(MainActivity.this,ManualActivity.class);
                myIntent.putExtra("DataBundle", mbData);
                startActivityForResult(myIntent, 0);
                break;

            case R.id.Gyro:
                Bundle mbData2 = new Bundle();
                mbData2.putString("saddIP", mAdressIP.getText().toString());
                Intent myIntent2 = new Intent(MainActivity.this,GyroActivity.class);
                myIntent2.putExtra("DataBundle", mbData2);
                startActivityForResult(myIntent2, 0);
                break;

            case R.id.GPS:
                Bundle mbData3 = new Bundle();
                mbData3.putString("saddIP", mAdressIP.getText().toString());
                Intent myIntent3 = new Intent(MainActivity.this,MainActivityAuto.class);
                myIntent3.putExtra("DataBundle", mbData3);
                startActivityForResult(myIntent3, 0);
                break;

            case R.id.Close:
                this.finish();
                break;
        }
    }
}
