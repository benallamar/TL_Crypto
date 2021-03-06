package connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.operator.OperatorCreationException;
import security.Certificat;
import security.PaireClesRSA;

/**
 * Project Name : TL_crypto
 */
public abstract class IOOperation extends Thread {

  protected ServerSocket server = null;
  protected PaireClesRSA maCle = null;
  protected Certificat monCert = null;
  protected String name;
  protected HashMap<Integer, Object[]> CA = new HashMap<Integer, Object[]>();
  protected HashMap<Integer, PublicKey> sessions = new HashMap<Integer, PublicKey>();
  protected int port;
  protected LinkedList<String> errors = new LinkedList<String>();
  boolean mode_server = true;

  // @over
  public void write(SocketHandler s, boolean encrypt) throws IOException, ClassNotFoundException {
    s.write(maCle, encrypt);
  }

  public void read(SocketHandler s, boolean decrypt) throws IOException, ClassNotFoundException {
    s.read(maCle, decrypt);
  }

  public void print(String string) {
    System.out.println(string);
  }

  public void close(SocketHandler s) throws IOException {
    s.close();
  }

  public String genSecCode(int length) {
    return UUID.randomUUID().toString().substring(0, length);
  }

  public void openSession(SocketHandler s) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
    //Deserialize the key
    PublicKey pub_client = s.request.getPubKey();
    //Open the session by adding the information to the sessions base
    sessions.put(s.getFromPort(), pub_client);
  }

  public void closeSession(SocketBody request) {
    //Remove the key of the sessions
    sessions.remove(request.getFromPort());
  }

  public PublicKey getSession(SocketHandler s) {
    return sessions.get(s.getFromPort());
  }

  public void unauthorized(SocketHandler s) throws IOException, ClassNotFoundException {
    //set the option that you have and error and close the connection
    s.response.setNewBody();
    s.response.setFailed();
    //Send the response and close the socket after

    write(s, false);
  }

  public void acceptConnection(SocketHandler s)
      throws OperatorCreationException, IOException, ClassNotFoundException, CertException {
    //we set the option to get the write from the server
    s.setOption(1);

    //Set the response body
    s.setNewBody();

    //Set the body of the connections

    //We update the CA file.
    PublicKey pubKey = getSession(s);
    Certificat cert = s.getCertificat();
    //System.out.print(cert);
    Object[] trusted_certificat = {pubKey, cert};
    if (cert.verifiCerif(pubKey)) {
      //print("Get certificat from " + s.getFromPort() + " Certificat:" + cert.x509.toString());
      CA.put(s.getFromPort(), trusted_certificat);
      s.setSuccess();
    } else {
      s.setFailed();
    }
  }

  //Create the certificat for the user.
  public void establishConnection(SocketHandler s)
      throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, ClassNotFoundException {
    //Set the header of the destination
    s.setHeader();
    //Instantiate the body of the response
    s.setNewBody();
    //We generate the certificate after accepting the connection
    String client = s.getSourceName();
    PublicKey pubKey = getSession(s);
    Certificat cert = new Certificat(name, client, pubKey, maCle.privKey(), 356);
    s.setCertificat(cert);
    //print(cert.toString());
    //Set the status for the response
    s.setSuccess();

    //Set the response
    write(s, false);
  }

  public boolean isExpired(SocketHandler s) {
    if (CA.containsKey(s.request.getFromPort())) {
      String lastLog = (String) CA.get(s.getFromPort())[3];
      if (lastLog.equals(s.getSubject())) {
        return true;
      }
    }
    return false;
  }

  public int getPort() {
    return port;
  }

  public void switchMode() {
    mode_server = !mode_server;
  }

  public static String[] genKeyToken(PublicKey key) {
    //TODO: Generate a token base on the publickey
  }

  public static String genKeyMessage(PublicKey key, String token, String code) {
    //TODO: Generate the key message
  }
}
