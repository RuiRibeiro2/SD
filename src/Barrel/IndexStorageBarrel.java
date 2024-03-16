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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;




public class IndexStorageBarrel extends Thread implements Serializable, IndexStorageBarrelInterface
{
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int MULTICAST_PORT = 4000;


    private HashMap<String, ArrayList<String>> indexMap; // <<word>, <url1, url2, ...>>
    private HashMap<String, ArrayList<String>> linksMap; // <<url>, <title, description, url1, url2, ...>>


    public static void main(String[] args) throws RemoteException  {
        IndexStorageBarrel barrel = new IndexStorageBarrel();
        //RMI connection to Gateway
        LocateRegistry.createRegistry(1099).rebind("barrel", barrel);
        barrel.start();

    }
    public IndexStorageBarrel() {
        super("Index Storage Barrel " + (long) (Math.random() * 1000) + "now available");
        this.indexMap = new HashMap<>();
        this.linksMap = new HashMap<>();
    }

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
        }
    }

    @Override
    public ArrayList<String> searchWord(String word) throws FileNotFoundException, IOException
    {
        ArrayList<String> urls = indexMap.get(word);
        return urls;
    }

    @Override
    public ArrayList<String> searchPage(String word) throws FileNotFoundException, IOException
    {
        return null;
    }
}
