package ru.radiationx.lexxie.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.lexxie.R
import ru.radiationx.lexxie.databinding.ViewShadowDescriptionBinding

class ShadowDescriptionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding by viewBinding<ViewShadowDescriptionBinding>(attachToRoot = true)

    init {
        setBackgroundResource(R.drawable.bg_grid_description_shadow)
    }

    fun setDescription(title: CharSequence, subtitle: CharSequence) {
        binding.cardDescriptionView.apply {
            setTitle(title)
            setSubtitle(subtitle)
        }
    }

    fun getCardDescriptionView(): CardDescriptionView = binding.cardDescriptionView
}