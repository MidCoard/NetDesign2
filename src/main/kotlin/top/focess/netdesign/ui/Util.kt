package top.focess.netdesign.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.Key.Companion.Menu
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import top.focess.netdesign.config.LangFile

@Composable
fun CustomLayout(
    modifier: Modifier = Modifier,
    how: Placeable.PlacementScope.(measurables: List<Measurable>, constraints: Constraints) -> Unit,
    children: @Composable () -> Unit
) = Layout({ children() }, modifier) { measurables, constraints ->
    layout(constraints.maxWidth, constraints.maxHeight) {
        how(measurables, constraints)
    }
}

@Composable
fun LangFile.LangScope.DefaultView(
    state: WindowState = rememberWindowState(),
    title: String,
    onCloseRequest: () -> Unit = {},
    colors: Colors = DefaultTheme.colors(),
    children: @Composable ColumnScope.() -> Unit
) {

    Window(
        onCloseRequest = onCloseRequest, state = state, title = title,
        transparent = true,
        resizable = false,
        undecorated = true
    ) {

        MenuBar {
            Menu("menu.status.title".l, mnemonic = 'S') {
                Item("menu.status.quit".l, onClick = {
                    onCloseRequest()
                }, shortcut = KeyShortcut(Key.Q, ctrl = true))
            }
            Menu("menu.language.title".l, mnemonic = 'L'){
                Item("menu.language.chinese".l, onClick = {
                    langFile = LangFile("langs/zh_CN.yml")
                })
                Item("menu.language.english".l, onClick = {
                    langFile = LangFile("langs/en_US.yml")
                })
            }
        }

        Box(Modifier.clip(RoundedCornerShape(5.dp))) {

            MaterialTheme(colors = colors) {
                Column(
                    Modifier.background(colors.background)
                ) {

                    WindowDraggableArea {

                        Row(Modifier.fillMaxWidth().height(48.dp).background(colors.primary)) {

                            Spacer(modifier = Modifier.weight(3f))


                            CustomLayout(modifier = Modifier.weight(6f), how = {
                                    measurables, constraints ->
                                val placeable =
                                    measurables[0].measure(
                                        constraints.copy(minWidth = 0, minHeight = 0)
                                    )
                                val x = (constraints.maxWidth - placeable.width) / 2
                                val y = (constraints.maxHeight - placeable.height) / 2
                                placeable.placeRelative(x, y)
                            }) {
                                Text(
                                    text = title,
                                    fontSize = 18.sp, textAlign = TextAlign.Center,
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            CustomLayout(modifier = Modifier.weight(2f), how = {
                                measurables, constraints ->
                                val offset = 128;
                                measurables[0].measure(
                                    constraints.copy(minWidth = 0, maxWidth = offset)
                                ).place(constraints.maxWidth - offset, 0)
                            }) {
                                Button(modifier = Modifier.fillMaxHeight(), onClick = onCloseRequest) {
                                    Icon(Icons.Default.Close, "close".l)
                                }
                            }
                        }
                    }
                    children()
                }
            }
        }
    }
}

@Composable
fun LangFile.LangScope.SurfaceView(
    state: WindowState = rememberWindowState(),
    title: String,
    onCloseRequest: () -> Unit = {},
    colors: Colors = DefaultTheme.colors(),
    children: @Composable () -> Unit
) {
    DefaultView(state = state, title = title, onCloseRequest = onCloseRequest, colors = colors) {
        Surface {
            Column {
                children()
            }
        }
    }
}

@Composable
fun LangFile.LangScope.ProgressionIcon(contentDescription: String? = "progress".l) {
    val infiniteTransition = rememberInfiniteTransition()
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Icon(
        painter = painterResource("icons/progress_activity.svg"),
        contentDescription = contentDescription,
        modifier = Modifier.rotate(angle)
    )
}

