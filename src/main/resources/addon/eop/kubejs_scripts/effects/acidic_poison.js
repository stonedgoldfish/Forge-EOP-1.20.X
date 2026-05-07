StartupEvents.registry('mob_effect', event => {
    event.create('eop:acidic_poison')
    .displayName("Acidic Poison")
        .effectTick((entity, lvl) => {
            if (!entity.server) return
            entity.server.runCommandSilent(`execute as ${entity.uuid} at @s run superpower add eop:effects/acidic_poison @s`);
        }) 
        
})