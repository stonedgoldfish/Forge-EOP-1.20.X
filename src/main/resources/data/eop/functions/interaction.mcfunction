execute unless entity @s[tag=EOP.Phasing] run execute as @e[type=interaction,tag=EOP.EmptyBody.HP,distance=..6] at @s run particle end_rod ~ ~1 ~ 0.3 0.3 0.3 0.2 20
execute unless entity @s[tag=EOP.Phasing] run execute as @e[type=interaction,tag=EOP.EmptyBody.HP,distance=..6] at @s run playsound minecraft:entity.generic.hurt master @a ~ ~ ~ 1 1
execute unless entity @s[tag=EOP.Phasing] run execute as @e[type=interaction,tag=EOP.EmptyBody.HP,distance=..6] at @s run scoreboard players remove @s EOP.EmptyBody.HP 1
advancement revoke @s only eop:interaction