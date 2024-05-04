package src.UrlQueue;

import src.RMIGateway.Configuration;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Class responsible for storing the url information obtained from Downloaders in a single queue.
 * Works alongside 2 threads: one for sending and other for receiving urls.
 */
public class UrlQueue {

    private Queue<String> queue;
    private ArrayList<String> visited;

    /**
     * Class constructor
     */
    public UrlQueue() {
        queue = new LinkedList<String>();
        visited = new ArrayList<String>();
    }

    /**
     * Adds URL to queue
     * @param url URL
     * @param resend Boolean signaling if url has been sent before
     */
    public void addUrl(String url, boolean resend)
    {
        if (!resend)
        {
            if (visited.contains(url)) return;
        }
        System.out.println("Added url: " + url);
        queue.add(url);
        visited.add(url);
    }

    /**
     * Gets the first element/url in the queue.
     * @return Url if queue is not empty, null if it's empty
     */
    public String getUrl() {
        if (queue.isEmpty()) return null;
        return queue.poll();
    }

    public static void main(String[] args) throws UnknownHostException, IOException
    {
        UrlQueue urlQueue = new UrlQueue();
        QueueThread queueSend = new QueueThread(urlQueue, Configuration.SEND_PORT);
        QueueThread queueReceive = new QueueThread(urlQueue, Configuration.RECEIVE_PORT);
        queueSend.start();
        queueReceive.start();
    }

}
