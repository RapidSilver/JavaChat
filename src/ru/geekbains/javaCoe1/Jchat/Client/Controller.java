package ru.geekbains.javaCoe1.Jchat.Client;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    TextArea textArea;

    @FXML
    TextField msgField;

    @FXML
    HBox msgPanel;

    @FXML
    HBox authPanel;

    @FXML
    TextField loginField;

    @FXML
    PasswordField passField;

    @FXML
    ListView<String> clientsListView;

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    final String SERVER_IP = "localhost";
    final int SERVER_PORT = 8189;

    private  boolean authhoorized;
    private  String myNick;

    private ObservableList<String> clientList;

    public void setAuthhoorized(boolean authhoorized) {
        this.authhoorized = authhoorized;
        if(authhoorized) {
            msgPanel.setVisible(true);
            msgPanel.setManaged(true);
            authPanel.setVisible(false);
            authPanel.setManaged(false);
            clientsListView.setVisible(true);
            clientsListView.setManaged(true);
        } else {
            msgPanel.setVisible(false);
            msgPanel.setManaged(false);
            authPanel.setVisible(true);
            authPanel.setManaged(true);
            clientsListView.setVisible(false);
            clientsListView.setManaged(false);
            myNick = " ";
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthhoorized(false);

    }

    public void connect () {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            clientList = FXCollections.observableArrayList();
            clientsListView.setItems(clientList);
            clientsListView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
                @Override
                public ListCell<String> call(ListView<String> param) {
                    return new ListCell<String>(){
                        @Override
                        protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            if (!empty) {
                                setText(item);
                                if (item.equals(myNick)){
                                    setStyle("-fx-font-weight: bold; -fx-background-color: #ffff00");
                                }
                            }
                        }
                    };
                }
            });
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String s  = in.readUTF();
                            if (s.startsWith("/authok ")){
                                setAuthhoorized(true);
                                myNick = s.split("\\s")[1];
                                break;
                            }
                            textArea.appendText(s + "\n");
                        }

                        while (true) {
                            String s  = in.readUTF();
                            if (s.startsWith("/")) {
                                if (s.startsWith("/clientlist")){
                                    String[] data = s.split("\\s");
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            clientList.clear();
                                            for (int i = 1; i < data.length; i++) {
                                                clientList.addAll(data[i]);
                                            }
                                        }
                                    });
                                }
                            }
                            textArea.appendText(s + "\n");
                        }

                    } catch (IOException e) {
                       showAlert("Сервер перестал отвечать.");
                    } finally {
                        setAuthhoorized(false);
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            t.setDaemon(true);
            t.start();

        } catch (IOException e) {
           showAlert("Не удалось подключиться к серверу, проверьте сетевое соединение.");
        }
    }

    public  void  sendAuthMsg(){
        if (loginField.getText().isEmpty()||passField.getText().isEmpty()){
            showAlert("Указаны неверные данные авторизации");
            return;
        }
        if (socket == null||socket.isClosed()) {
            connect();
        }
        try {
            out.writeUTF("/auth " +loginField.getText() + " " + passField.getText());
            loginField.clear();
            passField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void sendMsg() {
        try {
            out.writeUTF(msgField.getText());
            msgField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public  void showAlert (String msg) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Что-то пошло не так...");
                alert.setHeaderText(null);
                alert.setContentText(msg);
                alert.showAndWait();

            }
        });
    }
}


