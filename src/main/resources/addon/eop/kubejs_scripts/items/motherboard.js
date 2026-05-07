StartupEvents.registry('block', event => {
    event.create('eop:motherboard', 'cardinal')
      .displayName('Motherboard') 
      .soundType('stone')
      .requiresTool(false)
      .resistance(1.0) 
      .hardness(1.0)  
      .model('eop:block/motherboardd')
      .box(3, 0, 3, 13, 11, 13, true)
      .lightLevel(0.0)
      .defaultCutout()
  })

  StartupEvents.modifyCreativeTab('kubejs:tab', event => {
    event.remove('eop:motherboard')
})