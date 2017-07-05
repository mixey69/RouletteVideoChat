package com.tokbox.android.tutorials.basicvideochat;

import javax.inject.Singleton;

import dagger.Component;


@Singleton
@Component(modules = MainModule.class)
interface InjectionComponent {

    void inject(MainActivity activity);

}
