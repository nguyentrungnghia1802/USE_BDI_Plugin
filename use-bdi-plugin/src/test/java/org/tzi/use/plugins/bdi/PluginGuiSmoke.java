package org.tzi.use.plugins.bdi;

import java.nio.file.Path;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.tzi.use.config.Options;
import org.tzi.use.gui.main.MainWindow;
import org.tzi.use.main.Session;
import org.tzi.use.main.runtime.IRuntime;
import org.tzi.use.runtime.MainPluginRuntime;

public final class PluginGuiSmoke {
    private PluginGuiSmoke() {
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Expected the extracted USE home directory.");
            System.exit(2);
        }

        MainWindow window = null;
        int exitCode = 0;
        try {
            Path useHome = Path.of(args[0]).toAbsolutePath();
            Options.processArgs(new String[] { "-nr", "-H=" + useHome });

            IRuntime runtime = MainPluginRuntime.run(useHome.resolve("lib").resolve("plugins"));
            window = MainWindow.create(new Session(), runtime);

            JMenu pluginsMenu = findMenu(window.getRootPane().getJMenuBar(), "Plugins");
            JMenu agentSpeakMenu = findSubmenu(pluginsMenu, "AgentSpeak");
            findItem(agentSpeakMenu, "Hello BDI Plugin");
            findItem(agentSpeakMenu, "Import AgentSpeak...");
            System.out.println("GUI_SMOKE_OK: Plugins > AgentSpeak > Hello BDI Plugin + Import AgentSpeak...");
        } catch (Throwable error) {
            error.printStackTrace(System.err);
            exitCode = 1;
        } finally {
            if (window != null) {
                window.setVisible(false);
                window.dispose();
            }
        }

        System.exit(exitCode);
    }

    private static JMenu findMenu(JMenuBar menuBar, String text) {
        for (int index = 0; index < menuBar.getMenuCount(); index++) {
            JMenu menu = menuBar.getMenu(index);
            if (menu != null && text.equals(menu.getText())) {
                return menu;
            }
        }
        throw new IllegalStateException("Menu not found: " + text);
    }

    private static JMenu findSubmenu(JMenu parent, String text) {
        JMenuItem item = findItem(parent, text);
        if (item instanceof JMenu) {
            return (JMenu) item;
        }
        throw new IllegalStateException("Expected submenu: " + text);
    }

    private static JMenuItem findItem(JMenu parent, String text) {
        for (int index = 0; index < parent.getItemCount(); index++) {
            JMenuItem item = parent.getItem(index);
            if (item != null && text.equals(item.getText())) {
                return item;
            }
        }
        throw new IllegalStateException("Menu item not found: " + text);
    }
}
