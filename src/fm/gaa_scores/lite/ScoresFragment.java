/*
 *  MatchRecordFragment.java
 *
 *  Written by: Fintan Mahon 12101524
 *  
 *  Description: GUI to get input and display output for
 *  1. match timer
 *  2. match score
 *  3. match statistics
 *  
 * store data to database tables and pass relevant details into MatchReviewFragment
 *  
 *  Written on: Jan 2013
 *  
 * 
 */
package fm.gaa_scores.lite;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fm.gaa_scores.lite.R;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class ScoresFragment extends Fragment {

	// declare and initialise variables
	public int minsPerHalf = 30;
	private int homeGoals = 0, homePoints = 0, oppGoals = 0, oppPoints = 0;
	private Timer timer;
	private TextView tStartTime, tTimeGone, tTimeToGo, tTimeLeft;
	private TextView tHomeTotal, tUpDownDrawText, tHomeDifference, tOppTotal;
	private TextView tStats;
	private TextView tOurTeam, tOppTeam;
	private Button bResetAll;
	private Button bStartStop, bDecreaseTime, bIncreaseTime;
	private Button bDecHomeGoals, bHomeGoals, bHomePoints, bDecHomePoints;
	private Button bDecOppGoals, bOppGoals, bOppPoints, bDecOppPoints;
	private Button bShotHome, bShotOpp, bMinsPerHalf;
	public Button bPeriod;
	private Button bUndo, btweetScore;
	private Button btextScore;
	private int statsButton, txtButton, periodInt = 0;
	private String stats1, stats2, team, phone, periodStr;
	private EditText tLoc, input;
	private Handler h = new Handler();
	private long starttime = 0;
	private Date currentDate;
	private SimpleDateFormat sdf, sdftime;
	private AlertDialog alertshot = null, alertpitch = null;
	private String[] minsList, tList;
	private boolean[] tListChecked;
	private String[] undoString = new String[6];
	private HashMap<String, Integer> playerIDLookUp = new HashMap<String, Integer>();
	// setup uri to read panel from database
	private ArrayList<String> undoList = new ArrayList();
	private ArrayList<String> txtList = new ArrayList<String>();
	private ArrayList txtListOut = new ArrayList<String>();
	private long rowId;	private Context context;
	private Uri allTitles = TeamContentProvider.CONTENT_URI_2;


	@Override
	// start main method to display screen
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.scores_layout, container, false);
		// register tag name
		String myTag = getTag();
		((Startup) getActivity()).setTagFragmentScore(myTag);
		this.setHasOptionsMenu(true);
		context = getActivity();

		// open sharedpreferences file to read in saved persisted data on
		// startup

		SharedPreferences sharedPref = getActivity().getSharedPreferences(
				"team_stats_record_data", Context.MODE_PRIVATE);
		phone = sharedPref.getString("PHONE", "");


		// get the tag name of this Fragment and pass it up to the parent
		// activity MatchApplication so that this Fragment may be accessed
		// by other fragments through using a reference created from tag name

		// set up text buttons edittexts etc.
		tStartTime = (TextView) v.findViewById(R.id.start_time);
		tTimeGone = (TextView) v.findViewById(R.id.time_gone);
		tTimeToGo = (TextView) v.findViewById(R.id.time_to_go);
		tTimeLeft = (TextView) v.findViewById(R.id.time_left);
		tHomeTotal = (TextView) v.findViewById(R.id.home_total);
		tUpDownDrawText = (TextView) v.findViewById(R.id.up_down_draw_text);
		tHomeDifference = (TextView) v.findViewById(R.id.home_difference);
		tOppTotal = (TextView) v.findViewById(R.id.opp_total);
		tStats = (TextView) v.findViewById(R.id.textViewStats);
		// tFreeReason = (TextView) v.findViewById(R.id.textViewFreeType);
		// tFreePlayer = (TextView) v.findViewById(R.id.textViewFreePlayer);
		// tFreePosn = (TextView) v.findViewById(R.id.textViewFreePosn);
		// tPuckOutReason = (TextView) v.findViewById(R.id.textViewPuckType);
		// tPuckOutPlayer = (TextView) v.findViewById(R.id.textViewPuckPlayer);
		tOurTeam = (TextView) v.findViewById(R.id.ourTeam);
		tOppTeam = (TextView) v.findViewById(R.id.oppTeam);
		tLoc = (EditText) v.findViewById(R.id.etLocn);

		bStartStop = (Button) v.findViewById(R.id.start_stop_timer);
		bDecreaseTime = (Button) v.findViewById(R.id.decrease_timer);
		bIncreaseTime = (Button) v.findViewById(R.id.increase_timer);

		bDecHomeGoals = (Button) v.findViewById(R.id.dec_home_goals);
		bHomeGoals = (Button) v.findViewById(R.id.home_goals);
		bHomePoints = (Button) v.findViewById(R.id.home_points);
		bDecHomePoints = (Button) v.findViewById(R.id.dec_home_points);
		bDecOppGoals = (Button) v.findViewById(R.id.dec_opp_goals);
		bOppGoals = (Button) v.findViewById(R.id.opp_goals);
		bOppPoints = (Button) v.findViewById(R.id.opp_points);
		bDecOppPoints = (Button) v.findViewById(R.id.dec_opp_points);
		bResetAll = (Button) v.findViewById(R.id.reset_all);
		bUndo = (Button) v.findViewById(R.id.buttonUndo);
		bUndo.setOnClickListener(undoOnClickListener);

		// //////////////////////set Team Names//////////////////////////
		// use persisted data if it exists else use default data
		// get team names first

		tOurTeam.setText(sharedPref.getString("OURTEAM", "OWN TEAM"));
		tOppTeam.setText(sharedPref.getString("OPPTEAM", "OPPOSITION"));
		if (!sharedPref.getString("LOCATION", tLoc.toString()).equals(""))
			tLoc.setText(sharedPref.getString("LOCATION", ""));
		
		tOurTeam.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				try {
					((Startup) getActivity()).getFragmentReview().setTeamNames(
							tOurTeam.getText().toString(),
							tOppTeam.getText().toString());
				} catch (Exception ex) {
					//
				}
			}
		});
		
		tOppTeam.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				try {
					((Startup) getActivity()).getFragmentReview().setTeamNames(
							tOurTeam.getText().toString(),
							tOppTeam.getText().toString());
				} catch (Exception ex) {
					//
				}
			}
		});

		// ///////////////////MINUTES PER HALF SECTION////////////////////////
		bMinsPerHalf = (Button) v.findViewById(R.id.mins_per_half);
		// set mins per half from saved value if it exists, else default to 30
		bMinsPerHalf.setText(String.valueOf(sharedPref
				.getInt("MINSPERHALF", 30)));
		minsPerHalf = sharedPref.getInt("MINSPERHALF", 30);
		// set click listener for mins per half button
		bMinsPerHalf.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View w) {
				Button b = (Button) w;
				// read list of allowable times from array in assets and put in
				// adapter to display in alertdialog for selection
				minsList = getResources().getStringArray(R.array.minsPerHalf);
				ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(
						getActivity(), R.layout.single_row_layout, minsList);
				new AlertDialog.Builder(getActivity())
						.setTitle("set minutes per half")
						.setAdapter(adapter1,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// convert string input to integer
										minsPerHalf = Integer
												.valueOf(minsList[which]);
										// put new value on button
										bMinsPerHalf.setText(minsList[which]);
										dialog.dismiss();
									}
								}).create().show();
			}
		});

		// //////////////////SET TIME PERIOD/////////////////////////////////
		bPeriod = (Button) v.findViewById(R.id.bPeriod);
		bPeriod.setOnClickListener(periodClickListener);

		// ///////////////////////////TIMER SETUP///////////////////////////
		// retrieve saved value if its there
		tStartTime.setText(sharedPref
				.getString("STARTTEXT", "start time 00:00"));
		tTimeLeft.setText(sharedPref.getString("TIMELEFT", "time left"));

		starttime = sharedPref.getLong("STARTTIME", 0);
		String[] str = new String[2];// stores display text for 1st/2nd half
		// set text on screen according to whether in first half or 2nd half
		// and whether timer is running or not
		if ((sharedPref.getString("TIMERBUTTON", "start").equals("stop"))
				&& (sharedPref.getString("PERIOD", "1st half").equals("1st half"))) {
			str = settTimer("start", "1st half");
			bStartStop.setText(str[0]);
			bPeriod.setText(str[1]);
		} else if ((sharedPref.getString("TIMERBUTTON", "start").equals("stop"))
				&& (sharedPref.getString("PERIOD", "1st half").equals("2nd half"))) {
			str = settTimer("start", "2nd half");
			bStartStop.setText(str[0]);
			bPeriod.setText(str[1]);

		} else if ((sharedPref.getString("TIMERBUTTON", "start").equals("stop"))
				&& (sharedPref.getString("PERIOD", "1st half").equals("ET 1st"))) {
			periodInt = 2;
			str = settTimer("start", "ET 1st");
			bStartStop.setText(str[0]);
			bPeriod.setText(str[1]);

		} else if ((sharedPref.getString("TIMERBUTTON", "start").equals("stop"))
				&& (sharedPref.getString("PERIOD", "1st half").equals("ET 2nd"))) {
			periodInt = 2;
			str = settTimer("start", "ET 2nd");
			bStartStop.setText(str[0]);
			bPeriod.setText(str[1]);

		} else if (sharedPref.getString("TIMERBUTTON", "start").equals("start")) {
			str = settTimer("stop", "2nd half");
			bStartStop.setText(str[0]);
			bPeriod.setText(sharedPref.getString("PERIOD", "1st half"));
		}

		// clicklistener for start/stop button toggle
		bStartStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Button b = (Button) v;
				String sPeriod;
				String locn = "";
				if (tLoc.getText().length() > 0) {
					locn = tLoc.getText() + ". ";
				}
				String[] str = new String[2];
				// write start/time to database
				str = settTimer((String) b.getText(),
						(String) bPeriod.getText());

				sdf = new SimpleDateFormat("HH:mm   dd-MM-yy");
				ContentValues values = new ContentValues();
				if (b.getText().equals("start")) {
					// add to database
					values.put("line", locn + bPeriod.getText() + " start: "
							+ sdf.format(starttime) + " " + tOurTeam.getText()
							+ " v. " + tOppTeam.getText());

				} else {
					if (bPeriod.getText().equals("1st half")) {
						sPeriod = "half time score: ";
					} else if (bPeriod.getText().equals("2nd half")) {
						sPeriod = "full time score: ";
					} else if (bPeriod.getText().equals("ET 1st")) {
						sPeriod = "score after 1st half extra time: ";
					} else {
						sPeriod = "score after 2nd half extra time: ";
					}

					values.put(
							"line",
							locn
									+ sPeriod
									+ tOurTeam.getText()
									+ " "
									+ (bHomeGoals.getText().equals("+") ? "0"
											: bHomeGoals.getText())
									+ "-"
									+ (bHomePoints.getText().equals("+") ? "0"
											: bHomePoints.getText())
									+ tHomeTotal.getText()
									+ "  "
									+ tOppTeam.getText()
									+ " "
									+ (bOppGoals.getText().equals("+") ? "0"
											: bOppGoals.getText())
									+ "-"
									+ (bOppPoints.getText().equals("+") ? "0"
											: bOppPoints.getText())
									+ tOppTotal.getText());
				}
				getActivity().getContentResolver().insert(
						TeamContentProvider.CONTENT_URI_2, values);
				updateStats();
				b.setText(str[0]);
				bPeriod.setText(str[1]);
			}
		});

		// clicklistener for increment time button
		// if clicked add a minute to the timer be subtracting a minute from the
		// timer starttime. Update starttime text too
		bIncreaseTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (starttime != 0) {
					starttime = starttime - 30000;
					currentDate = new Date(starttime);
					sdf = new SimpleDateFormat("HH:mm:ss   dd-MM-yy");
					tStartTime.setText("Start Time: " + sdf.format(currentDate));
				}
			}
		});

		// clicklistener for decrement time button
		// if clicked take a minute to the timer be subtracting a minute from
		// the
		// timer starttime. Update starttime text too
		bDecreaseTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ((starttime != 0)
						&& (System.currentTimeMillis() - starttime > 30000)) {
					starttime = starttime + 30000;
					currentDate = new Date(starttime);
					sdf = new SimpleDateFormat("HH:mm:ss   dd-MM-yyyy");
					tStartTime.setText("Start Time: " + sdf.format(currentDate));
				}
			}
		});

		// /////////////////////////////SCORE///////////////////////////////////////
		// one clickListener handles all input from score buttons
		bDecHomeGoals.setOnClickListener(scoreAddClickListener);
		bHomeGoals.setOnClickListener(scoreAddClickListener);
		bHomePoints.setOnClickListener(scoreAddClickListener);
		bDecHomePoints.setOnClickListener(scoreAddClickListener);
		bDecOppGoals.setOnClickListener(scoreAddClickListener);
		bOppGoals.setOnClickListener(scoreAddClickListener);
		bOppPoints.setOnClickListener(scoreAddClickListener);
		bDecOppPoints.setOnClickListener(scoreAddClickListener);

		// ///////HANDLE SCORES FROM PERSISTED SHARED PREFERENCES////
		homeGoals = sharedPref.getInt("HOMEGOALS", 0);
		homePoints = sharedPref.getInt("HOMEPOINTS", 0);
		oppGoals = sharedPref.getInt("OPPGOALS", 0);
		oppPoints = sharedPref.getInt("OPPPOINTS", 0);

		if (homeGoals + homePoints + oppGoals + oppPoints > 0) {
			bHomeGoals.setText(String.valueOf(homeGoals));
			bHomePoints.setText(String.valueOf(homePoints));
			bOppGoals.setText(String.valueOf(oppGoals));
			bOppPoints.setText(String.valueOf(oppPoints));
			setTotals();
		}

		// stats button click listener just diplays message to longpress
		bResetAll.setOnClickListener(resetClickListener);

		// reset stats button click listener
		// set all stats back to zero on REVIEW fragment screen
		bResetAll.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				resetStats();
				resetTime();
				resetScore();
				tLoc.setText("");
				v.playSoundEffect(SoundEffectConstants.CLICK);
				return true;
			}
		});

		// ////////////////////SHOT STATS SETUP///////////////////
		tStats = (TextView) v.findViewById(R.id.textViewStats);
		bShotHome = (Button) v.findViewById(R.id.buttonShotHome);
		bShotHome.setOnClickListener(statsClickListener);
		bShotOpp = (Button) v.findViewById(R.id.buttonShotOpp);
		bShotOpp.setOnClickListener(statsClickListener);

		// populate undolist
		String[] projection = { TeamContentProvider.STATSID,
				TeamContentProvider.STATSLINE };
		CursorLoader cL;
		cL = new CursorLoader(getActivity(), allTitles, projection, null, null,
				TeamContentProvider.STATSID);
		Cursor c1 = cL.loadInBackground();
		if (c1.getCount() > 0) {
			c1.moveToFirst();
			do {
				// read in player nicknames
				undoList.add(c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSLINE)));
				// insert players into positions
			} while (c1.moveToNext());
		} else {
			tStats.setText("");
		}
		String undo1 = "", undo2 = "", undo3 = "", undo4 = "";
		if (c1.getCount() >= 4) {
			undo1 = undoList.get(c1.getCount() - 4);
			undo2 = undoList.get(c1.getCount() - 3);
			undo3 = undoList.get(c1.getCount() - 2);
			undo4 = undoList.get(c1.getCount() - 1);
			tStats.setText(undo1 + "\n" + undo2 + "\n" + undo3 + "\n" + undo4);
		} else if (c1.getCount() == 3) {
			undo1 = undoList.get(c1.getCount() - 3);
			undo2 = undoList.get(c1.getCount() - 2);
			undo3 = undoList.get(c1.getCount() - 1);
			tStats.setText(undo1 + "\n" + undo2 + "\n" + undo3);
		} else if (c1.getCount() == 2) {
			undo1 = undoList.get(c1.getCount() - 2);
			undo2 = undoList.get(c1.getCount() - 1);
			tStats.setText(undo1 + "\n" + undo2);
		} else if (c1.getCount() == 1) {
			undo1 = undoList.get(c1.getCount() - 1);
			tStats.setText(undo1);
		} // call update in review
		c1.close();

		btweetScore = (Button) v.findViewById(R.id.bTweetScore);
		btweetScore.setOnClickListener(tweetScoreListener);
		
		btextScore = (Button) v.findViewById(R.id.bTextScore);
		btextScore.setOnClickListener(tweetScoreListener);
		
		return v;
	}

	// ********************************************************************//
	// ///////////////////////////END OF ONCREATE SECTION //////////////////
	// ********************************************************************//

	@Override
	public void onPause() {
		// Save/persist data to be used on reopen
		super.onPause(); // Always call the superclass method first
		SharedPreferences sharedPref = getActivity().getSharedPreferences(
				"team_stats_record_data", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt("MINSPERHALF", minsPerHalf);
		editor.putLong("STARTTIME", starttime);
		editor.putString("PERIOD", bPeriod.getText().toString());
		editor.putInt("HOMEGOALS", homeGoals);
		editor.putInt("HOMEPOINTS", homePoints);
		editor.putInt("OPPGOALS", oppGoals);
		editor.putInt("OPPPOINTS", oppPoints);
		editor.putString("LOCATION", tLoc.getText().toString());
		editor.putString("TIMELEFT", tTimeLeft.getText().toString());
		editor.putString("TIMERBUTTON", bStartStop.getText().toString());
		editor.putString("OURTEAM", tOurTeam.getText().toString());
		editor.putString("OPPTEAM", tOppTeam.getText().toString());
		editor.putString("STARTTEXT", tStartTime.getText().toString());
		editor.putString("PHONE", phone);
		editor.commit();
	}
	
	

	private String getScore() {
		// String str = getTime().equals("") ? "" : getTime() + "mins ";
		String str = "";
		String str1 = bPeriod.getText().toString();
		String str2 = tStartTime.getText().toString();
		if (getTime().equals("")) {
			if (str2.contains("Start T")) {
				str = "";
			} else if ((str2.contains("1st Half"))&&(!str2.contains("Extra"))) {
				str = "half time-";
			} else if ((str2.contains("2nd Half"))&&(!str2.contains("Extra"))) {
				str = "full time-";

			} else if (str2.contains("Time-1")) {
				str = "extra time, half time-";
			} else if (str2.contains("Time-2")) {
				str = "extra fime, full time-";
			}

		} else {
			str = getTime() + "mins " + bPeriod.getText() + "-";
		}

		String comment = tLoc.getText().length() <= 1 ? "" : (tLoc.getText()
				.toString() + ". ");
		return comment
				+ str
				+ tOurTeam.getText()
				+ ":"
				+ (bHomeGoals.getText().equals("+") ? "0" : bHomeGoals
						.getText())
				+ "-"
				+ (bHomePoints.getText().equals("+") ? "0" : bHomePoints
						.getText())
				+ tHomeTotal.getText()
				+ "  "
				+ tOppTeam.getText()
				+ ":"
				+ (bOppGoals.getText().equals("+") ? "0" : bOppGoals.getText())
				+ "-"
				+ (bOppPoints.getText().equals("+") ? "0" : bOppPoints
						.getText()) + tOppTotal.getText() + ". ";
	}
	
	OnClickListener tweetScoreListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			txtButton = ((Button) v).getId();

			switch (txtButton) {
			case R.id.bTweetScore:
				try {
					Intent shareIntent = findTwitterClient();
					shareIntent.putExtra(Intent.EXTRA_TEXT, getScore());
					startActivity(Intent.createChooser(shareIntent, "Share"));
				} catch (Exception ex) {
					Log.e("Error in Tweet", ex.toString());
					Toast.makeText(
							getActivity(),
							"Can't find twitter client\n"
									+ "Please install Twitter App\nand login to Twitter",
							Toast.LENGTH_LONG).show();
				}
				break;
			case R.id.bTextScore:
				try {
					Intent intentText = new Intent(Intent.ACTION_VIEW);
					intentText.setType("vnd.android-dir/mms-sms");
					intentText.putExtra("sms_body", getScore());
					intentText.setData(Uri.parse("sms: " + phone));
					startActivity(intentText);
				} catch (Exception ex) {
					Log.e("Error in Text", ex.toString());
					Toast.makeText(
							getActivity(),
							"Unable to send text message",
							Toast.LENGTH_LONG).show();
				}
				break;
			}
		}
	};

	public Intent findTwitterClient() {
		final String[] twitterApps = {
				// package // name - nb installs (thousands)
				"com.twitter.android", // official - 10 000
				"com.twidroid", // twidroid - 5 000
				"com.handmark.tweetcaster", // Tweecaster - 5 000
				"com.thedeck.android" }; // TweetDeck - 5 000 };
		Intent tweetIntent = new Intent();
		tweetIntent.setType("text/plain");
		final PackageManager packageManager = this.context.getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(
				tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);

		for (int i = 0; i < twitterApps.length; i++) {
			for (ResolveInfo resolveInfo : list) {
				String p = resolveInfo.activityInfo.packageName;
				if (p != null && p.startsWith(twitterApps[i])) {
					tweetIntent.setPackage(p);
					return tweetIntent;
				}
			}
		}
		return null;
	}

	
	// for reset buttons diplay message to long click, won't work with ordinary
	// click
	OnClickListener resetClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// get reference to REVIEW fragment from parent activity
			// MatchApplication and use reference to execute resetStats
			// method in REVIEW fragment which will reset stats there to 0
			Toast.makeText(getActivity(), "Long Press to Reset",
					Toast.LENGTH_SHORT).show();
		}
	};

	private void resetTime() {
		if (timer != null) {
			timer.cancel();
			timer.purge();
			h.removeCallbacks(run);
		}
		tTimeLeft.setText("time left");
		bStartStop.setText("start");
		tTimeGone.setText("00:00");
		tTimeToGo.setText("00:00");
		tStartTime.setText("Start Time: 00:00");
		bPeriod.setText(getResources().getStringArray(R.array.periodShort)[0]);
		starttime = 0;
		periodInt = 0;
	}

	private void resetScore() {
		// reset score in this fragment and also on REVIEW fragment
		bHomeGoals.setText("+");
		homeGoals = homePoints = oppGoals = oppPoints = 0;
		// reset score in REVIEW fragment
		// get reference to REVIEW fragment from parent activity
		// MatchApplication and use reference to execute setHomeGoals
		// method in REVIEW fragment which will reset score there to 0
		((Startup) getActivity()).getFragmentReview().settHomeGoals(0);
		bHomePoints.setText("+");
		((Startup) getActivity()).getFragmentReview().settHomePoints(0);
		bOppGoals.setText("+");
		((Startup) getActivity()).getFragmentReview().settOppGoals(0);
		bOppPoints.setText("+");
		((Startup) getActivity()).getFragmentReview().settOppPoints(0);
		tHomeTotal.setText("(0)");
		tOppTotal.setText("(0)");
		tUpDownDrawText.setText("drawn game. ");
		tHomeDifference.setText(" ");
	}

	private void resetStats() {
		// get reference to REVIEW fragment from parent activity
		// MatchApplication and use reference to execute resetStats
		// method in REVIEW fragment which will reset stats there to 0

		Toast.makeText(getActivity(), "Stats Reset", Toast.LENGTH_SHORT).show();
		// delete stats in database
		getActivity().getContentResolver().delete(
				TeamContentProvider.CONTENT_URI_2, null, null);
		String[] projection = { TeamContentProvider.STATSID,
				TeamContentProvider.STATSLINE };
		CursorLoader cL = new CursorLoader(getActivity(), allTitles, projection, null, null,
				TeamContentProvider.STATSID);
		((Startup) getActivity()).getFragmentReview().resetStats();
		updateStats();
		tStats.setText("");
	}

	// Run Match timer section. Set text strings and timer based on 4
	// possibilities:
	// 1. ready to start first half
	// 2. first half running
	// 3. first half ended ready to start second half
	// 4. second half running
	private String[] settTimer(String bStr, String bHalf) {
		String[] str = new String[2];
		sdf = new SimpleDateFormat("HH:mm:ss   dd-MM-yyyy");
		sdftime = new SimpleDateFormat("HH:mm:ss");

		if (bStr.equals("start") && bHalf.equals("2nd half")) {
			// 3. first half ended ready to start second half
			if (starttime == 0)
				starttime = System.currentTimeMillis();
			currentDate = new Date(starttime);
			if (periodInt < 2) {
				tStartTime
						.setText("2nd Half Start: " + sdf.format(currentDate));
			} else {
				tStartTime.setText("Extra Time-2nd Half Start: "
						+ sdf.format(currentDate));
			}
			timer = new Timer();
			tTimeLeft.setText("time left");//new
			h.postDelayed(run, 0);
			str[0] = "stop";
			str[1] = "2nd half";
			bPeriod.setOnClickListener(nullPeriodClickListener);
			return str;

		} else if (bStr.equals("start") && bHalf.equals("ET 2nd")) {
			// 3. first half ET ended ready to start second half
			if (starttime == 0)
				starttime = System.currentTimeMillis();
			currentDate = new Date(starttime);
			if (periodInt < 2) {
				tStartTime
						.setText("2nd Half Start: " + sdf.format(currentDate));
			} else {
				tStartTime.setText("Extra Time-2nd Half Start: "
						+ sdf.format(currentDate));
			}
			timer = new Timer();
			tTimeLeft.setText("time left");//new
			h.postDelayed(run, 0);
			str[0] = "stop";
			str[1] = "ET 2nd";
			bPeriod.setOnClickListener(nullPeriodClickListener);
			return str;

		} else if ((bStr.equals("stop") && (bHalf.equals("2nd half") || bHalf
				.equals("ET 2nd")))) {
			// 4. second half running
			if (timer != null) {
				timer.cancel();
				timer.purge();
			}
			h.removeCallbacks(run);
			starttime = 0;
			str[0] = "start";
			str[1] = "1st half";
			bPeriod.setOnClickListener(periodClickListener);
			return str;

		} else if (bStr.equals("stop") && bHalf.equals("1st half")) {
			// 2. first half running
			if (timer != null) {
				timer.cancel();
				timer.purge();
			}
			h.removeCallbacks(run);
			starttime = 0;
			str[0] = "start";
			str[1] = "2nd half";
			bPeriod.setOnClickListener(periodClickListener);
			return str;

		} else if (bStr.equals("stop") && bHalf.equals("ET 1st")) {
			// 2. first half running
			if (timer != null) {
				timer.cancel();
				timer.purge();
			}
			h.removeCallbacks(run);
			starttime = 0;
			str[0] = "start";
			str[1] = "ET 2nd";
			bPeriod.setOnClickListener(periodClickListener);
			return str;

		} else {
			// 1. ready to start first half
			if (starttime == 0)
				starttime = System.currentTimeMillis();
			currentDate = new Date(starttime);
			sdf = new SimpleDateFormat("HH:mm:ss   dd-MM-yyyy");
			if (periodInt < 2) {
				tStartTime
						.setText("1st Half Start: " + sdf.format(currentDate));
			} else {
				tStartTime.setText("Extra Time-1st Half Start: "
						+ sdf.format(currentDate));
			}
			timer = new Timer();
			tTimeLeft.setText("time left");//new

			h.postDelayed(run, 0);
			str[0] = "stop";
			if (bHalf.equals("1st half")) {
				str[1] = "1st half";
			} else {
				str[1] = "ET 1st";
			}
			bPeriod.setOnClickListener(nullPeriodClickListener);
			return str;
		}
	}

	// //////////////////////////////////////////////////////////////////////
	// method to update score and update shots data in review scrreen
	public void updateStats(int button, int count) {
		switch (button) {
		case R.id.buttonShotHome:
			team = tOurTeam.getText().toString();
			// for home team commit
			// WRITE TO REVIEW PAGE///////////////////////////////////
			if (stats1.equals("goal")) {
				// increment goal counter
				if (homeGoals + count >= 0) {
					homeGoals = homeGoals + count;
					bHomeGoals.setText(String.valueOf(homeGoals));
					// update totals
					setTotals();
					// increment score in REVIEW fragment
					((Startup) getActivity()).getFragmentReview()
							.settHomeGoals(homeGoals);
					// change display from + to 0 if first score
					if (bHomePoints.getText().equals("+"))
						bHomePoints.setText("0");
					// remind user score is updated in case they try and do it
					// manually
					Toast.makeText(getActivity(), "Score Updated",
							Toast.LENGTH_SHORT).show();
				}
				// update goal counter display in review page
				((Startup) getActivity()).getFragmentReview()
						.addtShotGoalsHome(count);
				// increment goal from play counter in review page
				// unless score was from free/45/65/penalty
				if ((!stats2.equals("from free"))
						&& (!stats2.equals("from 45/65"))
						&& (!stats2.equals("from penalty"))
						&& (!stats2.equals("from sideline"))) {
					((Startup) getActivity()).getFragmentReview()
							.addtShotGoalsPlayHome(count);
				}
			} else if (stats1.equals("point")) {
				// increment points counter
				if (homePoints + count >= 0) {
					homePoints = homePoints + count;
					bHomePoints.setText(String.valueOf(homePoints));
					// update totals
					setTotals();
					// increment score in REVIEW fragment
					((Startup) getActivity()).getFragmentReview()
							.settHomePoints(homePoints);
					// change display from + to 0 if first score
					if (bHomeGoals.getText().equals("+"))
						bHomeGoals.setText("0");
					// remind user score is updated in case they try and do it
					// manually
					Toast.makeText(getActivity(), "Score Updated",
							Toast.LENGTH_SHORT).show();
				}
				// update points counter display in review page
				((Startup) getActivity()).getFragmentReview()
						.addtShotPointsHome(count);
				// increment goal from play counter in review page
				// unless score was from free/45/65
				if ((!stats2.equals("from free"))
						&& (!stats2.equals("from 45/65"))
						&& (!stats2.equals("from penalty"))
						&& (!stats2.equals("from sideline"))) {
					((Startup) getActivity()).getFragmentReview()
							.addtShotPointsPlayHome(count);
				}
			} else if (stats1.equals("wide")) {
				// increment counter in review page
				((Startup) getActivity()).getFragmentReview()
						.addtShotWidesHome(count);
			} else if (stats1.equals("saved")) {
				// increment counter in review page
				((Startup) getActivity()).getFragmentReview()
						.addtShotSavedHome(count);
			} else if (stats1.equals("off posts")) {
				// increment counter in review page
				((Startup) getActivity()).getFragmentReview()
						.addtShotPostsHome(count);
			} else if (stats1.equals("free/pen conceded")) {
				// increment counter in review page
				((Startup) getActivity()).getFragmentReview()
						.addFreeConcededHome(count);
			} else if (stats1.equals("own puckout won")) {
				// increment counter in review page
				((Startup) getActivity()).getFragmentReview()
						.addPuckWonCleanHome(count);
			} else if (stats1.equals("own puckout lost")) {
				// increment counter in review page
				((Startup) getActivity()).getFragmentReview()
						.addPuckLostCleanHome(count);
			}
			break;
		case R.id.buttonShotOpp:
			team = tOppTeam.getText().toString();
			// for opposition team
			// WRITE TO REVIEW PAGE///////////////////////////////////
			if (stats1.equals("goal")) {
				// increment goal counter
				if (oppGoals + count >= 0) {
					oppGoals = oppGoals + count;
					bOppGoals.setText(String.valueOf(oppGoals));
					// update totals
					setTotals();
					// increment score in REVIEW fragment
					((Startup) getActivity()).getFragmentReview().settOppGoals(
							oppGoals);
					if (bOppPoints.getText().equals("+"))
						bOppPoints.setText("0");
					Toast.makeText(getActivity(), "Score Updated",
							Toast.LENGTH_SHORT).show();
				}
				// increment goal counter in review page
				((Startup) getActivity()).getFragmentReview().addtShotGoalsOpp(
						count);
				// increment goal from play counter in review page
				// unless scrore was from free/45/65
				if ((!stats2.equals("from free"))
						&& (!stats2.equals("from penalty"))
						&& (!stats2.equals("from sideline"))
						&& (!stats2.equals("from 45/65"))) {
					((Startup) getActivity()).getFragmentReview()
							.addtShotGoalsPlayOpp(count);
				}
			} else if (stats1.equals("point")) {
				// increment points counter
				if (oppPoints + count >= 0) {
					oppPoints = oppPoints + count;
					bOppPoints.setText(String.valueOf(oppPoints));
					// update totals
					setTotals();
					// increment score in REVIEW fragment
					((Startup) getActivity()).getFragmentReview()
							.settOppPoints(oppPoints);
					if (bOppGoals.getText().equals("+"))
						bOppGoals.setText("0");
					Toast.makeText(getActivity(), "Score Updated",
							Toast.LENGTH_SHORT).show();
				}
				// increment goal counter in review page
				((Startup) getActivity()).getFragmentReview()
						.addtShotPointsOpp(count);
				// increment goal from play counter in review page
				// unless scrore was from free/45/65
				if ((!stats2.equals("from free"))
						&& (!stats2.equals("from penalty"))
						&& (!stats2.equals("from sideline"))
						&& (!stats2.equals("45/65"))) {
					((Startup) getActivity()).getFragmentReview()
							.addtShotPointsPlayOpp(count);
				}
			} else if (stats1.equals("wide")) {
				// increment counter in review page
				((Startup) getActivity()).getFragmentReview().addtShotWidesOpp(
						count);
			} else if (stats1.equals("saved")) {
				// increment counter in review page
				((Startup) getActivity()).getFragmentReview().addtShotSavedOpp(
						count);
			} else if (stats1.equals("off posts")) {
				// increment counter in review page
				((Startup) getActivity()).getFragmentReview().addtShotPostsOpp(
						count);
			} else if (stats1.equals("free/pen conceded")) {
				// increment counter in review page
				((Startup) getActivity()).getFragmentReview()
						.addFreeConcededOpp(count);
			} else if (stats1.equals("own puckout won")) {
				// increment counter in review page
				((Startup) getActivity()).getFragmentReview()
						.addPuckWonCleanOpp(count);
			} else if (stats1.equals("own puckout lost")) {
				// increment counter in review page
				((Startup) getActivity()).getFragmentReview()
						.addPuckLostCleanOpp(count);
			}
			break;
		}
		// add to database
		if (!(stats1.equals("") && stats2.equals(""))) {
			ContentValues values = new ContentValues();
			if (starttime > 10) {
				values.put("line", getTime() + "mins " + bPeriod.getText()
						+ " " + team + " " + stats1 + " " + stats2 );
			} else {
				values.put("line", team + " " + stats1 + " " + stats2);
			}
			getActivity().getContentResolver().insert(
					TeamContentProvider.CONTENT_URI_2, values);
		}
		// update display
		updateStats();
	}

	public void updateStats() {
		// load panel from database and assign to arraylist
		Uri allTitles = TeamContentProvider.CONTENT_URI_2;
		String[] projection = { TeamContentProvider.STATSID,
				TeamContentProvider.STATSLINE };
		CursorLoader cL;
		cL = new CursorLoader(getActivity(), allTitles, projection, null, null,
				TeamContentProvider.STATSID);
		Cursor c1 = cL.loadInBackground();
		undoList.clear();
		if (c1.getCount() > 0) {
			c1.moveToFirst();
			do {
				// read in player nicknames
				undoList.add(c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSLINE)));
				// insert players into positions
			} while (c1.moveToNext());
		} else {
			tStats.setText("");
		}
		String undo1 = "", undo2 = "", undo3 = "", undo4 = "";
		if (c1.getCount() >= 4) {
			undo1 = undoList.get(c1.getCount() - 4);
			undo2 = undoList.get(c1.getCount() - 3);
			undo3 = undoList.get(c1.getCount() - 2);
			undo4 = undoList.get(c1.getCount() - 1);
			tStats.setText(undo1 + "\n" + undo2 + "\n" + undo3 + "\n" + undo4);
		} else if (c1.getCount() == 3) {
			undo1 = undoList.get(c1.getCount() - 3);
			undo2 = undoList.get(c1.getCount() - 2);
			undo3 = undoList.get(c1.getCount() - 1);
			tStats.setText(undo1 + "\n" + undo2 + "\n" + undo3);
		} else if (c1.getCount() == 2) {
			undo1 = undoList.get(c1.getCount() - 2);
			undo2 = undoList.get(c1.getCount() - 1);
			tStats.setText(undo1 + "\n" + undo2);
		} else if (c1.getCount() == 1) {
			undo1 = undoList.get(c1.getCount() - 1);
			tStats.setText(undo1);
		}
		// call update in review
		c1.close();
		((Startup) getActivity()).getFragmentReview().updateListView();
	}

	
	// **********************************************************************//
	/*-------------------DIALOG FOR STATS INPUT*---------------------------*///
	// handles button clicks for shot / free / puckout ///
	// **********************************************************************//
	OnClickListener statsClickListener = new OnClickListener() {

		@Override
		public void onClick(View w) {
			// clear default values from input/output textfields
			stats1 = "";
			stats2 = "";
			// get team name from button
			int tempButton = ((Button) w).getId();
		

			// use SHOT_FREE_PUCK_OUT to store which button was pressed
			statsButton = ((Button) w).getId();

			// throw up stats input screen layout
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View vv = inflater.inflate(R.layout.stats_layout, null);
			// ///////////
			AlertDialog.Builder builder;

			builder = new AlertDialog.Builder(getActivity()).setView(vv)
			// ok button just closes the dialog
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									// light up the save button

									updateStats(statsButton, 1);

									dialog.dismiss();
								}
							});

			// 7 choices for shots
			RadioButton[] rbrshot = new RadioButton[8];
			for (int i = 0; i < 8; i++) {
				rbrshot[i] = (RadioButton) vv.findViewById(getResources()
						.getIdentifier(
								"radio_shot_r" + String.format("%02d", i),
								"id", "fm.gaa_scores.lite"));
				rbrshot[i].setOnClickListener(getStats1ClickListener);
			}

			// 4 options for shot type
			RadioButton[] rbtShot = new RadioButton[8];
			for (int i = 0; i < 8; i++) {
				rbtShot[i] = (RadioButton) vv.findViewById(getResources()
						.getIdentifier(
								"radio_shot_t" + String.format("%02d", i),
								"id", "fm.gaa_scores.lite"));
				rbtShot[i].setOnClickListener(getStats2ClickListener);
			}

			ScoresFragment.this.alertshot = builder.create();
			ScoresFragment.this.alertshot.show();
		}

	};

	
	// Listener to get shot outcome
	OnClickListener getStats1ClickListener = new OnClickListener() {
		@Override
		public void onClick(View vvv) {
			RadioButton rB = (RadioButton) vvv;
			stats1 = (rB.getText().toString());

		}
	};

	// listener to get shot type
	OnClickListener getStats2ClickListener = new OnClickListener() {
		@Override
		public void onClick(View vvv) {
			RadioButton rB = (RadioButton) vvv;
			stats2 = (rB.getText().toString());
		}
	};

	// *******************************************************************//
	// *******************************************************************//
	// *******************************************************************//
	// *******************************************************************//
	// *******************************************************************//

	// ///////////////////SCORE CLICK LISTENER//////////////////////////////////
	OnClickListener scoreAddClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {

			// for each case update score display in this fragment
			// and update score display in REVIEW fragment
			switch (v.getId()) {
			case R.id.home_goals:
				homeGoals++;
				bHomeGoals.setText(String.valueOf(homeGoals));
				((Startup) getActivity()).getFragmentReview().settHomeGoals(
						homeGoals);
				// if first score change + on buttons to 0
				if (bHomePoints.getText().equals("+"))
					bHomePoints.setText("0");
				break;
			case R.id.home_points:
				homePoints++;
				bHomePoints.setText(String.valueOf(homePoints));
				((Startup) getActivity()).getFragmentReview().settHomePoints(
						homePoints);
				// if first score change + on buttons to 0
				if (bHomeGoals.getText().equals("+"))
					bHomeGoals.setText("0");
				break;
			case R.id.opp_goals:
				oppGoals++;
				bOppGoals.setText(String.valueOf(oppGoals));
				((Startup) getActivity()).getFragmentReview().settOppGoals(
						oppGoals);
				// if first score change + on buttons to 0
				if (bOppPoints.getText().equals("+"))
					bOppPoints.setText("0");
				break;
			case R.id.opp_points:
				oppPoints++;
				bOppPoints.setText(String.valueOf(oppPoints));
				((Startup) getActivity()).getFragmentReview().settOppPoints(
						oppPoints);
				// if first score change + on buttons to 0
				if (bOppGoals.getText().equals("+"))
					bOppGoals.setText("0");
				break;
			case R.id.dec_home_goals:
				if (homeGoals > 0) {
					homeGoals--;
					bHomeGoals.setText(String.valueOf(homeGoals));
					((Startup) getActivity()).getFragmentReview()
							.settHomeGoals(homeGoals);
					break;
				} else
					return;
			case R.id.dec_home_points:
				if (homePoints > 0) {
					homePoints--;
					bHomePoints.setText(String.valueOf(homePoints));
					((Startup) getActivity()).getFragmentReview()
							.settHomePoints(homePoints);
					break;
				} else
					return;
			case R.id.dec_opp_goals:
				if (oppGoals > 0) {
					oppGoals--;
					bOppGoals.setText(String.valueOf(oppGoals));
					((Startup) getActivity()).getFragmentReview().settOppGoals(
							oppGoals);
					break;
				} else
					return;
			case R.id.dec_opp_points:
				if (oppPoints > 0) {
					oppPoints--;
					bOppPoints.setText(String.valueOf(oppPoints));
					((Startup) getActivity()).getFragmentReview()
							.settOppPoints(oppPoints);
					break;
				} else
					return;
			}
			// update totals values and text
			setTotals();
		}
	};

	OnClickListener periodClickListener = new OnClickListener() {
		@Override
		public void onClick(View w) {
			Button b = (Button) w;
			// read list of allowable times from array in assets and put in
			// adapter to display in alertdialog for selection
			minsList = getResources().getStringArray(R.array.period);
			ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(
					getActivity(), R.layout.single_row_layout, minsList);
			new AlertDialog.Builder(getActivity())
					.setTitle("set time period")
					.setAdapter(adapter1,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// convert string input to integer
									periodInt = which;
									bPeriod.setText(getResources()
											.getStringArray(R.array.periodShort)[which]);
									periodStr = minsList[which];
									dialog.dismiss();
								}
							}).create().show();
		}
	};

	OnClickListener nullPeriodClickListener = new OnClickListener() {
		@Override
		public void onClick(View w) {
			Toast.makeText(getActivity(), "stop timer first",
					Toast.LENGTH_SHORT).show();
		}
	};

	// method to calculate total score from goals and points
	// and update if home team is ahead or behind or if game is a draw
	private void setTotals() {
		int homeTotal = (homeGoals * 3) + homePoints;
		tHomeTotal.setText("(" + String.valueOf(homeTotal) + ")");
		int oppTotal = (oppGoals * 3) + oppPoints;
		tOppTotal.setText("(" + String.valueOf(oppTotal) + ")");

		if (homeTotal > oppTotal) {
			tUpDownDrawText.setText("up by: ");
			tHomeDifference.setText("(" + String.valueOf(homeTotal - oppTotal)
					+ ")");
		} else if (homeTotal < oppTotal) {
			tUpDownDrawText.setText("down by: ");
			tHomeDifference.setText("(" + String.valueOf(-homeTotal + oppTotal)
					+ ")");
		} else {
			tUpDownDrawText.setText("drawn game. ");
			tHomeDifference.setText(" ");
		}
	}

	// //////////////////////////TIMER///////////////////////////////
	// set up thread to run match timer
	private Runnable run = new Runnable() {
		@Override
		public void run() {
			long millis = System.currentTimeMillis() - starttime;
			int seconds = (int) (millis / 1000);
			int minutes = seconds / 60;
			seconds = seconds % 60;
			tTimeGone.setText(String.format("%02d:%02d", minutes, seconds));
			if (minsPerHalf - minutes > 0) {
				tTimeToGo.setText(String.format("%02d:%02d", minsPerHalf - 1
						- minutes, 60 - seconds));
			} else {
				tTimeToGo.setText(String.format("%02d:%02d", minutes
						- minsPerHalf, seconds));
				tTimeLeft.setText("extra time:");
			}
			h.postDelayed(this, 1000);
		}
	};

	public String getTime() {
		if (starttime > 0) {
			long millis = System.currentTimeMillis() - starttime;
			int seconds = (int) (millis / 1000);
			int minutes = seconds / 60;
			return String.format("%02d", minutes);
		} else {
			return "";
		}
	}

	// this method is called from the SETUP fragment to update the names of the
	// home and away teams and to receive team line and teams from setup screen
	public void setTeamLineUp(String homeTeam, String oppTeam) {
		if (!homeTeam.equals(""))
			tOurTeam.setText(homeTeam);
		if (!oppTeam.equals(""))
			tOppTeam.setText(oppTeam);
	}

	// Undo stats entries
	OnClickListener undoOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Uri allTitles = TeamContentProvider.CONTENT_URI_2;
			String strTemp;
			String[] projection = { TeamContentProvider.STATSID,
					TeamContentProvider.STATSLINE };
			CursorLoader cL;
			cL = new CursorLoader(getActivity(), allTitles, projection, null,
					null, TeamContentProvider.STATSID);
			Cursor c1 = cL.loadInBackground();
			if (c1.getCount() > 0) {
				c1.moveToLast();
				rowId = (c1.getLong(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSID)));
				strTemp = (c1.getString(c1
						.getColumnIndexOrThrow(TeamContentProvider.STATSLINE)));

				// check for goal
				if (strTemp.indexOf("goal") >= 0) {
					// check which team
					if (strTemp.indexOf(tOurTeam.getText().toString()) >= 0) {
						if (homeGoals - 1 >= 0) {
							homeGoals = homeGoals - 1;
							bHomeGoals.setText(String.valueOf(homeGoals));
							// update totals
							setTotals();
							// increment score in REVIEW fragment
							((Startup) getActivity()).getFragmentReview()
									.settHomeGoals(homeGoals);
							// remind user score is updated in case they try and
							// do it manually
							Toast.makeText(getActivity(), "Score Updated",
									Toast.LENGTH_SHORT).show();
							// update goal counter display in review page
							((Startup) getActivity()).getFragmentReview()
									.addtShotGoalsHome(-1);
							// increment goal from play counter in review page
							// unless score was from free/45/65/penalty
							if ((strTemp.indexOf("from free") < 0)
									&& (strTemp.indexOf("from penalty") < 0)
									&& (strTemp.indexOf("from 45/65") < 0)
									&& (strTemp.indexOf("from sideline") < 0)) {
								((Startup) getActivity()).getFragmentReview()
										.addtShotGoalsPlayHome(-1);
							}
						}
					} else if (strTemp.indexOf(tOppTeam.getText().toString()) >= 0) {
						if (oppGoals - 1 >= 0) {
							oppGoals = oppGoals - 1;
							bOppGoals.setText(String.valueOf(oppGoals));
							// update totals
							setTotals();
							// increment score in REVIEW fragment
							((Startup) getActivity()).getFragmentReview()
									.settOppGoals(oppGoals);
							// remind user score is updated in case they try and
							// do it manually
							Toast.makeText(getActivity(), "Score Updated",
									Toast.LENGTH_SHORT).show();
							// update goal counter display in review page
							((Startup) getActivity()).getFragmentReview()
									.addtShotGoalsOpp(-1);
							// increment goal from play counter in review page
							// unless score was from free/45/65/penalty
							if ((strTemp.indexOf("from free") < 0)
									&& (strTemp.indexOf("from penalty") < 0)
									&& (strTemp.indexOf("from 45/65") < 0)
									&& (strTemp.indexOf("from sideline") < 0)) {
								((Startup) getActivity()).getFragmentReview()
										.addtShotGoalsPlayOpp(-1);
							}
						}
					}
				}
				// check for point
				else if (strTemp.indexOf("point") >= 0) {
					// check which team
					if (strTemp.indexOf(tOurTeam.getText().toString()) >= 0) {
						if (homePoints - 1 >= 0) {
							homePoints = homePoints - 1;
							bHomePoints.setText(String.valueOf(homePoints));
							// update totals
							setTotals();
							// increment score in REVIEW fragment
							((Startup) getActivity()).getFragmentReview()
									.settHomePoints(homePoints);
							// remind user score is updated in case they try and
							// do it manually
							Toast.makeText(getActivity(), "Score Updated",
									Toast.LENGTH_SHORT).show();
							// update goal counter display in review page
							((Startup) getActivity()).getFragmentReview()
									.addtShotPointsHome(-1);
							// increment goal from play counter in review page
							// unless score was from free/45/65/penalty
							if ((strTemp.indexOf("from free") < 0)
									&& (strTemp.indexOf("from penalty") < 0)
									&& (strTemp.indexOf("from 45/65") < 0)
									&& (strTemp.indexOf("from sideline") < 0)) {
								((Startup) getActivity()).getFragmentReview()
										.addtShotPointsPlayHome(-1);
							}
						}
					} else if (strTemp.indexOf(tOppTeam.getText().toString()) >= 0) {
						if (oppPoints - 1 >= 0) {
							oppPoints = oppPoints - 1;
							bOppPoints.setText(String.valueOf(oppPoints));
							// update totals
							setTotals();
							// increment score in REVIEW fragment
							((Startup) getActivity()).getFragmentReview()
									.settOppPoints(oppPoints);
							// remind user score is updated in case they try and
							// do it manually
							Toast.makeText(getActivity(), "Score Updated",
									Toast.LENGTH_SHORT).show();
							// update goal counter display in review page
							((Startup) getActivity()).getFragmentReview()
									.addtShotPointsOpp(-1);
							// increment goal from play counter in review page
							// unless score was from free/45/65/penalty
							if ((strTemp.indexOf("from free") < 0)
									&& (strTemp.indexOf("from penalty") < 0)
									&& (strTemp.indexOf("from 45/65") < 0)
									&& (strTemp.indexOf("from sideline") < 0)) {
								((Startup) getActivity()).getFragmentReview()
										.addtShotPointsPlayOpp(-1);
							}
						}
					}
				}
				// check for wides
				else if (strTemp.indexOf("wide") >= 0) {
					// check which team
					if (strTemp.indexOf(tOurTeam.getText().toString()) >= 0) {
						((Startup) getActivity()).getFragmentReview()
								.addtShotWidesHome(-1);
					} else if (strTemp.indexOf(tOppTeam.getText().toString()) >= 0) {
						((Startup) getActivity()).getFragmentReview()
								.addtShotWidesOpp(-1);
					}
				}
				// check for off posts
				else if (strTemp.indexOf("off posts") >= 0) {
					// check which team
					if (strTemp.indexOf(tOurTeam.getText().toString()) >= 0) {
						((Startup) getActivity()).getFragmentReview()
								.addtShotPostsHome(-1);
					} else if (strTemp.indexOf(tOppTeam.getText().toString()) >= 0) {
						((Startup) getActivity()).getFragmentReview()
								.addtShotPostsOpp(-1);
					}
				}
				// check for saved
				else if (strTemp.indexOf("saved") >= 0) {
					// check which team
					if (strTemp.indexOf(tOurTeam.getText().toString()) >= 0) {
						((Startup) getActivity()).getFragmentReview()
								.addtShotSavedHome(-1);
					} else if (strTemp.indexOf(tOppTeam.getText().toString()) >= 0) {
						((Startup) getActivity()).getFragmentReview()
								.addtShotSavedOpp(-1);
					}
				} else if (strTemp.indexOf("puckout won") >= 0) {
					// check which team
					if (strTemp.indexOf(tOurTeam.getText().toString()) >= 0) {
						((Startup) getActivity()).getFragmentReview()
								.addPuckWonCleanHome(-1);
					} else if (strTemp.indexOf(tOppTeam.getText().toString()) >= 0) {
						((Startup) getActivity()).getFragmentReview()
								.addPuckWonCleanOpp(-1);
					}
				} else if (strTemp.indexOf("puckout lost") >= 0) {
					// check which team
					if (strTemp.indexOf(tOurTeam.getText().toString()) >= 0) {
						((Startup) getActivity()).getFragmentReview()
								.addPuckLostCleanHome(-1);
					} else if (strTemp.indexOf(tOppTeam.getText().toString()) >= 0) {
						((Startup) getActivity()).getFragmentReview()
								.addPuckLostCleanOpp(-1);
					}
				} else if (strTemp.indexOf("free/pen") >= 0) {
					// check which team
					if (strTemp.indexOf(tOurTeam.getText().toString()) >= 0) {
						((Startup) getActivity()).getFragmentReview()
								.addFreeConcededHome(-1);
					} else if (strTemp.indexOf(tOppTeam.getText().toString()) >= 0) {
						((Startup) getActivity()).getFragmentReview()
								.addFreeConcededOpp(-1);
					}
				}

				getActivity().getContentResolver().delete(
						Uri.parse(TeamContentProvider.CONTENT_URI_2 + "/"
								+ rowId), null, null);
			}

			updateStats();
			c1.close();
		}
	};

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.scores_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	// set up help menu in action bar
	// @Override

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent ihelp;

		switch (item.getItemId()) {
		case 0:
			// menu pointer do nothing
		case R.id.helpTeam:
			ihelp = new Intent(getActivity(), HelpActivity.class);
			ihelp.putExtra("HELP_ID", R.string.scoresHelp);
			startActivity(ihelp);
			return true;			
		case R.id.resetTimer:
			resetTime();
			return true;
		case R.id.resetScore:
			resetScore();
			return true;
		case R.id.resetStats:
			resetStats();
			return true;
		case R.id.resetAll:
			tLoc.setText("");
			resetTime();
			resetScore();
			resetStats();
			return true;
		case R.id.phone:
			AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
			input = new EditText(getActivity());
			input.setId(991);
			input.setText(phone);
			alert.setTitle("Enter Phone Number(s) for Texts");
			alert.setMessage("Format 0001234567 (no spaces)\nUse comma to separate numbers\n"
					+ "if there's more than one");
			alert.setView(input);
			alert.setNegativeButton("Reset",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface indialog, int which) {
							phone = "";

							// update title and panelname
							SharedPreferences sharedPref = getActivity()
									.getSharedPreferences(
											"team_stats_record_data",
											Context.MODE_PRIVATE);
							SharedPreferences.Editor editor = sharedPref.edit();
							editor.putString("PHONE", phone);
							editor.commit();
						}
					});
			alert.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface indialog, int which) {
							Pattern p = Pattern.compile("^[0-9]{10}$");
							Matcher m;
							int check = 0;
							String inName = input.getText().toString()
									.replace(" ", "");
							String[] separated = inName.split(",");
							for (int i = 0; i < separated.length; i++) {
								m = p.matcher(separated[i].replace(" ", ""));
								if (m.find()) {
									check = check + 1;
								}
							}
							if (check == separated.length) {
								phone = input.getText().toString()
										.replace(" ", "");

								// update title and panelname
								SharedPreferences sharedPref = getActivity()
										.getSharedPreferences(
												"team_stats_record_data",
												Context.MODE_PRIVATE);
								SharedPreferences.Editor editor = sharedPref
										.edit();
								editor.putString("PHONE", phone);
								editor.commit();
							} else {
								Toast.makeText(getActivity(),
										"Invalid Number(s), Try Again",
										Toast.LENGTH_SHORT).show();
							}
						}
					});
			alert.create();
			alert.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public String getLocText(){
		return tLoc.getText().toString();		
	}
	


}
