package com.brainwallet.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.brainwallet.R

val BoldenVan = FontFamily(
    Font(R.font.bolden_van, FontWeight.Normal)
)

val IBMPlexSans = FontFamily(
    Font(R.font.ibm_plex_sans_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_sans_medium, FontWeight.Medium),
    Font(R.font.ibm_plex_sans_semi_bold, FontWeight.SemiBold),
    Font(R.font.ibm_plex_sans_bold, FontWeight.Bold),
    Font(R.font.ibm_plex_sans_extra_light, FontWeight.ExtraLight),
    Font(R.font.ibm_plex_sans_light, FontWeight.Light),
    Font(R.font.ibm_plex_sans_thin, FontWeight.Thin),
    Font(R.font.ibm_plex_sans_italic, FontWeight.Normal, FontStyle.Italic)
)
