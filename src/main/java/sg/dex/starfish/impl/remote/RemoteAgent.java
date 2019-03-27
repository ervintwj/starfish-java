package sg.dex.starfish.impl.remote;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import sg.dex.starfish.*;
import sg.dex.starfish.exception.*;
import sg.dex.starfish.impl.AAgent;
import sg.dex.starfish.util.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class implementing a remote storage agent using the Storage API
 *
 * @author Mike
 *
 */
public class RemoteAgent extends AAgent implements Invokable, MarketAgent {

	private final Account account;
	
	/**
	 * Creates a RemoteAgent with the specified Ocean connection and DID
	 *
	 * @param ocean Ocean connection to use
	 * @param did DID for this agent
	 */
	protected RemoteAgent(Ocean ocean, DID did, Account acc) {
		super(ocean, did);
		this.account=acc;
	}

	/**
	 * Creates a RemoteAgent with the specified Ocean connection and DID
	 *
	 * @param ocean Ocean connection to use
	 * @param did DID for this agent
	 * @return RemoteAgent
	 */
	public static RemoteAgent create(Ocean ocean, DID did) {
		return new RemoteAgent(ocean, did,null);
	}
	
	public RemoteAgent connect(Account acc) {
		// TODO: get user token and store this in account
		return new RemoteAgent(ocean, did, acc);
	}

	/**
	 * Registers Asset a with the RemoteAgent
	 *
	 * @param a Asset to register
	 * @throws RemoteException if a is not found
	 * @throws TODOException for unhandled results
	 * @throws RuntimeException for protocol errors
	 * @return RemoteAsset corresponding to a
	 */
	@Override
	public RemoteAsset registerAsset(Asset a) {
		URI uri = getMetaURI();
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(uri);
		addAuthHeaders(httpPost);
		httpPost.setEntity(HTTP.textEntity(a.getMetadataString()));
		CloseableHttpResponse response;
		try {
			response = httpclient.execute(httpPost);
			try {
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode == 404) {
					throw new RemoteException("Asset ID not found for at: " + uri);
				}
				if (statusCode == 200) {
					String body = Utils.stringFromStream(response.getEntity().getContent());
					String id = JSON.parse(body);
					return getAsset(id);
				}
				throw new TODOException("Result not handled: " + statusLine);
			}
			finally {
				response.close();
			}
		}
		catch (ClientProtocolException e) {
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	void addAuthHeaders(HttpRequest request) {
		request.setHeader("Authorization", "Basic QWxhZGRpbjpPcGVuU2VzYW1l");
	}

	/**
	 * Gets an asset for the given asset ID from this agent.
	 * Returns Runtime Exception  if the asset ID does not exist.
	 *
	 * @param id The ID of the asset to get from this agent
	 * @throws AuthorizationException if requestor does not have access permission
	 * @throws StorageException if there is an error in retreiving the Asset
	 * @throws RemoteException if there is a failure in a remote operation
	 * @throws TODOException for unhandled status codes
	 * @return Asset The asset found
	 */
	@Override
	public RemoteAsset getAsset(String id) {
		URI uri = getMetaURI(id);
		HttpGet httpget = new HttpGet(uri);
		addAuthHeaders(httpget);
		CloseableHttpResponse response = HTTP.execute(httpget);
		try {
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 404) {
				throw new RemoteException("Asset ID not found for at: " + uri);
			}
			else if (statusCode == 200) {
				String body = Utils.stringFromStream(HTTP.getContent(response));
				// TODO: consider if this this an operation rather than data asset?
				return RemoteAsset.create(this, body);
			}
			else {
				throw new TODOException("status code not handled: " + statusCode);
			}
		}
		finally {
			HTTP.close(response);
		}
	}

