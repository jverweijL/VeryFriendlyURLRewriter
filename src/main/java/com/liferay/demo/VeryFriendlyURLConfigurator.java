package com.liferay.demo;


import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.messaging.Destination;
import com.liferay.portal.kernel.messaging.DestinationConfiguration;
import com.liferay.portal.kernel.messaging.DestinationFactory;
import com.liferay.portal.kernel.util.HashMapDictionary;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;



@Component (
        immediate = true,
        service = VeryFriendlyURLConfigurator.class
)
public class VeryFriendlyURLConfigurator {

    public static final String DESTINATION = "liferay/veryfriendlyurl/task";

    @Activate
    protected void activate(BundleContext bundleContext) {
        _log.debug("Howdy, I'm VeryFriendlyURLConfigurator, here to serve you.");

        _bundleContext = bundleContext;

        // Create a DestinationConfiguration for parallel destinations.

        DestinationConfiguration destinationConfiguration =
                new DestinationConfiguration(
                        DestinationConfiguration.DESTINATION_TYPE_SERIAL,
                        DESTINATION);

        // Set the DestinationConfiguration's max queue size and
        // rejected execution handler.
        destinationConfiguration.setMaximumQueueSize(500);

        // Create the destination
        Destination destination = _destinationFactory.createDestination(destinationConfiguration);
        _log.debug("My destination is " + destination.getName());

        // Add the destination to the OSGi service registry
        Dictionary<String, Object> properties = new HashMapDictionary<>();
        properties.put("destination.name", destination.getName());

        ServiceRegistration<Destination> serviceRegistration =
                _bundleContext.registerService(
                        Destination.class, destination, properties);

        // Track references to the destination service registrations
        _serviceRegistrations.put(destination.getName(),
                serviceRegistration);
    }

    @Deactivate
    protected void deactivate() {

        // Unregister and destroy destinations this component unregistered

        for (ServiceRegistration<Destination> serviceRegistration :
                _serviceRegistrations.values()) {

            Destination destination = _bundleContext.getService(
                    serviceRegistration.getReference());
            _log.debug("Deactivate destination " + destination.getName());

            serviceRegistration.unregister();

            destination.destroy();
        }

        _serviceRegistrations.clear();
    }

    @Reference
    private DestinationFactory _destinationFactory;

    private final Map<String, ServiceRegistration<Destination>>
            _serviceRegistrations = new HashMap<>();

    private volatile BundleContext _bundleContext;

    private static final Log _log = LogFactoryUtil.getLog(
            VeryFriendlyURLConfigurator.class);

}