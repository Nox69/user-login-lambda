package com.cts.mc.s3;

import static com.cts.mc.config.AwsClientConfiguration.s3Client;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.S3Object;
import com.cts.mc.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

/**
 * 
 * @author Bharat Kumar
 *
 */
public abstract class S3BaseService {

	private static Gson gson = new Gson();
	private static Logger log = LoggerFactory.getLogger(S3BaseService.class);
	private static final String S3_USER_BUCKET = "aws-user-registration";
	private static final String S3_USER_FILE_KEY = "user.json";

	public static List<User> readS3Object(S3Object s3Object) {
		try {
			return Arrays
					.asList(gson.fromJson(new InputStreamReader(s3Object.getObjectContent(), UTF_8), User[].class));
		} catch (JsonSyntaxException | JsonIOException e) {
			log.error("Unable to parse the S3 Object");
		}
		return null;
	}

	public static List<User> retrieveUsersListFromS3(User user) {
		try {
			// Get the existing object from Bucket.
			S3Object s3Object = s3Client().getObject(S3_USER_BUCKET, S3_USER_FILE_KEY);

			// parse the s3Object.
			List<User> usersList = readS3Object(s3Object);

			if (usersList == null || usersList.isEmpty())
				throw new AmazonServiceException("Data not present in S3 Bucket");

			return usersList;
		} catch (AmazonServiceException e) {
			log.error("Unable to either parse the user.json or its not available");
		} catch (Exception e) {
			log.error("Unexpected Exception occurred {}", e.getMessage());
		}
		return Collections.emptyList();

	}

}
