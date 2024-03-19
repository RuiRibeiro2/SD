import Barrel.IndexStorageBarrelInterface;
import Gateway.RMIGatewayInterface;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIClient
{
    public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException
    {
        int num_tries = 4;
        boolean gatewayConnection = false;

        RMIGatewayInterface gi = null;
        while (num_tries > 0) {
            try {
                System.out.println("trying");
                gi = (RMIGatewayInterface) Naming.lookup("rmi://localhost/gateway");
                System.out.println("connected");
                for(String url: gi.searchWord("mooshak"))
                {
                    System.out.println(url);
                }
                gatewayConnection = true;
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

        //RMI connection to barrel
    }
}
