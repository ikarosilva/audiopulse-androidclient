/* ===========================================================
 * SanaAudioPulse : a free platform for teleaudiology.
 *              
 * ===========================================================
 *
 * (C) Copyright 2012, by Sana AudioPulse
 *
 * Project Info:
 *    SanaAudioPulse: http://code.google.com/p/audiopulse/
 *    Sana: http://sana.mit.edu/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * [Android is a trademark of Google Inc.]
 *
 * -----------------
 * AudioPulseCalibrationActivity.java
 * -----------------
 * (C) Copyright 2012, by SanaAudioPulse
 *
 * Original Author:  Ikaro Silva
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * Check: http://code.google.com/p/audiopulse/source/list
 */ 

package org.audiopulse.activities;

import org.audiopulse.R;
import org.audiopulse.tests.DPOAEProcedure;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

//Contains menu from which tests can be selected and run
public class TestMenuActivity extends AudioPulseActivity 
{
	public static final String TAG="TestMenuActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_menu);
		
		// set actions for clickable menu items
		ListView menuList = (ListView) findViewById(R.id.menu_list);
        menuList.setOnItemClickListener(
        	new AdapterView.OnItemClickListener() {
        		public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
        			
        			TextView item = (TextView) itemClicked;
        			String itemText = item.getText().toString();
        			        			//item.getId(), R.id.??
        			
        			//TODO: Tests should return a Bundle containing the data they are suppose to provide,
        			//and URLs to any file location they create
        			if (itemText.equalsIgnoreCase(getResources().getString(R.string.calibration_title))) {
        				//Start menu with calibration related activities
        				startActivity(new Intent(TestMenuActivity.this, CalibrationMenuActivity.class));

        			} else if(itemText.equalsIgnoreCase(getResources().getString(R.string.DPOAE_RIGHT))) {
        				//TODO: Pass Ear information to file
        				Intent DPOAEIntent = new Intent(TestMenuActivity.this, DPOAEActivity.class);
        				startActivity(DPOAEIntent);
        				
        			} else if(itemText.equalsIgnoreCase(getResources().getString(R.string.DPOAE_LEFT))) {
        				//TODO: Pass Ear information to file
        				Intent DPOAEIntent = new Intent(TestMenuActivity.this, DPOAEActivity.class);
        				startActivity(DPOAEIntent);	
        			}else if(itemText.equalsIgnoreCase(getResources().getString(R.string.audio_calibration))) {
        				startActivity(new Intent(TestMenuActivity.this, AudioCalibrationActivity.class));
        			} else if(itemText.equalsIgnoreCase(getResources().getString(R.string.tests_device_calibration))) {
        				startActivity(new Intent(TestMenuActivity.this, InputCalibrationActivity.class));
        			} else if(itemText.equalsIgnoreCase(getResources().getString(R.string.TEOAE_RIGHT))) {
        				//TODO: Pass Ear information to file
        				Bundle tests = new Bundle();
        				tests.putString("testName",itemText);
        				Intent testIntent = new Intent(TestMenuActivity.this, TEOAEActivity.class);
        				testIntent.putExtras(tests);
        				startActivity(testIntent);
        			} else if(itemText.equalsIgnoreCase(getResources().getString(R.string.TEOAE_LEFT))) {
        				//TODO: Pass Ear information to file
        				Bundle tests = new Bundle();
        				tests.putString("testName",itemText);
        				Intent testIntent = new Intent(TestMenuActivity.this, TEOAEActivity.class);
        				testIntent.putExtras(tests);
        				startActivity(testIntent);
        			} else if(itemText.equalsIgnoreCase(getResources().getString(R.string.menu_debug))) {
        				Intent testIntent = new Intent(TestMenuActivity.this, TestActivity.class);
        				startActivity(testIntent);
        			}
        			
        			else {
        				//TODO: launch test activity
        			} 
        			
        		}
        	}
		);
	}
    
	
	
	
}