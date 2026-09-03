package com.wafflehq.monitoring.ui.pageform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wafflehq.monitoring.R
import com.wafflehq.monitoring.ui.components.AppButton
import com.wafflehq.monitoring.ui.components.AppStatusPill
import com.wafflehq.monitoring.ui.components.AppTextField
import com.wafflehq.monitoring.ui.components.ButtonVariant
import com.wafflehq.monitoring.ui.components.SettingsScaffold
import com.wafflehq.monitoring.ui.components.SettingsSwitchRow
import com.wafflehq.monitoring.ui.theme.AppRadius
import com.wafflehq.monitoring.ui.theme.AppRole
import com.wafflehq.monitoring.ui.theme.AppSpacing
import com.wafflehq.monitoring.ui.theme.AppTheme
import com.wafflehq.monitoring.ui.theme.GeistMono

@Composable
fun PageFormScreen(
    onBack: () -> Unit,
    viewModel: PageFormViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved, state.deleted) {
        if (state.saved || state.deleted) onBack()
    }

    SettingsScaffold(
        title = stringResource(
            if (state.isEditing) R.string.page_form_title_edit else R.string.page_form_title_new,
        ),
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
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                AppTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    label = stringResource(R.string.page_form_name_label),
                    role = AppRole.Primary,
                    isError = state.nameError,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (state.nameError) {
                    ErrorText(stringResource(R.string.page_form_error_name_required))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Text(
                    text = stringResource(R.string.page_form_type_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = AppTheme.colors.onSurfaceVariant,
                )
                AppStatusPill(text = stringResource(R.string.home_type_json_api), role = AppRole.Neutral)
            }

            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                AppTextField(
                    value = state.url,
                    onValueChange = viewModel::onUrlChange,
                    label = stringResource(R.string.page_form_url_label),
                    role = AppRole.Primary,
                    isError = state.urlError,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (state.urlError) {
                    val messageRes = if (state.url.isBlank()) {
                        R.string.page_form_error_url_required
                    } else {
                        R.string.page_form_error_url_invalid
                    }
                    ErrorText(stringResource(messageRes))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                AppTextField(
                    value = state.jsonPath,
                    onValueChange = viewModel::onJsonPathChange,
                    label = stringResource(R.string.page_form_json_path_label),
                    role = AppRole.Primary,
                    isError = state.pathError,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (state.pathError) {
                    ErrorText(stringResource(R.string.page_form_error_path_required))
                }
                Text(
                    text = stringResource(R.string.page_form_json_path_helper),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceVariant,
                )
            }

            SettingsSwitchRow(
                title = stringResource(R.string.page_form_enabled_label),
                subtitle = stringResource(R.string.page_form_enabled_sub),
                checked = state.enabled,
                onCheckedChange = viewModel::onEnabledChange,
            )

            AppButton(
                text = stringResource(R.string.page_form_test_button),
                role = AppRole.Secondary,
                variant = ButtonVariant.Tonal,
                onClick = viewModel::testNow,
                modifier = Modifier.fillMaxWidth(),
            )

            TestResultRow(state.testResult)

            AppButton(
                text = stringResource(R.string.label_save),
                role = AppRole.Primary,
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.isEditing) {
                AppButton(
                    text = stringResource(R.string.label_delete),
                    role = AppRole.Error,
                    variant = ButtonVariant.Outlined,
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.page_form_delete_confirm_title)) },
            text = { Text(stringResource(R.string.page_form_delete_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.delete()
                }) { Text(stringResource(R.string.label_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.label_cancel))
                }
            },
        )
    }
}

@Composable
private fun TestResultRow(result: TestResult) {
    val (text, role, rawResponse) = when (result) {
        TestResult.Idle -> return
        TestResult.Running -> Triple(stringResource(R.string.page_form_test_running), AppRole.Neutral, null)
        is TestResult.Match -> Triple(
            stringResource(R.string.page_form_test_result_match, result.count),
            AppRole.Success,
            result.rawResponse,
        )
        is TestResult.NoMatch -> Triple(
            stringResource(R.string.page_form_test_result_no_match),
            AppRole.Neutral,
            result.rawResponse,
        )
        is TestResult.Error -> Triple(
            stringResource(R.string.page_form_test_result_error, result.message),
            AppRole.Error,
            result.rawResponse,
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        AppStatusPill(text = text, role = role)
        if (rawResponse != null) {
            Text(
                text = stringResource(R.string.page_form_test_response_label),
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colors.onSurfaceVariant,
            )
            ResponseBox(rawResponse)
        }
    }
}

@Composable
private fun ResponseBox(rawResponse: String) {
    val colors = AppTheme.colors
    SelectionContainer {
        Text(
            text = rawResponse,
            style = TextStyle(fontFamily = GeistMono, fontSize = 12.sp, lineHeight = 16.sp),
            color = colors.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 240.dp)
                .verticalScroll(rememberScrollState())
                .background(colors.surfaceVariant, RoundedCornerShape(AppRadius.card))
                .padding(AppSpacing.sm),
        )
    }
}

@Composable
private fun ErrorText(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = AppTheme.colors.error.accent,
    )
}
