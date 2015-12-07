package com.sap.ims.isa.jcomigrationhelper.markers.helpers;

/**
 * Lambdas cannot work so easy with checked exceptions, therefore we throw a {@link RuntimeException} to stop processing.
 * 
 * @author Iwan Zarembo
 *
 */
public class UserCancelRequestException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4254369077100957563L;

	public UserCancelRequestException() {
		super();
	}

	public UserCancelRequestException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UserCancelRequestException(String message, Throwable cause) {
		super(message, cause);
	}

	public UserCancelRequestException(String message) {
		super(message);
	}

	public UserCancelRequestException(Throwable cause) {
		super(cause);
	}

}
