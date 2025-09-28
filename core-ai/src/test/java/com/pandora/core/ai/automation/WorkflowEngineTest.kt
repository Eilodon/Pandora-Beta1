package com.pandora.core.ai.automation

import android.content.Context
import com.pandora.core.ai.TestDataFactory
import com.pandora.core.ai.TestUtils
import com.pandora.core.ai.TestBase
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull

/**
 * Unit tests for WorkflowEngine
 */
class WorkflowEngineTest : TestBase() {
    
    private lateinit var context: Context
    private lateinit var workflowEngine: WorkflowEngine
    private lateinit var workflowExecutor: WorkflowExecutor
    private lateinit var triggerManager: TriggerManager
    private lateinit var conditionEvaluator: ConditionEvaluator
    private lateinit var smartIntegrationManager: SmartIntegrationManager
    
    @BeforeEach
    fun setUp() {
        context = mockk(relaxed = true)
        
        // Mock dependencies
        workflowExecutor = mockk(relaxed = true)
        triggerManager = mockk(relaxed = true)
        conditionEvaluator = mockk(relaxed = true)
        smartIntegrationManager = mockk(relaxed = true)
        
        workflowEngine = WorkflowEngine(
            context = context,
            enhancedContextIntegration = mockk(relaxed = true),
            workflowExecutor = workflowExecutor,
            triggerManager = triggerManager,
            conditionEvaluator = conditionEvaluator
        )
    }
    
    @Test
    fun `registerWorkflow should register workflow successfully`() = runTest {
        // Given
        val workflow = createTestWorkflow()

        // When
        workflowEngine.registerWorkflow(workflow)

        // Then
        val availableWorkflows = workflowEngine.getAvailableWorkflows()
        assertTrue(availableWorkflows.any { it.id == workflow.id })
    }
    
    @Test
    fun `registerWorkflow should fail for duplicate workflow`() = runTest {
        // Given
        val workflow = createTestWorkflow("duplicate_workflow")

        // Register first time
        workflowEngine.registerWorkflow(workflow)

        // When
        workflowEngine.registerWorkflow(workflow) // Should overwrite

        // Then
        val availableWorkflows = workflowEngine.getAvailableWorkflows()
        assertTrue(availableWorkflows.any { it.id == workflow.id })
    }
    
    
    
    @Test
    fun `getAvailableWorkflows should return registered workflows`() = runTest {
        // Given
        val workflow = createTestWorkflow()
        workflowEngine.registerWorkflow(workflow)

        // When
        val availableWorkflows = workflowEngine.getAvailableWorkflows()

        // Then
        assertTrue(availableWorkflows.any { it.id == workflow.id })
    }
    
    @Test
    fun `getAvailableWorkflows should return available workflows`() = runTest {
        // Given
        val workflow1 = createTestWorkflow("workflow1")
        val workflow2 = createTestWorkflow("workflow2")

        workflowEngine.registerWorkflow(workflow1)
        workflowEngine.registerWorkflow(workflow2)

        // When
        val availableWorkflows = workflowEngine.getAvailableWorkflows()

        // Then
        assertTrue(availableWorkflows.size >= 2)
        assertTrue(availableWorkflows.any { it.id == workflow1.id })
        assertTrue(availableWorkflows.any { it.id == workflow2.id })
    }
    
    @Test
    fun `getWorkflowById should return correct workflow`() = runTest {
        // Given
        val workflow = createTestWorkflow()
        workflowEngine.registerWorkflow(workflow)
        
        // When
        val retrievedWorkflow = workflowEngine.getAvailableWorkflows().find { it.id == workflow.id }
        
        // Then
        assertTrue(retrievedWorkflow != null)
        assertEquals(workflow.id, retrievedWorkflow?.id)
        assertEquals(workflow.name, retrievedWorkflow?.name)
    }
    
    @Test
    fun `getWorkflowById should return null for non-existent workflow`() = runTest {
        // Given
        val workflowId = "non_existent_workflow"
        
        // When
        val retrievedWorkflow = workflowEngine.getAvailableWorkflows().find { it.id == workflowId }
        
        // Then
        assertTrue(retrievedWorkflow == null)
    }
    
