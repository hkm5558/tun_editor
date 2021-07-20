package com.tuntech.tun_editor.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import com.tuntech.tun_editor.R
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView

internal class TunEditorToolbarView(
    context: Context,
    id: Int,
    creationParams: Map<String?, Any?>?,
    messenger: BinaryMessenger
) : PlatformView, MethodChannel.MethodCallHandler {

    // UI related.
    private val toolbar: View = LayoutInflater.from(context).inflate(
        R.layout.editor_toolbar, null)
    private lateinit var btnUndo: Button
    private lateinit var btnRedo: Button
    private lateinit var btnSetBold: Button

    private val methodChannel = MethodChannel(messenger, "tun/editor/toolbar/${id}")

    init {
        methodChannel.setMethodCallHandler(this)

        btnUndo = toolbar.findViewById<Button>(R.id.action_undo)
        btnRedo = toolbar.findViewById<Button>(R.id.action_redo)
        btnSetBold = toolbar.findViewById<Button>(R.id.action_bold)
        btnUndo.setOnClickListener {
            methodChannel.invokeMethod("undo", null)
        }
        btnRedo.setOnClickListener {
            methodChannel.invokeMethod("redo", null)
        }
        btnSetBold.setOnClickListener {
            methodChannel.invokeMethod("setBold", null)
        }
    }

    override fun getView(): View {
        return toolbar
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
        }
    }

    override fun dispose() {
    }

}
