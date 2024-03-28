package src.RMIGateway;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RMIGatewayInterface extends Remote {
    public List<String> searchWords(String words)
            throws RemoteException, MalformedURLException, NotBoundException, FileNotFoundException, IOException;

    public List<String> searchLinks(String word) throws FileNotFoundException, IOException, NotBoundException;

    public String getStringMenu() throws RemoteException;

    public void indexNewURL(String url) throws RemoteException, IOException, NotBoundException;

    public boolean login(String username, String password) throws RemoteException;

}