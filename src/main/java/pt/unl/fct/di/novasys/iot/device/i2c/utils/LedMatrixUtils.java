package pt.unl.fct.di.novasys.iot.device.i2c.utils;

import java.security.SecureRandom;

import pt.unl.fct.di.novasys.iot.device.i2c.GroveLedMatrix;

public class LedMatrixUtils {

	public class Letter {
		public static final byte A[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1,
				1, 0, 0, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0,
				1, 1, 0 };
		public static final byte B[] = { 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1,
				1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1, 1,
				1, 0, 0 };
		public static final byte C[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1,
				0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1,
				1, 1, 0 };
		public static final byte D[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0,
				1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0 };
		public static final byte E[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0,
				1, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0 };
		public static final byte F[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0,
				1, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0 };
		public static final byte G[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0,
				1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0,
				0, 0, 0 };
		public static final byte H[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0,
				1, 1, 1, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0 };
		public static final byte I[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0,
				0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0 };
		public static final byte J[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0,
				0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0 };
		public static final byte K[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0,
				1, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0 };
		public static final byte L[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0,
				1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0 };
		public static final byte M[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0,
				1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0,
				0, 0, 0 };
		public static final byte N[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0,
				1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0 };
		public static final byte O[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1,
				0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0 };
		public static final byte P[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0,
				1, 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0 };
		public static final byte Q[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1,
				0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0,
				0, 0, 1 };
		public static final byte R[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0,
				1, 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0 };
		public static final byte S[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0,
				1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0 };
		public static final byte T[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0,
				0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0 };
		public static final byte U[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1,
				0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0 };
		public static final byte V[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0,
				1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0 };
		public static final byte W[] = { 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
				0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0 };
		public static final byte X[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0,
				0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0,
				0, 0, 0 };
		public static final byte Y[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0,
				0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0 };
		public static final byte Z[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0,
				0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0 };

		public static final byte[] alphabet[] = { A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W,
				X, Y, Z };
	}

	public class Icon {
		public static final byte ARROW_UP[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0,
				0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0 };
		public static final byte ARROW_RIGHT[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0,
				0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0 };
		public static final byte ARROW_LEFT[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0,
				0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0 };
		public static final byte ARROW_DOWN[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0,
				0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0 };

		public static final byte[] arrows[] = { ARROW_UP, ARROW_RIGHT, ARROW_LEFT, ARROW_DOWN };

		public static final byte WRONG[] = { 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 0,
				0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 0, 0,
				0, 0, 1, 1 };
		public static final byte OK[] = { 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 0, 1,
				1, 0, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0 };
		public static final byte FORBIDDEN[] = { 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0,
				1, 1, 2, 2, 2, 2, 1, 1, 1, 1, 2, 2, 2, 2, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
				1, 1, 0, 0, 0 };
		public static final byte MANDATORY_FRONT[] = { 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 2, 2, 1,
				1, 0, 1, 1, 2, 2, 2, 2, 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 0, 1, 1, 2, 2, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0,
				0, 0, 1, 1, 0, 0, 0 };
		public static final byte MANDATORY_RIGHT[] = { 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 2, 1,
				1, 0, 1, 1, 2, 2, 2, 2, 1, 1, 1, 1, 2, 2, 2, 2, 1, 1, 0, 1, 1, 1, 2, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0,
				0, 0, 1, 1, 0, 0, 0 };
		public static final byte MANDATORY_LEFT[] = { 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 2, 1, 1,
				1, 0, 1, 1, 2, 2, 2, 2, 1, 1, 1, 1, 2, 2, 2, 2, 1, 1, 0, 1, 1, 2, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0,
				0, 0, 1, 1, 0, 0, 0 };
		public static final byte MANDATORY_BACK[] = { 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 2, 2, 1,
				1, 0, 1, 1, 1, 2, 2, 1, 1, 1, 1, 1, 2, 2, 2, 2, 1, 1, 0, 1, 1, 2, 2, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0,
				0, 0, 1, 1, 0, 0, 0 };

		public static final byte[] symbols[] = { WRONG, OK, FORBIDDEN, MANDATORY_FRONT, MANDATORY_RIGHT, MANDATORY_LEFT,
				MANDATORY_BACK };
	}

	public enum Symbol {
		WRONG(0), OK(1), FORBIDDEN(2), MANDATORY_FRONT(3), MANDATORY_RIGHT(4), MANDATORY_LEFT(5), MANDATORY_BACK(6);

		private final int value;

		Symbol(final int value) {
			this.value = value;
		}

		public int getValue() {
			return this.value;
		}
	}

	public enum Arrow {
		ARROW_UP(0), ARROW_RIGTH(1), ARROW_LEFT(2), ARROW_DOWN(3);

		private final int value;

		Arrow(final int value) {
			this.value = value;
		}

		public int getValue() {
			return this.value;
		}
	}

	public static final byte[] colors = new byte[] { GroveLedMatrix.orange, GroveLedMatrix.yellow, GroveLedMatrix.green,
			GroveLedMatrix.cyan, GroveLedMatrix.blue, GroveLedMatrix.purple, GroveLedMatrix.pink,
			GroveLedMatrix.white };

	public static byte randomColor() {
		return colors[new SecureRandom((System.currentTimeMillis() + "").getBytes()).nextInt(colors.length)];
	}

