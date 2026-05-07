StartupEvents.registry('mob_effect', event => {
    event.create('eop:charge')
    .displayName("Charge")
        .effectTick((entity, lvl) => {
            if (!entity.server) return
            entity.server.runCommandSilent(`execute as ${entity.uuid} at @s run superpower add eop:effects/charged_tempest @s`);
        }) 
        
})