package sne.workorganizer;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import sne.workorganizer.util.Mix;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

/**
 * General purpose utilities for Espresso tests.
 */
public class TestUtils
{
    private static final String TAG = TestUtils.class.getName();
    private static final String GRANT_PERMISSION_ACTIVITY = "com.android.packageinstaller.permission.ui.GrantPermissionsActivity";
    private static final int PERMISSIONS_DIALOG_DELAY = 3;
    private static final int GRANT_BUTTON_INDEX = 1;

    private TestUtils()
    {
    }

    /**
     * Based on ideas from
     * http://stackoverflow.com/questions/33929937/android-marshmallow-test-permissions-with-espresso
     */
    public static void allowAllNeededPermissions()
    {
        Log.i(TAG, "allowAllNeededPermissions() called");
        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                Mix.sleep(PERMISSIONS_DIALOG_DELAY);
                // Espresso doesn't allow us to interact with system dialogs,
                // so we used UiAutomator, which is perfectly compatible with Espresso.
                UiDevice device = UiDevice.getInstance(getInstrumentation());
                boolean hasDialog = device.hasObject(By.clazz(GRANT_PERMISSION_ACTIVITY));
                while (hasDialog)
                {
                    UiObject allowPermissions = device.findObject(new UiSelector()
                            .clickable(true)
                            .checkable(false)
                            .instance(GRANT_BUTTON_INDEX));
                    if (allowPermissions.exists())
                    {
                        Log.i(TAG, "allowAllNeededPermissions() click on "+allowPermissions.getText());
                        allowPermissions.click();
                    }
                    else
                    {
                        break;
                    }

                    Mix.sleep(PERMISSIONS_DIALOG_DELAY);
                    hasDialog = device.hasObject(By.clazz(GRANT_PERMISSION_ACTIVITY));
                }
            }
        }
        catch (UiObjectNotFoundException e)
        {
            Log.e(TAG, "There is no permissions dialog to interact with");
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
                Mix.sleep(PERMISSIONS_DIALOG_DELAY);
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
                    Mix.sleep(PERMISSIONS_DIALOG_DELAY);
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
}
