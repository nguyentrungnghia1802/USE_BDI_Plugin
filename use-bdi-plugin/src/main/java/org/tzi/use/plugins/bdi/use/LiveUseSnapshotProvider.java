package org.tzi.use.plugins.bdi.use;

import java.util.Objects;
import java.util.function.Supplier;

import org.tzi.use.uml.sys.MSystem;

/** USE adapter that resolves the current system anew for every capture. */
public final class LiveUseSnapshotProvider implements UseSnapshotProvider {
    private final Supplier<MSystem> currentSystem;
    private final UseUmlModelFacade facade;

    public LiveUseSnapshotProvider(Supplier<MSystem> currentSystem) {
        this(currentSystem, new UseUmlModelFacade());
    }

    LiveUseSnapshotProvider(Supplier<MSystem> currentSystem, UseUmlModelFacade facade) {
        this.currentSystem = Objects.requireNonNull(currentSystem, "currentSystem");
        this.facade = Objects.requireNonNull(facade, "facade");
    }

    @Override
    public UseSnapshotContext capture() {
        MSystem system = Objects.requireNonNull(currentSystem.get(), "current USE system");
        return new UseSnapshotContext(
                facade.snapshot(system),
                new UseSnapshotOclEvaluator(system));
    }
}
