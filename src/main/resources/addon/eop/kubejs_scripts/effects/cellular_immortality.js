StartupEvents.registry('mob_effect', event => {
    event.create('eop:cellular_immortality')
    .displayName("Cellular Immortality")
        .effectTick((entity, lvl) => {
            if (!entity.server) return
            entity.server.runCommandSilent(`execute as ${entity.uuid} at @s run superpower add eop:effects/immortality @s`);
        }) 
        
})