StartupEvents.registry('mob_effect', event => {
    event.create('eop:ghostly_sensory_deception')
    .displayName("Sensory Deception")
        .effectTick((entity, lvl) => {
            if (!entity.server) return
            entity.server.runCommandSilent(`execute as ${entity.uuid} at @s run superpower add eop:effects/ghost_crazy @s`);
        }) 
        
})