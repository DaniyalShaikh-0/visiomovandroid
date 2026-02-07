package com.visiomovandroid

import android.content.Context
import android.widget.FrameLayout
import com.visioglobe.visiomoveessential.VMEMapController
import com.visioglobe.visiomoveessential.VMEMapControllerBuilder
import com.visioglobe.visiomoveessential.VMEMapView
import com.visioglobe.visiomoveessential.listeners.VMELifeCycleListener

/**
 * Container view that hosts [VMEMapView] and [VMEMapController] for VisioMove Essential.
 * Configure via [setMapHash] and [setMapSecretCode] from the React Native bridge.
 */
class VisioMoveView(context: Context) : FrameLayout(context) {

    private val mapView: VMEMapView = VMEMapView(context, null).apply {
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
    }

    private var mapController: VMEMapController? = null
    private var pendingMapHash: String? = null
    private var pendingMapSecretCode: Int = 0

    private val lifeCycleListener = object : VMELifeCycleListener() {}

    init {
        addView(mapView)
    }

    /**
     * Set the online map hash (from Visioglobe map server).
     * Call this and [setMapSecretCode] before the view is attached; loading starts when both are set.
     */
    fun setMapHash(mapHash: String?) {
        if (pendingMapHash == mapHash) return
        pendingMapHash = mapHash
        tryLoadMap()
    }

    /**
     * Set the map secret code (default 0).
     */
    fun setMapSecretCode(code: Int) {
        if (pendingMapSecretCode == code) return
        pendingMapSecretCode = code
        tryLoadMap()
    }

    private fun tryLoadMap() {
        val hash = pendingMapHash ?: return
        if (mapController != null) return

        val builder = VMEMapControllerBuilder().apply {
            this.mapHash = hash
            mapSecretCode = pendingMapSecretCode
        }
        mapController = VMEMapController(context, builder).also { controller ->
            controller.setLifeCycleListener(lifeCycleListener)
            controller.loadMapData()
            controller.loadMapView(mapView)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mapController = null
    }
}
