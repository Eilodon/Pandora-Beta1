package com.pandora.core.ai.di

import android.content.Context
import com.pandora.core.ai.flows.FlowScheduler
import com.pandora.core.ai.ml.ModelManager
import com.pandora.core.ai.ml.AdvancedModelManager
import com.pandora.core.ai.personalization.PersonalizationEngine
import com.pandora.core.ai.prediction.PredictiveAnalytics
import com.pandora.core.ai.context.ContextAwareness
import com.pandora.core.ai.context.TimeContextEnhancer
import com.pandora.core.ai.context.LocationAwareness
import com.pandora.core.ai.context.UserActivityAnalyzer
import com.pandora.core.ai.context.AppUsageIntelligence
import com.pandora.core.ai.context.EnhancedContextIntegration
import com.pandora.core.ai.EnhancedInferenceEngine
import com.pandora.core.ai.optimization.ModelOptimizer
import com.pandora.core.ai.performance.PerformanceMonitor
import com.pandora.core.ai.automation.WorkflowEngine
import com.pandora.core.ai.automation.WorkflowExecutor
import com.pandora.core.ai.automation.TriggerManager
import com.pandora.core.ai.automation.ConditionEvaluator
import com.pandora.core.ai.automation.SmartIntegrationManager
import com.pandora.core.ai.compression.CompressionCodec
import com.pandora.core.ai.storage.ModelStorageManager
import com.pandora.core.ai.storage.IModelStorageManager
import com.pandora.core.ai.hybrid.SimpleHybridModelManager
import com.pandora.core.ai.hybrid.ProductionHybridModelManager
import com.pandora.core.ai.hybrid.FullHybridModelManager
import com.pandora.core.ai.hybrid.HybridModelManagerConfig
import com.pandora.core.ai.network.NetworkHealthMonitor
import com.pandora.core.ai.delta.DeltaUpdateManager
import com.pandora.core.cac.db.CACDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module for AI dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AIModule {
    
    @Provides
    @Singleton
    fun provideModelManager(@ApplicationContext context: Context): ModelManager {
        return ModelManager(context)
    }

    @Provides
    @Singleton
    fun provideAdvancedModelManager(@ApplicationContext context: Context): AdvancedModelManager {
        return AdvancedModelManager(context)
    }

    @Provides
    @Singleton
    fun providePersonalizationEngine(
        @ApplicationContext context: Context,
        cacDao: CACDao
    ): PersonalizationEngine {
        return PersonalizationEngine(context, cacDao)
    }

    @Provides
    @Singleton
    fun providePredictiveAnalytics(
        @ApplicationContext context: Context,
        cacDao: CACDao
    ): PredictiveAnalytics {
        return PredictiveAnalytics(context, cacDao)
    }

    @Provides
    @Singleton
    fun provideContextAwareness(
        @ApplicationContext context: Context,
        cacDao: CACDao
    ): ContextAwareness {
        return ContextAwareness(context, cacDao)
    }

    @Provides
    @Singleton
    fun provideEnhancedInferenceEngine(
        @ApplicationContext context: Context,
        advancedModelManager: AdvancedModelManager,
        personalizationEngine: PersonalizationEngine,
        predictiveAnalytics: PredictiveAnalytics,
        contextAwareness: ContextAwareness,
        enhancedContextIntegration: EnhancedContextIntegration,
        cacDao: CACDao
    ): EnhancedInferenceEngine {
        return EnhancedInferenceEngine(
            context = context,
            advancedModelManager = advancedModelManager,
            personalizationEngine = personalizationEngine,
            predictiveAnalytics = predictiveAnalytics,
            contextAwareness = contextAwareness,
            enhancedContextIntegration = enhancedContextIntegration,
            cacDao = cacDao
        )
    }
    
    @Provides
    @Singleton
    fun provideFlowScheduler(@ApplicationContext context: Context): FlowScheduler {
        return FlowScheduler(context)
    }
    
    @Provides
    @Singleton
    fun providePerformanceMonitor(@ApplicationContext context: Context): PerformanceMonitor {
        return PerformanceMonitor(context)
    }
    
    @Provides
    @Singleton
    fun provideModelOptimizer(@ApplicationContext context: Context): ModelOptimizer {
        return ModelOptimizer(context)
    }
    
    @Provides
    @Singleton
    fun provideTimeContextEnhancer(@ApplicationContext context: Context): TimeContextEnhancer {
        return TimeContextEnhancer(context)
    }
    
    @Provides
    @Singleton
    fun provideLocationAwareness(@ApplicationContext context: Context): LocationAwareness {
        return LocationAwareness(context)
    }
    
    @Provides
    @Singleton
    fun provideUserActivityAnalyzer(@ApplicationContext context: Context): UserActivityAnalyzer {
        return UserActivityAnalyzer(context)
    }
    
    @Provides
    @Singleton
    fun provideAppUsageIntelligence(@ApplicationContext context: Context): AppUsageIntelligence {
        return AppUsageIntelligence(context)
    }
    
    @Provides
    @Singleton
    fun provideEnhancedContextIntegration(
        @ApplicationContext context: Context,
        timeContextEnhancer: TimeContextEnhancer,
        locationAwareness: LocationAwareness,
        userActivityAnalyzer: UserActivityAnalyzer,
        appUsageIntelligence: AppUsageIntelligence
    ): EnhancedContextIntegration {
        return EnhancedContextIntegration(
            context = context,
            timeContextEnhancer = timeContextEnhancer,
            locationAwareness = locationAwareness,
            userActivityAnalyzer = userActivityAnalyzer,
            appUsageIntelligence = appUsageIntelligence
        )
    }
    
    // Automation Dependencies
    @Provides
    @Singleton
    fun provideWorkflowExecutor(@ApplicationContext context: Context): WorkflowExecutor {
        return WorkflowExecutor(context)
    }
    
    @Provides
    @Singleton
    fun provideTriggerManager(
        @ApplicationContext context: Context,
        enhancedContextIntegration: EnhancedContextIntegration
    ): TriggerManager {
        return TriggerManager(context, enhancedContextIntegration)
    }
    
    @Provides
    @Singleton
    fun provideConditionEvaluator(
        @ApplicationContext context: Context,
        enhancedContextIntegration: EnhancedContextIntegration
    ): ConditionEvaluator {
        return ConditionEvaluator(context, enhancedContextIntegration)
    }
    
    @Provides
    @Singleton
    fun provideSmartIntegrationManager(@ApplicationContext context: Context): SmartIntegrationManager {
        return SmartIntegrationManager(context)
    }
    
    @Provides
    @Singleton
    fun provideWorkflowEngine(
        @ApplicationContext context: Context,
        enhancedContextIntegration: EnhancedContextIntegration,
        workflowExecutor: WorkflowExecutor,
        triggerManager: TriggerManager,
        conditionEvaluator: ConditionEvaluator
    ): WorkflowEngine {
        return WorkflowEngine(
            context = context,
            enhancedContextIntegration = enhancedContextIntegration,
            workflowExecutor = workflowExecutor,
            triggerManager = triggerManager,
            conditionEvaluator = conditionEvaluator
        )
    }
    
    // Hybrid Model Manager Dependencies - Temporarily commented out for Phase 2
    @Provides
    @Singleton
    fun provideModelStorageManager(@ApplicationContext context: Context): IModelStorageManager {
        return ModelStorageManager(context)
    }

    @Provides
    @Singleton
    fun provideNetworkHealthMonitor(@ApplicationContext context: Context): NetworkHealthMonitor {
        return NetworkHealthMonitor()
    }

    @Provides
    @Singleton
    fun provideDeltaUpdateManager(@ApplicationContext context: Context): DeltaUpdateManager {
        return DeltaUpdateManager()
    }

    @Provides
    @Singleton
    fun provideSimpleHybridModelManager(
        @ApplicationContext context: Context,
        storageManager: IModelStorageManager
    ): SimpleHybridModelManager {
        return SimpleHybridModelManager(
            context = context,
            storageManager = storageManager
        )
    }

    @Provides
    @Singleton
    fun provideHybridModelManagerConfig(): HybridModelManagerConfig {
        return HybridModelManagerConfig.PRODUCTION
    }

    @Provides
    @Singleton
    fun provideFullHybridModelManager(
        @ApplicationContext context: Context,
        storageManager: ModelStorageManager,
        networkMonitor: NetworkHealthMonitor,
        deltaUpdateManager: DeltaUpdateManager,
        config: HybridModelManagerConfig
    ): FullHybridModelManager {
        return FullHybridModelManager(
            context = context,
            storageManager = storageManager,
            networkMonitor = networkMonitor,
            deltaUpdateManager = deltaUpdateManager,
            config = config
        )
    }
}
