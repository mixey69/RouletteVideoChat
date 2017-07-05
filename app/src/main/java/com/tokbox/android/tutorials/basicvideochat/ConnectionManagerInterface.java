package com.tokbox.android.tutorials.basicvideochat;

import com.opentok.android.OpentokError;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;



interface ConnectionManagerInterface extends
        WebServiceCoordinator.Listener,
        Session.SessionListener,
        PublisherKit.PublisherListener {

    void startWebServiceCoordinator();

    void pauseSession();

    void resumeSession();

    void getUIInterface(UIInterface uiInterface);

    void clearLastSessionData();

    @Override
    void onStreamCreated(PublisherKit publisherKit, Stream stream);

    @Override
    void onStreamDestroyed(PublisherKit publisherKit, Stream stream);

    @Override
    void onError(PublisherKit publisherKit, OpentokError opentokError);

    @Override
    void onConnected(Session session);

    @Override
    void onDisconnected(Session session);

    @Override
    void onStreamReceived(Session session, Stream stream);

    @Override
    void onStreamDropped(Session session, Stream stream);

    @Override
    void onError(Session session, OpentokError opentokError);

    @Override
    void onSessionConnectionDataReady(String apiKey, String sessionId, String token, Boolean isRoomToConnectEmpty);

    @Override
    void onWebServiceCoordinatorError(Exception error);
}
