package com.example.diecasthangar

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.example.diecasthangar.ui.onboarding.OnboardingFragment
import org.junit.Test

class OnboardingFragmentTest {

    @Test
    fun testProfileFragment() {
        val scenario = launchFragmentInContainer<OnboardingFragment>(themeResId = R.style.Theme_DiecastHangar,
            initialState = Lifecycle.State.STARTED
        )
        //check proper fields displayed
        Espresso.onView(ViewMatchers.withId(R.id.reg_password_edit_text))
            .check(ViewAssertions.matches((ViewMatchers.isDisplayed())))
        Espresso.onView(ViewMatchers.withId(R.id.reg_username_edit_text))
            .check(ViewAssertions.matches((ViewMatchers.isDisplayed())))
        Espresso.onView(ViewMatchers.withId(R.id.reg_email_edit_text))
            .check(ViewAssertions.matches((ViewMatchers.isDisplayed())))

        //move to login mode and check fields
        Espresso.onView(ViewMatchers.withId(R.id.text_to_login)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.login_button))
            .check(ViewAssertions.matches((ViewMatchers.isDisplayed())))
        Espresso.onView(ViewMatchers.withId(R.id.login_email_edit_text))
            .check(ViewAssertions.matches((ViewMatchers.isDisplayed())))
        Espresso.onView(ViewMatchers.withId(R.id.login_password_edit_text))
            .check(ViewAssertions.matches((ViewMatchers.isDisplayed())))

        //check recover password popup
        Espresso.onView(ViewMatchers.withId(R.id.recover_password)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText("reset password"))
            .check(ViewAssertions.matches((ViewMatchers.isDisplayed())))
        Espresso.onView(ViewMatchers.withText("cancel"))
            .check(ViewAssertions.matches((ViewMatchers.isDisplayed())))
        Espresso.onView(ViewMatchers.withText("send"))
            .check(ViewAssertions.matches((ViewMatchers.isDisplayed())))
    }
}