package com.wafflehq.monitoring.ui.pagedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wafflehq.monitoring.R
import com.wafflehq.monitoring.data.db.CheckResultEntity
import com.wafflehq.monitoring.ui.components.AppCard
import com.wafflehq.monitoring.ui.components.AppIconButton
import com.wafflehq.monitoring.ui.components.AppStatusPill
import com.wafflehq.monitoring.ui.components.IconButtonVariant
import com.wafflehq.monitoring.ui.components.SettingsScaffold
import com.wafflehq.monitoring.ui.format.formatTimestamp
import com.wafflehq.monitoring.ui.theme.AppRole
import com.wafflehq.monitoring.ui.theme.AppSpacing
import com.wafflehq.monitoring.ui.theme.AppTheme

@Composable
fun PageDetailScreen(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: PageDetailViewModel = hiltViewModel(),
) {
    val page by viewModel.page.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()

    SettingsScaffold(
        title = page?.name ?: stringResource(R.string.page_detail_title),
        onBack = onBack,
        backDescription = stringResource(R.string.label_back),
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
                horizontalArrangement = Arrangement.End,
            ) {
                AppIconButton(
                    icon = Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.label_edit),
                    role = AppRole.Primary,
                    variant = IconButtonVariant.Tonal,
                    onClick = onEdit,
                )
            }
            Box(modifier = Modifier.fillMaxSize()) {
                if (results.isEmpty()) {
                    Text(
                        text = stringResource(R.string.page_detail_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center).padding(AppSpacing.xxl),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(AppSpacing.lg),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    ) {
                        items(results, key = { it.id }) { result ->
                            ResultCard(result = result, onAcknowledge = viewModel::acknowledgeAll)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultCard(result: CheckResultEntity, onAcknowledge: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(AppSpacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatTimestamp(result.triggeredAt),
                    style = MaterialTheme.typography.titleSmall,
                    color = AppTheme.colors.onSurface,
                )
                if (result.acknowledged) {
                    AppStatusPill(text = stringResource(R.string.page_detail_acknowledged), role = AppRole.Neutral)
                } else {
                    AppStatusPill(text = stringResource(R.string.page_detail_open_badge), role = AppRole.Warning)
                }
            }
            Text(
                text = stringResource(R.string.page_detail_matched_count, result.matchCount),
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(top = AppSpacing.xs),
            )
            Text(
                text = result.matchedSummary,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(top = AppSpacing.xs),
            )
            if (!result.acknowledged) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.sm), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onAcknowledge) {
                        Text(stringResource(R.string.page_detail_acknowledge))
                    }
                }
            }
        }
    }
}
