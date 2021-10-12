package ru.axu.signer;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Watermark {
	private String text;

	private String fontName;
	private float fontMaxSize;
	private Color fontColor;

	private int transparency;

	private int offset;

	private boolean flip;

	private Map<IntPair, Integer> fontSizeCache;

	public Watermark() {
		text = "*** водный знак *** водный знак *** водный знак ***";
		fontName = "Arial";
		fontMaxSize = 100;
		fontColor = Color.BLACK;
		transparency = 10;
		offset = 10;
		flip = false;

		fontSizeCache = new HashMap<>();

		PdfUtils.registerSystemFontsOnce();
	}

	public void setText(String text) {
		if (text == null || text.isEmpty()) {
			throw new IllegalArgumentException("Argument value can't be null or empty!");
		}

		this.text = text;
	}

	public void setFontName(String fontName) {
		if (fontName == null) {
			throw new IllegalArgumentException("Argument value can't be null!");
		}

		this.fontName = fontName;
	}

	public void setFontMaxSize(int fontMaxSize) {
		if (fontMaxSize < 1) {
			throw new IllegalArgumentException("Argument value can't be less than 1!");
		}

		this.fontMaxSize = fontMaxSize;
	}

	public void setFontColor(Color fontColor) {
		if (fontColor == null) {
			throw new IllegalArgumentException("Argument value can't be null!");
		}

		this.fontColor = fontColor;
	}

	public void setTransparency(int transparency) {
		if (transparency < 0 || transparency > 100) {
			throw new IllegalArgumentException("Argument value can't be less than 0 and greater than 100!");
		}

		this.transparency = transparency;
	}

	public void setOffset(int offset) {
		if (offset < 0 || offset > 20) {
			throw new IllegalArgumentException("Argument value can't be less than 0 and greater than 20!");
		}

		this.offset = offset;
	}

	public void setFlip(boolean flip) {
		this.flip = flip;
	}

	private int recursiveFindFontSize(PdfFont font, String text, int targetLen, int fontMaxSize, int fontMinSize, int targetLenOffs, int angDeg) {
		int fontCurSize = fontMaxSize;
		int fontPrevSize = 0;

		while (true) {
			int tlCalcOffs = (int)((fontCurSize / 2) * Math.tan(Math.toRadians(90 - angDeg)));
			if (font.getWidth(text, fontCurSize) > targetLen - Math.max(tlCalcOffs, targetLenOffs) * 2) {
				fontPrevSize = fontCurSize;
				fontCurSize -= (fontCurSize - fontMinSize) / 2;
				if (fontCurSize == fontPrevSize) {
					return fontCurSize;
				}
			} else {
				if (fontPrevSize - fontCurSize < 1) {
					return fontCurSize;
				} else {
					return recursiveFindFontSize(font, text, targetLen, fontPrevSize, fontCurSize, targetLenOffs, angDeg);
				}
			}
		}
	}

	public void placeWatermark(PdfPage pdfPage, int pageNumber) throws Exception {
		PdfFont font = null;

		try {
			font = PdfFontFactory.createRegisteredFont(fontName, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
		} catch (IOException e) {
			throw new Exception("Can't create PDF font with name \"" + fontName + "\"");
		}

		float pageHeight = pdfPage.getPageSize().getHeight();
		float pageWidth = pdfPage.getPageSize().getWidth();

		IntPair pageHW = new IntPair((int)pageHeight, (int)pageWidth);

		float diagLen = (float) Math.sqrt((pageHeight * pageHeight) + (pageWidth * pageWidth));
		float rotAngle = (float) Math.asin(pageHeight / diagLen);

		int diagLenOffs = (int)(diagLen / 100 * offset);

		float actualFontSize;

		if (fontSizeCache.containsKey(pageHW)) {
			actualFontSize = fontSizeCache.get(pageHW);
		} else {
			actualFontSize = recursiveFindFontSize(font, text, (int)diagLen, (int)fontMaxSize, 1, diagLenOffs, (int) Math.toDegrees(rotAngle));
			fontSizeCache.put(pageHW, (int)actualFontSize);
		}

		PdfCanvas pageCanvas = new PdfCanvas(pdfPage);

		pageCanvas.saveState();

		pageCanvas.setFillColor(new DeviceRgb(fontColor));

		PdfExtGState gs1 = new PdfExtGState();
		gs1.setFillOpacity(transparency / 100.0f);
		pageCanvas.setExtGState(gs1);

		Paragraph watermarkPar = new Paragraph(text).setFont(font).setFontSize(actualFontSize);

		Canvas watermarkCvs = new Canvas(pageCanvas, pdfPage.getPageSize()).showTextAligned(watermarkPar, pageWidth / 2, pageHeight / 2, pageNumber, TextAlignment.CENTER, VerticalAlignment.MIDDLE, flip ? -1 * rotAngle : rotAngle);

		watermarkCvs.close();

		pageCanvas.restoreState();
	}
}
