package top.focess.netdesign.ui

import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.input.PasswordVisualTransformation

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    onEnterKey: () -> Unit = {}
) {

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.onKeyEvent {
            if (it.key == Key.Enter)
                onEnterKey()
            it.key == Key.Enter
        },
        label = label,
        singleLine = singleLine,
        visualTransformation = PasswordVisualTransformation()
    );
}