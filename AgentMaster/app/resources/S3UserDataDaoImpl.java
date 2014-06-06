package resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import play.Logger;
import resources.aws.AwsResourceProvider;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.StringInputStream;

/**
 * use S3 as user data store
 * @author binyu
 *
 */
public class S3UserDataDaoImpl implements IUserDataDao {

	@Override
	public String readData(DataType type, String name) throws IOException 
	{
		try {
			
			S3Object s3object = AwsResourceProvider.getS3Client().getObject(new GetObjectRequest(type.getUuid(), name));
			return readInputStream(s3object.getObjectContent());
			
		} catch (AmazonServiceException ase) {
			play.Logger.error("Caught an AmazonServiceException, which"
					+ " means your request made it "
					+ "to Amazon S3, but was rejected with an error response"
					+ " for some reason.");
			play.Logger.error("Error Message:    " + ase.getMessage());
			play.Logger.error("HTTP Status Code: " + ase.getStatusCode());
			play.Logger.error("AWS Error Code:   " + ase.getErrorCode());
			play.Logger.error("Error Type:       " + ase.getErrorType());
			play.Logger.error("Request ID:       " + ase.getRequestId());
			throw new IOException(ase);
		} catch (AmazonClientException ace) {
			play.Logger.error("Caught an AmazonClientException, which means"
					+ " the client encountered "
					+ "an internal error while trying to "
					+ "communicate with S3, "
					+ "such as not being able to access the network.");
			play.Logger.error("Error Message: " + ace.getMessage());
			throw new IOException(ace);
		}

	}

	@Override
	public void saveData(DataType type, String name, String content)
			throws IOException 
	{
		try {
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(content.getBytes().length);
			AwsResourceProvider.getS3Client().putObject(
					type.getUuid(), 
					name, 
					new StringInputStream(content), 
					metadata);
			play.Logger.info("uploaded to S3 " + name);

		} catch (AmazonServiceException ase) {
			play.Logger.error("Caught an AmazonServiceException, which "
					+ "means your request made it "
					+ "to Amazon S3, but was rejected with an error response"
					+ " for some reason.");
			play.Logger.error("Error Message:    " + ase.getMessage());
			play.Logger.error("HTTP Status Code: " + ase.getStatusCode());
			play.Logger.error("AWS Error Code:   " + ase.getErrorCode());
			play.Logger.error("Error Type:       " + ase.getErrorType());
			play.Logger.error("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			play.Logger.error("Caught an AmazonClientException, which "
					+ "means the client encountered "
					+ "an internal error while trying to "
					+ "communicate with S3, "
					+ "such as not being able to access the network.");
			play.Logger.error("Error Message: " + ace.getMessage());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deleteData(DataType type, String name) throws IOException 
	{
        try {
        	
        	AwsResourceProvider.getS3Client().deleteObject(
        			new DeleteObjectRequest(type.getUuid(), name));
        	play.Logger.info("deleted from S3 " + name);
        	
        } catch (AmazonServiceException ase) {
            play.Logger.error("Caught an AmazonServiceException.");
            play.Logger.error("Error Message:    " + ase.getMessage());
            play.Logger.error("HTTP Status Code: " + ase.getStatusCode());
            play.Logger.error("AWS Error Code:   " + ase.getErrorCode());
            play.Logger.error("Error Type:       " + ase.getErrorType());
            play.Logger.error("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            play.Logger.error("Caught an AmazonClientException.");
            play.Logger.error("Error Message: " + ace.getMessage());
        }
	}

	@Override
	public List<String> listNames(DataType type) 
	{
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(type.getUuid());
		ObjectListing objectListing;
		ArrayList<String> result = new ArrayList<String>();

		do {
			objectListing = AwsResourceProvider.getS3Client().listObjects(listObjectsRequest);
			for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
				result.add(objectSummary.getKey());
			}
			listObjectsRequest.setMarker(objectListing.getNextMarker());

		} while (objectListing.isTruncated());
		
		return result;
	}
	
	/**
	 * read string presentation of an inputstream
	 * @param input
	 * @return
	 * @throws IOException
	 */
	private static String readInputStream(InputStream input)
			throws IOException 
	{
		StringBuffer buf = new StringBuffer();
		// Read one text line at a time and display.
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		while (true) {
			String line = reader.readLine();
			if (line == null)
				break;
			buf.append(line).append("\n");
		}
		return buf.toString();
	}

}
