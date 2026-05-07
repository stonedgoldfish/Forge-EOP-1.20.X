StartupEvents.registry('mob_effect', event => {
    event.create('eop:boil_strong')
    .displayName("Boil")
        .effectTick((entity, lvl) => {
            if (!entity.server) return
            entity.server.runCommandSilent(`execute as ${entity.uuid} at @s run superpower add eop:effects/boil_strong @s`);
        }) 
        
})