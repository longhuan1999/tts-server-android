package com.github.jing332.tts_server_android.compose.nav.systts.edit.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.nav.systts.edit.BasicInfoEditScreen
import com.github.jing332.tts_server_android.compose.nav.systts.edit.IntSlider
import com.github.jing332.tts_server_android.compose.nav.systts.edit.ui.base.AuditionTextField
import com.github.jing332.tts_server_android.compose.nav.systts.edit.ui.base.TtsTopAppBar
import com.github.jing332.tts_server_android.compose.nav.systts.list.AuditionDialog
import com.github.jing332.tts_server_android.compose.widgets.DenseOutlinedField
import com.github.jing332.tts_server_android.compose.widgets.ExposedDropTextField
import com.github.jing332.tts_server_android.compose.widgets.LabelSlider
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.model.speech.tts.LocalTTS

class LocalTtsUI : TtsUI() {
    @Composable
    override fun ParamsEditScreen(
        modifier: Modifier,
        systts: SystemTts,
        onSysttsChange: (SystemTts) -> Unit
    ) {
        var showDirectPlayHelpDialog by remember { mutableStateOf(false) }
        if (showDirectPlayHelpDialog)
            AlertDialog(
                onDismissRequest = { showDirectPlayHelpDialog = false },
                title = { Text(stringResource(id = R.string.systts_direct_play_help)) },
                text = { Text(stringResource(id = R.string.systts_direct_play_help_msg)) },
                confirmButton = {
                    TextButton(onClick = { showDirectPlayHelpDialog = false }) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                })

        Column(modifier) {
            val tts = systts.tts as LocalTTS
            val rateStr = stringResource(
                id = R.string.label_speech_rate,
                if (tts.rate == 0) stringResource(id = R.string.follow_system_or_read_aloud_app) else tts.rate.toString()
            )
            IntSlider(label = rateStr, value = tts.rate.toFloat(), onValueChange = {
                onSysttsChange(systts.copy(tts = tts.copy(rate = it.toInt())))
            }, valueRange = 0f..100f)

            val pitchStr = stringResource(
                id = R.string.label_speech_pitch,
                if (tts.pitch == 0) stringResource(id = R.string.follow_system_or_read_aloud_app) else tts.pitch.toString()
            )
            LabelSlider(value = tts.pitch * 0.01f, onValueChange = {
                onSysttsChange(
                    systts.copy(
                        tts = tts.copy(
                            pitch = (it * 100).toInt()
                        )
                    )
                )
            }, valueRange = 0f..2f, text = {
                Text(pitchStr)
            })

            Row {
                var sampleRateStr by remember { mutableStateOf(tts.audioFormat.sampleRate.toString()) }
                DenseOutlinedField(
                    label = { Text(stringResource(id = R.string.systts_sample_rate)) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    value = sampleRateStr,
                    onValueChange = {
                        if (it.isEmpty()) {
                            sampleRateStr = it
                        } else {
                            sampleRateStr = it.toInt().toString()
                            onSysttsChange(systts.copy(tts = tts.copy(audioFormat = tts.audioFormat.apply {
                                this.sampleRate = it.toInt()
                            })))
                        }
                    }
                )

                Row(
                    Modifier
                        .minimumInteractiveComponentSize()
                        .padding(8.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(role = Role.Checkbox) {
                            onSysttsChange(systts.copy(tts = tts.copy(isDirectPlayMode = !tts.isDirectPlayMode)))
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = tts.isDirectPlayMode, onCheckedChange = null)
                    Text(text = stringResource(id = R.string.direct_play))
                    IconButton(onClick = { showDirectPlayHelpDialog = true }) {
                        Icon(
                            Icons.Default.HelpOutline,
                            stringResource(id = R.string.systts_direct_play_help)
                        )
                    }
                }
            }
        }
    }

    @Composable
    override fun FullEditScreen(
        modifier: Modifier,
        systts: SystemTts,
        onSysttsChange: (SystemTts) -> Unit,
        onSave: () -> Unit,
        onCancel: () -> Unit
    ) {
        val saveEvent = rememberCallbackState()
        Scaffold(
            topBar = {
                TtsTopAppBar(
                    title = { Text(stringResource(id = R.string.edit_local_tts)) },
                    onBackAction = onCancel,
                    onSaveAction = {
                        saveEvent.value?.invoke()
                        onSave()
                    }
                )
            }
        ) { paddingValues ->
            Content(
                modifier = Modifier
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                systts = systts,
                onSysttsChange = onSysttsChange,
                saveEvent = saveEvent
            )
        }
    }

    @Composable
    private fun Content(
        modifier: Modifier,
        systts: SystemTts,
        onSysttsChange: (SystemTts) -> Unit,
        saveEvent: CallbackState,
        vm: LocalTtsViewModel = viewModel()
    ) {
        val tts = systts.tts as LocalTTS
        var showAuditionDialog by remember { mutableStateOf(false) }
        if (showAuditionDialog)
            AuditionDialog(systts = systts) {
                showAuditionDialog = false
            }

        Column(modifier) {
            Column(Modifier.padding(horizontal = 8.dp)) {

                BasicInfoEditScreen(
                    modifier = Modifier.fillMaxWidth(),
                    systts = systts,
                    onSysttsChange = onSysttsChange,
                    saveEvent = saveEvent,
                )
                AuditionTextField(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp), onAudition = {
                    showAuditionDialog = true
                })

                LaunchedEffect(tts.engine) {
                    vm.setEngine(tts.engine ?: "")
                }

                ExposedDropTextField(
                    label = { Text(stringResource(id = R.string.label_tts_engine)) },
                    key = tts.engine ?: "",
                    keys = vm.engines.map { it.name },
                    values = vm.engines.map { it.label },
                    onSelectedChange = { k, _ ->
                        onSysttsChange(systts.copy(tts = tts.copy(engine = k as String)))
                    }
                )

                ExposedDropTextField(
                    label = { Text(stringResource(id = R.string.label_language)) },
                    key = tts.locale,
                    keys = vm.locales.map { it.toLanguageTag() },
                    values = vm.locales.map { it.displayName },
                    onSelectedChange = { k, _ ->
                        onSysttsChange(systts.copy(tts = tts.copy(locale = k as String)))
                    }
                )

                ExposedDropTextField(
                    label = { Text(stringResource(id = R.string.label_voice)) },
                    key = tts.voiceName ?: "",
                    keys = vm.voices.map { it.name },
                    values = vm.voices.map { it.name },
                    onSelectedChange = { k, _ ->
                        onSysttsChange(systts.copy(tts = tts.copy(voiceName = k as String)))
                    }
                )
            }

            ParamsEditScreen(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                systts = systts,
                onSysttsChange = onSysttsChange
            )
        }
    }
}