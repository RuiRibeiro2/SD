package src.UrlQueue;

import src.RMIGateway.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
/**
 * Thread class responsible for managing the flow of urls in class URLQueue.
 * Depending on the port used, it can be used for receiving or sending urls.
 */
public class QueueThread extends Thread {
    private ServerSocket serverSocket;
    private int port;
    private UrlQueue urlQueue;

    public QueueThread(UrlQueue urlQueue, int port) throws IOException {
        if (port != Configuration.SEND_PORT && port != Configuration.RECEIVE_PORT)
            throw new IllegalArgumentException("Invalid port number");

        this.port = port;
        this.serverSocket = new ServerSocket(port);
        this.urlQueue = urlQueue;
    }

    private void sendUrl() throws IOException {
        String url;

        synchronized (urlQueue) {
            url = urlQueue.getUrl();
        }

        if (url != null) {
            Socket socket = serverSocket.accept();
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(url);
            socket.close();
            System.out.println("Sent url: " + url);
        }
    }

    private void receiveUrl() throws IOException
    {
        Socket socket = serverSocket.accept();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String url;
        boolean resend = false;
        while ((url = in.readLine()) != null)
        {
            if (url.startsWith("[RESEND]"))
            {
                url = url.substring(8);
                System.out.println("[RE-ADDED]: " + url);
                resend = true;
            }
            synchronized (urlQueue)
            {
                urlQueue.addUrl(url, resend);
            }
        }

        socket.close();
    }

    public void run()
    {
        while (true)
        {
            try
            {
                if (port == Configuration.SEND_PORT)
                {
                    sendUrl();
                }
                else if (port == Configuration.RECEIVE_PORT)
                {
                    receiveUrl();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}