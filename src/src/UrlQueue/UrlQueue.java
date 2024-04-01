package src.UrlQueue;

import src.RMIGateway.Configuration;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class UrlQueue {

    private Queue<String> queue;
    private ArrayList<String> visited;

    public UrlQueue() {
        queue = new LinkedList<String>();
        visited = new ArrayList<String>();
    }

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

    public String getUrl() {
        if (queue.isEmpty()) return null;

        return queue.poll();
    }

    public static void main(String[] args) throws UnknownHostException, IOException {

        UrlQueue urlQueue = new UrlQueue();
        QueueThread queueSend = new QueueThread(urlQueue, Configuration.PORT_A);
        QueueThread queueReceive = new QueueThread(urlQueue, Configuration.PORT_B);
        queueSend.start();
        queueReceive.start();
    }

}
