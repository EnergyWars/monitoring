package com.wafflehq.monitoring.ui.settings

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wafflehq.monitoring.R
import com.wafflehq.monitoring.ui.components.AppButton
import com.wafflehq.monitoring.ui.components.AppCard
import com.wafflehq.monitoring.ui.components.AppStatusPill
import com.wafflehq.monitoring.ui.components.ButtonVariant
import com.wafflehq.monitoring.ui.components.SettingsScaffold
import com.wafflehq.monitoring.ui.theme.AppRole
import com.wafflehq.monitoring.ui.theme.AppSpacing
import com.wafflehq.monitoring.ui.theme.AppTheme

@Composable
fun ReliabilitySettingsScreen(
    onBack: () -> Unit,
    viewModel: ReliabilitySettingsViewModel = hiltViewModel(),
) {
    val status by viewModel.status.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { viewModel.refresh() }

    SettingsScaffold(
        title = stringResource(R.string.reliability_title),
        onBack = onBack,
        backDescription = stringResource(R.string.label_back),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            Text(
                text = stringResource(R.string.reliability_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.onSurfaceVariant,
            )

            ReliabilityRow(
                title = stringResource(R.string.reliability_notifications_title),
                body = stringResource(R.string.reliability_notifications_body),
                granted = status.notificationsGranted,
                actionLabel = stringResource(R.string.reliability_notifications_action),
                onAction = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        context.startActivity(appNotificationSettingsIntent(context))
                    }
                },
            )

            ReliabilityRow(
                title = stringResource(R.string.reliability_battery_title),
                body = stringResource(R.string.reliability_battery_body),
                granted = status.batteryOptimizationIgnored,
                actionLabel = stringResource(R.string.reliability_battery_action),
                onAction = { context.startActivity(ignoreBatteryOptimizationsIntent(context)) },
            )

            ReliabilityRow(
                title = stringResource(R.string.reliability_alarms_title),
                body = stringResource(R.string.reliability_alarms_body),
                granted = status.exactAlarmsAllowed,
                actionLabel = stringResource(R.string.reliability_alarms_action),
                onAction = { context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)) },
            )

            if (status.showAutostart) {
                ReliabilityRow(
                    title = stringResource(R.string.reliability_autostart_title),
                    body = stringResource(R.string.reliability_autostart_body),
                    granted = null,
                    actionLabel = stringResource(R.string.reliability_autostart_action),
                    onAction = { openAutostartSettings(context) },
                )
            }
        }
    }
}

@Composable
private fun ReliabilityRow(
    title: String,
    body: String,
    granted: Boolean?,
    actionLabel: String,
    onAction: () -> Unit,
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(AppSpacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = AppTheme.colors.onSurface,
                    modifier = Modifier.weight(1f),
                )
                if (granted != null) {
                    AppStatusPill(
                        text = stringResource(
                            if (granted) R.string.reliability_status_granted else R.string.reliability_status_missing,
                        ),
                        role = if (granted) AppRole.Success else AppRole.Warning,
                    )
                }
            }
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(top = AppSpacing.xs, bottom = AppSpacing.sm),
            )
            if (granted != true) {
                AppButton(
                    text = actionLabel,
                    role = AppRole.Primary,
                    variant = ButtonVariant.Tonal,
                    onClick = onAction,
                )
            }
        }
    }
}

private fun ignoreBatteryOptimizationsIntent(context: Context): Intent =
    Intent(
        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
        Uri.parse("package:${context.packageName}"),
    )

private fun appNotificationSettingsIntent(context: Context): Intent =
    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)

private val XIAOMI_AUTOSTART_COMPONENT = ComponentName(
    "com.miui.securitycenter",
    "com.miui.permcenter.autostart.AutoStartManagementActivity",
)

private fun openAutostartSettings(context: Context) {
    val intent = Intent().apply { component = XIAOMI_AUTOSTART_COMPONENT }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")),
        )
    }
}
