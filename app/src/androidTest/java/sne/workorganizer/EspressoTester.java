package sne.workorganizer;

import android.Manifest;
import android.database.Cursor;
import android.os.SystemClock;
import android.support.design.widget.TextInputEditText;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.CursorMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.SparseArray;
import android.util.SparseIntArray;
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
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
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

    private static final int PAUSE_NONE = 0;
    private static final int PAUSE_SHORT = 1000;
    private static final int PAUSE_LONG = 3000;
    private static final String CLIENT_NAME_PREFIX = "Espresso ";
    private static final String WORK_TITLE_PREFIX = "Tattoo ";

    @Rule
    public final ActivityTestRule<MainActivity> main = new ActivityTestRule<>(MainActivity.class, true);

    static private String _clientName1;
    static private String _workTitle1;
    static private String _clientName2;
    static private String _workTitle2;
    static private SparseIntArray _pauses = new SparseIntArray();

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
        _pauses.put(3, PAUSE_LONG);
        _pauses.put(10, PAUSE_NONE);
        _pauses.put(11, PAUSE_LONG);
        _pauses.put(99, PAUSE_NONE);

        String guid1 = UUID.randomUUID().toString();
        _clientName1 = CLIENT_NAME_PREFIX + guid1;
        _workTitle1 = WORK_TITLE_PREFIX + guid1;

        String guid2 = UUID.randomUUID().toString();
        _clientName2 = CLIENT_NAME_PREFIX + guid2;
        _workTitle2 = WORK_TITLE_PREFIX + guid2;
    }

