package HomKit;

import Component.Equipement;
import Console.FilerParser;
import Console.JSONParser;
import Interfaces.IHMHome.IHMHome;
import Interfaces.IHMHome.Loading;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Project Name : TL_crypto
 */
public class Home extends Thread implements Runnable {
    private HashSet<Equipement> equipements = new HashSet<Equipement>();
    private static IHMHome homeInterface = null;
    private boolean check_new_equi = false;
    final static int TIME_CHECK = 5000; //Check the new equipements every TIME_CHECK Seconds
    public static boolean DEBUG_MODE = true;
    public static Loading loadPage = null;
    public static boolean newCo = false;

    public void checkComponent() {
        try {
            List<File> filesInFolder = Files.walk(Paths.get("src/data/CompData/"))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
            int size = filesInFolder.size();
            for (File file : filesInFolder) {
                //We check if we don't already have this equipements
                JSONParser.genrateEquipement(file.getName(), equipements);
            }
            loadPage.setValue(20);
        } catch (IOException e) {
            System.out.print(e.fillInStackTrace());
        }
    }

    //Check for new componenet
    public void checkNewComponent() {
        while (true) {
            try {
                checkComponent();
                Thread.sleep(TIME_CHECK);
                if (newCo) {
                    homeInterface.update();
                }
            } catch (InterruptedException e) {

            }
        }
    }

    public void run() {

        if (!check_new_equi) {
            //If is the first time we reolad the equipement
            loadPage = new Loading();
            checkComponent();
            homeInterface = new IHMHome(equipements);
            homeInterface.update();
            check_new_equi = true;
            new Thread(this).start();
        } else {
            checkNewComponent();
        }

    }

    public static void update() {
        homeInterface.update();
    }
}
