StartupEvents.registry('item', event => {
  event.create('eop:dna_capsule')
    .rarity('common')
    .displayName('DNA Capsule') 
    .maxStackSize(1)
    .maxDamage(3)
    .modelJson({
      parent: 'eop:item/filled_dna_capsule'
    })
})

StartupEvents.modifyCreativeTab('kubejs:tab', event => {
    event.remove('eop:dna_capsule')
})