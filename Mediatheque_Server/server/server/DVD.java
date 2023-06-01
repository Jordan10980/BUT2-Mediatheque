package server;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DVD implements Document {
    private int numero;
    private String titre;
    private boolean adulte;
    private Abonne empruntePar;
    private Abonne reservePar;
    private LocalDateTime reservationTimestamp;

    public DVD(int numero, String titre, boolean adulte) {
        this.numero = numero;
        this.titre = titre;
        this.adulte = adulte;
    }

    @Override
    public int numero() {
        return numero;
    }

    @Override
    public Abonne empruntePar() {
        return empruntePar;
    }

    @Override
    public Abonne reservePar() {
        return reservePar;
    }

    public LocalDateTime getReservationTimestamp() {
        return reservationTimestamp;
    }

    public long getSecondsSinceReservation() {
        return ChronoUnit.SECONDS.between(reservationTimestamp, LocalDateTime.now());
    }

    @Override
    public void reservation(Abonne ab) {
        reservePar = ab;
        reservationTimestamp = LocalDateTime.now();
    }

    @Override
    public void emprunt(Abonne ab) {
        empruntePar = ab;
        reservePar = null;
        reservationTimestamp = null;
    }

    @Override
    public void retour() {
        empruntePar = null;
        reservePar = null;
        reservationTimestamp = null;
    }

    public void playWaitingMusic() {
        try {
            File yourFile = new File("attente.wav");
            AudioInputStream stream = AudioSystem.getAudioInputStream(yourFile);
            AudioFormat format = stream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(stream);
            System.out.println("Starting music playback..."); // This line
            clip.start();

            while (clip.isRunning()) { // This loop
                Thread.sleep(100); // Sleep a while
            }

            System.out.println("Music playback ended."); // And this line
        } catch (UnsupportedAudioFileException e) {
            System.err.println("The specified audio file is not supported.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error opening the audio file.");
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            System.err.println("Audio line for playing the file is unavailable.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Sleep interrupted");
            e.printStackTrace();
        }
    }
}
