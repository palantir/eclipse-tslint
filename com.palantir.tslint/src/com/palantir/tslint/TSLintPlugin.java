
package com.palantir.tslint;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class TSLintPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "CheckTypeScriptStyle";

    // The shared instance
    private static TSLintPlugin plugin;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static String getLinterPath() throws IOException {
        File bundleFile = FileLocator.getBundleFile(TSLintPlugin.getDefault().getBundle());
        File tslintFile = new File(bundleFile, "../node_modules/tslint/bin/tslint");

        return tslintFile.getAbsolutePath();
    }

    public static TSLintPlugin getDefault() {
        return plugin;
    }

}
