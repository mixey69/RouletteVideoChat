package com.mixey69.roulettevideochat;

import javax.inject.Singleton;

import dagger.Component;


@Singleton
@Component(modules = MainModule.class)
interface InjectionComponent {

    void inject(MainActivity activity);

}
