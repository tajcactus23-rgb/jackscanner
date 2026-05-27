package com.jackscanner.ui.theme

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.sp

// Common type extensions
val Int.sp: TextUnit
    get() = TextUnit(this.toFloat(), TextUnitType.Sp)

val Float.sp: TextUnit
    get() = TextUnit(this, TextUnitType.Sp)

val Int.dp: androidx.compose.ui.unit.Dp
    get() = androidx.compose.ui.unit.Dp(this.toFloat())

val Float.dp: androidx.compose.ui.unit.Dp
    get() = androidx.compose.ui.unit.Dp(this)