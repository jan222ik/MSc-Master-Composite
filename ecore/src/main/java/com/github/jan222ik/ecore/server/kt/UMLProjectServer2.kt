package com.github.jan222ik.ecore.server.kt

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.eclipse.emfcloud.modelserver.emf.launch.CLIBasedModelServerLauncher
import org.eclipse.emfcloud.modelserver.emf.launch.CLIParser

class UMLProjectServer2(
    args: Array<String>
) {

    companion object {
        private val LOG: Logger = LogManager.getLogger(UMLProjectServer2::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            UMLProjectServer2(args)
        }
    }

    private val launcher = CLIBasedModelServerLauncher(
        CLIParser(
            args,
            CLIParser.getDefaultCLIOptions(),
            "java -jar org.eclipse.emfcloud.modelserver.example-X.X.X-SNAPSHOT-standalone.jar",
            8081
        ),
        UMLServerModule2()
    )

    init {
        launcher.run()
    }

}

