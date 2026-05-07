StartupEvents.registry('mob_effect', event => {
    event.create('eop:pain_intolerance')
    .displayName("Pain Intolerance")
        .effectTick((entity, lvl) => {
            if (!entity.server) return
            entity.server.runCommandSilent(`execute as ${entity.uuid} at @s run superpower add eop:effects/more_pain @s`);
        }) 
        
})