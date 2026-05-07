StartupEvents.registry('mob_effect', event => {
    event.create('eop:brain_overload')
    .displayName("Brain Overload")
        .effectTick((entity, lvl) => {
            if (!entity.server) return
            entity.server.runCommandSilent(`execute as ${entity.uuid} at @s run superpower add eop:effects/brain_overload @s`);
        }) 
        
})