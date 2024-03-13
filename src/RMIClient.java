import Barrel.IndexStorageBarrelInterface;
import Gateway.RMIGatewayInterface;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class RMIClient
{
    public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException
    {
        //RMI connection to barrel
        RMIGatewayInterface gi = (RMIGatewayInterface) Naming.lookup("rmi://localhost/gateway");
        //gi.searchWord("universidade");
        //gi.searchPage("http://www.uc.pt");
    }
}
