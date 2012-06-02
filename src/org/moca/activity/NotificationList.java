package org.moca.activity;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class NotificationList extends Activity {
	
	private TextView ikarobio, joaobio, emeliabio;
	private ImageView ikaro, joao, emelia;
	private View createView() {
		
		int bkID = getResources().getIdentifier("main_background2","drawable",getPackageName());
		//int bkID = getResources().getIdentifier("icon","drawable",getPackageName());
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setBackgroundDrawable(getResources().getDrawable(bkID));
		
		int emeliaID = getResources().getIdentifier("emelia","drawable",getPackageName());
		emelia = new ImageView(this);
		emelia.setPadding(5, 0, 5, 0);
		emelia.setImageResource(emeliaID);
		emeliabio = new TextView(this);
		emeliabio.setPadding(5, 0, 5, 0);
		emeliabio.setText("Emelia Benjamin\nM.D., Sc.M., FACC, FAHA\nemelia@bu.edu");
		emeliabio.setTextAppearance(this,android.R.style.TextAppearance_Medium);

		
		int ikaroID = getResources().getIdentifier("ikaro","drawable",getPackageName());
		ikaro = new ImageView(this);
		ikaro.setPadding(5, 0, 5, 0);
		ikaro.setImageResource(ikaroID);
				
		ikarobio = new TextView(this);
		ikarobio.setPadding(5, 0, 5, 0);
		ikarobio.setText("Ikaro Silva\nPh.D.\nikaro@mit.edu");
		ikarobio.setTextAppearance(this,android.R.style.TextAppearance_Medium);

		int joaoID = getResources().getIdentifier("joao","drawable",getPackageName());
		joao = new ImageView(this);
		joao.setPadding(5, 0, 5, 0);
		joao.setImageResource(joaoID);
		joaobio = new TextView(this);
		joaobio.setPadding(5, 0, 5, 0);
		joaobio.setText("Joao Daniel Fontes\nM.D., M.P.H.\nfontesjd@bu.edu");
		joaobio.setTextAppearance(this,android.R.style.TextAppearance_Medium);

		
		ll.addView(emelia);
		ll.addView(emeliabio);
		ll.addView(ikaro);
		ll.addView(ikarobio);
		ll.addView(joao);
		ll.addView(joaobio);
		
		LinearLayout topRow = new LinearLayout(this);
		topRow.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout middleRow = new LinearLayout(this);
		middleRow.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout bottomRow = new LinearLayout(this);
		bottomRow.setOrientation(LinearLayout.HORIZONTAL);

		ScrollView scrollView = new ScrollView(this);
		scrollView.addView(ll);
		return scrollView;
	}

    /** Called when the activity is first created. */
	public static final String TAG = Moca.class.toString();
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.w(TAG, "at the info page");
        super.onCreate(savedInstanceState);
        Log.w(TAG, "past create super");
        setContentView(createView());
    }
}


