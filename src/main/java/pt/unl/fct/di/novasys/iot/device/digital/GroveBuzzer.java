package pt.unl.fct.di.novasys.iot.device.digital;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;

/**
 * Driver for the Grove Buzzer (active piezo). Plays single tones, named
 * notes from the lower-case octave, and pre-built {@link Melody}
 * sequences ({@link Melody#TWINKLE_TWINKLE}, {@link Melody#HAPPY_BIRTHDAY},
 * {@link Melody#JINGLE_BELLS}, {@link Melody#MARIO_THEME}).
 *
 * <p>Tones are produced by toggling the output pin at a fixed half-period
 * (in microseconds), so this device occupies its calling thread for the
 * duration of {@link #playTone(int, int)}; consider playing melodies on a
 * background thread if you need responsiveness elsewhere.
 */
public class GroveBuzzer extends DigitalOutputDevice {
    private Melody mel;

    /**
     * Constructs a buzzer driven from a single output pin.
     *
     * @param pi4j Pi4J context
     * @param name human-readable name
     * @param line BCM pin number
     * @param ID   caller-assigned device identifier
     */
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

    /**
     * Plays the melody previously installed via
     * {@link #setMelody(Melody)} (or the {@link Melody#getNotes()} /
     * {@link Melody#getBeats()} / {@link Melody#getTempo()} setters).
     *
     * @throws IllegalStateException if no melody has been set
     * @throws InterruptedException  if the inter-note sleep is interrupted
     */
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

    /**
     * Plays the supplied melody once. Does not modify the buzzer's
     * configured melody.
     *
     * @param mel the melody to play
     * @throws InterruptedException if the inter-note sleep is interrupted
     */
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

    /**
     * Plays a named note from the lower-case octave (one of
     * {@code 'c'}, {@code 'd'}, {@code 'e'}, {@code 'f'}, {@code 'g'},
     * {@code 'a'}, {@code 'b'}, or {@code 'C'}). Unknown characters are
     * silent.
     *
     * @param note     the note character
     * @param duration milliseconds to hold the note
     */
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

    /**
     * Sounds a square-wave tone by toggling the output pin. Busy-loops on
     * the calling thread for the full duration.
     *
     * @param tone     half-period in microseconds (smaller = higher pitch)
     * @param duration tone duration in milliseconds
     */
    public void playTone(int tone, int duration) {
        for (long i = 0; i < duration * 1000L; i += tone * 2) {
            dataOut.state(DigitalState.HIGH);
            delayMicroseconds(tone);
            dataOut.state(DigitalState.LOW);
            delayMicroseconds(tone);
        }
    }

    /** @return the notes of the configured melody */
    public char[] getNotes() { return mel.notes; }

    /**
     * Replaces the notes of the configured melody.
     * @param notes the new notes
     */
    public void setNotes(char... notes) { this.mel.notes = notes; }

    /** @return the beat counts of the configured melody */
    public int[] getBeats() { return mel.beats; }

    /**
     * Replaces the beat counts of the configured melody.
     * @param beats the new beat counts
     */
    public void setBeats(int... beats) { this.mel.beats = beats; }

    /** @return the tempo (millisecond multiplier) of the configured melody */
    public int getTempo() { return mel.tempo; }

    /**
     * Replaces the tempo of the configured melody.
     * @param tempo the new tempo (millisecond multiplier per beat)
     */
    public void setTempo(int tempo) { this.mel.tempo = tempo; }

    /** @return the configured melody, or {@code null} if none has been set */
    public Melody getMelody() { return mel; }

    /**
     * Installs a melody to be played by {@link #playMelody()}.
     * @param mel the melody to install
     */
    public void setMelody(Melody mel) { this.mel = mel; }

    /**
     * Immutable description of a melody: a sequence of notes, a parallel
     * sequence of beat counts, and a tempo (millisecond multiplier per
     * beat). A note of {@code ' '} is treated as a rest. Use the
     * {@link Builder} or one of the pre-built constants
     * ({@link #TWINKLE_TWINKLE}, {@link #HAPPY_BIRTHDAY},
     * {@link #JINGLE_BELLS}, {@link #MARIO_THEME}) to build instances.
     */
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

        /** @return the notes in this melody */
        public char[] getNotes() { return notes; }

        /** @return the per-note beat counts */
        public int[] getBeats() { return beats; }

        /** @return the tempo (millisecond multiplier per beat) */
        public int getTempo() { return tempo; }

        /** @return the number of notes in this melody */
        public int getLength() { return notes.length; }

        /**
         * Fluent builder for {@link Melody}. Notes and beats are required
         * and must have the same length; tempo defaults to 300 ms per beat.
         */
        public static class Builder {
            private char[] notes;
            private int[] beats;
            private int tempo = 300; // default

            /**
             * Sets the note sequence. {@code ' '} is treated as a rest.
             * @param notes the notes
             * @return this builder
             */
            public Builder notes(char... notes) {
                this.notes = notes;
                return this;
            }

            /**
             * Sets the beat count for each note. Must have the same length
             * as {@link #notes(char...)}.
             * @param beats per-note beat counts
             * @return this builder
             */
            public Builder beats(int... beats) {
                this.beats = beats;
                return this;
            }

            /**
             * Sets the tempo (millisecond multiplier per beat). Defaults to 300.
             * @param tempo new tempo
             * @return this builder
             */
            public Builder tempo(int tempo) {
                this.tempo = tempo;
                return this;
            }

            /**
             * Builds the melody.
             *
             * @return the new melody
             * @throws IllegalStateException if notes or beats are unset, or
             *                               their lengths differ
             */
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

        /** @return a fresh melody builder */
        public static Builder newBuilder() { return new Builder(); }
    }
}
