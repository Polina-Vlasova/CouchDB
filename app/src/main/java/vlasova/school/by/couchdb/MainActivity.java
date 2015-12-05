package vlasova.school.by.couchdb;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ImageReader;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText id = (EditText) findViewById(R.id.editText);
        findViewById(R.id.get).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!id.getText().toString().equals("")) {
                    final String url = "http://46.101.205.23:4444/test_db/" + id.getText();
                    RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                    StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.GET, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject json = new JSONObject(response);
                                        ArrayList<String> keys = new ArrayList<String>();
                                        ArrayList<String> values = new ArrayList<String>();
                                        Iterator<String> jsonKeys = json.keys();
                                        while(jsonKeys.hasNext() ){
                                            String key = jsonKeys.next();
                                            keys.add(key);
                                            values.add(json.getString(key));
                                        }
                                        Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                                        intent.putExtra("keys", keys);
                                        intent.putExtra("values", values);
                                        intent.putExtra("type", "get");
                                        MainActivity.this.startActivity(intent);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), "Invalid ID!", Toast.LENGTH_SHORT).show();
                        }
                    });
                queue.add(stringRequest);
                } else
                    Toast.makeText(getApplicationContext(), "Enter ID!", Toast.LENGTH_SHORT).show();

            }
        });


        findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                intent.putExtra("type", "create");
                MainActivity.this.startActivity(intent);
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
