
package AudioPulseCalibration.AudioPulse.org;

import org.afree.chart.ChartFactory;
import org.afree.chart.AFreeChart;
import org.afree.chart.plot.PlotOrientation;
import org.afree.chart.plot.XYPlot;
import org.afree.chart.renderer.xy.DeviationRenderer;
import org.afree.data.xy.XYDataset;
import org.afree.data.xy.YIntervalSeries;
import org.afree.data.xy.YIntervalSeriesCollection;
import org.afree.graphics.SolidColor;
import AudioPulseCalibration.AudioPulse.org.DemoView;

import android.content.Context;
import android.graphics.Color;

/**
 * DeviationRendererDemo02View
 */
public class DeviationRendererDemo02View extends DemoView {

	/**
	 * constructor
	 * @param context
	 */
	public DeviationRendererDemo02View(Context context) {
		super(context);

		final AFreeChart chart = createChart2();

		setChart(chart);
	}

	/**
	 * Creates a sample dataset.
	 * @return a sample dataset.
	 */
	
    private static YIntervalSeriesCollection createDataset2() {
    	
    	
    	YIntervalSeries series1 = new YIntervalSeries("RE DPOAE");
    	int[] NUB={-10, -5, -5, -5, -4};
    	int[] NLB={-15, -10, -13, -15, -13};
    	
		series1.add(7.206, -7,NLB[0], NUB[0]);
		series1.add(5.083, 13.1,NLB[1], NUB[1]);
		series1.add(3.616, 17.9,NLB[2], NUB[2]);
		series1.add(2.542, 11.5,NLB[3], NUB[3]);
		series1.add(1.818, 17.1,NLB[4], NUB[4]);
		
		YIntervalSeriesCollection dataset = new YIntervalSeriesCollection();
		dataset.addSeries(series1);
		return dataset;
    }

	/**
	 * Creates a chart.
	 * @param dataset the data for the chart.
	 * @return a chart.
	 */

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

	DeviationRenderer renderer = new DeviationRenderer(true, false);
	renderer.setSeriesStroke(0, 3.0f);
	renderer.setSeriesStroke(1, 3.0f);
	
	renderer.setSeriesPaintType(0, new SolidColor(Color.rgb(0, 0, 255)));
	renderer.setSeriesFillPaintType(0, new SolidColor(Color.rgb(250, 100, 100)));
	//renderer.setSeriesFillPaintType(1, new SolidColor(Color.rgb(0, 0, 0)));
	//renderer.setSeriesFillPaintType(2, new SolidColor(Color.rgb(0, 0, 0)));
	//renderer.setSeriesFillPaintType(3, new SolidColor(Color.rgb(0, 0, 0)));
	plot.setRenderer(renderer);

	return chart;
}
}


