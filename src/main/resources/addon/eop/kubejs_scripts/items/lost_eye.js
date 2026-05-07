StartupEvents.registry('block', event => {
    event.create('eop:lost_eye', 'cardinal')
      .displayName('Lost Eye') 
      .soundType('stone')
      .requiresTool(false)
      .resistance(1.0) 
      .hardness(1.0)  
      .model('eop:block/lost_eyee')
      .box(5, 0, 5, 11, 13, 11, true)
      .lightLevel(0.0)
      .defaultCutout()
  })

  StartupEvents.modifyCreativeTab('kubejs:tab', event => {
    event.remove('eop:lost_eye')
})