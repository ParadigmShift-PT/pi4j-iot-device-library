package pt.unl.fct.di.novasys.iot.device.i2c.utils;

import java.security.SecureRandom;

import pt.unl.fct.di.novasys.iot.device.i2c.GroveLedMatrix;

/**
 * Helper utilities for the {@link pt.unl.fct.di.novasys.iot.device.i2c.GroveLedMatrix
 * Grove RGB LED Matrix}: pre-built 8×8 bitmaps for the alphabet, four
 * cardinal arrows, and a small symbol set (✓ / ✗ / forbidden /
 * mandatory direction signs), plus encoders that turn each bitmap into
 * a 64-byte colour buffer ready to be written via
 * {@link pt.unl.fct.di.novasys.iot.device.i2c.GroveLedMatrix#loadSnapshot(byte[])}.
 *
 * <p>Encoder methods come in solid-colour, mosaic (random colour per
 * lit pixel), and inverted (background filled, foreground black)
 * variants.
 */
public class LedMatrixUtils {

	/** Pre-built 8×8 bitmaps for the Latin alphabet (uppercase). */
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

	/** Pre-built 8×8 bitmaps for arrows and common safety symbols. */
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

	/**
	 * Index into the {@link Icon#symbols} array, used by the
	 * symbol-encoding helpers.
	 */
	public enum Symbol {
		/** ✗ — wrong / cancel. */ WRONG(0),
		/** ✓ — ok / confirm. */ OK(1),
		/** Forbidden / no-entry circle. */ FORBIDDEN(2),
		/** Mandatory direction: forward. */ MANDATORY_FRONT(3),
		/** Mandatory direction: right. */ MANDATORY_RIGHT(4),
		/** Mandatory direction: left. */ MANDATORY_LEFT(5),
		/** Mandatory direction: back. */ MANDATORY_BACK(6);

		private final int value;

		Symbol(final int value) {
			this.value = value;
		}

		/** @return the index into {@link Icon#symbols} */
		public int getValue() {
			return this.value;
		}
	}

	/** Index into the {@link Icon#arrows} array. */
	public enum Arrow {
		/** Arrow pointing up. */ ARROW_UP(0),
		/** Arrow pointing right (note: misspelling preserved for compatibility with callers). */ ARROW_RIGTH(1),
		/** Arrow pointing left. */ ARROW_LEFT(2),
		/** Arrow pointing down. */ ARROW_DOWN(3);

		private final int value;

		Arrow(final int value) {
			this.value = value;
		}

		/** @return the index into {@link Icon#arrows} */
		public int getValue() {
			return this.value;
		}
	}

	/** Random-pick palette used by the mosaic encoders. */
	public static final byte[] colors = new byte[] { GroveLedMatrix.orange, GroveLedMatrix.yellow, GroveLedMatrix.green,
			GroveLedMatrix.cyan, GroveLedMatrix.blue, GroveLedMatrix.purple, GroveLedMatrix.pink,
			GroveLedMatrix.white };

	/** @return a random colour from the {@link #colors} palette */
	public static byte randomColor() {
		return colors[new SecureRandom((System.currentTimeMillis() + "").getBytes()).nextInt(colors.length)];
	}

	/**
	 * Encodes a letter as a 64-byte frame buffer with caller-chosen
	 * foreground and background colours. The character is uppercased and
	 * mapped through {@link Letter#alphabet}.
	 *
	 * @param c       the letter A–Z (case insensitive)
	 * @param color   foreground colour (lit pixels)
	 * @param bgColor background colour (unlit pixels)
	 * @return a 64-byte frame buffer ready for {@code loadSnapshot}
	 */
	public static byte[] encodeLetter(char c, byte color, byte bgColor) {
		byte[] encoding = new byte[8 * 8];
		int letterIndex = Character.compare(Character.toUpperCase(c), 'A');

		byte[] letter = Letter.alphabet[letterIndex];

		for (int i = 0; i < 8 * 8; i++)
			encoding[i] = letter[i] == 1 ? color : bgColor;

		return encoding;
	}

