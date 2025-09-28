package com.pandora.core.ui.designkit.responsive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext

/**
 * Responsive Design Tokens
 * Breakpoints and responsive values for different screen sizes
 */
@Immutable
object ResponsiveTokens {
    
    /**
     * Breakpoints for different screen sizes
     */
    object Breakpoints {
        val mobile = 0.dp
        val mobileLarge = 480.dp
        val tablet = 768.dp
        val tabletLarge = 1024.dp
        val desktop = 1200.dp
        val desktopLarge = 1440.dp
    }
    
    /**
     * Screen size categories
     */
    enum class ScreenSize {
        MOBILE,
        MOBILE_LARGE,
        TABLET,
        TABLET_LARGE,
        DESKTOP,
        DESKTOP_LARGE
    }
    
    /**
     * Responsive spacing values
     */
    object Spacing {
        val xs = ResponsiveValue(4.dp, 6.dp, 8.dp, 10.dp, 12.dp, 14.dp)
        val sm = ResponsiveValue(8.dp, 12.dp, 16.dp, 20.dp, 24.dp, 28.dp)
        val md = ResponsiveValue(12.dp, 16.dp, 20.dp, 24.dp, 28.dp, 32.dp)
        val lg = ResponsiveValue(16.dp, 20.dp, 24.dp, 28.dp, 32.dp, 36.dp)
        val xl = ResponsiveValue(20.dp, 24.dp, 28.dp, 32.dp, 36.dp, 40.dp)
        val xxl = ResponsiveValue(24.dp, 28.dp, 32.dp, 36.dp, 40.dp, 44.dp)
    }
    
    /**
     * Responsive corner radius values
     */
    object CornerRadius {
        val small = ResponsiveValue(4.dp, 6.dp, 8.dp, 10.dp, 12.dp, 14.dp)
        val medium = ResponsiveValue(8.dp, 10.dp, 12.dp, 14.dp, 16.dp, 18.dp)
        val large = ResponsiveValue(12.dp, 14.dp, 16.dp, 18.dp, 20.dp, 22.dp)
        val xlarge = ResponsiveValue(16.dp, 18.dp, 20.dp, 22.dp, 24.dp, 26.dp)
    }
    
    /**
     * Responsive elevation values
     */
    object Elevation {
        val none = ResponsiveValue(0.dp, 0.dp, 0.dp, 0.dp, 0.dp, 0.dp)
        val small = ResponsiveValue(2.dp, 3.dp, 4.dp, 5.dp, 6.dp, 7.dp)
        val medium = ResponsiveValue(4.dp, 5.dp, 6.dp, 7.dp, 8.dp, 9.dp)
        val large = ResponsiveValue(6.dp, 7.dp, 8.dp, 9.dp, 10.dp, 11.dp)
        val xlarge = ResponsiveValue(8.dp, 9.dp, 10.dp, 11.dp, 12.dp, 13.dp)
    }
    
    /**
     * Responsive typography sizes
     */
    object Typography {
        val caption = ResponsiveValue(10.dp, 11.dp, 12.dp, 13.dp, 14.dp, 15.dp)
        val body = ResponsiveValue(12.dp, 13.dp, 14.dp, 15.dp, 16.dp, 17.dp)
        val title = ResponsiveValue(16.dp, 18.dp, 20.dp, 22.dp, 24.dp, 26.dp)
        val headline = ResponsiveValue(20.dp, 22.dp, 24.dp, 26.dp, 28.dp, 30.dp)
        val display = ResponsiveValue(24.dp, 26.dp, 28.dp, 30.dp, 32.dp, 34.dp)
    }
    
    /**
     * Responsive icon sizes
     */
    object IconSize {
        val small = ResponsiveValue(16.dp, 18.dp, 20.dp, 22.dp, 24.dp, 26.dp)
        val medium = ResponsiveValue(20.dp, 22.dp, 24.dp, 26.dp, 28.dp, 30.dp)
        val large = ResponsiveValue(24.dp, 26.dp, 28.dp, 30.dp, 32.dp, 34.dp)
        val xlarge = ResponsiveValue(28.dp, 30.dp, 32.dp, 34.dp, 36.dp, 38.dp)
    }
    
