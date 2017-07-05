package com.mixey69.roulettevideochat;


public class Application extends android.app.Application {

    private static Application instance;
    private InjectionComponent component;

    public static Application getInstance() {
        return instance;
    }

    InjectionComponent getInjectionComponent(){
        return component;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        component = DaggerInjectionComponent.builder().mainModule(new MainModule(this)).build();
    }
}
