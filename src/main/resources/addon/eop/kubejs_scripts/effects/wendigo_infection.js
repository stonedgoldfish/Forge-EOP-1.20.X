StartupEvents.registry('mob_effect', event => {
    event.create('eop:wendigo_infection')
    .displayName("Wendigo Curse")
        .effectTick((entity, lvl) => {
            if (!entity.server) return
            entity.server.runCommandSilent(`execute as ${entity.uuid} at @s run superpower add eop:effects/wendigo_turn @s`);
        }) 
        
})