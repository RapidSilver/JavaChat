package ru.geekbains.javaCoe1.Jchat.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Vector;

public class Server {
    private Vector<ClientHandler> clients;
    AuthService authService;

    public AuthService getAuthService() {
        return authService;
    }



    public Server() {
        try(ServerSocket serverSocket = new ServerSocket(8189)){
            clients = new Vector<>();
            authService = new AuthService();
            authService.connect();
            System.out.println("ServerMain started...Waiting for clients");
            while (true){
                Socket socket = serverSocket.accept();
                System.out.println("Client connected" + " " + socket.getInetAddress() + " " + socket.getPort() + " " + socket.getLocalPort());
                new ClientHandler(this, socket);
            }
        } catch (IOException e){
            e.printStackTrace();
        } catch (SQLException| ClassNotFoundException e) {
            System.out.println("не удалось запустить сервер авторизации");
        } finally {
            authService.disconnect();
        }
    }

    public  void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
        broadcastClientList();
    }
    public  void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
        broadcastClientList();
    }


    public  void broadCastMesg(String msg) {
        if (msg.startsWith("/w ")) {

        }
        for (ClientHandler o: clients) {
            o.sendMesg(msg);
            
        }
    }

    public  boolean isNickBusy (String nick) {
        for (ClientHandler o:
             clients) {
            if (o.getNick().equals(nick)) {
                return  true;
            }
        } return false;
    }

    public void privatMsg (ClientHandler from, String nickTo, String msg) {
       for (ClientHandler o: clients) {
            if (o.getNick().equals(nickTo)) {
                o.sendMesg("от" + from.getNick() + " : "+ msg);
                from.sendMesg("личное" + nickTo + " : " + msg);
                return;
            }
        }
        from.sendMesg("Клиент с ником " + nickTo + " не найден");
    }

    public void broadcastClientList(){
        StringBuilder sb = new StringBuilder("/clientlist ");
        for (ClientHandler o:
             clients) {
            sb.append(o.getNick() + " ");
        }
        String out = sb.toString();
        for (ClientHandler o:
             clients) {
            o.sendMesg(out);

        }
    }
}

