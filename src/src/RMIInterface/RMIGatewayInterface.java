package src.RMIInterface;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RMIGatewayInterface extends Remote
{
    List<String> searchWords(String words) throws NotBoundException, IOException;
    List<String> searchLinks(String word) throws IOException, NotBoundException;
    String getAdminMenu() throws RemoteException;
    void indexNewURL(String url) throws IOException, NotBoundException;


}