package src.Barrels;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StatusThread extends Thread
{
    private RMIBarrel barrel;
    public StatusThread(RMIBarrel barrel)
    {
        this.barrel = barrel;
    }
    public void run()
    {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                barrel.sendStatus("Alive");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 0, 10, TimeUnit.SECONDS); // Executes every 10 seconds
    }




}
