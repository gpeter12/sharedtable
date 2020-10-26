package com.sharedtable.model.persistence;

import com.sharedtable.Constants;
import com.sharedtable.controller.MainViewController;
import com.sharedtable.view.MessageBox;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

public class UserDataPersistence {

    private String userNickname = "username";
    private UUID userID = generateUserID();
    private boolean everPastedImage = false;

    private boolean requiresInit = false;
    private File userDataFile = null;
    private boolean hasPersistence = true;

    private Logger logger = null;

    private static UserDataPersistence instance = new UserDataPersistence();

    private UserDataPersistence() {
        logger = Logger.getLogger(MainViewController.class.getName());
        try {
            getFile();
            readAllUserData();
        } catch (UnrecognizedOSException e) {
            logger.severe("unknown operating system!");
            MessageBox.showError("Ismeretlen operációs rendszer!","Felhasználói adatfájl írása nem lehetséges!");
            hasPersistence = false;
            e.printStackTrace();
        } catch (IOException e) {
            logger.severe(e.getMessage());
            MessageBox.showError("Adatfájl I/O hiba","Felhasználói adatfájl írása/olvasása nem lehetséges!");
            hasPersistence = false;
            e.printStackTrace();
        } catch (ParseException e) {
            logger.severe(e.getMessage());
            MessageBox.showError("Adatfájl I/O hiba","Felhasználói adatfájl sérült!");
            hasPersistence = false;
            e.printStackTrace();
            userDataFile.delete();
        }
    }

    public static UserDataPersistence getInstance() {
        if(instance == null) {
            instance = new UserDataPersistence();
        }
        return instance;
    }

    private File getFile() throws UnrecognizedOSException, IOException {
        String filePath;
        if(Constants.isPlatformWindows()) {
            FilePathHandler.getInstance().createDirectory(FilePathHandler.getInstance().getDirectoryPathOnWindows());
            filePath = FilePathHandler.getInstance().getFilePathOnWindows();
        } else if(Constants.isPlatformLinux()){
            FilePathHandler.getInstance().createDirectory(FilePathHandler.getInstance().getDirectoryPathOnLinux());
            filePath = FilePathHandler.getInstance().getFilePathOnLinux();
        } else {
            throw new UnrecognizedOSException();
        }


        File userDataFile = new File(filePath);
        if(! userDataFile.exists()){
            createFile(filePath);
            requiresInit = true;
            this.userDataFile = userDataFile;
            writeAllUserData();
        }
        this.userDataFile = userDataFile;
        return userDataFile;
    }



    private void createFile(String path) throws IOException {
        File file = new File(path);
        if(file.createNewFile())
            logger.info("file created: "+path);
        else
            logger.info("file NOT created becaouse of its existance: "+path);
    }

    public void writeAllUserData() throws IOException {
        if(userDataFile == null ||!hasPersistence())
            return;
        JSONObject obj = new JSONObject();
        obj.put("nickname",userNickname);
        obj.put("UserID", userID.toString());
        obj.put("everPastedImage",everPastedImage);

        String path = userDataFile.getPath();
        userDataFile.delete();
        createFile(path);

        FileWriter fileWriter = new FileWriter(userDataFile);
        fileWriter.write(obj.toJSONString());
        fileWriter.flush();
        fileWriter.close();
    }

    private void readAllUserData() throws IOException, ParseException {
        if(userDataFile == null || !userDataFile.canWrite() ||!hasPersistence()){
            logger.severe("cant read file!");
            return;
        }
        JSONParser parser = new JSONParser();
        FileReader fileReader = new FileReader(userDataFile);
        Object obj = parser.parse(fileReader);
        JSONObject jsonObject = (JSONObject) obj;
        userNickname = jsonObject.get("nickname").toString();
        userID = UUID.fromString(jsonObject.get("UserID").toString());
        everPastedImage = Boolean.parseBoolean(jsonObject.get("everPastedImage").toString());
        fileReader.close();
    }

    private static UUID generateUserID() {
        UUID uuid = UUID.randomUUID();
        return uuid;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public UUID getUserID() {
        return userID;
    }

    public void setUserID(UUID userID) {
        this.userID = userID;
    }

    public boolean isEverPastedImage() {
        return everPastedImage;
    }

    public void setEverPastedImage(boolean everPastedImage) {
        this.everPastedImage = everPastedImage;
    }

    public boolean isRequiresInit() {
        return requiresInit;
    }

    public boolean hasPersistence() {
        return hasPersistence;
    }





}
