/**
 * Represents an Asset on the Ocean Network.
 * <p>
 * Assets are defined by JSON metadata, and the Asset ID is the keccak256 hash of the metadata
 * as encoded in UTF-8.
 */
package sg.dex.starfish.impl.memory;

import sg.dex.crypto.Hash;
import sg.dex.starfish.Asset;
import sg.dex.starfish.DataAsset;
import sg.dex.starfish.exception.AuthorizationException;
import sg.dex.starfish.exception.StorageException;
import sg.dex.starfish.util.Hex;
import sg.dex.starfish.util.JSON;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static sg.dex.starfish.constant.Constant.*;

/**
 * Class representing a local in-memory data asset.
 *
 * Intended for use in testing or local development situations.
 *
 * @author Mike
 *
 */
public class MemoryAsset extends AMemoryAsset implements DataAsset {

    private byte[] data;

    private MemoryAsset(byte[] data,String metaData) {
        super(metaData);

        this.data = data;

    }

    private static String buildMetaData(byte[] data, Map<String, Object> meta) {
        String hash = Hex.toString(Hash.keccak256(data));

        Map<String, Object> ob = new HashMap<>();
        ob.put(DATE_CREATED, Instant.now().toString());
        ob.put(CONTENT_HASH, hash);
        ob.put(TYPE, DATA_SET);
        ob.put(SIZE, Integer.toString(data.length));
        ob.put(CONTENT_TYPE, "application/octet-stream");

        if (meta != null) {
            for (Map.Entry<String, Object> me : meta.entrySet()) {
                ob.put(me.getKey(), me.getValue());
            }
        }

        return JSON.toString(ob);
    }
    /**
     * Gets a MemoryAsset using the content and metadata from the provided asset
     *
     * @param asset The asset to use to construct this MemoryAsset
     * @return A new MemoryAsset containing the data from the passed asset argument
     */
    public static MemoryAsset create(Asset asset) {
        if (asset instanceof MemoryAsset) {
            return (MemoryAsset) asset;
        } else if (asset.isDataAsset()) {
            byte[] data = asset.getContent();
            return new MemoryAsset(data,buildMetaData(data,null));
        } else {
            throw new IllegalArgumentException("Asset must be a data asset");
        }
    }

    /**
     * Creates a MemoryAsset with the provided data. Default metadata will be
     * generated.
     *
     * @param data Byte array containing the data for this asset
     * @return The newly created in-memory asset
     */
    public static MemoryAsset create(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Missing data");
        }
        return new MemoryAsset(data,buildMetaData(data,null));
    }

    /**
     * Creates a MemoryAsset with the provided string data, encoded in UTF_8 Default
     * metadata will be generated.
     *
     * @param string String containing the data for this asset
     * @return The newly created in-memory asset
     */
    public static Asset create(String string) {
        byte[] data = string.getBytes(StandardCharsets.UTF_8);
        return new MemoryAsset(data,buildMetaData(data,null));
    }

    /**
     * Creates a MemoryAsset with the provided metadata and content
     *
     * @param meta A map containing the metadata for this asset
     * @param data Byte array containing the data for this asset
     * @return The newly created in-memory asset
     */
    public static MemoryAsset create(byte[] data, Map<String, Object> meta) {
        return new MemoryAsset(data,buildMetaData(data,meta));
    }

    /**
     * Creates a MemoryAsset with the finalized metadata and content
     *
     * @param meta A string containing the finalized metadata for this asset
     * @param data Byte array containing the data for this asset
     * @return The newly created in-memory asset
     */
    public static MemoryAsset create(byte[] data, String meta) {
        return new MemoryAsset(data,meta);
    }

    /**
     * Gets InputStream corresponding to this Asset
     *
     * @throws AuthorizationException if requestor does not have access permission
     * @throws StorageException if unable to load the Asset
     * @return An input stream allowing consumption of the asset data
     */
    @Override
    public InputStream getContentStream() {
        if (data == null) throw new Error("MemoryAsset has not been initialised with data");
        return new ByteArrayInputStream(data);
    }

    /**
     * Gets raw data corresponding to this Asset
     *
     * @throws AuthorizationException if requestor does not have access permission
     * @throws StorageException if unable to load the Asset
     * @return An input stream allowing consumption of the asset data
     */
    @Override
    public byte[] getContent() {
        // we take a copy of data to protected immutability of MemoryAsset instance
        return null != data ? data.clone() : null;
    }


    @Override
    public Map<String, Object> getParamValue() {
        Map<String, Object> o = new HashMap<>();
        // pass the asset ID, i.e. hash of content
        o.put(ID, getAssetID());
        return o;
    }

    @Override
    public long getContentSize() {

        return this.data != null ? this.data.length : -1;
    }
}
