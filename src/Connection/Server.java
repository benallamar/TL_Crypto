package Connection;

import Security.Certificat;
import Security.PaireClesRSA;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;

/**
 * Created by marouanebenalla on 07/10/2016.
 */
/*
For server there are four main option:
    1- Connect.
    2- Connection Accepted.
    3- Do you trust the equipement with the given id.
 */
public class Server extends IOOperation {
    protected int port = 12345;


    public Server(int port) {
        try {
            server = new ServerSocket(port);
            maCle = new PaireClesRSA(1024);
            name = "server";

        } catch (IOException e) {
            System.out.println("Error 1" + e.getMessage());
        }
    }

    public void run() {
        try {
            while (true) {
                socket = server.accept();
                SocketBody request = read();
                //We switch from one user to another.
                switch (request.getOption()) {
                    case 1:
                        print("Connection with the component: " + request.getKey("name"));
                        SocketBody response = new SocketBody();
                        acceptConnection(request, response);
                        print("Connection accepter!");
                        write(response);
                        request = read();
                        if (request.isSuccess()) {
                            connect(request, response);
                            write(response);
                        }
                        break;
                    case 2:
                        print("Get the component CA");
                        break;
                    case 3:
                        print("Get the componenet DA");
                        break;
                    case 3:
                        //Here for handling trust.
                        break;
                    default:
                        print("Unknow option");
                }
                close();
            }
        } catch (IOException e) {
            System.out.println("Error 2" + e.getMessage());

        } catch (ClassNotFoundException e) {
            System.out.println("Error 3" + e.getMessage());
        }
    }

    public void connect(SocketBody request, SocketBody response) {
        //we set the option to get the write from the server
        response.setOption(1);

        //Set the response body
        response.setBody(new HashMap<String, Object>());

        //Set the body of the connections
        response.getBody().put("public_key", maCle.pubKey().toString());

        //We want tell the client the operation has been well
        response.setSuccess();

        //Set the response to True
        return true;
    }

    public void acceptConnection(SocketBody request, SocketBody response) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        //Set to the next option of the operation
        response.setOption(1);

        //Instantiate the body of the response
        response.setBody(new HashMap<String, Object>());

        //We have to instantiate the body fo the response
        HashMap<String, Object> key_spec = request.getBody();
        PublicKey pubKey = PaireClesRSA.genertatePublicKey((BigInteger) request.getKey("modulus"), (BigInteger) request.getKey("exponent"));
        Certificat certificat = new Certificat(
                name,
                pubKey,
                maCle.privKey(),
                356
        );
        response.getBody().put("certificate", Certificat.serialize(certificat));
        DA.put(response.getKey("port"), certificat);
        //Set the status for the response
        response.setSuccess();

    }

    public void trust(SocketBody request, SocketBody response) {
        //And we have to see if we trust any of the following


    }

    public static void main(String[] args) {
        Server server = new Server(3000);
        server.run();
    }
}
