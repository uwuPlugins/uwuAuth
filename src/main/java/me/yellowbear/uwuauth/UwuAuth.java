package me.yellowbear.uwuauth;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import org.slf4j.Logger;

@Plugin(
        id = "uwuauth",
        name = "uwuAuth",
        version = "1.0-SNAPSHOT",
        authors = {"yellowbear"}
)
public class UwuAuth {

    @Inject
    private Logger logger;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

    }
}
