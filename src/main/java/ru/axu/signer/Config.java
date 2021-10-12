package ru.axu.signer;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.Optional;
import java.util.Properties;

public class Config {
	private StampPosition stampPosition;
	private int stampMarginTb;
	private int stampMarginLr;
	private int stampPadding;
	private double stampBorderWidth;
	private Color stampBorderColor;
	private String stampFontName;
	private int stampFontSize;
	private Color stampFontColor;
	private boolean stampMacEnable;

	private String watermarkText;
	private String watermarkFontName;
	private int watermarkFontMaxSize;
	private Color watermarkFontColor;
	private int watermarkTransparency;
	private int watermarkOffset;
	private boolean watermarkFlip;

	private boolean copyrightDisable;
	private String copyrightLine3Text;
	private String copyrightFontName;
	private int copyrightFontSize;
	private Color copyrightFontColor;

	public Config() {
		stampPosition = StampPosition.TOP_LEFT;
		stampMarginTb = 10;
		stampMarginLr = 10;
		stampPadding = 8;
		stampBorderWidth = 2;
		stampBorderColor = Color.BLUE;
		stampFontName = "Arial";
		stampFontSize = 10;
		stampFontColor = Color.BLUE;
		stampMacEnable = true;

		watermarkText = "";
		watermarkFontName = "Arial";
		watermarkFontMaxSize = 100;
		watermarkFontColor = Color.BLACK;
		watermarkTransparency = 10;
		watermarkOffset = 10;
		watermarkFlip = false;

		copyrightDisable = true;
		copyrightLine3Text = "";
		copyrightFontName = "Arial";
		copyrightFontSize = 8;
		copyrightFontColor = Color.BLACK;
	}

	public void loadFromFile(File configFile) throws GeneralException {
		Properties configProps = new Properties();

		try {
			configProps.load(new FileInputStream(configFile));
		} catch (Exception e) {
			throw new GeneralException("Can't load config file \"" + configFile.getAbsolutePath() + "\"!", e);
		}

		stampPosition = StampPosition.fromString(Optional.ofNullable(configProps.getProperty("stamp.position")).filter(s -> !s.isEmpty()).orElse("tl"));
		stampMarginTb = MiscUtils.parseIntDef(configProps.getProperty("stamp.margin.tb"), 10);
		stampMarginLr = MiscUtils.parseIntDef(configProps.getProperty("stamp.margin.lr"), 10);
		stampPadding = MiscUtils.parseIntDef(configProps.getProperty("stamp.padding"), 8);
		stampBorderWidth = MiscUtils.parseDoubleDef(configProps.getProperty("stamp.border.width"), 2.0);
		stampBorderColor = MiscUtils.parseColorDef(configProps.getProperty("stamp.border.color"), Color.BLUE);
		stampFontName = Optional.ofNullable(configProps.getProperty("stamp.font.name")).filter(s -> !s.isEmpty()).orElse("Arial");
		stampFontSize = MiscUtils.parseIntDef(configProps.getProperty("stamp.font.size"), 10);
		stampFontColor = MiscUtils.parseColorDef(configProps.getProperty("stamp.font.color"), Color.BLUE);
		stampMacEnable = Boolean.parseBoolean(configProps.getProperty("stamp.mac.enable"));

		watermarkText = Optional.ofNullable(configProps.getProperty("watermark.text")).filter(s -> !s.isEmpty()).orElse("");
		watermarkFontName = Optional.ofNullable(configProps.getProperty("watermark.font.name")).filter(s -> !s.isEmpty()).orElse("Arial");
		watermarkFontMaxSize = MiscUtils.parseIntDef(configProps.getProperty("watermark.font.maxsize"), 100);
		watermarkFontColor = MiscUtils.parseColorDef(configProps.getProperty("watermark.font.color"), Color.BLACK);
		watermarkTransparency = MiscUtils.parseIntDef(configProps.getProperty("watermark.transparency"), 10);
		watermarkOffset = MiscUtils.parseIntDef(configProps.getProperty("watermark.offset"), 10);
		watermarkFlip = Boolean.parseBoolean(configProps.getProperty("watermark.flip"));

		copyrightDisable = Boolean.parseBoolean(configProps.getProperty("copyright.disable"));
		copyrightLine3Text = Optional.ofNullable(configProps.getProperty("copyright.line3.text")).filter(s -> !s.isEmpty()).orElse("");
		copyrightFontName = Optional.ofNullable(configProps.getProperty("copyright.font.name")).filter(s -> !s.isEmpty()).orElse("Arial");
		copyrightFontSize = MiscUtils.parseIntDef(configProps.getProperty("copyright.font.size"), 8);
		copyrightFontColor = MiscUtils.parseColorDef(configProps.getProperty("copyright.font.color"), Color.BLACK);
	}

	public StampPosition getStampPosition() {
		return stampPosition;
	}

	public int getStampMarginTb() {
		return stampMarginTb;
	}

	public int getStampMarginLr() {
		return stampMarginLr;
	}

	public int getStampPadding() {
		return stampPadding;
	}

	public double getStampBorderWidth() {
		return stampBorderWidth;
	}

	public Color getStampBorderColor() {
		return stampBorderColor;
	}

	public String getStampFontName() {
		return stampFontName;
	}

	public int getStampFontSize() {
		return stampFontSize;
	}

	public Color getStampFontColor() {
		return stampFontColor;
	}

	public boolean getStampMacEnable() {
		return stampMacEnable;
	}

	public String getWatermarkText() {
		return watermarkText;
	}

	public String getWatermarkFontName() {
		return watermarkFontName;
	}

	public int getWatermarkFontMaxSize() {
		return watermarkFontMaxSize;
	}

	public Color getWatermarkFontColor() {
		return watermarkFontColor;
	}

	public int getWatermarkTransparency() {
		return watermarkTransparency;
	}

	public int getWatermarkOffset() {
		return watermarkOffset;
	}

	public boolean getWatermarkFlip() {
		return watermarkFlip;
	}

	public boolean getCopyrightDisable() {
		return copyrightDisable;
	}

	public String getCopyrightLine3Text() {
		return copyrightLine3Text;
	}

	public String getCopyrightFontName() {
		return copyrightFontName;
	}

	public int getCopyrightFontSize() {
		return copyrightFontSize;
	}

	public Color getCopyrightFontColor() {
		return copyrightFontColor;
	}
}
