package pt.unl.fct.di.novasys.iot.device.digital;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;

public class GroveBuzzer extends DigitalOutputDevice {
    private Melody mel;

    public GroveBuzzer(Context pi4j, String name, int line, int ID) {
        super(pi4j,
              DigitalOutput.newConfigBuilder(pi4j)
                  .id(name)
                  .name(name + " — " + ID)
                  .address(line)
                  .initial(DigitalState.LOW)
                  .build(),
              ID);
    }

    public void playMelody()
        throws IllegalStateException, InterruptedException {
        if (mel == null) {
            throw new IllegalStateException("Melody must be set");
        }

        for (int i = 0; i < mel.notes.length; i++) {
            if (mel.notes[i] == ' ') {
                Thread.sleep(mel.beats[i] * mel.tempo);
            } else {
                playNote(mel.notes[i], mel.beats[i] * mel.tempo);
            }
            Thread.sleep(mel.tempo / 2); /* delay between notes */
        }
    }

    public void playMelody(Melody mel) throws InterruptedException {
        for (int i = 0; i < mel.notes.length; i++) {
            if (mel.notes[i] == ' ') {
                Thread.sleep(mel.beats[i] * mel.tempo);
            } else {
                playNote(mel.notes[i], mel.beats[i] * mel.tempo);
            }
            Thread.sleep(mel.tempo / 2); /* delay between notes */
        }
    }

    public void playNote(char note, int duration) {
        // not sure if the commented-out notes are even be distinguishable
        char notes[] = {
            'c', 'd', 'e', 'f', 'g', 'a', 'b', 'C',
            // 'D', 'E', 'F', 'G', 'A', 'B', 'H'
        };
        int tones[] = {
            1915, 1700, 1519, 1432, 1275, 1136, 1014, 956,
            // 3830, 3400, 3035, 2865, 2550, 2270, 2025
        };

        // play the tone corresponding to the note name
        for (int i = 0; i < tones.length; i++) {
            if (notes[i] == note) {
                playTone(tones[i], duration);
            }
        }
    }

    public void playTone(int tone, int duration) {
        for (long i = 0; i < duration * 1000L; i += tone * 2) {
            dataOut.state(DigitalState.HIGH);
            delayMicroseconds(tone);
            dataOut.state(DigitalState.LOW);
            delayMicroseconds(tone);
        }
    }

    public char[] getNotes() { return mel.notes; }

    public void setNotes(char... notes) { this.mel.notes = notes; }

    public int[] getBeats() { return mel.beats; }

    public void setBeats(int... beats) { this.mel.beats = beats; }

    public int getTempo() { return mel.tempo; }

    public void setTempo(int tempo) { this.mel.tempo = tempo; }

    public Melody getMelody() { return mel; }

    public void setMelody(Melody mel) { this.mel = mel; }

    public static class Melody {
        private char[] notes;
        private int[] beats;
        private int tempo;

        public static final Melody TWINKLE_TWINKLE =
            newBuilder()
                .notes('c', 'c', 'g', 'g', 'a', 'a', 'g', 'f', 'f', 'e', 'e',
                       'd', 'd', 'c')
                .beats(1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 2)
                .tempo(300)
                .build();

        public static final Melody HAPPY_BIRTHDAY =
            newBuilder()
                .notes('c', 'c', 'd', 'c', 'f', 'e', 'c', 'c', 'd', 'c', 'g',
                       'f', 'c', 'c', 'C', 'a', 'f', 'e', 'd', 'a', 'a', 'a',
                       'f', 'g', 'f')
                .beats(1, 1, 2, 2, 2, 4, 1, 1, 2, 2, 2, 4, 1, 1, 2, 2, 2, 2, 4,
                       1, 1, 2, 2, 2, 4)
                .tempo(250)
                .build();

        public static final Melody JINGLE_BELLS =
            newBuilder()
                .notes('e', 'e', 'e', 'e', 'e', 'e', 'e', 'g', 'c', 'd', 'e',
                       'f', 'f', 'f', 'f', 'f', 'e', 'e', 'e', 'e', 'e', 'd',
                       'd', 'e', 'd', 'g')
                .beats(1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 4, 1, 1, 1, 1, 1, 1, 1, 1,
                       1, 1, 1, 1, 1, 2, 2)
                .tempo(300)
                .build();

        public static final Melody MARIO_THEME =
            newBuilder()
                .notes('e', 'e', 'e', 'c', 'e', 'g', 'g', 'c', 'g', 'e', 'a',
                       'b', 'a', 'a', 'g', 'e', 'g', 'a', 'f', 'g', 'e', 'c',
                       'd', 'b')
                .beats(1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                       1, 1, 1, 1, 1)
                .tempo(200)
                .build();

        private Melody(Builder builder) {
            this.notes = builder.notes;
            this.beats = builder.beats;
            this.tempo = builder.tempo;
        }

        public char[] getNotes() { return notes; }

        public int[] getBeats() { return beats; }

        public int getTempo() { return tempo; }

        public int getLength() { return notes.length; }

        public static class Builder {
            private char[] notes;
            private int[] beats;
            private int tempo = 300; // default

            public Builder notes(char... notes) {
                this.notes = notes;
                return this;
            }

            public Builder beats(int... beats) {
                this.beats = beats;
                return this;
            }

            public Builder tempo(int tempo) {
                this.tempo = tempo;
                return this;
            }

            public Melody build() {
                if (notes == null || beats == null || notes.length == 0 ||
                    beats.length == 0) {
                    throw new IllegalStateException(
                        "Notes and beats must be set");
                }

                if (notes.length != beats.length) {
                    throw new IllegalStateException(
                        "Notes and beats arrays must have the same length");
                }

                return new Melody(this);
            }
        }

        public static Builder newBuilder() { return new Builder(); }
    }
}
