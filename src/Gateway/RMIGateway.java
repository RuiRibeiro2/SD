package Gateway;

import Barrel.IndexStorageBarrelInterface;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class RMIGateway
{
    public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException
    {
        //RMI connection to barrel
        IndexStorageBarrelInterface isbi = (IndexStorageBarrelInterface) Naming.lookup("rmi://localhost/barrel");
        //isbi.searchWord("universidade");
        //isbi.searchPage("http://www.uc.pt");
    }


}
