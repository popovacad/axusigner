package ru.axu.signer;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.Translate;
import org.bouncycastle.crypto.digests.GOST3411_2012_256Digest;
import org.bouncycastle.crypto.prng.DigestRandomGenerator;
import org.bouncycastle.crypto.prng.RandomGenerator;
import org.bouncycastle.util.encoders.Hex;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

public class Main {
	private static TextIO con;
	private static TextTerminal<?> term;

	private static RandomGenerator secureRandom;

  public static void main(String[] args) {
  	// Create and setup terminal

		con = TextIoFactory.getTextIO();
		term = con.getTextTerminal();

		term.getProperties().put("pane.title", "Консоль axusigner");
		term.getProperties().setPromptColor(Color.LIGHT_GRAY);
		term.getProperties().setInputColor(Color.WHITE);
		term.getProperties().setInputBold(true);

		// Translate terminal built-in prompts

		Translate.addTranslation("Invalid value", "Некорректное значение");
		Translate.addTranslation("Expected", "Необходимо");
		Translate.addTranslation("an integer", "целочисленное");
		Translate.addTranslation("value", "значение");
		Translate.addTranslation("Please enter one of", "Введите одно из значений");
		Translate.addTranslation("value between", "значение между");
		Translate.addTranslation("and", "и");
		Translate.addTranslation("Expected a string with at least", "Необходима строка, состоящая минимум из");
		Translate.addTranslation("character", "символа");
		Translate.addTranslation("characters", "символов");
		Translate.addTranslation("Expected format", "Необходимый формат");

		// Create secure random source based on GOST Streebog 256

		secureRandom = new DigestRandomGenerator(new GOST3411_2012_256Digest());
		secureRandom.addSeedMaterial(System.currentTimeMillis());

		// Print welcome message

		term.executeWithPropertiesConfigurator(
			p -> { p.setPromptColor(Color.ORANGE); p.setInputBold(true); },
			t -> {
				t.println("Добро пожаловать в ПО axusigner!");
				t.println(Constants.COPYRIGHT_2);
			}
		);

		// Prompt user for operation mode

		promptMode();

		System.exit(0);
	}

	private static void printSuccess(String m) {
		term.executeWithPropertiesConfigurator(
			p -> { p.setPromptColor(Color.GREEN); },
			t -> { t.println(m); }
		);
	}

	private static void printError(String m) {
		term.executeWithPropertiesConfigurator(
			p -> { p.setPromptColor(Color.RED); },
			t -> { t.println(m); }
		);
	}

	private static File checkCreateKeysDir() {
		File keysPath = new File("keys");

		if (!keysPath.exists()) {
			if (!keysPath.mkdir()) {
				printError("Не удается создать директорию для ключей \"" + keysPath.getAbsolutePath() + "\"");
				return null;
			}
		}

		return keysPath;
	}

	private static File checkCreateInDir() {
		File inPath = new File("in");

		if (!inPath.exists()) {
			if (!inPath.mkdir()) {
				printError("Не удается создать директорию для исходных файлов \"" + inPath.getAbsolutePath() + "\"");
				return null;
			}
		}

		return inPath;
	}

	private static File checkCreateOutDir() {
		File outPath = new File("out");

		if (!outPath.exists()) {
			if (!outPath.mkdir()) {
				printError("Не удается создать директорию для подписанных файлов \"" + outPath.getAbsolutePath() + "\"");
				return null;
			}
		}

		return outPath;
	}

	private static void printExceptionTrace(Exception e) {
		final String trace = MiscUtils.getStackTrace(e);

		term.executeWithPropertiesConfigurator(
			p -> { p.setPromptColor("#AA0000"); },
			t -> { t.print(trace); }
		);
	}

