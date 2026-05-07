StartupEvents.registry('block', event => {
    event.create('eop:genie_lamp', 'cardinal')
      .displayName('Genie Lamp') 
      .soundType('stone')
      .requiresTool(false)
      .resistance(1.0) 
      .hardness(1.0)  
      .model('eop:block/genie_lampp')
      .box(5.5, 0, -1, 10.5, 7, 16, true)
      .lightLevel(0.0)
      .defaultCutout()
  })

  StartupEvents.modifyCreativeTab('kubejs:tab', event => {
    event.remove('eop:genie_lamp')
})