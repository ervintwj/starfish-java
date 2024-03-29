
package sg.dex.starfish.impl.file;

import sg.dex.starfish.DataAsset;
import sg.dex.starfish.exception.AuthorizationException;
import sg.dex.starfish.exception.StorageException;
import sg.dex.starfish.impl.AAsset;
import sg.dex.starfish.util.JSON;
import sg.dex.starfish.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import static sg.dex.starfish.constant.Constant.*;

/**
 * Class exposing a file on the local file system as an Ocean asset
 *
 * @author Mike
 *
 */
public class FileAsset extends AAsset implements DataAsset {
	private final File file;

	protected FileAsset(String meta, File file) {
		super(meta);
		this.file = file;
	}

	/**
	 * Create a FileAsset to read from an existing file
	 * 
	 * @param f file handle one which the File Asset will be created
	 * @return FileAsset
	 */
	public static FileAsset create(File f) {
		String metaString = JSON.toString(buildMetadata(f));
		return new FileAsset(metaString, f);
	}


	/**
	 * Build default metadata for a file asset
	 * 
	 * @param f The file to use for this file asset
	 * @return The default metadata as a String
	 */
	protected static Map<String, Object> buildMetadata(File f) {

		Map<String, Object> ob = Utils.createDefaultMetadata();
		ob.put(TYPE, DATA_SET);
		ob.put(SIZE, f.length());
		ob.put(FILE_NAME, f.getName());
		ob.put(CONTENT_TYPE, OCTET_STREAM);

		return ob;
	}

	/**
	 * Gets an input stream that can be used to consume the content of this asset.
	 *
	 * Will throw an exception if consumption of the asset data in not possible
	 * locally.
	 * 
	 * @throws AuthorizationException if requestor does not have access permission
	 * @throws StorageException if unable to load the Asset
	 * @return An input stream allowing consumption of the asset data
	 */
	@Override
	public InputStream getContentStream() {
		try {
			return new FileInputStream(file);
		}
		catch (FileNotFoundException e) {
			throw new StorageException("File not found ,file : "+file , e);
		}
	}

	@Override
	public long getContentSize() {
		return null != file ? file.length(): -1;
	}

	/**
	 * Gets an input stream that can be used to consume the content of this asset.
	 *
	 * Will throw an exception if consumption of the asset data in not possible
	 * locally.
	 * 
	 * @throws AuthorizationException if requestor does not have access permission
	 * @throws StorageException if unable to load the Asset
	 * @return An input stream allowing consumption of the asset data
	 */
	public File getFile() {
		return file;
	}

}
