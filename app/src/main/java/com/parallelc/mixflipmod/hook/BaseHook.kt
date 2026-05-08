package com.parallelc.mixflipmod.hook

import com.parallelc.mixflipmod.Prefs
import com.parallelc.mixflipmod.hook.util.safeHook
import com.parallelc.mixflipmod.model.PrefSpec
import com.parallelc.mixflipmod.model.appConfigs
import com.parallelc.mixflipmod.module
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

abstract class BaseHook {
    abstract val targetPackages: List<String>

    open fun hook(param: PackageReadyParam) {
        val cfg = appConfigs.firstOrNull { it.packageName == param.packageName } ?: return
        val prefs = module!!.getRemotePreferences(Prefs.NAME)
        cfg.prefs.filterIsInstance<PrefSpec.Switch>().forEach { spec ->
            val enabled = prefs.getBoolean(spec.prefKey, false)
            if (enabled) {
                safeHook("[${param.packageName}] ${spec.prefKey}") { setupHooks(spec.prefKey, param) }
            }
        }
        cfg.prefs.filterIsInstance<PrefSpec.ListSelect>().forEach { spec ->
            val value = prefs.getInt(spec.prefKey, spec.defaultValue)
            if (value != spec.defaultValue) {
                safeHook("[${param.packageName}] ${spec.prefKey}") { setupHooks(spec.prefKey, param) }
            }
        }
    }

    protected open fun setupHooks(prefKey: String, param: PackageReadyParam) {}
}
