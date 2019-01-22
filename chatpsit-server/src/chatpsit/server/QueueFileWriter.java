package chatpsit.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Utilizzato per scrivere su file in maniera asincrona e thread-safe.
 * Accoda le stringhe da scrivere sul file in una coda che viene continuamente elaborata
 * da un thread che si occupa di effettuare la scrittura.
 */
public class QueueFileWriter
{
    private BufferedWriter fileWriter;
    private Thread queueProcessingThread;
    private final LinkedBlockingQueue<String> textQueue = new LinkedBlockingQueue<>();

    public QueueFileWriter(Path path) throws IOException
    {
        fileWriter = Files.newBufferedWriter(path);
        queueProcessingThread = new Thread(this::processQueue);
        queueProcessingThread.start();
    }

    public void appendText(String text)
    {
        try {
            textQueue.put(text);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void processQueue()
    {
        while (!Thread.interrupted())
        {
            try {
                String textToWrite = textQueue.take();
                fileWriter.write(textToWrite);
                fileWriter.newLine();
            }
            catch (InterruptedException exc) {
                exc.printStackTrace();
            }
            catch (IOException exc) {
                System.out.println("Impossibile scrivere il messaggio di log su file.");
            }
        }

        try {
            fileWriter.close();
        }
        catch (IOException e) {
            System.out.println("Impossibile chiudere il file di log.");
        }
    }

    public void stopProcessingAndClose()
    {
        queueProcessingThread.interrupt();
    }
}
