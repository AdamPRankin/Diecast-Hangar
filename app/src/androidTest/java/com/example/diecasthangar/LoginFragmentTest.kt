package com.example.diecasthangar

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.example.diecasthangar.onboarding.presentation.LoginFragment
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginFragmentTest {
    private val TAG = "LoginFragmentTest"


        @Test
        fun testEventFragment() {
            val scenario = launchFragmentInContainer<LoginFragment>(
                initialState = Lifecycle.State.INITIALIZED
            )
            scenario.moveToState(Lifecycle.State.RESUMED)
            onView(withId(R.id.recover_password)).perform(click())
            onView(withText("reset password"))
                .check(matches((isDisplayed())))
            onView(withText("cancel"))
                .check(matches((isDisplayed())))
            onView(withText("send"))
                .check(matches((isDisplayed())))
        }

}