StartupEvents.registry('mob_effect', event => {
    event.create('eop:vampire_infection')
    .displayName("Vampire Infection")
        .effectTick((entity, lvl) => {
            if (!entity.server) return
            entity.server.runCommandSilent(`execute as ${entity.uuid} at @s run superpower add eop:effects/vampire_turn @s`);
        }) 
        
})