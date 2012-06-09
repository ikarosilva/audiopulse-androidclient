package org.framingham.risk;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;

//Implements the Coronary Heart Disease 10yr Risk assesment as decribed by
// http://www.framinghamheartstudy.org/risk/coronary.html

//Uses Maps to map values with the point system determined from the study


public class chd10yr {

	static Integer ldl_risk= (Integer) 0;
	static Integer chol_risk= (Integer) 0;

	public Integer get_ldlrisk(){
		return ldl_risk;
	}
	public Integer get_cholrisk(){
		return chol_risk;
	}
	private static final Map<String, Integer> MAP_AGE = 
			Collections.unmodifiableMap(new HashMap<String, Integer>() {{ 
				put(" less than 40",0);
				put(" 40-44",1);
				put(" 45-49",3);
				put(" 50-54",4);
				put(" 55-59",6);
				put(" 60-64",7);
				put(" 65-69",9);
				put(" more than 70",10);
			}});
	private static final Map<String, Integer> MAP_LDLC = 
			Collections.unmodifiableMap(new HashMap<String, Integer>() {{ 
				put(" less than 100 [less than 2.59]",-3);
				put(" 100-159 [2.6-4.14]",0);
				put(" 160-190 [4.15-4.92]",1);
				put(" greater than 190 [greater than 4.92]",2);
			}});		
	private static final Map<String, Integer> MAP_CHOL = 
			Collections.unmodifiableMap(new HashMap<String, Integer>() {{ 
				put(" less than 160 [less than 4.14]",-3);
				put(" 160-199 [4.15-5.17]",0);
				put(" 200-239 [5.18-6.21]",1);
				put(" 240-279 [6.22-7.24]",2);
				put(" greater than 280 [greater than 7.25]",3);
			}});
	private static final Map<String, Integer> MAP_HDLC_LDL = 
			Collections.unmodifiableMap(new HashMap<String, Integer>() {{ 
				put(" less than 35 [less than 0.90]",2);
				put(" 35-44 [0.91-1.16]",1);
				put(" 45-69 [1.17-1.55]",0);
				put(" greater than 60 [greater than 1.56]",-1);
			}});
	private static final Map<String, Integer> MAP_HDLC_CHOL = 
			Collections.unmodifiableMap(new HashMap<String, Integer>() {{ 
				put(" less than 35 [less than 0.90]",2);
				put(" 35-44 [0.91-1.16]",1);
				put(" 45-69 [1.17-1.55]",0);
				put(" greater than 60 [greater than 1.56]",-2);
			}});

	//For the BP case, concatenate Systolic with Diastolic strings 
	//NOT SURE IF THIS MAPPING IS EXACTLY CORRECT - NEED TO DOUBLE CHECK
	private static final Map<String, Integer> MAP_BP= 
			Collections.unmodifiableMap(new HashMap<String, Integer>() {{ 
				put(" less than 120 less than 80",0);
				put(" less than 120 81-84",0);
				put(" less than 120 85-89",1);
				put(" less than 120 90-99",2);
				put(" less than 120 over 100",3);
				put(" 120-129 less than 80",0);
				put(" 120-129 81-84",0);
				put(" 120-129 85-89",1);
				put(" 120-129 90-99",2);
				put(" 120-129 over 100",3);
				put(" 130-139 less than 80",1);
				put(" 130-139 81-84",1);
				put(" 130-139 85-89",1);
				put(" 130-139 90-99",2);
				put(" 130-139 over 100",3);
				put(" over 160 less than 80",3);
				put(" over 160 81-84",3);
				put(" over 160 85-89",3);
				put(" over 160 90-99",3);
				put(" over 160 over 100",3);
			}});

	private static final Map<String, Integer> MAP_RISK = 
			Collections.unmodifiableMap(new HashMap<String, Integer>() {{ 
				put(" ",0);
				put("smoker",2);
				put("diabetic",2);
				put("smoker, diabetic",4);
			}});

