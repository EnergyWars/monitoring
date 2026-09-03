package com.wafflehq.monitoring.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wafflehq.monitoring.R
import com.wafflehq.monitoring.data.db.MonitoredPageEntity
import com.wafflehq.monitoring.data.monitoring.CheckStatus
import com.wafflehq.monitoring.ui.components.AppBanner
import com.wafflehq.monitoring.ui.components.AppCard
import com.wafflehq.monitoring.ui.components.AppScaffold
import com.wafflehq.monitoring.ui.components.AppStatusPill
import com.wafflehq.monitoring.ui.components.HeaderItem
import com.wafflehq.monitoring.ui.theme.AppRole
import com.wafflehq.monitoring.ui.theme.AppSpacing
import com.wafflehq.monitoring.ui.format.formatTimestamp
import com.wafflehq.monitoring.ui.theme.AppTheme
import java.net.URI

@Composable
fun HomeScreen(
    onOpenMenu: () -> Unit,
    onNavigateHome: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddPage: () -> Unit,
    onOpenPage: (Long) -> Unit,
    onOpenReliability: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val pages by viewModel.pages.collectAsStateWithLifecycle()
    val reliabilityConfigured by viewModel.reliabilityConfigured.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshReliabilityStatus()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AppScaffold(
        activeItem = HeaderItem.Home,
        onOpenMenu = onOpenMenu,
        onNavigateHome = onNavigateHome,
        onOpenSettings = onOpenSettings,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
                .padding(padding),
        ) {
            if (pages.isEmpty()) {
                EmptyState(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(AppSpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                ) {
                    item {
                        Text(
                            text = stringResource(R.string.home_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = AppTheme.colors.onBackground,
                        )
                    }
                    if (!reliabilityConfigured) {
                        item {
                            AppBanner(
                                title = stringResource(R.string.home_reliability_banner_title),
                                body = stringResource(R.string.home_reliability_banner_body),
                                role = AppRole.Warning,
                                icon = Icons.Outlined.WarningAmber,
                                action = stringResource(R.string.home_reliability_banner_action) to onOpenReliability,
                            )
                        }
                    }
                    items(pages, key = { it.id }) { page ->
                        PageCard(
                            page = page,
                            onOpen = { onOpenPage(page.id) },
                            onEnabledChange = { viewModel.setEnabled(page, it) },
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = onAddPage,
                modifier = Modifier.align(Alignment.BottomEnd).padding(AppSpacing.lg),
                containerColor = AppTheme.colors.primary.accent,
                contentColor = AppTheme.colors.primary.onAccent,
            ) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = stringResource(R.string.cd_add_page))
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(AppSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Text(
            text = stringResource(R.string.home_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = AppTheme.colors.onBackground,
        )
        Text(
            text = stringResource(R.string.home_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun PageCard(
    page: MonitoredPageEntity,
    onOpen: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
) {
    val enabledSwitchDescription = stringResource(R.string.home_enabled_switch_cd)
    AppCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
        Column(modifier = Modifier.padding(AppSpacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = page.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = AppTheme.colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = hostOf(page.url),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Switch(
                    checked = page.enabled,
                    onCheckedChange = onEnabledChange,
                    modifier = Modifier.semantics {
                        contentDescription = enabledSwitchDescription
                    },
                )
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = stringResource(R.string.home_open_history_cd),
                    tint = AppTheme.colors.onSurfaceVariant,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                AppStatusPill(text = stringResource(R.string.home_type_json_api), role = AppRole.Neutral)
                StatusPill(status = page.lastCheckStatus)
                if (page.lastCheckedAt != null) {
                    Text(
                        text = stringResource(R.string.home_last_checked, formatTimestamp(page.lastCheckedAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusPill(status: CheckStatus) {
    val (textRes, role) = when (status) {
        CheckStatus.NONE -> R.string.home_status_none to AppRole.Neutral
        CheckStatus.OK_NO_MATCH -> R.string.home_status_ok_no_match to AppRole.Neutral
        CheckStatus.OK_MATCH -> R.string.home_status_ok_match to AppRole.Success
        CheckStatus.ERROR -> R.string.home_status_error to AppRole.Error
    }
    AppStatusPill(text = stringResource(textRes), role = role)
}

private fun hostOf(url: String): String = runCatching { URI(url).host }.getOrNull() ?: url
