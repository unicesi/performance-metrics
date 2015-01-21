package co.edu.icesi.driso.measurement.report;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import co.edu.icesi.driso.measurement.metrics.Measurement;
import co.edu.icesi.driso.measurement.metrics.MeasurementPhase;
import co.edu.icesi.driso.measurement.metrics.Metric;

public class ActualMetricsPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	protected final Metric metric;
	protected final String chartTitle;
	protected final String yAxisLabel;
	protected int chartPadding;
	protected int fontPadding;
	protected int fontHeight;
	protected int halfFontHeight;
	protected int leftLabelsWidth;
	protected int bottomLabelsHeight;
	protected BasicStroke basicStroke;
	protected BasicStroke boldStroke;
	protected BasicStroke dottedStroke;
	protected Color[] colors;
	protected Font font;
	protected Font smallFont;
	protected Font boldFont;
	protected double brightenFactor;
	protected double darkenFactor;

	private HashMap<String, Color> colorsPerLevel;
	private int scaleFactor;
	private int barHeight;
	private int nodeBlockHeight;

	private JScrollPane parent;

	/**
	 * This is internally used to hide the white background while exporting 
	 * the PNG image
	 */
	private boolean exporting;

	public ActualMetricsPanel(Metric metric, String chartTitle, String yAxisLabel){
		this.metric = metric;
		this.chartTitle = chartTitle;
		this.yAxisLabel = yAxisLabel;
		initialize();
	}

	protected void initialize(){

		// General panel configuration
		exporting = false;
		setOpaque(false);

		// Temp values
		leftLabelsWidth = 0;
		bottomLabelsHeight = 0;

		// Chart padding
		chartPadding = 35;

		// Font padding (vertical space among labels)
		fontPadding = 7;

		// Strokes
		basicStroke = new BasicStroke(1);
		boldStroke = new BasicStroke(2);
		dottedStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, 
				BasicStroke.JOIN_MITER, 1.0f, new float[]{1.0f}, 0.0f);

		// Default colors to be used when drawing chart lines

		colors = new Color[]{
				new Color(152, 192, 220),
				new Color(253, 153, 140),
				new Color(254, 196, 124),
				new Color(193, 229, 130),
				new Color(202, 152, 203),
				new Color(161, 219, 210),
				new Color(212, 239, 206),

				hex2Rgb("#FFD3A7"),
				hex2Rgb("#FFE5A7"),
				hex2Rgb("#FFB3A7"),
				hex2Rgb("#90C8D8"),

				hex2Rgb("#FFBE7B"),
				hex2Rgb("#FFD87B"),
				hex2Rgb("#FF8D7B"),
				hex2Rgb("#5DA5B9"),

				hex2Rgb("#FCAA57"),
				hex2Rgb("#FCCA57"),
				hex2Rgb("#FC6E57"),
				hex2Rgb("#3A879D"),

				hex2Rgb("#E68C31"),
				hex2Rgb("#E6B031"),
				hex2Rgb("#E64A31"),
				hex2Rgb("#22778F"),

				hex2Rgb("#BB6814"),
				hex2Rgb("#BB8914"),
				hex2Rgb("#BB2B14"),
				hex2Rgb("#105E74")
		};

		// Fonts to draw chart labels
		font = new Font("Arial", Font.PLAIN, 13);
		smallFont = new Font("Arial", Font.PLAIN, 9);
		boldFont = new Font("Arial", Font.BOLD, 13);

		// Colors
		brightenFactor = 0.10;
		darkenFactor = 0.12;

		// Each level is assigned to a color
		colorsPerLevel = new HashMap<String, Color>();

		// Initial scale factor for the chart values
		scaleFactor = 15;

		// chart bars' height
		barHeight = 12;

		// Initial height
		nodeBlockHeight = 0;
	}

	public Color hex2Rgb(String colorStr) {
		return new Color(
				Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
				Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
				Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
	}

	@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;
		g2.setFont(font);

		// Chart variables
		FontMetrics fontMetrics = g2.getFontMetrics(font);
		FontMetrics boldFontMetrics = g2.getFontMetrics(boldFont);
		fontHeight = fontMetrics.getHeight();
		halfFontHeight = (int) (fontHeight / 2.0) - 4;
		int width = this.getWidth();
		int height = this.getHeight();

		int x1 = chartPadding;
		int x2 = width - chartPadding;
		int y1 = chartPadding;
		int y2 = height - chartPadding;

		int fontHeight = fontMetrics.getHeight();
		// * 2 corresponds to phase and level names (two vertical labels)
		bottomLabelsHeight = (fontHeight + fontPadding) * 2;

		// Panel configuration
		g2.setColor(Color.WHITE);
		if(!isExporting())
			g2.fillRect(0, 0, width, height);
		g2.setColor(Color.BLACK);

		// Area per node (height)
		int nBars = 0;
		for (int i = 0; i < metric.getConfig().getPhases().size(); i++) {
			MeasurementPhase tempPhase = metric.getConfig().getPhases().get(i);
			nBars += tempPhase.getLevels().length;
		}
		nodeBlockHeight = nBars * barHeight;

		// Left labels
		String longestLeftLabel = 
				drawLeftLabels(g2, fontMetrics, x1, x2, y1, 
						y2 - bottomLabelsHeight - 2);
		leftLabelsWidth = fontMetrics.stringWidth(longestLeftLabel);

		// Assigned width for phases' levels
		int calculatedChartWidth = 
				width - ((chartPadding * 2) + leftLabelsWidth + fontPadding + 1);

		drawChartAxis(
				g2, x1 + leftLabelsWidth + fontPadding, x2, y1, 
				y2 - bottomLabelsHeight);

		drawChartTitleAndLabels(
				g2, fontMetrics, boldFontMetrics, calculatedChartWidth, 
				bottomLabelsHeight, x1 + leftLabelsWidth + fontPadding, x2, y1, y2);

		drawChartBars(
				g2, metric, fontMetrics, x1 + leftLabelsWidth + 1, x2, 
				y2 - bottomLabelsHeight - 2, y1, y2 - bottomLabelsHeight);
	}

	protected void drawChartAxis(Graphics2D g2, int x1, int x2, int y1, int y2){
		// X axis
		g2.setStroke(boldStroke);
		g2.drawLine(x1 + 1, y2, x2 + 2, y2);

		// Y axis
		g2.setStroke(basicStroke);
		g2.drawLine(x1, y1, x1, y2);
	}

	public void drawChartTitleAndLabels(Graphics2D g2, FontMetrics fontMetrics,
			FontMetrics boldFontMetrics, int calculatedChartWidth,
			int bottomLabelsHeight, int x1, int x2, int y1, int y2) {

		y2 += 6;

		// Chart title
		int halfChartWidth = (int) (calculatedChartWidth / 2.0);
		int halfTitleWidth = (int) (boldFontMetrics.stringWidth(chartTitle) / 2.0);
		g2.setFont(boldFont);
		g2.drawString(chartTitle, x1 + halfChartWidth - halfTitleWidth, y1 - fontPadding);

		// Y axis label
		int halfYAxisLabelWidth = (int) (fontMetrics.stringWidth(yAxisLabel) / 2.0);
		g2.setFont(font);
		g2.drawString(yAxisLabel, x1 - halfYAxisLabelWidth, y1 - fontPadding);

		// X axis labels		
		int rectSize = 20;
		int xPhaseLabel = x1;
		int yPhaseLevel = y2;
		int iColor = 0;

		for (int i = 0; i < metric.getConfig().getPhases().size(); i++) {
			MeasurementPhase tempPhase = metric.getConfig().getPhases().get(i);
			MeasurementPhase.Level[] tempLevels = tempPhase.getLevels();

			if(tempLevels.length == 1 && 
					tempLevels[0].getName().equals(MeasurementPhase.Level.DEFAULT_NAME)){

				// Validate: xPhaseLabel is not greater than the chart width
				int tempEndLabel = xPhaseLabel + rectSize + 
						fontPadding + fontMetrics.stringWidth(tempPhase.getName());

				if(tempEndLabel > x2){
					xPhaseLabel = x1;
					yPhaseLevel += rectSize + fontPadding;
				}

				// Assign color
				Color tempColor = colors[iColor++ % colors.length];
				colorsPerLevel.put(
						tempPhase.getName() + ":" +
								MeasurementPhase.Level.DEFAULT_NAME, tempColor);

				// Fill rectangle and draw the name of the phase
				g2.setColor(tempColor);
				g2.fillRect(xPhaseLabel, yPhaseLevel - rectSize, rectSize, rectSize);
				g2.setColor(Color.BLACK);
				g2.drawString(tempPhase.getName(), xPhaseLabel + rectSize + fontPadding, yPhaseLevel - 5);

				xPhaseLabel += rectSize;
				xPhaseLabel += fontMetrics.stringWidth(tempPhase.getName());
				xPhaseLabel += fontPadding * 2;
			}else{
				for (int j = 0; j < tempLevels.length; j++) {
					MeasurementPhase.Level tempLevel = tempLevels[j];
					String strPhaseLevel = tempPhase.getName() + "[" + tempLevel.getName() + "]";

					// Validate: xPhaseLabel is not greater than the chart width
					int tempEndLabel = xPhaseLabel + rectSize + 
							fontPadding + fontMetrics.stringWidth(strPhaseLevel);

					if(tempEndLabel > x2){
						xPhaseLabel = x1;
						yPhaseLevel += rectSize + fontPadding;
					}

					// Assign color
					Color tempColor = colors[iColor++ % colors.length];
					colorsPerLevel.put(
							tempPhase.getName() + ":" + 
									tempLevel.getName(), tempColor);

					// Fill rectangle and draw the name of the level
					g2.setColor(tempColor);
					g2.fillRect(xPhaseLabel, yPhaseLevel - rectSize, rectSize, rectSize);
					g2.setColor(Color.BLACK);
					g2.drawString(strPhaseLevel, xPhaseLabel + rectSize + fontPadding, yPhaseLevel - 5);

					xPhaseLabel += rectSize;
					xPhaseLabel += fontMetrics.stringWidth(strPhaseLevel);
					xPhaseLabel += fontPadding * 2;
				}
			}
		}

		// X axis unit
		g2.drawString(metric.getConfig().getMeasureUnit(), x2 + fontPadding, y2 - bottomLabelsHeight + 3);
	}

	protected String drawLeftLabels(Graphics2D g2, FontMetrics fontMetrics, 
			int x1, int x2, int y1, int y2){

		// The longest label, starting with the father
		String longest = metric.getIdentifier();
		int longestWidth = 0;
		int newX1 = 0;
		int halfNodeBlockHeight = (int) (nodeBlockHeight / 2.0);
		Color nodesAreaColor = new Color(240, 240, 240);

		// Father's label space
		int y2Father = y2;
		y2 -= nodeBlockHeight;

		// Children's labels
		// First: find the longest label
		for (int i = 0; i < metric.getChildren().size(); i++) {
			Metric tempChild = metric.getChildren().get(i);
			longest = tempChild.getIdentifier().length() > longest.length() ? 
					tempChild.getIdentifier() : longest;
		}

		// Then: draw the labels from right to left
		longestWidth = fontMetrics.stringWidth(longest);
		newX1 = x1 + longestWidth;

		for (int i = 0; i < metric.getChildren().size(); i++) {
			Metric tempChild = metric.getChildren().get(i);
			int x1Temp = 
					newX1 - fontMetrics.stringWidth(tempChild.getIdentifier());

			g2.setColor(nodesAreaColor);
			g2.setStroke(dottedStroke);
			g2.drawLine(newX1 + fontPadding + 1, y2 - nodeBlockHeight, x2, y2 - nodeBlockHeight);
			g2.setStroke(basicStroke);
			g2.setColor(Color.BLACK);

			g2.drawString(
					tempChild.getIdentifier(), 
					x1Temp, y2 + halfFontHeight - halfNodeBlockHeight);
			g2.drawLine(
					newX1 + 4, y2 - halfNodeBlockHeight, 
					newX1 + 7, y2 - halfNodeBlockHeight);

			y2 -= nodeBlockHeight;
		}

		// Once the longest label have been found, draw the father's label
		g2.setColor(nodesAreaColor);
		g2.setStroke(dottedStroke);
		g2.drawLine(newX1 + fontPadding + 1, y2Father - nodeBlockHeight, x2, y2Father - nodeBlockHeight);
		g2.setStroke(basicStroke);
		g2.setColor(Color.BLACK);

		g2.drawString(metric.getIdentifier(),
				newX1 - fontMetrics.stringWidth(metric.getIdentifier()), 
				y2Father + halfFontHeight - halfNodeBlockHeight);

		g2.drawLine(
				newX1 + 4, y2Father - halfNodeBlockHeight, 
				newX1 + 7, y2Father - halfNodeBlockHeight);

		return longest;
	}

	private void drawChartBars(Graphics2D g2, Metric metric,
			FontMetrics fontMetrics, int x, int x2, int y, int y1, int y2){
		int leftOffset = 0;
		long executionStart = getExecutionStart(metric);
		ArrayList<Point> labelsPoint = new ArrayList<Point>();

		// Father's values
		fillRectanglePerLevel(g2, metric, executionStart, 
				leftOffset + fontPadding + x, x2, y2 - 1, y1, y2, labelsPoint);

		// Children's values
		for (int i = 0; i < metric.getChildren().size(); i++) {
			y -= nodeBlockHeight;
			fillRectanglePerLevel(g2, metric.getChildren().get(i), 
							executionStart, leftOffset + fontPadding + x, 
							x2, y, y1, y2, labelsPoint);
		}
	}

	private void fillRectanglePerLevel(Graphics2D g2, Metric metric, 
			long executionStart, int x, int x2, int y, int y1, int y2, 
			ArrayList<Point> labelsPoint){

		int rectX = x;
		int yOffset = 0;
		FontMetrics smallFontMetrics = g2.getFontMetrics(smallFont);
		
		ArrayList<String> drawnLabels = new ArrayList<String>();

		// Then: fill rectangle per level
		for (int i = 0; i < metric.getConfig().getPhases().size(); i++) {
			MeasurementPhase tempPhase = metric.getConfig().getPhases().get(i);
			MeasurementPhase.Level[] tempLevels = tempPhase.getLevels();

			// Fill a rectangle for each level
			for (int j = 0; j < tempLevels.length; j++) {
				MeasurementPhase.Level tempLevel = tempLevels[j];
				long[] levelValues = getFirstAndLastStageValues(metric, tempPhase, tempLevel);

				if(levelValues != null){
					double chartStartValue = metric.getConfig().scaleValue(levelValues[0] * scaleFactor - executionStart * scaleFactor);
					double chartEndValue = metric.getConfig().scaleValue(levelValues[1] * scaleFactor - executionStart * scaleFactor);
					String strChartStartValue = "" + (metric.getConfig().scaleValue(levelValues[0] - executionStart));
					
					// If the value to be drawn has been already drawn, skip
					if(drawnLabels.contains(strChartStartValue))
						continue;
					else
						drawnLabels.add(strChartStartValue);

					int barWidth = (int) (chartEndValue - chartStartValue);
					barWidth = barWidth == 0 ? 2 : barWidth;

					Color tempColor = 
							colorsPerLevel.get(tempPhase.getName() + ":" + tempLevel.getName());

					g2.setColor(new Color(230, 230, 230));
					g2.setStroke(dottedStroke);
					int xLeftLimit = rectX + (int) chartStartValue;
					int leftLimitWidth = smallFontMetrics.stringWidth(strChartStartValue);
					int halfStrWidth = (int) (leftLimitWidth / 2.0);

					// Left limit line
					g2.drawLine(xLeftLimit, y1, rectX + (int) chartStartValue, y2);
					
					// Verify collision with previous label
					int labelStart = xLeftLimit - halfStrWidth;
					int labelEnd = xLeftLimit - halfStrWidth 
							+ smallFontMetrics.stringWidth(strChartStartValue);
					Point tempPoint = new Point(labelStart, labelEnd);
					
					if(!verifyCollision(labelsPoint, labelStart, labelEnd)){
						labelsPoint.add(tempPoint);
						
						// Number labels
						g2.setColor(Color.BLACK);
						g2.setFont(smallFont);
						g2.drawString(strChartStartValue, xLeftLimit - halfStrWidth, y2 + fontPadding * 2);
						
						// Tick
						g2.setStroke(basicStroke);
						g2.drawLine(xLeftLimit, y2, rectX + (int) chartStartValue, y2 + 3);
					}

					// Bar
					g2.setColor(tempColor);
					g2.setStroke(basicStroke);
					g2.fillRect(rectX + (int) chartStartValue, y - barHeight - yOffset, barWidth, barHeight);
				}

				yOffset += barHeight;
			}
		}

		g2.setColor(Color.BLACK);
	}
	
	private boolean verifyCollision(ArrayList<Point> labelsPoint, int labelStart, int labelEnd){
		boolean b = false;
		
		if(!labelsPoint.contains(new Point(labelStart, labelEnd))){
			for (Point point : labelsPoint) {
				if(labelStart >= point.x && labelStart <= point.y 
						 || point.x >= labelStart && point.x <= labelEnd){
					System.out.println(point);
					b = true;
					break;
				}
			}
		}
		
		return b;
	}

	private long[] getFirstAndLastStageValues(Metric metric, MeasurementPhase phase, MeasurementPhase.Level level){
		long[] values = {Long.MAX_VALUE, Long.MIN_VALUE};
		boolean firstChanged = false, lastChanged = false;

		for (MeasurementPhase.Stage stage : level.getStages()) {
			Measurement m = 
					metric.getMeasurement(
							phase.getName(), level.getName(), stage.getName());

			if(m != null){
				if(m.hasValue() && m.getOwnValue() < values[0]){
					values[0] = m.getOwnValue();
					firstChanged = true;
				}else if(m.hasValue() && m.getOwnValue() > values[1]){
					values[1] = m.getOwnValue();
					lastChanged = true;
				}

				for (int i = 0; i < metric.getChildren().size(); i++) {
					long[] tempValues = getFirstAndLastStageValues(
							metric.getChildren().get(i), phase, level);

					if(tempValues != null){
						if(tempValues[0] < values[0]){
							values[0] = tempValues[0];
							firstChanged = true;
						}

						if(tempValues[1] > values[1]){
							values[1] = tempValues[1];
							lastChanged = true;
						}
					}
				}
			}
		}

		if(firstChanged && lastChanged){
			return values;
		}else{
			return null;
		}
	}

	private long getExecutionStart(Metric metric){
		long fewer = Long.MAX_VALUE;

		for(MeasurementPhase phase : metric.getConfig().getPhases()){
			for(MeasurementPhase.Level level : phase.getLevels()){
				for(MeasurementPhase.Stage stage : level.getStages()){
					Measurement m = 
							metric.getMeasurement(
									phase.getName(), level.getName(), stage.getName());

					if(m != null && m.hasValue() && m.getOwnValue() < fewer){
						fewer = m.getOwnValue();
					}

					for (int i = 0; i < metric.getChildren().size(); i++) {
						long tempFewer = getExecutionStart(metric.getChildren().get(i));
						if(tempFewer < fewer){
							fewer = tempFewer;
						}
					}
				}
			}
		}

		return fewer;
	}

	public void exportImage(File file) throws IOException {
		// Hides white background
		exporting = true;

		BufferedImage bi = new BufferedImage(this.getWidth(), 
				this.getHeight(), BufferedImage.TYPE_INT_ARGB); 
		Graphics g = bi.createGraphics();
		this.paint(g);
		g.dispose();

		if(!file.getName().endsWith(".png")){
			file = new File(file.getAbsolutePath() + ".png");
		}

		ImageIO.write(bi,"png", file);

		// Shows white background again
		exporting = true;
	}

	/**
	 * Makes a color brighten.
	 * Taken from: 
	 * http://stackoverflow.com/questions/18648142/creating-brighter-color-java
	 *
	 * @param color Color to make brighten.
	 * @param fraction Darkness fraction.
	 * @return Lighter color.
	 */
	protected Color brighten(Color color, double fraction) {

		int red = (int) Math.round(Math.min(255, color.getRed() + 255 * fraction));
		int green = (int) Math.round(Math.min(255, color.getGreen() + 255 * fraction));
		int blue = (int) Math.round(Math.min(255, color.getBlue() + 255 * fraction));

		int alpha = color.getAlpha();

		return new Color(red, green, blue, alpha);

	}

	/**
	 * Makes a color darken.
	 *
	 * @param color Color to make darken.
	 * @param fraction Darkness fraction.
	 * @return Darker color.
	 */
	protected Color darken(Color color, double fraction) {

		int red = (int) Math.round(Math.max(0, color.getRed() - 255 * fraction));
		int green = (int) Math.round(Math.max(0, color.getGreen() - 255 * fraction));
		int blue = (int) Math.round(Math.max(0, color.getBlue() - 255 * fraction));

		int alpha = color.getAlpha();

		return new Color(red, green, blue, alpha);

	}

	public int getScaleFactor(){
		return scaleFactor;
	}

	public void setScaleFactor(int scaleFactor){
		this.scaleFactor = scaleFactor;
		repaint();
		validate();
		parent.revalidate();
	}

	public void setViewportWidth(int width){
		setPreferredSize(new Dimension(width, getHeight()));
		repaint();
		parent.repaint();
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() { 
				parent.getHorizontalScrollBar()
					.setValue(parent.getHorizontalScrollBar().getMaximum());
				parent.revalidate();
			}
		});
	}

	public boolean isExporting(){
		return exporting;
	}

	public void setParent(JScrollPane parent){
		this.parent = parent;
	}
}
