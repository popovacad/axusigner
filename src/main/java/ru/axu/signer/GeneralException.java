package ru.axu.signer;

public class GeneralException extends Exception {
	public GeneralException(String message) {
		super(message);
	}

	public GeneralException(String message, Exception reason) {
		super(message, reason);
	}
}
