package com.parallelc.mixflipmod.ui.component

import android.content.Context
import android.content.SharedPreferences
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.parallelc.mixflipmod.R
import com.parallelc.mixflipmod.model.PrefSpec
import com.parallelc.mixflipmod.ui.util.checkScope
import io.github.libxposed.service.XposedService
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.DropdownArrowEndAction
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.SliderDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayListPopup
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import kotlin.math.roundToInt

@Composable
fun PrefSpecItem(
    spec: PrefSpec,
    prefs: SharedPreferences,
    xposedService: XposedService?,
    scopePackage: String,
) {
    when (spec) {
        is PrefSpec.Switch -> {
            val checked = remember(prefs, spec.prefKey) { mutableStateOf(prefs.getBoolean(spec.prefKey, false)) }
            SwitchPreference(
                title = stringResource(spec.titleRes),
                summary = spec.summaryRes?.let { stringResource(it) },
                checked = checked.value,
                onCheckedChange = {
                    checked.value = it
                    prefs.edit { putBoolean(spec.prefKey, it) }
                    runCatching { checkScope(xposedService, scopePackage, it) }
                },
            )
            if (checked.value) {
                spec.children.forEach {
                    PrefSpecItem(
                        spec = it,
                        prefs = prefs,
                        xposedService = xposedService,
                        scopePackage = scopePackage,
                    )
                }
            }
        }
        is PrefSpec.IntInput -> {
            val stored = remember { mutableIntStateOf(prefs.getInt(spec.prefKey, spec.defaultValue)) }
            val maxValue = spec.maxValue ?: spec.minValue.coerceAtLeast(spec.defaultValue)
            val slider: @Composable () -> Unit = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Slider(
                        value = stored.intValue.toFloat(),
                        onValueChange = {
                            val value = it.roundToInt().coerceIn(spec.minValue, maxValue)
                            if (stored.intValue != value) {
                                stored.intValue = value
                                prefs.edit { putInt(spec.prefKey, value) }
                            }
                        },
                        valueRange = spec.minValue.toFloat()..maxValue.toFloat(),
                        steps = (maxValue - spec.minValue - 1).coerceAtLeast(0),
                        hapticEffect = SliderDefaults.SliderHapticEffect.Step,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = stored.intValue.toString(),
                        modifier = Modifier.width(40.dp),
                        color = colorScheme.onSurfaceVariantSummary,
                        textAlign = TextAlign.End,
                    )
                }
            }
            if (spec.titleRes == null) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    slider()
                }
            } else {
                BasicComponent(
                    title = stringResource(spec.titleRes),
                    summary = spec.summaryRes?.let { stringResource(it) } ?: stored.intValue.toString(),
                    bottomAction = { slider() },
                )
            }
        }
        is PrefSpec.StringInput -> {
            val stored = remember { mutableStateOf(prefs.getString(spec.prefKey, spec.defaultValue) ?: spec.defaultValue) }
            BasicComponent(
                title = stringResource(spec.titleRes),
                summary = spec.summaryRes?.let { stringResource(it) },
                bottomAction = {
                    TextField(
                        value = stored.value,
                        onValueChange = {
                            stored.value = it
                            prefs.edit { putString(spec.prefKey, it.trim()) }
                        },
                        label = stringResource(spec.titleRes),
                        useLabelAsPlaceholder = true,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
            )
        }
        is PrefSpec.ImeSelect -> {
            val context = LocalContext.current
            val pm = context.packageManager
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val imeOptions = remember {
                imm.inputMethodList
                    .groupBy { it.serviceInfo.packageName }
                    .map { (pkg, methods) -> pkg to methods.first().loadLabel(pm).toString() }
                    .sortedBy { it.second.lowercase() }
            }
            val optionCount = imeOptions.size + 1
            var stored by remember { mutableStateOf(prefs.getString(spec.prefKey, "") ?: "") }
            var showDropdown by remember { mutableStateOf(false) }
            val currentLabel = if (stored.isEmpty()) null
                else imeOptions.find { it.first == stored }?.second ?: stored
            BasicComponent(
                title = stringResource(spec.titleRes),
                endActions = {
                    Box {
                        OverlayListPopup(
                            show = showDropdown,
                            alignment = PopupPositionProvider.Align.End,
                            onDismissRequest = { showDropdown = false },
                        ) {
                            ListPopupColumn {
                                DropdownImpl(
                                    text = stringResource(R.string.ime_select_default),
                                    optionSize = optionCount,
                                    isSelected = stored.isEmpty(),
                                    index = 0,
                                    onSelectedIndexChange = {
                                        stored = ""
                                        prefs.edit { putString(spec.prefKey, "") }
                                        showDropdown = false
                                    },
                                )
                                imeOptions.forEachIndexed { index, (pkg, label) ->
                                    DropdownImpl(
                                        text = label,
                                        optionSize = optionCount,
                                        isSelected = stored == pkg,
                                        index = index + 1,
                                        onSelectedIndexChange = {
                                            stored = pkg
                                            prefs.edit { putString(spec.prefKey, pkg) }
                                            showDropdown = false
                                        },
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.clickable { showDropdown = true },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = currentLabel ?: stringResource(R.string.ime_select_default),
                                color = if (stored.isEmpty()) colorScheme.onSurfaceVariantSummary else colorScheme.primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            Spacer(Modifier.size(8.dp))
                            DropdownArrowEndAction(actionColor = colorScheme.onSurfaceVariantActions)
                        }
                    }
                },
                holdDownState = showDropdown,
            )
        }
        is PrefSpec.ListSelect -> {
            val optionCount = spec.entries.size
            var stored by remember { mutableIntStateOf(prefs.getInt(spec.prefKey, spec.defaultValue)) }
            var showDropdown by remember { mutableStateOf(false) }
            val selectedIndex = spec.entryValues.indexOf(stored).coerceAtLeast(0)
            BasicComponent(
                title = stringResource(spec.titleRes),
                summary = spec.summaryRes?.let { stringResource(it) },
                endActions = {
                    Box {
                        OverlayListPopup(
                            show = showDropdown,
                            alignment = PopupPositionProvider.Align.End,
                            onDismissRequest = { showDropdown = false },
                        ) {
                            ListPopupColumn {
                                spec.entries.forEachIndexed { index, entryRes ->
                                    DropdownImpl(
                                        text = stringResource(entryRes),
                                        optionSize = optionCount,
                                        isSelected = index == selectedIndex,
                                        index = index,
                                        onSelectedIndexChange = {
                                            val newValue = spec.entryValues[index]
                                            stored = newValue
                                            prefs.edit { putInt(spec.prefKey, newValue) }
                                            showDropdown = false
                                        },
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.clickable { showDropdown = true },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(spec.entries[selectedIndex]),
                                color = if (stored == spec.defaultValue) colorScheme.onSurfaceVariantSummary else colorScheme.primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            Spacer(Modifier.size(8.dp))
                            DropdownArrowEndAction(actionColor = colorScheme.onSurfaceVariantActions)
                        }
                    }
                },
                holdDownState = showDropdown,
            )
        }
    }
}
