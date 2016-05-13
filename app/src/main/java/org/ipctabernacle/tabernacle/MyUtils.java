package org.ipctabernacle.tabernacle;

import android.content.Context;
import android.view.View;
import android.view.animation.AnimationUtils;

/**
 * Created by Charles Koshy on 4/5/2016.
 */
public class MyUtils {

    public void SlideUp(View view, Context context) {
        view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_down));
    }

    public void SlideDown(View view, Context context) {
        view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_up));
    }
}
