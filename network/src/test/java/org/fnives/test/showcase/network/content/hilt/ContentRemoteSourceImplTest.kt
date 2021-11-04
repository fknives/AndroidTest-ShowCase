package org.fnives.test.showcase.network.content.hilt

import kotlinx.coroutines.runBlocking
import org.fnives.test.showcase.network.DaggerTestNetworkComponent
import org.fnives.test.showcase.network.content.ContentRemoteSourceImpl
import org.fnives.test.showcase.network.mockserver.ContentData
import org.fnives.test.showcase.network.mockserver.scenario.content.ContentScenario
import org.fnives.test.showcase.network.session.NetworkSessionLocalStorage
import org.fnives.test.showcase.network.shared.MockServerScenarioSetupExtensions
import org.fnives.test.showcase.network.shared.exceptions.NetworkException
import org.fnives.test.showcase.network.shared.exceptions.ParsingException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.inject
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import javax.inject.Inject

@Suppress("TestFunctionName")
class ContentRemoteSourceImplTest {

    @Inject
    internal lateinit var sut: ContentRemoteSourceImpl

    @RegisterExtension
    @JvmField
    val mockServerScenarioSetupExtensions = MockServerScenarioSetupExtensions()
    private lateinit var mockNetworkSessionLocalStorage: NetworkSessionLocalStorage
    private val mockServerScenarioSetup
        get() = mockServerScenarioSetupExtensions.mockServerScenarioSetup

    @BeforeEach
    fun setUp() {
        mockNetworkSessionLocalStorage = mock()
        DaggerTestNetworkComponent.builder()
            .setBaseUrl(mockServerScenarioSetupExtensions.url)
            .setEnableLogging(true)
            .setNetworkSessionLocalStorage(mockNetworkSessionLocalStorage)
            .setNetworkSessionExpirationListener(mock())
            .build()
            .inject(this)
    }

    @DisplayName("GIVEN successful response WHEN getting content THEN its parsed and returned correctly")
    @Test
    fun successResponseParsing() = runBlocking {
        whenever(mockNetworkSessionLocalStorage.session).doReturn(ContentData.loginSuccessResponse)
        mockServerScenarioSetup.setScenario(ContentScenario.Success(false))
        val expected = ContentData.contentSuccess

        val actual = sut.get()

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN successful response WHEN getting content THEN the request is setup properly")
    @Test
    fun successResponseRequestIsCorrect() = runBlocking {
        whenever(mockNetworkSessionLocalStorage.session).doReturn(ContentData.loginSuccessResponse)
        mockServerScenarioSetup.setScenario(ContentScenario.Success(false), false)

        sut.get()
        val request = mockServerScenarioSetup.takeRequest()

        Assertions.assertEquals("GET", request.method)
        Assertions.assertEquals("Android", request.getHeader("Platform"))
        Assertions.assertEquals(ContentData.loginSuccessResponse.accessToken, request.getHeader("Authorization"))
        Assertions.assertEquals("/content", request.path)
        Assertions.assertEquals("", request.body.readUtf8())
    }

    @DisplayName("GIVEN response with missing Field WHEN getting content THEN invalid is ignored others are returned")
    @Test
    fun dataMissingFieldIsIgnored() = runBlocking {
        whenever(mockNetworkSessionLocalStorage.session).doReturn(ContentData.loginSuccessResponse)
        mockServerScenarioSetup.setScenario(ContentScenario.SuccessWithMissingFields(false))

        val expected = ContentData.contentSuccessWithMissingFields

        val actual = sut.get()

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN error response WHEN getting content THEN network request is thrown")
    @Test
    fun errorResponseResultsInNetworkException() {
        whenever(mockNetworkSessionLocalStorage.session).doReturn(ContentData.loginSuccessResponse)
        mockServerScenarioSetup.setScenario(ContentScenario.Error(false))

        Assertions.assertThrows(NetworkException::class.java) {
            runBlocking { sut.get() }
        }
    }

    @DisplayName("GIVEN unexpected json response WHEN getting content THEN parsing request is thrown")
    @Test
    fun unexpectedJSONResultsInParsingException() {
        whenever(mockNetworkSessionLocalStorage.session).doReturn(ContentData.loginSuccessResponse)
        mockServerScenarioSetup.setScenario(ContentScenario.UnexpectedJsonAsSuccessResponse(false))

        Assertions.assertThrows(ParsingException::class.java) {
            runBlocking { sut.get() }
        }
    }

    @DisplayName("GIVEN malformed json response WHEN getting content THEN parsing request is thrown")
    @Test
    fun malformedJSONResultsInParsingException() {
        whenever(mockNetworkSessionLocalStorage.session).doReturn(ContentData.loginSuccessResponse)
        mockServerScenarioSetup.setScenario(ContentScenario.MalformedJsonAsSuccessResponse(false))

        Assertions.assertThrows(ParsingException::class.java) {
            runBlocking { sut.get() }
        }
    }
}
