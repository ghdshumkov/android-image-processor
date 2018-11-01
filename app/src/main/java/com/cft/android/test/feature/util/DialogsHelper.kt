package com.cft.android.test.feature.util

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import com.cft.android.test.R


/**
 * Created by administrator <dshumkov@icerockdev.com> on 28.10.18.
 */
class InputValueDialogFragment : AppCompatDialogFragment() {

    private var mInputValueDialogListener: InputValueDialogListener? = null

    interface InputValueDialogListener {
        fun onAcceptValueClick(value: String)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is InputValueDialogListener) {
            mInputValueDialogListener = context
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context!!)

        val et = EditText(context)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        et.layoutParams = lp
        et.maxLines = 1
        et.setHint(R.string.label_input_url_hint)
        et.requestFocus()

        builder.setTitle(arguments!!.getString("title"))
                .setMessage(arguments!!.getString("message"))
                .setView(et)
                .setPositiveButton(android.R.string.ok, { _, _ ->
                    mInputValueDialogListener?.onAcceptValueClick(et.text.toString())
                    val input = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    input.hideSoftInputFromWindow(et.windowToken, 0)
                })

                // Hardcoded url for fast testing download feature
                .setNeutralButton("Use predefined url", { _, _ ->
                    mInputValueDialogListener?.onAcceptValueClick("https://images.pexels.com/photos/1533057/pexels-photo-1533057.jpeg?cs=tinysrgb&dpr=2&h=3333&w=5000")
                    val input = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    input.hideSoftInputFromWindow(et.windowToken, 0)
                })

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)

        // Show soft keyboard automatically for best UX
        dialog.setOnShowListener {
            val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }

        return dialog
    }

    companion object {

        fun getInstance(title: String, message: String, value: String): InputValueDialogFragment {
            val args = Bundle()
            args.putString("title", title)
            args.putString("message", message)
            args.putString("inputValue", value)

            val editValueDialog = InputValueDialogFragment()
            editValueDialog.arguments = args
            return editValueDialog
        }
    }
}

class MenuDialogFragment : AppCompatDialogFragment() {

    private var mMenuDialogListener: MenuDialogListener? = null

    interface MenuDialogListener {
        fun onItemSelected(fragmentTag: String?, pos: Int)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is MenuDialogListener) {
            mMenuDialogListener = context
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context!!)

        builder.setTitle(arguments!!.getString("title"))
                .setItems(arguments!!.getStringArray("items"), {_, pos ->
                    mMenuDialogListener?.onItemSelected(tag, pos)
                })
        return builder.create()
    }

    companion object {

        fun getInstance(title: String, items: Array<String>): MenuDialogFragment {
            val args = Bundle()
            args.putString("title", title)
            args.putStringArray("items", items)

            val menuDialog = MenuDialogFragment()
            menuDialog.arguments = args
            return menuDialog
        }
    }
}