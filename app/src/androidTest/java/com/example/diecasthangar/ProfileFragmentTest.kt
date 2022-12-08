package com.example.diecasthangar

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.diecasthangar.ui.profile.ProfileFragment
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileFragmentTest {

    @Test
    fun testProfileFragment() {
        val scenario = launchFragmentInContainer<ProfileFragment>(themeResId = R.style.Theme_DiecastHangar,
            initialState = Lifecycle.State.INITIALIZED
        )
        scenario.moveToState(Lifecycle.State.RESUMED)
        Espresso.onView(ViewMatchers.withId(R.id.profile_text_bio))
            .check(ViewAssertions.matches((ViewMatchers.isDisplayed())))
        Espresso.onView(ViewMatchers.withId(R.id.profile_avatar))
            .check(ViewAssertions.matches((ViewMatchers.isDisplayed())))

        // check that editing views are displayed after edit button clicked
        Espresso.onView(ViewMatchers.withId(R.id.profile_btn_edit)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.profile_edit_text_bio))
            .check(ViewAssertions.matches((ViewMatchers.isDisplayed())))
        Espresso.onView(ViewMatchers.withId(R.id.profile_edit_avatar))
            .check(ViewAssertions.matches((ViewMatchers.isDisplayed())))
        Espresso.onView(ViewMatchers.withId(R.id.profile_btn_save))
            .check(ViewAssertions.matches((ViewMatchers.isDisplayed())))

        //check that display views are visible again
        Espresso.onView(ViewMatchers.withId(R.id.profile_btn_save)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.profile_text_bio))
            .check(ViewAssertions.matches((ViewMatchers.isDisplayed())))
        Espresso.onView(ViewMatchers.withId(R.id.profile_avatar))
            .check(ViewAssertions.matches((ViewMatchers.isDisplayed())))


        //check that selector buttons change visible views properly
        Espresso.onView(ViewMatchers.withId(R.id.profile_btn_posts)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.profile_post_recycler))
            .check(ViewAssertions.matches((ViewMatchers.isDisplayed())))

        Espresso.onView(ViewMatchers.withId(R.id.profile_btn_models)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.profile_model_recycler))
            .check(ViewAssertions.matches((ViewMatchers.isDisplayed())))

        Espresso.onView(ViewMatchers.withId(R.id.profile_btn_friends)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.profile_friend_recycler))
           .check(ViewAssertions.matches((ViewMatchers.isDisplayed())))


    }
}