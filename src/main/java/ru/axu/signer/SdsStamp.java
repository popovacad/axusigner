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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SdsStamp {
	private String signerText;
	private String posText;

	private LocalDateTime dateTime;

	private String keyIdText;
	private String docIdText;
	private String macText;

	private String fontName;
	private float fontSize;
	private Color fontColor;

	private float borderWidth;
	private Color borderColor;

	private StampPosition stampPosition;
	private float marginTb;
	private float marginLr;
	private float padding;

	private boolean macEnable;

	public SdsStamp() {
		signerText = "Фамилия Имя Отчество";
		posText = "должность";
		dateTime = LocalDateTime.now();
		keyIdText = "00000000-0000-0000-0000-000000000000";
		docIdText = "00000000-0000-0000-0000-000000000000";
		macText = "00000000";
		fontName = "Arial";
		fontSize = 10;
		fontColor = Color.BLACK;
		borderWidth = 2;
		borderColor = Color.BLACK;
		stampPosition = StampPosition.TOP_LEFT;
		marginTb = 10;
		marginLr = 10;
		padding = 10;
		macEnable = true;

		PdfUtils.registerSystemFontsOnce();
	}

	public void setSignerText(String signerText) {
		if (signerText == null) {
			throw new IllegalArgumentException("Argument value can't be null!");
		}

		this.signerText = signerText;
	}

	public void setPosText(String posText) {
		if (posText == null) {
			throw new IllegalArgumentException("Argument value can't be null!");
		}

		this.posText = posText;
	}

	public void setDateTime(LocalDateTime dateTime) {
		if (posText == null) {
			throw new IllegalArgumentException("Argument value can't be null!");
		}

		this.dateTime = dateTime;
	}

	public void setKeyIdText(String keyIdText) {
		if (keyIdText == null) {
			throw new IllegalArgumentException("Argument value can't be null!");
		}

		this.keyIdText = keyIdText;
	}

	public void setDocIdText(String docIdText) {
		if (docIdText == null) {
			throw new IllegalArgumentException("Argument value can't be null!");
		}

		this.docIdText = docIdText;
	}

	public void setMacText(String macText) {
		if (macText == null) {
			throw new IllegalArgumentException("Argument value can't be null!");
		}

		this.macText = macText;
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

	public void setBorderWidth(double borderWidth) {
		if (borderWidth < 0.1) {
			throw new IllegalArgumentException("Argument value can't be less than 0.1!");
		}

		this.borderWidth = (float)borderWidth;
	}

	public void setBorderColor(Color borderColor) {
		if (borderColor == null) {
			throw new IllegalArgumentException("Argument value can't be null!");
		}

		this.borderColor = borderColor;
	}

	public void setStampPosition(StampPosition stampPosition) {
		if (stampPosition == null) {
			throw new IllegalArgumentException("Argument value can't be null!");
		}

		this.stampPosition = stampPosition;
	}

	public void setMarginTb(int marginTb) {
		if (marginTb < 0) {
			throw new IllegalArgumentException("Argument value can't be less than 0!");
		}

		this.marginTb = marginTb;
	}

	public void setMarginLr(int marginLr) {
		if (marginLr < 0) {
			throw new IllegalArgumentException("Argument value can't be less than 0!");
		}

		this.marginLr = marginLr;
	}

	public void setPadding(int padding) {
		if (padding < 0) {
			throw new IllegalArgumentException("Argument value can't be less than 0!");
		}

		this.padding = padding;
	}

	public void setMacEnable(boolean macEnable) {
		this.macEnable = macEnable;
	}

	public void placeStamp(PdfPage pdfPage, int pageNumber) throws Exception {
		PdfFont font = null;

		try {
			font = PdfFontFactory.createRegisteredFont(fontName, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
		} catch (IOException e) {
			throw new Exception("Can't create PDF font with name \"" + fontName + "\"");
		}

		float pageHeight = pdfPage.getPageSize().getHeight();
		float pageWidth = pdfPage.getPageSize().getWidth();

		PdfCanvas pageCanvas = new PdfCanvas(pdfPage);

		pageCanvas.saveState();

		float signMaxWidth = 0.0f;

		String resSignedText = "Подписано простой электронной подписью";
		String resDateText = "Дата и время подписания: " + dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
		String resSignerText = "ФИО: " + signerText;
		String resPosText = "Должность: " + posText;
		String resKeyIdText = "Ключ: " + keyIdText;
		String resDocIdText = "Документ: " + docIdText;
		String resMacText = "Имитовставка: " + macText;

		signMaxWidth = Math.max(signMaxWidth, font.getWidth(resSignedText, fontSize));
		signMaxWidth = Math.max(signMaxWidth, font.getWidth(resSignerText, fontSize));
		signMaxWidth = Math.max(signMaxWidth, font.getWidth(resPosText, fontSize));
		signMaxWidth = Math.max(signMaxWidth, font.getWidth(resDateText, fontSize));
		signMaxWidth = Math.max(signMaxWidth, font.getWidth(resKeyIdText, fontSize));

		if (macEnable) {
			signMaxWidth = Math.max(signMaxWidth, font.getWidth(resDocIdText, fontSize));
			signMaxWidth = Math.max(signMaxWidth, font.getWidth(resMacText, fontSize));
		}

		int linesCount = 5;

		if (macEnable) {
			linesCount = 7;
		}

		float stampHeight = padding + ((fontSize * linesCount) + ((fontSize / 2) * (linesCount - 1))) + padding;
		float stampWidth = padding + signMaxWidth + padding;

		pageCanvas.setStrokeColor(new DeviceRgb(borderColor));
		pageCanvas.setLineWidth(borderWidth);

		switch (stampPosition) {
			case TOP_LEFT:
				pageCanvas.rectangle(marginLr, pageHeight - marginTb - stampHeight, stampWidth, stampHeight);
				break;
			case TOP_RIGHT:
				pageCanvas.rectangle(pageWidth - marginLr - stampWidth, pageHeight - marginTb - stampHeight, stampWidth, stampHeight);
				break;
			case BOTTOM_LEFT:
				pageCanvas.rectangle(marginLr, marginTb, stampWidth, stampHeight);
				break;
			case BOTTOM_RIGHT:
				pageCanvas.rectangle(pageWidth - marginLr - stampWidth, marginTb, stampWidth, stampHeight);
				break;
		}

		pageCanvas.stroke();

		float textPosX = 0.0f;
		float textPoxY = 0.0f;

		switch (stampPosition) {
			case TOP_LEFT:
				textPosX = marginLr + padding;
				textPoxY = pageHeight - marginTb - padding;
				break;
			case TOP_RIGHT:
				textPosX = pageWidth - marginLr - stampWidth + padding;
				textPoxY = pageHeight - marginTb - padding;
				break;
			case BOTTOM_LEFT:
				textPosX = marginLr + padding;
				textPoxY = marginTb + stampHeight - padding;
				break;
			case BOTTOM_RIGHT:
				textPosX = pageWidth - marginLr - stampWidth + padding;
				textPoxY = marginTb + stampHeight - padding;
				break;
		}

		pageCanvas.setFillColor(new DeviceRgb(fontColor));

		Canvas signatureCvs = new Canvas(pageCanvas, pdfPage.getPageSize());

		printStampText(signatureCvs, pageNumber, font, textPosX, textPoxY, resSignedText);

		textPoxY -= fontSize + (fontSize / 2);

		printStampText(signatureCvs, pageNumber, font, textPosX, textPoxY, resSignerText);

		textPoxY -= fontSize + (fontSize / 2);

		printStampText(signatureCvs, pageNumber, font, textPosX, textPoxY, resPosText);

		textPoxY -= fontSize + (fontSize / 2);

		printStampText(signatureCvs, pageNumber, font, textPosX, textPoxY, resDateText);

		textPoxY -= fontSize + (fontSize / 2);

		printStampText(signatureCvs, pageNumber, font, textPosX, textPoxY, resKeyIdText);

		if (macEnable) {
			textPoxY -= fontSize + (fontSize / 2);

			printStampText(signatureCvs, pageNumber, font, textPosX, textPoxY, resDocIdText);

			textPoxY -= fontSize + (fontSize / 2);

			printStampText(signatureCvs, pageNumber, font, textPosX, textPoxY, resMacText);
		}

		signatureCvs.close();

		pageCanvas.restoreState();
	}

	private void printStampText(Canvas canvas, int pageNumber, PdfFont font, float posX, float posY, String text) {
		Paragraph par = new Paragraph(text).setFont(font).setFontSize(fontSize);
		canvas.showTextAligned(par, posX, posY, pageNumber, TextAlignment.LEFT, VerticalAlignment.TOP, 0);
	}
}
