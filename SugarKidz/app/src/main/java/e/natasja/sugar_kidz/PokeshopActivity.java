package e.natasja.sugar_kidz;

import android.content.Context;
import android.content.Intent;

import android.graphics.Bitmap;

import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;

import android.text.Html;

import android.util.Base64;
import android.util.Log;

import android.view.MenuItem;
import android.view.View;

import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class PokeshopActivity extends AppCompatActivity {

    RequestQueue serialRequestQueue;
    private static String TAG = "PokeshopActivity";
    ArrayList<Pokemon> pokemons;

    static int MAX_SERIAL_THREAD_POOL_SIZE = 1;
    static final int MAX_CACHE_SIZE = 2 * 1024 * 1024; //2 MB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokeshop);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        pokemons = new ArrayList<>();

        showXPAMount();
        showPokemons();

        // instantiate a serial requestqueue
        // source: https://stackoverflow.com/questions/30149453/volley-serial-requests-instead-of-parallel
//        serialRequestQueue = prepareSerialRequestQueue(getApplicationContext());
//        serialRequestQueue.start();
//
//        for (int i = 7; i < 9; i++) {
//            PokemonRequest(i);
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(PokeshopActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showPokemons() {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("pokemons");

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> mIterator = dataSnapshot.getChildren().iterator();

                pokemons.clear();

                while (mIterator.hasNext()) {
                    DataSnapshot datasnap = mIterator.next();
                    Pokemon pokemon = datasnap.getValue(Pokemon.class);
                    pokemons.add(pokemon);
                    updateList();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Not able to load pokemons from Firebase");
            }
        });
    }

    public void showXPAMount() {
        TextView XPTextView = findViewById(R.id.XP);
        final TextView XPAmountTextView = findViewById(R.id.xpAmount);

        String text =   "<font color='red'>X</font>" +
                "<font color='orange'>P</font>" + " " +
                "<font color='yellow'>A</font>" +
                "<font color='green'>m</font>" +
                "<font color='blue'>o</font>" +
                "<font color='indigo'>u</font>" +
                "<font color='violet'>n</font>" +
                "<font color='red'>t</font>" + ": ";

        XPTextView.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userID = mAuth.getCurrentUser().getUid();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users/" + userID + "/xpAmount");

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Long xpAmount = (Long) dataSnapshot.getValue();

                Log.d(TAG, "Value is: " + xpAmount);
                XPAmountTextView.setText(String.valueOf(xpAmount));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public void updateList() {
        PokelistAdapter adapter = new PokelistAdapter(getApplicationContext(), pokemons);
        ListView pokemonlist = findViewById(R.id.pokemonListView);

        pokemonlist.setAdapter(adapter);
    }

    public String encodeBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        return imageEncoded;
    }

    private static RequestQueue prepareSerialRequestQueue(Context context) {
        Cache cache = new DiskBasedCache(context.getCacheDir(), MAX_CACHE_SIZE);
        Network network = getNetwork();
        return new RequestQueue(cache, network, MAX_SERIAL_THREAD_POOL_SIZE);
    }

    private static Network getNetwork() {
        HttpStack stack;
        stack = new HurlStack();
        return new BasicNetwork(stack);
    }

    public void PokemonRequest(final int pokemon) {

        // use a trivia api to get questions: it's not secured so you only need the url
        String url = "https://pokeapi.co/api/v2/pokemon/" + pokemon + "/";
        Log.d(TAG, url);

        // Request a string response from the provided URL.
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject group = response.getJSONObject("sprites");
                            String imageUrl = group.getString("front_default");

                            JSONArray forms = response.getJSONArray("forms");
                            JSONObject thing = forms.getJSONObject(0);

                            String pokemonName = thing.getString("name");

                            Log.d(TAG, imageUrl);
                            String pokemonNumber = String.valueOf(pokemon);
                            imageRequestFunction(pokemonNumber, pokemonName, imageUrl);

                        } catch (JSONException e) {

                            // show error (in case of an error) jsonobject to array
                            Log.w(TAG, "JsonObject to Json Array didn't go right.");
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        // show error (in case of error) no response on request
                        Toast.makeText(getApplicationContext(), "No response on request, you " +
                                "probably don't have an internet connection right now.", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "No response on request." + error.toString());
                    }
                });

        // after this, we have an array with our questions, and the answers
        serialRequestQueue.add(jsObjRequest);
    }

    public void imageRequestFunction(final String pokemonNumber, final String pokemonName, String imageUrl) {
        ImageRequest imageRequest = new ImageRequest(imageUrl, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap bitmap) {

                String bitmapString = encodeBitmap(bitmap);
                Pokemon pokemon = new Pokemon(pokemonName,"1000", bitmapString);
                pokemons.add(pokemon);

                DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
                mRef.child("pokemons").child(pokemonNumber).setValue(pokemon);

                updateList();
            }
        },
                0,
                0,
                null,
                Bitmap.Config.ALPHA_8,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PokeshopActivity.this, "Loading image failed", Toast.LENGTH_LONG).show();
                    }
                }
        );

        serialRequestQueue.add(imageRequest);
    }
}
