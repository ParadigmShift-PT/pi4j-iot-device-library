/********************************************************************************************
 * GroveLcd
 * 
 * @author João Brilha (j.brilha@campus.fct.unl.pt)
 * @author João Leitão (jc.leitao@fct.unl.pt)
 ********************************************************************************************/

package pt.unl.fct.di.novasys.iot.device.i2c;

import com.github.yafna.raspberry.grovepi.GrovePiSequenceVoid;
import com.github.yafna.raspberry.grovepi.devices.GroveRgbLcd;
import com.github.yafna.raspberry.grovepi.pi4j.GrovePi4J;
import com.github.yafna.raspberry.grovepi.pi4j.IO;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CConfigBuilder;

import pt.unl.fct.di.novasys.iot.device.I2CDevice;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class GroveLcd extends GroveRgbLcd implements I2CDevice {
	private final I2C text;
	private Thread scrolling;
	
	private static final String spacing = "    ";
	private static final long scrool_time = 800;
	private static final short lcd_char_limit = 32;
	
	private AtomicBoolean runText;
	
	public GroveLcd(Context pi4j) throws IOException {
		I2CConfigBuilder configtext = I2C.newConfigBuilder(pi4j);
		configtext.id("Grovepi-plus" + DISPLAY_TEXT_ADDR);
		configtext.name("My I2C Bus " + DISPLAY_TEXT_ADDR);
		configtext.bus(GrovePi4J.I2C_BUS);
		configtext.device(DISPLAY_TEXT_ADDR);
		I2CConfig c = configtext.build();
		text = pi4j.create(c);
		init();
		
		this.scrolling = null;
		this.runText = new AtomicBoolean(false);
	}

	@Override
	public void close() {
		text.close();
	}

	@Override 
	public synchronized void setText(String s) {
		if(this.runText.getAcquire()) {
			this.runText.set(false);
			try {
				this.scrolling.join();
			} catch (InterruptedException e) {
				this.scrolling.interrupt();
			}
		}
		
		if(s.trim().length() > GroveLcd.lcd_char_limit) {
			this.setScrollingText(s.trim());
		} else {
			try {
				super.setText(s);
			} catch (IOException e) {
				System.err.println("Could not set text");
			}
		}
	}
	
	private void setDirectText(String s) throws IOException {
		super.setText(s);
	}
	
	private void setScrollingText(String text) {
		this.runText.set(true);
		this.scrolling = new Thread(new Runnable() {
			
			@Override
			public void run() {
				String txt = text + GroveLcd.spacing;
				String lcd_txt;
				int txt_len = txt.length();
				int until;
				
				try {
					for (int i = 0; runText.getAcquire(); i = (i + 1) % txt_len) {
			            until = Math.min((i % txt_len) + lcd_char_limit, txt_len);

			            lcd_txt = txt.substring(i % txt_len, until);
			            if (lcd_txt.length() < lcd_char_limit) {
			                lcd_txt += txt.substring(0, lcd_char_limit - lcd_txt.length());
			            }

			            setDirectText(lcd_txt);
			            Thread.sleep(GroveLcd.scrool_time); 	// any faster and it looks pretty bad with
			            											// the LCD ghosting
			        }
					return;
				} catch (IOException e) {
					e.printStackTrace();
					return;
				} catch (InterruptedException e) {
					return;
				}
			}
		});
		this.scrolling.start();
	}
	
	@Override
	public void execTEXT(GrovePiSequenceVoid<?> sequence) throws IOException {
		synchronized (this) {
			sequence.execute(new IO(text));
		}
	}

	@Override
	public void execRGB(GrovePiSequenceVoid<?> sequence) throws IOException {
		
	}

}
