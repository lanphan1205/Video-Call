package com.example.videocall.UnitTests;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;

import com.example.videocall.MainActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class MainActivityWidgets {
    private Context context = ApplicationProvider.getApplicationContext();

    @Test
    public void CheckInitialBooleanValues() {
        // Given a Context object retrieved from Robolectric...
        UnitTestMainActivity MainActivityUnderTest = new UnitTestMainActivity();

        // get status of initialised values
        boolean audioStatus = MainActivityUnderTest.audioMuted;
        boolean videoStatus = MainActivityUnderTest.videoMuted;

        // ...then the result should be the expected one.
        assertFalse(audioStatus);
        assertFalse(videoStatus);
    }

}
