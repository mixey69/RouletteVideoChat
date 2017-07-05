package com.tokbox.android.tutorials.basicvideochat;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.Subscriber;

import java.util.List;

import javax.inject.Inject;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends AppCompatActivity
                            implements EasyPermissions.PermissionCallbacks,
                            UIInterface{

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;

    @Inject ConnectionManagerInterface mConnectionManager;

    private FrameLayout mPublisherViewContainer;
    private FrameLayout mSubscriberViewContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(LOG_TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPublisherViewContainer = (FrameLayout)findViewById(R.id.publisher_container);
        mSubscriberViewContainer = (FrameLayout)findViewById(R.id.subscriber_container);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Getting interfaces for MainActivity and ConnectionManager interactions

        Application.getInstance().getInjectionComponent().inject(this);
        mConnectionManager.getUIInterface(this);

        requestPermissions();
    }

     /* Activity lifecycle methods */

    @Override
    protected void onStart() {

        Log.d(LOG_TAG, "onStart");

        super.onStart();

        mConnectionManager.startWebServiceCoordinator();

    }

    @Override
    protected void onPause() {

        Log.d(LOG_TAG, "onPause");

        super.onPause();

        mConnectionManager.pauseSession();

    }

    @Override
    protected void onResume() {

        Log.d(LOG_TAG, "onResume");

        super.onResume();

        mConnectionManager.resumeSession();
    }


    @Override
    protected void onStop() {

        Log.d(LOG_TAG, "onStop");

        super.onStop();

        mConnectionManager.clearLastSessionData();
    }

    /* UIInterface methods */

    @Override
    public void showOpenTokErrorMessage(OpentokError opentokError) {
        Toast.makeText(this, "Error caused by internal problem. Please, try again.", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void showPublisher(Publisher publisher) {
        mPublisherViewContainer.removeAllViews();
            publisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_FILL,
                    BaseVideoRenderer.STYLE_VIDEO_FILL);
            mPublisherViewContainer.addView(publisher.getView());
    }

    @Override
    public void showSubscriber(Subscriber subscriber) {
        mSubscriberViewContainer.removeAllViews();
        subscriber.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_FILL,
                BaseVideoRenderer.STYLE_VIDEO_FILL);
        mSubscriberViewContainer.addView(subscriber.getView());
    }

    @Override
    public void clearSubscriberView() {
        mSubscriberViewContainer.removeAllViews();
    }

    @Override
    public void clearPublisherView() {
        mPublisherViewContainer.removeAllViews();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void showWebServiceCoordinatorError(Exception error) {
        Toast.makeText(this, "Please, check Internet connection and try again.", Toast.LENGTH_LONG).show();
        this.finish();
    }

    /* PermissionsCallbacks methods */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

        Log.d(LOG_TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

        Log.d(LOG_TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this)
                    .setTitle(getString(R.string.title_settings_dialog))
                    .setRationale(getString(R.string.rationale_ask_again))
                    .setPositiveButton(getString(R.string.setting))
                    .setNegativeButton(getString(R.string.cancel))
                    .setRequestCode(RC_SETTINGS_SCREEN_PERM)
                    .build()
                    .show();
        }
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {

        String[] perms = { Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };
        if (!EasyPermissions.hasPermissions(this, perms)){
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_video_app), RC_VIDEO_APP_PERM, perms);
        }
    }

}
