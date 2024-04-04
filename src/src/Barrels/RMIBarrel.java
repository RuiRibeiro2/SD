package src.Barrels;

import src.RMIGateway.Configuration;
import src.RMIInterface.RMIBarrelInterface;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class RMIBarrel implements RMIBarrelInterface, Serializable
{
    private String INDEXFILE;
    private String LINKSFILE;
    private int id;
    private HashMap<String, ArrayList<String>> indexMapA_M;
    private HashMap<String, ArrayList<String>> indexMapN_Z; // <<word>, <url1, url2, ...>>
    private HashMap<String, ArrayList<String>> linksMap; // <<url>, <title, description, url1, url2, ...>>
    private HashMap<String, Integer> relevanceMap;
    private String ip;
    private String stats;

    public RMIBarrel(int id, String ip) throws IOException, RemoteException
    {
        if(id < 1 || id > Configuration.NUM_BARRELS)
        {
            System.err.println("Invalid ID");
            System.exit(0);
        }
        else this.id = id;
        this.ip = ip;
        this.indexMapA_M = new HashMap<>();
        this.indexMapN_Z = new HashMap<>();
        this.linksMap = new HashMap<>();
        this.relevanceMap = new HashMap<>();
        this.stats = "Offline";

        File f1,f2;
        boolean copyExists = false;

        for(int i = 1; i <= Configuration.NUM_BARRELS; i++)
        {
            this.INDEXFILE = "Googol\\src\\src\\Barrels\\SaveFiles\\Barrel"+i+".txt";
            this.LINKSFILE = "Googol\\src\\src\\Barrels\\SaveFiles\\Links"+i+".txt";
            f1 = new File(INDEXFILE);
            f2 = new File(LINKSFILE);
            if(f1.exists() && f2.exists())
            {
                updateHashMaps();
                copyExists = true;
                break;
            }
        }
        if(!copyExists)
        {
            this.INDEXFILE = "Googol\\src\\src\\Barrels\\SaveFiles\\Barrel"+id+".txt";
            this.LINKSFILE = "Googol\\src\\src\\Barrels\\SaveFiles\\Links"+id+".txt";
            f1 = new File(INDEXFILE);
            f2 = new File(LINKSFILE);
            f1.createNewFile();
            f2.createNewFile();
            updateHashMaps();
        }
        UnicastRemoteObject.exportObject(this, 0);
        Naming.rebind("rmi://localhost/Barrel" + id, this);
    }

    public static void main(String[] args) throws IOException
    {
        int id = Integer.parseInt(args[0]);
        String ip = InetAddress.getLocalHost().toString().split("/")[1];
        RMIBarrel barrel = new RMIBarrel(id, ip);
        StatusThread statusWarning = new StatusThread(barrel);
        statusWarning.start();
        barrel.startup();
    }
    public void startup()
    {
        try
        {
            sendStatus("Waiting");
            multicastReceiver();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void multicastReceiver() throws IOException {
        MulticastSocket socket = null;
        try
        {
            socket = new MulticastSocket(Configuration.MULTICAST_PORT);
            InetAddress group = InetAddress.getByName(Configuration.MULTICAST_ADDRESS);
            socket.joinGroup(group);

            while (true)
            {
                byte[] buffer = new byte[65533];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                sendStatus("Active");
                String received = new String(packet.getData(), 0, packet.getLength());

                ArrayList<String> data = extractDataFromMulticast(received);
                if (data == null) continue;
                writeToIndexesFile(data);
                writeToLinksFile(data);
                updateHashMaps();
                sendStatus("Waiting");
            }

        } catch (IOException e) {
            sendStatus("Offline");
        } finally {
            sendStatus("Offline");
            socket.close();
        }
    }

    private ArrayList<String> extractDataFromMulticast(String received) {

        // Protocol :
        // type | url; item_count | number; url | www.example.com; referenced_urls |
        // url1 url2 url3; title | title; words | word1 word2 word3

        ArrayList<String> data = new ArrayList<String>();

        String[] fields = received.split("; ");

        String type = fields[0].split(" \\| ")[1];
        if (!type.equals("url"))
            return null;

        String[] url = fields[2].split(" \\| ");

        String referencedUrls = fields[3].split(" \\| ")[1];

        String[] list_referenceUrl = referencedUrls.split(" ");

        // Create a url: url|referencedUrl1|referencedUrl2|...
        String urlAndReferencedUrls = url[1];
        for (int i = 0; i < list_referenceUrl.length; i++) {
            if (list_referenceUrl[i].equals("None")) {
                // If the last character is a "|", remove it
                if (urlAndReferencedUrls.charAt(urlAndReferencedUrls.length() - 1) == '|') {
                    urlAndReferencedUrls = urlAndReferencedUrls.substring(0, urlAndReferencedUrls.length() - 1);
                }
                break;
            }

            urlAndReferencedUrls += "|" + list_referenceUrl[i];
        }

        data.add(urlAndReferencedUrls);

        // Get the title
        try {
            String[] title = fields[4].split(" \\| ");
            data.add(title[1]);
        } catch (Exception e) {
            for (String field : fields) {
                System.out.println("FIELD: " + field);
            }
            e.printStackTrace();
        }

        // Get the words
        String[] words = fields[5].split(" \\| ");

        // replace spaces with ";" except the first one
        String wordsSeparatedBySemicolon = words[1].replace(" ", ";");

        // Remove pontuation from wordsSeparatedBySemicolon except for ;
        wordsSeparatedBySemicolon = wordsSeparatedBySemicolon.replaceAll("[^a-zA-Z0-9\\p{L};]", "");

        data.add(wordsSeparatedBySemicolon);

        return data;
    }

    private void updateHashMaps()
    {
        synchronized (linksMap)
        {
            try
            {
                BufferedReader reader = new BufferedReader(new FileReader(LINKSFILE));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(";");
                    String urlString = parts[0];

                    if (parts.length != 3) {
                        System.out.println("Barrel[" + this.id + "] [No description] failed to store in barrel");
                        reader.close();
                        return;
                    }

                    ArrayList<String> info = new ArrayList<>();
                    info.add(parts[1]); // title
                    info.add(parts[2]); // context

                    String[] urlParts = urlString.split("\\|");
                    for (int i = 1; i < urlParts.length; i++) {
                        info.add(urlParts[i]);
                    }

                    linksMap.put(urlString.split("\\|")[0], info);
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        synchronized (indexMapA_M)
        {
            try
            {
                BufferedReader reader = new BufferedReader(new FileReader(INDEXFILE));
                String line;
                while ((line = reader.readLine()) != null)
                {
                    String[] parts = line.split(";");
                    String word = parts[0].toLowerCase();
                    ArrayList<String> urls = new ArrayList<>();

                    for(int i = 1; i < parts.length; i++)
                    {
                        urls.add(parts[i]);
                    }

                    if (word.charAt(0) <= 'm')
                    {
                        indexMapA_M.put(word, urls);
                    }
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        synchronized (indexMapN_Z)
        {
            try
            {
                BufferedReader reader = new BufferedReader(new FileReader(INDEXFILE));
                String line;
                while ((line = reader.readLine()) != null)
                {
                    String[] parts = line.split(";");
                    String word = parts[0].toLowerCase();
                    ArrayList<String> urls = new ArrayList<>();

                    for(int i = 1; i < parts.length; i++)
                    {
                        urls.add(parts[i]);
                    }

                    if (word.charAt(0) > 'm')
                    {
                        indexMapN_Z.put(word, urls);
                    }
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void writeToLinksFile(ArrayList<String> data)
    {
        String[] firstElement = data.get(0).split("\\|"); // url|referencedUrl1|referencedUrl2|...
        String url = firstElement[0];
        synchronized (LINKSFILE)
        {
            List<String> lines = new ArrayList<String>();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(LINKSFILE));
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                    if (line.split(";")[0].equals(url)) {
                        reader.close();
                        return;
                    }
                }
                reader.close();
            } catch (IOException e) {
                System.err.println("Error reading links file");
            }

            String otherUrls = "";
            for (int i = 2; i < firstElement.length; i++) {
                if (i != firstElement.length - 1)
                    otherUrls += firstElement[i] + "|";
                else
                    otherUrls += firstElement[i];
            }

            boolean found = false;
            for (String linha : lines) {
                if (linha.equals(url)) {
                    found = true;
                }
            }

            if (!found)
            {
                int titleSize = data.get(1).split(" ").length;
                String[] words = data.get(2).split(";");
                String context = "";
                for (int i = 2 + titleSize; i < words.length && i < Configuration.CONTEXT_SIZE + 2 + titleSize; i++) {
                    context += words[i] + " ";
                }

                String linha;
                if (!otherUrls.isEmpty()) {
                    linha = url + "|" + otherUrls + ";" + data.get(1) + ";" + context;
                } else {
                    linha = url + ";" + data.get(1) + ";" + context;
                }

                if (context == null || context.isEmpty()) {
                    System.out.println("Barrel[" + this.id + "] [No description] failed to store in barrel");
                    return;
                }

                try {
                    FileWriter writer = new FileWriter(LINKSFILE, true);
                    writer.write(linha);
                    writer.write(System.lineSeparator());
                    writer.close();
                } catch (IOException e) {
                    System.err.println("Error writing to links file");
                }
            }
        }


    }

    private void writeToIndexesFile(ArrayList<String> data) throws IOException
    {
        synchronized (INDEXFILE)
        {
            List<String> lines = new ArrayList<String>();
            BufferedReader reader = new BufferedReader(new FileReader(INDEXFILE));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();


            String[] firstElement = data.get(0).split("\\|"); // url|referencedUrl1|referencedUrl2|...
            String url = firstElement[0];

            String[] words = data.get(2).split(";");

            for (String word : words)
            {
                boolean found = false;

                if (word == null || word.isEmpty()) {
                    continue;
                }
                // If a given word already exists in the file, then it is updated with new urls
                // If not, simply add the word to file
                for (String linha : lines)
                {
                    String[] parts = linha.split(";");
                    String wordInFile = parts[0].toLowerCase();
                    List<String> links = Arrays.asList(parts).subList(1, parts.length);
                    if (wordInFile.equals(word.toLowerCase())) {
                        if (!links.contains(url)) {
                            lines.set(lines.indexOf(linha), linha + ";" + url);
                        }
                        found = true;
                    }
                }

                if (!found) {
                    lines.add(word + ";" + url);
                }
            }

            FileWriter writer = new FileWriter(INDEXFILE);
            for (String linha : lines) {
                writer.write(linha);
                writer.write(System.lineSeparator());
            }
            writer.close();
        }

    }

    // Find every link that points to a page
    @Override
    public List<String> searchLinks(String word) throws FileNotFoundException, IOException
    {

        List<String> result = new ArrayList<String>();
        for (String url : this.linksMap.keySet()) {
            ArrayList<String> info = this.linksMap.get(url);
            for (int i = 2; i < info.size(); i++) {
                if (info.get(i).equals(word)) {
                    result.add(url);
                }
            }
        }

        return result;
    }

    public void sendStatus(String status) throws IOException
    {

        // Compare this.stats with status
        if (this.stats.equals(status))
        {
            return;
        }
        this.stats = status;

        InetAddress group = InetAddress.getByName(Configuration.MULTICAST_ADDRESS);
        MulticastSocket socket = new MulticastSocket(Configuration.MULTICAST_PORT);

        String statusString = "type | Barrel; index | " + this.id + "; status | " + status + "; ip | "
                + this.ip + ";";

        byte[] buffer = statusString.getBytes();

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, Configuration.MULTICAST_PORT);
        System.out.println(statusString);
        socket.send(packet);
        socket.close();
    }

    @Override
    public List<String> searchWords(String word) throws FileNotFoundException, IOException
    {
        String words[] = word.split(" ");
        relevanceMap.clear();

        // Gets the links for each word
        for (String target : words)
        {
            word = target.toLowerCase();
            // Search for word that starts with A-M
            if (word.charAt(0) <= 'm' && indexMapA_M.containsKey(word))
            {
                ArrayList<String> urls = indexMapA_M.get(word);
                for (String url : urls)
                {
                    if (relevanceMap.containsKey(url))
                    {
                        relevanceMap.put(url, relevanceMap.get(url) + 1);
                    }
                    else
                    {
                        relevanceMap.put(url, 1);
                    }
                }
            }
            // Search for word that starts with N-Z
            else if(word.charAt(0) > 'm' && indexMapN_Z.containsKey(word))
            {
                ArrayList<String> urls = indexMapN_Z.get(word);
                for (String url : urls)
                {
                    if (relevanceMap.containsKey(url))
                    {
                        relevanceMap.put(url, relevanceMap.get(url) + 1);
                    }
                    else
                    {
                        relevanceMap.put(url, 1);
                    }
                }
            }
        }

        // Remove links from the map that don't have all the words
        Iterator<String> iterator = relevanceMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (relevanceMap.get(key) != words.length) {
                iterator.remove();
            }
        }

        // Create a list with the results
        ArrayList<String> results = new ArrayList<String>();
        for (int i = 0; i < relevanceMap.size(); i++) {
            String key = (String) relevanceMap.keySet().toArray()[i];
            ArrayList<String> info = linksMap.get(key);
            String result = key + ";" + info.get(0) + ";" + info.get(1);
            results.add(result);
        }

        // Count the number of times each url is referenced in the keys of linksMap
        Collections.sort(results, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String[] parts1 = o1.split(";");
                String[] parts2 = o2.split(";");
                String url1 = parts1[0];
                String url2 = parts2[0];
                int count1 = 0;
                int count2 = 0;
                for (String key : linksMap.keySet())
                {
                    ArrayList<String> urls = linksMap.get(key);
                    if (urls.contains(url1))
                    {
                        count1++;
                    }
                    if (urls.contains(url2)) {
                        count2++;
                    }
                }
                return count2 - count1;
            }
        });

        return results;
    }
}