package top.focess.netdesign.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import top.focess.netdesign.config.LangFile

class FocessDialog(_title: String = "", _message: String = "") {
    var title by mutableStateOf(_title)
    var message by mutableStateOf(_message)
    var show by mutableStateOf(false)
    fun show() {
        show = true
    }
}

@Composable
fun LangFile.LangScope.FocessDialogWindow(focessDialog: FocessDialog) {

    val state = rememberDialogState(size = DpSize(275.dp, Dp.Unspecified))

    DialogWindow(
        onCloseRequest = { focessDialog.show = false },
        state = state,
        visible = focessDialog.show,
        title = focessDialog.title,
        undecorated = true,
        transparent = true,
    ) {
        Box(Modifier.clip(RoundedCornerShape(5.dp))) {
            MaterialTheme(colors = DefaultTheme.colors()) {

                Column(Modifier.background(DefaultTheme.colors().background)) {
                    WindowDraggableArea {

                        Row(Modifier.fillMaxWidth().height(48.dp).background(DefaultTheme.colors().primary)) {

                            Spacer(modifier = Modifier.weight(3f))


                            CustomLayout(modifier = Modifier.weight(6f), how = { measurables, constraints ->
                                val placeable =
                                    measurables[0].measure(
                                        constraints.copy(minWidth = 0, minHeight = 0)
                                    )
                                val x = (constraints.maxWidth - placeable.width) / 2
                                val y = (constraints.maxHeight - placeable.height) / 2
                                placeable.placeRelative(x, y)
                            }) {
                                Text(
                                    text = focessDialog.title.l,
                                    fontSize = 18.sp, textAlign = TextAlign.Center,
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            CustomLayout(modifier = Modifier.weight(2f), how = { measurables, constraints ->
                                val offset = 128;
                                measurables[0].measure(
                                    constraints.copy(minWidth = 0, maxWidth = offset)
                                ).place(constraints.maxWidth - offset, 0)
                            }) {
                                Button(
                                    modifier = Modifier.fillMaxHeight(),
                                    onClick = { focessDialog.show = false }) {
                                    Icon(Icons.Default.Close, "close".l)
                                }
                            }
                        }
                    }

                    Surface {
                        Column {
                            Row(
                                Modifier.fillMaxWidth().height(50.dp).padding(horizontal = 32.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = focessDialog.message)
                            }

                            Row(Modifier.fillMaxWidth().height(55.dp), horizontalArrangement = Arrangement.Center) {
                                Button(onClick = { focessDialog.show = false }) {
                                    Text("dialog.ok".l)
                                }
                            }
                        }
                    }


                }
            }
        }


    }

}