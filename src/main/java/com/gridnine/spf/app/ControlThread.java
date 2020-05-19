/*****************************************************************
 * Gridnine AB http://www.gridnine.com
 * Project: SPF
 *****************************************************************/

package com.gridnine.spf.app;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileLock;

@SuppressWarnings("unused")
final class ControlThread extends Thread {
    private final ServerSocket serverSocket;
    private boolean appRunning;
    private final SpfApplication app;
    private final FileLock lock;
    private final File tempFile;



    interface RequestHandler {

        byte[] getRequest();

        boolean handleResponse(String response);

    }

    private static boolean makeRequest(InetAddress host, int port, RequestHandler handler) {
        try {
            try (Socket socket = new Socket(host, port)) {
                socket.setKeepAlive(true);
                InputStream in = null;
                try (OutputStream out = socket.getOutputStream()) {
                    println("found running control service on " + host + ":" + port);
                    out.write(handler.getRequest());
                    out.flush();
                    socket.shutdownOutput();
                    in = socket.getInputStream();
                    StringBuilder commandResult = new StringBuilder();
                    byte[] buf = new byte[16];

                    int len;
                    while ((len = in.read(buf)) != -1) {
                        commandResult.append(new String(buf, 0, len));
                    }

                    socket.shutdownInput();
                    String result = commandResult.toString();
                    return handler.handleResponse(result);
                } finally {
                    if (in != null) in.close();
                }
            }
        } catch (IOException e) {
            println("seems that there is no control service running on " + host + ":" + port);
            return false;
        }
    }

    static boolean isApplicationRunning(InetAddress host, int port) {

        return makeRequest(host, port, new RequestHandler() {
            private final String test = "" + System.currentTimeMillis();

            @Override
            public byte[] getRequest() {
                return ("PING " + test).getBytes();
            }

            @Override
            public boolean handleResponse(String response) {
                if (response.startsWith("OK") && response.contains(test)) {
                    println("PING command succeed");
                    return true;
                } else {
                    println("PING command failed");
                    return false;
                }
            }
        });
    }

    static void stopRunningApplication(InetAddress host, int port) {
        makeRequest(host, port, new RequestHandler() {
            @Override
            public byte[] getRequest() {
                return "STOP".getBytes();
            }

            @Override
            public boolean handleResponse(String response) {
                if (response.startsWith("OK")) {
                    println("STOP command succeed");
                    return true;
                } else {
                    println("STOP command failed");
                    return false;
                }
            }
        });
    }

    ControlThread(InetAddress host, int port, SpfApplication app, FileLock lock, File tempFile) throws Exception {

        this.serverSocket = new ServerSocket(port, 1, host);
        this.appRunning = true;
        this.app = app;
        this.lock = lock;
        this.tempFile = tempFile;
        this.setName("spf-application-control-thread");
    }

    public void run() {
        try {
            while (true) {
                try {
                    try(Socket clientSocket = this.serverSocket.accept()){
                        if (this.handleRequest(clientSocket)) {
                            break;
                        }
                    } catch (Exception e){
                        //noops
                    }
                } catch (Exception e) {
                    println("error on server socket");
                    e.printStackTrace();
                    break;
                }
            }
        } finally {
            try {
                this.serverSocket.close();
            } catch (IOException e) {
                println("error closing server socket");
                e.printStackTrace();
            }

            if (this.appRunning) {
                this.stopApplication();
            }
            if(lock != null){
                releaseLock(lock, tempFile);
            }
        }

    }

    private synchronized boolean handleRequest(Socket clientSocket) {
        if (!this.isValidRemoteHost(clientSocket.getInetAddress())) {
            println("incoming connection to control socket registered from REMOTE address " + clientSocket.getInetAddress() + ", attempt to execute command was IGNORED");

            try {
                clientSocket.close();
            } catch (IOException e) {
                //nopps
            }

            return false;
        }


        boolean result = false;

        OutputStream out = null;
        try (InputStream in = clientSocket.getInputStream()) {
            StringBuilder command = new StringBuilder();
            byte[] buf = new byte[16];

            int len;
            while ((len = in.read(buf)) != -1) {
                command.append(new String(buf, 0, len));
            }

            clientSocket.shutdownInput();
            String commandResult;
            if ("STOP".equals(command.toString())) {
                this.stopApplication();
                result = true;
                commandResult = "OK: stop done";
            } else if (command.toString().startsWith("PING")) {
                commandResult = "OK: " + command.substring("PING".length());
            } else {
                commandResult = "ERROR: unknown command";
            }

            out = clientSocket.getOutputStream();
            out.write(commandResult.getBytes());
            out.flush();
            clientSocket.shutdownOutput();

        } catch (IOException e) {
            println("error processing control request");
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    println("error closing socket");
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    private void stopApplication() {
        if (!this.appRunning) {
            println("application not running");
            return;
        }

        this.appRunning = false;
        println("stopping application");

        try {
            this.app.stop();
        } catch (Exception e) {
            println("an error has occurred while stopping application");
            e.printStackTrace();
            return;
        }
        println("application stopped from control thread");
    }

    private boolean isValidRemoteHost(InetAddress addr) {
        byte[] localAddr = this.serverSocket.getInetAddress().getAddress();
        byte[] remoteAddr = addr.getAddress();
        if (localAddr.length != remoteAddr.length) {
            return false;
        } else {
            for (int i = 0; i < remoteAddr.length; ++i) {
                if (localAddr[i] != remoteAddr[i]) {
                    return false;
                }
            }

            return true;
        }
    }

    private static void println(String text) {
        System.out.println(text);
    }


    static void releaseLock(FileLock lock, File tempFile){
        if (lock != null) {
            try {
                lock.release();
                lock.channel().close();
                if(tempFile.exists() && !tempFile.delete()){
                    throw new Exception("unable to delete temp file " + tempFile);
                }
            } catch (Exception e) {
                println("error releasing lock");
                e.printStackTrace();
            }
        }
    }
}
