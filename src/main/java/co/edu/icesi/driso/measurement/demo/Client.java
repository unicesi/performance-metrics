package co.edu.icesi.driso.measurement.demo;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

import co.edu.icesi.driso.measurement.metrics.Metric;
import co.edu.icesi.driso.measurement.metrics.MetricException;
import co.edu.icesi.driso.measurement.metrics.MetricFactory;
import co.edu.icesi.driso.measurement.report.MetricsWindow;

/**
 * This class provides examples on how to use the classes in package 
 * org.driso.measurement
 * 
 * @author Miguel A. Jiménez
 * @date 26/11/2014
 */
public class Client {

	public static Metric father;

	public static void main(String[] args) throws MetricException {

		String path = "/Users/migueljimenez/Desktop";

		MetricFactory.registerConfigClass(
				"apvillota", 
				"co/edu/icesi/driso/measurement/demo/configFile.properties", 
				ClientConfig.class);
		
		father = MetricFactory.getMetric("apvillota", "grid0-control");

		Metric child1 = MetricFactory.getMetric("apvillota", "grid11-apvillota-01");
		Metric child2 = MetricFactory.getMetric("apvillota", "grid2-apvillota-02");
		Metric child3 = MetricFactory.getMetric("apvillota", "grid7-apvillota-03");
		Metric child4 = MetricFactory.getMetric("apvillota", "grid8-apvillota-04-05");

		// Set father's data
		father.setAttribute("NODE", "grid0");
		father.setAttribute("COMPOSITE", "composite0");
		father.setAttribute("COMPONENT", "compPK");

		// Set children's data	
		child1.setAttribute("NODE", "grid1");
		child1.setAttribute("COMPOSITE", "composite1");
		child1.setAttribute("COMPONENT", "compXY");

		child2.setAttribute("NODE", "grid2");
		child2.setAttribute("COMPOSITE", "composite2");
		child2.setAttribute("COMPONENT", "compXYX");

		child3.setAttribute("NODE", "grid3");
		child3.setAttribute("COMPOSITE", "composite3");
		child3.setAttribute("COMPONENT", "compXYX");

		child4.setAttribute("NODE", "grid4");
		child4.setAttribute("COMPOSITE", "composite3");
		child4.setAttribute("COMPONENT", "compXYX");

		// merge
		child1.setMeasure("Merge", "One", "Start", 2);
		child1.setMeasure("Merge", "One", "End", 110);
		child2.setMeasure("Merge", "One", "Start", 9);
		child2.setMeasure("Merge", "One", "End", 56);

		child1.setMeasure("Merge", "Two", "Start", 2);
		child1.setMeasure("Merge", "Two", "End", 45);
		child2.setMeasure("Merge", "Two", "Start", 9);
		child2.setMeasure("Merge", "Two", "End", 90);

		child1.setMeasure("Merge", "Three", "Start", 2);
		child1.setMeasure("Merge", "Three", "End", 98);
		child2.setMeasure("Merge", "Three", "Start", 10);
		child2.setMeasure("Merge", "Three", "End", 88);

		// sorting
		// Using the default level
		child3.setMeasure("Sorting", "Start", 0);
		child3.setMeasure("Sorting", "End", 90);

		child4.setMeasure("Sorting", "Start", 5);
		child4.setMeasure("Sorting", "End", 10);

		child3.setMeasure("Distribution", "Start", 40);
		child3.setMeasure("Distribution", "End", 20);

		child4.setMeasure("Distribution", "Start", 10);
		child4.setMeasure("Distribution", "End", 30);

		// merge shared measurements from children to father
		father.merge(child1, child2, child3, child4);

		try {

			father.report(new File(path + "/log-metricas-father.txt"), false);

		} catch (IOException e) {
			e.printStackTrace();
		}

		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				MetricsWindow w1 = new MetricsWindow(
						MetricsWindow.ChartType.CALCULATED_METRICS,
						"Performance Metrics Report", 
						father,
						"Performance Chart",
						"Processing nodes",
						"Measurement phases");
				w1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				w1.setVisible(true);

				MetricsWindow w2 = new MetricsWindow(
						MetricsWindow.ChartType.ACTUAL_METRICS,
						"Performance Metrics Report", 
						father,
						"Performance Chart",
						"Processing nodes",
						"Measurement phases");
				w2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				w2.setVisible(true);
			}
		});

	}

}
