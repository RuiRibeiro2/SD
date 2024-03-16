package Gateway;

import Barrel.IndexStorageBarrelInterface;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Random;



// check if barrel1 is available
// else check for barrel2,barrel3

public class RMIGateway
{
    public static int getBarrelId()
    {
        Random rand = new Random();
        return rand.nextInt(2);
    }
    public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException
    {
        //RMI connection to barrel
        int rand = getBarrelId();
        var id = Integer.toString(rand);
        IndexStorageBarrelInterface isbi = (IndexStorageBarrelInterface) Naming.lookup("rmi://localhost/barrel"+id);
        //isbi.searchWord("universidade");
        //isbi.searchPage("http://www.uc.pt");
    }



}
