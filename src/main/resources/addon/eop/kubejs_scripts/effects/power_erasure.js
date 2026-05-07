StartupEvents.registry('mob_effect', event => {
    event.create('eop:power_erasure')
    .displayName("Power Erasure")
        .effectTick((entity, lvl) => {
            if (!entity.server) return
            entity.server.runCommandSilent(`execute as ${entity.uuid} at @s run superpower add eop:effects/power_erased @s`);
        }) 
        
})