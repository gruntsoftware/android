package com.brainwallet.presenter.entities

import android.view.View

class BRMenuItem(
    @JvmField var text: String?,
    @JvmField var resId: Int,
    @JvmField var listener: View.OnClickListener?
)
