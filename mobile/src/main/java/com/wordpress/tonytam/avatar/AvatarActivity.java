package com.wordpress.tonytam.avatar;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class AvatarActivity extends AppCompatActivity
        implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final String COUNT_KEY = "com.example.key.count";
    private static final String RAW_X = "com.wordpress.tonytam.avatar.sensorx";
    private static final String RAW_Y = "com.wordpress.tonytam.avatar.sensory";

    private GoogleApiClient mGoogleApiClient;
    private int count = 0;

    private int prevCount = -100;

    private ImageView prevView;

    private int flag = 0;
    private ImageView mFirstIndicator;
    private TextView textViewData;

    PrimeRun aniRunnable;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar
        if (false) {
            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }}
    };

    private ImageSwitcher viewSwitcher;

    private int avatarResourceIds [] = {
        R.drawable.avatar_m_10,
            R.drawable.avatar_m_9,
            R.drawable.avatar_m_8,
            R.drawable.avatar_m_7,
            R.drawable.avatar_m_6,
            R.drawable.avatar_m_5,
            R.drawable.avatar_m_4,
            R.drawable.avatar_m_3,
            R.drawable.avatar_m_2,
            R.drawable.avatar_m_1,
            R.drawable.avatar_0,
            R.drawable.avatar_1,
            R.drawable.avatar_2,
            R.drawable.avatar_3,
            R.drawable.avatar_4,
            R.drawable.avatar_5,
            R.drawable.avatar_6,
            R.drawable.avatar_7,
            R.drawable.avatar_8,
            R.drawable.avatar_9,
            R.drawable.avatar_10
    };

    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_avatar);
        viewSwitcher = (ImageSwitcher) findViewById(R.id.flipper);
        viewSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView myView = new ImageView(getApplicationContext());
                myView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                myView.setLayoutParams(new ImageSwitcher.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));
                return myView;
            }
        });
        mVisible = true;

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        textViewData = (TextView) findViewById(R.id.textViewData);

        // Register the local broadcast receiver, defined in step 3.
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
        // mFirstIndicator = (ImageView) findViewById(R.id.indicator_0);


        aniRunnable = new PrimeRun(viewSwitcher);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }
    @Override
    protected void onResume() {
        super.onResume();
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            Log.d("Event", dataEvents.toString());
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/count") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    updateCount(
                            dataMap.getInt(COUNT_KEY),
                            dataMap.getFloat(RAW_X),
                            dataMap.getFloat(RAW_Y)
                            );
                    Log.d("onDataChanged", String.valueOf(dataMap.getInt(COUNT_KEY)));
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }

        }
    }

    // Our method to update the count
    private void updateCount(int c,
                             float x,
                             float y) {
        Log.d("Avatar", "JUMP TONY!!!");
        setIndicator ( c );
        textViewData.setText(
                String.valueOf(x) + "\n" +
                        String.valueOf(y) + "->" +
                        String.valueOf(c)
        );
    }
    @Override
    public void onConnected(Bundle bundle) {
        Log.d("Avatar", "Connected");
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }


    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /**
     * Sets the page indicator for the ViewPager.
     */
    private void setIndicator(int i) {

        int avatarId = i;
        avatarId = Math.min(10, avatarId);
        avatarId = Math.max(-10, avatarId);
        avatarId = avatarId + 10;
        avatarId = Math.min(avatarId, 20);
        avatarId = Math.max(avatarId, 0);
        Log.d("Avatar Num", String.valueOf(i) + " Avatar " + String.valueOf(avatarId));

        if (avatarId == prevCount) {
            Log.d("setIndicator", "Same avatarId skip");
            return;
        }

        final int finalChild = avatarId;
        final int resource
                = avatarResourceIds[avatarId];
        //viewSwitcher.setImageResource(resource);

        if (true) {
            aniRunnable.setResource(resource);
            aniRunnable.run();
        }
        prevCount = avatarId;
    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            // Display message in UI
            Log.d("Avatar", message);
            //updateCount(flag - 10);
            flag++;
            flag = flag % 20;
        }
    }
    public class PrimeRun implements Runnable {
        public ImageSwitcher view;
        int res;

        PrimeRun(ImageSwitcher aniView) {
            this.view = aniView;
        }

        public void run() {
            view.setImageResource(res);
        }

        public void setResource(int r) {
            res = r;
        }
    }
}
