StartupEvents.registry('block', event => {
    event.create('eop:golden_idol', 'cardinal')
      .displayName('Golden Idol') 
      .soundType('stone')
      .requiresTool(false)
      .resistance(1.0) 
      .hardness(1.0)  
      .model('eop:block/golden_idoll')
      .box(5, 0, 5, 11, 10, 11, true)
      .lightLevel(0.0)
      .defaultCutout()
  })

  StartupEvents.modifyCreativeTab('kubejs:tab', event => {
    event.remove('eop:golden_idol')
})