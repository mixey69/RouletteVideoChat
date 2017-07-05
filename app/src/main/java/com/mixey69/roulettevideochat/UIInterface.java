package com.mixey69.roulettevideochat;

import android.content.Context;

import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.Subscriber;


 interface UIInterface {

    void showOpenTokErrorMessage(OpentokError opentokError);

    void showPublisher(Publisher publisher);

    void showSubscriber(Subscriber subscriber);

    void clearSubscriberView();

    void clearPublisherView();

    void showWebServiceCoordinatorError(Exception error);

    Context getContext();
}
