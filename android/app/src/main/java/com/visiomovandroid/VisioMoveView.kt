package com.visiomovandroid

import android.content.Context
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.visioglobe.visiomoveessential.VMEMapController
import com.visioglobe.visiomoveessential.VMEMapControllerBuilder
import com.visioglobe.visiomoveessential.VMEMapView
import com.visioglobe.visiomoveessential.listeners.VMELifeCycleListener

/**
 * React Native host view for VisioMove Essential (Visioglobe).
 *
 * This implementation is inspired by the official Jetpack Compose sample (AndroidView hosting
 * [VMEMapView]), but remains a classic Android [FrameLayout] so it can be mounted by React Native.
 *
 * The proprietary SDK binary (`VisioMoveEssential.aar`) must be available at `android/app/libs/`.
 * This repo does not commit the AAR; `android/app/build.gradle` includes a helper task to download
 * and extract it automatically when missing.
 */
class VisioMoveView(context: Context) : FrameLayout(context) {

    private val mapHashState: MutableState<String?> = mutableStateOf(null)
    private val mapSecretCodeState = mutableIntStateOf(0)

    private var mapController: VMEMapController? = null
    private var loadedMapHash: String? = null
    private var loadedMapSecretCode: Int = 0

    private val lifeCycleListener = object : VMELifeCycleListener() {}

    private val composeView: ComposeView =
        ComposeView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            // React Native doesn't always provide a ViewTreeLifecycleOwner; dispose on detach.
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                val mapHash = mapHashState.value
                val mapSecretCode = mapSecretCodeState.intValue
                var mapView by remember { mutableStateOf<VMEMapView?>(null) }

                if (mapHash.isNullOrBlank()) {
                    Placeholder(
                        "Set `mapHash` to load the VisioMove map."
                    )
                } else {
                    AndroidView(
                        modifier = Modifier.fillMaxSize().background(Color.Transparent),
                        factory = { ctx ->
                            VMEMapView(ctx, null).also { created ->
                                mapView = created
                            }
                        },
                        update = {
                            // no-op; controller wiring happens in LaunchedEffect below.
                        },
                    )

                    LaunchedEffect(mapHash, mapSecretCode, mapView) {
                        val v = mapView ?: return@LaunchedEffect
                        ensureControllerLoaded(v, mapHash, mapSecretCode)
                    }
                }
            }
        }

    init {
        addView(composeView)
    }

    /**
     * Set the online map hash (from Visioglobe map server).
     * Call this and [setMapSecretCode] before the view is attached; loading starts when both are set.
     */
    fun setMapHash(mapHash: String?) {
        if (mapHashState.value == mapHash) return
        mapHashState.value = mapHash
    }

    /**
     * Set the map secret code (default 0).
     */
    fun setMapSecretCode(code: Int) {
        if (mapSecretCodeState.intValue == code) return
        mapSecretCodeState.intValue = code
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mapController = null
    }

    private fun ensureControllerLoaded(mapView: VMEMapView, mapHash: String, mapSecretCode: Int) {
        if (mapController != null && loadedMapHash == mapHash && loadedMapSecretCode == mapSecretCode) {
            return
        }

        val builder =
            VMEMapControllerBuilder().apply {
                this.mapHash = mapHash
                this.mapSecretCode = mapSecretCode
            }

        mapController =
            VMEMapController(context, builder).also { controller ->
                loadedMapHash = mapHash
                loadedMapSecretCode = mapSecretCode
                controller.setLifeCycleListener(lifeCycleListener)
                controller.loadMapData()
                controller.loadMapView(mapView)
            }
    }

    @androidx.compose.runtime.Composable
    private fun Placeholder(text: String) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF0F0F0))
                    .padding(16.dp),
        ) {
            Text(text = text, color = Color(0xFF666666))
        }
    }
}
