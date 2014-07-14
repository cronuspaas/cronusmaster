package resources.aws;

import org.lightj.util.SpringContextUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import play.Play;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

/**
 * aws resources
 * to use the provide, uncomment the @Configuration annotation
 * 
 * @author binyu
 *
 */
public class AwsResourceProvider {

	static final String myAccessKeyID = new Play().configuration.getProperty("agentmaster.userDataDao.s3.myAccessKeyID");
	static final String mySecretKey = new Play().configuration.getProperty("agentmaster.userDataDao.s3.mySecretKey");

	private static AmazonS3Client s3Client;
	
	/**
	 * s3 client
	 * @return
	 */
//	public @Bean @Scope("singleton") @Lazy(true) AmazonS3 s3client() {
//		AWSCredentials myCredentials = new BasicAWSCredentials(myAccessKeyID, mySecretKey);
//		return new AmazonS3Client(myCredentials);
//	}
	

	/**
	 * get s3 client
	 * @return
	 */
	public synchronized static AmazonS3 getS3Client() {
		if (s3Client == null) {
			AWSCredentials myCredentials = new BasicAWSCredentials(myAccessKeyID, mySecretKey);
			s3Client = new AmazonS3Client(myCredentials);
		}
		return s3Client;
	}
	
}
