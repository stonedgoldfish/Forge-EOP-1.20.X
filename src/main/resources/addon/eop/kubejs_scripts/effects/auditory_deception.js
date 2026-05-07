StartupEvents.registry('mob_effect', event => {
    event.create('eop:auditory_deception')
    .displayName("Auditory Deception")
        .effectTick((entity, lvl) => {
            if (!entity.server) return
            entity.server.runCommandSilent(`execute as ${entity.uuid} at @s run superpower add eop:effects/crazy_hear @s`);
        }) 
        
})