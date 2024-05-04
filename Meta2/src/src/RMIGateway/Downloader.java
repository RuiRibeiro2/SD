package src.RMIGateway;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.HashSet;

public class Downloader extends Thread
{
    private String url;
    private Document doc;
    private HashSet<String> links;
    private String words;
    private String title;
    private String data;
    private int ID;

    /**
     *
     * Class constructor
     *
     * @param ID Downloader ID
     */
    public Downloader(int ID) {
        this.ID = ID;
        this.links = new HashSet<String>();
        this.words = "";
    }

    /**
     * Running thread
     * @throws RuntimeException
     */
    public void run() throws RuntimeException {

        try {
            sendStatus("Waiting");
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (true)
        {
            clear();
            try // Tries to retrieve URL from queue
            {
                this.url = getUrl();
            } catch (InterruptedException e)
            {
                System.err.printf("Downloader[%d] [ON WHILE TRUE] failed to get URL from queue", this.ID);

            }
            if (this.url == null)
            {
                System.out.println("No more urls to download");
                continue;
            }
            sendStatus("Active");

            try
            {
                // Using jsoup parser
                this.doc = Jsoup.connect(this.url).get();
                // Invalid url
            } catch (ConnectException e)
            {
                System.out.printf("Downloader[%d] [Connection failed] failed to connect to url: %s%n", this.ID, this.url);

                try
                {
                    this.links.clear();
                    this.links.add(this.url);
                    sendLinkToQueue(true);
                } catch (Exception e1)
                {
                    System.err.printf("Failed to enqueue URL in the downloader[%d]", this.ID);
                }
                continue;
            } catch (Exception e)
            {
                System.err.printf("Failed to download the URL[%s] in the downloader[%d]", this.url, this.ID);
                continue;
            }

            try
            {
                if (Configuration.SABOTAGE_DOWNLOADERS)
                {
                    int random = (int) (Math.random() * 5) + 1;
                    if (this.ID == random)
                    {
                        System.out.printf("Simulated a crash in downloader with ID: [%d]", this.ID);
                        throw new Exception();
                    }
                }
                download();
                if (this.title == null || this.title.isEmpty())
                {
                    // URL without title is invalid
                    System.out.printf("Downloader[%d] [No title] failed to download url: %s%n", this.ID, this.url);
                    continue;
                }
                sendWords();
                sendLinkToQueue(false);

            } catch (Exception e)
            {
                System.out.printf("The downloader with ID: [%d] has stopped working!%n", this.ID);

                // Resend current link to queue
                try {
                    this.links.clear();
                    this.links.add(this.url);
                    sendLinkToQueue(true);
                } catch (Exception e1) {
                    System.out.printf("Failed to send URL to the queue by downloader with ID: [%d]%n", this.ID);
                }
                try {
                    sendStatus("Offline");
                    return;
                } catch (Exception e1) {
                    System.out.printf("Failed to send status of Downloader[%d]%n", this.ID);
                }
            }
        }
    }

    /**
     * Download process filters special characters: "|" ";" "\n"
     */
    private void download() {
        String title;
        try {
            title = doc.title();
        } catch (NullPointerException e) {
            return;
        }

        this.title = title;
        this.title = this.title.replace("|", "");
        this.title = this.title.replace(";", "");
        this.title = this.title.replace("\n", "");

        String[] words = doc.text().split(" ");
        for (String word : words)
        {
            if (word.contains("|") || word.contains(";") || word.contains("\n")) continue;
            this.words += word + ";";
        }

        Elements links = doc.select("a[href]");
        for (Element link : links) {
            String url = link.attr("abs:href");
            // Removing special characters to prevent future conflicts
            url = url.replace("|", "");
            url = url.replace(";", "");
            url = url.replace("\n", "");
            this.links.add(url);
        }
    }

    /**
     * Sends information collected about a url to Barrels through Multicast
     * @throws Exception
     */
    private void sendWords() throws Exception
    {

        // Protocol Multicast :
        // type | url; item_count | number; url | www.example.com; referenced_urls |
        // url1 url2 url3; title | title; words | word1 word2 word3

        String referencedUrls = "type | url; item_count | " + this.links.size() + "; url | " + this.url
                + "; referenced_urls | ";

        if (this.links.isEmpty()) referencedUrls += "None; ";

        int linkCount = 0;
        for (String link : this.links)
        {
            if (linkCount == Configuration.MAX_REF_LINKS)
            {
                referencedUrls += "; ";
                break;
            }
            if (link == this.links.toArray()[this.links.size() - 1]) referencedUrls += link + "; ";
            else referencedUrls += link + " ";
            linkCount++;
        }

        if (this.words == null) this.words = "None";


        this.words = this.words.replace(";", " ");

        referencedUrls += "title | " + this.title + "; " + "words | " + this.words;
        this.data = referencedUrls;

        InetAddress group = InetAddress.getByName(Configuration.MULTICAST_ADDRESS);
        MulticastSocket socket = new MulticastSocket(Configuration.MULTICAST_PORT);

        byte[] buffer = this.data.getBytes();

        if (buffer.length > 65534) {
            System.err.println("Downloader[" + this.ID + "] [Page too long] " + "failed to send url to queue");
            socket.close();
            return;
        }

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, Configuration.MULTICAST_PORT);
        socket.send(packet);
        socket.close();
    }

