package hmi.hmiprojekt.Connection;

import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;

import hmi.hmiprojekt.MainActivity;

public class NearbyConnect {
    private ConnectionsClient connectionsClient;
    private String endpoint;
    private String codeName;
    private File fileToSend=null;

    public NearbyConnect(ConnectionsClient connectionsClient){
        this.connectionsClient = connectionsClient;
        Random r = new Random();
        codeName = Integer.toString(r.nextInt(1000 - 1));
    }

    private void startAdvertisingHere() {

        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build();
        connectionsClient.startAdvertising(codeName, "hmi.hmiprojekt", connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener(
                        (Void unused) -> {
                            // We're advertising!
                            Log.e("Advertising", "Advertising gestartet");
                        })
                .addOnFailureListener(
                        (Exception e) -> {
                            // We were unable to start advertising.
                            Log.e("Advertising", "Advertising abgebrochen");
                        });
    }

    private void startDiscoveryHere() {
        Log.e("Discovery", "Suche gestartet");
        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build();
        connectionsClient.startDiscovery("hmi.hmiprojekt", endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener(
                        (Void unused) -> {
                        })
                .addOnFailureListener(
                        (Exception e) -> {
                        });
    }

    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                    // An endpoint was found. We request a connection to it.
                    connectionsClient.stopDiscovery();
                    Log.e("Endpoint Found", "try connecting");
                    connectionsClient.requestConnection(codeName, endpointId, connectionLifecycleCallback)
                            .addOnSuccessListener(
                                    (Void unused) -> {
                                    })
                            .addOnFailureListener(
                                    (Exception e) -> {
                                        Log.e("Endpoint Found", e.getMessage());
                                    });
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    // A previously discovered endpoint has gone away.
                }
            };

    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    // Automatically accept the connection on both sides.
                    Log.e("Connection", "connection akzeptieren");
                    connectionsClient.acceptConnection(endpointId, payloadCallback);
                    endpoint=connectionInfo.getEndpointName();
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            Log.e("ConnectionResult", "verbunden!");


                            if(fileToSend!=null) {
                                try {
                                    Log.e("ConnectionResult", "senden");
                                    Payload filePayload = Payload.fromFile(fileToSend);
                                    connectionsClient.sendPayload(endpoint, filePayload);
                                } catch (FileNotFoundException e) {
                                    Log.e("senden", e.getMessage());
                                }
                            }
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            // The connection was rejected by one or both sides.
                            Log.e("ConnectionResult", "Verbindung wurde abgelehnt");
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            // The connection broke before it was able to be accepted.
                            Log.e("ConnectionResult", "Verbindung wurde unterbrochen");
                            break;
                        default:
                            // Unknown status code
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    // We've been disconnected from this endpoint. No more data can be
                    // sent or received.
                }
            };

    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    Log.e("Payload", "empfangen");
                    File payloadFile = payload.asFile().asJavaFile();
                    //wo auch immer speichern
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                        connectionsClient.disconnectFromEndpoint(endpoint);
                    }
                }
            };

    public void sender(File file){

        startAdvertisingHere();
        startDiscoveryHere();
        fileToSend=file;

    }

    public void receiver(){
        startAdvertisingHere();
        startDiscoveryHere();
    }
}
/*

  public void sendFile(File file){
    NearbyConnect connectionsClient = new NearbyConnect(Nearby.getConnectionsClient(this));
    connectionsClient.sender(file);
  }

  public void receiveFile(){
    NearbyConnect connectionsClient = new NearbyConnect(Nearby.getConnectionsClient(this));
    connectionsClient.receiver();
  }
 */