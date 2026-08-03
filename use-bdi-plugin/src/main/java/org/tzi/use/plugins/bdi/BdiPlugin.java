package org.tzi.use.plugins.bdi;

import org.tzi.use.runtime.IPluginRuntime;
import org.tzi.use.runtime.impl.Plugin;
import org.tzi.use.util.Log;

public final class BdiPlugin extends Plugin {
    public static final String NAME = "USE BDI Plugin";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void doRun(IPluginRuntime pluginRuntime) {
        Log.verbose(NAME + " initialized.");
    }
}
