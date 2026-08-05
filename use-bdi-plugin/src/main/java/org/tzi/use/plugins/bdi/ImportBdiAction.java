package org.tzi.use.plugins.bdi;

import java.awt.BorderLayout;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.tzi.use.gui.main.ViewFrame;
import org.tzi.use.runtime.gui.IPluginAction;
import org.tzi.use.runtime.gui.IPluginActionDelegate;
import org.tzi.use.plugins.bdi.ui.BdiExplorerView;

/** Opens the BDI explorer and starts a multi-file AgentSpeak import. */
public final class ImportBdiAction implements IPluginActionDelegate {
    @Override
    public void performAction(IPluginAction pluginAction) {
        JFileChooser chooser = createFileChooser();
        if (chooser.showOpenDialog(pluginAction.getParent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        List<Path> sources = Arrays.stream(chooser.getSelectedFiles())
                .map(java.io.File::toPath)
                .toList();
        openView(pluginAction, sources);
    }

    @Override
    public boolean shouldBeEnabled(IPluginAction pluginAction) {
        return true;
    }

    static JFileChooser createFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import AgentSpeak source");
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new FileNameExtensionFilter("AgentSpeak files (*.asl)", "asl"));
        return chooser;
    }

    public static void chooseAndImport(BdiExplorerView view) {
        JFileChooser chooser = createFileChooser();
        if (chooser.showOpenDialog(view) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        view.importFiles(Arrays.stream(chooser.getSelectedFiles()).map(java.io.File::toPath).toList());
    }

    static void openView(IPluginAction pluginAction, List<Path> sources) {
        BdiExplorerView view = pluginAction.getSession().hasSystem()
                ? new BdiExplorerView(pluginAction.getSession().system())
                : new BdiExplorerView();
        ViewFrame frame = new ViewFrame("BDI Explorer", view, "New.gif");
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(view, BorderLayout.CENTER);
        pluginAction.getParent().addNewViewFrame(frame);
        frame.setSize(920, 580);
        view.importFiles(sources);
    }
}
