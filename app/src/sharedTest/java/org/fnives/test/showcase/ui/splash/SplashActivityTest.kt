package org.fnives.test.showcase.ui.splash

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.fnives.test.showcase.testutils.MockServerScenarioSetupTestRule
import org.fnives.test.showcase.testutils.ReloadKoinModulesIfNecessaryTestRule
import org.fnives.test.showcase.testutils.configuration.SpecificTestConfigurationsFactory
import org.fnives.test.showcase.testutils.idling.Disposable
import org.fnives.test.showcase.testutils.idling.NetworkSynchronization
import org.fnives.test.showcase.testutils.robot.RobotTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest

@Suppress("TestFunctionName")
@RunWith(AndroidJUnit4::class)
class SplashActivityTest : KoinTest {

    private lateinit var activityScenario: ActivityScenario<SplashActivity>

    private val splashRobot: SplashRobot get() = robotTestRule.robot

    @Rule
    @JvmField
    val robotTestRule = RobotTestRule(SplashRobot())

    @Rule
    @JvmField
    val mainDispatcherTestRule = SpecificTestConfigurationsFactory.createMainDispatcherTestRule()

    @Rule
    @JvmField
    val mockServerScenarioSetupTestRule = MockServerScenarioSetupTestRule()

    @Rule
    @JvmField
    val reloadKoinModulesIfNecessaryTestRule = ReloadKoinModulesIfNecessaryTestRule()

    lateinit var disposable: Disposable

    @Before
    fun setUp() {
        SpecificTestConfigurationsFactory.createServerTypeConfiguration()
            .invoke(mockServerScenarioSetupTestRule.mockServerScenarioSetup)
        disposable = NetworkSynchronization.registerNetworkingSynchronization()
    }

    @After
    fun tearDown() {
        activityScenario.close()
        disposable.dispose()
    }

    /** GIVEN loggedInState WHEN opened after some time THEN MainActivity is started */
    @Test
    fun loggedInStateNavigatesToHome() {
        splashRobot.setupLoggedInState(mainDispatcherTestRule, mockServerScenarioSetupTestRule.mockServerScenarioSetup)

        activityScenario = ActivityScenario.launch(SplashActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)

        mainDispatcherTestRule.advanceTimeBy(501)

        splashRobot.assertHomeIsStarted()
            .assertAuthIsNotStarted()

        workaroundForActivityScenarioCLoseLockingUp()
    }

    /** GIVEN loggedOffState WHEN opened after some time THEN AuthActivity is started */
    @Test
    fun loggedOutStatesNavigatesToAuthentication() {
        splashRobot.setupLoggedOutState(mainDispatcherTestRule)
        activityScenario = ActivityScenario.launch(SplashActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)

        mainDispatcherTestRule.advanceTimeBy(501)

        splashRobot.assertAuthIsStarted()
            .assertHomeIsNotStarted()

        workaroundForActivityScenarioCLoseLockingUp()
    }

    /** GIVEN loggedOffState and not enough time WHEN opened THEN no activity is started */
    @Test
    fun loggedOutStatesNotEnoughTime() {
        splashRobot.setupLoggedOutState(mainDispatcherTestRule)
        activityScenario = ActivityScenario.launch(SplashActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)

        mainDispatcherTestRule.advanceTimeBy(10)

        splashRobot.assertAuthIsNotStarted()
            .assertHomeIsNotStarted()
    }

    /** GIVEN loggedInState and not enough time WHEN opened THEN no activity is started */
    @Test
    fun loggedInStatesNotEnoughTime() {
        splashRobot.setupLoggedInState(mainDispatcherTestRule, mockServerScenarioSetupTestRule.mockServerScenarioSetup)

        activityScenario = ActivityScenario.launch(SplashActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)

        mainDispatcherTestRule.advanceTimeBy(10)

        splashRobot.assertHomeIsNotStarted()
            .assertAuthIsNotStarted()
    }

    /**
     * This should not be needed, we shouldn't use sleep ever.
     * However, it seems to be and issue described here: https://github.com/android/android-test/issues/676
     *
     * If an activity is finished in code, the ActivityScenario.close() can hang 30 to 45 seconds.
     * This sleeps let's the Activity finish it state change and unlocks the ActivityScenario.
     *
     * As soon as that issue is closed, this should be removed as well.
     */
    private fun workaroundForActivityScenarioCLoseLockingUp() {
        Thread.sleep(1000L)
    }
}
