package src.RMIGateway;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;

public class AdminPage {
    private ArrayList<String> downloaders;
    private ArrayList<String> barrels;
    private HashMap<String, Integer> relevanceDictionary;

    private String stringMenu;

    public AdminPage(HashMap<String, Integer> relevanceDictionary) {
        this.downloaders = new ArrayList<String>();
        this.barrels = new ArrayList<String>();
        this.relevanceDictionary = relevanceDictionary;
    }

    public void showMenu() {
        inicializeArrays();
        stringMenu = generatePanelString();
        getActiveDownloadersAndBarrels();

    }

    private void getActiveDownloadersAndBarrels()
    {
        MulticastSocket socket = null;
        try
        {
            socket = new MulticastSocket(Configuration.MULTICAST_PORT);
            InetAddress group = InetAddress.getByName(Configuration.MULTICAST_ADDRESS);
            socket.joinGroup(group);

            while (true)
            {
                byte[] buffer = new byte[16384];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String msg = new String(packet.getData(), 0, packet.getLength());
                System.out.println(msg);
                update(msg);

                stringMenu = generatePanelString();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    private void update(String msg)
    {
        String[] msg_split = msg.split(";");

        // Protocol : "type | Downloader; status | Active; url | www.example.com;

        if (msg_split[0].split("\\|")[1].trim().equals("Downloader"))
        {
            int index = Integer.parseInt(msg_split[1].split("\\|")[1].trim());
            String status = msg_split[2].split("\\|")[1].trim();
            String url = msg_split[3].split("\\|")[1].trim();
            this.downloaders.set(index - 1, status + " - " + url);
        }
        else if (msg_split[0].split("\\|")[1].trim().equals("Barrel"))
        {
            int index = Integer.parseInt(msg_split[1].split("\\|")[1].trim());
            String status = msg_split[2].split("\\|")[1].trim();
            String ip = msg_split[3].split("\\|")[1].trim();
            String port = msg_split[4].split("\\|")[1].trim();
            this.barrels.set(index - 1, status + " - " + ip + " - " + port);
        }
    }

    public void updateOfflineBarrels(int id)
    {
        this.barrels.set(id,"Offline");
    }



    private String generatePanelString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("------- Downloaders -------\n");
        for (int i = 0; i < Configuration.NUM_DOWNLOADERS; i++) {
            int aux = i + 1;
            sb.append("Downloader[" + aux + "] " + this.downloaders.get(i) + "\n");
        }

        sb.append("\n------- Barrels -------\n");
        for (int i = 0; i < Configuration.NUM_BARRELS; i++) {
            int aux = i + 1;
            sb.append("Barrel[" + aux + "] " + this.barrels.get(i) + "\n");
        }

        sb.append("\n------- Most Frequent Searches -------\n");
        if (this.relevanceDictionary.isEmpty()) {
            for (int i = 0; i < 10; i++) {
                int aux = i + 1;
                sb.append("Search[" + aux + "] None\n");
            }
        } else {

            for (int i = 0; i < 10 && i < this.relevanceDictionary.size(); i++) {
                int aux = i + 1;
                if (this.relevanceDictionary.containsKey(this.relevanceDictionary.keySet().toArray()[i])) {
                    sb.append("Search[" + aux + "]: " + this.relevanceDictionary.keySet().toArray()[i] + " - "
                            + this.relevanceDictionary.get(this.relevanceDictionary.keySet().toArray()[i])
                            + "\n");
                }
            }
        }

        return sb.toString();
    }

    private void inicializeArrays() {
        for (int i = 0; i < Configuration.NUM_DOWNLOADERS; i++) {
            this.downloaders.add("Waiting");
        }
        for (int i = 0; i < Configuration.NUM_BARRELS; i++) {
            this.barrels.add("Offline");
        }
    }

    public void updateHashMap(HashMap<String, Integer> dic)
    {
        this.relevanceDictionary = dic;
        stringMenu = generatePanelString();
    }



    public String getStringMenu() {
        return this.stringMenu;
    }



}