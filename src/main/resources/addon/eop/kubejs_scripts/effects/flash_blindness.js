StartupEvents.registry('mob_effect', event => {
    event.create('eop:flash_blindness')
    .displayName("Flash Blindness")
        .effectTick((entity, lvl) => {
            if (!entity.server) return
            entity.server.runCommandSilent(`execute as ${entity.uuid} at @s run superpower add eop:effects/sun_blind @s`);
        }) 
        
})