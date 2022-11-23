package com.example.diecasthangar

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.diecasthangar.ui.DashboardFragment
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardFragmentTest {

    @Test
    fun testAddPostFragment() {
        val scenario = launchFragmentInContainer<DashboardFragment>(
            themeResId = R.style.Theme_DiecastHangar,
            initialState = Lifecycle.State.STARTED
        )

        Espresso.onView(ViewMatchers.withText("Welcome"))
            .check(ViewAssertions.matches((ViewMatchers.withText("Welcome"))))
    }


}