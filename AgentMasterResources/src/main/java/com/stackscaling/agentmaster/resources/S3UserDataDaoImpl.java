package com.stackscaling.agentmaster.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.stackscaling.agentmaster.resources.aws.AwsResourceProvider;

/**
 * use S3 as user data store
 * @author binyu
 *
 */
public class S3UserDataDaoImpl implements IUserDataDao {

	static Logger logger = LoggerFactory.getLogger(S3UserDataDaoImpl.class);

	@Override
	public String readData(DataType type, String name) throws IOException
	{
		try {

			S3Object s3object = AwsResourceProvider.getS3Client().getObject(new GetObjectRequest(type.getUuid(), name));
			return readInputStream(s3object.getObjectContent());

		} catch (AmazonServiceException ase) {
			logger.error("Caught an AmazonServiceException, which"
					+ " means your request made it "
					+ "to Amazon S3, but was rejected with an error response"
					+ " for some reason.");
			logger.error("Error Message:    " + ase.getMessage());
			logger.error("HTTP Status Code: " + ase.getStatusCode());
			logger.error("AWS Error Code:   " + ase.getErrorCode());
			logger.error("Error Type:       " + ase.getErrorType());
			logger.error("Request ID:       " + ase.getRequestId());
			throw new IOException(ase);
		} catch (AmazonClientException ace) {
			logger.error("Caught an AmazonClientException, which means"
					+ " the client encountered "
					+ "an internal error while trying to "
					+ "communicate with S3, "
					+ "such as not being able to access the network.");
			logger.error("Error Message: " + ace.getMessage());
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
			logger.info("uploaded to S3 " + name);

		} catch (AmazonServiceException ase) {
			logger.error("Caught an AmazonServiceException, which "
					+ "means your request made it "
					+ "to Amazon S3, but was rejected with an error response"
					+ " for some reason.");
			logger.error("Error Message:    " + ase.getMessage());
			logger.error("HTTP Status Code: " + ase.getStatusCode());
			logger.error("AWS Error Code:   " + ase.getErrorCode());
			logger.error("Error Type:       " + ase.getErrorType());
			logger.error("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			logger.error("Caught an AmazonClientException, which "
					+ "means the client encountered "
					+ "an internal error while trying to "
					+ "communicate with S3, "
					+ "such as not being able to access the network.");
			logger.error("Error Message: " + ace.getMessage());
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
        	logger.info("deleted from S3 " + name);

        } catch (AmazonServiceException ase) {
            logger.error("Caught an AmazonServiceException.");
            logger.error("Error Message:    " + ase.getMessage());
            logger.error("HTTP Status Code: " + ase.getStatusCode());
            logger.error("AWS Error Code:   " + ase.getErrorCode());
            logger.error("Error Type:       " + ase.getErrorType());
            logger.error("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            logger.error("Caught an AmazonClientException.");
            logger.error("Error Message: " + ace.getMessage());
        }
	}

	@Override
	public List<UserDataMeta> listNames(DataType type)
	{
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(type.getUuid());
		ObjectListing objectListing;
		ArrayList<UserDataMeta> result = new ArrayList<UserDataMeta>();

		do {
			objectListing = AwsResourceProvider.getS3Client().listObjects(listObjectsRequest);
			for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
				result.add(new UserDataMeta(
						objectSummary.getKey(), 
						objectSummary.getSize(), 
						objectSummary.getLastModified()));
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

	@Override
	public InputStream readStream(DataType type, String name)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveStream(DataType type, String name, InputStream dataStream)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

}
