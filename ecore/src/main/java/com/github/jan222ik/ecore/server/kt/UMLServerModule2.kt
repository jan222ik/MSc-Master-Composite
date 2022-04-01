package com.github.jan222ik.ecore.server.kt

import com.github.jan222ik.ecore.UMLPackageConfiguration
import org.eclipse.emfcloud.modelserver.common.Routing
import org.eclipse.emfcloud.modelserver.common.utils.MultiBinding
import org.eclipse.emfcloud.modelserver.emf.configuration.EPackageConfiguration
import org.eclipse.emfcloud.modelserver.emf.di.DefaultModelServerModule

class UMLServerModule2 : DefaultModelServerModule() {
    override fun configureEPackages(binding: MultiBinding<EPackageConfiguration>?) {
        super.configureEPackages(binding)
        binding!!.add(UMLPackageConfiguration::class.java)
    }

    override fun configureRoutings(binding: MultiBinding<Routing>?) {
        super.configureRoutings(binding)
        binding!!.add(UMLRouting2::class.java)
    }


}