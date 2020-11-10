package com.sdp.recipebuilder;

import android.content.Context;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    private String stringToBeTyped;

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule
            = new ActivityScenarioRule<MainActivity>(MainActivity.class);

    @Before
    public void initValidString() {
        // Specify a valid string.
        stringToBeTyped = "HELLO THIS IS A TEST MESSAGE. STANDBY.";
    }

    @Test
    public void changeText_sameActivity() {
        // Type text and then press the button.
        onView(withId(R.id.etNewStep))
                .perform(typeText(stringToBeTyped), closeSoftKeyboard());
        onView(withId(R.id.btnAddStep)).perform(click());

        // Check that the text was added.
        onView(withText((stringToBeTyped)))
                .check(matches(isDisplayed()));

    }

    @Test
    public void add_Recipe() {
        for (int i = 1; i < 7; i++) {
            onView(withId(R.id.etNewStep))
                    .perform(typeText("SAMPLE STEP " + i), closeSoftKeyboard());
            onView(withId(R.id.btnAddStep)).perform(click());
        }
        onView(withId(R.id.etNewStep))
                .perform(typeText(stringToBeTyped), closeSoftKeyboard());
        onView(withId(R.id.btnAddStep)).perform(click());


        // Check the string is in the list by clicking it in the list.
        onData(allOf(is(instanceOf(String.class)), is(stringToBeTyped))).perform(click());
    }

    @Test
    public void delete_Recipe() {
        stringToBeTyped = "TESTING DELETE STRING.";
        onView(withId(R.id.etNewStep))
                .perform(typeText(stringToBeTyped), closeSoftKeyboard());
        onView(withId(R.id.btnAddStep)).perform(click());

        // Check string is in list and long click to delete.
        onView(withId(R.id.mic)).perform(longClick());
    }

    @Test
    public void voice_ClickError() {
        onView(withId(R.id.mic)).perform(click());
        onView(withId(R.id.mic)).perform(click());

        onView(withText(("ERROR CODE: 8")))
                .check(matches(isDisplayed()));
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.sdp.recipebuilder", appContext.getPackageName());
    }
}