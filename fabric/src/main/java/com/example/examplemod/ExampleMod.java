package com.example.examplemod;

import net.fabricmc.api.ModInitializer;
import realcolin.whmod.WarhammerMod;
import realcolin.whmod.Constants;

public class ExampleMod implements ModInitializer {
    
    @Override
    public void onInitialize() {
        
        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.
        Constants.LOG.info("Hello Fabric world!");
        WarhammerMod.init();
    }
}
