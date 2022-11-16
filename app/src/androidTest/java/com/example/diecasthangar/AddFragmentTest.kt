package com.example.diecasthangar

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.diecasthangar.data.Post
import com.example.diecasthangar.profile.presentation.ProfileFragment
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddFragmentTest {

    @Test
    fun testAddPostFragment() {
        val scenario = launchFragmentInContainer<AddPostFragment>(
            themeResId = R.style.Theme_DiecastHangar,
            initialState = Lifecycle.State.STARTED
        )

        Espresso.onView(ViewMatchers.withId(R.id.add_post_btn_add))
            .check(ViewAssertions.matches((ViewMatchers.withText("Add"))))
    }

    @Test
    fun testEditPostMode() {
        val fragmentArgs = bundleOf("post" to Post(),"editMode" to true)
        val scenario = launchFragmentInContainer<AddPostFragment>(
            themeResId = R.style.Theme_DiecastHangar,
            initialState = Lifecycle.State.STARTED,
        )
        Espresso.onView(ViewMatchers.withId(R.id.add_post_btn_add))
            .check(ViewAssertions.matches((ViewMatchers.withText("Edit"))))

    }
}