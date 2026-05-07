StartupEvents.registry('block', event => {
    event.create('eop:mask_of_madness', 'cardinal')
      .displayName('Mask of Madness') 
      .soundType('stone')
      .requiresTool(false)
      .resistance(1.0) 
      .hardness(1.0)  
      .model('eop:block/mask_of_madnesss')
      .box(1, 0, 4, 15, 14, 6, true)
      .lightLevel(0.0)
      .defaultCutout()
  })

  StartupEvents.modifyCreativeTab('kubejs:tab', event => {
    event.remove('eop:mask_of_madness')
})