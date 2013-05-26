/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.controller.connectivity.database.security;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.modes.networking.controller.connectivity.database.DAO.DAO;
import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class BackupService implements Runnable {

    private static String dbLocation = null;
    private static final Logger logger = Logger.getLogger(BackupService.class);
    private static CallableStatement cs;
    private static ResultSet rs;
    private static String formattedDate;
    private static String backupdirectory;
    private static int maxBackups = 10;
    private static int backupMinutes = 30;
    private static String dbLocationorigin;
    private Connection conn;
    private static BackupService bs;

    private BackupService() {
    }

    public static BackupService getInstance() {
        if (bs == null) {
            bs = new BackupService();
        }
        return BackupService.bs;
    }

    public static void backupDerbyDatabase(Connection conn) throws SQLException {
        try {
            dbLocation = RelimsProperties.getTaskDatabaseLocation().getParent().concat("/databases/");
            Calendar calendar = Calendar.getInstance();
            Date date = calendar.getTime();
            formattedDate = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(date);
            backupdirectory = dbLocation + "/" + formattedDate;
            cs = conn.prepareCall("CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE(?)");
            cs.setString(1, backupdirectory);
            cs.execute();
            cs.close();
            logger.warn("Backed up databases to " + backupdirectory);
            clearBackups();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        } finally {
        }
    }

    public static void backupSQLliteDatabase(Connection conn) throws SQLException {
        logger.info("Attempt to back up taskdatabase to : " + backupdirectory);
        try {
            dbLocation = RelimsProperties.getConfigFolder().getAbsolutePath().replace("conf", "databases");
            Calendar calendar = Calendar.getInstance();
            Date date = calendar.getTime();
            formattedDate = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(date);
            backupdirectory = dbLocation + "/backups/";
            File backupDirectoryFile = new File(backupdirectory);
            File originalDatabase = new File(dbLocation + "/" + RelimsProperties.getTaskDatabaseName() + ".db");
            backupDirectoryFile.mkdirs();
            FileUtils.copyFileToDirectory(originalDatabase, backupDirectoryFile);
            logger.info("Backed up databases to " + backupdirectory);
            clearBackups();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        } finally {
        }
    }

    public static void clearBackups() {
        int currentBackups = getBackupCount();
        if (currentBackups > maxBackups) {
            logger.info("More than " + maxBackups + " backups are in the backupfolder...");
            File oldestBackup = getOldestBackup();
            if (oldestBackup != null) {
                boolean succes = deleteOldestFile(oldestBackup);
                if (succes) {
                    logger.info("Deleted oldest backup in folder : " + oldestBackup.getName());
                } else {
                    logger.error("Could not delete oldest backup...");
                }
            }
        }
    }

    public static int getBackupCount() {
        int count = 0;
        File countingDirectory = new File(dbLocation);
        for (File file : countingDirectory.listFiles()) {
            if (file.isDirectory()) {
                count++;
            }
        }
        return count;
    }

    public static File getOldestBackup() {
        File oldestBackup = null;
        File countingDirectory = new File(dbLocation);
        File[] backupList = countingDirectory.listFiles();
        Arrays.sort(backupList, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
        if (backupList.length != 0) {
            oldestBackup = backupList[backupList.length - 1];
        }
        return oldestBackup;
    }

    public static boolean deleteOldestFile(File directory) {

        // System.out.println("removeDirectory " + directory);

        if (directory == null) {
            logger.error("Backupdirectory cannot be null");
            return false;
        }
        if (!directory.exists()) {
            logger.error("Backupdirectory doesn't exist");
            return true;
        }
        if (!directory.isDirectory()) {
            logger.error("Backupdirectory could not be removed");
            return false;
        }

        String[] list = directory.list();

        // Some JVMs return null for File.list() when the
        // directory is empty.
        if (list != null) {
            for (int i = 0; i < list.length; i++) {
                File entry = new File(directory, list[i]);

                //        System.out.println("\tremoving entry " + entry);

                if (entry.isDirectory()) {
                    if (!deleteOldestFile(entry)) {
                        return false;
                    }
                } else {
                    if (!entry.delete()) {
                        return false;
                    }
                }
            }
        }
        return directory.delete();
    }

    @Override
    public void run() {
        try {
            backupMinutes = RelimsProperties.getBackupInterval();
            maxBackups = RelimsProperties.getMaxBackups();
            backupMinutes = 1440;
        } catch (Exception e) {
            logger.error("Could not read information from properties. Using default settings");
            backupMinutes = 1440;
            maxBackups = 10;
        }

        logger.info("Backing up every " + backupMinutes + " minutes. Max amount of backups = " + maxBackups + ".");
        while (true) {
            try {
                try {
                    conn = DAO.getConnection();
                    if (conn.toString().contains("derby")) {
                        backupDerbyDatabase(conn);
                    } else {
                        backupSQLliteDatabase(conn);
                    }
                } catch (SQLException ex) {
                    logger.error("Failed to backup database !");
                }
                Thread.sleep(backupMinutes * 60 * 1000);
            } catch (Exception ex) {
                logger.error("Could not correctly perform backup of database...!");
            } finally {
                DAO.disconnect(conn, rs, cs);
            }
        }
    }
}
