package com.parallelc.mixflipmod

object Prefs {
    const val NAME = "mixflipmod"

    // android (system framework)
    const val SYSTEM_COMPAT_CONFIG     = "system_compat_config"
    const val SYSTEM_FLIP_CONTINUITY   = "system_flip_continuity"
    const val SYSTEM_FLIP_SCREEN_MODE  = "system_flip_screen_mode"
    const val SYSTEM_FLIP_IME_PKG      = "system_flip_ime_pkg"

    // com.android.systemui
    const val SYSUI_NOTIFICATION       = "sysui_notification"
    const val SYSUI_CONTROL_CENTER     = "sysui_control_center"
    const val SYSUI_STATUS_BAR_ICON    = "sysui_status_bar_icon"
    const val SYSUI_STATUS_BAR_ICON_MAX = "sysui_status_bar_icon_max"
    const val SYSUI_STATUS_BAR_CLOCK   = "sysui_status_bar_clock"

    // com.miui.fliphome
    const val FLIPHOME_NO_START_PAGE   = "fliphome_no_start_page"
    const val FLIPHOME_RECENTS_STYLE   = "fliphome_recents_style"

    // Recents style values
    const val RECENTS_STYLE_DEFAULT    = -1  // 跟随系统
    const val RECENTS_STYLE_HORIZONTAL = 0   // 横向
    const val RECENTS_STYLE_VERTICAL   = 1   // 纵向

    // Per-app hide outer & dpi config
    const val HIDE_OUTER_PREFIX = "hide_outer_"
    const val HIDE_OUTER_DPI = 340
    fun hideOuterKey(pkg: String) = "$HIDE_OUTER_PREFIX$pkg"

    const val FLIP_SCREEN_MODE_DEFAULT = -1
    const val FLIP_SCREEN_MODE_NO_SCALE = 1
    const val FLIP_SCREEN_MODE_SCALE = 2
    private const val FLIP_SCREEN_MODE_PREFIX = "flip_screen_mode_"
    fun flipScreenModeKey(pkg: String) = "$FLIP_SCREEN_MODE_PREFIX$pkg"

    const val DEFAULT_FLIP_IME_PKG = "com.sohu.inputmethod.sogou.xiaomi"
}
