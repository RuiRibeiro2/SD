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
import java.util.HashMap;
import java.util.List;

public class IndexStorageBarrel extends Thread implements Serializable, IndexStorageBarrelInterface
{
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int MULTICAST_PORT = 4000;

    private HashMap<String, ArrayList<String>> indexMap; // <<word>, <url1, url2, ...>>
    private HashMap<String, ArrayList<String>> linksMap; // <<url>, <title, description, url1, url2, ...>>


    public static void main(String[] args) {
        IndexStorageBarrelInterface isbi = new IndexStorageBarrel();

        try {
            LocateRegistry.createRegistry(1099).rebind("barrel", isbi);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
    public IndexStorageBarrel() {
        super("Index Storage Barrel " + (long) (Math.random() * 1000) + "now available");
        this.indexMap = new HashMap<>();
        this.linksMap = new HashMap<>();
        try {
            UnicastRemoteObject.exportObject(this, 0);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
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
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            socket.close();
        }



    }

    @Override
    public List<String> searchWord(String word) throws FileNotFoundException, IOException
    {
        return null;
    }

    @Override
    public List<String> searchPage(String word) throws FileNotFoundException, IOException
    {
        return null;
    }
}
