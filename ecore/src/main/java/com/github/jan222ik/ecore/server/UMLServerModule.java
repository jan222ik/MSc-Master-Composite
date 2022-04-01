package com.github.jan222ik.ecore.server;

import com.github.jan222ik.ecore.UMLPackageConfiguration;
import org.eclipse.emfcloud.modelserver.common.Routing;
import org.eclipse.emfcloud.modelserver.common.utils.MultiBinding;
import org.eclipse.emfcloud.modelserver.emf.configuration.EPackageConfiguration;
import org.eclipse.emfcloud.modelserver.emf.di.DefaultModelServerModule;

public class UMLServerModule extends DefaultModelServerModule {
    @Override
    protected void configureEPackages(final MultiBinding<EPackageConfiguration> binding) {
        super.configureEPackages(binding);
        binding.add(UMLPackageConfiguration.class);
    }

    @Override
    protected void configureRoutings(final MultiBinding<Routing> binding) {
        super.configureRoutings(binding);
        binding.add(UMLRouting.class);
    }
}
