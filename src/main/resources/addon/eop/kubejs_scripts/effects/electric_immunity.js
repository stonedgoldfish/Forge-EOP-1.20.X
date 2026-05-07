StartupEvents.registry('mob_effect', event => {
    event.create('eop:electric_immunity')
    .displayName("Shock Resistance")
        .effectTick((entity, lvl) => {
            if (!entity.server) return
            entity.server.runCommandSilent(`execute as ${entity.uuid} at @s run superpower add eop:effects/electricity_immunity @s`);
        }) 
        
})