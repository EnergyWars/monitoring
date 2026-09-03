package com.wafflehq.monitoring.ui.navigation

import android.net.Uri
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wafflehq.monitoring.ui.components.AppDrawer
import com.wafflehq.monitoring.ui.features.FeatureFileDetailScreen
import com.wafflehq.monitoring.ui.features.FeatureFilesListScreen
import com.wafflehq.monitoring.ui.home.HomeScreen
import com.wafflehq.monitoring.ui.pagedetail.PageDetailScreen
import com.wafflehq.monitoring.ui.pageform.PageFormScreen
import com.wafflehq.monitoring.ui.settings.DisplaySettingsScreen
import com.wafflehq.monitoring.ui.settings.ReliabilitySettingsScreen
import com.wafflehq.monitoring.ui.settings.SettingsScreen
import kotlinx.coroutines.launch

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val SETTINGS_DISPLAY = "settings_display"
    const val SETTINGS_RELIABILITY = "settings_reliability"
    const val PAGE_FORM = "page_form/{pageId}"
    const val PAGE_DETAIL = "page_detail/{pageId}"
    const val FEATURE_FILES = "feature_files"
    const val FEATURE_FILE_DETAIL = "feature_file_detail/{fileName}"

    const val NEW_PAGE_ID = -1L

    fun featureFileDetail(fileName: String): String =
        "feature_file_detail/${Uri.encode(fileName)}"

    fun pageForm(pageId: Long = NEW_PAGE_ID): String = "page_form/$pageId"

    fun pageDetail(pageId: Long): String = "page_detail/$pageId"
}

private fun NavController.switchTo(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(Routes.HOME) { saveState = true }
    }
}

@Composable
fun AppNavHost(openPageId: Long? = null) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val openMenu: () -> Unit = { scope.launch { drawerState.open() } }
    val navigateHome: () -> Unit = { navController.switchTo(Routes.HOME) }
    val openSettings: () -> Unit = {
        navController.navigate(Routes.SETTINGS) { launchSingleTop = true }
    }

    LaunchedEffect(openPageId) {
        if (openPageId != null) {
            navController.navigate(Routes.pageDetail(openPageId)) { launchSingleTop = true }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = currentRoute,
                onSelect = { route ->
                    scope.launch { drawerState.close() }
                    if (route == Routes.SETTINGS) {
                        navController.navigate(route) { launchSingleTop = true }
                    } else {
                        navController.switchTo(route)
                    }
                },
            )
        },
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenMenu = openMenu,
                    onNavigateHome = navigateHome,
                    onOpenSettings = openSettings,
                    onAddPage = { navController.navigate(Routes.pageForm()) },
                    onOpenPage = { pageId -> navController.navigate(Routes.pageDetail(pageId)) },
                    onOpenReliability = { navController.navigate(Routes.SETTINGS_RELIABILITY) },
                )
            }
            composable(
                route = Routes.PAGE_FORM,
                arguments = listOf(navArgument("pageId") { type = NavType.LongType }),
            ) {
                PageFormScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Routes.PAGE_DETAIL,
                arguments = listOf(navArgument("pageId") { type = NavType.LongType }),
            ) { entry ->
                val pageId = entry.arguments?.getLong("pageId") ?: 0L
                PageDetailScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.pageForm(pageId)) },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenDisplay = { navController.navigate(Routes.SETTINGS_DISPLAY) },
                    onOpenFeatureFiles = { navController.navigate(Routes.FEATURE_FILES) },
                    onOpenReliability = { navController.navigate(Routes.SETTINGS_RELIABILITY) },
                )
            }
            composable(Routes.SETTINGS_DISPLAY) {
                DisplaySettingsScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.SETTINGS_RELIABILITY) {
                ReliabilitySettingsScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.FEATURE_FILES) {
                FeatureFilesListScreen(
                    onBack = { navController.popBackStack() },
                    onOpenFile = { fileName ->
                        navController.navigate(Routes.featureFileDetail(fileName))
                    },
                )
            }
            composable(
                route = Routes.FEATURE_FILE_DETAIL,
                arguments = listOf(navArgument("fileName") { type = NavType.StringType }),
            ) {
                FeatureFileDetailScreen(
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
