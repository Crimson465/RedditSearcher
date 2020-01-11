
package redditsearcher;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FXMLDocumentController implements Initializable {
    @FXML
    TextField field;
    
    @FXML
    Button search;
    
    @FXML 
    ChoiceBox<String> nameType;
    
    @FXML ListView<String> savedSubreddits;
    
    @FXML ListView<String> savedUsers;
    
    @FXML
    private void handleButtonActionSearch(ActionEvent event) {
        search(makeURL(field.getText()));
    }
    
    @FXML
    private void handleButtonActionSave(ActionEvent event) {
        save();
    }
    
    @FXML
    private void handleButtonActionDelete(ActionEvent event) {
        deleteSave(field.getText());
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        nameType.getItems().addAll("Subreddit", "User");
        nameType.setValue("Subreddit");
        
        makeNameList(savedSubreddits, "Subreddit");
        Collections.sort(savedSubreddits.getItems());
        makeNameList(savedUsers, "User");
        Collections.sort(savedUsers.getItems());
        
        savedSubreddits.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                field.setText(newValue);
                nameType.setValue("Subreddit");
            }
        });
        
        savedUsers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                field.setText(newValue);
                nameType.setValue("User");
            }
        });
    }
    
    public String makeURL(String name){
        String subredditURL = "https://www.reddit.com/r/";
        String userURL = "https://www.reddit.com/user/";
        
        
        if(nameType.getValue().equals("Subreddit")){
            subredditURL += (name + "/");
            //System.out.println("Searching for " + subredditURL + " ...");
            return subredditURL;
        }else if(nameType.getValue().equals("User")){
            userURL += (name + "/");
            //System.out.println("Searching for " + userURL + " ...");
            return userURL;
        }
        
        return "";
    }
    
    public void search(String url){
        try{
            Desktop desktop = Desktop.getDesktop();
            URI oURL = new URI(url);
            desktop.browse(oURL);
        }catch(Exception e){
            System.out.println("couldn't find subreddit/user");
            displayErrorMessage("URL Error Message", "couldn't find subreddit/user");
            return;
        }
        field.setText("");
        field.requestFocus();
        System.out.println("searching for: " + url + "...");
    }
    
    public void displayErrorMessage(String title, String message){
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        window.setMinWidth(250);
        
        Label label = new Label();
        label.setText(message);
        
        Button closeButton = new Button("Close the Window");
        
        closeButton.setOnAction(e -> window.close());
        
        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, closeButton);
        layout.setAlignment(Pos.CENTER);
        
        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();
    }
    
    public void save(){
        String fileName = nameType.getValue() + ".txt";
        
        File file = new File(fileName);
        try{
            if(!file.exists()){
                System.out.println("Creating New File");
                file.createNewFile();
            }
            
            PrintWriter writer = new PrintWriter(new FileWriter(file, true));
            writer.append(field.getText() + "\n");
            writer.close();
            System.out.println("Printing Successful");
        }catch(IOException e){
            System.out.println("Printing Failed");
            e.printStackTrace();
        }
        
        System.out.println("Save Method finished");
    }
    
    public void makeNameList(ListView<String> list, String type){
        String fileName = type + ".txt";
        BufferedReader reader = null;
        
        try{
            reader = new BufferedReader(new FileReader(fileName));
            ArrayList<String> temp = new ArrayList<>();
            String line;
            
            while((line = reader.readLine()) != null){
                
                if(!temp.contains(line) && !line.equals("")){
                    temp.add(line);
                    System.out.println("Name: " + line);
                    list.getItems().add(line);
                }
            }
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("Could Not Read File");
        }finally{
            try {
                reader.close();
            } catch (IOException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private Scanner x;
    
    public void deleteSave(String name){
        System.out.println("Deleting " + name);
        
        String filePath = nameType.getValue() + ".txt";
        String removeTerm = field.getText();
        
        removeRecord(filePath, removeTerm);
    }
    
    public void removeRecord(String filePath, String term){
        String tempFile = "temp.txt";
        File oldFile = new File(filePath);
        File newFile = new File(tempFile);
        String value = "";
        
        try{
            FileWriter fWriter = new FileWriter(tempFile, true);
            BufferedWriter bWriter = new BufferedWriter(fWriter);
            PrintWriter pWriter = new PrintWriter(bWriter);
            x = new Scanner(new File(filePath));
            x.useDelimiter("[\n]");
            
            while(x.hasNextLine()){
                value = x.nextLine();
                
                if(!value.equals(term) && !value.equals("")){
                    pWriter.println(value);
                }
            }
            
            x.close();
            pWriter.flush();
            pWriter.close();
            oldFile.delete();
            File fileName = new File(filePath);
            newFile.renameTo(fileName);
        }catch(IOException e){
            displayErrorMessage("Delete Name Error", "Could Not Delete Name");
        }
    }
}
