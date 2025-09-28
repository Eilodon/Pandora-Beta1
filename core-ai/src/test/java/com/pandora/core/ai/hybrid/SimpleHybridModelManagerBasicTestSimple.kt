package com.pandora.core.ai.hybrid

import android.content.Context
import com.pandora.core.ai.storage.IModelStorageManager
import com.pandora.core.ai.storage.TestModelStorageManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class SimpleHybridModelManagerBasicTestSimple {

    @Mock
    private lateinit var mockContext: Context

    private lateinit var testStorageManager: IModelStorageManager
    private lateinit var hybridModelManager: SimpleHybridModelManager
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        testDispatcher = UnconfinedTestDispatcher()

        // Create test storage manager
        testStorageManager = TestModelStorageManager()

        // Create instance using reflection to bypass @Inject constructor
        hybridModelManager = SimpleHybridModelManager::class.java
            .getDeclaredConstructor(Context::class.java, IModelStorageManager::class.java)
            .newInstance(mockContext, testStorageManager)
    }

    @After
    fun teardown() {
        // Clean up
    }

    @Test
    fun testManagerStatus() = runTest(testDispatcher) {
        val status = hybridModelManager.managerStatus.value
        assertFalse(status.isInitialized)
        assertFalse(status.isLoading)
    }

    @Test
    fun testManagerUnloadModel() = runTest(testDispatcher) {
        val result = hybridModelManager.unloadModel("test-model")
        assertFalse(result) // Should return false for non-existent model
    }
}
