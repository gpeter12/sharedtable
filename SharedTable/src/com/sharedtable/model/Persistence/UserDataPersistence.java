package com.sharedtable.model.Persistence;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;


import com.sharedtable.view.MessageBox;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class UserDataPersistence {

    public UserDataPersistence() {
        try {
            getFile();
            readAllUserData();
        } catch (UnrecognizedOSException e) {
            MessageBox.showError("Ismeretlen operációs rendszer!","Felhasználói adatfájl írása nem lehetséges!");
            hasPersistance = false;
            e.printStackTrace();
        } catch (IOException e) {
            MessageBox.showError("Adatfájl I/O hiba","Felhasználói adatfájl írása/olvasása nem lehetséges!");
            hasPersistance = false;
            e.printStackTrace();
        } catch (ParseException e) {
            MessageBox.showError("Adatfájl I/O hiba","Felhasználói adatfájl sértült!");
            hasPersistance = false;
            e.printStackTrace();
            userDataFile.delete();
        }
    }

    private File getFile() throws UnrecognizedOSException, IOException {
        String filePath;
        if(System.getProperty("os.name").contains("Windows")) {
            createDirectory(getDirectoryPathOnWindows());
            filePath = getFilePathOnWindows();
        } else if(System.getProperty("os.name").contains("Linux")){
            createDirectory(getDirectoryPathOnLinux());
            filePath = getFilePathOnLinux();
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

    private String getFilePathOnLinux() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDirectoryPathOnLinux())
                .append("/userconfig.json");
        return sb.toString();
    }

    private String getFilePathOnWindows() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDirectoryPathOnWindows())
                .append("\\userconfig.json");
        return sb.toString();
    }

    private String getDirectoryPathOnLinux() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.getProperty("user.home"))
            .append("/.config/SharedTable");
        return sb.toString();
    }

    private String getDirectoryPathOnWindows() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.getProperty("user.home"))
                .append("\\AppData\\Local\\SharedTable");
        return sb.toString();
    }

    private void createDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()){
            directory.mkdirs();
        }
    }

    private void createFile(String path) throws IOException {
        File file = new File(path);
        if(file.createNewFile())
            System.out.println("file created: "+path);
        else
            System.out.println("file NOT created: "+path);
    }

    public void writeAllUserData() throws IOException {
        if(userDataFile == null)
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

    public void readAllUserData() throws IOException, ParseException {
        if(userDataFile == null || !userDataFile.canWrite()){
            System.out.println("cant read file!");
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

    public boolean hasPersistance() {
        return hasPersistance;
    }

    private String userNickname = "username";
    private UUID userID = generateUserID();
    private boolean everPastedImage = false;

    private boolean requiresInit = false;
    private File userDataFile = null;
    private boolean hasPersistance = true;


}
