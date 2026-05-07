StartupEvents.registry('block', event => {
    event.create('eop:polymorph_dice', 'cardinal')
      .displayName('Polymorph Dice') 
      .soundType('stone')
      .requiresTool(false)
      .resistance(1.0) 
      .hardness(1.0)  
      .model('eop:block/polymorph_dicee')
      .box(5, 0, 5, 11, 6, 11, true)
      .lightLevel(0.2)
      .defaultCutout()
  })

  StartupEvents.modifyCreativeTab('kubejs:tab', event => {
    event.remove('eop:polymorph_dice')
})