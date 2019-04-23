package sg.dex.starfish;

import java.util.Map;

import sg.dex.starfish.util.DID;
import sg.dex.starfish.exception.AuthorizationException;
import sg.dex.starfish.exception.StorageException;

/**
 * Interface representing an Asset. An Asset in Starfish is an entity in the Ocean Ecosystem
 * that is:
 * A) Described by metadata
 * B) Able to be used in Data Supply Lines
 *
 * @author Mike
 */
public interface 	Asset {

	/**
	 * Gets DID for this Asset
	 *
	 * @throws UnsupportedOperationException if unable to obtain DID
	 * @return the assetID
	 */
	public String getAssetID();

	/**
	 * Gets the Ocean DID for this asset. The DID may include a DID path to specify
	 * the precise asset if the DID refers to an agent managing the asset.
	 *
	 * Throws an exception if a DID is not available or cannot be constructed.
	 *
	 * @return The global DID for this asset.
	 */
	public DID getAssetDID();

	/**
	 * Gets a copy of the JSON metadata for this asset.
	 *
	 * @return New clone of the parsed JSON metadata for this asset
	 */
	public Map<String, Object> getMetadata();

	/**
	 * Returns true if this asset is a data asset, i.e. the asset represents an immutable
	 * data object.
	 *
	 * @return true if the asset is a data asset, false otherwise
	 */
	public default boolean isDataAsset(){
		return false;
	}

	/**
	 * Returns this asset as a DataAsset.
	 *
	 * @throws RuntimeException an exception if this asset is not a valid data asset
	 * @return This asset cast to a DataAsset
	 */
	public default DataAsset asDataAsset() {
		return (DataAsset)this;
	}

	/**
	 * Returns true if this asset is an operation, i.e. can be invoked on an
	 * appropriate agent
	 *
	 * @return true if this asset is an operation, false otherwise
	 */
	public default boolean isOperation(){
		return false;
	}

	/**
	 * Returns the metadata for this asset as a String.
	 *
	 * @return The metadata of this asset as a String
	 */
	public String getMetadataString();

	/**
	 * Gets the contents of this data asset as a byte[] array
	 *
	 * @throws UnsupportedOperationException If this asset does not support getting byte data
	 * @throws AuthorizationException if requestor does not have access permission
	 * @throws StorageException if unable to load the Asset
	 * @return The byte contents of this asset.
	 */
	public default byte[] getContent() {
		throw new UnsupportedOperationException("Cannot get bytes for asset of class: "+this.getClass().getCanonicalName());
	}

	/**
	 * Gets the representation of this asset as required to pass to a remote invokable
	 * service.
	 * @return A map representing this asset
	 */
	public Map<String, Object> getParamValue();
	/**
	 * Returns true if this asset is an bundle, i.e. can have many sub-asset.
	 *
	 * @return true if this asset is an bundle, false otherwise
	 */
	public default boolean isBundle(){
		return false;
	}
}
