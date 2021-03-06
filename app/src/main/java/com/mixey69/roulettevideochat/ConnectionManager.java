package com.mixey69.roulettevideochat;

import android.content.Context;
import android.util.Log;

import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


class ConnectionManager implements
        ConnectionManagerInterface,
        WebServiceCoordinator.Listener,
        Session.SessionListener,
        PublisherKit.PublisherListener {

    private static final String LOG_TAG = ConnectionManager.class.getSimpleName();

    private WebServiceCoordinator mWebServiceCoordinator;

    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;

    private Disposable mSubscription;

    private Boolean isRoomToConnectEmpty;
    private Boolean wasOnStreamReceivedCalled = false;

    private UIInterface uiInterface;
    private Context mContext;


    @Inject
    ConnectionManager() {
    }


    /* ConnectionMangerInterface methods */

    //ConnectionManager entry point
    @Override
    public void init(UIInterface uiInterface) {
        this.uiInterface = uiInterface;
        mContext = this.uiInterface.getContext();
        mWebServiceCoordinator = new WebServiceCoordinator(mContext, this);
        mWebServiceCoordinator.fetchSessionConnectionData(
                mContext.getResources().getString(R.string.SESSION_INFO_ENDPOINT));
    }

    @Override
    public void pauseSession() {
        if (mSession != null) {
            mSession.onPause();
        }
    }

    @Override
    public void resumeSession() {
        if (mSession != null) {
            mSession.onResume();
        }
    }

    @Override
    public void dropUIInterface() {
        clearLastSessionData();
        this.uiInterface = null;
    }

    @Override
    public void clearLastSessionData() {
        if (mPublisher != null) {
            mSession.unpublish(mPublisher);
            mPublisher.destroy();
            mPublisher = null;
        }
        if (mSubscriber != null) {
            mSession.unsubscribe(mSubscriber);
            mSubscriber.destroy();
            mSubscriber = null;
        }
        if (mSession != null) {
            mSession.disconnect();
            mSession = null;
        }

        if (mSubscription != null) {
            mSubscription.dispose();
        }

        wasOnStreamReceivedCalled = false;

        if (uiInterface != null) {
            uiInterface.clearSubscriberView();
            uiInterface.clearPublisherView();
        }
    }

    /* SessionListener methods */

    @Override
    public void onConnected(Session session) {
        Log.d(LOG_TAG, "onConnected: Connected to session: " + session.getSessionId());

        if (uiInterface == null) {
            return;
        }

        //recreating publisher

        mPublisher = new Publisher.Builder(mContext).build();
        mPublisher.setPublisherListener(this);
        if (uiInterface != null) {
            uiInterface.showPublisher(mPublisher);
        }
        mSession.publish(mPublisher);

        /*
        if server tells client that there's someone in the room(!isRoomToConnectEmpty),
        but we receive no stream in 500ms,
        we clear session data  and request another session
         */


        if (!isRoomToConnectEmpty) {
            if (mSubscription != null) {
                mSubscription.dispose();
            }
            mSubscription = Observable.interval(500, TimeUnit.MILLISECONDS)
                    .take(1)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(@io.reactivex.annotations.NonNull Long aLong) throws Exception {
                            if (!wasOnStreamReceivedCalled) {

                                //clearing all data

                                clearLastSessionData();

                                //and getting a new session where we'll be alone and waiting if UI is still being shown

                                if (uiInterface != null) {
                                    mWebServiceCoordinator.fetchSessionConnectionData(
                                            mContext.getResources().getString(R.string.SESSION_INFO_ENDPOINT));
                                }
                            }
                        }
                    });
        }

    }

    @Override
    public void onDisconnected(Session session) {
        Log.d(LOG_TAG, "onDisconnected: Disconnected from session: " + session.getSessionId());
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {

        //telling timeout consumer that the session is not empty and we won't need another one
        wasOnStreamReceivedCalled = true;

        Log.d(LOG_TAG, "onStreamReceived: New Stream Received " + stream.getStreamId() + " in session: " + session.getSessionId());

        //creating subscriber object and telling UI to show it
        if (uiInterface != null) {
            mSubscriber = new Subscriber.Builder(mContext, stream).build();
            mSession.subscribe(mSubscriber);
            uiInterface.showSubscriber(mSubscriber);
        }

    }

    //if subscriber stopped streaming we're leaving the session and requesting another one
    @Override
    public void onStreamDropped(Session session, Stream stream) {

        clearLastSessionData();

        if (uiInterface != null) {
            mWebServiceCoordinator.fetchSessionConnectionData(
                    mContext.getResources().getString(R.string.SESSION_INFO_ENDPOINT));
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.e(LOG_TAG, "onError: " + opentokError.getErrorDomain() + " : " +
                opentokError.getErrorCode() + " - " + opentokError.getMessage() + " in session: " + session.getSessionId());
        // UnknownSubscriberInstance error is not critical so ignore it
        if (!(opentokError.getErrorCode() == OpentokError.ErrorCode.UnknownSubscriberInstance)) {
            if (uiInterface != null) {
                uiInterface.showOpenTokErrorMessage(opentokError);
            }
        }
    }

    /* PublisherListener methods */

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.d(LOG_TAG, "onStreamCreated: Publisher Stream Created. Own stream " + stream.getStreamId());
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.d(LOG_TAG, "onStreamDestroyed: Publisher Stream Destroyed. Own stream " + stream.getStreamId());
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.e(LOG_TAG, "onError: " + opentokError.getErrorDomain() + " : " +
                opentokError.getErrorCode() + " - " + opentokError.getMessage());
        if (uiInterface != null) {
            uiInterface.showOpenTokErrorMessage(opentokError);
        }
    }

    /* WebServiceCoordinatorListener methods*/

    @Override
    public void onSessionConnectionDataReady(String apiKey, String sessionId, String token, Boolean isRoomToConnectEmpty) {

        Log.d(LOG_TAG, "ApiKey: " + apiKey + " SessionId: " + sessionId + " Token: " + token);
        this.isRoomToConnectEmpty = isRoomToConnectEmpty;
        initializeSession(mContext.getResources().getString(R.string.API_KEY), sessionId, token);
    }

    @Override
    public void onWebServiceCoordinatorError(Exception error) {
        Log.e(LOG_TAG, "Web Service error: " + error.getMessage());
        if (uiInterface != null) {
            uiInterface.showWebServiceCoordinatorError(error);
        }
    }

    private void initializeSession(String apiKey, String sessionId, String token) {
        clearLastSessionData();
        mSession = new Session.Builder(mContext, apiKey, sessionId).build();
        mSession.setSessionListener(this);
        mSession.connect(token);
    }
}