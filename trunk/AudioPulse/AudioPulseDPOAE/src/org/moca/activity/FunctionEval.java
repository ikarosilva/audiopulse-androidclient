package org.moca.activity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.afree.chart.demo.activity.DeviationRendererDemo02Activity;
import org.framingham.risk.chd10yr;

import android.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.net.Uri;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;


public class FunctionEval extends Activity {
        Uri mData;
        public static final String FUNCEVAL_CONTENT_TYPE = "application/functioneval";
        private TextView mStatus;
        private Button detail, exit;
        Map<String, String> data = new HashMap<String, String>();
        
                private View createView() {
                LinearLayout ll = new LinearLayout(this);
                ll.setOrientation(LinearLayout.VERTICAL);

                LinearLayout hl = new LinearLayout(this);
                hl.setOrientation(LinearLayout.HORIZONTAL);


                mStatus = new TextView(this);
                mStatus.setPadding(5, 0, 5, 0);
                
                //Caculate and Display the RISK -- very basic right now
                chd10yr myRisk= new chd10yr();
                myRisk.getRisks(data);
                
                //ARIVIND TO DO: Implement CvdFram Here
                //Might be good to have the folowing signature (note data is a map!!)
                //CvdFram myCvdFram= new CvdFram();
                //myCvdFram.getRisks(data);
                
                mStatus.setTextSize(20);
                mStatus.setTextColor(Color.GRAY);
                //ARVIND TO DO: can display value from myCvdFram here
                setStatus("LDL Risks: " + myRisk.get_ldlrisk() + "% CHOL Risk: " + myRisk.get_cholrisk() + "%");
                detail = new Button(this);
                detail.setEnabled(true);               
                detail.setText("Detailed Test Analysis");
                detail.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {

                                setStatus("Generating detailed analysis...");
                        }
                });
                detail.setPadding(5, 0, 5, 0);

                exit = new Button(this);
                exit.setEnabled(true);
                exit.setText("Exit");
                exit.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                                finish();
                        }
                });
                exit.setPadding(5, 0, 5, 0);


                //hl.addView(start);
                hl.addView(detail);
                hl.addView(exit);

                ll.addView(hl);
                ll.addView(mStatus);

                
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

        private void setStatus(String status) {
                if (mStatus != null)
                        mStatus.setText(status);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                
                Intent intent = new Intent(this.getApplicationContext(), DeviationRendererDemo02Activity.class);
                startActivity(intent);
                /*
                //Get answers from questionaire 
                Bundle answers = getIntent().getExtras();
                Set keys= answers.keySet();
                Iterator it=keys.iterator();
				while (it.hasNext()) {
				  String tmp_ky=(String) it.next();
				  data.put(tmp_ky,answers.getString(tmp_ky));
				}
                
                setContentView(createView());
                if(this.getIntent().hasExtra("patient_file_name")){
                        mData = getIntent().getData();
                        //pt_file_name= this.getIntent().getExtras().getString("patient_file_name");
                }else{
                        //pt_file_name = "test.txt";
                }
                */
        }

        Intent mIntent=null;
        @Override
        public void onStart() {
                super.onStart();

        }

        @Override
        public void onDestroy(){
                super.onDestroy();
        }

}
