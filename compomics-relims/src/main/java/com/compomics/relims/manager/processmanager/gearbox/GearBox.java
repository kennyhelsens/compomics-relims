package com.compomics.relims.manager.processmanager.gearbox;


import com.compomics.relims.manager.processmanager.gearbox.enums.PriorityLevel;
import com.compomics.relims.manager.processmanager.gearbox.interfaces.ProcessManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;

/**
 * Hello world!
 *
 */
public class GearBox {

    private static ProcessManager manager = MainProcessManager.getPriorityManager();
    private static PriorityLevel priority = PriorityLevel.NORMAL;
    private final static Logger logger = Logger.getLogger(GearBox.class);
    private static GearBox gearbox;
    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    private Thread invokerThread;

    private GearBox() {
    }

    public static GearBox getInstance() {
        if (gearbox == null) {
            gearbox = new GearBox();
            //needed to make sure if another process starts, it's not started in normal mode :)
            Thread updaterThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        manager.setPriority(priority);
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException ex) {
                        }
                    }
                }
            });
            try {
                updaterThread.join();
                updaterThread.start();
            } catch (InterruptedException ex) {
            }
        }
        return gearbox;
    }

    public PriorityLevel getPriorityLevel() {
        return priority;
    }

    public void changeGear(String direction) {
        if (direction.toLowerCase().equals("up")) {
            priority = priority.getNext();
        } else {
            priority = priority.getPrevious();
        }
        manager.setPriority(priority);
    }

    public void addProcess(String processName) {
        manager.addProcess(processName);
    }
}
