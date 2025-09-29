package com.pandora.feature.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Contains all tutorial content and step definitions for onboarding
 */
object OnboardingTutorials {
    
    /**
     * Get the main onboarding flow
     */
    fun getMainOnboardingFlow(): OnboardingConfig {
        return OnboardingConfig(
            flowId = "main_onboarding",
            version = "1.0.0",
            steps = getMainOnboardingSteps(),
            allowSkip = true,
            showProgress = true,
            enableAnalytics = true,
            autoAdvance = false
        )
    }
    
    /**
     * Get quick start flow for returning users
     */
    fun getQuickStartFlow(): OnboardingConfig {
        return OnboardingConfig(
            flowId = "quick_start",
            version = "1.0.0",
            steps = getQuickStartSteps(),
            allowSkip = true,
            showProgress = true,
            enableAnalytics = true,
            autoAdvance = false
        )
    }
    
    /**
     * Get advanced features flow
     */
    fun getAdvancedFeaturesFlow(): OnboardingConfig {
        return OnboardingConfig(
            flowId = "advanced_features",
            version = "1.0.0",
            steps = getAdvancedFeaturesSteps(),
            allowSkip = true,
            showProgress = true,
            enableAnalytics = true,
            autoAdvance = false
        )
    }
    
    /**
     * Main onboarding steps
     */
    private fun getMainOnboardingSteps(): List<OnboardingStep> {
        return listOf(
            // Phase 1: Welcome & Setup
            OnboardingStep(
                id = "welcome",
                title = "Chào mừng đến với PandoraOS",
                description = "Khám phá bàn phím thông minh với AI mạnh mẽ",
                type = StepType.WELCOME,
                content = StepContent.TextContent(
                    text = "PandoraOS là bàn phím thông minh với khả năng AI tiên tiến, giúp bạn làm việc hiệu quả hơn.",
                    highlightText = "AI tiên tiến"
                ),
                isRequired = true,
                estimatedTimeSeconds = 30,
                order = 1
            ),
            
            OnboardingStep(
                id = "permissions",
                title = "Cấp quyền cần thiết",
                description = "Để PandoraOS hoạt động tốt nhất, chúng tôi cần một số quyền",
                type = StepType.PERMISSION,
                content = StepContent.InteractiveContent(
                    instructions = "Cấp quyền System Alert Window để hiển thị overlay",
                    actionType = ActionType.GRANT_PERMISSION,
                    successMessage = "Quyền đã được cấp thành công!"
                ),
                validation = StepValidation(
                    validationType = ValidationType.PERMISSION_GRANTED,
                    condition = "SYSTEM_ALERT_WINDOW",
                    errorMessage = "Vui lòng cấp quyền để tiếp tục"
                ),
                isRequired = true,
                estimatedTimeSeconds = 45,
                order = 2
            ),
            
            OnboardingStep(
                id = "keyboard_activation",
                title = "Kích hoạt bàn phím",
                description = "Kích hoạt PandoraOS Keyboard trong cài đặt hệ thống",
                type = StepType.INTERACTIVE,
                content = StepContent.InteractiveContent(
                    instructions = "Mở Settings → System → Languages & input → Virtual keyboard → Manage keyboards → Bật PandoraOS Keyboard",
                    actionType = ActionType.NAVIGATE,
                    successMessage = "Bàn phím đã được kích hoạt!"
                ),
                validation = StepValidation(
                    validationType = ValidationType.SETTING_ENABLED,
                    condition = "keyboard_enabled",
                    errorMessage = "Vui lòng kích hoạt bàn phím trong cài đặt"
                ),
                isRequired = true,
                estimatedTimeSeconds = 60,
                order = 3
            ),
            
            // Phase 2: Core Features
            OnboardingStep(
                id = "smart_context_bar",
                title = "Smart Context Bar",
                description = "Thanh ngữ cảnh thông minh hiển thị gợi ý dựa trên nội dung",
                type = StepType.DEMO,
                content = StepContent.DemoContent(
                    featureName = "Smart Context Bar",
                    demoSteps = listOf(
                        "Gõ văn bản bình thường",
                        "Xem gợi ý xuất hiện trên thanh ngữ cảnh",
                        "Nhấn vào gợi ý để thực hiện hành động"
                    ),
                    expectedOutcome = "Gợi ý thông minh xuất hiện dựa trên nội dung bạn gõ"
                ),
                isRequired = true,
                estimatedTimeSeconds = 45,
                order = 4
            ),
            
            OnboardingStep(
                id = "quick_actions",
                title = "Quick Actions",
                description = "10 hành động nhanh được tích hợp sẵn",
                type = StepType.DEMO,
                content = StepContent.DemoContent(
                    featureName = "Quick Actions",
                    demoSteps = listOf(
                        "Gõ 'họp lúc 3pm' → Thêm vào lịch",
                        "Gõ 'gửi tin nhắn cho Nam' → Mở ứng dụng tin nhắn",
                        "Gõ 'tính 15% của 200' → Tính toán nhanh"
                    ),
                    expectedOutcome = "Hành động được thực hiện tự động dựa trên văn bản"
                ),
                isRequired = true,
                estimatedTimeSeconds = 60,
                order = 5
            ),
            
            OnboardingStep(
                id = "mini_flows",
                title = "Mini-Flows",
                description = "Tự động hóa thông minh dựa trên ngữ cảnh",
                type = StepType.DEMO,
                content = StepContent.DemoContent(
                    featureName = "Mini-Flows",
                    demoSteps = listOf(
                        "Kết nối tai nghe Bluetooth → Tự động mở Spotify",
                        "Gõ 'đi siêu thị' → Tự động mở Maps",
                        "Gõ 'chụp ảnh' → Tự động mở Camera"
                    ),
                    expectedOutcome = "Hành động được kích hoạt tự động dựa trên ngữ cảnh"
                ),
                isRequired = true,
                estimatedTimeSeconds = 45,
                order = 6
            ),
            
            OnboardingStep(
                id = "ai_learning",
                title = "AI Learning",
                description = "Hệ thống AI học hỏi từ cách sử dụng của bạn",
                type = StepType.INFO,
                content = StepContent.TextContent(
                    text = "PandoraOS sử dụng AI để học hỏi từ cách bạn sử dụng, giúp đưa ra gợi ý ngày càng chính xác hơn.",
                    highlightText = "học hỏi từ cách bạn sử dụng"
                ),
                isRequired = true,
                estimatedTimeSeconds = 30,
                order = 7
            ),
            
            OnboardingStep(
                id = "settings",
                title = "Cài đặt cơ bản",
                description = "Tùy chỉnh PandoraOS theo sở thích của bạn",
                type = StepType.SETUP,
                content = StepContent.InteractiveContent(
                    instructions = "Mở cài đặt PandoraOS để tùy chỉnh giao diện và tính năng",
                    actionType = ActionType.NAVIGATE,
                    successMessage = "Cài đặt đã được cấu hình!"
                ),
                isRequired = false,
                estimatedTimeSeconds = 60,
                order = 8
            ),
            
            // Phase 3: Advanced Features
            OnboardingStep(
                id = "floating_assistant",
                title = "Floating Assistant",
                description = "Trợ lý nổi giúp bạn mọi lúc mọi nơi",
                type = StepType.DEMO,
                content = StepContent.DemoContent(
                    featureName = "Floating Assistant",
                    demoSteps = listOf(
                        "Nhấn vào Floating Orb",
                        "Sử dụng các tính năng trợ lý",
                        "Di chuyển và ẩn/hiện orb"
                    ),
                    expectedOutcome = "Trợ lý nổi luôn sẵn sàng hỗ trợ bạn"
                ),
                isRequired = false,
                estimatedTimeSeconds = 45,
                order = 9
            ),
            
            OnboardingStep(
                id = "customization",
                title = "Tùy chỉnh giao diện",
                description = "Làm cho PandoraOS trở thành của riêng bạn",
                type = StepType.INFO,
                content = StepContent.TextContent(
                    text = "Bạn có thể tùy chỉnh giao diện, màu sắc, kích thước và nhiều thứ khác để phù hợp với sở thích.",
                    highlightText = "tùy chỉnh giao diện"
                ),
                isRequired = false,
                estimatedTimeSeconds = 30,
                order = 10
            ),
            
            OnboardingStep(
                id = "performance",
                title = "Monitoring hiệu suất",
                description = "Theo dõi và tối ưu hiệu suất",
                type = StepType.INFO,
                content = StepContent.TextContent(
                    text = "PandoraOS tự động theo dõi và tối ưu hiệu suất để đảm bảo trải nghiệm mượt mà.",
                    highlightText = "tối ưu hiệu suất"
                ),
                isRequired = false,
                estimatedTimeSeconds = 30,
                order = 11
            ),
            
            OnboardingStep(
                id = "completion",
                title = "Hoàn thành!",
                description = "Bạn đã sẵn sàng sử dụng PandoraOS",
                type = StepType.COMPLETION,
                content = StepContent.TextContent(
                    text = "Chúc mừng! Bạn đã hoàn thành hướng dẫn sử dụng PandoraOS. Hãy bắt đầu khám phá các tính năng mạnh mẽ!",
                    highlightText = "sẵn sàng sử dụng PandoraOS"
                ),
                isRequired = true,
                estimatedTimeSeconds = 30,
                order = 12
            )
        )
    }
    