    /**
     * Receives URL from URLQueue
     * @return URL obtained from URLQueue
     * @throws InterruptedException
     */
    private String getUrl() throws InterruptedException {
        String url = null;
        while (url == null)
        {
            try
            {
                Socket socket = new Socket("localhost", Configuration.SEND_PORT);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                url = in.readLine();
                socket.close();
            } catch (Exception e)
            {
                System.err.println(
                        "Downloader[" + this.ID + "] " + "failed to get url from queue, trying again in 3 seconds");
                Thread.sleep(3000);
            }
        }

        return url;
    }

    /**
     * Sends any URLs obtained from HTML parsing back to URLQueue, including repeated ones
     * @param resend Boolean that checks if the URL has already been analysed by Downloader
     * @throws IOException
     * @throws InterruptedException
     */
    private void sendLinkToQueue(boolean resend) throws IOException, InterruptedException {

        int numberTries = 0;
        boolean success = false;
        while (!success)
        {
            try
            {
                Socket socket = new Socket("localhost", Configuration.RECEIVE_PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                if (resend) {
                    for (String link : links) {
                        link = "[RESEND]" + link;
                        out.println(link);
                    }
                } else {
                    for (String link : links) {
                        out.println(link);
                    }
                }

                socket.close();
                success = true;
            } catch (Exception e)
            {
                numberTries++;
                System.err.println(
                        "Downloader[" + this.ID + "] [Attempts: " + numberTries + "] "
                                + "failed to send url to queue, trying again in 3 seconds");
                Thread.sleep(3000);
            }
        }
    }

    /**
     * Updates admin page about this downloader' status
     * @param status Status in string format
     */
    private void sendStatus(String status) {
        try {
            InetAddress group = InetAddress.getByName(Configuration.MULTICAST_ADDRESS);
            MulticastSocket socket = new MulticastSocket(Configuration.MULTICAST_PORT);

            // Protocol : "type | Downloader; status | Active; url | www.example.com;
            String statusString = "type | Downloader; index | " + this.ID + "; status | " + status + "; url | "
                    + this.url;

            byte[] buffer = statusString.getBytes();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group,
                    Configuration.MULTICAST_PORT);
            socket.send(packet);
            socket.close();
        } catch (Exception e) {
            System.err.println("Downloader[" + this.ID + "] " + "failed to send status to admin");
        }
    }

    /**
     * Clears data structures used by this Downloader
     */
    private void clear() {
        this.links.clear();
        this.words = "";
        this.data = "";
    }
}