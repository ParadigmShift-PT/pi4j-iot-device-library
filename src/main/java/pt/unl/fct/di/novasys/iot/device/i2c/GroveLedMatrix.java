/********************************************************************************************
 * GroveLedMatrix
 * 
 * @author João Brilha (j.brilha@campus.fct.unl.pt)
 * @author João Leitão (jc.leitao@fct.unl.pt)
 ********************************************************************************************/


package pt.unl.fct.di.novasys.iot.device.i2c;

import java.io.IOException;

import com.github.yafna.raspberry.grovepi.pi4j.GrovePi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CConfigBuilder;

import pt.unl.fct.di.novasys.iot.device.I2CDevice;

/**
 * Driver for the Grove RGB LED Matrix (8×8). Wraps the matrix's I²C
 * command set with high-level operations: per-pixel colour, full-frame
 * fill, built-in emojis and animations, level bars, snapshots, and
 * orientation control.
 *
 * <p>Lives at I²C address {@code 0x65}. Construction enables the
 * indicator LED and clears the display.
 *
 * <p>Colours are encoded as a single byte; the constants
 * {@link #red}, {@link #orange}, {@link #yellow}, {@link #green},
 * {@link #cyan}, {@link #blue}, {@link #purple}, {@link #pink},
 * {@link #white}, and {@link #black} cover the common palette.
 * Animations and emojis are selected via the {@link Animation},
 * {@link Emoji}, and {@link Orientation} enums.
 */
@SuppressWarnings("unused")
public class GroveLedMatrix implements I2CDevice {
	/** I²C address of the LED Matrix. */
	public final static int LED_DISPLAY_ADDR = 0x65;

	private final I2C matrix;

	/** Colour palette: red. */ public static final byte red = (byte) 0x00;
	/** Colour palette: orange. */ public static final byte orange = (byte) 0x12;
	/** Colour palette: yellow. */ public static final byte yellow = (byte) 0x18;
	/** Colour palette: green. */ public static final byte green = (byte) 0x52;
	/** Colour palette: cyan. */ public static final byte cyan = (byte) 0x7f;
	/** Colour palette: blue. */ public static final byte blue = (byte) 0xaa;
	/** Colour palette: purple. */ public static final byte purple = (byte) 0xc3;
	/** Colour palette: pink. */ public static final byte pink = (byte) 0xdc;
	/** Colour palette: white. */ public static final byte white = (byte) 0xfe;
	/** Colour palette: black (off). */ public static final byte black = (byte) 0xff;

	private static final byte I2C_CMD_CONTINUE_DATA	= (byte) 0x81;

	private static final byte GROVE_TWO_RGB_LED_MATRIX_DEF_I2C_ADDR = (byte) 0x65; // The device i2c address in default
	private static final int GROVE_TWO_RGB_LED_MATRIX_VID = 0x2886; // Vender ID of the device
	private static final int GROVE_TWO_RGB_LED_MATRIX_PID =	0x8005; // Product ID of the device

	private static final byte I2C_CMD_GET_DEV_ID = (byte) 0x00; // This command gets device ID information
	private static final byte I2C_CMD_DISP_BAR = (byte) 0x01; // This command displays LED bar
	private static final byte I2C_CMD_DISP_EMOJI = (byte) 0x02; // This command displays emoji
	private static final byte I2C_CMD_DISP_NUM = (byte) 0x03; // This command displays number
	private static final byte I2C_CMD_DISP_STR = (byte) 0x04; // This command displays string
	private static final byte I2C_CMD_DISP_CUSTOM = (byte) 0x05; // This command displays user-defined pictures
	private static final byte I2C_CMD_DISP_OFF = (byte) 0x06; // This command cleans the display
	private static final byte I2C_CMD_DISP_ASCII = (byte) 0x07; // not use
	private static final byte I2C_CMD_DISP_FLASH = (byte) 0x08; // This command displays pictures which are stored in flash
	private static final byte I2C_CMD_DISP_COLOR_BAR = (byte) 0x09; // This command displays colorful led bar
	private static final byte I2C_CMD_DISP_COLOR_WAVE =  (byte) 0x0a; // This command displays built-in wave animation
	private static final byte I2C_CMD_DISP_COLOR_CLOCKWISE = (byte) 0x0b; // This command displays built-in clockwise animation
	private static final byte I2C_CMD_DISP_COLOR_ANIMATION =  (byte) 0x0c; // This command displays other built-in animation
	private static final byte I2C_CMD_DISP_COLOR_BLOCK = (byte) 0x0d; // This command displays an user-defined color
	private static final byte I2C_CMD_STORE_FLASH =	 (byte) 0xa0; // This command stores frames in flash
	private static final byte I2C_CMD_DELETE_FLASH =  (byte) 0xa1; // This command deletes all the frames in flash

