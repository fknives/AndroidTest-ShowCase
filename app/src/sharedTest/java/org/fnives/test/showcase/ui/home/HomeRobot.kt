package org.fnives.test.showcase.ui.home

import android.app.Instrumentation
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.fnives.test.showcase.R
import org.fnives.test.showcase.model.content.Content
import org.fnives.test.showcase.model.content.FavouriteContent
import org.fnives.test.showcase.testutils.robot.Robot
import org.fnives.test.showcase.testutils.viewactions.PullToRefresh
import org.fnives.test.showcase.testutils.viewactions.WithDrawable
import org.fnives.test.showcase.testutils.viewactions.notIntended
import org.fnives.test.showcase.ui.ActivityClassHolder
import org.hamcrest.Matchers.allOf

class HomeRobot : Robot {

    override fun init() {
        Intents.init()
        Intents.intending(IntentMatchers.hasComponent(ActivityClassHolder.authActivity().java.canonicalName))
            .respondWith(Instrumentation.ActivityResult(0, null))
    }

    override fun release() {
        Intents.release()
    }

    fun assertNavigatedToAuth() = apply {
        Intents.intended(IntentMatchers.hasComponent(ActivityClassHolder.authActivity().java.canonicalName))
    }

    fun assertDidNotNavigateToAuth() = apply {
        notIntended(IntentMatchers.hasComponent(ActivityClassHolder.authActivity().java.canonicalName))
    }

    fun clickSignOut() = apply {
        Espresso.onView(withId(R.id.logout_cta)).perform(click())
    }

    fun assertContainsItem(item: FavouriteContent) = apply {
        val isFavouriteResourceId = if (item.isFavourite) {
            R.drawable.favorite_24
        } else {
            R.drawable.favorite_border_24
        }
        Espresso.onView(
            allOf(
                withChild(allOf(withText(item.content.title), withId(R.id.title))),
                withChild(allOf(withText(item.content.description), withId(R.id.description))),
                withChild(allOf(withId(R.id.favourite_cta), WithDrawable(isFavouriteResourceId)))
            )
        )
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    fun clickOnContentItem(item: Content) = apply {
        Espresso.onView(
            allOf(
                withId(R.id.favourite_cta),
                withParent(
                    allOf(
                        withChild(allOf(withText(item.title), withId(R.id.title))),
                        withChild(allOf(withText(item.description), withId(R.id.description)))
                    )
                )
            )
        )
            .perform(click())
    }

    fun swipeRefresh() = apply {
        Espresso.onView(withId(R.id.swipe_refresh_layout)).perform(PullToRefresh())
    }

    fun assertContainsNoItems() = apply {
        Espresso.onView(withId(R.id.recycler))
            .check(matches(hasChildCount(0)))
    }

    fun assertContainsError() = apply {
        Espresso.onView(withId(R.id.error_message))
            .check(matches(allOf(isDisplayed(), withText(R.string.something_went_wrong))))
    }
}
