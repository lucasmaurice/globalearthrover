package com.example.simplecamera;

import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {
    public EditText mAdressIP;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdressIP = (EditText) findViewById(R.id.editText);
    }
    public void OnButtonClick(View view){
        Bundle mbData = new Bundle();
        mbData.putString("saddIP", mAdressIP.getText().toString());
        Intent myIntent = new Intent(MainActivity.this,FirstActivity.class);
        myIntent.putExtra("DataBundle", mbData);
        startActivityForResult(myIntent, 0);
    }
}
