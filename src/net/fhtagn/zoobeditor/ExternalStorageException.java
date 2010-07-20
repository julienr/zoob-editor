package net.fhtagn.zoobeditor;

import java.io.IOException;

public class ExternalStorageException extends IOException {
	public ExternalStorageException (String message) {
		super(message);
	}
}