//    @Test
//    public void t000_grantPermissions()
//    {
//        TestUtils.allowAllNeededPermissions();
//    }

    @Test
    public void t001_createClient()
    {
        TestUtils.allowAllNeededPermissions();

        int pause = _pauses.get(1);
        SystemClock.sleep(pause);

        // Open the overflow menu OR open the options menu,
        // depending on if the device has a hardware or software overflow menu button.
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        SystemClock.sleep(pause);

        // Go to Clients.
        onView(withText(R.string.clients)).perform(click());
        SystemClock.sleep(pause);

        // Go to Create Client form.
        onView(withText(R.string.create)).perform(click());
        SystemClock.sleep(pause);

        // Fill the form.
        onView(withId(R.id.client_name)).perform(typeText(_clientName1));
        onView(withId(R.id.client_phone)).perform(typeText("1234567890"));
        onView(withId(R.id.client_email)).perform(typeText("espresso@gmail.com"));
        SystemClock.sleep(pause);

        // Save new client.
        onView(withText(R.string.save)).perform(click());
        SystemClock.sleep(pause);

        // Check existence of the new client.
        Matcher<View> rv = withId(R.id.client_list);
        onView(rv).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withText(_clientName1)).check(matches(isDisplayed()));
        SystemClock.sleep(pause);
    }

    @Test
    public void t002_createWork()
    {
        TestUtils.allowAllNeededPermissions();

        int pause = _pauses.get(2);
        SystemClock.sleep(pause);

        // Go to Calendar
        onView(withId(R.id.menu_calendar)).perform(click());
        SystemClock.sleep(pause);

        // Go to Create Work form.
        onView(withText(R.string.create)).perform(click());
        SystemClock.sleep(pause);

        // Fill the form.
        onView(withId(R.id.select_client)).perform(typeText(CLIENT_NAME_PREFIX.substring(0,3)));
        SystemClock.sleep(pause);

        onData(allOf(is(instanceOf(Cursor.class)), CursorMatchers.withRowString(DatabaseHelper.CLIENTS_COL_FULLNAME, _clientName1)))
                .inRoot(isPlatformPopup())
                .perform(click());
        SystemClock.sleep(pause);

        onView(withId(R.id.work_title)).perform(typeText(_workTitle1));
        SystemClock.sleep(pause);

        onView(withId(R.id.work_price)).perform(typeText("1000"));
        SystemClock.sleep(pause);

        // Save new work.
        onView(withText(R.string.save)).perform(click());

        // Check existence of the new work.
        onView(withText(_workTitle1)).check(matches(isDisplayed()));
    }

    @Test
    public void t003_editWork()
    {
        TestUtils.allowAllNeededPermissions();

        int pause = _pauses.get(3);
        SystemClock.sleep(pause);

        // Long click on item to open popup action menu.
        onView(withText(_workTitle1)).perform(longClick());
        SystemClock.sleep(pause);

        onView(withText(R.string.edit)).perform(click());
        SystemClock.sleep(pause);

        String new_title = _workTitle1+" edited";
        onView(allOf(is(instanceOf(TextInputEditText.class)), withText(_workTitle1))).perform(clearText(), typeText(new_title));
        SystemClock.sleep(pause);

        // Save edited work.
        onView(withText(R.string.save)).perform(click());

        // Check existence of the edited work.
        onView(withText(new_title)).check(matches(isDisplayed()));
    }

    @Test
    public void t010_mainMenu()
    {
        TestUtils.allowAllNeededPermissions();

        int pause = _pauses.get(10);
        SystemClock.sleep(pause);

        // Open the overflow menu OR open the options menu,
        // depending on if the device has a hardware or software overflow menu button.
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        SystemClock.sleep(pause);

        // Menu About
        onView(withText(R.string.menu_about)).perform(click());
        SystemClock.sleep(pause);
        pressBack();

        // Menu Gallery Design
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        SystemClock.sleep(pause);
        onView(withText(R.string.menu_gallery_design)).perform(click());
        SystemClock.sleep(pause);
        pressBack();

        // Menu Gallery Work results
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        SystemClock.sleep(pause);
        onView(withText(R.string.menu_gallery_work)).perform(click());
        SystemClock.sleep(pause);
        pressBack();
    }

    private static void deleteClient(String clientName, int pause)
    {
        SystemClock.sleep(pause);

        // Open the overflow menu OR open the options menu,
        // depending on if the device has a hardware or software overflow menu button.
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        SystemClock.sleep(pause);

        // Go to Clients.
        onView(withText(R.string.clients)).perform(click());
        SystemClock.sleep(pause);

        // Long click on item to open popup action menu.
        onView(withText(clientName)).perform(longClick());
        SystemClock.sleep(pause);

        // Click on Delete and then confirm.
        onView(withText(R.string.delete)).perform(click());
        SystemClock.sleep(pause);
        onView(withText(R.string.ok)).perform(click());
        SystemClock.sleep(pause);

        // Check that the item does not exist.
        onView(withText(clientName)).check(doesNotExist());
        SystemClock.sleep(pause);
    }

    @Test
    public void t099_deleteClient()
    {
        TestUtils.allowAllNeededPermissions();

        int pause = _pauses.get(99);
        deleteClient(_clientName1, pause);

        // Go to Journal
        pressBack();
        SystemClock.sleep(pause);

        // Go to Calendar
        onView(withId(R.id.menu_calendar)).perform(click());
        SystemClock.sleep(pause);

        onView(withText(_workTitle1)).check(doesNotExist());
        SystemClock.sleep(pause);
    }

    @Test
    public void t011_createWorkWithClient()
    {
        TestUtils.allowAllNeededPermissions();

        int pause = _pauses.get(11);
        SystemClock.sleep(pause);

        // Go to Calendar
        onView(withId(R.id.menu_calendar)).perform(click());
        SystemClock.sleep(pause);

        // Go to Create Work form.
        onView(withText(R.string.create)).perform(click());
        SystemClock.sleep(pause);

        // Fill the form.
        onView(withId(R.id.select_client)).perform(typeText(_clientName2));
        SystemClock.sleep(pause);

        onView(withId(R.id.work_title)).perform(typeText(_workTitle2));
        SystemClock.sleep(pause);

        onView(withId(R.id.work_price)).perform(typeText("1000"));
        SystemClock.sleep(pause);

        // Save new work.
        onView(withText(R.string.save)).perform(click());
        SystemClock.sleep(pause);

        // Check existence of the new work.
        onView(withText(_workTitle2)).check(matches(isDisplayed()));

        deleteClient(_clientName2, pause);
        SystemClock.sleep(pause);
    }
}
