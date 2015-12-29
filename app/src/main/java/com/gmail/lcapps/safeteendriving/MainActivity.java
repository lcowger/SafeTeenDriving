package com.gmail.lcapps.safeteendriving;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends Activity {

    private Button parentButton;
    private Button teenButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.parentButton = (Button) findViewById(R.id.buttonParent);
        this.teenButton = (Button) findViewById(R.id.buttonTeen);

        parentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent registerAccountIntent = new Intent(MainActivity.this, RegisterParentAccountActivity.class);
                startActivity(registerAccountIntent);
            }
        });

        teenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent mainTeenIntent = new Intent(MainActivity.this, MainTeenActivity.class);
                startActivity(mainTeenIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
