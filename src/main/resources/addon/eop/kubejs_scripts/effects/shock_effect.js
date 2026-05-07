StartupEvents.registry('mob_effect', event => {
    event.create('eop:shock')
    .displayName("Shock")
        .effectTick((entity, lvl) => {
            if (!entity.server) return
            entity.server.runCommandSilent(`execute as ${entity.uuid} at @s run superpower add eop:effects/shock @s`);
        }) 
        
})