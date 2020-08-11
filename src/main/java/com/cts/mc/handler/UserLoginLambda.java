package com.cts.mc.handler;

import static com.cts.mc.s3.GenerateOrderCodeService.generateOrderCode;
import static com.cts.mc.s3.VerifyUserService.verifyUser;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.cts.mc.model.LoggedUser;
import com.cts.mc.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * @author bharatkumar
 *
 */
public class UserLoginLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static final String FAILURE = "Incorrect User Login Credentials";
	private static Logger log = LoggerFactory.getLogger(UserLoginLambda.class);

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {

		try {
			User user = retrieveUser(request.getBody());
			log.info("Verifying User Credentials with Email Id : [{}]", user.getEmailId());

			if (verifyUser(user)) {
				return generateAPIGatewayResponse(generateOrderCode(user));
			}

		} catch (Exception e) {
			log.error("ExceptionOccurred while Verifying UserLogin Credentials : [{}] at [{}] with exception {}",
					request.getBody(), LocalDateTime.now(), e);
		}

		return generateAPIGatewayResponse(null);
	}

	private User retrieveUser(String userDetails) {
		try {
			log.info("URL Encoded JSON automatically Decoded : [{}]", userDetails);
			return new Gson().fromJson(userDetails, User.class);
		} catch (JsonSyntaxException e) {
			log.error("Unable to Parse String to User Object.");
			throw new AmazonServiceException("Unable to Retrieve User");
		}
	}

	private APIGatewayProxyResponseEvent generateAPIGatewayResponse(User user) {
		Map<String, String> corsHeaders = new HashMap();
		corsHeaders.put("Access-Control-Allow-Origin", "*");
		return user != null && StringUtils.isNotEmpty(user.getOrderCode())
				? new APIGatewayProxyResponseEvent().withBody(buildFromUser(user)).withStatusCode(200)
						.withHeaders(corsHeaders)
				: new APIGatewayProxyResponseEvent().withBody(FAILURE).withStatusCode(400).withHeaders(corsHeaders);
	}

	private String buildFromUser(User user) {
		return new Gson().toJson(LoggedUser.builder().dateOfBirth(user.getDateOfBirth()).firstName(user.getFirstName())
				.orderCode(user.getOrderCode()).lastName(user.getLastName()).mobileNo(user.getMobileNo())
				.emailId(user.getEmailId()).build(), LoggedUser.class);
	}

}
