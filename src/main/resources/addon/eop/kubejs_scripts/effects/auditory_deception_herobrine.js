StartupEvents.registry('mob_effect', event => {
    event.create('eop:auditory_deception_herobrine')
    .displayName("Auditory Deception")
        .effectTick((entity, lvl) => {
            if (!entity.server) return
            entity.server.runCommandSilent(`execute as ${entity.uuid} at @s run superpower add eop:effects/herobrine_crazy @s`);
        }) 
        
})