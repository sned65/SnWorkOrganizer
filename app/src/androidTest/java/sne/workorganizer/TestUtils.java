package sne.workorganizer;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

/**
 * General purpose utilities for Espresso tests.
 */
class TestUtils
{
    private static final String TAG = TestUtils.class.getName();
    private static final String PERMISSION_ALLOW_BUTTON_ID = "permission_allow_button";
    private static final String PERMISSION_MESSAGE = "permission_message";
    private static final int UI_AUTOMATOR_DELAY = 3000;
    private static final int GRANT_BUTTON_INDEX = 1;

    private TestUtils()
    {
    }

    /**
     * Based on ideas from
     * http://stackoverflow.com/questions/33929937/android-marshmallow-test-permissions-with-espresso
     */
    static void allowAllNeededPermissions()
    {
        Log.i(TAG, "allowAllNeededPermissions() called");
        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                SystemClock.sleep(UI_AUTOMATOR_DELAY);
                // Espresso doesn't allow us to interact with system dialogs,
                // so we used UiAutomator, which is perfectly compatible with Espresso.
                UiDevice device = UiDevice.getInstance(getInstrumentation());

                UiSelector selector = new UiSelector()
                        .resourceIdMatches(".*/"+PERMISSION_ALLOW_BUTTON_ID);
                UiSelector selector_message = new UiSelector()
                        .resourceIdMatches(".*/"+PERMISSION_MESSAGE);

                UiObject allowPermissions = device.findObject(selector);
                Log.i(TAG, "allowPermissions exists "+allowPermissions.exists());
                while (allowPermissions.exists())
                {
                    Log.i(TAG, "allowAllNeededPermissions() click on " + allowPermissions.getText()+" for "+permissionText(device, selector_message));
                    allowPermissions.click();

                    // Next permission
                    SystemClock.sleep(UI_AUTOMATOR_DELAY);
                    allowPermissions = device.findObject(selector);
                }
            }
        }
        catch (UiObjectNotFoundException e)
        {
            Log.e(TAG, "There is no permissions dialog to interact with");
        }
    }

    private static String permissionText(UiDevice device, UiSelector selector)
    {
        UiObject pm = device.findObject(selector);
        try
        {
            return pm.getText();
        }
        catch (UiObjectNotFoundException e)
        {
            return "";
        }
    }

    /**
     * http://stackoverflow.com/questions/33929937/android-marshmallow-test-permissions-with-espresso
     *
     * @param permissionNeeded The name of the permission being checked.
     */
    public static void allowPermissionsIfNeeded(String permissionNeeded)
    {
        Log.i(TAG, "allowPermissionsIfNeeded("+permissionNeeded+") called");
        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasNeededPermission(permissionNeeded))
            {
                SystemClock.sleep(UI_AUTOMATOR_DELAY);
                // Espresso doesn't allow us to interact with system dialogs,
                // so we used UiAutomator, which is perfectly compatible with Espresso.
                UiDevice device = UiDevice.getInstance(getInstrumentation());
//                UiObject unknown = device.findObject(new UiSelector()
//                        .clickable(true)
//                        .checkable(false)
//                        .index(0));
//                Log.i(TAG, "*** unknown: "+unknown.getClassName()+": "+unknown.getText());
                UiObject allowPermissions = device.findObject(new UiSelector()
                        .clickable(true)
                        .checkable(false)
                        .instance(GRANT_BUTTON_INDEX));
                Log.i(TAG, "*** allowPermissions: "+allowPermissions.getText());
                if (allowPermissions.exists())
                {
                    Log.i(TAG, "allowPermissionsIfNeeded() click");
                    allowPermissions.click();
                    SystemClock.sleep(UI_AUTOMATOR_DELAY);
                }
                Log.i(TAG, "hasNeededPermission -> "+hasNeededPermission(permissionNeeded));
            }
        }
        catch (UiObjectNotFoundException e)
        {
            Log.e(TAG, "There is no permissions dialog to interact with");
        }
    }

    private static boolean hasNeededPermission(String permissionNeeded)
    {
        Context context = InstrumentationRegistry.getTargetContext();
        int permissionStatus = ContextCompat.checkSelfPermission(context, permissionNeeded);
        Log.i(TAG, "hasNeededPermission("+permissionNeeded+") returns "+(permissionStatus == PackageManager.PERMISSION_GRANTED));
        return permissionStatus == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Selects picture from device.
     *
     * @param title text containing in the picture file name.
     * @return {@code true} if a picture is found and selected.
     */
    static boolean selectPicture(String title)
    {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        UiSelector selector = new UiSelector().textContains(title);
        UiObject picture_btn = device.findObject(selector);
        try
        {
            picture_btn.click();
            SystemClock.sleep(UI_AUTOMATOR_DELAY);
            return true;
        }
        catch (UiObjectNotFoundException e)
        {
            return false;
        }
    }

    /**
     *
     * @return current screen orientation. That is one of
     * the {@code Configuration} values for orientation, such as
     * {@code Configuration.ORIENTATION_LANDSCAPE}.
     */
    public static int getOrientation()
    {
        return InstrumentationRegistry.getTargetContext()
                .getResources().getConfiguration().orientation;
    }
}
