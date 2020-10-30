package com.dubhe.imageselector

import android.annotation.SuppressLint
import android.content.Context
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

/**
 * 选项适配器
 */
class OptionAdapter(var context: Context, data: MutableList<ImgTextOptionModel>) :
    BaseQuickAdapter<ImgTextOptionModel, BaseViewHolder>(R.layout.item_img_text_dialog, data) {

    @SuppressLint("SimpleDateFormat")
    override fun convert(helper: BaseViewHolder, bean: ImgTextOptionModel) {
        helper.setText(R.id.text, bean.text)
        helper.setImageResource(R.id.img, bean.imgId)
    }

}
