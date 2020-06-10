package com.example.videosdisplayapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.integration.IntegrationHelper;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.InterstitialListener;
import com.ironsource.mediationsdk.sdk.OfferwallListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "AppCompatActivity";

    private Button mPlayAdBtn;

    private AppRewardedVideoListener mRewardedVideoListener;
    private InterstitialListener mInterstitialListener;
    private OfferwallListener mOfferwallListener;
    private final static String IRON_SOURCE_APP_KEY = "4ea90fad";

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

    public void PlayAdBtn_OnClick(View view) {
        Log.d(TAG, "PlayAdB7tn_OnClick: called");
        IronSource.showRewardedVideo();
    }

    //TODO - what's the best method to act?
    private void updateByRewardedVideoAvailability(final boolean availability) {
        Log.d(TAG, "updateByRewardedVideoAvailability");
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mPlayAdBtn.setEnabled(availability);
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
            Log.i(TAG, "onRewardedVideoAdRewarded: reward name = " + rewardName + ", amount = " + rewardAmount);
        }

        @Override
        public void onRewardedVideoAdShowFailed(IronSourceError ironSourceError) {
            Log.d(TAG, "onRewardedVideoAdShowFailed: called");
            //Retrieve details from a SupersonicError object.
            int errorCode =  ironSourceError.getErrorCode();
            String errorMessage = ironSourceError.getErrorMessage();
            if (errorCode == ironSourceError.ERROR_CODE_GENERIC){
                //Write a Handler for specific error's.
            }
        }

        @Override
        public void onRewardedVideoAdClicked(Placement placement) {

        }
    };
}