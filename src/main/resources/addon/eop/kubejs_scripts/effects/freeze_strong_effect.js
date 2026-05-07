StartupEvents.registry('mob_effect', event => {
    event.create('eop:freeze_stronger')
    .displayName("Freeze")
        .effectTick((entity, lvl) => {
            if (!entity.server) return
            entity.server.runCommandSilent(`execute as ${entity.uuid} at @s run superpower add eop:effects/freeze_strong @s`);
        }) 
        
})