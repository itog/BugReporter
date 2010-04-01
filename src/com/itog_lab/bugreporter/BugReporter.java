package com.itog_lab.bugreporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.AlertDialog;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;

public class BugReporter {
	private static final String BUG_REPORT_SERVER = "http://yourappid.appspot.com/bug";
	private static final String BUG_REPORT_FILE_NAME = "bugreport_sample.txt";
	private static File BUG_REPORT_FILE = null;

	static {
		String sdcard = Environment.getExternalStorageDirectory().getPath();
		String path = sdcard + File.separator + BUG_REPORT_FILE_NAME;
		BUG_REPORT_FILE = new File(path);
	}
	private static Context context;
	private static PackageInfo packageInfo;
	
	BugReporter(Context c) {
		context = c;
		try {
			// package information
			packageInfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
	public final void showBugReportDialogIfExist() {
		File file = BUG_REPORT_FILE;
		if (file != null & file.exists()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(R.string.bugreport_title);
			builder.setMessage(R.string.bugreport_description);
			builder.setNegativeButton(android.R.string.no, new OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					finish(dialog);
				}});
			builder.setPositiveButton(android.R.string.yes, new OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					postBugReportInBackground();
					dialog.dismiss();
				}});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}
	
	private static void postBugReportInBackground() {
		new Thread(new Runnable(){
			public void run() {
				postBugReport();
				final File file = BUG_REPORT_FILE;
				if (file != null && file.exists()) {
					file.delete();
				}
			}}).start();
	}
	
	private static void postBugReport() {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        String bug = getFileBody(BUG_REPORT_FILE);
        nvps.add(new BasicNameValuePair("dev", Build.DEVICE));
        nvps.add(new BasicNameValuePair("mod", Build.MODEL));
        nvps.add(new BasicNameValuePair("sdk", Build.VERSION.SDK));
        nvps.add(new BasicNameValuePair("ver", packageInfo.versionName));
        nvps.add(new BasicNameValuePair("bug", bug));
        try {
        	HttpPost httpPost = new HttpPost(BUG_REPORT_SERVER);
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            DefaultHttpClient httpClient = new DefaultHttpClient();
            httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	private static String getFileBody(File file) {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while((line = br.readLine()) != null) {
				sb.append(line).append("\r\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	private static void finish(DialogInterface dialog) {
		File file = BUG_REPORT_FILE;
		if (file.exists()) {
			file.delete();
		}
		dialog.dismiss();
	}

	public void saveState(Throwable e) throws FileNotFoundException {
//		StackTraceElement[] stacks = e.getStackTrace();
		File file = BUG_REPORT_FILE;
		PrintWriter pw = null;
		pw = new PrintWriter(new FileOutputStream(file));
		pw.println(getLogcat());
//		StringBuilder sb = new StringBuilder();
//		int len = stacks.length;
//		for (int i = 0; i < len; i++) {
//			StackTraceElement stack = stacks[i];
//			sb.setLength(0);
//			sb.append(stack.getClassName()).append("#");
//			sb.append(stack.getMethodName()).append(":");
//			sb.append(stack.getLineNumber());
//			pw.println(sb.toString());
//		}
		pw.close();
	}
	
	public String getLogcat() {
        StringBuilder log = new StringBuilder();
		try {
			ArrayList<String> commandLine = new ArrayList<String>();
			commandLine.add( "logcat");
			commandLine.add( "-d");
			commandLine.add( "-v");
			commandLine.add( "brief");
			commandLine.add( "BugReporter:W");
			commandLine.add( "*:E");
			Process process = Runtime.getRuntime().exec( commandLine.toArray( new String[commandLine.size()]));
			BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(process.getInputStream()), 1024);
			String line = bufferedReader.readLine();
			while (line != null) {
				log.append(line);
				log.append("\n");
				line = bufferedReader.readLine();
			}
		} catch ( IOException e) {
			;
		}
		return log.toString();
	}
}
