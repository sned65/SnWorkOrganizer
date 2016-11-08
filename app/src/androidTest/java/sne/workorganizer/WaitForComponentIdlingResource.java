package sne.workorganizer;

import android.support.test.espresso.IdlingResource;

/**
 * Created by Nikolay_Smirnov on 08.11.2016.
 * https://github.com/chiuki/espresso-samples/blob/a70a343862a4199804f6fdffd9173cd453bb8d11/idling-resource-elapsed-time/app/src/androidTest/java/com/sqisland/espresso/idling_resource/elapsed_time/ElapsedTimeIdlingResource.java
 */

public class WaitForComponentIdlingResource implements IdlingResource
{
    private ResourceCallback _resourceCallback;

    public WaitForComponentIdlingResource()
    {
        // TODO
    }

    @Override
    public String getName()
    {
        // TODO
        return null;
    }

    @Override
    public boolean isIdleNow()
    {
        // TODO
        return false;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback)
    {
        _resourceCallback = callback;
    }
}