	private static final byte I2C_CMD_LED_ON = (byte) 0xb0; // This command turns on the indicator LED flash mode
	private static final byte I2C_CMD_LED_OFF = (byte) 0xb1; // This command turns off the indicator LED flash mode
	private static final byte I2C_CMD_AUTO_SLEEP_ON = (byte) 0xb2; // This command enable device auto sleep mode
	private static final byte I2C_CMD_AUTO_SLEEP_OFF = (byte) 0xb3; // This command disable device auto sleep mode (default mode)

	private static final byte I2C_CMD_DISP_ROTATE = (byte) 0xb4; // This command setting the display orientation
	private static final byte I2C_CMD_DISP_OFFSET = (byte) 0xb5; // This command setting the display offset

	private static final byte I2C_CMD_SET_ADDR = (byte) 0xc0; // This command sets device i2c address
	private static final byte I2C_CMD_RST_ADDR = (byte) 0xc1; // This command resets device i2c address
	private static final byte I2C_CMD_TEST_TX_RX_ON = (byte) 0xe0; // This command enable TX RX pin test mode
	private static final byte I2C_CMD_TEST_TX_RX_OFF = (byte) 0xe1; // This command disable TX RX pin test mode
	private static final byte I2C_CMD_TEST_GET_VER = (byte) 0xe2; // This command use to get software version
	private static final byte I2C_CMD_GET_DEVICE_UID = (byte) 0xf1; // This command use to get chip id    	
	
	private static final byte ORIENTATION_DISPLAY_ROTATE_0= 0x0;	   
	private static final byte ORIENTATION_DISPLAY_ROTATE_90 = 0x1;
	private static final byte ORIENTATION_DISPLAY_ROTATE_180 = 0x2;
	private static final byte ORIENTATION_DISPLAY_ROTATE_270 = 0x3;

	private byte[] display; 
	
	/** Rotation applied to subsequent display writes. */
	public static enum Orientation {
		/** No rotation. */ ZeroDegrees((byte) 0x0),
		/** Rotated 90° clockwise. */ NinetyDegrees((byte) 0x1),
		/** Rotated 180°. */ OneEightyDegrees((byte) 0x2),
		/** Rotated 270° clockwise. */ TwoSeventyDegrees((byte) 0x3);

		/** Wire-format byte for this orientation. */
		public final byte code;

		Orientation(byte code) {
			this.code = code;
		}
	}
	
	/**
	 * Built-in emoji set baked into the matrix's firmware. Selected via
	 * {@link #displayEmoji(Emoji)}.
	 */
	public static enum Emoji {
		/** Smile face. */
		Smile(0),
		Laught(1),
		Sad(2),
		Mad(3),
		Angry(4),
		Cry(5),
		Greedy(6),
		Cood(7),
		Shy(8),
		Awkward(9),
		Heart(10),
		SmallHeart(11),
		BrokenHeart(12),
		Waterdrop(13),
		Flame(14),
		Creeper(15),
		MadCreeper(16),
		Sword(17),
		WoodenSword(18),
		CrystalSword(19),
		House(20),
		Tree(21),
		Flower(22),
		Umbrella(23),
		Rain(24),
		Monster(25),
		Crab(26),
		Duck(27),
		Rabbit(28),
		/** Cat. */ Cat(29);

		/** Wire-format integer for this emoji. */
		public final int code;

		Emoji(int code) {
			this.code = code;
		}
			
	}
	
    // *    index: the index of animations,
    // *			0. big clockwise
    // *			1. small clockwise
    // *			2. rainbow cycle
    // *			3. fire
    // *			4. walking child
    // *			5. broken heart
	/**
	 * Built-in firmware animations. Selected via
	 * {@link #displayColorAnimation(Animation)}.
	 */
	public static enum Animation {
		/** Big clockwise sweep. */ BigClock(0),
		/** Small clockwise sweep. */ SmallClock(1),
		/** Rainbow cycle. */ Rainbow(2),
		/** Fire / flame animation. */ Fire(3),
		/** Walking child animation. */ WalkingChild(4),
		/** Broken-heart animation. */ BrokenHeart(5);

