package de.tum.i13.KVStoreTest;

import de.tum.i13.ECS.ECS;
import de.tum.i13.Util;
import de.tum.i13.server.kv.KVCommandProcessor;
import de.tum.i13.server.kv.KVStore;
import de.tum.i13.server.nio.NioServer;
import de.tum.i13.shared.Config;
import de.tum.i13.shared.Constants;
import de.tum.i13.shared.ServerStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static de.tum.i13.shared.Config.parseCommandlineArgs;
import static org.junit.jupiter.api.Assertions.*;

public class TestPut {

    private static KVStore kv;
    private static Thread th;
    private static NioServer ns;

    private static Thread ecs;
    public static Integer port = 5186;


    private static void launchServer(){


        ECS ex = new ECS(52357, "OFF");
        ecs = new Thread(ex);
        ecs.start();
        while(!ex.isReady()){
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Path path = Paths.get("data/");
        File[] files = new File(path.toAbsolutePath().toString() +"/").listFiles();
        if(files != null){
            for(File file: files){
                file.delete();
            }
        }
        String[] args = {"-s", "FIFO", "-c", "100", "-d", "data/", "-l", "loggg.log", "-ll", "OFF", "-b", "127.0.0.1:52357", "-p", String.valueOf(port)};
        Config cfg = Config.parseCommandlineArgs(args);
        kv = new KVStore(cfg, true, new ServerStatus(Constants.INACTIVE));
        KVCommandProcessor cmdp = new KVCommandProcessor(kv);
        ns = new NioServer(cmdp);
        try {

            ns.bindSockets(cfg.listenaddr, cfg.port);

            th = new Thread(() -> {
                try {
                    ns.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            th.start();
        } catch (IOException e) {
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    void testPut() throws IOException {
        launchServer();

        assertEquals(0, kv.put("Hello", "World"));
        assertEquals(0, kv.put("World", "Hello"));
        assertEquals(0, kv.put("Ciao", "Mondo"));
        assertEquals(1, kv.put("Ciao", "World"));
        assertEquals(1, kv.delete("Ciao"));

    }


    @AfterAll
    static void afterAll(){
        Path path = Paths.get("data/");
        File[] files = new File(path.toAbsolutePath().toString() +"/").listFiles();
        if(files != null){
            for(File file: files){
                file.delete();
            }
        }
        ecs.interrupt();
        th.interrupt();
        ns.close();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
