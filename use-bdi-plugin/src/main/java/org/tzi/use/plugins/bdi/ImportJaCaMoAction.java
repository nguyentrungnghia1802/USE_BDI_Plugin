package org.tzi.use.plugins.bdi;

import java.awt.BorderLayout;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.tzi.use.gui.main.ViewFrame;
import org.tzi.use.plugins.bdi.application.BdiProjectConfiguration;
import org.tzi.use.plugins.bdi.application.BdiProjectConfigurationLoader;
import org.tzi.use.plugins.bdi.ui.BdiExplorerView;
import org.tzi.use.plugins.bdi.ui.BdiFileChooserSupport;
import org.tzi.use.runtime.gui.IPluginAction;
import org.tzi.use.runtime.gui.IPluginActionDelegate;
import org.tzi.use.uml.sys.MSystem;

/** Opens the Explorer for one static JaCaMo `.jcm` project. */
public final class ImportJaCaMoAction implements IPluginActionDelegate {
    @Override
    public void performAction(IPluginAction pluginAction) {
        JFileChooser chooser = createFileChooser();
        if (chooser.showOpenDialog(pluginAction.getParent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        openView(pluginAction, chooser.getSelectedFile().toPath());
    }

    @Override
    public boolean shouldBeEnabled(IPluginAction pluginAction) {
        return true;
    }

    static JFileChooser createFileChooser() {
        JFileChooser chooser = BdiFileChooserSupport.create();
        chooser.setDialogTitle("Import JaCaMo project");
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(new FileNameExtensionFilter("JaCaMo projects (*.jcm)", "jcm"));
        return chooser;
    }

    public static void chooseAndImport(BdiExplorerView view) {
        JFileChooser chooser = createFileChooser();
        if (chooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
            view.importProject(chooser.getSelectedFile().toPath());
        }
    }

    static void openView(IPluginAction pluginAction, Path projectFile) {
        BdiExplorerView view;
        try {
            if (pluginAction.getSession().hasSystem()) {
                MSystem system = pluginAction.getSession().system();
                BdiProjectConfiguration configuration = new BdiProjectConfigurationLoader()
                        .loadModel(system.model().filename());
                view = new BdiExplorerView(() -> system, configuration);
            } else {
                view = new BdiExplorerView();
            }
        } catch (IOException error) {
            JOptionPane.showMessageDialog(
                    pluginAction.getParent(), error.getMessage(),
                    "BDI project configuration error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ViewFrame frame = new ViewFrame("BDI Explorer", view, "New.gif");
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(view, BorderLayout.CENTER);
        pluginAction.getParent().addNewViewFrame(frame);
        frame.setSize(920, 580);
        view.importProject(projectFile);
    }
}
