StartupEvents.registry('item', event => {
  event.create('eop:concentrated_nether_star')
    .rarity('epic')
    .displayName('Concentrated Nether Star') 
    .maxStackSize(1)
    .glow(true)
    .modelJson({
      parent: 'eop:item/cconcentrated_nether_star'
    })
})

StartupEvents.modifyCreativeTab('kubejs:tab', event => {
    event.remove('eop:dna_capsule')
})