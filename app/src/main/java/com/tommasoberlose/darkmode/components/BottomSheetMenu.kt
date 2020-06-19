package com.tommasoberlose.darkmode.components

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tommasoberlose.darkmode.R
import kotlinx.android.synthetic.main.bottom_sheet_menu.view.*
import kotlinx.android.synthetic.main.bottom_sheet_menu_item.view.*


open class BottomSheetMenu<T>(context: Context, private val header: String? = null, private val message: String? = null, private val isMessageWarning: Boolean = false, private val isMultiSelection: Boolean = false) : BottomSheetDialog(context, R.style.BottomSheetDialogTheme) {

    private val items: ArrayList<MenuItem<T>> = ArrayList()
    private var selectedRes: ArrayList<T> = ArrayList()
    private var callback: ((selectedValue: T) -> Unit)? = null
    private var multipleSelectionCallback: ((selectedValues: ArrayList<T>) -> Unit)? = null

    fun setSelectedValue(res: T): BottomSheetMenu<T> {
        selectedRes = ArrayList(listOf(res))
        return this
    }

    fun setSelectedValues(res: List<T>): BottomSheetMenu<T> {
        selectedRes = ArrayList(res)
        return this
    }

    fun addItem(title: String, value: T? = null): BottomSheetMenu<T> {
        items.add(MenuItem(title, value))
        return this
    }

    fun addOnSelectItemListener(callback: (selectedValue: T) -> Unit): BottomSheetMenu<T> {
        this.callback = callback
        return this
    }

    fun addOnMultipleSelectItemListener(multipleSelectionCallback: (selectedValues: ArrayList<T>) -> Unit): BottomSheetMenu<T> {
        this.multipleSelectionCallback = multipleSelectionCallback
        return this
    }

    override fun show() {
        val view = View.inflate(context, R.layout.bottom_sheet_menu, null)

        // Header
        view.header.isVisible = header != null
        view.header_text.text = header ?: ""

        view.warning_text.isVisible = message != null
        view.warning_text.text = message ?: ""
        view.warning_text.setTextColor(ContextCompat.getColor(context, if (isMessageWarning) R.color.warningColorText else R.color.colorSecondaryText))

        // Menu
        for (item in items) {
            if (item.value != null) {
                val itemView = View.inflate(context, R.layout.bottom_sheet_menu_item, null)
                itemView.label.text = item.title
                if (isMultiSelection) {
                    itemView.icon_check.isVisible = selectedRes.contains(item.value)
                    itemView.label.setTextColor(
                        if (selectedRes.contains(item.value)) ContextCompat.getColor(
                            context,
                            R.color.colorPrimaryText
                        ) else ContextCompat.getColor(context, R.color.colorSecondaryText)
                    )
                } else {
                    itemView.isSelected = selectedRes.contains(item.value)
                }
                itemView.setOnClickListener {
                    if (!isMultiSelection) {
                        callback?.invoke(item.value)
                        this.dismiss()
                    } else {
                        if (selectedRes.contains(item.value)) {
                            selectedRes.remove(item.value)
                        } else {
                            selectedRes.add(item.value)
                        }

                        multipleSelectionCallback?.invoke(selectedRes)
                        itemView.icon_check.isVisible = selectedRes.contains(item.value)
                        itemView.label.setTextColor(
                            if (selectedRes.contains(item.value)) ContextCompat.getColor(
                                context,
                                R.color.colorPrimaryText
                            ) else ContextCompat.getColor(context, R.color.colorSecondaryText)
                        )
                    }
                }
                view.menu.addView(itemView)
            } else {
                val itemView = View.inflate(context, R.layout.bottom_sheet_menu_divider, null)
                itemView.label.text = item.title
                view.menu.addView(itemView)
            }
        }
        setContentView(view)
        super.show()
    }

    class MenuItem<T>(val title: String, val value: T? = null)

}