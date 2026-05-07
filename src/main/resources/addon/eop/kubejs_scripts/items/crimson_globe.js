StartupEvents.registry('block', event => {
    event.create('eop:crimson_globe', 'cardinal')
      .displayName('§dCrimson Globe') 
      .soundType('stone')
      .requiresTool(false)
      .resistance(1.0) 
      .hardness(1.0)  
      .model('eop:block/crimsonn_globe')
      .box(6, 0, 6, 10, 6, 10, true)
      .lightLevel(0.4)
      .defaultCutout()
  })

  StartupEvents.modifyCreativeTab('kubejs:tab', event => {
    event.remove('eop:crimson_globe')
})