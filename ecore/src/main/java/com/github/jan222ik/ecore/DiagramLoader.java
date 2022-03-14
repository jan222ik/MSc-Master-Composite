package com.github.jan222ik.ecore;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;

import java.io.File;

public class DiagramLoader {
    public static ResourceSet open(File file) {
        URI fileUri = URI.createFileURI(file.getAbsolutePath());
        EMFResourceLoader.initBaseResources();
        ResourceSet resourceSet1 = new ResourceSetImpl();
        resourceSet1.setResourceFactoryRegistry(Resource.Factory.Registry.INSTANCE);
        resourceSet1.getResource(fileUri, true);
        return resourceSet1;
    }

    public static void main(String[] args) {
        File file = new File("C:\\Users\\jan\\Documents\\master-dependencies\\master-dependencies.uml");
        assert file.exists();
        ResourceSet resourceSet1 = DiagramLoader.open(file);
        TreeIterator<Object> allContents = EcoreUtil.getAllContents(resourceSet1, true);


        allContents.forEachRemaining(el -> {
            if (el instanceof Element) {
                Element castEl = ((Element) el);
                System.out.println("el.getOwner() = " + castEl.getOwner());
                if (((Element) el).getOwner() instanceof Model) {
                    Class casted = (Class) el;
                    var name = casted.getQualifiedName();
                    System.out.println("name = " + name);
                    casted.getAppliedStereotypes().forEach(System.out::println);
                } else {
                    System.out.println("el = " + el);
                }
            } else {
                System.err.println("el = " + el.getClass());
            }
        });


    }

}