	private static void promptMode() {
		term.println();

		term.executeWithPropertiesConfigurator(
			p -> { p.setPromptColor(Color.GREEN); },
			t -> { t.println("Выберите режим работы:"); }
		);

		term.executeWithPropertiesConfigurator(
			p -> { p.setPromptColor(Color.WHITE); },
			t -> {
				t.println("1. Простая электронная подпись");
				t.println("0. Выход"); }
		);

		Integer selMode = con.newIntInputReader()
			.withInlinePossibleValues(1, 0)
			.read("Введите значение");

		term.println();

		switch (selMode) {
			case 1:
				promptModeSds();
				break;
			case 0:
				return;
		}

		promptMode();
	}

	private static void promptModeSds() {
		term.println();

		term.executeWithPropertiesConfigurator(
			p -> { p.setPromptColor(Color.GREEN); },
			t -> { t.println("Режим ПЭП. Выберите функцию:"); }
		);
		term.executeWithPropertiesConfigurator(
			p -> { p.setPromptColor(Color.WHITE); },
			t -> {
				t.println("1. Подписать документы ПЭП");
				t.println("2. Создать ключ ПЭП");
				t.println("3. Показать информацию о ключе ПЭП");
				t.println("4. Проверить имитовставку штампа ПЭП");
				t.println("0. Назад"); }
		);

		Integer selMode = con.newIntInputReader()
			.withInlinePossibleValues(1, 2, 3, 4, 0)
			.read("Введите значение");

		term.println();

		switch (selMode) {
			case 1:
				execSdsSign();
				break;
			case 2:
				execSdsCreateKey();
				break;
			case 3:
				execSdsShowKeyInfo();
				break;
			case 4:
				execSdsCheckMac();
				break;
			case 0:
				return;
		}

		promptModeSds();
	}

	private static void execSdsCreateKey() {
		term.println();

		term.executeWithPropertiesConfigurator(
			p -> { p.setPromptColor(Color.MAGENTA); },
			t -> { t.println("Создание ключа ПЭП."); }
		);

		term.println();

		File keysPath = checkCreateKeysDir();

		if (keysPath == null) {
			return;
		}

		String personText = con.newStringInputReader()
			.read("Введите ФИО");

		String positionText = con.newStringInputReader()
			.read("Введите должность");

		String fileNameText = con.newStringInputReader()
			.read("Введите имя файла ключа (без расширения)");

		int tryOuts = 3;

		String passwText;

		while (true) {
			passwText = con.newStringInputReader()
				.withInputMasking(true)
				.read("Введите пароль");

			String passw2Text = con.newStringInputReader()
				.withInputMasking(true)
				.read("Повторите пароль");

			if (!passwText.equals(passw2Text)) {
				tryOuts--;

				if (tryOuts == 0) {
					return;
				}

				printError("Пароли не совпадают. Осталось попыток: " + tryOuts);
			} else {
				break;
			}
		}

		term.println();

		// Now create key file

		SdsKeyEntity ke = new SdsKeyEntity(secureRandom);

		ke.setPersonText(personText);
		ke.setPositionText(positionText);

		File keyFile = new File(keysPath, fileNameText + ".askey");

		if (keyFile.exists()) {
			printError("Файл ключа \"" + keyFile.getAbsolutePath() + "\" уже существует и не будет перезаписан! Попробуйте снова.");

			term.println();

			return;
		}

		try {
			ke.saveToFile(keyFile, passwText);
		} catch (Exception e) {
			printError("Ошибка при создании файла ключа \"" + keyFile.getAbsolutePath() + "\"! Попробуйте снова:");
			printExceptionTrace(e);
			return;
		}

		final String absPath = keyFile.getAbsolutePath();

		printSuccess("Файл ключа \"" + keyFile.getAbsolutePath() + "\" успешно создан:");

		term.println("Ключ: " + ke.getEntityId().toString());
		term.println("ФИО: " + ke.getPersonText());
		term.println("Должность: " + ke.getPositionText());
	}

