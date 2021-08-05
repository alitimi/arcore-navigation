package com.aut.navigation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import androidx.appcompat.app.AppCompatActivity;

public class Destination extends AppCompatActivity {

    ListView listView;
    String place = null;
    String source;
    Button nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destinations_list);
        nav = findViewById(R.id.navButton);

        //mAdapter = ArrayAdapter.createFromResource(this,R.array.destinations,);

        listView = (ListView) findViewById(R.id.destination_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemValue = (String) listView.getItemAtPosition(position);
                String values = ((TextView)view).getText().toString();
                source = values;
                IntentIntegrator integrator = new IntentIntegrator(Destination.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });
        nav.setOnClickListener(view -> {
            SharedPreferences sd = getSharedPreferences("data", Context.MODE_PRIVATE);
            SharedPreferences.Editor ed=sd.edit();
            String mText = (String)((TextView) view).getText();
            Toast.makeText(getApplicationContext(),mText,Toast.LENGTH_SHORT).show();
            ed.putString("sdDest",mText);
            ed.commit();
            Intent intent = new Intent(Destination.this, BasicNavigation.class);
            intent.putExtra("destination", source);
            intent.putExtra("source", place);
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.e("Scan*******", "Cancelled scan");
            } else {
                Log.e("Scan", "Scanned");
                place = result.getContents();
                if (!place.equals(null)){
                    listView.setVisibility(View.GONE);
                    nav.setVisibility(View.VISIBLE);
                } else {
                    listView.setVisibility(View.VISIBLE);
                    nav.setVisibility(View.GONE);
                }
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
