package co.edu.icesi.driso.measurement.report;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import co.edu.icesi.driso.measurement.metrics.Measurement;
import co.edu.icesi.driso.measurement.metrics.MeasurementPhase;
import co.edu.icesi.driso.measurement.metrics.Metric;

/**
 * 
 * @author Miguel A. Jiménez
 * @date 07/12/2014
 */
public class CalculatedMetricsPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	protected final Metric metric;
	protected final String chartTitle;
	protected final String yAxisLabel;
	protected final String xAxisLabel;
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
	
	/**
	 * This is internally used to hide the white background while exporting 
	 * the PNG image
	 */
	private boolean exporting;

	public CalculatedMetricsPanel(Metric metric, String chartTitle, String yAxisLabel, String xAxisLabel){
		this.metric = metric;
		this.chartTitle = chartTitle;
		this.yAxisLabel = yAxisLabel;
		this.xAxisLabel = xAxisLabel;
		initialize();
	}
	
	/**
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void exportImage(File file) throws IOException {
		
		// Hides white background
		exporting = true;
		
		BufferedImage bi = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB); 
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
		FontMetrics smallFontMetrics = g2.getFontMetrics(smallFont);
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
		
		String longestLeftLabel = drawLeftLabels(g2, fontMetrics, x1, x2, y1, y2 - bottomLabelsHeight - fontPadding);
		leftLabelsWidth = fontMetrics.stringWidth(longestLeftLabel);
		
		// Assigned width for phases' levels
		int calculatedChartWidth = width - ((chartPadding * 2) + leftLabelsWidth + fontPadding + 1);
		
		drawBottomLabels(g2, fontMetrics, smallFontMetrics, calculatedChartWidth, bottomLabelsHeight, x1 + leftLabelsWidth + fontPadding + 1, x2, y1, y2);
		drawChartAxis(g2, x1 + leftLabelsWidth + fontPadding, x2, y1, y2 - bottomLabelsHeight);
		drawChartTitleAndLabels(g2, fontMetrics, boldFontMetrics, calculatedChartWidth, bottomLabelsHeight, x1 + leftLabelsWidth + fontPadding, x2, y1, y2);
	}
	
	/**
	 * 
	 * @param g2
	 * @param fontMetrics
	 * @param boldFontMetrics
	 * @param calculatedChartWidth
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 */
	public void drawChartTitleAndLabels(Graphics2D g2, FontMetrics fontMetrics,
			FontMetrics boldFontMetrics, int calculatedChartWidth, int bottomLabelsHeight, int x1, int x2, int y1, int y2){
		
		// Chart title
		int halfChartWidth = (int) (calculatedChartWidth / 2.0);
		int halfTitleWidth = (int) (boldFontMetrics.stringWidth(chartTitle) / 2.0);
		g2.setFont(boldFont);
		g2.drawString(chartTitle, x1 + halfChartWidth - halfTitleWidth, y1 - fontPadding);
		
		// Y axis label
		int halfYAxisLabelWidth = (int) (fontMetrics.stringWidth(yAxisLabel) / 2.0);
		g2.setFont(font);
		g2.drawString(yAxisLabel, x1 - halfYAxisLabelWidth, y1 - fontPadding);
		
		// X axis label
		int halfXAxisLabelWidth = (int) (fontMetrics.stringWidth(xAxisLabel) / 2.0);
		g2.drawString(xAxisLabel, x1 + halfChartWidth - halfXAxisLabelWidth, y2 + fontPadding * 3);
		
		// X axis unit
		g2.drawString(metric.getConfig().getMeasureUnit(), x2 + fontPadding, y2 - bottomLabelsHeight + 3);
	}
	
	/**
	 * 
	 * @param g2
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 */
	protected void drawChartAxis(Graphics2D g2, int x1, int x2, int y1, int y2){
		// X axis
		g2.setStroke(boldStroke);
		g2.drawLine(x1 + 1, y2, x2 + 2, y2);
		
		// Y axis
		g2.setStroke(basicStroke);
		g2.drawLine(x1, y1, x1, y2);
	}
	
	/**
	 * 
	 * @param g2
	 * @param fontMetrics
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 * @return
	 */
	protected String drawLeftLabels(Graphics2D g2, FontMetrics fontMetrics, 
		int x1, int x2, int y1, int y2){
		
		// The longest label, starting with the father
		String longest = metric.getIdentifier();
		int longestWidth = 0;
		int newX1 = 0;
		
		// Father's label space
		int y2Father = y2;
		y2 -= fontMetrics.getHeight() + fontPadding;
		
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

			g2.drawString(tempChild.getIdentifier(), x1Temp, y2);
			g2.drawLine(
					newX1 + 4, y2 - halfFontHeight, newX1 + 7, y2 - halfFontHeight);
			
			y2 -= fontHeight + fontPadding;
		}
		
		// Once the longest label have been found, draw the father's label
		g2.drawString(metric.getIdentifier(),
				newX1 - fontMetrics.stringWidth(metric.getIdentifier()), y2Father);
		g2.drawLine(
				newX1 + 4, y2Father - halfFontHeight, newX1 + 7, y2Father - halfFontHeight);
		
		return longest;
	}
	
	/**
	 * 
	 * @param g2
	 * @param fontMetrics
	 * @param smallFontMetrics
	 * @param calculatedChartWidth
	 * @param bottomLabelsHeight
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 */
	protected void drawBottomLabels(Graphics2D g2, FontMetrics fontMetrics, FontMetrics smallFontMetrics,
			int calculatedChartWidth, int bottomLabelsHeight,	int x1,	int x2, int y1, int y2){
		
		// Levels to be painted in the chart
		int totalLevels = 0;
		
		for (int i = 0; i < metric.getConfig().getPhases().size(); i++) {
			MeasurementPhase tempPhase = metric.getConfig().getPhases().get(i);
			totalLevels += tempPhase.getLevels().length;
		}
		
		// Calculated width for each level
		int levelWidth = (int) (calculatedChartWidth / Double.valueOf(totalLevels));
		
		for (int i = 0; i < metric.getConfig().getPhases().size(); i++) {
			MeasurementPhase tempPhase = metric.getConfig().getPhases().get(i);
			int nLevels = tempPhase.getLevels().length;
			int widthLevels = (nLevels * levelWidth) + nLevels;
			int phaseLabelWidth = fontMetrics.stringWidth(tempPhase.getName());
			
			int phaseLabelX = (int) (x1 + (widthLevels / 2.0) - (phaseLabelWidth / 2.0));
			
			// Draws the phase's name at the bottom
			Color phaseColor = colors[i % colors.length];
			Color phaseTextColor = darken(phaseColor, 0.70);
			boolean defaultLevel = tempPhase.getLevels().length == 1 && 
					tempPhase.getLevels()[0].getName().equals(
							MeasurementPhase.Level.DEFAULT_NAME);
			
			g2.setColor(phaseColor);
			g2.setFont(boldFont);
			
			if(defaultLevel){
				int offsetTempPhase = (fontHeight + fontPadding) * 2;
				g2.fillRect(x1, y2 - offsetTempPhase, widthLevels - 1, offsetTempPhase);
				g2.setColor(darken(phaseColor, darkenFactor));
				g2.drawRect(x1, y2 - offsetTempPhase, widthLevels - 1 - 1, offsetTempPhase - 1);
				g2.setColor(phaseColor);
				g2.setColor(phaseColor);
				g2.setColor(phaseTextColor);
				g2.drawString(tempPhase.getName(), phaseLabelX, y2 - fontHeight);
			}else{
				g2.fillRect(x1, y2 - (fontHeight + fontPadding), widthLevels - 1, 
						fontHeight + fontPadding);
				g2.setColor(darken(phaseColor, darkenFactor));
				g2.drawRect(x1, y2 - (fontHeight + fontPadding), widthLevels - 1 - 1, 
						fontHeight + fontPadding - 1);
				g2.setColor(phaseColor);
				g2.setColor(phaseTextColor);
				g2.drawString(tempPhase.getName(), phaseLabelX, y2 - fontPadding);
			}
			
			g2.setFont(font);
			g2.setColor(Color.BLACK);
			
			int xLevels = x1;
			
			for (int j = 0; j < tempPhase.getLevels().length; j++) {
				
				// Draw one vertical line at the end of the level
				g2.setStroke(dottedStroke);
				g2.setColor(new Color(200, 200, 200));
				g2.drawLine(xLevels + levelWidth, y1, xLevels + levelWidth, y2 - bottomLabelsHeight - 1);
				g2.setStroke(basicStroke);
				g2.setColor(Color.BLACK);
				
				MeasurementPhase.Level tempLevel = tempPhase.getLevels()[j];
				
				if(!defaultLevel){
					int levelLabelWidth = fontMetrics.stringWidth(tempLevel.getName());
					
					// Draws the level's name at the bottom
					phaseColor = brighten(phaseColor, brightenFactor);
					g2.setColor(phaseColor);
					
					g2.fillRect(xLevels, y2 - (fontHeight + fontPadding) * 2, 
							levelWidth, fontHeight + fontPadding - 1);
					g2.setColor(darken(phaseColor, darkenFactor));
					g2.drawRect(xLevels, y2 - (fontHeight + fontPadding) * 2, 
							levelWidth - 1, fontHeight + fontPadding - 1 - 1);
					g2.setColor(phaseColor);
					
					g2.setColor(phaseTextColor);
					g2.drawString(tempLevel.getName(), 
							xLevels + (int) (levelWidth / 2.0) - (int) (levelLabelWidth / 2.0), 
							y2 - fontMetrics.getHeight() - (fontPadding * 2));
					g2.setColor(Color.BLACK);
				}
				
				// draw horizontal lines per child (besides the father)
				int horizontalLinesY = y2 - bottomLabelsHeight + 2;
				double longestLevelValue = 0;
				for (int k = -1; k < metric.getChildren().size(); k++) {
					HashMap<String, List<Measurement.Entry>> tempMeasurements = null;
					
					if(k == -1) tempMeasurements = metric.getPhaseMeasurements(tempPhase);
					else tempMeasurements = metric.getChildren().get(k).getPhaseMeasurements(tempPhase);
					
					List<Measurement.Entry> levelMeasurements = tempMeasurements.get(tempLevel.getName());
					double tempLevelValue = metric.getConfig().scaleValue(tempPhase.calculateLevelValue(levelMeasurements));
					longestLevelValue = tempLevelValue > longestLevelValue ? tempLevelValue : longestLevelValue;
				}
				
				for (int k = -1; k < metric.getChildren().size(); k++) {
					HashMap<String, List<Measurement.Entry>> tempMeasurements = null;
					if(k == -1) tempMeasurements = metric.getPhaseMeasurements(tempPhase);
					else tempMeasurements = metric.getChildren().get(k).getPhaseMeasurements(tempPhase);

					List<Measurement.Entry> levelMeasurements = tempMeasurements.get(tempLevel.getName());
					double tempLevelValue = metric.getConfig().scaleValue(tempPhase.calculateLevelValue(levelMeasurements));
					
					if(tempLevelValue > 0){					
						int chartLevelValue = (int) ((tempLevelValue * levelWidth) / longestLevelValue);
						String barLabel = "" + tempLevelValue;
						
//						Color horizontalRectColor = 
//								brighten(colors[(k < 0 ? colors.length - 1 : k) % colors.length], brightenFactor * 1.4);
//						Color horizontalRectColor = colors[colorTemp++ % colors.length];
						Color horizontalRectColor = brighten(phaseColor, brightenFactor / 2);
						g2.setColor(horizontalRectColor);
						g2.fillRect(xLevels, horizontalLinesY - fontHeight 
								- (int)(fontPadding / 2.0) - 3, chartLevelValue, fontHeight + 4);
						
						g2.setColor(phaseTextColor);
						g2.setFont(smallFont);
						int tempLabelX = Math.max(xLevels + 2, xLevels + (int)(chartLevelValue / 2.0) - 
								(int)(smallFontMetrics.stringWidth(barLabel) / 2.0));
						g2.drawString(barLabel, tempLabelX, 
								horizontalLinesY - fontPadding - 2);
						g2.setFont(font);
					}
					
					horizontalLinesY -= fontHeight + fontPadding;
					
				}
				
				g2.setColor(Color.BLACK);
				xLevels += levelWidth + 1;
			}
			
			x1 += widthLevels;
		}
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
    
    public boolean isExporting(){
    	return exporting;
    }
	
}
