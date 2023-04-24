package org.philblandford.ui.settings.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import com.philblandford.kscore.engine.core.area.factory.TextType
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.common.Gap
import org.philblandford.ui.common.block
import org.philblandford.ui.settings.model.SettingsModel
import org.philblandford.ui.settings.viewmodel.SettingsInterface
import org.philblandford.ui.settings.viewmodel.SettingsViewModel
import org.philblandford.ui.stubs.StubSettingsInterface
import org.philblandford.ui.theme.AscoreTheme
import org.philblandford.ui.theme.DialogTheme
import org.philblandford.ui.util.LabelText
import org.philblandford.ui.util.TextSpinner

@Composable
fun Settings() {
    VMView(SettingsViewModel::class.java) { model, iface, _ ->
        SettingsInternal(model, iface)
    }
}

@Composable
private fun SettingsInternal(model: SettingsModel, iface: SettingsInterface) {

    var showColorDialog by remember { mutableStateOf(false) }

    DialogTheme { modifier ->
        Column(modifier.fillMaxWidth().clickable { showColorDialog = true }) {
            ItemRow(R.string.color_scheme) {
                Box(
                    Modifier
                        .size(50.dp, 20.dp)
                        .background(model.colors.surface)
                        .border(1.dp, model.colors.onSurface)
                )
                Gap(0.5f)
                Box(
                    Modifier
                        .size(50.dp, 20.dp)
                        .background(model.colors.onSurface)
                )
            }
            FontRow(R.string.font_title_text, TextType.TITLE, model, iface::setFont)
            FontRow(R.string.font_subtitle_text, TextType.SUBTITLE, model, iface::setFont)
            FontRow(R.string.font_composer_text, TextType.COMPOSER, model, iface::setFont)
            FontRow(R.string.font_lyricist_text, TextType.LYRICIST, model, iface::setFont)
            FontRow(R.string.font_system_text, TextType.SYSTEM, model, iface::setFont)
            FontRow(R.string.font_expression_text, TextType.EXPRESSION, model, iface::setFont)
            FontRow(R.string.font_lyric_text, TextType.LYRIC, model, iface::setFont)
            FontRow(R.string.font_harmony_text, TextType.HARMONY, model, iface::setFont)
        }
    }

    if (showColorDialog) {
        Dialog({ showColorDialog = false }) {
            ColorPicker(
                Modifier,
                model.colors,
                iface::setBackgroundColor,
                iface::setForegroundColor
            )
        }
    }
}

@Composable
private fun ItemRow(label: Int, children: @Composable () -> Unit) {
    Row(Modifier.padding(vertical = 5.dp)) {
        LabelText(
            label,
            Modifier
                .width(block(4))
                .align(Alignment.CenterVertically)
        )
        children()
    }
}


@Composable
private fun FontRow(
    label: Int,
    textType: TextType,
    model: SettingsModel,
    setFont: (TextType, String) -> Unit
) {
    ItemRow(label) {
        TextSpinner(strings = model.fonts, selected = {
            model.assignedFonts[textType] ?: model.defaultFont
        }, onSelect = {
            setFont(textType, model.fonts[it])
        }, tag = textType.toString())
    }
}

@Composable
@Preview
private fun Preview() {
    AscoreTheme() {
        val model = SettingsModel(MaterialTheme.colorScheme, listOf("default", "cursive", "gothic"),
            TextType.values().associateWith { "default" }, "default"
        )

        SettingsInternal(model, StubSettingsInterface())
    }
}