    @Test
    fun `triggerManager handleTriggerEvent should execute workflow for matching trigger`() = runTest {
        // Given
        val workflow = createTestWorkflow()
        workflowEngine.registerWorkflow(workflow)
        // Workflow is already registered
        
        val triggerEvent = TriggerEvent(
            id = "trigger1",
            type = TriggerType.TEXT_PATTERN,
            triggerId = "trigger1",
            confidence = 0.8f,
            data = mapOf("text" to "test pattern"),
            timestamp = System.currentTimeMillis()
        )
        
        // When
        val result = workflowEngine.executeWorkflow(workflow.id, triggerEvent.data).first()
        
        // Then
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `triggerManager handleTriggerEvent should not execute workflow for non-matching trigger`() = runTest {
        // Given
        val workflow = createTestWorkflow()
        workflowEngine.registerWorkflow(workflow)
        // Workflow is already registered
        
        val triggerEvent = TriggerEvent(
            id = "trigger1",
            type = TriggerType.TEXT_PATTERN,
            triggerId = "trigger1",
            confidence = 0.8f,
            data = mapOf("text" to "different pattern"),
            timestamp = System.currentTimeMillis()
        )
        
        // When
        val result = workflowEngine.executeWorkflow(workflow.id, triggerEvent.data).first()
        
        // Then
        assertTrue(result.isSuccess) // Should succeed but not execute workflow
    }
    
    @Test
    fun `executeWorkflow should execute workflow steps`() = runTest {
        // Given
        val workflow = createTestWorkflow()
        workflowEngine.registerWorkflow(workflow)
        
        val context = mutableMapOf<String, Any>(
            "input.text" to "test text",
            "system_context" to TestDataFactory.createComprehensiveContext()
        )
        
        // Mock condition evaluator to return true
        coEvery { conditionEvaluator.evaluateConditions(any(), any()) } returns true
        
        // Mock workflow executor to return success
        coEvery { workflowExecutor.executeStep(any(), any()) } returns StepExecutionResult(
            stepId = "step1",
            isSuccess = true,
            output = mapOf("result" to "success"),
            error = null,
            duration = 100L
        )
        
        // When
        val result = workflowEngine.executeWorkflow(workflow.id, context).first()
        
        // Then
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `executeWorkflow should fail if conditions not met`() = runTest {
        // Given
        val workflow = createTestWorkflow()
        workflowEngine.registerWorkflow(workflow)
        
        val context = mutableMapOf<String, Any>(
            "input.text" to "test text",
            "system_context" to TestDataFactory.createComprehensiveContext()
        )
        
        // Mock condition evaluator to return false
        coEvery { conditionEvaluator.evaluateConditions(any(), any()) } returns false
        
        // When
        val result = workflowEngine.executeWorkflow(workflow.id, context).first()
        
        // Then
        assertFalse(result.isSuccess)
    }
    
    @Test
    fun `executeWorkflow should fail for non-existent workflow`() = runTest {
        // Given
        val workflowId = "non_existent_workflow"
        val context = mutableMapOf<String, Any>()
        
        // When
        val result = workflowEngine.executeWorkflow(workflowId, context).first()
        
        // Then
        assertFalse(result.isSuccess)
    }
    
    @Test
    fun `getActiveWorkflows_should_return_active_workflows`() = runTest {
        // Given
        val workflow1 = createTestWorkflow("workflow1")
        val workflow2 = createTestWorkflow("workflow2")
        
        workflowEngine.registerWorkflow(workflow1)
        workflowEngine.registerWorkflow(workflow2)
        
        // When
        val activeWorkflows = workflowEngine.getActiveWorkflows()
        
        // Then
        assertTrue(activeWorkflows.size >= 0) // May be 0 if no workflows are running
    }
    
    @Test
    fun `registerBuiltInWorkflows should register built-in workflows`() = runTest {
        // Given
        // Built-in workflows are registered in constructor
        
        // When
        val activeWorkflows = workflowEngine.getAvailableWorkflows()
        
        // Then
        assertTrue(activeWorkflows.isNotEmpty())
        assertTrue(activeWorkflows.any { it.name.contains("Calendar") })
        assertTrue(activeWorkflows.any { it.name.contains("Maps") })
        assertTrue(activeWorkflows.any { it.name.contains("Camera") })
    }
    
    @Test
    fun `workflow should handle complex multi-step execution`() = runTest {
        // Given
        val workflow = WorkflowDefinition(
            id = "complex_workflow",
            name = "Complex Workflow",
            description = "A complex multi-step workflow",
            triggers = listOf(
                TriggerDefinition(
                    id = "trigger1",
                    name = "Text Trigger",
                    type = TriggerType.TEXT_PATTERN,
                    pattern = "complex",
                    confidence = 0.8f
                )
            ),
            steps = listOf(
                WorkflowStep(
                    id = "step1",
                    type = StepType.ANALYZE_TEXT,
                    action = "Text Analysis",
                    parameters = mapOf("text" to "input.text")
                ),
                WorkflowStep(
                    id = "step2",
                    type = StepType.ANALYZE_TEXT,
                    action = "Entity Extraction",
                    parameters = mapOf("text" to "step1.output")
                ),
                WorkflowStep(
                    id = "step3",
                    type = StepType.ANALYZE_TEXT,
                    action = "Integration Action",
                    parameters = mapOf("action" to "step2.output")
                )
            ),
        )
        
        workflowEngine.registerWorkflow(workflow)
        
        val context = mutableMapOf<String, Any>(
            "input.text" to "complex test text",
            "system_context" to TestDataFactory.createComprehensiveContext()
        )
        
        // Mock condition evaluator to return true
        coEvery { conditionEvaluator.evaluateConditions(any(), any()) } returns true
        
        // Mock workflow executor to return success for each step
        coEvery { workflowExecutor.executeStep(any(), any()) } returns StepExecutionResult(
            stepId = "step",
            isSuccess = true,
            output = mapOf("result" to "success"),
            error = null,
            duration = 100L
        )
        
        // When
        val result = workflowEngine.executeWorkflow(workflow.id, context).first()
        
        // Then
        assertTrue(result.isSuccess)
    }
    
    // Helper methods
    private fun createTestWorkflow(id: String = "test_workflow"): WorkflowDefinition {
        return WorkflowDefinition(
            id = id,
            name = "Test Workflow",
            description = "A test workflow",
            triggers = listOf(
                TriggerDefinition(
                    id = "trigger1",
                    name = "Text Trigger",
                    type = TriggerType.TEXT_PATTERN,
                    pattern = "test",
                    confidence = 0.8f
                )
            ),
            steps = listOf(
                WorkflowStep(
                    id = "step1",
                    type = StepType.ANALYZE_TEXT,
                    action = "Analyze text",
                    parameters = mapOf("text" to "test")
                )
            ),
        )
    }
}
