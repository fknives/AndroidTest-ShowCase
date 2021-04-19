package org.fnives.test.showcase.core.content

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.fnives.test.showcase.core.shared.UnexpectedException
import org.fnives.test.showcase.model.content.Content
import org.fnives.test.showcase.model.content.ContentId
import org.fnives.test.showcase.model.content.ImageUrl
import org.fnives.test.showcase.model.shared.Resource
import org.fnives.test.showcase.network.content.ContentRemoteSource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

@Suppress("TestFunctionName")
internal class ContentRepositoryTest {

    private lateinit var sut: ContentRepository
    private lateinit var mockContentRemoteSource: ContentRemoteSource
    private lateinit var testDispatcher: TestCoroutineDispatcher

    @BeforeEach
    fun setUp() {
        testDispatcher = TestCoroutineDispatcher()
        testDispatcher.pauseDispatcher()
        mockContentRemoteSource = mock()
        sut = ContentRepository(mockContentRemoteSource)
    }

    @DisplayName("GIVEN no interaction THEN remote source is not called")
    @Test
    fun fetchingIsLazy() {
        verifyNoMoreInteractions(mockContentRemoteSource)
    }

    @DisplayName("GIVEN content response WHEN content observed THEN loading AND data is returned")
    @Test
    fun happyFlow() = runBlockingTest {
        val expected = listOf(
                Resource.Loading(),
                Resource.Success(listOf(Content(ContentId("a"), "", "", ImageUrl(""))))
        )
        whenever(mockContentRemoteSource.get()).doReturn(listOf(Content(ContentId("a"), "", "", ImageUrl(""))))

        val actual = sut.contents.take(2).toList()

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN content error WHEN content observed THEN loading AND data is returned")
    @Test
    fun errorFlow() = runBlockingTest {
        val exception = RuntimeException()
        val expected = listOf(
                Resource.Loading(),
                Resource.Error<List<Content>>(UnexpectedException(exception))
        )
        whenever(mockContentRemoteSource.get()).doThrow(exception)

        val actual = sut.contents.take(2).toList()

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN saved cache WHEN collected THEN cache is returned")
    @Test
    fun verifyCaching() = runBlockingTest {
        val content = Content(ContentId("1"), "", "", ImageUrl(""))
        val expected = listOf(Resource.Success(listOf(content)))
        whenever(mockContentRemoteSource.get()).doReturn(listOf(content))
        sut.contents.take(2).toList()

        val actual = sut.contents.take(1).toList()

        verify(mockContentRemoteSource, times(1)).get()
        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN no response from remote source WHEN content observed THEN loading is returned")
    @Test
    fun loadingIsShownBeforeTheRequestIsReturned() = runBlockingTest {
        val expected = Resource.Loading<List<Content>>()
        val suspendedRequest = CompletableDeferred<Unit>()
        whenever(mockContentRemoteSource.get()).doSuspendableAnswer {
            suspendedRequest.await()
            emptyList()
        }

        val actual = sut.contents.take(1).toList()

        Assertions.assertEquals(listOf(expected), actual)
        suspendedRequest.complete(Unit)
    }

    @DisplayName("GIVEN content response THEN error WHEN fetched THEN returned states are loading data loading error")
    @Test
    fun whenFetchingRequestIsCalledAgain() =
            runBlockingTest(testDispatcher) {
                val exception = RuntimeException()
                val expected = listOf(
                        Resource.Loading(),
                        Resource.Success(emptyList()),
                        Resource.Loading(),
                        Resource.Error<List<Content>>(UnexpectedException(exception))
                )
                var first = true
                whenever(mockContentRemoteSource.get()).doAnswer {
                    if (first) emptyList<Content>().also { first = false } else throw exception
                }

                val actual = async(testDispatcher) { sut.contents.take(4).toList() }
                testDispatcher.advanceUntilIdle()
                sut.fetch()
                testDispatcher.advanceUntilIdle()

                Assertions.assertEquals(expected, actual.await())
            }

    @DisplayName("GIVEN content response THEN error WHEN fetched THEN only 4 items are emitted")
    @Test
    fun noAdditionalItemsEmitted() {
        Assertions.assertThrows(IllegalStateException::class.java) {
            runBlockingTest(testDispatcher) {
                val exception = RuntimeException()
                val expected = listOf(
                        Resource.Loading(),
                        Resource.Success(emptyList()),
                        Resource.Loading(),
                        Resource.Error<List<Content>>(UnexpectedException(exception))
                )
                var first = true
                whenever(mockContentRemoteSource.get()).doAnswer {
                    if (first) emptyList<Content>().also { first = false } else throw exception
                }

                val actual = async(testDispatcher) { sut.contents.take(5).toList() }
                testDispatcher.advanceUntilIdle()
                sut.fetch()
                testDispatcher.advanceUntilIdle()

                Assertions.assertEquals(expected, actual.await())
            }
        }
    }
}
