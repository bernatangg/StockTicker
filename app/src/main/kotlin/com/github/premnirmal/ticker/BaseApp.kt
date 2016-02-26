package com.github.premnirmal.ticker

import android.support.multidex.MultiDexApplication
import com.github.premnirmal.tickerwidget.R
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

/**
 * Created by premnirmal on 2/26/16.
 */
abstract class BaseApp : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder().setDefaultFontPath("fonts/Ubuntu-Regular.ttf").setFontAttrId(R.attr.fontPath).build())
    }

    companion object {
        var instance: BaseApp? = null
            private set
    }
}