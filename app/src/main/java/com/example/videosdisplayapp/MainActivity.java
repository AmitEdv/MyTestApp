package com.example.videosdisplayapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.integration.IntegrationHelper;
import com.ironsource.mediationsdk.sdk.InterstitialListener;
import com.ironsource.mediationsdk.sdk.OfferwallListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;

public class MainActivity extends AppCompatActivity {

    private RewardedVideoListener mRewardedVideoListener;
    private InterstitialListener mInterstitialListener;
    private OfferwallListener mOfferwallListener;
    private final static String APP_KEY = "4ea90fad";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //set IronSource Listeners
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
}