/*
The MIT License (MIT)

Copyright (c) 2015 Simon Päusch
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

This peace of code is heavily inspired by Alex Pretzlav
https://github.com/Pretz
and his project
https://github.com/Pretz/improved-android-remote-stacktrace
*/


package de.paeusch.stacktrace;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.net.HttpParametersUtils;


/**
 * A StackTraceSender that performs an individual http POST to a URL for a
 * stacktrace which either occured or was stored a session before. 
 * The http requests will be performed via GDX.net class.
 */
public class GdxStackTraceSender implements HttpResponseListener,
		UncaughtExceptionHandler {

	private static String TAG = "ExceptionHandler";
	private UncaughtExceptionHandler currentHandler;
	private String postURL;
	private Thread currentThread;
	private Throwable currentThrowable;
	private static String filePath = "stacktraces";
	private static String fileName = "stack.txt";
	
	/**
	 * Construct a new HttpPostStackInfoSender that will submit stack traces by
	 * POSTing them to postUrl.
	 * @param postUrl the URL to witch the stacktrace will be sent.
	 */
	public GdxStackTraceSender(String postUrl){
		postURL = postUrl;
		Gdx.app.log(TAG, Gdx.app.getType().toString());
		if(Gdx.app.getType() != ApplicationType.WebGL){
			currentHandler = Thread.getDefaultUncaughtExceptionHandler();
			Thread.setDefaultUncaughtExceptionHandler(this);
			String message = readFromFile();
			if (message != null) {
				sendStackTrace(message);
			}
		}
	}
	
	/**
	 * 
	 * Construct a new HttpPostStackInfoSender that will submit stack traces by
	 * POSTing them to postUrl.
	 * @param postUrl the URL to witch the stacktrace will be sent.
	 * @param path the filepath where a stacktrace could be stored.
	 * @param filename the filename of the stacktrace.
	 */
	public GdxStackTraceSender(String postUrl, String path, String filename) {
		this(postUrl);
		if(path == null || filename == null) {
			Gdx.app.error(TAG, "The given values should not be null. Using default values instead.");
			return;
		}
		filePath = path;
		fileName = filename;
	}

	/**
	 * Sends a stacktrace to the given URL.
	 * @param message the stacktrace.
	 */
	private void sendStackTrace(String message) {
		// LibGDX NET CLASS
		HttpRequest httpPost = new HttpRequest(HttpMethods.POST);
		httpPost.setUrl(postURL);
		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("package_name", Gdx.app.getType().toString());
		parameters.put("package_version", String.valueOf(Gdx.app.getVersion()));
		parameters.put("stacktrace", message);

		httpPost.setContent(HttpParametersUtils
				.convertHttpParameters(parameters));
		Gdx.net.sendHttpRequest(httpPost, this);
	}

	/**
	 * Handles a successful httpresponse.
	 * Deletes the file of the successfully sent
	 * stack trace and resets the exception handler
	 * to the given uncaught exception handler.
	 */
	@Override
	public void handleHttpResponse(HttpResponse httpResponse) {
		try {
			Gdx.app.debug(TAG , httpResponse.toString());
			Gdx.app.debug(TAG , String.valueOf(httpResponse.getStatus().getStatusCode()));
			if (httpResponse.getStatus().getStatusCode() == 200) {
				deleteFile();
				Gdx.app.debug(TAG, "Stacktrace successfully sent");
			}
			forwardException();
		} catch (Exception e) {
			Gdx.app.error(TAG, "Exception thrown while sending stack trace", e);
		} finally {
			forwardException();
		}
	}

	/**
	 * Restores the uncaught exception handler of the current thread.
	 */
	private void forwardException() {
		// After starting the application, the currentHandler = null
		if(currentHandler != null && currentThread != null && currentThrowable != null){
			currentHandler.uncaughtException(currentThread, currentThrowable);
		}
	}

	@Override
	public void failed(Throwable t) {
		forwardException();
	}

	/**
	 * Handles the uncaught exception.
	 * Here the thread and throwable is
	 * stored, to be able to restore
	 * both later on (after having caught and
	 * handled the exception properly).
	 * The restore of the uncaught exception 
	 * handler is done in {@link #forwardException()}.
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		currentThread = thread;
		currentThrowable = ex;
		StringBuilder message = new StringBuilder();
		message.append(ex.getClass() + " " + ex.getMessage() + "\n");
		for (StackTraceElement i : ex.getStackTrace()) {
			message.append("at " + i.getClassName() + "." + i.getMethodName()
					+ "(" + i.getFileName() + ":" + i.getLineNumber() + ")\n");
		}
		Gdx.app.error(TAG, message.toString());
		writeToFile(message.toString());
		sendStackTrace(message.toString());
	}

	/**
	 * Reads the content of the file, if exists.
	 * @return the stacktrace.
	 */
	private static String readFromFile() {
		try {
			if (Gdx.files.isLocalStorageAvailable()
					&& Gdx.files.local(filePath + "/" + fileName).exists()) {
				FileHandle handle = Gdx.files.local(filePath + "/" + fileName);
				// read from file
				return handle.readString();
			} else {
				return null;
			}
		} catch (Exception e) {
			// Nothing much we can do about this - the game is over
			Gdx.app.error(TAG, "Exception thrown while fetching stack trace from disk file", e);
			return null;
		}
	}

	/**
	 * Deletes the file (if exists).
	 */
	private void deleteFile() {
		if (Gdx.files.isLocalStorageAvailable()
				&& Gdx.files.local(filePath + "/" + fileName).exists()) {
			Gdx.files.local(filePath + "/" + fileName).delete();
		}
	}

	/**
	 * Writes to the file without appending the content.
	 * @param message the stacktrace.
	 */
	private void writeToFile(String message) {
		try {
			FileHandle handle = Gdx.files.local(filePath + "/" + fileName);
			// Write the stacktrace to disk
			handle.writeString(message, false);
		} catch (Exception e) {
			// Nothing much we can do about this - the game is over
			Gdx.app.error(TAG, "Exception thrown while storing stack trace", e);
		}
	}

	@Override
	public void cancelled() {
		forwardException();
	}

}
