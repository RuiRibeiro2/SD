package Gateway;

import Barrel.IndexStorageBarrelInterface;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Random;



// check if barrel1 is available
// else check for barrel2,barrel3



public class RMIGateway extends UnicastRemoteObject implements RMIGatewayInterface
{
    protected IndexStorageBarrelInterface isbi = null;
    public RMIGateway() throws RemoteException
    {
        super();
        Registry registry = LocateRegistry.getRegistry("localhost", 1100);
        boolean barrelConnection = false;
        int num_tries = 4;
        while (num_tries > 0) {
            try {
                System.out.println("trying");
                this.isbi = (IndexStorageBarrelInterface) registry.lookup("barrel");
                System.out.println("connected");
                barrelConnection = true;
                break;
            } catch (Exception e) {
                System.out.printf("RMI connection failed. %d tries remaining...\n",num_tries);
                try {
                    num_tries--;
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
    public static int getBarrelId()
    {
        Random rand = new Random();
        return rand.nextInt(2);
    }
    public static void main(String[] args) throws RemoteException
    {
        //RMI connection to barrel
        //int rand = getBarrelId();
        //var id = Integer.toString(rand);
        RMIGatewayInterface gi = new RMIGateway();
        LocateRegistry.createRegistry(1099).rebind("gateway", gi);
        System.out.println("Registered");

    }

    @Override
    public List<String> searchWord(String word) throws IOException
    {
        try
        {
            List<String> urls = isbi.searchWord(word);
            return urls;
        }
        catch (RemoteException e)
        {
            System.out.println("Error retrieving from barrel!");
        }
        return null;
    }

    @Override
    public List<String> searchPage(String link)
    {
        return null;
    }
}
