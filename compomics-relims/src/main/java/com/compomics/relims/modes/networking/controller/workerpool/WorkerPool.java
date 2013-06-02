/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.controller.workerpool;

import com.compomics.relims.manager.progressmanager.Checkpoint;
import com.compomics.relims.manager.usernotificationmanager.MailEngine;
import com.compomics.relims.modes.networking.controller.connectivity.database.service.DatabaseService;
import com.compomics.relims.modes.networking.controller.taskobjects.Task;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class WorkerPool {
    
    private static Logger logger;
    private static Map<Checkpoint, HashSet<WorkerRunner>> workerMap;
    private static ConcurrentHashMap<WorkerRunner, Boolean> totalServerPool;
    public static boolean databaseLocked;
    private static DatabaseService dds;
    private static WorkerPool workerpool;
    private static Checkpoint[] checkpoints;
    
    private WorkerPool() {
        initializeWorkerPool();
    }
    
    public static WorkerPool getInstance() {
        if (workerpool == null) {
            workerpool = new WorkerPool();
        }
        return workerpool;
    }
    
    public static void initializeWorkerPool() {
        try {
            checkpoints = new Checkpoint[]{Checkpoint.IDLE, Checkpoint.REGISTER, Checkpoint.CANCELLED, Checkpoint.FAILED, Checkpoint.FINISHED, Checkpoint.RUNNING};
            logger = Logger.getLogger(WorkerPool.class);
            //workerMap = Collections.synchronizedMap(new EnumMap<Checkpoint, HashSet<WorkerRunner>>(Checkpoint.class));
            workerMap = new EnumMap<Checkpoint, HashSet<WorkerRunner>>(Checkpoint.class);
            totalServerPool = new ConcurrentHashMap<>();
            databaseLocked = false;
            dds = DatabaseService.getInstance();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        
        for (Checkpoint aState : checkpoints) {
            HashSet<WorkerRunner> workerList = new HashSet<>();
            workerMap.put(aState, workerList);
            logger.debug("Made workerlist for checkpoint : " + aState.toString());
        }
        logger.debug("The workerpool was succesfully initialized.");
        Thread distributor = new Thread(new TaskDistributor());
        distributor.start();
        Thread workerManager = new Thread(new WorkerManager());
        workerManager.start();
    }
    
    public synchronized static WorkerRunner getWorker() {
//should return null if there is no potentialWorker !
        WorkerRunner selectedWorker = null;
        //List IDLE workers 
        HashSet<WorkerRunner> workerList = workerMap.get(Checkpoint.IDLE);
        //shuffle the idle workers so that they all get a potentialTask rather than one clogging the set
        if (!workerList.isEmpty()) {
            int size = workerList.size();
            int item = new Random().nextInt(size);
            int i = 0;
            for (WorkerRunner obj : workerList) {
                if (i == item) {
                    selectedWorker = obj;
                }
                i = i + 1;
            }
        }
        return selectedWorker;
    }
    
    public synchronized static boolean isRegistered(WorkerRunner worker) {
        //intermediate solution...
        boolean isRegistered = false;
        
        for (WorkerRunner aConnector : totalServerPool.keySet()) {
            if (aConnector != null) {
                if (aConnector.equals(worker)) {
                    return true;
                } else {
                    isRegistered = false;
                }
            }
        }
        return isRegistered;
    }
    
    public synchronized static void register(WorkerRunner worker) {
        workerMap.get(Checkpoint.REGISTER).add(worker);
        totalServerPool.put(worker, true);
        setWorkerState(worker, Checkpoint.IDLE);
    }
    
    public synchronized static void deRegister(WorkerRunner worker) {
        try {
            totalServerPool.remove(worker);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            System.out.println(sdf.format(new java.util.Date().getTime()) + " : " + worker.getHost() + "( " + worker.getPort() + " )could not send a heartbeat within the timespan and will be deregistered");
            //System.out.println("Deleting faulty potentialWorker : " + potentialWorker.getHost() + " ( " + potentialWorker.getPort() + " )");
            for (Checkpoint aState : checkpoints) {
                try {
                    if (!workerMap.get(aState).isEmpty()) {
                        workerMap.get(aState).remove(worker);
                        // remove the tasks the potentialWorker was running and reset them...
                        try {
                            long taskID = dds.getTaskID(worker.getHost(), worker.getPort());
                            if (taskID != 0L) {
                                dds.updateTask(taskID, Checkpoint.FAILED.toString());
                            }
                            dds.deleteWorker(worker.getHost(), worker.getPort());
                            
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Could not reset the task...");
                        }
                    }
                } catch (NullPointerException NPE) {
                }
            }
        } catch (NullPointerException NPE) {
            NPE.printStackTrace();
            logger.error("Worker was already removed from the WorkerPool...");
        }
    }
    
    public synchronized static void setWorkerState(WorkerRunner worker, Checkpoint state) {
        //ignore requests for inexistent lists...
        boolean contains = false;
        for (Checkpoint aState : checkpoints) {
            if (aState.equals(state)) {
                contains = true;
                break;
            }
        }
        if (contains) {
            //make sure the runner can't be used twice               
            for (Checkpoint aState : checkpoints) {
                workerMap.get(aState).remove(worker);
            }
            workerMap.get(state).add(worker);
            logger.debug(worker.getHost() + " : " + worker.getPort() + " was updated to be " + state.toString());
        } else {
            logger.error("An invalid request was made to change workerstate. Deregistering worker...");
            WorkerPool.deRegister(worker);
        }
    }
    
    public synchronized static ConcurrentHashMap<WorkerRunner, Boolean> getTotalServerPool() {
        return totalServerPool;
    }
    
    public synchronized static void setOffline() {
        for (WorkerRunner aWorker : totalServerPool.keySet()) {
            totalServerPool.put(aWorker, false);
        }
    }
    
    public synchronized static void setOnline(WorkerRunner aWorker) {
        totalServerPool.put(aWorker, true);
    }
    
    public synchronized static void deregisterOffline() {
        
        try {
            for (WorkerRunner aWorker : totalServerPool.keySet()) {
                if (totalServerPool.get(aWorker) == false) {
                    WorkerPool.setWorkerToDelete(aWorker);
                    Thread deregisteringThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            deRegister(workerToDelete);
                        }
                    });
                    deregisteringThread.join();
                    deregisteringThread.start();
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    static Map<Checkpoint, HashSet<WorkerRunner>> getWorkerMap() {
        return WorkerPool.workerMap;
    }
    
    private static void setWorkerToDelete(WorkerRunner aWorker) {
        WorkerPool.workerToDelete = aWorker;
    }
    
    private static WorkerRunner getWorkerToDelete() {
        return WorkerPool.workerToDelete;
    }
    private static WorkerRunner workerToDelete;
    
    public void updateFromDb() {
        logger.info("Looking for workers that are still active");
        List<WorkerRunner> activeWorkers = dds.getActiveWorkers();
        for (WorkerRunner aRunner : activeWorkers) {
            workerMap.get(Checkpoint.RUNNING).add(aRunner);
            logger.info(aRunner.getHost() + " was still running and was readded to the pool");
        }
    }
    
    private static class TaskDistributor implements Runnable {
        
        private static final Logger innerlogger = Logger.getLogger(TaskDistributor.class);
        static DatabaseService dds = DatabaseService.getInstance();
        static Task newTask = null;
        static WorkerRunner worker = null;
        
        private TaskDistributor() {
        }
        
        private synchronized static Task checkForTasks() {
            try {
                newTask = dds.findNewTask();
            } catch (SQLException | NullPointerException ex) {
                innerlogger.error(ex);
            }
            return newTask;
        }
        
        private synchronized static WorkerRunner checkForIDLEWorker() {
            worker = WorkerPool.getWorker();
            return worker;
        }
        
        private synchronized static void sendTaskToWorker() {
            try {
                String hostname = worker.getHost();
                int port = worker.getPort();
                Socket sock = new Socket(worker.getHost(), worker.getPort());
                ObjectOutputStream sockOut = new ObjectOutputStream(sock.getOutputStream());
                sockOut.writeObject(newTask);
                sockOut.flush();
                System.out.println("Task " + newTask.getTaskID() + " (project : " + newTask.getProjectID() + " ) was sent to " + worker.getHost() + " : " + worker.getPort());
                WorkerPool.setWorkerState(worker, Checkpoint.RUNNING);
                dds.updateTask(newTask.getTaskID(), Checkpoint.RUNNING.toString());
                dds.createWorker(hostname, port, newTask.getTaskID());
            } catch (UnknownHostException ex) {
                ex.printStackTrace();
                innerlogger.error("An error has occurred...");
                System.out.println(ex);
            } catch (IOException ex) {
                ex.printStackTrace();
                innerlogger.error("An error has occurred...");
                System.out.println(ex);
            }
        }
        
        @Override
        public void run() {
            logger.debug("Taskdistributor was started...");
            Task potentialTask;
            WorkerRunner potentialWorker;
            int tasksSinceStartup = 0;
            while (true) {
                potentialTask = checkForTasks();
                potentialWorker = checkForIDLEWorker();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    logger.error(ex);
                }
                if (potentialTask != null && potentialWorker != null) {
                    sendTaskToWorker();
                    tasksSinceStartup++;
                    if (tasksSinceStartup % 100 == 0 || tasksSinceStartup == 1) {
                        try {
                            StringBuilder onlineWorkers = new StringBuilder();
                            int workers = 0;
                            for (Checkpoint checkpoint : workerMap.keySet()) {
                                onlineWorkers.append(System.lineSeparator()).append(checkpoint.toString());
                                for (WorkerRunner runner : workerMap.get(checkpoint)) {
                                    onlineWorkers.append(System.lineSeparator()).append(runner.getHost()).append(":").append(runner.getPort());
                                    workers++;
                                }
                            }
                            MailEngine.sendMail("Controller has started " + tasksSinceStartup + " task(s) on " + workers + " machines", onlineWorkers.toString(), null);
                        } catch (Exception ex) {
                            logger.error(ex);
                        }
                    }
                }
            }
        }
    }
    
    private static class WorkerManager implements Runnable {
        
        private WorkerManager() {
        }
        
        private synchronized void cleanUpFailedWorkers() {
            int i = 0;
            for (WorkerRunner worker : workerMap.get(Checkpoint.FAILED)) {
                WorkerPool.deRegister(worker);
                i++;
            }
            if (i != 0) {
                logger.debug("Deregistered " + i + " failed worker(s).");
            }
        }
        
        private synchronized void cleanUpFailedTasks() {
            int restoredTasks = dds.restoreTasks();
            if (restoredTasks != 0) {
                logger.debug("Restored " + restoredTasks + " tasks.");
            }
        }
        
        @Override
        public void run() {
            logger.debug("WorkerManager was started...");
            
            while (true) {
                try {
                    Thread.sleep(120000);
                } catch (InterruptedException ex) {
                    logger.error(ex);
                }
                cleanUpFailedWorkers();
                cleanUpFailedTasks();
            }
        }
    }
}