/*****************************************************************
 * Gridnine AB http://www.gridnine.com
 * Project: SPF
 *****************************************************************/

package com.gridnine.spf.app;

import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SpfBoot {
    public static void main(String[] args) throws Exception {
        Properties properties = initProperties();
        String mode = System.getProperty("spf.mode");
        int port = Integer.parseInt(properties.getProperty("spf.port", "21566"));
        InetAddress address = InetAddress.getByName("localhost");
        if ("stop".equals(mode)) {
            ControlThread.stopRunningApplication(address, port);
            return;
        }
        File libFolder = new File(properties.getProperty("spf.libFolder", "lib"));
        if (!libFolder.exists()) {
            throw new IllegalArgumentException(String.format("lib folder %s does not exist", libFolder.getAbsolutePath()));
        }
        String applicationClass = properties.getProperty("spf.applicationClass", System.getProperty("spf.applicationClass"));
        if (applicationClass == null) {
            throw new IllegalArgumentException("application class is not defined");
        }
        String tempDirectory = properties.getProperty("spf.tempDirectory", System.getProperty("spf.tempDirectory", "temp"));
        FileLock fileLock = acquireLock(tempDirectory);
        List<URL> urls = new ArrayList<>();
        File externalsFile = new File(libFolder, "externals.txt");
        if (externalsFile.exists()) {
            try (InputStream is = new FileInputStream(externalsFile)) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String line = reader.readLine();
                while (line != null) {
                    urls.add(new File(line).toURI().toURL());
                    line = reader.readLine();
                }

            }
        }
        File[] files = libFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".jar")) {
                    urls.add(file.toURI().toURL());
                }
            }
        }
        ClassLoader cl = new SpfClassLoader(urls.toArray(new URL[0]), SpfBoot.class.getClassLoader());
        SpfApplication app = (SpfApplication) cl.loadClass(applicationClass).getConstructor().newInstance();

        try {
            app.start(properties);
        }catch (Exception e){
            app.stop();
            ControlThread.releaseLock(fileLock);
            throw e;
        }
        if ("start".equals(mode)) {
            System.out.println("Press 'q' key to exit.");
            int c;
            do {
                try {
                    c = System.in.read();
                } catch (IOException var2) {
                    break;
                }
            } while ('q' != (char) c && 'Q' != (char) c);
            app.stop();
            ControlThread.releaseLock(fileLock);
            return;
        }
        if (ControlThread.isApplicationRunning(address, port)) {
            System.out.println("Application already running.");
            ControlThread.releaseLock(fileLock);
            return;
        }

        ControlThread controlThread = new ControlThread(address, port, app, fileLock);
        controlThread.start();
        System.out.println("application started in BACKGROUND mode");


    }

    private static Properties initProperties() throws IOException {
        File configFile = new File(System.getProperty("spf.config", "config/boot.properties"));
        Properties properties = new Properties();
        if (configFile.exists()) {
            try (InputStream is = new FileInputStream(configFile)) {
                properties.load(new InputStreamReader(is, StandardCharsets.UTF_8));
            }
        }
        return properties;
    }

    @SuppressWarnings("unused")
    public static boolean isApplicationRunning() throws Exception {
        Properties properties = initProperties();
        int port = Integer.parseInt(properties.getProperty("spf.port", "21566"));
        InetAddress address = InetAddress.getByName("localhost");
        return ControlThread.isApplicationRunning(address, port);
    }

    private static FileLock acquireLock(String tempDirectory) throws Exception {
        File tempDir = new File(tempDirectory);
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            throw new Exception("unable to create dir " + tempDir);
        }
        File file = new File(tempDir, ".lock");
        FileLock result = null;
        try {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            file.deleteOnExit();
            result = new RandomAccessFile(file, "rwd").getChannel().tryLock();

        } catch (Exception e) {
            throw new Exception(
                    "Another instance of the application is running. Please terminate and try again.", e);
        }
        if(result == null){
            throw new Exception(
                    "Another instance of the application is running. Please terminate and try again.");
        }
        return result;
    }

    static class SpfClassLoader extends URLClassLoader{

        private final URL[] urls;

        public SpfClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
            this.urls = urls;
        }

        public SpfClassLoader(URL[] urls) {
            super(urls);
            this.urls = urls;
        }

        public SpfClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
            super(urls, parent, factory);
            this.urls = urls;
        }

    }
}
