package ru.axu.signer;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.UUID;

public class MiscUtils {
	public static UUID getUuidFromByteArray(byte[] bytes) {
		if (bytes == null) {
			throw new IllegalArgumentException("Argument value can't be null!");
		}

		if (bytes.length != 16) {
			throw new IllegalArgumentException("Argument value must be exactly 16 bytes long!");
		}

		// Set Version (4) and Variant
		bytes[6] &= 0x0f;
		bytes[6] |= 0x40;
		bytes[8] &= 0x3f;
		bytes[8] |= 0x80;

		ByteBuffer bb = ByteBuffer.wrap(bytes);

		long high = bb.getLong();
		long low = bb.getLong();

		return new UUID(high, low);
	}

	public static byte[] getByteArrayFromUuid(UUID uuid) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);

		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());

		return bb.array();
	}

	public static String getStackTrace(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		e.printStackTrace(pw);

		return sw.toString();
	}

	public static int parseIntDef(String strInt, int defInt) {
		try {
			return Integer.parseInt(strInt);
		} catch(Exception e) {
			return defInt;
		}
	}

	public static double parseDoubleDef(String strDouble, double defDouble) {
		try {
			return Double.parseDouble(strDouble);
		} catch(Exception e) {
			return defDouble;
		}
	}

	public static Color parseColorDef(String strColor, Color defColor) {
		try {
			return Color.decode(strColor);
		} catch(Exception e) {
			return defColor;
		}
	}
}
