/*
 *  HelpActivity.java
 *
 *  Written by: Fintan Mahon 12101524
 *  Description: Displays relevant help screen
 *  
 *  Written on: Jan 2013
 *  
 * 
 *  
 */
package fm.gaa_scores.lite;

import fm.gaa_scores.lite.R;
import android.content.Intent;
import android.text.Html;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class HelpActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help_layout);

		// this class is called with an intent from the calling application
		// HELP_ID is used to pass in the calling screen so that
		// the relevant help screen is to be displayed
		Intent i = getIntent();
		int ihelp = i.getIntExtra("HELP_ID", 0);
		TextView textView = (TextView) findViewById(R.id.help_page);
		// the help screens are formatted using html
		// so they are loaded as html pages
		textView.setText(Html.fromHtml(getString(ihelp)));

	}
}