package optimizationprototype.optimization;

import optimizationprototype.structure.SourceFile;

public abstract class OptimizerBase {

    protected SourceFile file;

    protected OptimizerBase(SourceFile file) {
        this.file = file;
    }

}
