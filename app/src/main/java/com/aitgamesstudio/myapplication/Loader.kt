package com.aitgamesstudio.myapplication

import android.content.Context
import androidx.annotation.Nullable
import androidx.loader.content.AsyncTaskLoader


class BookLoader internal constructor(
    context: Context?
) :
    AsyncTaskLoader<String?>(context!!) {
    protected override fun onStartLoading() {
        super.onStartLoading()
        forceLoad()
    }

    @Nullable
    override fun loadInBackground(): String? {
        for (i in 0 until 2) {
            Thread.sleep(1000)
        }
        return NetworkUtils.getInfo()
    }

}