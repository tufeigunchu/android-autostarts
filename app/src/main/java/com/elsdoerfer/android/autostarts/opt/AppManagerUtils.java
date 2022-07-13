package com.elsdoerfer.android.autostarts.opt;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Nullable;

public class AppManagerUtils {
    public static final String AM_DOWNLOAD_URL = "https://github.com/MuntashirAkon/AppManager/releases/latest";

    private static final String AM_DEBUG = "io.github.muntashirakon.AppManager.debug";
    private static final String AM_RELEASE = "io.github.muntashirakon.AppManager";

    private static final String AM_DETAILS_COMPONENT_NAME = "io.github.muntashirakon.AppManager.details.AppDetailsActivity";

    @Nullable
    public static Intent getAppDetailsIntent(@NonNull Context context, @NonNull String packageName) {
        int enabledSetting = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        String availablePackage = null;
        try {
            enabledSetting = context.getPackageManager().getApplicationEnabledSetting(AM_DEBUG);
            availablePackage = AM_DEBUG;
        } catch (IllegalArgumentException e) {
            // Not exists
            try {
                enabledSetting = context.getPackageManager().getApplicationEnabledSetting(AM_RELEASE);
                availablePackage = AM_RELEASE;
            } catch (IllegalArgumentException ignore) {
            }
        }
        if (availablePackage == null || enabledSetting >= PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            return null;
        }
        return new Intent(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_DEFAULT)
                .setClassName(availablePackage, AM_DETAILS_COMPONENT_NAME)
                .putExtra("pkg", packageName);
    }
}
