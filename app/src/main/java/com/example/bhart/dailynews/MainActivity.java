package com.example.bhart.dailynews;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView mRecyclerView;
    private List<News> mNewsList;
    private NewsAdapter mNewsAdapter;
    private Intent intent;

    private EditText editText;
    private TextView textView;

    Boolean isFirstTime=true;

    ProgressDialog progressDialog;
    String Url = "";
    String category="general";

    LinearLayout sortedByDate;
    LinearLayout filter;
    //default country name
    String country="";

    //Location manager and Listener
    LocationManager locationManager;
    LocationListener locationListener;


    //handling request permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){

            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
                //for saving user's battery power, we can further increase time and distance
                //since here we are interested in country only
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locationListener);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("DailyNews");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //preventing keyboard to appear after just starting app
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        editText = (EditText)findViewById(R.id.edittext);
        textView=(TextView)findViewById(R.id.ButtonSearch);

        mRecyclerView = (RecyclerView) findViewById(R.id.recylerView);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mNewsList = new ArrayList<News>();
        mNewsAdapter = new NewsAdapter(this,mNewsList);
        mRecyclerView.setAdapter(mNewsAdapter);

        //Initialising dialog box
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Data....");

        //Getting user's location
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    country=addresses.get(0).getCountryCode().toLowerCase();

                    //checking country name
                    Log.d("inside","inside locationListener");
                    Log.d("country",country);

                    if(isFirstTime) {
                        Url = "https://newsapi.org/v2/top-headlines?country=" + country + "&apiKey=2012066be1c944409c701878d544b5fc";

                        getNewsData(Url);
                        mNewsAdapter.notifyDataSetChanged();

                        isFirstTime=false;
                    }

                } catch (IOException e) {
                    Toast.makeText(MainActivity.this,"Unable to detect your location",Toast.LENGTH_SHORT).show();
                }

                progressDialog.dismiss();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        //checking user's permission for location service
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            //ask for permission
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);

            progressDialog.show();

        }else{
            //we have user's location
            Log.d("Inside user's location","permission already granted");
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locationListener);

            progressDialog.show();

        }



        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout)(findViewById(R.id.swipe_refresh_layout));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mNewsList.clear();
                getNewsData(Url);
                mNewsAdapter.notifyDataSetChanged();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                },4000);
            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                getFilteredNews(text);
            }
        });

        sortedByDate = (LinearLayout)findViewById(R.id.sort);
        sortedByDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewsList.clear();
                Url="https://newsapi.org/v2/top-headlines?country="+country+"&category="+category+"&sortBy=publishedAt&apiKey=2012066be1c944409c701878d544b5fc";
                getNewsData(Url);
                mNewsAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this,"Sorted by date",Toast.LENGTH_SHORT).show();
            }
        });

        //dialog builder for populating different news sources
        filter = (LinearLayout)findViewById(R.id.filter);
        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String [] sources = {"Google News (India)","Times Of India","The Hindu"};
                //creating builder for filtering news based on sources
                final ArrayList<Integer> mSelectedItems = new ArrayList();  // Where we track the selected items
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                // Set the dialog title
                builder.setTitle("Your country news sources")
                        // Specify the list array, the items to be selected by default (null for none),
                        // and the listener through which to receive callbacks when items are selected
                        .setMultiChoiceItems(sources, null,
                                new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which,
                                                        boolean isChecked) {
                                        if (isChecked) {
                                            // If the user checked the item, add it to the selected items
                                            mSelectedItems.add(which);
                                        } else if (mSelectedItems.contains(which)) {
                                            // Else, if the item is already in the array, remove it
                                            mSelectedItems.remove(Integer.valueOf(which));
                                        }
                                    }
                                })
                        // Set the action buttons
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked OK, so save the mSelectedItems results somewhere
                                // or return them to the component that opened the dialog
                                String source="";
                                for (int i : mSelectedItems){
                                    if (i==0) source+="google-news-in,";
                                    else if(i==1) source+="the-times-of-india,";
                                    else if(i==2)source+="the-hindu,";
                                }

                                //removing last comma from source string
                                source=source.substring(0, source.length() - 1);
                                //Toast.makeText(MainActivity.this,source,Toast.LENGTH_LONG).show();
                                mNewsList.clear();
                                Url="https://newsapi.org/v2/top-headlines?domains="+source+"&country="+country+"&category="+category+"&apiKey=2012066be1c944409c701878d544b5fc";
                                getNewsData(Url);
                                mNewsAdapter.notifyDataSetChanged();

                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        //setting news notification daily
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY,10);
        c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0);

        if (c.getTime().compareTo(new Date()) < 0) c.add(Calendar.DAY_OF_MONTH, 1);

        //Log.d("newHeading",mNewsList.toString());
        intent = new Intent(this, notification.class);
        intent.setAction("MY_NOTIFICATION_MESSAGE");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,100,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,c.getTimeInMillis(),AlarmManager.INTERVAL_DAY,pendingIntent);

    }

    //function which filter list according to the searched text...
    private void getFilteredNews(String text){
        mNewsList.clear();
        Url="https://newsapi.org/v2/top-headlines?q="+text+"&country="+country+"&category="+category+"&apiKey=2012066be1c944409c701878d544b5fc";
        getNewsData(Url);
        mNewsAdapter.notifyDataSetChanged();

    }
    private void getNewsData(String url) {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.e("inside onResponse","reached");

                        try{
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("articles");

                            Log.e("inside try block","reached");

                            for(int i=0; i<jsonArray.length();i++){
                                JSONObject object = jsonArray.getJSONObject(i);
                                News item = new News(object.getString("title"),object.getString("description")
                                        ,object.getString("urlToImage"),object.getString("url"));
                                mNewsList.add(item);
                                mNewsAdapter.notifyDataSetChanged();
                            }
                            //intent.putExtra("newsTitle",""+mNewsList.get(1).getHeading());
                            if(mNewsList.size()==0){
                                Toast.makeText(getApplicationContext(),"That was Last Page",Toast.LENGTH_SHORT).show();
                            }

                        }catch(JSONException e){
                            //progressDialog.dismiss();
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "try again", Toast.LENGTH_SHORT).show();

                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getApplicationContext(), "Check your Internet", Toast.LENGTH_SHORT).show();

            }
        })

        //caching news data once loaded
        {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                    if (cacheEntry == null) {
                        cacheEntry = new Cache.Entry();
                    }
                    final long cacheHitButRefreshed = 3 * 60 * 1000; // in 3 minutes cache will be hit, but also refreshed on background
                    final long cacheExpired = 24 * 60 * 60 * 1000; // in 24 hours this cache entry expires completely
                    long now = System.currentTimeMillis();
                    final long softExpire = now + cacheHitButRefreshed;
                    final long ttl = now + cacheExpired;
                    cacheEntry.data = response.data;
                    cacheEntry.softTtl = softExpire;
                    cacheEntry.ttl = ttl;
                    String headerValue;
                    headerValue = response.headers.get("Date");
                    if (headerValue != null) {
                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    headerValue = response.headers.get("Last-Modified");
                    if (headerValue != null) {
                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    cacheEntry.responseHeaders = response.headers;
                    final String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(jsonString, cacheEntry);
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                }

            }

            @Override
            protected void deliverResponse(String response) {
                super.deliverResponse(response);
            }

            @Override
            public void deliverError(VolleyError error) {
                super.deliverError(error);
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                return super.parseNetworkError(volleyError);
            }

        };

        //Requesting using volley
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }
    private String getUrl(String Category){
      String url="";
      url="https://newsapi.org/v2/top-headlines?country="+country+"&category="+Category+"&apiKey=2012066be1c944409c701878d544b5fc";
      //url="https://newsapi.org/v2/sources?language=en&category="+Category+"&apiKey=2012066be1c944409c701878d544b5fc";
      return url;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:")); // only email apps should handle this
            intent.putExtra(Intent.EXTRA_EMAIL, "contactus@android.com");
            startActivity(Intent.createChooser(intent, "Send Email"));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id==R.id.nav_home){
            category = "general";
            mNewsList.clear();
            Url= getUrl(category);
            getNewsData(Url);
            mNewsAdapter.notifyDataSetChanged();
            setTitle("DailyNews");
        }
        else if(id==R.id.nav_international){
            mNewsList.clear();
            Url="https://newsapi.org/v2/top-headlines?category=general&apiKey=2012066be1c944409c701878d544b5fc";
            getNewsData(Url);
            mNewsAdapter.notifyDataSetChanged();
            setTitle("International");
        }
        else if (id == R.id.nav_business) {
            category = "business";
            mNewsList.clear();
            Url= getUrl(category);
            getNewsData(Url);
            mNewsAdapter.notifyDataSetChanged();
            setTitle("Business");
        }
        else if (id == R.id.nav_technology) {
            category ="technology";
            mNewsList.clear();
            Url= getUrl(category);
            getNewsData(Url);
            mNewsAdapter.notifyDataSetChanged();
            setTitle("Technology ");
        }
        else if (id == R.id.nav_Science) {
            category="science";
            Url= getUrl(category);
            mNewsList.clear();
            getNewsData(Url);
            mNewsAdapter.notifyDataSetChanged();
            setTitle("Science");
        }
        else if (id == R.id.nav_health) {
            category="health";
            Url= getUrl(category);
            mNewsList.clear();
            getNewsData(Url);
            mNewsAdapter.notifyDataSetChanged();
            setTitle("Health");
        }
        else if (id == R.id.nav_sports) {
            category="sports";
            Url= getUrl(category);
            mNewsList.clear();
            getNewsData(Url);
            mNewsAdapter.notifyDataSetChanged();
            setTitle("Sports");
        }
        else if (id == R.id.nav_entertainment) {
            category="entertainment";
            Url= getUrl(category);
            mNewsList.clear();
            getNewsData(Url);
            mNewsAdapter.notifyDataSetChanged();
            setTitle("Entertainment");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
