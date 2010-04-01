package com.itog_lab.bugreporter;

import java.io.FileNotFoundException;
import java.lang.Thread.UncaughtExceptionHandler;
import android.content.Context;

public class MyUncaughtExceptionHandler implements UncaughtExceptionHandler {
	private UncaughtExceptionHandler mDefaultUEH;
	private Context context;
	public MyUncaughtExceptionHandler(Context c) {
		context = c;
		mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
	}
	
	public void uncaughtException(Thread th, Throwable t) {
		try {
			BugReporter bugReporter = new BugReporter(context);
			bugReporter.saveState(t);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		mDefaultUEH.uncaughtException(th, t);
	}
}
