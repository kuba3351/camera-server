package com.raspberry.camera.service;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Serwis służący do nasłuchiwania połączeń od klienta podczas wyszukiwania serwera w sieci
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
