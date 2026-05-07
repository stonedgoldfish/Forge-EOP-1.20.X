StartupEvents.registry('mob_effect', event => {
    event.create('eop:cold_immunity')
    .displayName("Cold Resistance")
        .effectTick((entity, lvl) => {
            if (!entity.server) return
            entity.server.runCommandSilent(`execute as ${entity.uuid} at @s run superpower add eop:effects/cold_immunity @s`);
        }) 
        
})