execute if score Gamerule EOP.Non.Destructive.Mode matches 0 run fill ~-2.5 ~ ~-2.5 ~2.5 ~ ~2.5 minecraft:fire replace minecraft:air
execute if score Gamerule EOP.Non.Destructive.Mode matches 0 run fill ~-2.5 ~ ~-2.5 ~2.5 ~ ~2.5 minecraft:fire replace minecraft:snow
execute if score Gamerule EOP.Non.Destructive.Mode matches 0 run fill ~2.5 ~ ~2.5 ~-2.5 ~ ~-2.5 minecraft:fire replace minecraft:snow_block
particle minecraft:explosion ~ ~ ~ 1 1 1 0 10
playsound minecraft:entity.generic.explode master @a ~ ~ ~ 2 1