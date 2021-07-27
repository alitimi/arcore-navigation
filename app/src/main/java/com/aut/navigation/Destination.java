package com.aut.navigation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class Destination extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destinations_list);

        //mAdapter = ArrayAdapter.createFromResource(this,R.array.destinations,);

        ListView listView = (ListView) findViewById(R.id.destination_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences sd = getSharedPreferences("data", Context.MODE_PRIVATE);
                SharedPreferences.Editor ed=sd.edit();
                String mText = (String)((TextView) view).getText();
                Toast.makeText(getApplicationContext(),mText,Toast.LENGTH_SHORT).show();
                ed.putString("sdDest",mText);
                ed.commit();
                Intent intent = new Intent(Destination.this, SourceDetection.class);
                intent.putExtra("destination", mText);
                startActivity(intent);
            }
        });
    }
}
