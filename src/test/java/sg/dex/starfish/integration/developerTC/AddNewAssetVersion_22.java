package sg.dex.starfish.integration.developerTC;

import org.junit.Before;
import org.junit.Test;
import sg.dex.starfish.Asset;
import sg.dex.starfish.impl.memory.MemoryAsset;
import sg.dex.starfish.impl.remote.RemoteAgent;
import sg.dex.starfish.impl.remote.RemoteAsset;

import static junit.framework.TestCase.assertEquals;

/**
 * As a publisher working for Ocean,
 * I want to publish a new asset (as a new version of an existing asset), so that consumers can purchase the latest version
 */
public class AddNewAssetVersion_22 {
    private RemoteAgent remoteAgent;


    @Before
    public void setUp() {
        // create remote Agent
        remoteAgent = RemoteAgentConfig.getRemoteAgent();
        // create remote Asset


    }

    @Test
    public void testNewVersionOfAsset() {
    	if (remoteAgent==null) return;

        String data ="test upload of asset";
        Asset asset = MemoryAsset.create(data);
        RemoteAsset ra = (RemoteAsset)remoteAgent.uploadAsset(asset);

        String assetData =RemoteAgentConfig.getDataAsStringFromInputStream(ra.getContentStream());

        assertEquals(assetData,data);
        assertEquals(asset.getAssetID(), ra.getAssetID());

        // modify the content


    }
}
