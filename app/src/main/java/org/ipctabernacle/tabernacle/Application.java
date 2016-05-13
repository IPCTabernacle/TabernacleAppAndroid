package org.ipctabernacle.tabernacle;

import com.firebase.client.Firebase;

/**
 * Created by Charles Koshy on 4/7/2016.
 */
public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
    }
}
