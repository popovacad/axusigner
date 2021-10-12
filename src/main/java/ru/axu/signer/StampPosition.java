package ru.axu.signer;

public enum StampPosition {
	TOP_LEFT,
	TOP_RIGHT,
	BOTTOM_LEFT,
	BOTTOM_RIGHT;

	public static StampPosition fromString(String posStr) {
		switch (posStr) {
			default:
			case "tl":
				return TOP_LEFT;
			case "tr":
				return TOP_RIGHT;
			case "bl":
				return BOTTOM_LEFT;
			case "br":
				return BOTTOM_RIGHT;
		}
	}
}
