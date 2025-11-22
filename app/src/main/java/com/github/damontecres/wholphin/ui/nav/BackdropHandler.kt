package com.github.damontecres.wholphin.ui.nav

import androidx.compose.runtime.staticCompositionLocalOf

val LocalBackdropHandler = staticCompositionLocalOf<(String?) -> Unit> { { } }

