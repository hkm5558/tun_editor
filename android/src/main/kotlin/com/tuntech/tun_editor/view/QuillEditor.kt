package com.tuntech.tun_editor.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONArray


@SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
class QuillEditor: WebView {

    companion object {
        val TAG: String = QuillEditor::class.java.name

        const val URL = "file:///android_asset/index.html"
    }

    constructor(context: Context): super(context)

    constructor(context: Context, attr: AttributeSet): super(context, attr)

    constructor(context: Context, attr: AttributeSet, defStyle: Int): super (context, attr, defStyle)

    constructor(context: Context, placeholder: String, padding: List<Int>, readOnly: Boolean,
                autoFocus: Boolean, delta: List<*>): this(context) {
        this.placeholder = placeholder
        this.readOnly = readOnly
        this.autoFocus = autoFocus
        this.padding = padding
        this.delta = delta
        Log.d(TAG, "delta: $delta")
    }

    private var placeholder: String = ""
    private var readOnly: Boolean = false
    private var autoFocus: Boolean = false
    private var padding: List<Int> = listOf(12, 15, 12, 15)
    private var delta: List<*> = listOf<Map<String, Any>>()

    private var onTextChangeListener: ((String, String) -> Unit)? = null
    private var onSelectionChangeListener: ((Int, Int, String) -> Unit)? = null
    private var onMentionClickListener: ((String, String) -> Unit)? = null
    private var onLinkClickListener: ((String) -> Unit)? = null
    private var onFocusChangeListener: ((Boolean) -> Unit)? = null

    init {
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        settings.javaScriptEnabled = true

        webChromeClient = WebChromeClient()
        webViewClient = QuillEditorWebClient(
            onPageFinished = {
                setPlaceholder(placeholder)
                setPadding(padding)
                setReadOnly(readOnly)
                setContents(delta)

                if (autoFocus) {
                    focus()
                } else {
                    blur()
                }
            }
        )

        addJavascriptInterface(JSInterface(
            onTextChangeListener = { delta, oldDelta ->
                (context as Activity).runOnUiThread {
                    onTextChangeListener?.invoke(delta, oldDelta)
                }
            },
            onSelectionChangeListener = { index, length, format ->
                (context as Activity).runOnUiThread {
                    onSelectionChangeListener?.invoke(index, length, format)
                }
            },
            onMentionClickListener = { id, text ->
                (context as Activity).runOnUiThread {
                    onMentionClickListener?.invoke(id, text)
                }
            },
            onLinkClickListener = { url ->
                (context as Activity).runOnUiThread {
                    onLinkClickListener?.invoke(url)
                }
            },
            onFocusChangeListener = { hasFocus ->
                (context as Activity).runOnUiThread {
                    onFocusChangeListener?.invoke(hasFocus)
                }
            }
        ), "tun")

        loadUrl(URL)
    }

    fun replaceText(index: Int, length: Int, data: Any) {
        if (data is String) {
            exec("javascript:replaceText($index, $length, \"$data\")")
        } else {
            exec("javascript:replaceText($index, $length, $data)")
        }
    }

    fun insertMention(id: String, text: String) {
        exec("javascript:insertMention(\"$id\", \"$text\")")
    }

    fun insertDivider() {
        exec("javascript:insertDivider()")
    }

    fun insertImage(url: String) {
        exec("javascript:insertImage(\"$url\")")
    }

    fun insertLink(text: String, url: String) {
        exec("javascript:insertLink(\"$text\", \"$url\")")
    }

    fun format(name: String, value: Any) {
        if (value is String) {
            exec("javascript:format(\"$name\", \"$value\")")
        } else {
            exec("javascript:format(\"$name\", $value)")
        }
    }

    fun formatText(index: Int, length: Int, name: String, value: Any?) {
        if (value is String) {
            exec("javascript:formatText($index, $length, \"$name\", \"$value\")")
        } else {
            exec("javascript:formatText($index, $length, \"$name\", $value)")
        }
    }

    fun setSelection(index: Int, length: Int) {
        exec("javascript:setSelection($index, $length)")
    }

    fun focus() {
        requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(this, 0)
        exec("javascript:focus()");
    }

    fun blur() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(windowToken, 0)
        exec("javascript:blur()");
    }

    fun setOnTextChangeListener(onTextChangeListener: ((String, String) -> Unit)?) {
        this.onTextChangeListener = onTextChangeListener
    }

    fun setOnSelectionChangeListener(onSelectionChangeListener: ((Int, Int, String) -> Unit)?) {
        this.onSelectionChangeListener = onSelectionChangeListener
    }

    fun setOnMentionClickListener(onMentionClick: ((String, String) -> Unit)?) {
        this.onMentionClickListener = onMentionClick
    }

    fun setOnLinkClickListener(onLinkClick: ((String) -> Unit)?) {
        this.onLinkClickListener = onLinkClick
    }

    fun setOnFocusChangeListener(onFocusChangeListener: ((Boolean) -> Unit)?) {
        this.onFocusChangeListener = onFocusChangeListener
    }

    private fun setPlaceholder(placeholder: String) {
        exec("javascript:setPlaceholder(\"$placeholder\")")
    }

    private fun setPadding(padding: List<Int>) {
        if (padding.size < 4) {
            Log.w(TAG, "set editor padding failed: padding size is less then 4")
            return
        }
        exec("javascript:setPadding(${padding[0]}, ${padding[1]}, ${padding[2]}, ${padding[3]})")
    }

    private fun setReadOnly(readOnly: Boolean) {
        exec("javascript:setReadOnly($readOnly)")
    }

    private fun setContents(delta: List<*>) {
        exec("javascript:setContents(${JSONArray(delta)})")
    }

    private fun exec(command: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(command, null)
        } else {
            loadUrl(command)
        }
    }

    data class Selection(
        val index: Int,
        val length: Int
    )

    class JSInterface(
        private val onTextChangeListener: ((String, String) -> Unit),
        private val onSelectionChangeListener: (Int, Int, String) -> Unit,
        private val onMentionClickListener: ((String, String) -> Unit),
        private val onLinkClickListener: ((String) -> Unit),
        private val onFocusChangeListener: ((Boolean) -> Unit)
    ) {
        @JavascriptInterface
        fun onSelectionChange(index: Int, length: Int, format: String) {
            onSelectionChangeListener(index, length, format)
        }

        @JavascriptInterface
        fun onTextChange(delta: String, oldDelta: String, source: String) {
            onTextChangeListener(delta, oldDelta)
        }

        @JavascriptInterface
        fun onMentionClick(id: String, text: String) {
            onMentionClickListener(id, text)
        }

        @JavascriptInterface
        fun onLinkClick(url: String) {
            onLinkClickListener(url)
        }

        @JavascriptInterface
        fun onFocusChange(hasFocus: Boolean) {
            onFocusChangeListener.invoke(hasFocus)
        }
    }

    class QuillEditorWebClient(
        private val onPageFinished: () -> Unit
    ): WebViewClient() {

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

            if (url?.equals(URL, true) == true) {
                onPageFinished.invoke()
            }
        }

    }

}
