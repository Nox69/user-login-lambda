package com.cts.mc.model;

import java.util.Comparator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author bharatkumar
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Comparable<User> {

	private String firstName;
	private String lastName;
	private String mobileNo;
	private String emailId;
	private String dateOfBirth;
	private String permamentAccessCode;
	private String orderCode;

	@Override
	public int compareTo(User o) {
		return Comparator.comparing(User::getEmailId).thenComparing(User::getPermamentAccessCode).compare(this, o);
	}

}
