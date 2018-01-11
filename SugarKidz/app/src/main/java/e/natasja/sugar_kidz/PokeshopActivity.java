package e.natasja.sugar_kidz;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PokeshopActivity extends AppCompatActivity {

    private GestureDetectorCompat gestureObject;
    RequestQueue queue;
    private static String TAG = "PokeshopActivity";
    ArrayList<Pokemon> pokemons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokeshop);

        gestureObject = new GestureDetectorCompat(this, new LearnGesture());
        pokemons = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            PokemonRequest(i);
        }
    }

    public void PokemonRequest(int pokemon) {
        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(getApplicationContext());

        // use a trivia api to get questions: it's not secured so you only need the url
        String url = "https://pokeapi.co/api/v2/pokemon/" + pokemon + "/";

        // Request a string response from the provided URL.
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject group = response.getJSONObject("sprites");
                            String imageUrl = group.getString("front_default");
                            imageRequestFunction(imageUrl);

                        } catch (JSONException e) {

                            // show error (in case of an error) jsonobject to array
                            Log.w(TAG, "JsonObject to Json Array didn't go right.");
                            Toast.makeText(getApplicationContext(), "No response on request.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        // show error (in case of error) no response on request
                        Toast.makeText(getApplicationContext(), "No response on request, you " +
                                "probably don't have an internet connection right now.", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "No response on request.");
                    }
                });

        // after this, we have an array with our questions, and the answers
        queue.add(jsObjRequest);
    }

    public void imageRequestFunction(String imageUrl) {
        ImageRequest imageRequest = new ImageRequest(imageUrl, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap bitmap) {
                Pokemon pokemon = new Pokemon("Hey, ik ben een pokemon!", "1000XP", bitmap);
                pokemons.add(pokemon);

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

        queue.add(imageRequest);
    }

    public void updateList() {
        PokelistAdapter adapter = new PokelistAdapter(getApplicationContext(), pokemons);
        ListView pokemonlist = findViewById(R.id.pokemonListView);

        pokemonlist.setAdapter(adapter);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureObject.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public void goToGarden(View view) {
        Intent intent = new Intent(PokeshopActivity.this, MyGardenActivity.class);
        finish();
        startActivity(intent);
    }

    class LearnGesture extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {

            if (event2.getX() > event1.getX()) {
                // this will listen to swipes from left to right
            } else {
                // same in opposite direction
                Intent intent = new Intent(PokeshopActivity.this, MainActivity.class);
                finish();
                startActivity(intent);
            }

            return true;
        }
    }
}
