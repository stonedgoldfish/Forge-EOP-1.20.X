StartupEvents.registry('mob_effect', event => {
    event.create('eop:wet')
    .displayName("Wet")
        .effectTick((entity, lvl) => {
            if (!entity.server) return
            entity.server.runCommandSilent(`execute as ${entity.uuid} at @s run superpower add eop:effects/wet @s`);
        }) 
        
})