	private static SdsKeyEntity getKeyEntityFromUser() {
		File keysPath = checkCreateKeysDir();

		if (keysPath == null) {
			return null;
		}

		// Get list of key files and prompt user to select one

		ArrayList<Path> keyFilePaths = new ArrayList<>();

		try {
			Files.walk(keysPath.toPath()).forEach(path -> {
				if (!path.toFile().isFile()) {
					// Do not count directories
					return;
				}

				if (path.toString().toLowerCase().endsWith(".askey")) {
					keyFilePaths.add(path);
				}
			});
		} catch (IOException e) {
			printError("Ошибка при получении списка файлов ключей:");
			printExceptionTrace(e);
			return null;
		}

		if (keyFilePaths.isEmpty()) {
			printError("Не найдено ни одного файла ключа!");
			return null;
		}

		term.executeWithPropertiesConfigurator(
			p -> { p.setPromptColor(Color.GREEN); },
			t -> { t.println("Файлы ключей для подписания документов:"); }
		);

		int keyFileIndex = 1;

		term.println("0. <Отменить операцию>");

		for (Path keyFile : keyFilePaths) {
			term.executeWithPropertiesConfigurator(
				p -> { p.setPromptColor(Color.WHITE); },
				t -> { t.println(keyFileIndex + ". " + keyFile.getFileName().toString()); }
			);
		}

		int selKeyFileIndex = con.newIntInputReader()
			.withMinVal(0)
			.withMaxVal(keyFileIndex)
			.read("Выберите файл ключа");

		term.println();

		if (selKeyFileIndex == 0) {
			return null;
		}

		String passwText = con.newStringInputReader()
			.withInputMasking(true)
			.read("Введите пароль");

		// Try to load key file

		SdsKeyEntity ke = new SdsKeyEntity(secureRandom);

		try {
			ke.loadFromFile(keyFilePaths.get(selKeyFileIndex - 1).toFile(), passwText);
		} catch (Exception e) {
			printError("Ошибка при загрузке файла ключей:");
			printExceptionTrace(e);
			return null;
		}

		term.println();

		printSuccess("Данные выбранного ключа:");

		term.executeWithPropertiesConfigurator(
			p -> { p.setPromptColor(Color.CYAN); },
			t -> {
				t.println("Ключ: " + ke.getEntityId().toString());
				t.println("ФИО: " + ke.getPersonText());
				t.println("Должность: " + ke.getPositionText());
			}
		);

		term.println();

		return ke;
	}

