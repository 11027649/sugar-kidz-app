static int MAX_SERIAL_THREAD_POOL_SIZE = 1;
static final int MAX_CACHE_SIZE = 2 * 1024 * 1024; //2 MB

// this function was in the onCreate of the mainactivity
// instantiate a serial requestqueue
// source: https://stackoverflow.com/questions/30149453/volley-serial-requests-instead-of-parallel
serialRequestQueue = prepareSerialRequestQueue(getApplicationContext());
serialRequestQueue.start();

for (int i = 1; i < 151; i++) {
    PokemonRequest(i);
}

// the rest of the functions are shown below:

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

    // use the pokemon api to get the pokemons
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

/**
* The imagerequest function to request the actual sprites of the pokemons and * save them to firebase.
*/
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

/**
* This method is used to save the sprite of the pokemon as a string.
* (See my code of the application for de decode method.)
*/
public String encodeBitmap(Bitmap bitmap) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
    String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    return imageEncoded;
}
