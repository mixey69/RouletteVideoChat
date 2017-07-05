package com.mixey69.roulettevideochat;

interface ConnectionManagerInterface {

    void pauseSession();

    void resumeSession();

    void init(UIInterface uiInterface);

    void clearLastSessionData();

    void dropUIInterface();

}
