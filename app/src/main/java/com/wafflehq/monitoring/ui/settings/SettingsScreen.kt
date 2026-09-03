package com.wafflehq.monitoring.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wafflehq.monitoring.R
import com.wafflehq.monitoring.ui.components.SettingsListContent
import com.wafflehq.monitoring.ui.components.SettingsListRow
import com.wafflehq.monitoring.ui.components.SettingsScaffold
import com.wafflehq.monitoring.ui.theme.AppTheme

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenDisplay: () -> Unit,
    onOpenFeatureFiles: () -> Unit,
    onOpenReliability: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val featureFilesCount by viewModel.featureFilesCount.collectAsStateWithLifecycle()

    SettingsScaffold(
        title = stringResource(R.string.settings_title),
        onBack = onBack,
        backDescription = stringResource(R.string.label_back),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsListContent(
                showFeatures = featureFilesCount > 0,
                featuresLabel = stringResource(R.string.settings_row_features),
                displayLabel = stringResource(R.string.settings_display_title),
                displaySubtitle = stringResource(R.string.settings_display_sub),
                onOpenFeatures = onOpenFeatureFiles,
                onOpenDisplay = onOpenDisplay,
            )
            Column(modifier = Modifier.fillMaxWidth().background(AppTheme.colors.surface)) {
                SettingsListRow(
                    title = stringResource(R.string.settings_row_reliability),
                    subtitle = stringResource(R.string.settings_row_reliability_sub),
                    onClick = onOpenReliability,
                )
                Box(Modifier.fillMaxWidth().height(1.dp).background(AppTheme.colors.outline))
            }
        }
    }
}
