package ru.axu.signer;

import com.itextpdf.kernel.font.PdfFontFactory;

public class PdfUtils {
	private static boolean systemFontsRegistered = false;

	public static void registerSystemFontsOnce() {
		if (!systemFontsRegistered) {
			PdfFontFactory.registerSystemDirectories();
			systemFontsRegistered = true;
		}
	}
}
