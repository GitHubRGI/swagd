package com.rgi.suite.cli;

/**
 * Exception class for use in headless validation
 * Created by matthew.moran on 6/30/15.
 */
class ValidationException extends Exception
{
	public ValidationException()
	{
		super();
	}

	public ValidationException(String message)
	{
		super(message);
	}

	public ValidationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ValidationException(Throwable cause)
	{
		super(cause);
	}
}
