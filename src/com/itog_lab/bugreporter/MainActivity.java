package com.itog_lab.bugreporter;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
	static final String TAG = "BugReporter";
    Button exceptionButton;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(this));

        exceptionButton = (Button)findViewById(R.id.exception_button);
        exceptionButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
            int index = 5;
            String[] strs = new String[index];
            Log.w(TAG, "LogCat Message");
            String str = strs[index];//ここでIndexOutOfBoundsException
        }});
    }
    
	protected void onStart() {
		super.onStart();
		BugReporter bugReporter = new BugReporter(this);
		bugReporter.showBugReportDialogIfExist();
	}
}
