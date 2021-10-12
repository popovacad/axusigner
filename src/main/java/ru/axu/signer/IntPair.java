package ru.axu.signer;

import javafx.beans.NamedArg;

import java.io.Serializable;

public class IntPair implements Serializable {
	private int valA;
	private int valB;

	public int getValA() {
		return this.valA;
	}

	public int getValB() {
		return this.valB;
	}

	public IntPair(@NamedArg("val A") int valA, @NamedArg("val B") int valB) {
		this.valA = valA;
		this.valB = valB;
	}

	public String toString() {
		return this.valA + ", " + this.valB;
	}

	public int hashCode() {
		return (this.valA * 31 + this.valB);
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof IntPair)) {
			return false;
		} else {
			IntPair pair = (IntPair)o;

			if (this.valA == pair.valA && this.valB == pair.valB) {
				return true;
			} else {
				return false;
			}
		}
	}
}
