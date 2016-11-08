package sne.workorganizer;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import sne.workorganizer.util.Mix;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.registerIdlingResources;
import static android.support.test.espresso.Espresso.unregisterIdlingResources;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest
{
    private static final int PAUSE_SHORT = 1;
    private static final int PAUSE_LONG = 3;
    @Rule
    public final ActivityTestRule<MainActivity> main = new ActivityTestRule<>(MainActivity.class, true);

//    @Test
//    public void useAppContext() throws Exception
//    {
//        // Context of the app under test.
//        Context appContext = InstrumentationRegistry.getTargetContext();
//
//        assertEquals("sne.workorganizer", appContext.getPackageName());
//    }

    @Test
    public void createClient()
    {
        Mix.sleep(PAUSE_LONG);

        // Open the overflow menu OR open the options menu,
        // depending on if the device has a hardware or software overflow menu button.
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        Mix.sleep(PAUSE_LONG);

        // Go to Clients.
        onView(withText(R.string.clients)).perform(click());
        Mix.sleep(PAUSE_LONG);

        // Go to Create Client form.
        onView(withText(R.string.create)).perform(click());
        Mix.sleep(PAUSE_LONG);

        onView(withId(R.id.client_name)).perform(typeText("Espresso"));
        onView(withId(R.id.client_phone)).perform(typeText("1234567890"));
        onView(withId(R.id.client_email)).perform(typeText("espresso@gmail.com"));
        Mix.sleep(PAUSE_LONG);

        //registerIdlingResources()
        onView(withText(R.string.save)).perform(click());
        //unregisterIdlingResources()
    }
}
