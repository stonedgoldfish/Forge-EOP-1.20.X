StartupEvents.registry('block', event => {
    event.create('eop:celestial_codex', 'cardinal')
      .displayName('Celestial Codex') 
      .soundType('stone')
      .requiresTool(false)
      .resistance(1.0) 
      .hardness(1.0)  
      .model('eop:block/celestial_codexx')
      .box(4, 0, 5, 12, 9, 11, true)
      .lightLevel(0.6)
      .defaultCutout()
  })

  StartupEvents.modifyCreativeTab('kubejs:tab', event => {
    event.remove('eop:celestial_codex')
})