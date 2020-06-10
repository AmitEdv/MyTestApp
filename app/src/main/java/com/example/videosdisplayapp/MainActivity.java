package com.example.videosdisplayapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
    private TextView mRewardsTv;
    String mRewardsTxt;

    private AppRewardedVideoListener mRewardedVideoListener = new AppRewardedVideoListener();
    private InterstitialListener mInterstitialListener = new AppInterstitialListener();
    private OfferwallListener mOfferwallListener;
    private int mRewardAmount = 0;
    private int mNumOfVidPlayedInLimitTime = 0;
    private boolean mShouldDisplayInterstitialAd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPlayAdBtn = (Button) findViewById(R.id.play_ad_btn);
        mRewardsTv = (TextView) findViewById(R.id.rewards_title_tv);
        mRewardsTv.setText(getString(R.string.rewards_txt, mRewardAmount));

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

    public void nextActivityBtnOnClick(View view) {
        Log.d(TAG, "nextActivityBtnOnClick: called");

        showInterstitialAd();
        Intent intent = new Intent(this, AnotherActivity.class);
        startActivity(intent);
    }

    public void playAdBtnOnClick(View view) {
        Log.d(TAG, "PlayAdB7tn_OnClick: called");

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
            mShouldDisplayInterstitialAd = true;
            IronSource.loadInterstitial();
        }
    }

    private void showInterstitialAd() {
        if (mShouldDisplayInterstitialAd && IronSource.isInterstitialReady()) {
            IronSource.showInterstitial(PLACEMENT_NAME);
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
            mShouldDisplayInterstitialAd = false;
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
            if (errorCode == ironSourceError.ERROR_CODE_GENERIC){
                //TODO - Write a Handler for specific error's.
            }
        }

        @Override
        public void onRewardedVideoAdClicked(Placement placement) {

        }
    };
}