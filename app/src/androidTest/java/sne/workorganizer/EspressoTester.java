package sne.workorganizer;

import android.database.Cursor;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.CursorMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import sne.workorganizer.db.DatabaseHelper;
import sne.workorganizer.util.Mix;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EspressoTester
{
    private static final String TAG = EspressoTester.class.getName();
    private static final int PAUSE_NONE = -1;
    private static final int PAUSE_SHORT = 1;
    private static final int PAUSE_LONG = 3;
    private static final String CLIENT_NAME_PREFIX = "Espresso ";
    private static final String WORK_TITLE_PREFIX = "Tattoo ";

    @Rule
    public final ActivityTestRule<MainActivity> main = new ActivityTestRule<>(MainActivity.class, true);

    static private String _guid;
    static private String _clientName;
    static private String _workTitle;
    static private Map<Integer, Integer> _pauses = new HashMap<>();

//    @Test
//    public void useAppContext() throws Exception
//    {
//        // Context of the app under test.
//        Context appContext = InstrumentationRegistry.getTargetContext();
//
//        assertEquals("sne.workorganizer", appContext.getPackageName());
//    }

    @BeforeClass
    static public void globalInit()
    {
        _pauses.put(1, PAUSE_NONE);
        _pauses.put(2, PAUSE_NONE);
        _pauses.put(99, PAUSE_NONE);

        _guid = UUID.randomUUID().toString();
        _clientName = CLIENT_NAME_PREFIX + _guid;
        _workTitle = WORK_TITLE_PREFIX + _guid;
    }

    @Test
    public void t001_createClient()
    {
        int pause = _pauses.get(1);
        Mix.sleep(pause);

        // Open the overflow menu OR open the options menu,
        // depending on if the device has a hardware or software overflow menu button.
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        Mix.sleep(pause);

        // Go to Clients.
        onView(withText(R.string.clients)).perform(click());
        Mix.sleep(pause);

        // Go to Create Client form.
        onView(withText(R.string.create)).perform(click());
        Mix.sleep(pause);

        // Fill the form.
        onView(withId(R.id.client_name)).perform(typeText(_clientName));
        onView(withId(R.id.client_phone)).perform(typeText("1234567890"));
        onView(withId(R.id.client_email)).perform(typeText("espresso@gmail.com"));
        Mix.sleep(pause);

        // Save new client.
        onView(withText(R.string.save)).perform(click());
        Mix.sleep(pause);

        // Check existence of the new client.
        Matcher<View> rv = withId(R.id.client_list);
        onView(rv).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withText(_clientName)).check(matches(isDisplayed()));
        Mix.sleep(pause);
    }

    @Test
    public void t002_createWork()
    {
        int pause = _pauses.get(2);
        Mix.sleep(pause);

        // Go to Calendar
        onView(withId(R.id.menu_calendar)).perform(click());
        Mix.sleep(pause);

        // Go to Create Work form.
        onView(withText(R.string.create)).perform(click());
        Mix.sleep(pause);

        // Fill the form.
        onView(withId(R.id.select_client)).perform(typeText(CLIENT_NAME_PREFIX.substring(0,3)));
        Mix.sleep(pause);

        onData(allOf(is(instanceOf(Cursor.class)), CursorMatchers.withRowString(DatabaseHelper.CLIENTS_COL_FULLNAME, _clientName)))
                .inRoot(isPlatformPopup())
                .perform(click());
        Mix.sleep(pause);

        onView(withId(R.id.work_title)).perform(typeText(_workTitle));
        Mix.sleep(pause);

        onView(withId(R.id.work_price)).perform(typeText("1000"));
        Mix.sleep(pause);

        // Save new work.
        onView(withText(R.string.save)).perform(click());

        // Check existence of the new work.
        onView(withText(_workTitle)).check(matches(isDisplayed()));
    }


    @Test
    public void t099_deleteClient()
    {
        int pause = _pauses.get(99);
        Mix.sleep(pause);

        // Open the overflow menu OR open the options menu,
        // depending on if the device has a hardware or software overflow menu button.
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        Mix.sleep(pause);

        // Go to Clients.
        onView(withText(R.string.clients)).perform(click());
        Mix.sleep(pause);

        // Long click on item to open popup action menu.
        onView(withText(_clientName)).perform(longClick());
        Mix.sleep(pause);

        // Click on Delete and then confirm.
        onView(withText(R.string.delete)).perform(click());
        Mix.sleep(pause);
        onView(withText(R.string.ok)).perform(click());
        Mix.sleep(pause);

        // Check that the item does not exist.
        onView(withText(_clientName)).check(doesNotExist());
        Mix.sleep(pause);

        // Go to Journal
        pressBack();
        Mix.sleep(pause);

        // Go to Calendar
        onView(withId(R.id.menu_calendar)).perform(click());
        Mix.sleep(pause);

        onView(withText(_workTitle)).check(doesNotExist());
        Mix.sleep(pause);
    }
}