    /**
     * Quick start steps for returning users
     */
    private fun getQuickStartSteps(): List<OnboardingStep> {
        return listOf(
            OnboardingStep(
                id = "quick_welcome",
                title = "Chào mừng trở lại!",
                description = "Cập nhật nhanh về các tính năng mới",
                type = StepType.WELCOME,
                content = StepContent.TextContent(
                    text = "Chào mừng bạn trở lại với PandoraOS! Có một số tính năng mới mà bạn có thể muốn khám phá.",
                    highlightText = "tính năng mới"
                ),
                isRequired = true,
                estimatedTimeSeconds = 20,
                order = 1
            ),
            
            OnboardingStep(
                id = "new_features",
                title = "Tính năng mới",
                description = "Khám phá các tính năng mới được thêm vào",
                type = StepType.INFO,
                content = StepContent.TextContent(
                    text = "• Quick Actions: 10 hành động nhanh mới\n• Mini-Flows: Tự động hóa thông minh\n• Floating Assistant: Trợ lý nổi",
                    highlightText = "Quick Actions, Mini-Flows, Floating Assistant"
                ),
                isRequired = true,
                estimatedTimeSeconds = 30,
                order = 2
            )
        )
    }
    
    /**
     * Advanced features steps
     */
    private fun getAdvancedFeaturesSteps(): List<OnboardingStep> {
        return listOf(
            OnboardingStep(
                id = "advanced_welcome",
                title = "Tính năng nâng cao",
                description = "Khám phá các tính năng nâng cao của PandoraOS",
                type = StepType.WELCOME,
                content = StepContent.TextContent(
                    text = "Khám phá các tính năng nâng cao để tận dụng tối đa sức mạnh của PandoraOS.",
                    highlightText = "tính năng nâng cao"
                ),
                isRequired = true,
                estimatedTimeSeconds = 20,
                order = 1
            ),
            
            OnboardingStep(
                id = "ai_personalization",
                title = "AI Personalization",
                description = "Tùy chỉnh AI theo sở thích cá nhân",
                type = StepType.DEMO,
                content = StepContent.DemoContent(
                    featureName = "AI Personalization",
                    demoSteps = listOf(
                        "Mở AI Settings",
                        "Cấu hình learning preferences",
                        "Xem personalized suggestions"
                    ),
                    expectedOutcome = "AI sẽ học hỏi và thích ứng với cách sử dụng của bạn"
                ),
                isRequired = false,
                estimatedTimeSeconds = 60,
                order = 2
            ),
            
            OnboardingStep(
                id = "performance_optimization",
                title = "Performance Optimization",
                description = "Tối ưu hiệu suất cho thiết bị của bạn",
                type = StepType.DEMO,
                content = StepContent.DemoContent(
                    featureName = "Performance Optimization",
                    demoSteps = listOf(
                        "Mở Performance Dashboard",
                        "Xem memory và CPU usage",
                        "Áp dụng optimization recommendations"
                    ),
                    expectedOutcome = "Hiệu suất được tối ưu cho thiết bị của bạn"
                ),
                isRequired = false,
                estimatedTimeSeconds = 45,
                order = 3
            )
        )
    }
    
    /**
     * Get step by ID
     */
    fun getStepById(stepId: String): OnboardingStep? {
        return getAllSteps().find { it.id == stepId }
    }
    
    /**
     * Get all available steps
     */
    fun getAllSteps(): List<OnboardingStep> {
        return getMainOnboardingSteps() + getQuickStartSteps() + getAdvancedFeaturesSteps()
    }
    
    /**
     * Get steps by type
     */
    fun getStepsByType(stepType: StepType): List<OnboardingStep> {
        return getAllSteps().filter { it.type == stepType }
    }
    
    /**
     * Get required steps only
     */
    fun getRequiredSteps(): List<OnboardingStep> {
        return getAllSteps().filter { it.isRequired }
    }
    
    /**
     * Get optional steps only
     */
    fun getOptionalSteps(): List<OnboardingStep> {
        return getAllSteps().filter { !it.isRequired }
    }
}