		/** Wire-format integer for this animation. */
		public final int code;

		Animation(int code) {
			this.code = code;
		}
	}
	
	/**
	 * Opens the I²C handle, enables the indicator LED, and clears the
	 * display.
	 *
	 * @param pi4j Pi4J context
	 * @throws IOException if the matrix cannot be initialised
	 */
	public GroveLedMatrix(Context pi4j) throws IOException {
		I2CConfigBuilder configtext = I2C.newConfigBuilder(pi4j);
		configtext.id("Grovepi-plus" + LED_DISPLAY_ADDR);
		configtext.name("My I2C Bus " + LED_DISPLAY_ADDR);
		configtext.bus(GrovePi4J.I2C_BUS);
		configtext.device(LED_DISPLAY_ADDR);
		I2CConfig c = configtext.build();
		matrix = pi4j.create(c);
		this.init();
	}

	protected void init() throws IOException {
		matrix.write(I2C_CMD_LED_ON);
		display = new byte[8*8];
		this.clearDisplay();
	}
	
	private void refreshDisplay() {
		byte cmd[] = new byte[8 + (8*8) + 3];
		cmd[0] = I2C_CMD_DISP_CUSTOM;
		cmd[1] = 0x0; //padding
		cmd[2] = 0x0; //padding
		cmd[3] = 0x01; //forever flag
		cmd[4] = 0x01; //number of frame
		cmd[5] = 0x0; //frame index
		
		cmd[6] = 0x0; //padding ?? 
		cmd[7] = 0x0; //padding ??
		
		System.arraycopy(this.display, 0, cmd, 8, 8*8);
			
		cmd[8+(8*8)] = (byte) 0xff;
		cmd[8+(8*8)+1] = (byte) 0xff;
		cmd[8+(8*8)+2] = 0x1;
		
		matrix.write(cmd);
	}
	
	/** Turns off every pixel and pushes the cleared frame to the matrix. */
	public void clearDisplay() {
		for(int i = 0; i < 8; i++)
			for(int j = 0; j < 8; j++)
				this.display[(i*8)+j] = black;
		
		refreshDisplay();
		
	}

	/**
	 * Changes the display orientation for subsequent writes.
	 *
	 * @param orientation the rotation to apply
	 */
	public void setDisplayOrientation(Orientation orientation) {
		byte[] cmd = {I2C_CMD_DISP_ROTATE, orientation.code};
		matrix.write(cmd);
		
	}
		
	/**
	 * Fills every pixel with one RGB colour. Bypasses the local frame
	 * buffer — subsequent {@link #setPixelColor(int, int, byte)} calls
	 * may overwrite this fill from the buffered state.
	 *
	 * @param red   red intensity (0–255)
	 * @param green green intensity (0–255)
	 * @param blue  blue intensity (0–255)
	 */
	public void setAllColor(byte red, byte green, byte blue) {
		byte[] cmd = {I2C_CMD_DISP_COLOR_BLOCK, red, green, blue, 0xf, 0xf, 0x01};
		matrix.write(cmd);
	}
	
	/**
	 * Lights one pixel and refreshes the display.
	 *
	 * @param x     row, 0 (top) to 7 (bottom)
	 * @param y     column, 0 (left) to 7 (right)
	 * @param color one of the palette constants
	 *              ({@link #red}, {@link #orange}, …, {@link #black})
	 * @throws Exception if {@code x} or {@code y} are outside [0, 8)
	 */
	public void setPixelColor(int x, int y, byte color) throws Exception {
		if(x < 0 || x >= 8 || y < 0 || y >= 8) {
			throw new Exception("Invalid coordinate (coordinates should be between [0,8[");
		}
		display[(x*8)+y] = color;
		refreshDisplay();
	}
	
