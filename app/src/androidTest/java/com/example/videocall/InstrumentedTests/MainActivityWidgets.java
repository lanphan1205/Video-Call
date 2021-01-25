package com.example.videocall.InstrumentedTests;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.videocall.MainActivity;
import com.example.videocall.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static com.example.videocall.InstrumentedTests.EspressoConfig.EspressoTestsMatchers.withDrawable;

public class MainActivityWidgets {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule
            = new ActivityScenarioRule<>(MainActivity.class);

    /*
        check if all widgets on the main layout are displayed
     */

    @Test
    public void mainLayoutJoinButton_displayed() {
        // Assert
        onView(withId(R.id.joinBtn)).check(matches(isDisplayed()));
    }

    @Test
    public void mainLayoutAudioButton_displayed() {
        // Assert
        onView(withId(R.id.audioBtn)).check(matches(isDisplayed()));
    }

    @Test
    public void mainLayoutLeaveButton_displayed() {
        // Assert
        onView(withId(R.id.leaveBtn)).check(matches(isDisplayed()));
    }

    @Test
    public void mainLayoutCameraButton_displayed() {
        // Assert
        onView(withId(R.id.cameraBtn)).check(matches(isDisplayed()));
    }

    @Test
    public void mainLayoutBackgroundVideoContainer_displayed() {
        // Assert
        onView(withId(R.id.bg_video_container)).check(matches(isDisplayed()));
    }

    @Test
    public void mainLayoutFloatingVideoContainer_displayed() {
        // Assert
        onView(withId(R.id.floating_video_container)).check(matches(isDisplayed()));
    }


    /*
        check if all the widgets on the main layout are clickable
     */

    @Test
    public void mainLayoutJoinButton_Clickable() {
        // Assert
        onView(withId(R.id.joinBtn)).check(matches(isClickable()));
    }

    @Test
    public void mainLayoutAudioButton_Clickable() {
        // Assert
        onView(withId(R.id.audioBtn)).check(matches(isClickable()));
    }

    @Test
    public void mainLayoutLeaveButton_Clickable() {
        // Assert
        onView(withId(R.id.leaveBtn)).check(matches(isClickable()));
    }

    @Test
    public void mainLayoutCameraButton_Clickable() {
        // Assert
        onView(withId(R.id.cameraBtn)).check(matches(isClickable()));
    }

    @Test
    public void mainLayoutBackgroundVideoContainer_NotClickable() {
        // Assert
        onView(withId(R.id.bg_video_container)).check(matches(not(isClickable())));
    }

    @Test
    public void mainLayoutFloatingVideoContainer_NotClickable() {
        // Assert
        onView(withId(R.id.floating_video_container)).check(matches(not(isClickable())));
    }



    /*
        check that widgets can carry out intended actions. I'm not sure how to check things that are private.
        It shouldn't take that much effort to test intents, but I'm not sure how to test whether a method of the
        external lib is called/called correctly.
     */

    @Test
    public void ToggleAudioBeforeVideoOn() {
        // Audio state should stay at unmute
        onView(withId(R.id.audioBtn)).perform(click());
        onView(withId(R.id.audioBtn)).check(matches(withDrawable(R.drawable.icons8_unmute_96)));
        
        onView(withId(R.id.audioBtn)).perform(click());
        onView(withId(R.id.audioBtn)).check(matches(withDrawable(R.drawable.icons8_unmute_96)));

    }

    @Test
    public void ToggleAudioAfterVideoOn() {
        // Audio state should start at unmute and be able to toggle
        onView(withId(R.id.joinBtn)).perform(click());
        onView(withId(R.id.audioBtn)).check(matches(withDrawable(R.drawable.icons8_unmute_96)));

        onView(withId(R.id.audioBtn)).perform(click());
        onView(withId(R.id.audioBtn)).check(matches(withDrawable(R.drawable.icons8_mute_96)));

        onView(withId(R.id.audioBtn)).perform(click());
        onView(withId(R.id.audioBtn)).check(matches(withDrawable(R.drawable.icons8_unmute_96)));

    }

    @Test
    public void ToggleCameraBeforeVideoOn() {
        // Camera state should stay at on video mode
        onView(withId(R.id.cameraBtn)).perform(click());
        onView(withId(R.id.cameraBtn)).check(matches(withDrawable(R.drawable.icons8_video_call_96)));
        
        onView(withId(R.id.cameraBtn)).perform(click());
        onView(withId(R.id.cameraBtn)).check(matches(withDrawable(R.drawable.icons8_video_call_96)));

    }

    @Test
    public void ToggleCameraAfterVideoOn() {
        // Camera state should start at video mode and be able to toggle
        onView(withId(R.id.joinBtn)).perform(click());
        onView(withId(R.id.cameraBtn)).check(matches(withDrawable(R.drawable.icons8_video_call_96)));

        onView(withId(R.id.cameraBtn)).perform(click());
        onView(withId(R.id.cameraBtn)).check(matches(withDrawable(R.drawable.icons8_no_video_96)));

        onView(withId(R.id.cameraBtn)).perform(click());
        onView(withId(R.id.cameraBtn)).check(matches(withDrawable(R.drawable.icons8_video_call_96)));

    }

    @Test
    public void JoinButton_JoinAndEndCall() {
        // click join button
        onView(withId(R.id.joinBtn)).perform(click());

        // check if call is joined


        // click the leave button
        onView(withId(R.id.leaveBtn)).perform(click());

        // check if call has ended
    }

    //TODO: test that the functionality wrt external lib works


}
