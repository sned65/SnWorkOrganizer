package sne.workorganizer;

import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.util.Log;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Nikolay_Smirnov on 08.11.2016.
 * https://github.com/chiuki/espresso-samples/blob/a70a343862a4199804f6fdffd9173cd453bb8d11/idling-resource-elapsed-time/app/src/androidTest/java/com/sqisland/espresso/idling_resource/elapsed_time/ElapsedTimeIdlingResource.java
 */
@Deprecated
public class WaitForComponentIdlingResource implements IdlingResource
{
    private static final String TAG = WaitForComponentIdlingResource.class.getName();
    private ResourceCallback _resourceCallback;
    private int _componentId;
    private long _endTime;

    public WaitForComponentIdlingResource(int componentId, long timeout)
    {
        _componentId = componentId;
        _endTime = System.currentTimeMillis() + timeout;
        // TODO
    }

    @Override
    public String getName()
    {
        // TODO
        return TAG;
    }

    @Override
    public boolean isIdleNow()
    {
        Log.i(TAG, "isIdleNow() called");
        try
        {
            //onView(withId(_componentId)).check(matches(isDisplayed())).perform(click());
            onView(withId(R.id.client_list)).check(matches(isDisplayed())); // should throw NoMatchingViewException
            Log.i(TAG, "isIdleNow() component found");
            if (_resourceCallback != null)
            {
                _resourceCallback.onTransitionToIdle();
            }
            return true;
        }
        catch (NoMatchingViewException e)
        {
            Log.i(TAG, "isIdleNow() component NOT found: "+e.getMessage());
            if (System.currentTimeMillis() > _endTime)
            {
                Log.i(TAG, "isIdleNow() timeout");
                if (_resourceCallback != null)
                {
                    _resourceCallback.onTransitionToIdle();
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback)
    {
        _resourceCallback = callback;
    }
}
