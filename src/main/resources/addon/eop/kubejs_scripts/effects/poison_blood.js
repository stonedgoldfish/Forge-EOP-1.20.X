StartupEvents.registry('mob_effect', event => {
    event.create('eop:poison_blood')
    .displayName("Poison Blood")
        .effectTick((entity, lvl) => {
            if (!entity.server) return
            entity.server.runCommandSilent(`execute as ${entity.uuid} at @s run superpower add eop:effects/blood_poisoned @s`);
        }) 
        
})