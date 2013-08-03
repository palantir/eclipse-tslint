
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
    public static final String PLUGIN_ID = "CheckTypeScriptStyle"; //$NON-NLS-1$

    // The shared instance
    private static TSLintPlugin plugin;

    /**
     * The constructor
     */
    public TSLintPlugin() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
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

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static TSLintPlugin getDefault() {
        return plugin;
    }

}
