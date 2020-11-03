package com.elsdoerfer.android.autostarts;

import android.Manifest.permission;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.elsdoerfer.android.autostarts.db.ComponentInfo;

import androidx.annotation.NonNull;

/**
 * How we toggle a component's states. Beware: This is a long comment.
 * <p>
 * Android has setComponentEnabledState(), but to use it on other apps
 * requires the CHANGE_COMPONENT_ENABLED_STATE permission, which is
 * signature-protected. Rarely will we be signed with the system
 * certificate, but for what it's worth, if we do have the permission,
 * we'll use it and make the call directly.
 * <p>
 * Normally we don't, and that means we need to use root to make a su
 * call to the "pm" command. "pm" is included in Android and is a command
 * line interface to the PackageManager service. Called as root, the
 * PackageManager will skip the permissions check.
 * <p>
 * Ideally, here's where the story would end. Unfortunately, there seems
 * to be a bug *somewhere* (in Dalvik? In the Superuser Whitelist app?)
 * that causes Process.waitFor() to hang if we run any process that involves
 * the "app_process" executable while "USB Debugging" is disabled.
 * "app_process" is a tool included in Android that will spawn a Dalvik VM
 * and execute Java code (i.e., the actual pm Command is a wrapper script
 * around app_process, the actual command is implemented as a Java class).
 * <p>
 * Note that the guilty party indeed is "app_process", not the "pm" command
 * in particular. Other commands like "ime" exhibit the same behavior.
 * <p>
 * The fact that the problem only occurs when ADB is disabled makes this
 * particularly hard to debug.
 * <p>
 * I've tried all kinds of things to find a solution, running "app_process"
 * directly, replacing Java's "Process" with a custom native library
 * calling "system" to run the command, but to no avail. Further options
 * begin to run thin, but here's some things we could still try:
 * <p>
 * a) Try executing "app_process" directly, but through the native
 * library. This is very unlikely to make a difference though.
 * <p>
 * b) Try to track down the problem and fix it. For starters, is it really
 * Superuser Whitelist, or does the standard su also have the same
 * problem. In the latter case, we might even get Google to fix it.
 * <p>
 * c) Try to write a tool that interacts with the PackageManager service in
 * C, bypassing "app_process". For reference, have a look at
 * 'frameworks/base.git/servicemanager/binder.c.' Note also the
 * /dev/binder device.
 * <p>
 * As a sidenote, a custom replacement for "pm" would also allow us to change
 * the component state without requirement the restart flag, which would
 * presumably be a lot faster.
 * <p>
 * In the meantime, we need to essentially require the user to have USB
 * Debugging enabled. There are however a few things we can do to help:
 * <p>
 * - Again in the rare case that we are installed on the system
 * partition, we can enable ADB (and re-disable it) automatically,
 * because we have the right to write to the secure settings area.
 * <p>
 * - If we don't have WRITE_SECURE_SETTINGS, we can use a "su setprop"
 * call to change the ADB Debugging setting.
 * <p>
 * - We make it clear to the user that ADB needs to be enabled for
 * optional functioning via a bar on the top (this is currently not
 * done, since auto-enabling ADB works well enough).
 * <p>
 * - We use a timeout when running in action, so that when we can see
 * that the state has changed, we simply stop waiting for the process
 * to finish. This at least improves the user experience.
 *
 * Takes care of toggling a component's state. This may take a
 * couple of seconds, so we use a thread.
 */
class ToggleTool {
    @NonNull
    static protected Boolean toggleState(@NonNull Context context, ComponentInfo component, boolean doEnable) {
        Log.i(Utils.TAG, "Asking package manger to " + "change component state to "
				+ (doEnable ? "enabled" : "disabled"));

        // As described above, in the rare case we are allowed to use
        // setComponentEnabledSetting(), we should do so.
        if (context.checkCallingOrSelfPermission(permission.CHANGE_COMPONENT_ENABLED_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(Utils.TAG, "Calling setComponentEnabledState() directly");
            PackageManager pm = context.getPackageManager();
            ComponentName c = new ComponentName(component.packageInfo.packageName, component.componentName);
            pm.setComponentEnabledSetting(
                    c, doEnable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                            : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
            component.currentEnabledState = pm.getComponentEnabledSetting(c);
            return (component.isCurrentlyEnabled() == doEnable);
        } else {
            Log.i(Utils.TAG, "Changing state by employing root access");
            // Run the command; we have different invocations we can try, but
            // we'll stop at the first one we succeed with.
            boolean success = false;
            if (Utils.runRootCommand(String.format("pm %s '%s/%s'", (doEnable ? "enable" : "disable"),
                    component.packageInfo.packageName, component.componentName)
            )) {
                success = true;
            }
            // We are happy if both the command itself succeed (return code)...
            if (!success) return false;

            // ...and the state should now actually be what we expect.
            // TODO: It would be more stable if we would reload
            // getComponentEnabledSetting() regardless of the return code.
            final PackageManager pm = context.getPackageManager();
            ComponentName c = new ComponentName(
                    component.packageInfo.packageName, component.componentName);
            component.currentEnabledState = pm.getComponentEnabledSetting(c);

            success = component.isCurrentlyEnabled() == doEnable;
            if (success)
                Log.i(Utils.TAG, "State successfully changed");
            else
                Log.i(Utils.TAG, "State change failed");
            return success;
        }
    }
}
