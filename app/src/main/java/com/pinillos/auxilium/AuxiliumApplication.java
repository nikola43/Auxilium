package com.pinillos.auxilium;

import android.app.Application;

/**
 * Created by miguel-windoews on 23/11/2017.
 */

public class AuxiliumApplication extends Application {

    private static AuxiliumApplication mSharedInstance;

    public AuxiliumApplication(){
        mSharedInstance = this;
    }

    public static AuxiliumApplication getInstance(){
        return mSharedInstance;
    }
}
