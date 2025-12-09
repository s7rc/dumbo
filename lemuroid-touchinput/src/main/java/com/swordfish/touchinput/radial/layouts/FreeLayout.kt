package com.swordfish.touchinput.radial.layouts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import com.swordfish.touchinput.radial.ui.LemuroidButtonForeground
import com.swordfish.touchinput.radial.ui.LemuroidControlBackground
import gg.padkit.PadKitScope
import gg.padkit.controls.ControlButton
import gg.padkit.ids.Id

data class FreeControl(
    val id: String,
    val defaultParams: TouchControllerSettingsManager.ControlSettings,
    val content: @Composable (Modifier) -> Unit
)

@Composable
fun FreeLayout(
    settings: TouchControllerSettingsManager.Settings,
    controls: List<FreeControl>,
    isEditing: Boolean = false,
    onControlUpdate: (String, TouchControllerSettingsManager.ControlSettings) -> Unit = { _, _ -> },
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val containerWidth = maxWidth.value // In dp? No, usage of constraints returns px?
        // maxWidth is Dp. We need pixels for dragAmount.
        // BoxWithConstraints scope provides constraints.maxWidth (int px) and maxWidth (Dp).
        
        val density = androidx.compose.ui.platform.LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        controls.forEach { control ->
            val id = control.id
            val params = settings.controls[id] ?: control.defaultParams
            
            val xOffset = maxWidth * params.x
            val yOffset = maxHeight * params.y
            
            Box(
                modifier = Modifier
                    .absoluteOffset(x = xOffset, y = yOffset)
                    .then(
                        if (isEditing) {
                            Modifier.pointerInput(Unit) {
                                androidx.compose.foundation.gestures.detectTransformGestures { _, pan, zoom, _ ->
                                    val newX = (params.x + pan.x / widthPx).coerceIn(0f, 1f)
                                    val newY = (params.y + pan.y / heightPx).coerceIn(0f, 1f)
                                    val newScale = (params.scale * zoom).coerceIn(0.5f, 5.0f)
                                    onControlUpdate(id, params.copy(x = newX, y = newY, scale = newScale))
                                }
                            }
                        } else Modifier
                    )
            ) {
                 control.content(
                    Modifier.scale(settings.scale * params.scale)
                )
            }
        }
    }
}

context(PadKitScope)
@Composable
fun FreeControlButton(
    modifier: Modifier = Modifier,
    id: Id.Key,
    label: String? = null,
    icon: Int? = null,
) {
    val theme = LocalLemuroidPadTheme.current
    ControlButton(
        modifier = modifier.padding(theme.padding),
        id = id,
        foreground = { LemuroidButtonForeground(pressed = it, icon = icon, label = label) },
        background = { LemuroidControlBackground() },
    )
}
