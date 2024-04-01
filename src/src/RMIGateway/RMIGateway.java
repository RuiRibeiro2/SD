package src.RMIGateway;

import src.RMIInterface.RMIBarrelInterface;
import src.RMIInterface.RMIGatewayInterface;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class RMIGateway extends UnicastRemoteObject implements RMIGatewayInterface
{
    private AdminPage adminPage;
    private HashMap<String, Integer> searchDictionary;

    public RMIGateway() throws RemoteException
    {
        super();
        this.searchDictionary = new HashMap<String, Integer>();
        adminPage = new AdminPage(searchDictionary);
    }

    private String separateWordsAlphabet(String frase) {
        String[] words = frase.split(" ");
        StringBuilder wordsA_M = new StringBuilder();
        for (String x : words) {
            if (x.toLowerCase().charAt(0) < 'm') {
                wordsA_M.append(x).append(" ");
            }
        }
        return wordsA_M.toString();
    }

    private String separateWordsAlphabet2(String frase)
    {
        String[] words = frase.split(" ");
        StringBuilder wordsN_Z = new StringBuilder();
        for (String x : words) {
            if (x.toLowerCase().charAt(0) >= 'm') {
                wordsN_Z.append(x).append(" ");
            }
        }
        return wordsN_Z.toString();
    }

    private int generateRandomEvenNumber()
    {
        int random = (int) (Math.random() * Configuration.NUM_BARRELS) + 1;
        while (random % 2 == 1) {
            random = (int) (Math.random() * Configuration.NUM_BARRELS) + 1;
        }
        return random;
    }

    private int generateRandomNumber(){ return (int) (Math.random() * Configuration.NUM_BARRELS) + 1;}
    private int generateRandomOddNumber()
    {
        int random = (int) (Math.random() * Configuration.NUM_BARRELS) + 1;
        while (random % 2 == 0) {
            random = (int) (Math.random() * Configuration.NUM_BARRELS) + 1;
        }
        return random;
    }


    @Override
    public List<String> searchWords(String word) throws IOException {

        // Barrels with even (pares) ID numbers store words from A to M
        // Barrels with odd (impares) ID numbers store words from N to Z


        List<String> resultA_M = new ArrayList<String>();
        List<String> resultN_Z = new ArrayList<String>();
        String wordsA_M = "";
        String wordsN_Z = "";
        long start;
        long finish;

        try {
            wordsA_M = separateWordsAlphabet(word);
            wordsN_Z = separateWordsAlphabet2(word);
        } catch (Exception e) {
            System.out.println("Error separating words alphabetically.");
        }

        int miss_counter = 0;


        if (!wordsA_M.isEmpty())
        {
            int randomBarrel = generateRandomEvenNumber();
            boolean connected = false;

            while (!connected)
            {
                if(miss_counter == 5)
                {
                    throw new RemoteException();
                }
                try
                {
                    RMIBarrelInterface barrel = (RMIBarrelInterface) Naming
                            .lookup("rmi://"+Configuration.IP_BARRELS +"/Barrel" + randomBarrel);
                    start = System.currentTimeMillis();
                    resultA_M = barrel.searchWords(wordsA_M);
                    finish = System.currentTimeMillis();
                    adminPage.updateResponseTimesBarrel(randomBarrel,finish-start);
                    connected = true;
                }
                catch (Exception e)
                {
                    // Communicate with Admin Page and notify that this barrel is Offline
                    adminPage.updateOfflineBarrels(randomBarrel);
                    miss_counter += 1;
                    randomBarrel = generateRandomEvenNumber();
                }
            }
        }
        miss_counter = 0;
        if (!wordsN_Z.isEmpty()) {
            int randomBarrel = generateRandomOddNumber();
            boolean connected = false;

            while (!connected)
            {
                if(miss_counter == 5)
                {
                    throw new RemoteException();
                }
                try
                {
                    RMIBarrelInterface barrel = (RMIBarrelInterface) Naming
                            .lookup("rmi://"+Configuration.IP_BARRELS +"/Barrel" + randomBarrel);
                    start = System.currentTimeMillis();
                    resultN_Z = barrel.searchWords(wordsN_Z);
                    finish = System.currentTimeMillis();
                    adminPage.updateResponseTimesBarrel(randomBarrel,finish-start);
                    connected = true;
                }
                catch (Exception e)
                {
                    // Communicate with Admin Page and notify that this barrel is Offline
                    adminPage.updateOfflineBarrels(randomBarrel);
                    miss_counter += 1;
                    randomBarrel = generateRandomOddNumber();
                }
            }
        }
        List<String> result = new ArrayList<String>();
        if (resultA_M.isEmpty()) {
            result = resultN_Z;
            System.out.println(result);
        } else if (resultN_Z.isEmpty()) {
            result = resultA_M;
            System.out.println(result);

        } else {
            // Get the intersection of the two lists
            for (String s : resultA_M) {
                if (resultN_Z.contains(s)) {
                    result.add(s);
                }
            }
        }
        if (this.searchDictionary.containsKey(word)) {
            this.searchDictionary.put(word, this.searchDictionary.get(word) + 1);
        } else {
            this.searchDictionary.put(word, 1);
        }

        sortSearchDictionary();
        return result;

    }

    private void sortSearchDictionary()
    {
        // Sort the search dictionary by the number of times a word has been searched
        List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(
                this.searchDictionary.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        this.searchDictionary = temp;
        adminPage.updateHashMap(temp);
    }

    @Override
    public List<String> searchLinks(String word) throws FileNotFoundException, IOException, NotBoundException
    {
        int randomBarrel = (int) (Math.random() * Configuration.NUM_BARRELS) + 1;
        int miss_counter = 0;
        RMIBarrelInterface barrel = null;
        List<String> result = null;
        boolean connected = false;
        long start,finish;

        while (!connected)
        {
            if(miss_counter == 5)
            {
                throw new RemoteException();
            }

            try
            {
                barrel = (RMIBarrelInterface) Naming.lookup("rmi://"+Configuration.IP_BARRELS +"/Barrel" + randomBarrel);
                start = System.currentTimeMillis();
                result = barrel.searchLinks(word);
                finish = System.currentTimeMillis();
                adminPage.updateResponseTimesBarrel(randomBarrel,finish-start);
                connected = true;
            }
            catch (Exception e)
            {
                // Try again with another barrel
                adminPage.updateOfflineBarrels(randomBarrel);
                miss_counter += 1;
                randomBarrel = generateRandomNumber();
            }
        }

        return result;
    }

    @Override
    public void indexNewURL(String url) throws RemoteException, IOException, NotBoundException
    {
        // Send the url to the urlqueue and the downloader will take care of the rest
        // via tcp
        Socket socket = new Socket("localhost", Configuration.PORT_B);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println(url);
        out.close();
        socket.close();
    }



    public static void main(String[] args) throws IOException, NotBoundException
    {
        RMIGateway gateway = new RMIGateway();
        LocateRegistry.createRegistry(1099);
        Naming.rebind("gateway", gateway);

        for (int i = 1; i <= Configuration.NUM_DOWNLOADERS; i++)
        {
            Downloader d = new Downloader(i);
            try {
                d.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        gateway.adminPage = new AdminPage(gateway.searchDictionary);
        gateway.adminPage.showMenu();
    }

    @Override
    public String getAdminMenu() throws RemoteException {
        return adminPage.getStringMenu();
    }

}