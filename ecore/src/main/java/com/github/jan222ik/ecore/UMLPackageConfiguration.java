package com.github.jan222ik.ecore;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.emfcloud.modelserver.emf.configuration.EPackageConfiguration;
import org.eclipse.uml2.uml.UMLPackage;

public class UMLPackageConfiguration implements EPackageConfiguration {
   @Override
   public String getId() { return UMLPackage.eINSTANCE.getNsURI(); }

   @Override
   public Collection<String> getFileExtensions() {
      var fileExt = new ArrayList<String>();
      fileExt.add(".uml");
      return fileExt;
   }

   @Override
   public void registerEPackage() {
      UMLPackage.eINSTANCE.eClass();
   }
}
