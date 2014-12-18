package co.edu.icesi.driso.measurement.report;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import co.edu.icesi.driso.measurement.metrics.Metric;

/**
 * 
 * @author Miguel A. Jiménez
 * @date 07/12/2014
 */
public class MetricsWindow extends JFrame implements ActionListener {

	public enum ChartType {
		ACTUAL_METRICS,
		CALCULATED_METRICS
	}

	private static final long serialVersionUID = 1L;
	private JMenuBar menuBar;
	private final String title;
	private final int initialFrameWidth;
	private final JPanel metricsPanel;
	private JScrollPane scrollContainer;
	private JSpinner scaleFactorSpinner;
	private JSpinner viewportSizeSpinner;
	private ChartType chartType;

	public MetricsWindow(ChartType chartType, String windowTitle, Metric metric, 
			String chartTitle, String yAxisLabel, String xAxisLabel){
		this(windowTitle, getPanel(chartType, metric, chartTitle, yAxisLabel, xAxisLabel));
		this.chartType = chartType;
	}

	public MetricsWindow(String title, JPanel panel){
		this.title = title;
		this.metricsPanel = panel;
		initialFrameWidth = 800;
		chartType = (panel instanceof ActualMetricsPanel) ? 
				ChartType.ACTUAL_METRICS : ChartType.CALCULATED_METRICS;

		changeLookAndFeel();
		initialize();
	}

	private static JPanel getPanel(ChartType chartType, 
			Metric metric, String chartTitle, String yAxisLabel, String xAxisLabel){

		if(chartType.equals(ChartType.ACTUAL_METRICS)){
			return new ActualMetricsPanel(metric, chartTitle, yAxisLabel);
		}else if(chartType.equals(ChartType.CALCULATED_METRICS)){
			return new CalculatedMetricsPanel(metric, chartTitle, yAxisLabel, xAxisLabel);
		}

		return null;
	}

	private void changeLookAndFeel(){
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}

	private void initialize(){

		setLayout(new BorderLayout());

		menuBar = new JMenuBar();

		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);

		JMenuItem exportImage = new JMenuItem("Export PNG");
		exportImage.setActionCommand("EXPORT_PNG");
		exportImage.addActionListener(this);

		JMenuItem exit = new JMenuItem("Exit");
		exit.setActionCommand("EXIT");
		exit.addActionListener(this);

		file.add(exportImage);
		file.add(exit);

		menuBar.add(file);

		// Scroll pane
		scrollContainer = new JScrollPane(
				metricsPanel, 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		scrollContainer.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		scrollContainer.setSize(getSize());

		if(chartType.equals(ChartType.ACTUAL_METRICS)){
			// Panel!
			ActualMetricsPanel actualMetricsPanel = ((ActualMetricsPanel) metricsPanel);

			setSize(new Dimension(initialFrameWidth, 800));
			actualMetricsPanel.setParent(scrollContainer);
			
			// helper panel
			JPanel topContent = new JPanel();
			topContent.setLayout(new FlowLayout());

			// Viewport spinner
			viewportSizeSpinner = new JSpinner(
					new SpinnerNumberModel(initialFrameWidth, 200, 10000, 100));
			viewportSizeSpinner.setFocusable(false);
			viewportSizeSpinner.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					((ActualMetricsPanel) metricsPanel)
					.setViewportWidth((Integer) viewportSizeSpinner.getValue());
				}
			});

			// Scale factor spinner
			int initialScaleFactor = actualMetricsPanel.getScaleFactor();

			scaleFactorSpinner = new JSpinner(
					new SpinnerNumberModel(initialScaleFactor, 0, 1000, 10));
			scaleFactorSpinner.setFocusable(false);
			scaleFactorSpinner.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					((ActualMetricsPanel) metricsPanel)
					.setScaleFactor((Integer) scaleFactorSpinner.getValue());
				}
			});

			topContent.add(new Label("Scale factor: "));
			topContent.add(scaleFactorSpinner);
			topContent.add(new Label("   Viewport width: "));
			topContent.add(viewportSizeSpinner);
			getContentPane().add(topContent, BorderLayout.NORTH);

		}else{
			setSize(new Dimension(initialFrameWidth, 550));
		}

		setJMenuBar(menuBar);
		setTitle(title);
		getContentPane().add(scrollContainer, BorderLayout.CENTER);
		setLocationRelativeTo(null);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if(command.equals("EXPORT_PNG")){
			JFileChooser chooser = new JFileChooser(); 
			chooser.setCurrentDirectory(new java.io.File("."));
			chooser.setDialogTitle("Export chart as PNG");
			chooser.setFileSelectionMode(JFileChooser.SAVE_DIALOG);

			if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) { 
				try {
					if(chartType.equals(ChartType.CALCULATED_METRICS)){
						((CalculatedMetricsPanel) metricsPanel).exportImage(chooser.getSelectedFile());
					}else if(chartType.equals(ChartType.ACTUAL_METRICS)){
						((ActualMetricsPanel) metricsPanel).exportImage(chooser.getSelectedFile());
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

		}else if(command.equals("EXIT")){
			System.exit(0);
		}
	}

}
