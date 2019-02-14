package chatpsit.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Utilizzato per scrivere su file in maniera asincrona e thread-safe.
 * Accoda le stringhe da scrivere sul file in una coda che viene continuamente elaborata
 * da un thread che si occupa di effettuare la scrittura.
 */
public class QueueFileWriter
{
    private boolean isClosed = false;

    private BufferedWriter fileWriter;
    private Thread queueProcessingThread;
    private final LinkedBlockingQueue<String> textQueue = new LinkedBlockingQueue<>();

    public QueueFileWriter(Path path) throws IOException
    {
        fileWriter = Files.newBufferedWriter(path);
        queueProcessingThread = new Thread(this::processQueue);
        queueProcessingThread.start();
    }

    /**
     * Accoda per la scrittura su file la stringa specificata
     * @param text la stringa da scrivere su file
     */
    public void appendText(String text)
    {
        if (isClosed)
            throw new UnsupportedOperationException("Il file è già chiuso o in fase di chiusura.");

        textQueue.offer(text);
    }

    private void processQueue()
    {
        // Ciclo di elaborazione della coda
        while (!Thread.currentThread().isInterrupted())
        {
            try
            {
                // FIXME: rimane fermo anche dopo che interrupt() viene chiamato sull'altro thread
                String textToWrite = textQueue.take();
                fileWriter.write(textToWrite + "\n");
            }
            catch (Exception exc)
            {
                if (!(exc instanceof InterruptedException))
                    System.out.println("Impossibile scrivere il messaggio di log su file: " + exc.getMessage());
            }
        }

        // Scrive gli elementi ancora presenti nella coda sul file
        List<String> remainingElements = new ArrayList<>();
        textQueue.drainTo(remainingElements);
        if (remainingElements.size() > 0)
        {
            for (String remainingEntry : remainingElements)
            {
                try {
                    fileWriter.write(remainingEntry + "\n");
                } catch (IOException exc) {
                    System.out.println("Impossibile scrivere il messaggio di log su file: " + exc.getMessage());
                }
            }
        }

        try {
            fileWriter.close();
        }
        catch (IOException e) {
            System.out.println("Impossibile chiudere il file di log: " + e.getMessage());
        }
    }

    /**
     * Segnala al thread che processa la coda di fermarsi appena possibile
     */
    public void stopProcessingAndClose()
    {
        queueProcessingThread.interrupt();
        isClosed = true;
    }
}
