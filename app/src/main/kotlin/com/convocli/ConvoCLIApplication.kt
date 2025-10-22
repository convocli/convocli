package com.convocli

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * ConvoCLI Application class.
 *
 * Initializes Hilt dependency injection for the entire application.
 */
@HiltAndroidApp
class ConvoCLIApplication : Application()
