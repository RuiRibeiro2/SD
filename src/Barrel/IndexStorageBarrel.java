package Barrel;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.net.InetAddress;
import java.io.IOException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;


class MulticastBarrel implements Runnable
{
    private String MULTICAST_ADDRESS;
    private int MULTICAST_PORT;
    private Map<String, List<String>> indexMap; // <<word>, <url1, url2, ...>>
    private Map<String, List<String>> linksMap; // <<url>, <title, description, url1, url2, ...>>

    public MulticastBarrel(String address, int port)
    {
        this.MULTICAST_ADDRESS = address;
        this.MULTICAST_PORT = port;
        this.indexMap = new HashMap<>();
        this.indexMap.computeIfAbsent("universidade", k -> new ArrayList<>()).add("http://uc.pt");
        this.indexMap.computeIfAbsent("mooshak", k -> new ArrayList<>()).add("http://mooshak.dei.pt");
        this.linksMap = new HashMap<>();
    }
    @Override
    public void run()
    {
        MulticastSocket socket = null;
        // Multicast communication with Downloaders
        try
        {
            socket = new MulticastSocket(this.MULTICAST_PORT); // socket made for receiving packets containing entries to hashmaps
            InetAddress address = InetAddress.getByName(this.MULTICAST_ADDRESS);
            socket.joinGroup(address);
            while(true)
            {
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                System.out.println("Waiting for packets...");
                socket.receive(packet);
                String data = new String(packet.getData(), 0, packet.getLength());
                unpackData(data);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            socket.close();
        }
    }

    public Map<String, List<String>> getIndexMap()
    {
        return indexMap;
    }

    public Map<String, List<String>> getLinksMap()
    {
        return linksMap;
    }

    private void unpackData(String data)
    {
        String[] dataElements = data.split(";", 0);
        String[] typeElement = dataElements[0].split("|",0);
        var type = typeElement[0];
        if (type.equals("url_list")) {
            var x = dataElements[1].split("|",0)[1];
            int num_items = Integer.parseInt(x);
            ArrayList<String> urlContent= new ArrayList<>(Arrays.asList(dataElements));

            //Remove type element
            urlContent.remove(0);
            //Remove count element
            urlContent.remove(0);
            updateIndexMap(urlContent,num_items);
        } else {
            throw new IllegalArgumentException("Invalid type received: " + type);
        }


    }
    private void updateIndexMap(ArrayList<String> urlContent,int num_items)
    {
        for(int i = 0; i < num_items; i++)
        {
            String[] entry = urlContent.get(i).split("|",0);
            String word = entry[0];
            String url = entry[1];
            if(this.indexMap.get(word) == null)
            {
                this.indexMap.computeIfAbsent(word, k -> new ArrayList<>()).add(url);
            }
            else
            {
                ArrayList<String> urls = (ArrayList<String>) this.indexMap.get(word);
                urls.add(url);
                this.indexMap.remove(word);
                this.indexMap.put(word,urls);
            }
        }
    }

}


public class IndexStorageBarrel extends UnicastRemoteObject implements Serializable, IndexStorageBarrelInterface
{
    private String MULTICAST_ADDRESS = "224.3.2.1";
    //2 barrel threads for multicast handling
    private MulticastBarrel barrel;
    private int MULTICAST_PORT = 4000;

    public static void main(String[] args) throws RemoteException
    {
        IndexStorageBarrelInterface isbi = new IndexStorageBarrel();
        //RMI connection to Gateway
        LocateRegistry.createRegistry(1100).rebind("barrel", isbi);
        System.out.println("Registered");


    }
    public IndexStorageBarrel() throws RemoteException {
        super();
        this.barrel = new MulticastBarrel(this.MULTICAST_ADDRESS,this.MULTICAST_PORT);
        Thread thread = new Thread(this.barrel);
        thread.start();



    }


    @Override
    public List<String> searchWord(String word) throws FileNotFoundException, IOException
    {
        List<String> urls = this.barrel.getIndexMap().get(word);
        return urls;
    }

    @Override
    public List<String> searchPage(String word) throws FileNotFoundException, IOException
    {
        return null;
    }
}
