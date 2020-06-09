package com.example.videosdisplayapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.integration.IntegrationHelper;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.InterstitialListener;
import com.ironsource.mediationsdk.sdk.OfferwallListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "AppCompatActivity";

    private AppRewardedVideoListener mRewardedVideoListener;
    private InterstitialListener mInterstitialListener;
    private OfferwallListener mOfferwallListener;
    private final static String APP_KEY = "4ea90fad";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRewardedVideoListener = new AppRewardedVideoListener();
        IronSource.setRewardedVideoListener(mRewardedVideoListener);
        IronSource.setInterstitialListener(mInterstitialListener);
        IronSource.setOfferwallListener(mOfferwallListener);

        IronSource.init(this, APP_KEY);
        IntegrationHelper.validateIntegration(this);
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
        Log.d(TAG, "PlayAdBtn_OnClick: called");
    }

    static class AppRewardedVideoListener implements RewardedVideoListener {

        @Override
        public void onRewardedVideoAdOpened() {

        }

        @Override
        public void onRewardedVideoAdClosed() {

        }

        @Override
        public void onRewardedVideoAvailabilityChanged(boolean b) {

        }

        @Override
        public void onRewardedVideoAdStarted() {

        }

        @Override
        public void onRewardedVideoAdEnded() {

        }

        @Override
        public void onRewardedVideoAdRewarded(Placement placement) {

        }

        @Override
        public void onRewardedVideoAdShowFailed(IronSourceError ironSourceError) {

        }

        @Override
        public void onRewardedVideoAdClicked(Placement placement) {

        }
    };
}