	private static void execSdsSign() {
		term.println();

		term.executeWithPropertiesConfigurator(
			p -> { p.setPromptColor(Color.MAGENTA); },
			t -> { t.println("Подписание документов ПЭП."); }
		);

		term.println();

		File inPath = checkCreateInDir();

		if (inPath == null) {
			return;
		}

		File outPath = checkCreateOutDir();

		if (outPath == null) {
			return;
		}

		// Load config data from file

		Config config = new Config();

		try {
			config.loadFromFile(new File("axusigner.ini"));
		} catch (GeneralException e) {
			printError("Ошибка при загрузке файла конфигурации \"axusigner.ini\":");
			printExceptionTrace(e);
			return;
		}

		// Get list of all source PDF files in current directory (without .signed.pdf extension)

		ArrayList<Path> srcPdfFilePaths = new ArrayList<>();

		try {
			Files.walk(inPath.toPath()).forEach(path -> {
				if (!path.toFile().isFile()) {
					// Do not count directories
					return;
				}

				if (path.toString().toLowerCase().endsWith(".pdf")) {
					srcPdfFilePaths.add(path);
				}
			});
		} catch (IOException e) {
			printError("Ошибка при получении списка исходных PDF файлов:");
			printExceptionTrace(e);
			return;
		}

		if (srcPdfFilePaths.isEmpty()) {
			printError("Не найдено ни одного PDF файла для обработки!");
			return;
		}

		term.executeWithPropertiesConfigurator(
			p -> { p.setPromptColor(Color.GREEN); },
			t -> { t.println("Список PDF файлов для обработки:"); }
		);

		for (Path pdfFile : srcPdfFilePaths) {
			term.executeWithPropertiesConfigurator(
				p -> { p.setPromptColor(Color.WHITE); },
				t -> { t.println(pdfFile.getFileName().toString()); }
			);
		}

		term.println();

		SdsKeyEntity ke = getKeyEntityFromUser();

		if (ke == null) {
			return;
		}

		printSuccess("Начинаем подписание:");

		for (Path srcPdfFilePath : srcPdfFilePaths) {
			String inPdfStrFile = srcPdfFilePath.getFileName().toString();

			term.executeWithPropertiesConfigurator(
				p -> { p.setPromptColor(Color.WHITE); },
				t -> { t.println("Обрабатываю файл " + inPdfStrFile); }
			);

			String inPdfStrPath = srcPdfFilePath.toString();
			String outPdfStrPath = new File(outPath, inPdfStrFile.substring(0, inPdfStrFile.lastIndexOf('.')) + ".signed.pdf").toString();

			PdfDocument pdfDoc = null;

			try {
				pdfDoc = new PdfDocument(new PdfReader(inPdfStrPath), new PdfWriter(outPdfStrPath));
			} catch (IOException e) {
				printError("Ошибка при попытке открыть исходный или создать целевой PDF-файл:");
				printExceptionTrace(e);
				return;
			}

			// Add simple digital signature stamp on first page

			SdsStamp stamp = new SdsStamp();

			stamp.setStampPosition(config.getStampPosition());

			stamp.setSignerText(ke.getPersonText());
			stamp.setPosText(ke.getPositionText());

			LocalDateTime dateTimeNow = LocalDateTime.now();

			stamp.setDateTime(dateTimeNow);

			stamp.setKeyIdText(ke.getEntityId().toString());

			if (config.getStampMacEnable()) {
				byte[] docIdBytes = new byte[16];
				secureRandom.nextBytes(docIdBytes);
				UUID docId = MiscUtils.getUuidFromByteArray(docIdBytes);

				stamp.setDocIdText(docId.toString());

				byte[] personBytes = ke.getPersonText().getBytes(StandardCharsets.UTF_16);
				byte[] positionBytes = ke.getPositionText().getBytes(StandardCharsets.UTF_16);

				byte[] entityIdBytes = MiscUtils.getByteArrayFromUuid(ke.getEntityId());

				ByteBuffer bb = ByteBuffer.allocate(1024);

				bb.put(personBytes);
				bb.put(positionBytes);
				bb.putLong(dateTimeNow.toEpochSecond(ZoneOffset.UTC));
				bb.put(entityIdBytes);
				bb.put(docIdBytes);

				bb.flip();
				byte[] plainForMacBytes = new byte[bb.remaining()];
				bb.get(plainForMacBytes);

				byte[] docMac = ke.calcMessageMac(plainForMacBytes);

				stamp.setMacText(Hex.toHexString(docMac));
			}

			try {
				stamp.setFontColor(config.getStampFontColor());
				stamp.setFontName(config.getStampFontName());
				stamp.setFontSize(config.getStampFontSize());

				stamp.setBorderColor(config.getStampBorderColor());
				stamp.setBorderWidth(config.getStampBorderWidth());

				stamp.setMarginLr(config.getStampMarginLr());
				stamp.setMarginTb(config.getStampMarginTb());
				stamp.setPadding(config.getStampPadding());

				stamp.setMacEnable(config.getStampMacEnable());
			} catch (Exception e) {
				printError("Ошибка при задании атрибутов штампа:");
				printExceptionTrace(e);
				return;
			}

			try {
				stamp.placeStamp(pdfDoc.getFirstPage(), 1);
			} catch (Exception e) {
				printError("Ошибка при постановке штампа:");
				printExceptionTrace(e);
				return;
			}

			// Add watermark on every page
			if (!config.getWatermarkText().isEmpty()) {
				Watermark watermark = new Watermark();

				watermark.setText(config.getWatermarkText());

				try {
					watermark.setFontColor(config.getWatermarkFontColor());
					watermark.setFontName(config.getWatermarkFontName());
					watermark.setFontMaxSize(config.getWatermarkFontMaxSize());

					watermark.setTransparency(config.getWatermarkTransparency());

					watermark.setOffset(config.getWatermarkOffset());

					watermark.setFlip(config.getWatermarkFlip());
				} catch (Exception e) {
					printError("Ошибка при задании атрибутов водяного знака:");
					printExceptionTrace(e);
					return;
				}

				for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
					try {
						watermark.placeWatermark(pdfDoc.getPage(i), i);
					} catch (Exception e) {
						printError("Ошибка при постановке водяного знака:");
						printExceptionTrace(e);
						return;
					}
				}
			}

			// Add copyright on last page

			if (!config.getCopyrightDisable()) {
				Copyright copyright = new Copyright();

				copyright.setPosLeft(pdfDoc.getNumberOfPages() == 1 && config.getStampPosition() == StampPosition.BOTTOM_RIGHT);

				try {
					copyright.setLine3text(config.getCopyrightLine3Text());

					copyright.setFontColor(config.getCopyrightFontColor());
					copyright.setFontName(config.getCopyrightFontName());
					copyright.setFontSize(config.getCopyrightFontSize());
				} catch (Exception e) {
					printError("Ошибка при задании атрибутов копирайта:");
					printExceptionTrace(e);
					return;
				}

				try {
					copyright.placeCopyright(pdfDoc.getPage(pdfDoc.getNumberOfPages()), pdfDoc.getNumberOfPages());
				} catch (Exception e) {
					printError("Ошибка при постановке копирайта:");
					printExceptionTrace(e);
					return;
				}
			}

			pdfDoc.close();
		}

