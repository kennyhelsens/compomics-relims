/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.controller.connectivity.handlers;

/**
 *
 * @author Kenneth
 */
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.modes.networking.controller.connectivity.database.DAO.TaskDAO;
import com.compomics.relims.modes.networking.controller.connectivity.database.service.DatabaseService;
import com.compomics.relims.modes.networking.controller.taskobjects.TaskContainer;
import com.compomics.relims.modes.networking.controller.workerpool.WorkerRunner;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import org.apache.log4j.Logger;

public class ClientHandler implements Runnable {

    private final static Logger logger = Logger.getLogger(ClientHandler.class);
    private Socket sock = null;
    private ObjectInputStream sockInput = null;
    private ObjectOutputStream sockOutput = null;
    private Thread myThread = null;
    private WorkerRunner worker;
    private boolean registered = false;
    private static final DatabaseService databaseService = DatabaseService.getInstance();
    private ServerSocket controllerSocket;//Public access setup
    private Socket socketConnection; //socket = a connection, rename later
    private int transferPort = RelimsProperties.getControllerPort();
    private int maxWaitingClients = RelimsProperties.getTaskDatabaseMaxClients();
    private String clientID = null;
    private String password = null;
    private String email = null;
    private String queryParameters = null;
    private Object taskInstructionMap;

    public ClientHandler(Socket sock, ObjectOutputStream sockOutput, ObjectInputStream sockInput) throws IOException {
        this.sock = sock;
        this.sockOutput = sockOutput;
        this.sockInput = sockInput;
        this.myThread = new Thread(this);
    }

    @Override
    public void run() {
        boolean handling = true;
        while (handling) {

            boolean allow = false;
            try {
                //verify client to check if it's a valid combination of credentials
                TaskContainer taskObject;
                try {
                    taskObject = (TaskContainer) sockInput.readObject();

                    clientID = taskObject.getName();
            
                    if (taskObject.isValid()) {
                        logger.debug(clientID + " provided a valid input...");
                        Map<String, Long> generatedTaskIDs //= databaseService.pushTaskMapToDB(taskObject);
                        = new TaskDAO().pushTaskMapToDB(taskObject);
                        sockOutput.writeObject(generatedTaskIDs);
                        sockOutput.flush();
                    } else {
                        sockOutput.writeBoolean(allow);
                        sockOutput.flush();
                    }

                } catch (ClassNotFoundException CNFExc) {
                    logger.error(clientID + " provided an invalid input...");
                    logger.error(CNFExc);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (allow) {
                    sockOutput.writeBoolean(allow);
                    sockOutput.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                try {
                    sock.close();
                } catch (IOException ex) {
                    sock = null;
                }
                try {
                    sockOutput.close();
                } catch (IOException ex) {
                    sockOutput = null;
                }
                handling = false;
            }
        }
    }
}