	private static final Map<Integer, Integer> MAP_CHOLRISK = 
			Collections.unmodifiableMap(new HashMap<Integer, Integer>() {{ 
				put(-1,2);put(0,3);put(1,3);
				put(2,4);put(3,5);put(4,7);
				put(5,8);put(6,10);put(7,13);
				put(8,16);put(9,20);put(10,25);
				put(11,31);put(12,37);put(13,45);
			}});
	private static final Map<Integer, Integer> MAP_LDLRISK = 
			Collections.unmodifiableMap(new HashMap<Integer, Integer>() {{ 
				put(-3,1);put(-2,2);put(-1,2);
				put(0,3);put(1,4);put(2,4);
				put(3,6);put(4,7);put(5,9);
				put(6,11);put(7,14);put(8,18);
				put(9,22);put(10,27);put(11,33);
				put(12,40);	put(13,47);put(14,56);
			}});

	public void getRisks(Map<String, String> data){

		Integer ldl_pts=(Integer) 0;
		Integer chol_pts=(Integer) 0;
		Iterator entries = data.entrySet().iterator();
		final String ans = new String();
		StringBuffer BP = new StringBuffer();
		while (entries.hasNext()) {
			Entry thisEntry = (Entry) entries.next();
			String MAP_PREDICTOR = (String) thisEntry.getKey();
			//Log.i("ch10yr","Getting values for " + MAP_PREDICTOR);
			String PRED_VAL = (String) thisEntry.getValue();
			//Log.i("ch10yr","value is " + PRED_VAL);
			//MAP PREDICTOR SHOULD MATCH ELEMENT ID in the xml file!
			if(MAP_PREDICTOR.contentEquals("age")){
				ldl_pts =MAP_AGE.get(PRED_VAL.toString())+ldl_pts;
				chol_pts =MAP_AGE.get(PRED_VAL.toString())+chol_pts;
			}
			else if(MAP_PREDICTOR.contentEquals("ldlc")){
				ldl_pts =MAP_LDLC.get(PRED_VAL.toString())+ldl_pts;
			}
			else if(MAP_PREDICTOR.contentEquals("chol")){
				chol_pts =MAP_CHOL.get(PRED_VAL.toString())+chol_pts;
			}
			else if(MAP_PREDICTOR.contentEquals("hdlc")){
				ldl_pts =MAP_HDLC_LDL.get(PRED_VAL.toString())+ldl_pts;
				chol_pts =MAP_HDLC_CHOL.get(PRED_VAL.toString())+chol_pts;
			}else if(MAP_PREDICTOR.contentEquals("sbp"))
				BP.append(PRED_VAL);
			else if(MAP_PREDICTOR.contentEquals("dbp")){
				BP.append(PRED_VAL.toString());
				//Log.i("ch10yr","bp is " + BP);
				ldl_pts =MAP_BP.get(BP.toString())+ldl_pts;
				chol_pts =MAP_BP.get(BP.toString())+chol_pts;
			}
			else if(MAP_PREDICTOR.contentEquals("risk")){
				ldl_pts =MAP_RISK.get(PRED_VAL.toString())+ldl_pts;
				chol_pts =MAP_RISK.get(PRED_VAL.toString())+chol_pts;
			}
			else if(MAP_PREDICTOR.contentEquals("execute"))//do nothing
				continue;
			//return null; //exit if unknown condition is found
			//Log.i("ch10yr","done with " + MAP_PREDICTOR);
		}

		//Get Point for each conditions
		ldl_risk=MAP_LDLRISK.get(ldl_pts);
		chol_risk=MAP_CHOLRISK.get(chol_pts);
	}
	/*
	public static void main(String args[]) {
		// TODO Auto-generated method stub

		// Testing of getRisk
		Map<String, String> answers = 
				Collections.unmodifiableMap(new HashMap<String, String>() {{ 
					put("age","40-44");
					put("ldlc","100-159 [2.6-4.14]");
					put("chol","240-279 [6.22-7.24]");
					put("hdls","greater than 60 [greater than 1.56]");
					put("sbp","120-129");
					put("dbp","90-99");
					put("risk","smoker");
				}});

		chd10yr myRisk= new chd10yr();
		myRisk.getRisks(answers); //caculate risk
		//System.out.println("CHD LDL Risk is= " + myRisk.ldl_risk + " CHD Chol Risk is= " + myRisk.chol_risk);
	}
	 */
}
