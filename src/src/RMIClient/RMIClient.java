package src.RMIClient;

import src.RMIGateway.Configuration;
import src.RMIInterface.RMIGatewayInterface;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class RMIClient
{
    private boolean login;

    private void run(RMIGatewayInterface gateway) throws Exception {
        printMenu();

        System.out.print("Type in new command: ");
        Scanner scanner = new Scanner(System.in);

        boolean exit = false;

        while (true) {
            int command;
            try {
                command = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("--------------------------------------------------------------------------");
                invalid_prompt();
                scanner.next();
                continue;
            }
            switch (command) {
                case 0:
                    printMenu();
                    regular_messages();
                    break;
                case 1:
                    System.out.println("--------------------------------------------------------------------------");
                    System.out.print("Insert URL to webpage: ");
                    scanner.nextLine();
                    String url = scanner.nextLine();
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        System.err.println("Invalid URL (must have <http://> or <https://> prefixes)");
                        regular_messages();
                        break;
                    }
                    gateway.indexNewURL(url);
                    System.out.println("URL has been indexed to queue");
                    regular_messages();
                    break;
                case 2:
                    System.out.println("--------------------------------------------------------------------------");
                    searchWord(gateway, scanner);
                    break;
                case 3:
                    System.out.println("--------------------------------------------------------------------------");
                    checkLogin(gateway, scanner, 3);
                    regular_messages();
                    break;
                case 4:
                    System.out.println("--------------------------------------------------------------------------");
                    System.out.println(gateway.getAdminMenu());
                    regular_messages();
                    break;
                case 5:
                    exit = true;
                    break;
                default:
                    System.out.println("--------------------------------------------------------------------------");
                    invalid_prompt();
                    break;
            }
            if (exit) {
                break;
            }
        }
        scanner.close();
    }

    private void invalid_prompt()
    {
        System.err.println("Invalid prompt");
        System.out.print("Type in new command: ");
    }
    private void regular_messages()
    {
        System.out.println("Type '0' to display menu again");
        System.out.print("Type in new command: ");
    }
    private boolean checkSearchFormat(String query) {
        String[] words = query.split(" ");
        for (String word : words) {
            if (word.matches("[^a-zA-Z0-9\\p{L};]+") || word.contains(";")) {
                return false;
            }
        }
        return true;
    }

    private void searchWord(RMIGatewayInterface searchModule, Scanner scanner)
            throws RemoteException, MalformedURLException, NotBoundException, FileNotFoundException, IOException {

        System.out.print("Type in terms for search: ");
        scanner.nextLine();
        String words = scanner.nextLine();

        if (!checkSearchFormat(words)) {
            System.err.println("Invalid search format!");
            System.out.println("--------------------------------------------------------------------------");
            regular_messages();
            return;
        }

        int pageNumber = 1;
        List<String> resultList = searchModule.searchWords(words);

        int i = 0;
        while (true)
        {
            System.out.println("--------------------------------------------------------------------------");
            if (resultList.isEmpty() && pageNumber == 1) {
                System.out.println("No results found");
                System.out.println("--------------------------------------------------------------------------");
                regular_messages();
                break;
            } else if (resultList.isEmpty()) {
                System.out.println("No more results found");
                System.out.println("--------------------------------------------------------------------------");
                regular_messages();
                break;
            }

            boolean info = false;
            System.out.println("Search results:");
            for (int j = i; j < resultList.size(); j++) {
                if (j == pageNumber * 10)
                    break;
                String fields[] = resultList.get(j).split(";");
                System.out.println("Link: " + fields[0]);
                System.out.println("Title: " + fields[1]);
                System.out.println("Description: " + fields[2] + "\n");
                info = true;
                i++;
            }

            // If i not multiple of
            if (!info || i % 10 != 0)
            {
                System.out.println("No more results");
                System.out.println("--------------------------------------------------------------------------");
                regular_messages();
                return;
            }

            System.out.println("--------------------------------------------------------------------------");
            System.out.println("Next page? (y/n)");
            String next = scanner.nextLine();
            if (next.equals("y"))
                pageNumber++;
            else {
                System.out.println("--------------------------------------------------------------------------");
                regular_messages();
                return;
            }
        }
    }

    private void checkLogin(RMIGatewayInterface searchModule, Scanner scanner, int command)
            throws FileNotFoundException, IOException, NotBoundException {

        System.out.print("Insert hyperlink to webpage: ");
        scanner.nextLine();
        String url = scanner.nextLine();
        List<String> links = (searchModule.searchLinks(url));

        if (links.isEmpty()) {
            System.out.println("No results found");
        } else {
            System.out.println("Search results:");
            for (String link : links) {
                System.out.println(link);
            }
        }

    }

    public static void main(String[] args) throws Exception
    {
        boolean connected = false;
        RMIGatewayInterface gateway = null;
        while (!connected) {
            try {
                gateway = (RMIGatewayInterface) Naming.lookup("rmi://"+ Configuration.IP_GATEWAY +"/gateway");
                connected = true;
            } catch (Exception e) {
                System.err.println("Error connecting to server, retrying in 3 seconds");
                Thread.sleep(3000);
            }
        }

        RMIClient client = new RMIClient();
        client.login = false;

        try {
            client.run(gateway);
        } catch (Exception e) {
            System.err.println("Error connecting to server, retrying in 3 seconds");
            Thread.sleep(3000);
            main(args);
        }

    }

    private void printMenu() {
        System.out.println("---------------------------------MENU-------------------------------------");
        System.out.println("0 - Refresh menu");
        System.out.println("1 - Index a new URL");
        System.out.println("2 - Search webpages that contain certain terms");
        System.out.println("3 - Consult lists of hyperlinks that are oonnected to a certain webpage");
        System.out.println("4 - Open real-time admin page");
        System.out.println("5 - Exit");
        System.out.println("--------------------------------------------------------------------------");
    }

}