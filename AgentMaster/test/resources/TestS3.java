package resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.StringInputStream;

public class TestS3 {

	static final String myAccessKeyID = "AKIAJL3MQLQ5JBU623RQ";
	static final String mySecretKey = "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";
	static final String bucketName = "yubin154_test";
	static final String keyName = "mytest2";

	@Test
	public void testUpload() throws Exception 
	{
		AWSCredentials myCredentials = new BasicAWSCredentials(myAccessKeyID, mySecretKey);
		AmazonS3 s3client = new AmazonS3Client(myCredentials);

		try {
			String content = "mytest from string";
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(content.getBytes().length);
			s3client.putObject(bucketName, keyName, new StringInputStream(
					content), metadata);
			System.out.println("uploaded a new object to S3 " + keyName);

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which "
					+ "means your request made it "
					+ "to Amazon S3, but was rejected with an error response"
					+ " for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which "
					+ "means the client encountered "
					+ "an internal error while trying to "
					+ "communicate with S3, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testList() throws Exception {

		AWSCredentials myCredentials = new BasicAWSCredentials(myAccessKeyID, mySecretKey);
		AmazonS3 s3client = new AmazonS3Client(myCredentials);

		ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
				.withBucketName(bucketName).withPrefix("m");
		ObjectListing objectListing;

		do {
			objectListing = s3client.listObjects(listObjectsRequest);
			for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
				System.out.println(" - " + objectSummary.getKey() + "  "
						+ "(size = " + objectSummary.getSize() + ")");
			}
			listObjectsRequest.setMarker(objectListing.getNextMarker());
			
		} while (objectListing.isTruncated());
	}

	@Test
	public void testGet() throws Exception {
		AWSCredentials myCredentials = new BasicAWSCredentials(myAccessKeyID, mySecretKey);
		AmazonS3 s3client = new AmazonS3Client(myCredentials);

		try {
			System.out.println("Downloading an object");
			S3Object s3object = s3client.getObject(new GetObjectRequest(bucketName, keyName));
			System.out.println("Content-Type: "
					+ s3object.getObjectMetadata().getContentType());
			displayTextInputStream(s3object.getObjectContent());

			// Get a range of bytes from an object.
			GetObjectRequest rangeObjectRequest = new GetObjectRequest(bucketName, keyName);
			rangeObjectRequest.setRange(0, 10);
			S3Object objectPortion = s3client.getObject(rangeObjectRequest);

			System.out.println("Printing bytes retrieved.");
			displayTextInputStream(objectPortion.getObjectContent());

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which"
					+ " means your request made it "
					+ "to Amazon S3, but was rejected with an error response"
					+ " for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means"
					+ " the client encountered "
					+ "an internal error while trying to "
					+ "communicate with S3, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}
	
	@Test
	public void testDelete() throws Exception 
	{
		AWSCredentials myCredentials = new BasicAWSCredentials(myAccessKeyID,
				mySecretKey);
		AmazonS3 s3client = new AmazonS3Client(myCredentials);

        try {
        	s3client.deleteObject(new DeleteObjectRequest(bucketName, keyName));
        	System.out.println("deleted S3 object " + keyName);
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }		

	private static void displayTextInputStream(InputStream input)
			throws IOException {
		// Read one text line at a time and display.
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		while (true) {
			String line = reader.readLine();
			if (line == null)
				break;

			System.out.println("    " + line);
		}
		System.out.println();
	}
}
