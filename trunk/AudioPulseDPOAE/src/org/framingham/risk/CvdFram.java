package org.framingham.risk;
import java.lang.System;

public class CvdFram {
	/**
	 * Following inputs are required
	 * 
	 * @param aGE
	 *            - integer (AGE)
	 * @param sMOKER
	 *            - bool ( 1(TRUE)-smoker 0(false)-nonsmoker )
	 * @param gENDER
	 *            - bool ( 0(TRUE)-male 1(false)-female)
	 * @param bPSys
	 *            - integer (Systolic blood pressure)
	 * @param bChol
	 *            - float (Total cholesterol)
	 * @param hDL
	 *            - float (HDL)
	 * @param bG
	 *            - bool (1(TRUE):diabetic 0(FALSE):non-diabetic)
	 * @param lvh
	 *            - int (1:presence of ECG-LVH, 0:absence of ECG-LVG,
	 *            {3-unknown= incase you have made provisions for missing data})
	 */
	private int age;
	private boolean smoke;
	private boolean morf;
	private int sbp;
	private float tc;
	private float hdl;
	private boolean bloodglu;
	private int ecglvh;
	private float myRisk;

	public CvdFram() {
		//initialise some default values - later overridden by method insertFactors()
		age = 5;
		smoke = true;
		morf = true;// male
		sbp = 156;
		tc = (float) 6.7;
		hdl = (float) 2.55;
		bloodglu = true;// diabetic
		ecglvh = 1;//present
		myRisk = 0;
	}

	public void insertFactors(int N_aGE, boolean N_sMOKER, boolean N_gENDER,
			int N_bPSys, double N_tc, double N_hDL, boolean N_bG, int N_lvh) {
		age = N_aGE;
		smoke = N_sMOKER;
		morf = N_gENDER;
		sbp = N_bPSys;
		tc = (float) N_tc;
		hdl = (float) N_hDL;
		bloodglu = N_bG;
		ecglvh = N_lvh;
	}

	public void calltheScore() {
		myRisk = myFraminghamScore(age, smoke, morf, sbp, tc, hdl, bloodglu,
				ecglvh, "Indian");
	}

	public void displaytheScore() {
		System.out.println("the risk is " + myRisk);
	}

	private float myFraminghamScore(int aGE, boolean sMOKER, boolean gENDER,
			int bPSys, float bChol, float hDL, boolean bG, int lvh,
			String eThnic) {

		String c3, c4, c2;
		int smo;
		float ratio = 1;
		int diab = 0;
		float framinghamRiskScore;
		if (sMOKER == true) { // true(means=1) means smoker
			// CVD=CVD+5;
			c4 = "s";
			smo = 1;
		}

		else {
			c4 = "ns";
			smo = 0;
		}

		int whatGender;
		if (gENDER == true) { // true(means=0) means male
			// CVD=CVD+1;

			c3 = "m";
			whatGender = 0;
		}

		else {
			c3 = "f";
			whatGender = 1;
		}

		if (bChol != 0 && hDL != 0) {
			ratio = bChol / hDL;

		}

		if (bG == true) { // true(means=1) means diabetic
			// indicates the presence of diabetes mellitus
			c2 = "d";
			diab = 1;
		} else if (bG == false) {
			c2 = "ud"; // nondiabetic
			diab = 0;
		}

		// if the ethnicity is south asian, set the value to 1 else 0
		return framinghamRiskScore = calcFramingham(smo, aGE, diab, whatGender,
				bPSys, ratio, lvh);

	}

	private float calcFramingham(int smo, int AGE, int diab, int whatGender,
			int BP_sys, float ratio, int lvh) {
		double fram1, fram2 = 0;
		double u, sigma, uu;
		System.out.println(ratio);
		u = 18.8144 + (-1.2146 * whatGender) + (-1.8443 * Math.log(AGE))
				+ (0.3668 * Math.log(AGE) * whatGender)
				+ (-1.4032 * Math.log(BP_sys)) + (-0.3899 * smo)
				+ (-0.5390 * Math.log(ratio)) + (-.3036 * diab)
				+ (-0.1697 * diab * whatGender) + (-0.3662 * lvh);
		sigma = Math.exp(0.6536 + (-0.2402 * u));
		uu = (Math.log(10) - u) / sigma; // for t=10years
		fram1 = (1 - Math.exp(-Math.exp(uu)));

		// if (ETHNIC == true && GENDER == true) {
		// South-Asian men indicates cvd_risk*1.4 - NICE
		// guidelines
		// fram2 = fram1 * 1.4;
		// fram_cvd_risk = (float) (fram2 * 100);
		//
		// } else {
		float fram_cvd_risk = (float) (fram1 * 100);
		
		// }

		return (float) fram_cvd_risk;

	}

	public static void main(String[] args) {

		CvdFram myscore = new CvdFram();
		myscore.insertFactors(55, true, true, 132, 5.5, 4.1, true, 0);
		/**the entry may follow the order:
		 * @param N_aGE
		 * @param N_sMOKER
		 * @param N_gENDER (true=0=male)
		 * @param N_bPSys
		 * @param N_tc
		 * @param N_hDL
		 * @param N_bG
		 * @param N_lvh
		 */
		myscore.calltheScore();
		
		myscore.displaytheScore();

	}
}
