StartupEvents.registry('block', event => {
    event.create('eop:ion_core', 'cardinal')
      .displayName('Ion Core') 
      .soundType('stone')
      .requiresTool(false)
      .resistance(1.0) 
      .hardness(1.0)  
      .model('eop:block/ion_coree')
      .box(3.5, 0, 3.5, 12.5, 9, 12.5, true)
      .lightLevel(0.6)
      .defaultCutout()
  })

  StartupEvents.modifyCreativeTab('kubejs:tab', event => {
    event.remove('eop:ion_core')
})