	/**
	 * Encodes a letter inverted (foreground unlit, background filled).
	 *
	 * @param c               the letter A–Z (case insensitive)
	 * @param backgroundColor colour used for the background (lit pixels)
	 * @return a 64-byte frame buffer
	 */
	public static byte[] encodeInvertedLetter(char c, byte backgroundColor) {
		return encodeLetter(c, GroveLedMatrix.black, backgroundColor);
	}

	/**
	 * Encodes a letter on a black background.
	 *
	 * @param c     the letter A–Z (case insensitive)
	 * @param color foreground colour
	 * @return a 64-byte frame buffer
	 */
	public static byte[] encodeLetter(char c, byte color) {
		return encodeLetter(c, color, GroveLedMatrix.black);
	}

	/**
	 * Encodes a letter on a black background using a randomly chosen
	 * foreground colour.
	 *
	 * @param c the letter A–Z (case insensitive)
	 * @return a 64-byte frame buffer
	 */
	public static byte[] encodeLetter(char c) {
		return encodeLetter(c, randomColor(), GroveLedMatrix.black);
	}

	/**
	 * Inverted variant with a randomly chosen background colour.
	 *
	 * @param c the letter A–Z (case insensitive)
	 * @return a 64-byte frame buffer
	 */
	public static byte[] encodeInvertedLetter(char c) {
		return encodeLetter(c, GroveLedMatrix.black, randomColor());
	}

	/**
	 * Encodes a letter where every lit pixel is given an independently
	 * chosen random colour.
	 *
	 * @param c the letter A–Z (case insensitive)
	 * @return a 64-byte frame buffer
	 */
	public static byte[] encodeLetterMosaic(char c) {
		byte[] encoding = new byte[8 * 8];
		int letterIndex = Character.compare(Character.toUpperCase(c), 'A');

		byte[] letter = Letter.alphabet[letterIndex];

		for (int i = 0; i < 8 * 8; i++)
			encoding[i] = letter[i] == 1 ? randomColor() : GroveLedMatrix.black;

		return encoding;
	}

	/**
	 * Inverted mosaic — background pixels each get an independently
	 * chosen random colour, foreground pixels are dark.
	 *
	 * @param c the letter A–Z (case insensitive)
	 * @return a 64-byte frame buffer
	 */
	public static byte[] encodeInvertedLetterMosaic(char c) {
		byte[] encoding = new byte[8 * 8];
		int letterIndex = Character.compare(Character.toUpperCase(c), 'A');

		byte[] letter = Letter.alphabet[letterIndex];

		for (int i = 0; i < 8 * 8; i++)
			encoding[i] = letter[i] == 1 ? GroveLedMatrix.black : randomColor();

		return encoding;
	}

	/**
	 * Encodes an arrow with caller-chosen foreground and background
	 * colours.
	 *
	 * @param a       which arrow
	 * @param color   foreground colour
	 * @param bgColor background colour
	 * @return a 64-byte frame buffer
	 */
	public static byte[] encodeArrow(Arrow a, byte color, byte bgColor) {
		byte[] encoding = new byte[8 * 8];
		byte[] arrow = Icon.arrows[a.getValue()];

		for (int i = 0; i < 8 * 8; i++)
			encoding[i] = arrow[i] == 1 ? color : bgColor;

		return encoding;
	}

	/**
	 * Encodes an arrow on a black background.
	 *
	 * @param a     which arrow
	 * @param color foreground colour
	 * @return a 64-byte frame buffer
	 */
	public static byte[] encodeArrow(Arrow a, byte color) {
		return encodeArrow(a, color, GroveLedMatrix.black);
	}

	/**
	 * Encodes an arrow inverted (foreground unlit, background filled).
	 *
	 * @param a     which arrow
	 * @param color background colour
	 * @return a 64-byte frame buffer
	 */
	public static byte[] encodeInvertedArrow(Arrow a, byte color) {
		byte[] encoding = new byte[8 * 8];
		byte[] arrow = Icon.arrows[a.getValue()];

		for (int i = 0; i < 8 * 8; i++)
			encoding[i] = arrow[i] == 1 ? GroveLedMatrix.black : color;

		return encoding;
	}

	/**
	 * Encodes an arrow on a black background with a random foreground colour.
	 *
	 * @param a which arrow
	 * @return a 64-byte frame buffer
	 */
	public static byte[] encodeArrow(Arrow a) {
		return encodeArrow(a, randomColor(), GroveLedMatrix.black);
	}

