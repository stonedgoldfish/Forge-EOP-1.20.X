StartupEvents.registry('block', event => {
    event.create('eop:hourglass', 'cardinal')
      .displayName('Hourglass') 
      .soundType('stone')
      .requiresTool(false)
      .resistance(1.0) 
      .hardness(1.0)  
      .model('eop:block/hourglasss')
      .box(4.4, 0, 4.7, 11.7, 12, 12, true)
      .lightLevel(0.0)
      .defaultCutout()
  })

  StartupEvents.modifyCreativeTab('kubejs:tab', event => {
    event.remove('eop:hourglass')
})