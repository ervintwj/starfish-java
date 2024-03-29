package sg.dex.starfish.impl.memory;

import sg.dex.starfish.Operation;

/**
 *Class representing a local in-memory operation asset.
 *
 * Intended for use in testing or local development situations.
 * Abstract class that have common code required for different memory operation implementation
 */
public abstract class AMemoryOperation extends AMemoryAsset implements Operation {


    protected AMemoryOperation(String metaString,MemoryAgent memoryAgent) {
        super(metaString,memoryAgent);
    }

}
