package com.github.jan222ik.ecore.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.emfcloud.modelserver.emf.launch.CLIBasedModelServerLauncher;
import org.eclipse.emfcloud.modelserver.emf.launch.CLIParser;

public class UMLProjectServer {
    private static Logger LOG = LogManager.getLogger(UMLProjectServer.class);

    public UMLProjectServer(String[] args) {
        var launcher = new CLIBasedModelServerLauncher(
                new CLIParser(
                        args,
                        CLIParser.getDefaultCLIOptions(),
                        "java -jar org.eclipse.emfcloud.modelserver.example-X.X.X-SNAPSHOT-standalone.jar",
                        8081
                ),
                new UMLServerModule()
        );
        launcher.run();
    }

    public static void main(String[] args) {
        new UMLProjectServer(args);
    }

}


