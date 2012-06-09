package org.moca.activity;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class SaveProcedureList extends Activity {
	
	private TextView emeliabio;
	private View createView() {
		
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		
		emeliabio = new TextView(this);
		emeliabio.setPadding(5, 0, 5, 0);
		emeliabio.setText("Your 10 year CHD Risk : \n11% \n");
		emeliabio.setTextAppearance(this,android.R.style.TextAppearance_Medium);
		emeliabio.setTextColor(android.R.color.darker_gray);

		
		
		ll.addView(emeliabio);
		
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


