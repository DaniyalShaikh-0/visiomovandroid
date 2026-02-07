package com.visiomovandroid

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

class VisioMoveViewManager(
    private val reactContext: ReactApplicationContext
) : SimpleViewManager<VisioMoveView>() {

    override fun getName(): String = REACT_CLASS

    override fun createViewInstance(themeContext: ThemedReactContext): VisioMoveView {
        return VisioMoveView(themeContext)
    }

    @ReactProp(name = "mapHash")
    fun setMapHash(view: VisioMoveView, mapHash: String?) {
        view.setMapHash(mapHash)
    }

    @ReactProp(name = "mapSecretCode")
    fun setMapSecretCode(view: VisioMoveView, code: Int?) {
        view.setMapSecretCode(code ?: 0)
    }

    companion object {
        const val REACT_CLASS = "VisioMoveView"
    }
}