    /**
     * Responsive grid columns
     */
    object Grid {
        val mobile = 1
        val mobileLarge = 2
        val tablet = 3
        val tabletLarge = 4
        val desktop = 5
        val desktopLarge = 6
    }
    
    /**
     * Responsive padding values
     */
    object Padding {
        val screen = ResponsiveValue(16.dp, 20.dp, 24.dp, 28.dp, 32.dp, 36.dp)
        val card = ResponsiveValue(12.dp, 16.dp, 20.dp, 24.dp, 28.dp, 32.dp)
        val button = ResponsiveValue(8.dp, 12.dp, 16.dp, 20.dp, 24.dp, 28.dp)
    }
}

/**
 * Responsive value that changes based on screen size
 */
@Immutable
data class ResponsiveValue(
    val mobile: Dp,
    val mobileLarge: Dp,
    val tablet: Dp,
    val tabletLarge: Dp,
    val desktop: Dp,
    val desktopLarge: Dp
) {
    @Composable
    fun getValue(): Dp {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ResponsiveTokens.ScreenSize.MOBILE -> mobile
            ResponsiveTokens.ScreenSize.MOBILE_LARGE -> mobileLarge
            ResponsiveTokens.ScreenSize.TABLET -> tablet
            ResponsiveTokens.ScreenSize.TABLET_LARGE -> tabletLarge
            ResponsiveTokens.ScreenSize.DESKTOP -> desktop
            ResponsiveTokens.ScreenSize.DESKTOP_LARGE -> desktopLarge
        }
    }
}

/**
 * Get current screen size based on configuration
 */
@Composable
fun getScreenSize(): ResponsiveTokens.ScreenSize {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    return when {
        screenWidth < ResponsiveTokens.Breakpoints.mobileLarge -> ResponsiveTokens.ScreenSize.MOBILE
        screenWidth < ResponsiveTokens.Breakpoints.tablet -> ResponsiveTokens.ScreenSize.MOBILE_LARGE
        screenWidth < ResponsiveTokens.Breakpoints.tabletLarge -> ResponsiveTokens.ScreenSize.TABLET
        screenWidth < ResponsiveTokens.Breakpoints.desktop -> ResponsiveTokens.ScreenSize.TABLET_LARGE
        screenWidth < ResponsiveTokens.Breakpoints.desktopLarge -> ResponsiveTokens.ScreenSize.DESKTOP
        else -> ResponsiveTokens.ScreenSize.DESKTOP_LARGE
    }
}

/**
 * Get responsive grid columns
 */
@Composable
fun getGridColumns(): Int {
    val screenSize = getScreenSize()
    return when (screenSize) {
        ResponsiveTokens.ScreenSize.MOBILE -> ResponsiveTokens.Grid.mobile
        ResponsiveTokens.ScreenSize.MOBILE_LARGE -> ResponsiveTokens.Grid.mobileLarge
        ResponsiveTokens.ScreenSize.TABLET -> ResponsiveTokens.Grid.tablet
        ResponsiveTokens.ScreenSize.TABLET_LARGE -> ResponsiveTokens.Grid.tabletLarge
        ResponsiveTokens.ScreenSize.DESKTOP -> ResponsiveTokens.Grid.desktop
        ResponsiveTokens.ScreenSize.DESKTOP_LARGE -> ResponsiveTokens.Grid.desktopLarge
    }
}

/**
 * Check if current screen is mobile
 */
@Composable
fun isMobile(): Boolean {
    val screenSize = getScreenSize()
    return screenSize == ResponsiveTokens.ScreenSize.MOBILE || 
           screenSize == ResponsiveTokens.ScreenSize.MOBILE_LARGE
}

/**
 * Check if current screen is tablet
 */
@Composable
fun isTablet(): Boolean {
    val screenSize = getScreenSize()
    return screenSize == ResponsiveTokens.ScreenSize.TABLET || 
           screenSize == ResponsiveTokens.ScreenSize.TABLET_LARGE
}

/**
 * Check if current screen is desktop
 */
@Composable
fun isDesktop(): Boolean {
    val screenSize = getScreenSize()
    return screenSize == ResponsiveTokens.ScreenSize.DESKTOP || 
           screenSize == ResponsiveTokens.ScreenSize.DESKTOP_LARGE
}
