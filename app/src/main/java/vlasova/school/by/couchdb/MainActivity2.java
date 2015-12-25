package vlasova.school.by.couchdb;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity2 extends ActionBarActivity {

    ProgressBar progressBar;
    EditText value1;
    EditText value2;
    String type;
    int n;
    TableLayout tl;
    ArrayList<String> keys;
    ArrayList<String> values;
    ArrayList<ImageView> images;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity2);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        value1 = (EditText)findViewById(R.id.value1);
        value2 = (EditText)findViewById(R.id.value2);
        tl = (TableLayout)findViewById(R.id.tl);
        keys = new ArrayList<String>();
        values = new ArrayList<String>();
        images = new ArrayList<>();
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
        final LinearLayout ll = (LinearLayout)findViewById(R.id.ll);
        int index = keys.indexOf("_attachments");
        if(index != -1) {
            String att = values.get(index);
            try {
                for(int i = 0; i < new JSONObject(att).names().length(); i++) {
                    String title = new JSONObject(att).names().get(i).toString();
                    RequestQueue queue = Volley.newRequestQueue(MainActivity2.this);
                    String url = "http://46.101.205.23:4444/test_db/" + values.get(0) + "/" + title;
                    final int finalI = i;
                    final int finalI1 = i;
                    ImageRequest iq = new ImageRequest(url,
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap bitmap) {
                                    ImageView iv = new ImageView(MainActivity2.this);
                                    iv.setImageBitmap(bitmap);
                                    ll.addView(iv);
                                    images.add(iv);
                                    iv.setOnLongClickListener(new View.OnLongClickListener() {
                                        @Override
                                        public boolean onLongClick(View v) {
                                            showD(finalI1);
                                            return true;
                                        }
                                    });

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
            imageView = new ImageView(MainActivity2.this);
            imageView.setImageResource(R.drawable.default_img);
            ll.addView(imageView);
            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showD(-1);
                    return true;
                }
            });
        }
        progressBar.setVisibility(View.INVISIBLE);

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(ProgressBar.VISIBLE);
                final String url = "http://46.101.205.23:4444/test_db/";
                final RequestQueue queue = Volley.newRequestQueue(MainActivity2.this);
                final JSONObject j = new JSONObject();
                try {
                    if(!value1.getText().toString().equals(""))
                        j.put("_id", value1.getText());
                    if(type.equals("get"))
                        j.put("_rev", value2.getText());
                    for(int i = 2; i < n; i++) {
                        if(keys.get(i).equals("_attachments"))
                            continue;
                        else if (!keys.get(i).equals(""))
                            j.put(keys.get(i), values.get(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST, url, j,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(final JSONObject jsonObject) {
                                if(images.size() == 0) {
                                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                                    MainActivity2.this.startActivity(new Intent(MainActivity2.this, MainActivity.class));
                                    Toast.makeText(getApplicationContext(), jsonObject.toString(), Toast.LENGTH_LONG).show();
                                } else {
                                    try {
                                        putImage(0, jsonObject.getString("rev"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();

                    }
                });
                queue.add(jsonObjectRequest);
            }
        });
        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity2.this.startActivity(new Intent(MainActivity2.this, MainActivity.class));
            }
        });
    }

    public void putImage(int i, String rev){
        final String url = "http://46.101.205.23:4444/test_db/";
        String url2 = url + values.get(0) + "/picture" + (i + 1) + ".png?rev=" + rev;
        Bitmap bmp = ((BitmapDrawable)images.get(i).getDrawable()).getBitmap();
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        final byte[] photoBytes = stream.toByteArray();
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final int finalI = i;
        StringRequest stringRequest = new StringRequest(Request.Method.PUT, url2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(images.size() == finalI + 1) {
                            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(ProgressBar.INVISIBLE);
                            MainActivity2.this.startActivity(new Intent(MainActivity2.this, MainActivity.class));
                        } else
                            try {
                                putImage(finalI + 1, new JSONObject(response).getString("rev"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(images.size() == finalI + 1) {
                            Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(ProgressBar.INVISIBLE);
                            MainActivity2.this.startActivity(new Intent(MainActivity2.this, MainActivity.class));
                        }
                    }
                })
        {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return photoBytes;
            }
        };
        queue.add(stringRequest);

    }


    public void showD(final int id) {
        final String[] task;
        if(id != -1)
            task = new String[]{"Изменить", "Удалить"};
        else
            task = new String[]{"Изменить"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите действие");
        builder.setItems(task, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if(item == 0) {
                    progressBar.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, id);
                    }
                } else {
                    LinearLayout ll = (LinearLayout)findViewById(R.id.ll);
                    ll.removeView(images.get(id));
                    images.remove(id);
                    if(images.size() == 0){
                        imageView = new ImageView(MainActivity2.this);
                        imageView.setImageResource(R.drawable.default_img);
                        ll.addView(imageView);
                        imageView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                showD(-1);
                                return true;
                            }
                        });
                    }
                }
            }
        });
        builder.setCancelable(false);
        AlertDialog alert =  builder.create();
        alert.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("WTF", requestCode + "/" + resultCode);
        progressBar.setVisibility(View.INVISIBLE);
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if(images.size() != 0) {
                images.get(requestCode).setImageBitmap(bitmap);
            } else {
                imageView.setImageBitmap(bitmap);
                images.add(imageView);
            }
        }

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
}
