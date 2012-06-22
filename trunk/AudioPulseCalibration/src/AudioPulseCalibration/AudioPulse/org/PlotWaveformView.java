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
 * PlotWaveformView.java 
 * based on DeviationRendererDemo02View.java
 * from afreechartdemo
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
package AudioPulseCalibration.AudioPulse.org;

import java.util.Random;

import org.afree.chart.ChartFactory;
import org.afree.chart.AFreeChart;
import org.afree.chart.plot.PlotOrientation;
import org.afree.chart.plot.XYPlot;
import org.afree.data.xy.XYDataset;
import org.afree.data.xy.XYSeries;
import org.afree.data.xy.XYSeriesCollection;

import AudioPulseCalibration.AudioPulse.org.DemoView;

import android.content.Context;

/**
 * DeviationRendererDemo02View
 */
public class PlotWaveformView extends DemoView {

	/**
	 * constructor
	 * @param context
	 */
	public PlotWaveformView(Context context) {
		super(context);

		final AFreeChart chart = createChart2();
		setChart(chart);
	}

	
    private static XYSeriesCollection createDataset2() {	
    		XYSeriesCollection result = new XYSeriesCollection();
        	XYSeries series = new XYSeries(1);
        	XYSeries series2 = new XYSeries(2);
        	Random generator = new Random();
        	double output=0;
        	int N =5000;
    		int[] a = new int[N];
    		for(int n=0;n<N;n++){
    			if(n<3){
    				a[n]=1;
    				output=1;
    			}else{
    				a[n]=a[a[n-1]]+ a[n-a[n-1]];
    				output=(double) a[n] - ((double)n)/2;
    			}
    			series.add(n, output);
    			series2.add(n, generator.nextGaussian()-output);
    		}
        	
        	result.addSeries(series);
        	result.addSeries(series2);
            return result;
    }


private static AFreeChart createChart2() {
	XYDataset dataset = createDataset2();
	// create the chart...
	AFreeChart chart = ChartFactory.createXYLineChart(
			"RE DPOAE Test", // chart title
			"Frequency (kHz)", // x axis label
			"DPOAE Level (dB SPL)", // y axis label
			dataset, // data
			PlotOrientation.VERTICAL,
			true, // include legend
			true, // tooltips
			false // urls
			);
        
	XYPlot plot = (XYPlot) chart.getPlot();
    plot.getDomainAxis().setLowerMargin(0.0);
    plot.getDomainAxis().setUpperMargin(0.0);
    return chart;
}
}


