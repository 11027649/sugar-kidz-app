package e.natasja.sugar_kidz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;

import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

public class MyGardenActivity extends AppCompatActivity {
    ImageView pokemon1;
    ImageView pokemon2;
    ImageView pokemon3;
    ImageView pokemon4;
    ImageView pokemon5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_garden);
        setUIOptions();

        pokemon1 = findViewById(R.id.pokemon1);
        pokemon2 = findViewById(R.id.pokemon2);
        pokemon3 = findViewById(R.id.pokemon3);
        pokemon4 = findViewById(R.id.pokemon4);
        pokemon5 = findViewById(R.id.pokemon5);

        pokemon1.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.walking));
        pokemon2.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.walking));
        pokemon3.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.walking));
        pokemon4.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.walking));
        pokemon5.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.walking));
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUIOptions();
    }

    public void setUIOptions() {
        View decorView = getWindow().getDecorView();

        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions =   View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public void goBack(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }
    }
