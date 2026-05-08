package com.parallelc.mixflipmod.hook

import android.content.Intent
import android.os.Bundle
import android.os.UserHandle
import android.view.View
import com.parallelc.mixflipmod.Prefs
import com.parallelc.mixflipmod.hook.util.findClass
import com.parallelc.mixflipmod.hook.util.hook
import com.parallelc.mixflipmod.hook.util.method
import com.parallelc.mixflipmod.hook.util.prefInt
import com.parallelc.mixflipmod.hook.util.replaceResult
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

object FlipHomeHook : BaseHook() {
    override val targetPackages = listOf("com.miui.fliphome")

    override fun setupHooks(prefKey: String, param: PackageReadyParam) {
        when (prefKey) {
            Prefs.FLIPHOME_NO_START_PAGE -> hookNoStartPage(param)
            Prefs.FLIPHOME_RECENTS_STYLE -> hookRecentsStyle(param)
        }
    }

    private fun hookNoStartPage(param: PackageReadyParam) {
        val cls = param.classLoader.findClass("com.miui.fliphome.utils.PerformLaunchAction")
        hook(
            cls.method("onStartIntercept", UserHandle::class.java, Intent::class.java, Bundle::class.java, View::class.java),
            replaceResult(false)
        )
    }

    private fun hookRecentsStyle(param: PackageReadyParam) {
        val style = prefInt(Prefs.FLIPHOME_RECENTS_STYLE, Prefs.RECENTS_STYLE_DEFAULT)
        if (style == Prefs.RECENTS_STYLE_DEFAULT) return

        // 外屏桌面的样式常量映射（和主屏不同，外屏只有横向和纵向）
        // styleValue == 1 是横向 (TaskStackViewLayoutStyleHorizontal)
        // 其他值是纵向 (TaskStackViewLayoutStyleVertical)
        val flipHomeStyleValue = when (style) {
            Prefs.RECENTS_STYLE_HORIZONTAL -> 1
            Prefs.RECENTS_STYLE_VERTICAL -> 0
            else -> return
        }

        // 强行拦截 TaskStackViewLayoutStyle.create(int, Context)
        val cls = param.classLoader.findClass("com.miui.fliphome.recents.TaskStackViewLayoutStyle")
        hook(cls.method("create", Int::class.java, android.content.Context::class.java)) { chain ->
            val context = chain.args[1] as android.content.Context
            if (flipHomeStyleValue == 1) {
                val horizontalCls = param.classLoader.findClass("com.miui.fliphome.recents.TaskStackViewLayoutStyleHorizontal")
                horizontalCls.getConstructor(android.content.Context::class.java).newInstance(context)
            } else {
                val verticalCls = param.classLoader.findClass("com.miui.fliphome.recents.TaskStackViewLayoutStyleVertical")
                verticalCls.getConstructor(android.content.Context::class.java).newInstance(context)
            }
        }
    }
}
