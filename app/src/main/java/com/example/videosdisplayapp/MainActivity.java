package com.example.videosdisplayapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.InterstitialListener;
import com.ironsource.mediationsdk.sdk.OfferwallListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private final static String IRON_SOURCE_APP_KEY = "4ea90fad";
    private final static String PLACEMENT_NAME = null;
    private final static int LIMIT_AMOUNT = 4;
    private final static int LIMIT_TIMEFRAME_HOURS = 12;
    private final static int FIRST_VIDEO = 1;
    private final static int MILLIS_IN_SECOND = 1000;
    private final static int SECONDS_IN_MINUTE = 60;
    private final static int MINUTES_IN_HOUR = 60;

    private Button mPlayAdBtn;

    private AppRewardedVideoListener mRewardedVideoListener;
    private InterstitialListener mInterstitialListener;
    private OfferwallListener mOfferwallListener;
    private int mRewardAmount = 0;
    private int mNumOfVidPlayedInLimitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPlayAdBtn = (Button) findViewById(R.id.play_ad_btn);

        mRewardedVideoListener = new AppRewardedVideoListener();
        IronSource.setRewardedVideoListener(mRewardedVideoListener);
        IronSource.setInterstitialListener(mInterstitialListener);
        IronSource.setOfferwallListener(mOfferwallListener);

        IronSource.init(this, IRON_SOURCE_APP_KEY);
        //TODO - remove / what's the best way to compile out DEBUG code on production?
//        IntegrationHelper.validateIntegration(this);
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

    public void playAdBtnOnClick(View view) {
        Log.d(TAG, "PlayAdB7tn_OnClick: called");

        IronSource.showRewardedVideo(PLACEMENT_NAME);
        mNumOfVidPlayedInLimitTime++;
        if (mNumOfVidPlayedInLimitTime == FIRST_VIDEO) {
            new Timer().schedule(new TimerTask() {

                @Override
                public void run() {
                    onLimitTimeframePassed();
                }
            }, LIMIT_TIMEFRAME_HOURS * MINUTES_IN_HOUR * SECONDS_IN_MINUTE * MILLIS_IN_SECOND);
        }

        Log.d(TAG, "playAdBtnOnClick: mNumOfVidPlayedInLimitTime=" + mNumOfVidPlayedInLimitTime);
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
            mRewardAmount += rewardAmount;
            Log.i(TAG, "onRewardedVideoAdRewarded: reward name=" + rewardName + ", amount=" + rewardAmount);
            Log.d(TAG, "onRewardedVideoAdRewarded: accumulated amount=" + mRewardAmount);
        }

        @Override
        public void onRewardedVideoAdShowFailed(IronSourceError ironSourceError) {
            Log.d(TAG, "onRewardedVideoAdShowFailed: called");
            int errorCode =  ironSourceError.getErrorCode();
            String errorMessage = ironSourceError.getErrorMessage();
            if (errorCode == ironSourceError.ERROR_CODE_GENERIC){
                //TODO - Write a Handler for specific error's.
            }
        }

        @Override
        public void onRewardedVideoAdClicked(Placement placement) {

        }
    };
}