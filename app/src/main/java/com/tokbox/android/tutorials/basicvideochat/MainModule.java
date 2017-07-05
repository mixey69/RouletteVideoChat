package com.tokbox.android.tutorials.basicvideochat;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@Module
class MainModule {

    Context mContext;

    public MainModule(Context context){
        mContext = context;
    }

    @Provides
    @Singleton
    ConnectionManagerInterface providesConnectionManagerInterface(){
        return new ConnectionManager();
    }
}