		printSuccess("Все файлы обработаны!");
	}

	private static void execSdsShowKeyInfo() {
		term.println();

		term.executeWithPropertiesConfigurator(
			p -> { p.setPromptColor(Color.MAGENTA); },
			t -> { t.println("Информация о ключе ПЭП."); }
		);

		term.println();

		// Just call getKeyEntityFromUser() and it will show all needed key info
		getKeyEntityFromUser();
	}

	private static void execSdsCheckMac() {
		term.println();

		term.executeWithPropertiesConfigurator(
			p -> { p.setPromptColor(Color.MAGENTA); },
			t -> { t.println("Проверка имитовставки штампа ПЭП."); }
		);

		term.println();

		SdsKeyEntity ke = getKeyEntityFromUser();

		if (ke == null) {
			return;
		}

		String docIdText = con.newStringInputReader()
			.withPattern("[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}")
			.read("Введите идентификатор документа");

		String dateText = con.newStringInputReader()
			.withPattern("[0-9]{2}.[0-9]{2}.[0-9]{4} [0-9]{2}:[0-9]{2}:[0-9]{2}")
			.read("Введите дату подписания документа");

		UUID docId = null;
		LocalDateTime dateTime = null;

		try {
			docId = UUID.fromString(docIdText);
			dateTime = LocalDateTime.parse(dateText, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
		} catch (Exception e) {
			printError("Ошибка при обработке введенных данных:");
			printExceptionTrace(e);
			return;
		}

		byte[] docIdBytes = MiscUtils.getByteArrayFromUuid(docId);
		byte[] personBytes = ke.getPersonText().getBytes(StandardCharsets.UTF_16);
		byte[] positionBytes = ke.getPositionText().getBytes(StandardCharsets.UTF_16);

		byte[] entityIdBytes = MiscUtils.getByteArrayFromUuid(ke.getEntityId());

		ByteBuffer bb = ByteBuffer.allocate(1024);

		bb.put(personBytes);
		bb.put(positionBytes);
		bb.putLong(dateTime.toEpochSecond(ZoneOffset.UTC));
		bb.put(entityIdBytes);
		bb.put(docIdBytes);

		bb.flip();
		byte[] plainForMacBytes = new byte[bb.remaining()];
		bb.get(plainForMacBytes);

		byte[] docMac = ke.calcMessageMac(plainForMacBytes);

		term.println();

		term.executeWithPropertiesConfigurator(
			p -> { p.setPromptColor(Color.CYAN); },
			t -> { t.println("Вычисленная имитовставка: " + Hex.toHexString(docMac)); }
		);
	}
}
