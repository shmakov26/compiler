package org.example;

public class VarInfo {
    private Type type;
    private boolean initialized;

    public VarInfo(Type type, boolean init) {
        this.type = type;
        this.initialized = init;
    }

    public Type getType() {
        return type;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
}
