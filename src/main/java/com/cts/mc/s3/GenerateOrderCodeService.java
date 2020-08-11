package com.cts.mc.s3;

import static com.cts.mc.config.AwsClientConfiguration.s3Client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.cts.mc.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;

/**
 * @author bharatkumar
 *
 */
public class GenerateOrderCodeService extends S3BaseService {

	private GenerateOrderCodeService() {
		// Utility classes should not have public constructors (squid:S1118)
	}

	private static Logger log = LoggerFactory.getLogger(GenerateOrderCodeService.class);
	private static final String S3_USER_BUCKET = "aws-user-registration";
	private static final String S3_USER_FILE_KEY = "user.json";
	private static final String TEMP_FILE_PATH = "/tmp/user.json";
	private static Gson gson = new Gson();
	private static List<User> tempList;

	public static User generateOrderCode(User user) {
		try {
			
			tempList = new ArrayList<>();
			tempList.addAll(retrieveUsersListFromS3(user));

			// Retrieve the logged In user and add the orderCode
			User loggedInUser = tempList.stream().filter(usr -> user.compareTo(usr) == 0).findFirst().get();
			tempList.remove(loggedInUser);

			// Set the Generated Order Code
			loggedInUser.setOrderCode(generateRandomOrder());

			log.info("Order Code : [{}] Generated for current Login for User : [{}] ", loggedInUser.getOrderCode(),
					loggedInUser.getFirstName());
			tempList.add(loggedInUser);

			File file = writeToTempFile(tempList, new File(TEMP_FILE_PATH));

			// Delete the object as we will append with new data. AWS S3 doesn't support
			// appending.
			s3Client().deleteObject(S3_USER_BUCKET, S3_USER_FILE_KEY);

			// Finally upload the modified file back to S3.
			log.info("Uploading the modified Json file with OrderCode [{}] to S3 with bucketname [{}] and key [{}]",
					loggedInUser.getOrderCode(), S3_USER_BUCKET, S3_USER_FILE_KEY);
			s3Client().putObject(S3_USER_BUCKET, S3_USER_FILE_KEY, file);

			log.info("File Successfully Uploaded [{}]", file);

			return loggedInUser;

		} catch (AmazonServiceException e) {
			log.error("Unable to either parse the user.json or its not available");
		} catch (Exception e) {
			log.error("Generation of Order Code and saving to Bucket Failed");
		}
		return null;

	}

	private static String generateRandomOrder() {
		RandomStringGenerator randomStringGenerator = new RandomStringGenerator.Builder().withinRange('0', 'z')
				.filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS).build();
		return randomStringGenerator.generate(10);

	}

	private static File writeToTempFile(List<User> userList, File tempFile) {
		try (FileWriter fileWriter = new FileWriter(tempFile)) {
			gson.toJson(userList, fileWriter);
		} catch (IOException | JsonIOException e) {
			log.error("Unable to create temporary File");
		}
		return tempFile;
	}
}
