package com.cts.mc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author bharatkumar
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoggedUser {

	private String firstName;
	private String lastName;
	private String mobileNo;
	private String dateOfBirth;
	private String orderCode;
	private String emailId;
	
}
