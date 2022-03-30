plugins {
    kotlin("jvm")
    java
    id("java-library")
}

group = "com.github.jan222ik.ecore"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    maven(uri("https://dist.wso2.org/maven2/"))
}



dependencies {
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    val emfVersion = "0.7.0-SNAPSHOT"
    api("org.eclipse.emfcloud.modelserver:org.eclipse.emfcloud.modelserver.bundles.parent:$emfVersion")
    //api("org.eclipse.emfcloud.modelserver:org.eclipse.emfcloud.modelserver.client.tests:$emfVersion")
    api("org.eclipse.emfcloud.modelserver:org.eclipse.emfcloud.modelserver.client:$emfVersion")
    api("org.eclipse.emfcloud.modelserver:org.eclipse.emfcloud.modelserver.codecoverage:$emfVersion")
    api("org.eclipse.emfcloud.modelserver:org.eclipse.emfcloud.modelserver.codestyle:$emfVersion")
    api("org.eclipse.emfcloud.modelserver:org.eclipse.emfcloud.modelserver.coffee.model:$emfVersion")
    //api("org.eclipse.emfcloud.modelserver:org.eclipse.emfcloud.modelserver.common.tests:$emfVersion")
    api("org.eclipse.emfcloud.modelserver:org.eclipse.emfcloud.modelserver.common:$emfVersion")
    //api("org.eclipse.emfcloud.modelserver:org.eclipse.emfcloud.modelserver.edit.tests:$emfVersion")
    api("org.eclipse.emfcloud.modelserver:org.eclipse.emfcloud.modelserver.edit:$emfVersion")
    //api("org.eclipse.emfcloud.modelserver:org.eclipse.emfcloud.modelserver.emf.tests:$emfVersion")
    api("org.eclipse.emfcloud.modelserver:org.eclipse.emfcloud.modelserver.emf:$emfVersion")
    //api("org.eclipse.emfcloud.modelserver:org.eclipse.emfcloud.modelserver.example:$emfVersion")
    //api("org.eclipse.emfcloud.modelserver:org.eclipse.emfcloud.modelserver.examples.parent:$emfVersion")
    api("org.eclipse.emfcloud.modelserver:org.eclipse.emfcloud.modelserver.features.parent:$emfVersion")
    api("org.eclipse.emfcloud.modelserver:org.eclipse.emfcloud.modelserver.lib:$emfVersion")
    api("org.eclipse.emfcloud.modelserver:org.eclipse.emfcloud.modelserver.parent:$emfVersion")
    api("org.eclipse.emfcloud.modelserver:org.eclipse.emfcloud.modelserver.releng.parent:$emfVersion")
    //api("org.eclipse.emfcloud.modelserver:org.eclipse.emfcloud.modelserver.tests.parent:$emfVersion")
    //api("org.eclipse.emfcloud.modelserver:org.eclipse.emfcloud.modelserver.tests:$emfVersion")
    api("org.eclipse.emfcloud:emfjson-jackson:2.1.0-SNAPSHOT")

    // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core
    // api("org.apache.logging.log4j:log4j-core:2.17.2")
    // https://mvnrepository.com/artifact/com.google.guava/guava
    // api("com.google.guava:guava:31.1-jre")

    val apachelogginglog4jversion = "2.17.1"
    val google_guice_version = "3.0"
    val google_guava_version = "30.1-jre"
    val jackson_databind_version = "2.12.1"
    val eclipse_core_runtime_version = "3.7.0"
    val eclipse_core_resources_version = "3.7.100"
    val emf_common_version = "2.23.0"
    val emf_ecore_version = "2_23.0"
    val emf_ecore_change_version = "2.14.0"
    val emf_ecore_xmi_version = "2.16.0"
    val emf_edit_version = "2.16.0"
    val emf_transaction_version = "1.8.0.201405281451"
    val emf_validation_version = "1.8.0.201405281429"
    val emfjson_jackson_version = "2.0.0"
    val commons_io_version = "2.8.0"
    val junit_version = "4.13.2"
    val mockito_core_version = "2.23.0"
    val hamcrest_core_version = "1.3"
    val maven_dependency_version = "3.2.0"
    val json_patch_version = "1.13"


    api("org.apache.logging.log4j:log4j-slf4j-impl:${apachelogginglog4jversion}")
    api("org.apache.logging.log4j:log4j-core:${apachelogginglog4jversion}")
    api("org.apache.logging.log4j:log4j-web:${apachelogginglog4jversion}")
    api("com.google.inject:guice:${google_guice_version}")
    api("com.google.inject.extensions:guice-multibindings:${google_guice_version}")
    api("com.google.guava:guava:${google_guava_version}")
    api("com.fasterxml.jackson.core:jackson-databind:${jackson_databind_version}")
    api("org.eclipse.core:org.eclipse.core.runtime:${eclipse_core_runtime_version}")
    api("org.eclipse.core:org.eclipse.core.resources:${eclipse_core_resources_version}")
    api("org.eclipse.emf:org.eclipse.emf.common:${emf_common_version}")
    api("org.eclipse.emf:org.eclipse.emf.ecore:${emf_ecore_version}")
    api("org.eclipse.emf:org.eclipse.emf.ecore.change:${emf_ecore_change_version}")
    api("org.eclipse.emf:org.eclipse.emf.edit:${emf_edit_version}")
    api("org.eclipse:org.eclipse.emf.transaction:${emf_transaction_version}")
    api("org.eclipse:org.eclipse.emf.validation:${emf_validation_version}")
    api("org.eclipse.emf:org.eclipse.emf.ecore.xmi:${emf_ecore_xmi_version}")
    api("org.eclipse.emfcloud:emfjson-jackson:${emfjson_jackson_version}")
    api("commons-io:commons-io:${commons_io_version}")
    api("junit:junit:${junit_version}")
    api("org.mockito:mockito-core:${mockito_core_version}")
    api("org.hamcrest:hamcrest-core:${hamcrest_core_version}")


    // https://mvnrepository.com/artifact/org.eclipse.uml2/uml
    //api("org.eclipse.uml2:uml:5.0.0-v20140602-0749")






    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}


tasks.getByName<Test>("test") {
    useJUnitPlatform()
}