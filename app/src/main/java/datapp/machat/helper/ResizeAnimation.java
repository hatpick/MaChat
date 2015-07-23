package datapp.machat.helper;

/**
 * Created by hat on 7/8/15.
 */

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by hat on 9/9/14.
 */
public class ResizeAnimation extends Animation {

    private int startHeight;
    private int deltaHeight; // distance between start and end height
    private int startWidth;
    private int deltaWidth; // distance between start and end height
    private View view;

    public ResizeAnimation (View v) {
        this.view = v;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        view.getLayoutParams().height = (int) (startHeight + deltaHeight * interpolatedTime);
        view.getLayoutParams().width = (int) (startWidth + deltaWidth * interpolatedTime);
        view.requestLayout();
    }

    public void setParams(int heightStart, int heightEnd, int widthStart, int widthEnd) {

        this.startHeight = heightStart;
        deltaHeight = heightEnd - startHeight;
        this.startWidth = widthStart;
        deltaWidth = widthEnd - widthStart;
    }

    /**
     * set the duration for the hideshowanimation
     */
    @Override
    public void setDuration(long durationMillis) {
        super.setDuration(durationMillis);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}
