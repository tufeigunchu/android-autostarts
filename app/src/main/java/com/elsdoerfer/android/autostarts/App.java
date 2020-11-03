package com.elsdoerfer.android.autostarts;

import android.app.Application;

import com.topjohnwu.superuser.Shell;

public class App extends Application {
    static {
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
        Shell.setDefaultBuilder(Shell.Builder.create()
                .setFlags(Shell.FLAG_MOUNT_MASTER)
                .setTimeout(10));
    }
}
