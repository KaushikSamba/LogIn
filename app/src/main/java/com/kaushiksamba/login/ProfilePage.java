package com.kaushiksamba.login;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ProfilePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        setData();
    }

    private void setData()
    {
        String name = null, F_ID = null, college = null, email_ID = null;
        ArrayList<String> reg_events = new ArrayList<>();
        TextView nameView,fIDView, collegeView, emailIDView;
        nameView = (TextView) findViewById(R.id.welcomeText);
        fIDView = (TextView) findViewById(R.id.f_id);
        collegeView = (TextView) findViewById(R.id.college_name);
        emailIDView = (TextView) findViewById(R.id.email_id);

        //Getting the details from SharedPreferences/Utilities
            //Get name, F_ID, college, email_ID, reg_events

        //Setting the details
        ArrayAdapter<String> eventsArrayAdapter = new ArrayAdapter<>(getApplicationContext(),R.layout.single_list_item,reg_events);
        ListView regEventsList = (ListView) findViewById(R.id.registeredEventsListView);
        nameView.setText(nameView.getText().toString() + name);
        fIDView.setText(fIDView.getText().toString() + F_ID);
        collegeView.setText(fIDView.getText().toString() + college);
        emailIDView.setText(fIDView.getText().toString() + email_ID);
        regEventsList.setAdapter(eventsArrayAdapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout)
        {
            //Logout
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
