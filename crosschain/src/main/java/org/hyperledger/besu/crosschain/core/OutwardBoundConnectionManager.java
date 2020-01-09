package org.hyperledger.besu.crosschain.core;

import org.hyperledger.besu.ethereum.core.Address;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class will manage all outward bound connections. For the moment, this is just JSON RPC.
 */
public class OutwardBoundConnectionManager {


  // TODO this should be implemented as a Vertx HTTPS Client. We should probably submit all
  // TODO Subordinate Views together, and wait for them to all return, and submit all
  //  Subordinate Transactions together and wait for them to all return.
  public static String post(final String ipAddressPort, final String method, final String params)
      throws Exception {
    URL url = new URL("http://" + ipAddressPort);
    URLConnection con = url.openConnection();
    HttpURLConnection http = (HttpURLConnection) con;
    http.setRequestMethod("POST");
    http.setDoOutput(true);
    byte[] out =
        ("{\"jsonrpc\":\"2.0\",\"method\":\""
            + method
            + "\",\"params\":[\""
            + params
            + "\"],\"id\":1}")
            .getBytes(UTF_8);
    int length = out.length;
    http.setFixedLengthStreamingMode(length);
    http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
    http.connect();
    OutputStream os = http.getOutputStream();
    os.write(out);
    BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream(), UTF_8));
    String line;
    String response = "";
    while ((line = in.readLine()) != null) {
      response += line;
    }
    os.close();
    in.close();
    http.disconnect();
    return response;
  }


  public static void coordContractStart(final String ipAddressPort, final Address contractAddress, )

}