	public static byte[] encodeLetter(char c, byte color, byte bgColor) {
		byte[] encoding = new byte[8 * 8];
		int letterIndex = Character.compare(Character.toUpperCase(c), 'A');

		byte[] letter = Letter.alphabet[letterIndex];

		for (int i = 0; i < 8 * 8; i++)
			encoding[i] = letter[i] == 1 ? color : bgColor;

		return encoding;
	}

	public static byte[] encodeInvertedLetter(char c, byte backgroundColor) {
		return encodeLetter(c, GroveLedMatrix.black, backgroundColor);
	}

	public static byte[] encodeLetter(char c, byte color) {
		return encodeLetter(c, color, GroveLedMatrix.black);
	}

	public static byte[] encodeLetter(char c) {
		return encodeLetter(c, randomColor(), GroveLedMatrix.black);
	}

	public static byte[] encodeInvertedLetter(char c) {
		return encodeLetter(c, GroveLedMatrix.black, randomColor());
	}

	public static byte[] encodeLetterMosaic(char c) {
		byte[] encoding = new byte[8 * 8];
		int letterIndex = Character.compare(Character.toUpperCase(c), 'A');

		byte[] letter = Letter.alphabet[letterIndex];

		for (int i = 0; i < 8 * 8; i++)
			encoding[i] = letter[i] == 1 ? randomColor() : GroveLedMatrix.black;

		return encoding;
	}

	public static byte[] encodeInvertedLetterMosaic(char c) {
		byte[] encoding = new byte[8 * 8];
		int letterIndex = Character.compare(Character.toUpperCase(c), 'A');

		byte[] letter = Letter.alphabet[letterIndex];

		for (int i = 0; i < 8 * 8; i++)
			encoding[i] = letter[i] == 1 ? GroveLedMatrix.black : randomColor();

		return encoding;
	}

	public static byte[] encodeArrow(Arrow a, byte color, byte bgColor) {
		byte[] encoding = new byte[8 * 8];
		byte[] arrow = Icon.arrows[a.getValue()];

		for (int i = 0; i < 8 * 8; i++)
			encoding[i] = arrow[i] == 1 ? color : bgColor;

		return encoding;
	}

	public static byte[] encodeArrow(Arrow a, byte color) {
		return encodeArrow(a, color, GroveLedMatrix.black);
	}

	public static byte[] encodeInvertedArrow(Arrow a, byte color) {
		byte[] encoding = new byte[8 * 8];
		byte[] arrow = Icon.arrows[a.getValue()];

		for (int i = 0; i < 8 * 8; i++)
			encoding[i] = arrow[i] == 1 ? GroveLedMatrix.black : color;

		return encoding;
	}

	public static byte[] encodeArrow(Arrow a) {
		return encodeArrow(a, randomColor(), GroveLedMatrix.black);
	}

	public static byte[] encodeInvertedArrow(Arrow a) {
		return encodeArrow(a, GroveLedMatrix.black, randomColor());
	}

	public static byte[] encodeMosaicArrow(Arrow a) {
		byte[] encoding = new byte[8 * 8];
		byte[] arrow = Icon.arrows[a.getValue()];

		for (int i = 0; i < 8 * 8; i++)
			encoding[i] = arrow[i] == 1 ? randomColor() : GroveLedMatrix.black;

		return encoding;
	}

	public static byte[] encodeInvertedMosaicArrow(Arrow a) {
		byte[] encoding = new byte[8 * 8];
		byte[] arrow = Icon.arrows[a.getValue()];

		for (int i = 0; i < 8 * 8; i++)
			encoding[i] = arrow[i] == 1 ? GroveLedMatrix.black : randomColor();

		return encoding;
	}

	public static byte[] encodeSymbol(Symbol s, byte c1, byte c2, byte bg) {
		byte[] encoding = new byte[8 * 8];
		byte[] symbol = Icon.symbols[s.getValue()];

		for (int i = 0; i < 8 * 8; i++)
			encoding[i] = symbol[i] == 0 ? bg : (symbol[i] == 1 ? c1 : c2);

		return encoding;
	}

	public static byte[] encodeWrong() {
		return encodeSymbol(Symbol.WRONG, GroveLedMatrix.red, GroveLedMatrix.white, GroveLedMatrix.black);
	}

	public static byte[] encodeOk() {
		return encodeSymbol(Symbol.OK, GroveLedMatrix.green, GroveLedMatrix.green, GroveLedMatrix.black);
	}

	public static byte[] encodeForbidden() {
		return encodeSymbol(Symbol.FORBIDDEN, GroveLedMatrix.red, GroveLedMatrix.white, GroveLedMatrix.black);
	}

	public static byte[] encodeMandatoryFront() {
		return encodeSymbol(Symbol.MANDATORY_FRONT, GroveLedMatrix.blue, GroveLedMatrix.white, GroveLedMatrix.black);
	}

	public static byte[] encodeMandatoryRight() {
		return encodeSymbol(Symbol.MANDATORY_RIGHT, GroveLedMatrix.blue, GroveLedMatrix.white, GroveLedMatrix.black);
	}

	public static byte[] encodeMandatoryLeft() {
		return encodeSymbol(Symbol.MANDATORY_LEFT, GroveLedMatrix.blue, GroveLedMatrix.white, GroveLedMatrix.black);
	}

	public static byte[] encodeMandatoryBack() {
		return encodeSymbol(Symbol.MANDATORY_BACK, GroveLedMatrix.blue, GroveLedMatrix.white, GroveLedMatrix.black);
	}
}
