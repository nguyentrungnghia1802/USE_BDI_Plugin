package org.tzi.use.plugins.bdi;

import javax.swing.JOptionPane;

import org.tzi.use.main.Session;
import org.tzi.use.runtime.gui.IPluginAction;
import org.tzi.use.runtime.gui.IPluginActionDelegate;

public final class HelloBdiAction implements IPluginActionDelegate {
    @Override
    public void performAction(IPluginAction pluginAction) {
        JOptionPane.showMessageDialog(
                pluginAction.getParent(),
                createMessage(pluginAction.getSession()),
                BdiPlugin.NAME,
                JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public boolean shouldBeEnabled(IPluginAction pluginAction) {
        return true;
    }

    static String createMessage(Session session) {
        if (session == null || !session.hasSystem()) {
            return "Hello from USE BDI Plugin.\nNo UML/OCL model is currently loaded.";
        }

        return "Hello from USE BDI Plugin.\nCurrent UML/OCL model: "
                + session.system().model().name();
    }
}
