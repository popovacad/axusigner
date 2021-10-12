package ru.axu.signer;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;

import java.awt.*;
import java.io.IOException;

public class Copyright {
	private String line3text;

	private String fontName;
	private float fontSize;
	private Color fontColor;

	private boolean posLeft;

	public Copyright() {
		line3text = "";

		fontName = "Arial";
		fontSize = 8;
		fontColor = Color.BLACK;

		posLeft = false;

		PdfUtils.registerSystemFontsOnce();
	}

	public void setLine3text(String line3text) {
		if (line3text == null) {
			throw new IllegalArgumentException("Argument value can't be null!");
		}

		this.line3text = line3text;
	}

	public void setFontName(String fontName) {
		if (fontName == null) {
			throw new IllegalArgumentException("Argument value can't be null!");
		}

		this.fontName = fontName;
	}

	public void setFontSize(int fontSize) {
		if (fontSize < 1) {
			throw new IllegalArgumentException("Argument value can't be less than 1!");
		}

		this.fontSize = fontSize;
	}

	public void setFontColor(Color fontColor) {
		if (fontColor == null) {
			throw new IllegalArgumentException("Argument value can't be null!");
		}

		this.fontColor = fontColor;
	}

	public void setPosLeft(boolean posLeft) {
		this.posLeft = posLeft;
	}

	public void placeCopyright(PdfPage pdfPage, int pageNumber) throws Exception {
		PdfFont font = null;

		try {
			font = PdfFontFactory.createRegisteredFont(fontName, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
		} catch (IOException e) {
			throw new Exception("Can't create PDF font with name \"" + fontName + "\"");
		}

		float pageHeight = pdfPage.getPageSize().getHeight();
		float pageWidth = pdfPage.getPageSize().getWidth();

		boolean useLine3 = !line3text.isEmpty();

		float posX;
		TextAlignment al;

		if (posLeft) {
			posX = (fontSize / 2);
			al = TextAlignment.LEFT;
		} else {
			posX = pageWidth - (fontSize / 2);
			al = TextAlignment.RIGHT;
		}

		float posY = (fontSize / 2) + (fontSize * 2) + (fontSize / 2);

		if (useLine3) {
			posY += fontSize + (fontSize / 2);
		}

		PdfCanvas pageCanvas = new PdfCanvas(pdfPage);

		pageCanvas.saveState();

		pageCanvas.setFillColor(new DeviceRgb(fontColor));

		Canvas copyrightCvs = new Canvas(pageCanvas, pdfPage.getPageSize());

		Paragraph copyrightPar = new Paragraph(Constants.COPYRIGHT_1).setFont(font).setFontSize(fontSize);
		copyrightCvs.showTextAligned(copyrightPar, posX, posY, pageNumber, al, VerticalAlignment.TOP, 0);

		posY -= fontSize + (fontSize / 2);

		copyrightPar = new Paragraph(Constants.COPYRIGHT_2).setFont(font).setFontSize(fontSize);
		copyrightCvs.showTextAligned(copyrightPar, posX, posY, pageNumber, al, VerticalAlignment.TOP, 0);

		posY -= fontSize + (fontSize / 2);

		if (useLine3) {
			copyrightPar = new Paragraph(line3text).setFont(font).setFontSize(fontSize);
			copyrightCvs.showTextAligned(copyrightPar, posX, posY, pageNumber, al, VerticalAlignment.TOP, 0);
		}

		copyrightCvs.close();

		pageCanvas.restoreState();
	}
}
