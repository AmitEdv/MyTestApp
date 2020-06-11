package com.example.videosdisplayapp;

import androidx.annotation.IntDef;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.InterstitialListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({InterstitialDisplayState.NO_NEED_TO_DISPLAY,
            InterstitialDisplayState.SET_NEXT_FOREGROUND_TO_DISPLAY,
            InterstitialDisplayState.DISPLAY_WHEN_BACK_IN_FOREGROUND})
    public @interface InterstitialDisplayState
    {
        int NO_NEED_TO_DISPLAY = 0;
        int SET_NEXT_FOREGROUND_TO_DISPLAY = 1;
        int DISPLAY_WHEN_BACK_IN_FOREGROUND = 2;
    }

    private final static String IRON_SOURCE_APP_KEY = "4ea90fad";
    private final static String SHARED_PREF_STORAGE_NAME = "AdDisplayAppS";
    private final static String SHARED_PREF_KEY_SHOULD_DISPLAY_INTERSTITIAL = "shouldDisplayInterstitial";
    private final static String PLACEMENT_NAME = null;
    private final static int FIRST_VIDEO = 1;
    private final static int LIMIT_AMOUNT = 4;
    private final static int LIMIT_TIMEFRAME_HOURS = 12;
    private final static int MINUTES_IN_HOUR = 60;
    private final static int SECONDS_IN_MINUTE = 60;
    private final static int MILLIS_IN_SECOND = 1000;

    private Button mPlayAdBtn;
    private TextView mRewardsTv;

    private final AppRewardedVideoListener mRewardedVideoListener = new AppRewardedVideoListener();
    private final InterstitialListener mInterstitialListener = new AppInterstitialListener();
    private int mRewardAmount = 0;
    private int mNumOfVidPlayedInLimitTime = 0;
    @InterstitialDisplayState
    private int mInterstitialAdDisplayState = InterstitialDisplayState.NO_NEED_TO_DISPLAY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPlayAdBtn = (Button) findViewById(R.id.play_ad_btn);
        mRewardsTv = (TextView) findViewById(R.id.rewards_title_tv);
        mRewardsTv.setText(getString(R.string.rewards_txt, mRewardAmount));

        restoreMembersFromPersistentLocalMemory();

        IronSource.setRewardedVideoListener(mRewardedVideoListener);
        IronSource.setInterstitialListener(mInterstitialListener);
        IronSource.init(this, IRON_SOURCE_APP_KEY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called");
        determineAndShowInterstitialAdIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IronSource.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        IronSource.onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: called");
        storeMembersInPersistentLocalMemory();
    }

    public void nextActivityBtnOnClick(View view) {
        Log.d(TAG, "nextActivityBtnOnClick: called");

        determineAndShowInterstitialAdIfNeeded();
        Intent intent = new Intent(this, AnotherActivity.class);
        startActivity(intent);
    }

    public void playAdBtnOnClick(View view) {
        Log.d(TAG, "playAdBtnOnClick: called");

        IronSource.showRewardedVideo(PLACEMENT_NAME);
        mNumOfVidPlayedInLimitTime++;
        Log.d(TAG, "playAdBtnOnClick: mNumOfVidPlayedInLimitTime=" + mNumOfVidPlayedInLimitTime);

        if (mNumOfVidPlayedInLimitTime == FIRST_VIDEO) {
            new Timer().schedule(new TimerTask() {

                @Override
                public void run() {
                    onLimitTimeframePassed();
                }
            }, LIMIT_TIMEFRAME_HOURS * MINUTES_IN_HOUR * SECONDS_IN_MINUTE * MILLIS_IN_SECOND);
        }

        if (mNumOfVidPlayedInLimitTime == LIMIT_AMOUNT) {
            mInterstitialAdDisplayState = InterstitialDisplayState.SET_NEXT_FOREGROUND_TO_DISPLAY;
        }
    }

    @SuppressLint("ApplySharedPref")
    private void storeMembersInPersistentLocalMemory() {
        Log.d(TAG, "storeMembersInPersistentLocalMemory: "
                + "mInterstitialAdDisplayState=" + mInterstitialAdDisplayState);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_STORAGE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SHARED_PREF_KEY_SHOULD_DISPLAY_INTERSTITIAL, mInterstitialAdDisplayState);
        editor.commit();
    }

    private void restoreMembersFromPersistentLocalMemory() {
        SharedPreferences sp = getSharedPreferences(SHARED_PREF_STORAGE_NAME, MODE_PRIVATE);
        mInterstitialAdDisplayState = sp.getInt(SHARED_PREF_KEY_SHOULD_DISPLAY_INTERSTITIAL, InterstitialDisplayState.NO_NEED_TO_DISPLAY);
        Log.d(TAG, "restoreMembersFromPersistentLocalMemory: "
                + "mInterstitialAdDisplayState=" + mInterstitialAdDisplayState);
    }

    private void determineAndShowInterstitialAdIfNeeded() {
        switch (mInterstitialAdDisplayState)
        {
            case InterstitialDisplayState.NO_NEED_TO_DISPLAY:
                break;
            case InterstitialDisplayState.SET_NEXT_FOREGROUND_TO_DISPLAY:
                mInterstitialAdDisplayState = InterstitialDisplayState.DISPLAY_WHEN_BACK_IN_FOREGROUND;
                break;
            case InterstitialDisplayState.DISPLAY_WHEN_BACK_IN_FOREGROUND:
                IronSource.loadInterstitial();
                break;
            default:
                Log.w(TAG, "showInterstitialAd: illegal state=" + mInterstitialAdDisplayState);
        }
    }

    private void onLimitTimeframePassed() {
        mNumOfVidPlayedInLimitTime = 0;
        updateByRewardedVideoAvailability(IronSource.isRewardedVideoAvailable());
    }

    private boolean isRewardedVideoPlacementAllowed() {
        final boolean isAllowed = mNumOfVidPlayedInLimitTime < LIMIT_AMOUNT;
        Log.d(TAG, "isRewardedVideoPlacementAllowed: is allowed=" + isAllowed);
        return isAllowed;
    }

    //TODO - what's the best method to act?
    private void updateByRewardedVideoAvailability(boolean availability) {
        Log.d(TAG, "updateByRewardedVideoAvailability");
        final boolean enableVideo = (availability && isRewardedVideoPlacementAllowed());

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mPlayAdBtn.setEnabled(enableVideo);
            }
        });
    }

    private void onVideoRewarded(int reward) {
        mRewardAmount += reward;
        Log.d(TAG, "onRewardedVideoAdRewarded: accumulated amount=" + mRewardAmount);

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mRewardsTv.setText(getString(R.string.rewards_txt, mRewardAmount));
            }
        });
    }

    private class AppInterstitialListener implements InterstitialListener {

        @Override
        public void onInterstitialAdReady() {
            Log.d(TAG,"onInterstitialAdReady: called");
            IronSource.showInterstitial(PLACEMENT_NAME);
        }

        @Override
        public void onInterstitialAdLoadFailed(IronSourceError ironSourceError) {

        }

        @Override
        public void onInterstitialAdOpened() {

        }

        @Override
        public void onInterstitialAdClosed() {

        }

        @Override
        public void onInterstitialAdShowSucceeded() {
            mInterstitialAdDisplayState = InterstitialDisplayState.NO_NEED_TO_DISPLAY;
        }

        @Override
        public void onInterstitialAdShowFailed(IronSourceError ironSourceError) {

        }

        @Override
        public void onInterstitialAdClicked() {

        }
    }

    private class AppRewardedVideoListener implements RewardedVideoListener {

        @Override
        public void onRewardedVideoAdOpened() {
            Log.d(TAG, "onRewardedVideoAdOpened: called");
        }

        @Override
        public void onRewardedVideoAdClosed() {
            Log.d(TAG, "onRewardedVideoAdClosed: called");
        }

        @Override
        public void onRewardedVideoAvailabilityChanged(boolean available) {
            Log.d(TAG, "onRewardedVideoAvailabilityChanged: called");
            updateByRewardedVideoAvailability(available);
        }

        @Override
        public void onRewardedVideoAdStarted() {
            Log.d(TAG, "onRewardedVideoAdStarted: called");
        }

        @Override
        public void onRewardedVideoAdEnded() {
            Log.d(TAG, "onRewardedVideoAdEnded: called");
        }

        @Override
        public void onRewardedVideoAdRewarded(Placement placement) {
            Log.d(TAG, "onRewardedVideoAdRewarded: called");
            if (placement == null) {
                //TODO - handle failure
                return;
            }

            String rewardName = placement.getRewardName();
            int rewardAmount = placement.getRewardAmount();
            Log.i(TAG, "onRewardedVideoAdRewarded: reward name=" + rewardName + ", amount=" + rewardAmount);
            onVideoRewarded(rewardAmount);
        }

        @Override
        public void onRewardedVideoAdShowFailed(IronSourceError ironSourceError) {
            Log.d(TAG, "onRewardedVideoAdShowFailed: called");
            int errorCode =  ironSourceError.getErrorCode();
            String errorMessage = ironSourceError.getErrorMessage();
            Log.e(TAG, "onRewardedVideoAdShowFailed: " + errorMessage);
        }

        @Override
        public void onRewardedVideoAdClicked(Placement placement) {

        }
    }
}