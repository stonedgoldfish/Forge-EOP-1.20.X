StartupEvents.registry('mob_effect', event => {
    event.create('eop:chimera_infection')
    .displayName("Curse of the Gods")
        .effectTick((entity, lvl) => {
            if (!entity.server) return
            entity.server.runCommandSilent(`execute as ${entity.uuid} at @s run superpower add eop:effects/chimera_turn @s`);
        }) 
        
})