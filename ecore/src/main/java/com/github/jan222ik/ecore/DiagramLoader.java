package com.github.jan222ik.ecore;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.Class;

import java.io.File;

public class DiagramLoader {
    public static void open(File file) {
        URI testUri = URI.createFileURI(file.getAbsolutePath());
        EMFResourceLoader.initBaseResources();
        ResourceSet resourceSet1 = new ResourceSetImpl();
        resourceSet1.setResourceFactoryRegistry(Resource.Factory.Registry.INSTANCE);
        resourceSet1.getResource(testUri, true);
        resourceSet1.getAllContents().forEachRemaining(el -> {
            if (el instanceof Class) {
                var name = ((Class) el).getQualifiedName();
                System.out.println("name = " + name);
                ((Class) el).getAppliedStereotypes().forEach(System.out::println);
            }
        });
    }

    public static void main(String[] args) {
        File file = new File("C:\\Users\\jan\\Documents\\master-dependencies\\master-dependencies.uml");
        assert file.exists();
        DiagramLoader.open(file);
    }

}
