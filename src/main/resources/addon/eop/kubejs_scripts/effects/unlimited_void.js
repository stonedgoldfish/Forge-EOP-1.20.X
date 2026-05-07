StartupEvents.registry('mob_effect', event => {
    event.create('eop:unlimited_void')
    .displayName("§kUnlimited")
        .effectTick((entity, lvl) => {
            if (!entity.server) return
            entity.server.runCommandSilent(`execute as ${entity.uuid} at @s run superpower add eop:effects/unlimited_void @s`);
        }) 
        
})