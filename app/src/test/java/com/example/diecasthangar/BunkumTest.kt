package com.example.diecasthangar

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diecasthangar.data.Post
import com.example.diecasthangar.domain.adapters.PostRecyclerAdapter
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals


class PostsAdapterTest {

    //val context: Context

    private val context: Context
        get() {
            TODO()
        }

    @Before
    fun setup()
    {
        //context = RuntimeEnvironment.application;
    }

    @Test
    fun postsAdapterViewRecyclingCaption()
    {
        // Set up input
/*        val posts: List<Post> = listOf()


        val adapter =  PostRecyclerAdapter()

        val rv = RecyclerView(context)
        rv.layoutManager = LinearLayoutManager (context)

        // Run test
        val viewHolder :PostRecyclerAdapter.ViewHolder  =
        adapter.onCreateViewHolder(rv, 0)

        adapter.onBindViewHolder(viewHolder, 0)

        // JUnit Assertion
        assertEquals(View.GONE, viewHolder.rightImageButton.visibility)*/

/*        // AssertJ-Android Assertion
        assertThat(viewHolder.tvCaption).isGone();

        adapter.onBindViewHolder(viewHolder, 1);

        // JUnit Assertion
        assertEquals("Steph: We Won!!!", viewHolder.tvCaption.getText().toString());

        // AssertJ-Android Assertion
        assertThat(viewHolder.tvCaption).isVisible().containsText("Won");

        assertThat(adapter).hasItemCount(2);*/
    }
}
