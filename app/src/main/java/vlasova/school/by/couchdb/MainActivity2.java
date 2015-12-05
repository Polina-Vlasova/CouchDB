package vlasova.school.by.couchdb;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.DocumentsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class MainActivity2 extends ActionBarActivity {

    EditText value1;
    EditText value2;
    String type;
    int n;
    TableLayout tl;
    ArrayList<String> keys;
    ArrayList<String> values;
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity2);

        value1 = (EditText)findViewById(R.id.value1);
        value2 = (EditText)findViewById(R.id.value2);
        tl = (TableLayout)findViewById(R.id.tl);
        keys = new ArrayList<String>();
        values = new ArrayList<String>();
        image = (ImageView)findViewById(R.id.imageView);
        type = MainActivity2.this.getIntent().getStringExtra("type");
        if(type.equals("get")) {
            keys = MainActivity2.this.getIntent().getStringArrayListExtra("keys");
            values = MainActivity2.this.getIntent().getStringArrayListExtra("values");
            int index = keys.indexOf("_id");
            keys.set(keys.indexOf("_id"), keys.get(0));
            keys.set(0, "_id");
            String id = values.get(index);
            values.set(index, values.get(0));
            values.set(0, id);
            int index2 = keys.indexOf("_rev");
            keys.set(index2, keys.get(1));
            keys.set(1, "_rev");
            String rev = values.get(index2);
            values.set(index2, values.get(1));
            values.set(1, rev);
            value1.setText(values.get(0));
            value2.setText(values.get(1));
        } else {
            keys.add("_id");
            keys.add("_rev");
            values.add("");
            values.add("");
        }
        n = keys.size();
        for(int i = 2; i < n; i++){
            newKey(i, "add");
        }
        n++;
        newKey(n - 1, "new");

        int index = keys.indexOf("_attachments");
        if(index != -1) {
            String att = values.get(index);
            try {
                for(int i = 0; i < new JSONObject(att).names().length(); i++) {
                    String title = new JSONObject(att).names().get(i).toString();
                    RequestQueue queue = Volley.newRequestQueue(MainActivity2.this);
                    String url = "http://46.101.205.23:4444/test_db/" + values.get(0) + "/" + title;
                    final int finalI = i;
                    ImageRequest iq = new ImageRequest(url,
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap bitmap) {
                                    if(finalI == 0) {
                                        image.setImageBitmap(bitmap);
                                    } else {
                                        LinearLayout ll = (LinearLayout)findViewById(R.id.ll);
                                        ImageView iv = new ImageView(MainActivity2.this);
                                        iv.setImageBitmap(bitmap);
                                        ll.addView(iv);
                                    }
                                }
                            }, 0, 0, null,
                            new Response.ErrorListener() {
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT);
                                }
                            });
                    queue.add(iq);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            image.setImageResource(R.drawable.default_img);
        }

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String url = "http://46.101.205.23:4444/test_db/";
                JSONObject j = new JSONObject();
                try {
                    if(!value1.getText().toString().equals(""))
                        j.put("_id", value1.getText());
                    if(type.equals("get"))
                        j.put("_rev", value2.getText());
                    for(int i = 2; i < n; i++)
                        if(!keys.get(i).equals(""))
                            j.put(keys.get(i), values.get(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RequestQueue queue = Volley.newRequestQueue(MainActivity2.this);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST, url, j,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                Toast.makeText(getApplicationContext(),  jsonObject.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage() + " /er", Toast.LENGTH_SHORT).show();
                    }
                });
                queue.add(jsonObjectRequest);
                MainActivity2.this.startActivity(new Intent(MainActivity2.this, MainActivity.class));
            }
        });
        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity2.this.startActivity(new Intent(MainActivity2.this, MainActivity.class));
            }
        });
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if(resCode == Activity.RESULT_OK && data != null){
            String realPath = RealPathUtil.getRealPathFromURI(this, data.getData());
            setTextViews(realPath);
        }

    }

    private void setTextViews(String realPath){

        Uri uriFromPath = Uri.fromFile(new File(realPath));
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uriFromPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        image.setImageBitmap(bitmap);
    }
    public void newKey(final int i, String type){
        final TableRow tr = new TableRow(this);
        RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        tr.setLayoutParams(lp3);
        tr.setGravity(Gravity.CENTER);
        final EditText key = new EditText(this);
        TableRow.LayoutParams lp1 = new TableRow.LayoutParams(value1.getLayoutParams().width, value1.getLayoutParams().height);
        lp1.setMargins(0, 10, 15, 10);
        key.setLayoutParams(lp1);
        key.setPadding(10, 0, 0, 0);
        key.setSingleLine();
        key.setGravity(Gravity.FILL_VERTICAL);
        key.setBackgroundResource(R.drawable.back);
        key.setHint("Key" + (i + 1) + ":");


        final EditText value = new EditText(this);
        TableRow.LayoutParams lp2 = new TableRow.LayoutParams(value1.getLayoutParams().width, value1.getLayoutParams().height);
        lp2.setMargins(15, 10, 0, 10);
        value.setLayoutParams(lp2);
        value.setPadding(10, 0, 0, 0);
        value.setSingleLine();
        value.setGravity(Gravity.FILL_VERTICAL);
        value.setBackgroundResource(R.drawable.back);
        value.setHint("Value" + (i + 1) + ":");
        if(type.equals("add")) {
            value.setText(values.get(i));
            key.setText(keys.get(i));
        } else {
            values.add("");
            keys.add("");
        }
        tr.addView(key);
        tr.addView(value);
        tl.addView(tr);

        final int[] finalI = {i};
        key.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(start == 0 && after == 1 && finalI[0] == n - 1){
                    n++;
                    newKey(finalI[0] + 1, "new");
                    keys.add(key.getText().toString());
                    values.add(value.getText().toString());
                }
                if(start == 0 && after == 0 && s.length() == 1){
                    keys.remove(finalI[0]);
                    values.remove(finalI[0]);
                    n--;
                    for(int j = n; j >= finalI[0]; j--){
                        tl.removeViewAt(j);

                    }
                    for(int j = finalI[0]; j < n; j++){
                        newKey(j, "add");
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().equals(""))
                    keys.set(finalI[0], key.getText().toString());
            }
        });

        value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try{
                    values.set((finalI[0]), value.getText().toString());}
                catch (Exception e){
                }
            }

            });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity2, menu);
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
