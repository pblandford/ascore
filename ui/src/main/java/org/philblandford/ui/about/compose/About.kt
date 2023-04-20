package org.philblandford.ui.about.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.philblandford.ui.BuildConfig
import org.philblandford.ui.R
import org.philblandford.ui.common.block
import org.philblandford.ui.theme.DialogTheme
import org.philblandford.ui.util.LabelText

@Composable
fun About() {

    DialogTheme { modifier ->
        Column(modifier.fillMaxWidth()) {
            AboutRow(R.string.product_version_id, BuildConfig.VERSION_CODE.toString())
            AboutRow(R.string.product_version, BuildConfig.VERSION_NAME)
            AboutRow(R.string.product_build, BuildConfig.BUILD_TYPE)
        }
    }
}

@Composable
private fun AboutRow(key: Int, value: String) {
    Row {
        LabelText(resId = key, modifier = Modifier.width(block(4)))
        LabelText(value)
    }
}