    // *    emoji: Set a number from 0 to 29 for different emoji.
    // *			0	smile	10	heart		    20	house
    // *			1	laugh	11	small heart		21	tree
    // *			2	sad	    12	broken heart	22	flower
    // *			3	mad	    13	waterdrop		23	umbrella
    // *			4	angry	14	flame		    24	rain
    // *			5	cry	    15	creeper		    25	monster
    // *			6	greedy	16	mad creeper		26	crab
    // *			7	cool	17	sword		    27	duck
    // *			8	shy	    18	wooden sword	28	rabbit
    // *			9	awkward	19	crystal sword	29	cat
    /**
     * Plays one of the firmware-baked emojis. Display is held until the
     * caller switches to a different content.
     *
     * @param emoji the emoji to show
     */
    public void displayEmoji(Emoji emoji){
		byte cmd[] = new byte[8 + (8*8) + 3];
		cmd[0] = I2C_CMD_DISP_EMOJI;
		cmd[1] = (byte)emoji.code;
		cmd[2] = 0x01; // duration_time & 0xff
		cmd[3] = 0x01; // (duration_time >> 8) & 0xff
		cmd[4] = 0x01; // forever flag overrides duration_time I think
		// cmd[5] = 0x0;
		// cmd[6] = 0x0; 
		// cmd[7] = 0x0; 
		
		System.arraycopy(this.display, 0, cmd, 8, 8*8);
			
		// cmd[8+(8*8)] = (byte) 0xff;
		// cmd[8+(8*8)+1] = (byte) 0xff;
		// cmd[8+(8*8)+2] = 0x1;
		
		matrix.write(cmd);

    }

    /**
     * Renders a colour level bar. Bar values run 0–32 (5 columns × 8
     * rows ≈ 32 LEDs in the bar).
     *
     * @param bar bar level (0–32)
     */
    public void displayColorBar(int bar){
		byte cmd[] = new byte[8 + (8*8) + 3];
		cmd[0] = I2C_CMD_DISP_COLOR_BAR;
		cmd[1] = (byte)bar;
		cmd[2] = 0x01; // duration_time & 0xff
		cmd[3] = 0x01; // (duration_time >> 8) & 0xff
		cmd[4] = 0x01; // forever flag overrides duration_time I think
		
		System.arraycopy(this.display, 0, cmd, 8, 8*8);
			
		matrix.write(cmd);

    }

    // *    index: the index of animations,
    // *			0. big clockwise
    // *			1. small clockwise
    // *			2. rainbow cycle
    // *			3. fire
    // *			4. walking child
    // *			5. broken heart

    /**
     * Plays a firmware animation (rainbow cycle, fire, walking child,
     * broken heart, big / small clockwise sweep). Display is held until
     * the caller switches to different content.
     *
     * @param animation the animation to play
     */
    public void displayColorAnimation(Animation animation){
		byte cmd[] = new byte[8 + (8*8) + 3];
		cmd[0] = I2C_CMD_DISP_COLOR_ANIMATION;
        int from, to;
        switch (animation.code) {
            case 0:
                from = 0;
                to = 28;
                break;

            case 1:
                from = 29;
                to = 41;
                break;

            case 2:				// rainbow cycle
                from = 255;
                to = 255;
                break;

            case 3: 			// fire
                from = 254;
                to = 254;
                break;

            case 4: 			// walking
                from = 42;
                to = 43;
                break;

            case 5:				// broken heart
                from = 44;
                to = 52;
                break;

            default:
                from = 0;
                to = 28;
                break;
        }
		cmd[1] = (byte)from;
		cmd[2] = (byte)to;
        cmd[3] = 0x01; // duration_time & 0xff
		cmd[4] = 0x01; // (duration_time >> 8) & 0xff
		cmd[5] = 0x01; // forever flag overrides duration_time I think
		
		System.arraycopy(this.display, 0, cmd, 8, 8*8);
			
		matrix.write(cmd);

    }
	
    /**
     * Returns a defensive copy of the local 64-byte frame buffer
     * representing the current display state.
     *
     * @return a copy of the current frame buffer
     */
    public byte[] getSnapshot() {
    	return this.display.clone();
    }

    /**
     * Restores a frame buffer captured by {@link #getSnapshot()} and
     * pushes it to the matrix. The snapshot is silently ignored if its
     * length doesn't match the 64-byte buffer.
     *
     * @param snapshot a 64-byte frame buffer
     */
    public void loadSnapshot(byte[] snapshot) {
    	if(display.length == snapshot.length) {
    		System.arraycopy(snapshot, 0, this.display, 0, this.display.length);
    		this.refreshDisplay();
    	}
    }
    
	/** Closes the underlying I²C handle. */
	public void close() {
		matrix.close();
	}

}