    /**
     * API to check if the Asset is present if present it will return true else false.
     *
     * @param assetId
     * @return
     */
    private boolean isAssetRegistered(String assetId) {
        URI uri = getMetaURI(assetId);
        HttpGet httpget = new HttpGet(uri);
        addAuthHeaders(httpget);
        CloseableHttpResponse response = HTTP.execute(httpget);
        try {
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                return true;
            }
            return false;
        } finally {
            HTTP.close(response);
        }
    }

	/**
	 * Uploads an asset to this agent. Registers the asset with the agent if required.
	 *
	 * Throws an exception if upload is not possible, with the following likely causes:
	 * - The agent does not support uploads of this asset type / size
	 * - The data for the asset cannot be accessed by the agent
	 *
	 * @param a Asset to upload
	 * @throws AuthorizationException if requestor does not have upload permission
	 * @throws StorageException if there is an error in uploading the Asset
	 * @throws RemoteException if there is an problem executing the task on remote Server.
	 * @return Asset An asset stored on the agent if the upload is successful
	 */
    @Override
    public RemoteAsset uploadAsset(Asset a) {
        RemoteAsset remoteAsset;

        // asset alredy registered then only upload
        if (isAssetRegistered(a.getAssetID())) {
            remoteAsset = getAsset(a.getAssetID());
            uploadAssetContent(a);
        }
        // if asset is not registered then registered and upload
        else {
            remoteAsset = registerAsset(a);
            uploadAssetContent(a);
        }

        return remoteAsset;
    }

    /**
     * API to uplaod the content of an Asset.
     * API to upload the Asset content
     *
     * @param asset
     * @return
     */
    private void uploadAssetContent(Asset asset) {
        // get the storage API to upload the Asset content
        URI uri = getStorageURI(asset.getAssetID());
        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpPost post = new HttpPost(uri);
        addAuthHeaders(post);
        post.addHeader("Accept", "application/json");

        InputStream assetContentAsStream = new ByteArrayInputStream(asset.getContent());
        HttpEntity entity = HTTP.createMultiPart("file", new InputStreamBody(assetContentAsStream, Utils.createRandomHexString(16) + ".tmp"));

        post.setEntity(entity);

        CloseableHttpResponse response;
        try {
            response = httpclient.execute(post);
            try {
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 201) {
                    return;
                }
                if (statusCode == 404) {
                    throw new RemoteException("server could not find what was requested or it was configured not to fulfill the request." + uri);
                } else if (statusCode == 500) {
                    throw new GenericException("Internal Server Error : " + statusLine);
                } else {
                    throw new TODOException("Result not handled: " + statusLine);
                }
            } finally {
                response.close();
            }
        } catch (ClientProtocolException e) {
            throw new RemoteException("ClientProtocolException executing HTTP request ," + e.getMessage(), e);
        } catch (IOException e) {
            throw new RemoteException("IOException executing HTTP request ," + e.getMessage(), e);
        }
    }

	/**
	 * Gets a URL string for accessing the specified asset ID
	 *
	 * @param id The asset ID to address
	 * @throws TODOException when not implemented yet
	 * @return The URL for the asset as a String
	 */
	public String getAssetURL(String id) {
		throw new TODOException();
	}

	/**
	 * Gets URI for this agent's invoke endpoint
	 *
	 * @throws RuntimeException on URI syntax errors
	 * @return The URI for this agent's invoke endpoint
	 */
	public URI getInvokeURI() {
		try {
			return new URI(getInvokeEndpoint() + "/invokesync");
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets URI for this agent's endpoint for jobID
	 *
	 * @param jobID of the URI to create
	 * @throws IllegalArgumentException on invalid URI for jobID
	 * @return The URI for this agent's invoke endpoint
	 */
	private URI getJobURI(String jobID) {
		try {
			return new URI(getInvokeEndpoint() + "/jobs/" + jobID);
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException("Can't create valid URI for job: " + jobID, e);
		}
	}

	/**
	 * Gets meta URI for this assetID
	 *
	 * @param assetID of the URI to create
	 * @throws UnsupportedOperationException if the agent does not support the Meta API
	 * @throws IllegalArgumentException on invalid URI for assetID
	 * @return The URI for this assetID
	 */
	private URI getMetaURI(String assetID) {
		String metaEndpoint = getMetaEndpoint();
		if (metaEndpoint == null)
			throw new UnsupportedOperationException("This agent does not support the Meta API (no endpoint defined)");
		try {
			return new URI(metaEndpoint + "/data/" + assetID);
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException("Can't create valid URI for asset metadata with ID: " + assetID, e);
		}
	}

	/**
	 * Gets storage URI for this assetID
	 *
	 * @param assetID of the URI to create
	 * @throws UnsupportedOperationException if the agent does not support the Storage API
	 * @throws IllegalArgumentException on invalid URI for assetID
	 * @return The URI for this assetID
	 */
	public URI getStorageURI(String assetID) {
		String storageEndpoint = getStorageEndpoint();
		if (storageEndpoint == null) throw new UnsupportedOperationException(
				"This agent does not support the Storage API (no endpoint defined)");
		try {
			return new URI(storageEndpoint + "/" + assetID);
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException("Can't create valid URI for asset storage with ID: " + assetID, e);
		}
	}

	/**
	 * Gets meta URI for this agent
	 *
	 * @throws UnsupportedOperationException if the agent does not support the Meta API (no endpoint defined)
	 * @throws IllegalArgumentException on invalid URI for asset metadata
	 * @return The URI for asset metadata
	 */
	private URI getMetaURI() {
		String metaEndpoint = getMetaEndpoint();
		if (metaEndpoint == null)
			throw new UnsupportedOperationException("This agent does not support the Meta API (no endpoint defined)");
		try {
			return new URI(metaEndpoint + "/data");
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException("Can't create valid URI for asset metadata", e);
		}
	}


	/**
	 * Gets URL for a given remoteAsset
	 *
	 * @param remoteAsset for the URL
	 * @throws IllegalStateException No storage endpoint available for agent
	 * @throws Error on failure to get asset URL
	 * @return The URL for the remoteAsset
	 */
	public URL getURL(RemoteAsset remoteAsset) {
		String storageEndpoint = getStorageEndpoint();
		if (storageEndpoint == null) throw new IllegalStateException("No storage endpoint available for agent");
		try {
			return new URL(storageEndpoint + "/" + remoteAsset.getAssetID());
		}
		catch (MalformedURLException e) {
			throw new Error("Failed to get asset URL", e);
		}
	}

	/**
	 * Gets the storage endpoint for this agent
	 *
	 * @return The storage endpoint for this agent e.g.
	 *         "https://www.myagent.com/api/v1/storage"
	 */
	public String getStorageEndpoint() {
		return getEndpoint("Ocean.Storage.v1");
	}

	/**
	 * Gets the Invoke API endpoint for this agent
	 *
	 * @return The invoke endpoint for this agent e.g.
	 *         "https://www.myagent.com/api/v1/invoke"
	 */
	public String getInvokeEndpoint() {
		return getEndpoint("Ocean.Invoke.v1");
	}

	/**
	 * Gets the Meta API endpoint for this agent, or null if this does not exist
	 *
	 * @return The Meta API endpoint for this agent e.g.
	 *         "https://www.myagent.com/api/v1/meta"
	 */
	public String getMetaEndpoint() {
		return getEndpoint("Ocean.Meta.v1");
	}

	/**
	 * Gets the Market API endpoint for this agent, or null if this does not exist
	 *
	 * @return The Meta API endpoint for this agent e.g.
	 *         "https://www.myagent.com/api/v1/meta"
	 */
	public String getMarketEndpoint() {
		return getEndpoint("Ocean.Market.v1");
	}

	@Override
	public Job invoke(Operation operation, Asset... params) {
		Map<String, Object> request = new HashMap<String, Object>(2);
		request.put("operation", operation.getAssetID());
		request.put("params", Params.formatParams(operation, params));
		return invoke(request);
	}

	/**
	 * Polls this agent for the Asset resulting from the given job ID
	 *
	 * @param jobID ID for the Job to poll
	 * @throws IllegalArgumentException If the job ID is invalid
	 * @throws RemoteException if there is a failure in a remote operation
	 * @throws TODOException for unhandled status codes
	 * @throws RuntimeException for protocol errors
	 * @return The asset resulting from this job ID if available, null otherwise.
	 */
	public Asset pollJob(String jobID) {
		URI uri = getJobURI(jobID);
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpget = new HttpGet(uri);
		CloseableHttpResponse response;
		try {
			response = httpclient.execute(httpget);
			try {
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode == 404) {
					throw new RemoteException("Job ID not found for invoke at: " + uri);
				}
				if (statusCode == 200) {
					String body = Utils.stringFromStream(response.getEntity().getContent());
					Map<String, Object> result = JSON.toMap(body);
					String status = (String) result.get("status");
					if (status == null) throw new RemoteException("No status in job result: " + body);
					if (status.equals("started") || status.equals("inprogress")) {
						return null; // no result yet
					}
					if (status.equals("complete")) {
						String assetID = (String) result.get("result");
						if (assetID == null) throw new RemoteException("No asset in completed job result: " + body);
						return RemoteAsset.create(this, assetID);
					}
				}
				throw new TODOException("status code not handled: " + statusCode);
			}
			finally {
				response.close();
			}
		}
		catch (ClientProtocolException e) {
			throw new JobFailedException(" Client Protocol Expectopn :",e);
		}
		catch (IOException e) {
			throw new JobFailedException(" IOException occured  Expectopn :",e);
		}
	}

	@Override
	public Job invoke(Operation operation, Map<String, Asset> params) {
		Map<String, Object> request = new HashMap<String, Object>(2);
		request.put("operation", operation.getAssetID());
		request.put("params", Params.formatParams(operation, params));
		return invoke(request);
	}

	/**
	 * Invokes request on this RemoteAgent
	 *
	 * @param request Invoke request
	 * @throws RuntimeException for protocol errors
	 * @return Job for this request
	 */
	private Job invoke(Map<String, Object> request) {
		String req = JSON.toString(request);
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(getInvokeURI());
		StringEntity entity = new StringEntity(req, StandardCharsets.UTF_8);
		httppost.setEntity(entity);
		CloseableHttpResponse response;
		try {
			response = httpclient.execute(httppost);
			try {
				return RemoteAgent.createJob(this, response);
			}
			finally {
				response.close();
			}
		}
		catch (ClientProtocolException e) {
			throw new JobFailedException(" Client Protocol Expectopn :",e);
		}
		catch (IOException e) {
			throw new JobFailedException(" IOException occured  Expectopn :",e);
		}
	}

	/**
	 * Invokes request on this RemoteAgent
	 *
	 * @param agent the remote
	 * @param response
	 * @throws RuntimeException for protocol errors
	 * @return Job for this request
	 */
	private static Job createJobWith200(RemoteAgent agent, HttpResponse response) {
		HttpEntity entity = response.getEntity();
		if (entity == null) throw new RuntimeException("Invoke failed: no response body");
		try {
			String body = Utils.stringFromStream(entity.getContent());
			return RemoteJob.create(agent, body);
		}
		catch (Exception e) {
			throw  new GenericException("Internal Server Error");
		}
	}

	/**
	 * Creates a remote invoke Job using the given HTTP response.
	 *
	 * @param agent RemoteAgent on which to create the Job
	 * @param response A valid successful response from the remote Invoke API
	 * @throws IllegalArgumentException for a bad invoke request
	 * @throws RuntimeException for protocol errors
	 * @return A job representing the remote invocation
	 */
	public static Job createJob(RemoteAgent agent, HttpResponse response) {
		StatusLine statusLine = response.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		if (statusCode == 200) {
			return RemoteAgent.createJobWith200(agent, response);
		}
		String reason = statusLine.getReasonPhrase();
		if ((statusCode) == 400) {
			throw new IllegalArgumentException("Bad invoke request: " + reason);
		}
		throw  new GenericException("Internal Server Error");
	}

	@Override
	public List<RemoteListing> getAllListing() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RemoteListing createListing(Map<String, Object> listingData) {
		// TODO Auto-generated method stub
		return null;
	}

}