	/**
	 * Encodes an arrow inverted with a random background colour.
	 *
	 * @param a which arrow
	 * @return a 64-byte frame buffer
	 */
	public static byte[] encodeInvertedArrow(Arrow a) {
		return encodeArrow(a, GroveLedMatrix.black, randomColor());
	}

	/**
	 * Encodes an arrow where every lit pixel gets an independently
	 * chosen random colour.
	 *
	 * @param a which arrow
	 * @return a 64-byte frame buffer
	 */
	public static byte[] encodeMosaicArrow(Arrow a) {
		byte[] encoding = new byte[8 * 8];
		byte[] arrow = Icon.arrows[a.getValue()];

		for (int i = 0; i < 8 * 8; i++)
			encoding[i] = arrow[i] == 1 ? randomColor() : GroveLedMatrix.black;

		return encoding;
	}

	/**
	 * Inverted mosaic — background pixels get random colours, foreground
	 * is dark.
	 *
	 * @param a which arrow
	 * @return a 64-byte frame buffer
	 */
	public static byte[] encodeInvertedMosaicArrow(Arrow a) {
		byte[] encoding = new byte[8 * 8];
		byte[] arrow = Icon.arrows[a.getValue()];

		for (int i = 0; i < 8 * 8; i++)
			encoding[i] = arrow[i] == 1 ? GroveLedMatrix.black : randomColor();

		return encoding;
	}

	/**
	 * Encodes a multi-colour symbol. Symbols use three logical layers —
	 * primary foreground (lit pixels with bitmap value 1), secondary
	 * foreground (lit pixels with bitmap value 2, used for accents), and
	 * background.
	 *
	 * @param s  which symbol
	 * @param c1 primary foreground colour
	 * @param c2 secondary foreground colour
	 * @param bg background colour
	 * @return a 64-byte frame buffer
	 */
	public static byte[] encodeSymbol(Symbol s, byte c1, byte c2, byte bg) {
		byte[] encoding = new byte[8 * 8];
		byte[] symbol = Icon.symbols[s.getValue()];

		for (int i = 0; i < 8 * 8; i++)
			encoding[i] = symbol[i] == 0 ? bg : (symbol[i] == 1 ? c1 : c2);

		return encoding;
	}

	/** @return a frame buffer for the ✗ (wrong) symbol in red on black */
	public static byte[] encodeWrong() {
		return encodeSymbol(Symbol.WRONG, GroveLedMatrix.red, GroveLedMatrix.white, GroveLedMatrix.black);
	}

	/** @return a frame buffer for the ✓ (ok) symbol in green on black */
	public static byte[] encodeOk() {
		return encodeSymbol(Symbol.OK, GroveLedMatrix.green, GroveLedMatrix.green, GroveLedMatrix.black);
	}

	/** @return a frame buffer for the forbidden / no-entry symbol */
	public static byte[] encodeForbidden() {
		return encodeSymbol(Symbol.FORBIDDEN, GroveLedMatrix.red, GroveLedMatrix.white, GroveLedMatrix.black);
	}

	/** @return a frame buffer for the mandatory-direction-forward symbol */
	public static byte[] encodeMandatoryFront() {
		return encodeSymbol(Symbol.MANDATORY_FRONT, GroveLedMatrix.blue, GroveLedMatrix.white, GroveLedMatrix.black);
	}

	/** @return a frame buffer for the mandatory-direction-right symbol */
	public static byte[] encodeMandatoryRight() {
		return encodeSymbol(Symbol.MANDATORY_RIGHT, GroveLedMatrix.blue, GroveLedMatrix.white, GroveLedMatrix.black);
	}

	/** @return a frame buffer for the mandatory-direction-left symbol */
	public static byte[] encodeMandatoryLeft() {
		return encodeSymbol(Symbol.MANDATORY_LEFT, GroveLedMatrix.blue, GroveLedMatrix.white, GroveLedMatrix.black);
	}

	/** @return a frame buffer for the mandatory-direction-back symbol */
	public static byte[] encodeMandatoryBack() {
		return encodeSymbol(Symbol.MANDATORY_BACK, GroveLedMatrix.blue, GroveLedMatrix.white, GroveLedMatrix.black);
	}
}
