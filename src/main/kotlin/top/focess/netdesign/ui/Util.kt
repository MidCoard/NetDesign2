package top.focess.netdesign.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState

val Int.tdp: TextUnit
    @Composable
    get() = with(LocalDensity.current) {
        this@tdp.dp.toSp()
    }

@Composable
fun CustomLayout(
    modifier: Modifier,
    how: Placeable.PlacementScope.(measurables: List<Measurable>, constraints: Constraints) -> Unit,
    children: @Composable () -> Unit
) = Layout({ children() }, modifier) { measurables, constraints ->
    layout(constraints.maxWidth, constraints.maxHeight) {
        how(measurables, constraints)
    }
}

@Composable
fun DefaultView(
    state: WindowState = rememberWindowState(),
    title: String,
    onCloseRequest: () -> Unit = {},
    colors: Colors = DefaultTheme.colors(),
    children: @Composable () -> Unit
) {

    Window(
        onCloseRequest = onCloseRequest, state = state, title = title,
//        transparent = true,
        resizable = false,
        undecorated = true
    ) {
        Box(Modifier.clip(RoundedCornerShape(5.dp))) {

            MaterialTheme(colors = colors) {

                Column(
//                    Modifier.background(colors.background)
                ) {

                    WindowDraggableArea {

                        CustomLayout(
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                                .background(MaterialTheme.colors.primary),
                            how = { measurables, constraints ->
                                val titleBar = measurables[0]
                                    .measure(
                                        Constraints(
                                            0,
                                            constraints.maxWidth * 2 / 3,
                                            constraints.minHeight,
                                            constraints.maxHeight
                                        )
                                    )

                                titleBar.place(
                                    constraints.maxWidth / 2 - titleBar.width / 2,
                                    constraints.maxHeight / 2 - 18
                                )

                                val exit = measurables[1]
                                    .measure(
                                        Constraints(
                                            0,
                                            constraints.maxWidth / 3,
                                            constraints.minHeight,
                                            constraints.maxHeight
                                        )
                                    )
                                exit.place(constraints.maxWidth - exit.width, 0)
                            }
                        ) {
                            Text(text = title, fontSize = 18.tdp, textAlign = TextAlign.Center)
                            Button(modifier = Modifier.fillMaxHeight(), onClick = onCloseRequest) {
                                Text("X")
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
fun SurfaceView(
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