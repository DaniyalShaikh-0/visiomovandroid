package com.visiomovandroid

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView

/**
 * Container view that hosts VisioMove Essential's map view and controller when the proprietary SDK
 * AAR is present in `android/app/libs/`.
 *
 * This class intentionally avoids compile-time references to the SDK so the project can build
 * (and CI can run) without bundling proprietary binaries.
 * Configure via [setMapHash] and [setMapSecretCode] from the React Native bridge.
 */
class VisioMoveView(context: Context) : FrameLayout(context) {

    private var pendingMapHash: String? = null
    private var pendingMapSecretCode: Int = 0
    private var sdkAvailable: Boolean = false
    private var mapView: android.view.View? = null
    private var mapController: Any? = null
    private var lifeCycleListener: Any? = null
    private var placeholderView: TextView? = null

    init {
        // Create the SDK view if available; otherwise show an informative placeholder.
        sdkAvailable = isSdkPresent()
        if (sdkAvailable) {
            mapView = tryCreateVmeMapView(context)
        }
        if (mapView != null) {
            addView(mapView)
        } else {
            sdkAvailable = false
            showMissingSdkPlaceholder()
        }
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
        if (!sdkAvailable) return
        val hash = pendingMapHash ?: return
        if (mapController != null) return
        val view = mapView ?: return

        try {
            val builderClass = Class.forName("com.visioglobe.visiomoveessential.VMEMapControllerBuilder")
            val builder = builderClass.getDeclaredConstructor().newInstance()

            setProperty(builder, "mapHash", hash)
            setIntProperty(builder, "mapSecretCode", pendingMapSecretCode)

            val controllerClass = Class.forName("com.visioglobe.visiomoveessential.VMEMapController")
            val controller = controllerClass.getConstructor(Context::class.java, builderClass).newInstance(context, builder)
            mapController = controller

            // Lifecycle listener is optional; if it can't be constructed, we continue without it.
            lifeCycleListener = tryCreateLifeCycleListener()
            lifeCycleListener?.let { listener ->
                invokeMethod(controller, "setLifeCycleListener", listener)
            }

            invokeMethod(controller, "loadMapData")
            invokeMethod(controller, "loadMapView", view)
        } catch (_: Throwable) {
            // If anything goes wrong (API mismatch, unexpected constructor), fall back to placeholder.
            sdkAvailable = false
            removeAllViews()
            mapController = null
            mapView = null
            showMissingSdkPlaceholder()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mapController = null
    }

    private fun showMissingSdkPlaceholder() {
        if (placeholderView != null) return
        placeholderView =
            TextView(context).apply {
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                gravity = Gravity.CENTER
                text =
                    "VisioMove Essential SDK missing.\n\n" +
                        "Add VisioMoveEssential.aar to:\n" +
                        "android/app/libs/\n\n" +
                        "Then rebuild the Android app."
            }
        addView(placeholderView)
    }

    private fun isSdkPresent(): Boolean {
        return try {
            Class.forName("com.visioglobe.visiomoveessential.VMEMapView")
            Class.forName("com.visioglobe.visiomoveessential.VMEMapController")
            Class.forName("com.visioglobe.visiomoveessential.VMEMapControllerBuilder")
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun tryCreateVmeMapView(context: Context): android.view.View? {
        return try {
            val clazz = Class.forName("com.visioglobe.visiomoveessential.VMEMapView")
            val ctor = clazz.getConstructor(Context::class.java, AttributeSet::class.java)
            val view = ctor.newInstance(context, null) as? android.view.View
            view?.layoutParams =
                LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT,
                )
            view
        } catch (_: Throwable) {
            null
        }
    }

    private fun tryCreateLifeCycleListener(): Any? {
        return try {
            val clazz = Class.forName("com.visioglobe.visiomoveessential.listeners.VMELifeCycleListener")
            clazz.getDeclaredConstructor().newInstance()
        } catch (_: Throwable) {
            null
        }
    }

    private fun setProperty(target: Any, name: String, value: Any) {
        // Try Kotlin/Java bean-style setter first.
        val setterName = "set" + name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        try {
            val method =
                target.javaClass.methods.firstOrNull { m ->
                    m.name == setterName &&
                        m.parameterTypes.size == 1 &&
                        m.parameterTypes[0].isAssignableFrom(value.javaClass)
                }
            if (method != null) {
                method.invoke(target, value)
                return
            }
        } catch (_: Throwable) {
            // fall through
        }

        // Try public field.
        try {
            val field = target.javaClass.getField(name)
            field.set(target, value)
        } catch (_: Throwable) {
            // ignore
        }
    }

    private fun setIntProperty(target: Any, name: String, value: Int) {
        val setterName = "set" + name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        try {
            val method =
                target.javaClass.methods.firstOrNull { m ->
                    m.name == setterName && m.parameterTypes.size == 1 &&
                        (m.parameterTypes[0] == Int::class.javaPrimitiveType || m.parameterTypes[0] == Int::class.javaObjectType)
                }
            if (method != null) {
                method.invoke(target, value)
                return
            }
        } catch (_: Throwable) {
            // fall through
        }

        try {
            val field = target.javaClass.getField(name)
            if (field.type == Int::class.javaPrimitiveType || field.type == Int::class.javaObjectType) {
                field.setInt(target, value)
            } else {
                field.set(target, value)
            }
        } catch (_: Throwable) {
            // ignore
        }
    }

    private fun invokeMethod(target: Any, name: String, vararg args: Any) {
        val argTypes = args.map { it.javaClass }.toTypedArray()
        try {
            // First try exact match.
            val method = target.javaClass.getMethod(name, *argTypes)
            method.invoke(target, *args)
            return
        } catch (_: Throwable) {
            // fall through
        }

        // Then try "best effort" match by name and parameter count.
        val method =
            target.javaClass.methods.firstOrNull { m ->
                m.name == name && m.parameterTypes.size == args.size
            } ?: return
        try {
            method.invoke(target, *args)
        } catch (_: Throwable) {
            // ignore
        }
    }
}
