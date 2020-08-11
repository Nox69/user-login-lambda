package com.cts.mc.s3;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cts.mc.model.User;

/**
 * @author bharatkumar
 *
 */
public class VerifyUserService extends S3BaseService {

	private VerifyUserService() {
		// Utility classes should not have public constructors (squid:S1118)
	}

	private static Logger log = LoggerFactory.getLogger(VerifyUserService.class);

	public static boolean verifyUser(User user) {
		try {
			List<User> usersList = retrieveUsersListFromS3(user);

			// Stream the User List to compare the Email and PAC code.
			if (!usersList.isEmpty() && usersList.stream().filter(usr -> user.compareTo(usr) == 0).count() == 1)
				return true;
		} catch (Exception e) {
			log.error("Verification Failed.");
		}
		return false;

	}

}
