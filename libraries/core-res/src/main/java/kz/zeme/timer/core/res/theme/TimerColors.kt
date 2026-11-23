package kz.zeme.timer.core.res.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class TimerColors(
    val basicDark: Color,
    val basicGray: Color,
    val basicError: Color,
    val basicSuccess: Color,
    val inputBg: Color,
    val inputErrorBg: Color,
    val mainBg: Color,
    val mainShimmer: Color,
    val buttonPrimary: Color,
    val buttonSecondary: Color,
    val buttonDisabled: Color,
    val buttonDarkGrayed: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val gray8: Color,
)

internal val Basic_Gray = Color(color = 0xFF898989)
internal val Basic_Dark = Color(color = 0xFF16171B)
internal val Input_Bg = Color(color = 0xFF2D2D30)
internal val Input_Error_Bg = Color(color = 0xFF3F2B2F)
internal val Main_Bg = Color(color = 0xFF010101)
internal val Basic_Error = Color(color = 0xFFDC1B27)
internal val Basic_Success = Color(color = 0xFF00D394)
internal val Button_Primary = Color(color = 0xFF2C2C2C)
internal val Button_Secondary = Color(color = 0xFF2B2B2E)
internal val Button_Disabled = Color(color = 0xFF202024)
internal val Button_Dark_Grayed = Color(0xE58B8787)
internal val Main_Shimmer = Color(color = 0xFF121316)
internal val Text_Primary = Color.White
internal val Text_Secondary = Basic_Gray
internal val Gray_8 = Color(color = 0xFF48494A)

internal val Light_Basic_Gray = Color(color = 0xFF75757A)
internal val Light_Basic_Dark = Color(color = 0xFFFFFFFF)
internal val Light_Input_Bg = Color(color = 0xFFEBEBEF)
internal val Light_Input_Error_Bg = Color(color = 0xFFFDECEE)
internal val Light_Main_Bg = Color(color = 0xFFF6F6F8)
internal val Light_Basic_Success = Color(color = 0xFF00B880)
internal val Light_Button_Primary = Color(color = 0xFFE4E4E9)
internal val Light_Button_Secondary = Color(color = 0xFFECECF0)
internal val Light_Button_Disabled = Color(color = 0xFFEFEFF3)
internal val Light_Button_Dark_Grayed = Color(0xE575757A)
internal val Light_Main_Shimmer = Color(color = 0xFFE6E7EC)
internal val Light_Text_Primary = Color(color = 0xFF111216)
internal val Light_Text_Secondary = Light_Basic_Gray
internal val Light_Gray_8 = Color(color = 0xFFD0D1D8)

val DarkTimerColors = TimerColors(
    basicDark = Basic_Dark,
    basicGray = Basic_Gray,
    basicError = Basic_Error,
    basicSuccess = Basic_Success,
    inputBg = Input_Bg,
    inputErrorBg = Input_Error_Bg,
    mainBg = Main_Bg,
    mainShimmer = Main_Shimmer,
    buttonPrimary = Button_Primary,
    buttonSecondary = Button_Secondary,
    buttonDarkGrayed = Button_Dark_Grayed,
    buttonDisabled = Button_Disabled,
    textPrimary = Text_Primary,
    textSecondary = Text_Secondary,
    gray8 = Gray_8,
)

val LightTimerColors = TimerColors(
    basicDark = Light_Basic_Dark,
    basicGray = Light_Basic_Gray,
    basicError = Basic_Error,
    basicSuccess = Light_Basic_Success,
    inputBg = Light_Input_Bg,
    inputErrorBg = Light_Input_Error_Bg,
    mainBg = Light_Main_Bg,
    mainShimmer = Light_Main_Shimmer,
    buttonPrimary = Light_Button_Primary,
    buttonSecondary = Light_Button_Secondary,
    buttonDarkGrayed = Light_Button_Dark_Grayed,
    buttonDisabled = Light_Button_Disabled,
    textPrimary = Light_Text_Primary,
    textSecondary = Light_Text_Secondary,
    gray8 = Light_Gray_8,
)

val LocalStaticColors = staticCompositionLocalOf { DarkTimerColors }

@Composable
fun Color.enabled(enabled: Boolean): Color = copy(
    alpha = ContentAlpha.getAlpha(enabled),
)

@Suppress("ConstPropertyName")
object ContentAlpha {
    const val disabled: Float = 0.38f

    fun getAlpha(
        enabled: Boolean,
        disabled: Float = ContentAlpha.disabled,
    ): Float = if (enabled) 1f else disabled
}
