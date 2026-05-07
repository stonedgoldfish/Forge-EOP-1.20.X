StartupEvents.registry('mob_effect', event => {
    event.create('eop:optic_blindness')
    .displayName("Optic Blindess")
        .effectTick((entity, lvl) => {
            if (!entity.server) return
            entity.server.runCommandSilent(`execute as ${entity.uuid} at @s run superpower add eop:effects/invis_blind @s`);
        }) 
        
})