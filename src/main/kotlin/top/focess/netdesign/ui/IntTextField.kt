package top.focess.netdesign.ui

import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged

@Composable
fun IntTextField(initText: String, value: Int, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier, label: @Composable (() -> Unit)? = null, singleLine: Boolean = false,
                 left: Int,
                 right: Int
                 // right is inclusive
) {
    var text by remember { mutableStateOf(initText) }
    var first by remember { mutableStateOf(true) }
    var focus by remember { mutableStateOf(false) }

    if (!focus) {
        if (first) {
            if (initText != text)
                first = false
        } else {
            val v = text.toIntOrNull() ?: 0
            if (v < left) {
                if (value != left)
                    onValueChange(left)
                text = left.toString()
            } else if (v > right) {
                if (value != right)
                    onValueChange(right)
                text = right.toString()
            } else if (v != value) {
                onValueChange(v)
                text = v.toString()
            } else if (text != v.toString())
                text = v.toString()
        }
    }

    TextField(text, onValueChange = {text = it}, modifier = modifier.onFocusChanged {
        focus = it.isFocused
    }, label = label, singleLine = singleLine);
}

@Composable
fun IntTextField(initText: String, value: Int, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier, label: @Composable (() -> Unit)? = null, singleLine: Boolean = false) {
    IntTextField(initText, value, onValueChange, modifier, label, singleLine, Int.MIN_VALUE, Int.MAX_VALUE)
}

@Composable
fun IntTextField(value: Int, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier, label: @Composable (() -> Unit)? = null, singleLine: Boolean = false) {
    IntTextField(value.toString(), value, onValueChange, modifier, label, singleLine)
}