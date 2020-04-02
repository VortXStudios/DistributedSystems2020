import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

//name may be given by keyboard OR it can be generated by name of class+id
public class Consumer extends Node {

    private Socket requestSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Scanner scn;
    //private List<Info> listOfInfos;
    private static int id = 0;

    public Consumer() {
        super();
        this.scn = new Scanner(System.in);
        //this.listOfInfos= new ArrayList<>();
        id++;
        //this.setName("Consumer"+Integer.toString(id))
    }

    /* dynamically name this node
    public Consumer(String ip, int port) {
        super(ip,port);
        this.scn = new Scanner(System.in);
        //this.listOfInfos= new ArrayList<>();
        id++;
        //this.setName("Consumer"+Integer.toString(id))
    }*/

    public Consumer(String name, String ip,int port) {
        super(name, ip,port);
        this.scn = new Scanner(System.in);
        //this.listOfInfos= new ArrayList<>();
        id++;

    }

    //TODO TAKE AN NAME OF SONG
    @Override
    public void connect(String ip, int port) {
        List<Broker> listOfBrokers = new ArrayList<>();
        try {
            this.requestSocket = new Socket(ip, port);
            // obtaining input and out streams
            this.out = new ObjectOutputStream(requestSocket.getOutputStream());
            this.in = new ObjectInputStream(requestSocket.getInputStream());
            out.writeObject(this.getName());
            out.flush();
            try {
                //HOUSTON MAY HAVE A PROBLEM OR GET THE LIST OF INFO OBJECTS
                listOfBrokers = (List<Broker>) in.readObject();
                //this.listOfInfos = (List<Info>) in.readObject();
                setBrokers(listOfBrokers);
            } catch (ClassNotFoundException classNot) {
                System.err.println("data received in unknown format");
            }
            findCorrespondingBroker();

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            super.disconnect();
        }
    }

    private void findCorrespondingBroker() {
        System.out.println("Give artist name: ");
        //check if input is correct and not /n char
        String artist = scn.nextLine();
        boolean isOK = false;
        Broker cb = null;
        ArtistName artistName = null;
        //Changes if we use Info object
        while (!isOK) {
            while (artist.isEmpty()) {
                System.out.println("Invalid artist name. Try again");
                System.out.println("Give artist name: ");
                artist = scn.nextLine();
            }
            artistName = new ArtistName(artist);
            List<Broker> listOfBrokers = this.getBrokers();
            for (Broker b : listOfBrokers) {
                for (ArtistName a : b.getRelatedArtists()) {
                    if (a.getArtistName().equals(artistName.getArtistName())) {
                        isOK = true;
                        break;
                    }
                }
                if (isOK) {
                    cb = b;
                    break;
                }
            }
            if (!isOK) {
                System.out.println("Artist name is not founded");
                System.out.println("Please give an other artist name: ");
                artist = scn.nextLine();
            }
        }
        register(cb,artistName);
    }

    //Changes if we use Info object
    private void register(Broker broker, ArtistName artistName){
        if(broker!=null && artistName!=null){
            if((broker.getIp().equals(this.getIp())) && (broker.getPort() == this.getPort())){
                /*
                for(ArtistName a : broker.getRelatedArtists()){
                    if (a.getArtistName().equals(artistName.getArtistName())){
                        try {
                            this.out.writeObject("Register");
                            break;
                        } catch (IOException e) {
                            e.printStackTrace();
                            break;
                        }

                    }
                }*/
                try {
                    this.out.writeObject("Register");
                    this.out.flush();
                    //this.out.writeObject(artistName);
                    transaction(broker,artistName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    super.disconnect();
                }
            }
            else{
                //TODO I DON'T IF DISCONNECT WILL WORK->CHECK IT
                super.disconnect();
                this.setPort(broker.getPort());
                this.setIp(broker.getIp());
                //if super.disconnect(); doesn't work
                /*
                try {
                    this.requestSocket.close();
                    this.out.close();
                    this.in.close();

                } catch (IOException e) {
                    System.out.println("Failed to disconnect");
                    e.printStackTrace();
                }*/
                try {
                    this.requestSocket=new Socket(this.getIp(),this.getPort());
                    System.out.println("Success to connected to "+broker.getName());
                    this.out = new ObjectOutputStream(requestSocket.getOutputStream());
                    this.in = new ObjectInputStream(requestSocket.getInputStream());
                    this.out.writeObject("Register");
                    this.out.flush();
                    transaction(broker,artistName);
                } catch (Exception e) {
                    System.out.println("Failed to connect to "+broker.getName());
                    e.printStackTrace();
                }
                finally {
                    super.disconnect();
                }
            }
        }
        else {
            System.out.println("Null broker or artist name");
            super.disconnect();
        }

    }


    private void transaction(Broker broker, ArtistName artistName) {
        //String line = "";
        boolean isExit = false;
        while(true){
            try {
                this.out.writeObject(artistName);
                this.out.flush();
                MusicFile mf = (MusicFile) this.in.readObject();
                //TODO save chunks or merge them
                while (mf!=null){
                    mf = (MusicFile) this.in.readObject();
                    //save chunks or merge them
                }
                System.out.println("Press continue if you want to listen an other songs. Else press exit: ");
                String ans1 = scn.nextLine();
                while (!(ans1.equalsIgnoreCase("continue")) && !(ans1.equalsIgnoreCase("exit"))){
                    System.out.println("Invalid answer. Try again");
                    System.out.println("Press continue if you want to listen an other songs. Else press exit: ");
                    ans1 = scn.nextLine();
                }
                if(ans1.equalsIgnoreCase("exit")) {
                    isExit = true;
                    break;
                }
                System.out.println("Do you want to listen an other song of this artist?(Please answer only with yes or no): ");
                ans1 = scn.nextLine();
                while (!(ans1.equalsIgnoreCase("yes")) && !(ans1.equalsIgnoreCase("no"))){
                    System.out.println("Invalid answer. Try again");
                    System.out.println("Do you want to listen an other song of this artist?(Please answer only with yes or no): ");
                    ans1 = scn.nextLine();
                }
                if(ans1.equalsIgnoreCase("no")){
                    break;
                }
                System.out.println("Give name of song that you want to listen from artist"+artistName.getArtistName()+": ");
                String song = scn.nextLine();
                while (song.isEmpty()) {
                    System.out.println("Invalid song name for this artist. Try again");
                    System.out.println("Give song name: ");
                    song = scn.nextLine();
                }
                //MAY SEND SONG+ARTIST
                this.out.writeObject(artistName);
                this.out.flush();
                this.out.writeObject(song);
                this.out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(isExit){
            super.disconnect();
        }
        else{
            findCorrespondingBroker();
        }
    }

    public void disconnect(Broker broker, ArtistName artistName){}


    public void playData(ArtistName artistName, Value value){}

    //MAIN
    public static void main(String args[]){
        //may auto-generate name and it is not needed to be given by keyboard
        /*args[0]->name
          args[1]->IP
          args[2]->Port
         */
        new Consumer(args[0],args[1],Integer.parseInt(args[2])).connect(args[1],Integer.parseInt(args[2]));
    }
}