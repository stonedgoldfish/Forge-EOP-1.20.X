StartupEvents.registry('block', event => {
    event.create('eop:jar_of_eyes', 'cardinal')
      .displayName('Jar of Eyes') 
      .soundType('stone')
      .requiresTool(false)
      .resistance(1.0) 
      .hardness(1.0)  
      .model('eop:block/jar_of_eyess')
      .box(3, 0, 4, 13, 16, 14, true)
      .lightLevel(0.0)
      .defaultCutout()
  })

  StartupEvents.modifyCreativeTab('kubejs:tab', event => {
    event.remove('eop:jar_of_eyes')
})