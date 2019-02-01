package hmi.hmiprojekt.Connection;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

import hmi.hmiprojekt.MainActivity;
import hmi.hmiprojekt.MemoryAccess.Config;

public class NearbyConnect {
    private ConnectionsClient connectionsClient;
    private String codeName;
    private File fileToSend;
    private Context context;

    public NearbyConnect(File fileToSend, ConnectionsClient connectionsClient, Context context){
        this.fileToSend = fileToSend;
        this.connectionsClient = connectionsClient;
        Random r = new Random();
        codeName = Integer.toString(r.nextInt(1000 - 1));
        this.context = context;
    }

    private void startAdvertisingHere() {

        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build();
        connectionsClient.startAdvertising(codeName, "hmi.hmiprojekt", connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener(
                        (Void unused) -> {
                            // We're advertising!
                            Toast.makeText(context,"Suche nach einem Austauschpartner",Toast.LENGTH_SHORT).show();

                        })
                .addOnFailureListener(
                        (Exception e) -> {
                            // We were unable to start advertising.
                        });
    }

    private void startDiscoveryHere() {
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
                    connectionsClient.stopAdvertising();
                    Toast.makeText(context,"Partner gefunden",Toast.LENGTH_SHORT).show();

                    connectionsClient.requestConnection(codeName, endpointId, connectionLifecycleCallback)
                            .addOnSuccessListener(
                                    (Void unused) -> {
                                    })
                            .addOnFailureListener(
                                    (Exception e) -> {
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
                    connectionsClient.acceptConnection(endpointId, payloadCallback);
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            if(fileToSend!=null) {
                                try {
                                    Payload filePayload = Payload.fromFile(fileToSend);
                                    connectionsClient.sendPayload(endpointId, filePayload);
                                } catch (FileNotFoundException e) {
                                    Log.e("senden", e.getMessage());
                                }
                            }
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            // The connection was rejected by one or both sides.
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            // The connection broke before it was able to be accepted.
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
                    Toast.makeText(context,"Datei empfangen",Toast.LENGTH_SHORT).show();
                    File lastFilePath = getLatestFilefromDir(Environment.getExternalStorageDirectory() + "/Download/Nearby");
                    if (lastFilePath != null) {
                        try {
                            Date todayDate = Calendar.getInstance().getTime();
                            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                            String dirName = dateFormat.format(todayDate) + "_SharedTrip";
                            Zipper.unzip(lastFilePath, new File(Environment.getExternalStorageDirectory() + "/roadbook/" + dirName));
                        } catch (IOException e) {
                            Log.e("ZIP", e.getMessage());
                        }
                    }
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                        connectionsClient.disconnectFromEndpoint(endpointId);
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage("Der Trip wurde auf Ihrem Gerät gespeichert")
                                .setTitle("Erfolg!");
                        builder.create();

                    }
                }
            };

    private File getLatestFilefromDir(String dirPath){
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }

        File lastModifiedFile = files[0];
        for (int i = 1; i < files.length; i++) {
            if (lastModifiedFile.lastModified() < files[i].lastModified()) {
                lastModifiedFile = files[i];
            }
        }
        return lastModifiedFile;
    }

    public void start(){
        startAdvertisingHere();
        startDiscoveryHere();
    }
}
