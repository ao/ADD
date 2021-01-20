package gl.ao.serengeti;

import gl.ao.serengeti.helpers.Globals;
import gl.ao.serengeti.helpers.ShutdownHandler;
import gl.ao.serengeti.network.Network;
import gl.ao.serengeti.server.Server;
import gl.ao.serengeti.storage.Storage;
import gl.ao.serengeti.storage.StorageReshuffle;
import gl.ao.serengeti.storage.StorageScheduler;
import gl.ao.serengeti.ui.Interactive;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

public class Serengeti {

    public static Storage storage = null;
    public static Server server = new Server();
    public static Date currentDate = new Date();
    public static Network network = new Network();
    public static StorageScheduler storageScheduler = new StorageScheduler();
    public static StorageReshuffle storageReshuffle = new StorageReshuffle();
    public Interactive interactive;
    public Serengeti instance;

    public static long startTime;

    /***
     * Main application entry point
     * @param args
     */
    public static void main(String[] args) {
        new Serengeti();
    }

    /**
     * Constructor
     */
    public Serengeti() {
        interactive = new Interactive();
        System.out.println(getBanner());
        startTime = System.currentTimeMillis();
        System.out.printf("Starting %s..%n", Globals.name);
        instance = this;
        server.init();
        storage = new Storage();
        network.init();

        storageScheduler.init();
        new ShutdownHandler();
    }

    private String getBanner() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("banner.txt");
        return String.format("\n%s", new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n")));
    }

}
