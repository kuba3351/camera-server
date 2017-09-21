package com.raspberry.camera;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.util.logging.Level;

/**
 * Created by jakub on 29.07.17.
 */
public class AutoDiscoveryListener implements Runnable {

    private final static Logger logger = Logger.getLogger(AutoDiscoveryListener.class);

    @Override
    public void run() {
        try {
            DatagramSocket socket;
            socket = new DatagramSocket(9000, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);

            while (true) {
                byte[] recvBuf = new byte[15000];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);

                logger.info("Klient łączy się z serwerem...");

                String message = new String(packet.getData()).trim();
                if (message.equals("DISCOVER_FUIFSERVER_REQUEST")) {
                    byte[] sendData = "DISCOVER_FUIFSERVER_RESPONSE".getBytes();

                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                    socket.send(sendPacket);

                    logger.info("Wysłano odpowiedź...");
                }
            }
        } catch (IOException ex) {
            logger.error("AutoDiscovery exception");
        }
    }
}
