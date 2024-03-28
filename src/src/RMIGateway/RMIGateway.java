package src.RMIGateway;

import src.AdminPage;
import src.Barrels.Barrel;
import src.Barrels.BarrelInterface;
import src.Configuration;
import src.Downloader;

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
        String wordsA_M = "";
        for (String x : words) {
            if (x.toLowerCase().charAt(0) < 'm') {
                wordsA_M += x + " ";
            }
        }
        return wordsA_M;
    }

    private String separateWordsAlphabet2(String frase)
    {
        String[] words = frase.split(" ");
        String wordsN_Z = "";
        for (String x : words) {
            if (x.toLowerCase().charAt(0) >= 'm') {
                wordsN_Z += x + " ";
            }
        }
        return wordsN_Z;
    }

    private int generateRandomNumber() {return (int) (Math.random() * Configuration.NUM_BARRELS) + 1;}


    @Override
    public List<String> searchWords(String word)
            throws NotBoundException, FileNotFoundException, IOException {

        // Oo barrels pares contem informação sobre as palavras que começam pelas letras
        // [a-m] e os impares [n-z]
        // É necessário verificar em qual dos dois barrels a palavra se encontra ou se
        // se encontra em ambos

        List<String> result_par = new ArrayList<String>();
        List<String> result_impar = new ArrayList<String>();
        String wordsA_M = "";
        String wordsN_Z = "";

        try {
            wordsA_M = separateWordsAlphabet(word);
            wordsN_Z = separateWordsAlphabet2(word);
        } catch (Exception e) {
            System.out.println("Error separating words alphabetically.");
        }

        if (wordsA_M != "") {
            int randomBarrel = generateRandomNumber();
            boolean connected = false;

            while (!connected) {
                try {
                    BarrelInterface barrel = (BarrelInterface) Naming
                            .lookup("rmi://localhost/Barrel" + randomBarrel);
                    result_par = barrel.searchWords(wordsA_M);
                    connected = true;
                } catch (RemoteException e) {
                    // Barrel is not available, try another one
                    randomBarrel = generateRandomNumber();
                }
            }
        }

        if (wordsN_Z != "") {
            int randomBarrel = generateRandomNumber();
            boolean connected = false;

            while (!connected) {
                try {
                    BarrelInterface barrel = (BarrelInterface) Naming
                            .lookup("rmi://localhost/Barrel" + randomBarrel);
                    result_impar = barrel.searchWords(wordsN_Z);
                    connected = true;
                } catch (RemoteException e) {
                    // Barrel is not available, try another one
                    randomBarrel = generateRandomNumber();
                }
            }
        }

        List<String> result = new ArrayList<String>();

        if (wordsA_M == "") {
            result = result_impar;
        } else if (wordsN_Z == "") {
            result = result_par;
        } else {
            // Get the intersection of the two lists
            for (String s : result_par) {
                if (result_impar.contains(s)) {
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
        BarrelInterface barrel = null;
        List<String> result = null;
        boolean connected = false;

        while (!connected)
        {
            try
            {
                barrel = (BarrelInterface) Naming.lookup("rmi://localhost/Barrel" + randomBarrel);
                result = barrel.searchLinks(word);
                connected = true;
            }
            catch (RemoteException e)
            {
                // Try again with another barrel
                randomBarrel = generateRandomNumber();
            }
        }

        return result;
    }

    @Override
    public void indexNewURL(String url) throws RemoteException, IOException, NotBoundException {
        // Send the url to the urlqueue and the downloader will take care of the rest
        // via tcp
        Socket socket = new Socket("localhost", Configuration.PORT_B);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        out.println(url);

        out.close();
        socket.close();
    }

    @Override
    public boolean login(String username, String password) throws RemoteException {
        return adminPage.login(username, password);
    }

    public static void main(String[] args) throws IOException, NotBoundException
    {
        RMIGateway gateway = new RMIGateway();
        LocateRegistry.createRegistry(1099);
        Naming.rebind("gateway", gateway);

        for (int i = 1; i <= Configuration.NUM_BARRELS; i++)
        {
            Barrel b = new Barrel(i);
            b.start();
        }

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
    public String getStringMenu() throws RemoteException {
        return adminPage.getStringMenu();
    }

}