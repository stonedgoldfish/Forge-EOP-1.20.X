StartupEvents.registry('mob_effect', event => {
    event.create('eop:werewolf_infection')
    .displayName("Werewolf Infection")
        .effectTick((entity, lvl) => {
            if (!entity.server) return
            entity.server.runCommandSilent(`execute as ${entity.uuid} at @s run superpower add eop:effects/werewolf_turn @s`);
        }) 
        
})