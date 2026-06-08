execute if score Gamerule EOP.Non.Destructive.Mode matches 0 run fill ~-0.5 ~ ~-0.5 ~0.5 ~ ~0.5 minecraft:fire replace minecraft:air
execute if score Gamerule EOP.Non.Destructive.Mode matches 0 run fill ~-0.5 ~ ~-0.5 ~0.5 ~ ~0.5 minecraft:fire replace minecraft:snow
execute if score Gamerule EOP.Non.Destructive.Mode matches 0 run fill ~0.5 ~ ~0.5 ~-0.5 ~ ~-0.5 minecraft:fire replace minecraft:snow_block
particle minecraft:explosion ~ ~ ~ 0 0 0 0 1
playsound minecraft:entity.generic.explode master @a ~ ~ ~ 1 1