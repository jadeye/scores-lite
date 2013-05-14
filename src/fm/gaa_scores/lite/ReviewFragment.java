/*
 *  MatchReviewFragment.java
 *
 *  Written by: Fintan Mahon 12101524
 *  
 *  Description: GUI to display match score and match statistics data summary. 
 *  Also can start activities to view detailed tables of match statistics
 *  
 * store data to database tables and pass relevant details into MatchRecordReview
 *  
 *  Written on: Jan 2013
 *  
 * 
 */
package fm.gaa_scores.lite;


import fm.gaa_scores.lite.R;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ReviewFragment extends Fragment {
	private int homeGoals, homePoints, homeTotal, oppGoals, oppPoints,
			oppTotal;
	private TextView tHomeGoals, tHomePoints, tHomeTotal, tOppGoals,
			tOppPoints;
	private TextView tShotGoalsHome;
	private TextView tShotWidesHome;
	private TextView tShotPointsHome;
	private TextView tShotSavedHome; 
	private TextView tShotPostsHome;

	private TextView tShotGoalsPlayHome;
	private TextView tShotPointsPlayHome;
	private TextView tFreeConcededHome;
	private TextView tFreeConcededOpp;
	private TextView tShotPointsPlayOpp;
	private TextView tPuckWonCleanHome;
	private TextView tPuckLostCleanHome;
	private TextView tPuckWonCleanOpp;
	private TextView tPuckLostCleanOpp;
	private TextView tOwnTeam, tOppTeam;
	private Button  bSendAll;


	private int  shotGoalsHome = 0, shotPointsHome = 0;
	private int shotGoalsPlayHome = 0, shotPointsPlayHome = 0;
	private int shotGoalsPlayOpp = 0, shotPointsPlayOpp = 0;
	private int shotWidesHome = 0,  shotSavedHome = 0,
			 shotPostsHome = 0;
	private int freeConcededHome = 0;
	private int freeConcededOpp = 00;
	int puckWonCleanHome = 0, puckWonCleanHomePerCent = 0;
	int puckLostCleanHome = 0, puckLostCleanHomePerCent = 0;
	int puckWonBreakHome = 0, puckWonBreakHomePerCent = 0;
	int puckLostBreakHome = 0, puckLostBreakHomePerCent = 0;
	int puckWonCleanOpp = 0, puckWonCleanOppPerCent = 0;
	int puckLostCleanOpp = 0, puckLostCleanOppPerCen = 0;
	int puckWonBreakOpp = 0, puckWonBreakOppPerCent = 0;
	int puckLostBreakOpp = 0, puckLostBreakOppPerCent = 0;
	int puckOutTotalHome = 0, puckOutTotalOpp = 0;
	int puckOtherHome = 0, puckOtherHomePerCent = 0;
	int puckOtherOpp = 0, puckOtherOppPerCent = 0;

	private TextView tOppTotal;
	private TextView tShotGoalsOpp, tShotGoalsPlayOpp;
	private TextView tShotWidesOpp;
	private TextView tShotPointsOpp;
	private TextView tShotSavedOpp;
	private TextView tShotPostsOpp;
	private int shotGoalsOpp = 0, shotPointsOpp = 0;
	private int shotWidesOpp = 0, shotSavedOpp = 0,
			 shotPostsOpp = 0;
	private ListView listViewStats;
	
	@Override
	// start main method to display screen
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.review_layout, container, false);
		// Open up shared preferences file to read in persisted data on startup
		SharedPreferences sharedPref = getActivity().getSharedPreferences(
				"team_stats_review_data", Context.MODE_PRIVATE);

		// get the tag name of this Fragment and pass it up to the parent
		// activity MatchApplication so that this Fragment may be accessed
		// by other fragments through using a reference created from tag name
		String myTag = getTag();
		((Startup) getActivity()).setTagFragmentReview(myTag);
		this.setHasOptionsMenu(true);

		// set up text buttons edittexts etc.
		tOwnTeam = (TextView) v.findViewById(R.id.textViewRevHome);
		tOppTeam = (TextView) v.findViewById(R.id.textViewRevOpp);

		tOwnTeam.setText(sharedPref.getString("OURTEAM", "OWN TEAM"));
		tOppTeam.setText(sharedPref.getString("OPPTEAM", "OPPOSITION"));

		tHomeGoals = (TextView) v.findViewById(R.id.tVHomeGoals);
		tHomePoints = (TextView) v.findViewById(R.id.tVHomePoints);
		tHomeTotal = (TextView) v.findViewById(R.id.tVHomeTotal);
		tOppGoals = (TextView) v.findViewById(R.id.tVOppGoals);
		tOppPoints = (TextView) v.findViewById(R.id.tVOppPoints);
		tOppTotal = (TextView) v.findViewById(R.id.tVOppTotal);

		tShotGoalsHome = (TextView) v.findViewById(R.id.tVwShotsGoalsNo);
		tShotPointsHome = (TextView) v.findViewById(R.id.tVwShotsPointsNo);
		tShotWidesHome = (TextView) v.findViewById(R.id.tVwShotsWidesNo);
		tShotSavedHome = (TextView) v.findViewById(R.id.tVwShotsSavedNo);
		tShotPostsHome = (TextView) v.findViewById(R.id.tVwShotsPostsNo);

		tShotGoalsPlayHome = (TextView) v.findViewById(R.id.tVGoalsPlay);
		tShotPointsPlayHome = (TextView) v.findViewById(R.id.tVPointsPlay);
		tShotGoalsPlayOpp = (TextView) v.findViewById(R.id.tVGoalsOppPlay);
		tShotPointsPlayOpp = (TextView) v.findViewById(R.id.tVPointsOppPlay);

		tShotGoalsOpp = (TextView) v.findViewById(R.id.tVwShotsGoalsOppNo);
		tShotPointsOpp = (TextView) v.findViewById(R.id.tVwShotsPointsOppNo);
		tShotWidesOpp = (TextView) v.findViewById(R.id.tVwShotsWidesOppNo);
		tShotSavedOpp = (TextView) v.findViewById(R.id.tVwShotsSavedOppNo);
		tShotPostsOpp = (TextView) v.findViewById(R.id.tVwShotsPostsOppNo);

		// Set up output for frees
		tFreeConcededHome = (TextView) v.findViewById(R.id.tVwFreeWonHome);
		tFreeConcededOpp = (TextView) v.findViewById(R.id.tVwFreeWonOpp);

		// Set up output for puckouts
		tPuckWonCleanHome = (TextView) v.findViewById(R.id.tVwPuckWonCleanHome);
		tPuckLostCleanHome = (TextView) v
				.findViewById(R.id.tVPuckLostCleanHome);
		tPuckWonCleanOpp = (TextView) v.findViewById(R.id.tVwPuckWonCleanOpp);
		tPuckLostCleanOpp = (TextView) v.findViewById(R.id.tVPuckLostCleanOpp);

		// Read in score from persisted data
		homeGoals = sharedPref.getInt("HOMEGOALS", 0);
		homePoints = sharedPref.getInt("HOMEPOINTS", 0);
		oppGoals = sharedPref.getInt("OPPGOALS", 0);
		oppPoints = sharedPref.getInt("OPPPOINTS", 0);

		// update screen if persisted data exists
		if (homeGoals + homePoints + oppGoals + oppPoints > 0) {
			settHomeGoals(homeGoals);
			settHomePoints(homePoints);
			settOppGoals(oppGoals);
			settOppPoints(oppPoints);
		}

		// setup shots/frees/puckouts values from persisted data
		shotGoalsHome = sharedPref.getInt("SHOTGOALSHOME", 0);
		shotPointsHome = sharedPref.getInt("SHOTPOINTSHOME", 0);
		shotWidesHome = sharedPref.getInt("SHOTWIDESHOME", 0);
		shotSavedHome = sharedPref.getInt("SHOTSAVEDHOME", 0);
		shotPostsHome = sharedPref.getInt("SHOTPOSTSHOME", 0);
		shotGoalsPlayHome = sharedPref.getInt("SHOTGOALSPLAYHOME", 0);
		shotPointsPlayHome = sharedPref.getInt("SHOTPOINTSPLAYHOME", 0);
		addtShotGoalsHome(0); 
		addtShotGoalsPlayHome(0);
		addtShotPointsHome(0); 
		addtShotPointsPlayHome(0); 
		addtShotWidesHome(0) ;
		addtShotSavedHome(0) ;
		addtShotPostsHome(0) ;

		shotGoalsOpp = sharedPref.getInt("SHOTGOALSOPP", 0);
		shotPointsOpp = sharedPref.getInt("SHOTPOINTSOPP", 0);
		shotWidesOpp = sharedPref.getInt("SHOTWIDESOPP", 0);
		shotSavedOpp = sharedPref.getInt("SHOTSAVEDOPP", 0);
		shotPostsOpp = sharedPref.getInt("SHOTPOSTSOPP", 0);
		shotGoalsPlayOpp = sharedPref.getInt("SHOTGOALSPLAYOPP", 0);
		shotPointsPlayOpp = sharedPref.getInt("SHOTPOINTSPLAYOPP", 0);
		addtShotGoalsOpp(0); 
		addtShotGoalsPlayOpp(0);
		addtShotPointsOpp(0); 
		addtShotPointsPlayOpp(0); 
		addtShotWidesOpp(0) ;
		addtShotSavedOpp(0) ;
		addtShotPostsOpp(0) ;


		freeConcededHome = sharedPref.getInt("FREEWONHOME", 0);
		freeConcededOpp = sharedPref.getInt("FREEWONOPP", 0);
		addFreeConcededHome(0);
		addFreeConcededOpp(0); 

		puckWonCleanHome = sharedPref.getInt("PUCKWONCLEANHOME", 0);
		puckLostCleanHome = sharedPref.getInt("PUCKLOSTCLEANHOME", 0);

		puckWonCleanOpp = sharedPref.getInt("PUCKWONCLEANOPP", 0);
		puckLostCleanOpp = sharedPref.getInt("PUCKLOSTCLEANOPP", 0);
		addPuckWonCleanHome(0); 
		addPuckLostCleanHome(0) ;
		addPuckWonCleanOpp(0) ;
		addPuckLostCleanOpp(0); 
		
		
		//fill in list view with datavbase
		listViewStats = (ListView) v.findViewById(R.id.listView1);
		
		updateListView();
	
		return v;

	}

	// *******************Home Shots********************///
	// increment counters for home team shots
	public void addtShotGoalsHome(int i) {
		shotGoalsHome = shotGoalsHome + i;
		tShotGoalsHome.setText(String.valueOf(shotGoalsHome));
	}

	public void addtShotGoalsPlayHome(int i) {
		shotGoalsPlayHome = shotGoalsPlayHome + i;
		tShotGoalsPlayHome.setText(String.valueOf(shotGoalsPlayHome));
	}

	public void addtShotPointsHome(int i) {
		shotPointsHome = shotPointsHome + i;
		tShotPointsHome.setText(String.valueOf(shotPointsHome));
	}

	public void addtShotPointsPlayHome(int i) {
		shotPointsPlayHome = shotPointsPlayHome + i;
		tShotPointsPlayHome.setText(String.valueOf(shotPointsPlayHome));
	}

	public void addtShotWidesHome(int i) {
		shotWidesHome = shotWidesHome + i;
		tShotWidesHome.setText(String.valueOf(shotWidesHome));
	}

	public void addtShotSavedHome(int i) {
		shotSavedHome = shotSavedHome + i;
		tShotSavedHome.setText(String.valueOf(shotSavedHome));
	}

	public void addtShotPostsHome(int i) {
		shotPostsHome = shotPostsHome + i;
		tShotPostsHome.setText(String.valueOf(shotPostsHome));
	}

	// *******************Opp Shots********************///
	// increment counters for opposition team shots
	public void addtShotGoalsOpp(int i) {
		shotGoalsOpp = shotGoalsOpp + i;
		tShotGoalsOpp.setText(String.valueOf(shotGoalsOpp));
	}

	public void addtShotGoalsPlayOpp(int i) {
		shotGoalsPlayOpp = shotGoalsPlayOpp + i;
		tShotGoalsPlayOpp.setText(String.valueOf(shotGoalsPlayOpp));
	}

	public void addtShotPointsOpp(int i) {
		shotPointsOpp = shotPointsOpp + i;
		tShotPointsOpp.setText(String.valueOf(shotPointsOpp));
	}

	public void addtShotPointsPlayOpp(int i) {
		shotPointsPlayOpp = shotPointsPlayOpp + i;
		tShotPointsPlayOpp.setText(String.valueOf(shotPointsPlayOpp));
	}

	public void addtShotWidesOpp(int i) {
		shotWidesOpp = shotWidesOpp + i;
		tShotWidesOpp.setText(String.valueOf(shotWidesOpp));
	}

	public void addtShotSavedOpp(int i) {
		shotSavedOpp = shotSavedOpp + i;
		tShotSavedOpp.setText(String.valueOf(shotSavedOpp));
	}

	public void addtShotPostsOpp(int i) {
		shotPostsOpp = shotPostsOpp + i;
		tShotPostsOpp.setText(String.valueOf(shotPostsOpp));
	}

	// ////////////////Update Free Section////////////////////////////
	// increment counters for frees
	public void addFreeConcededHome(int i) {
		freeConcededHome = freeConcededHome + i;
		tFreeConcededHome.setText(String.valueOf(freeConcededHome));
	}

	public void addFreeConcededOpp(int i) {
		freeConcededOpp = freeConcededOpp + i;
		tFreeConcededOpp.setText(String.valueOf(freeConcededOpp));
	}

	// ////////////////Update PuckOuts Section////////////////////////////
	// increment counters for puck outs
	public void addPuckWonCleanHome(int i) {// ///
		puckWonCleanHome = puckWonCleanHome + i;
		tPuckWonCleanHome.setText(String.valueOf(puckWonCleanHome));
	}

	public void addPuckLostCleanHome(int i) {
		puckLostCleanHome = puckLostCleanHome + i;
		tPuckLostCleanHome.setText(String.valueOf(puckLostCleanHome));
	}

	public void addPuckWonCleanOpp(int i) {
		puckWonCleanOpp = puckWonCleanOpp + i;
		tPuckWonCleanOpp.setText(String.valueOf(puckWonCleanOpp));
	}

	public void addPuckLostCleanOpp(int i) {
		puckLostCleanOpp = puckLostCleanOpp + i;
		tPuckLostCleanOpp.setText(String.valueOf(puckLostCleanOpp));
	}

	// ///////////UPDATE SCORES////////////////////////////
	// methods called from RECORD fragment to update score
	// and totals
	public void settHomeGoals(int i) {
		homeGoals = i;
		homeTotal = homeGoals * 3 + homePoints;
		tHomeGoals.setText(String.valueOf(homeGoals));
		tHomeTotal.setText(String.valueOf(homeTotal));
	}

	public void settHomePoints(int i) {
		homePoints = i;
		homeTotal = homeGoals * 3 + homePoints;
		tHomePoints.setText(String.valueOf(homePoints));
		tHomeTotal.setText(String.valueOf(homeTotal));
	}

	public void settOppGoals(int i) {
		oppGoals = i;
		oppTotal = oppGoals * 3 + oppPoints;
		tOppGoals.setText(String.valueOf(oppGoals));
		tOppTotal.setText(String.valueOf(oppTotal));
	}

	public void settOppPoints(int i) {
		oppPoints = i;
		oppTotal = oppGoals * 3 + oppPoints;
		tOppPoints.setText(String.valueOf(oppPoints));
		tOppTotal.setText(String.valueOf(oppTotal));
	}

	// method to reset all stats values to zero
	public void resetStats() {
		shotGoalsHome = 0;
		tShotGoalsHome.setText("0");
		shotPointsHome = 0;
		tShotPointsHome.setText("0");
		shotWidesHome = 0;
		tShotWidesHome.setText("0");
		shotSavedHome = 0;
		tShotSavedHome.setText("0");
		shotPostsHome = 0;
		tShotPostsHome.setText("0");
		shotGoalsPlayHome = 0;
		tShotGoalsPlayHome.setText("0");
		shotPointsPlayHome = 0;
		tShotPointsPlayHome.setText("0");

		shotGoalsOpp = 0;
		tShotGoalsOpp.setText("0");
		shotPointsOpp = 0;
		tShotPointsOpp.setText("0");
		shotWidesOpp = 0;
		tShotWidesOpp.setText("0");
		shotSavedOpp = 0;
		tShotSavedOpp.setText("0");
		shotPostsOpp = 0;
		tShotPostsOpp.setText("0");
		shotGoalsPlayOpp = 0;
		tShotGoalsPlayOpp.setText("0");
		shotPointsPlayOpp = 0;
		tShotPointsPlayOpp.setText("0");

		freeConcededHome = 0;
		tFreeConcededHome.setText("0");
		freeConcededOpp = 0;
		tFreeConcededOpp.setText("0");

		puckWonCleanHome = 0;
		tPuckWonCleanHome.setText("0");
		puckLostCleanHome = 0;
		tPuckLostCleanHome.setText("0");
		puckWonCleanOpp = 0;
		tPuckWonCleanOpp.setText("0");
		puckLostCleanOpp = 0;
		tPuckLostCleanOpp.setText("0");

	}

	// ///////////////////////////END OF ONCREATE///////////////////////////
	@Override
	public void onPause() {
		// persist data out to shared preferences file to be available for start
		// up
		super.onPause(); // Always call the superclass method first
		SharedPreferences sharedPref = getActivity().getSharedPreferences(
				"team_stats_review_data", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();

		editor.putString("OURTEAM", tOwnTeam.getText().toString());
		editor.putString("OPPTEAM", tOppTeam.getText().toString());

		editor.putInt("HOMEGOALS", homeGoals);
		editor.putInt("HOMEPOINTS", homePoints);
		editor.putInt("OPPGOALS", oppGoals);
		editor.putInt("OPPPOINTS", oppPoints);

		editor.putInt("SHOTGOALSHOME", shotGoalsHome);
		editor.putInt("SHOTPOINTSHOME", shotPointsHome);
		editor.putInt("SHOTWIDESHOME", shotWidesHome);
		editor.putInt("SHOTSAVEDHOME", shotSavedHome);
		editor.putInt("SHOTPOSTSHOME", shotPostsHome);
		editor.putInt("SHOTGOALSPLAYHOME", shotGoalsPlayHome);
		editor.putInt("SHOTPOINTSPLAYHOME", shotPointsPlayHome);
		editor.putInt("SHOTGOALSOPP", shotGoalsOpp);
		editor.putInt("SHOTPOINTSOPP", shotPointsOpp);
		editor.putInt("SHOTWIDESOPP", shotWidesOpp);
		editor.putInt("SHOTSAVEDOPP", shotSavedOpp);
		editor.putInt("SHOTPOSTSOPP", shotPostsOpp);
		editor.putInt("SHOTGOALSPLAYOPP", shotGoalsPlayOpp);
		editor.putInt("SHOTPOINTSPLAYOPP", shotPointsPlayOpp);

		editor.putInt("FREEWONHOME", freeConcededHome);
		editor.putInt("FREEWONOPP", freeConcededOpp);

		editor.putInt("PUCKWONCLEANHOME", puckWonCleanHome);
		editor.putInt("PUCKLOSTCLEANHOME", puckLostCleanHome);
		editor.putInt("PUCKWONCLEANOPP", puckWonCleanOpp);
		editor.putInt("PUCKLOSTCLEANOPP", puckLostCleanOpp);

		editor.commit();
	}

	public void updateListView() {

		Uri allTitles = TeamContentProvider.CONTENT_URI_2;
		String[] from = new String[] { TeamContentProvider.STATSLINE };
		String[] projection = { TeamContentProvider._ID,
				TeamContentProvider.STATSLINE };

		int[] to = new int[] { R.id.listrtxt };

		CursorLoader cL = new CursorLoader(getActivity(), allTitles,
				projection, null, null, TeamContentProvider.STATSID+ " desc");
		Cursor c1 = cL.loadInBackground();

		SimpleCursorAdapter reminders = new SimpleCursorAdapter(getActivity(),
				R.layout.single_row_list_layout, c1, from, to, 0);

		listViewStats.setAdapter(reminders);
	}
	
	// this method is called from the SETUP fragment to update the names of the
	// home and away teams and to receive team line and teams from setup screen
	public void setTeamNames(String homeTeam, String oppTeam) {
			tOwnTeam.setText(homeTeam);
			tOppTeam.setText(oppTeam);
	}
	

	
	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.review_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
	// set up help menu in action bar
	//@Override
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)	{
		Intent ihelp;
		switch (item.getItemId()) {
		case 0:
			//menu pointer do nothing
		case R.id.helpTeam:
			ihelp = new Intent(getActivity(), HelpActivity.class);
			ihelp.putExtra("HELP_ID", R.string.reviewHelp);
			startActivity(ihelp);
			return true;		}	
	    return super.onOptionsItemSelected(item);	
	}
		
}
