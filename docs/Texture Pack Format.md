<!--
SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>

SPDX-License-Identifier: CC0-1.0
-->

# Custom SkyBlock Items Texture Pack Format

## Items by internal id (ExtraAttributes)

Find the internal id of the item. This is usually stored in the ExtraAttributes tag (Check the Power User Config for 
keybinds). Once you found it, create an item model in a resource pack like you would for
a vanilla item model, but at the coordinate `firmskyblock:<internalid>`. So for an aspect of the end, this would be 
`firmskyblock:models/item/aspect_of_the_end.json` (or `assets/firmskyblock/models/item/aspect_of_the_end.json`). Then,
just use a normal minecraft item model. See https://github.com/romangraef/BadSkyblockTP/blob/master/assets/firmskyblock/models/item/magma_rod.json
as an example.

## (Placed) Skulls by texture id

Find the texture id of a skull. This is the hash part of an url like
`https://textures.minecraft.net/texture/bc8ea1f51f253ff5142ca11ae45193a4ad8c3ab5e9c6eec8ba7a4fcb7bac40` (so after the
/texture/). You can find it in game for placed skulls using the keybinding in the Power User Config. Then place the
replacement texture at `firmskyblock:textures/placedskulls/<thathash>.png`. Keep in mind that you will probably replace
the texture with another skin texture, meaning that skin texture has it's own hash. Do not mix those up, you need to use
the hash of the old skin.

## Armor Skull Models

You can replace the models of skull items (or other items) by specifying the `firmament:head_model` property on your
model. Note that this is resolved *after* all [overrides](#predicates) and further predicates are not resolved on the
head model.

```json5
{
    "parent": "minecraft:item/generated",
    "textures": {
        "layer0": "firmskyblock:item/regular_texture"
    },
    "firmament:head_model": "minecraft:block/diamond_block" // when wearing on the head render a diamond block instead (can be any item model, including custom ones)
}
```

## Predicates

Firmament adds the ability for more complex [item model predicates](https://minecraft.wiki/w/Tutorials/Models#Item_predicates).
Those predicates work on any model, including models for vanilla items, but they don't mix very well with vanilla model overrides.
Vanilla predicates only ever get parsed at the top level, so including a vanilla predicate inside of a more complex
firmament parser will result in an ignored predicate.

### Example usage

```json
{
    "parent": "minecraft:item/handheld",
    "textures": {
        "layer0": "firmskyblock:item/bat_wand"
    },
    "overrides": [
        {
            "predicate": {
                "firmament:display_name": {
                    "regex": ".*§d.*",
                    "color": "preserve"
                }
            },
            "model": "firmskyblock:item/recombobulated_bat_wand"
        }
    ]
}
```

You specify an override like normally, with a `model` that will replace the current model and a list of `predicate`s
that must match before that override takes place.

At the top level `predicate` you can still use all the normal vanilla predicates, as well as the custom ones, which are
all prefixed with `firmament:`.

#### Display Name

Matches the display name against a [string matcher](#string-matcher)

```json
"firmament:display_name": "Display Name Test"
```

#### Lore

Tries to find at least one lore line that matches the given [string matcher](#string-matcher).

```json
"firmament:lore": {
  "regex": "Mode: Red Mushrooms",
  "color": "strip"
}
```

#### Item type

Filter by item type:

```json
"firmament:item": "minecraft:clock"
```

#### Extra attributes

Filter by extra attribute NBT data:

Specify a `path` to look at, separating sub elements with a `.`. You can use a `*` to check any child.

Then either specify a `match` sub-object or directly inline that object in the format of an [nbt matcher](#nbt-matcher).

Inlined match:

```json5
"firmament:extra_attributes": {
    "path": "gems.JADE_0",
    "string": "PERFECT"
}
```

Sub object match:

```json5
"firmament:extra_attributes": {
    "path": "gems.JADE_0",
    "match": {
        "string": "PERFECT"
    }    
}
```

#### Pet Data

Filter by pet information. While you can already filter by the skyblock id for pet type and tier, this allows you to
further filter by level and some other pet info.

```json5
"firmament:pet" {
    "id": "WOLF",
    "exp": ">=25353230",
    "tier": "[RARE,LEGENDARY]",
    "level": "[50,)",
    "candyUsed": 0
}
```

| Name        | Type                                                                   | Description                                                                                                                          |
|-------------|------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| `id`        | [String](#string-matcher)                                              | The id of the pet                                                                                                                    |
| `exp`       | [Number](#number-matcher)                                              | The total experience of the pet                                                                                                      |
| `tier`      | Rarity (like [Number](#number-matcher), but with rarity names instead) | The total experience of the pet                                                                                                      |
| `level`     | [Number](#number-matcher)                                              | The current level of the pet                                                                                                         |
| `candyUsed` | [Number](#number-matcher)                                              | The number of pet candies used on the pet. This is present even if they are not shown in game (such as on a level 100 legendary pet) |

Every part of this matcher is optional.


#### Logic Operators

Logic operators allow to combine other firmament predicates into one. This is done by building boolean operators:

```json5
"firmament:any": [
  {
    "firmament:display_name": "SkyBlock Menu (Click)"
  },
  {
    "firmament:display_name": "SkyBlock",
    "firmament:lore": "Some Lore Requirement"
  }    
]
```

This `firmament:any` test if the display name is either "SkyBlock Menu (Click)" or "SkyBlock" (aka any of the child predicates match).

Similarly, there is `firmament:all`, which requires all of its children to match.

There is also `firmament:not`, which requires none of its children to match. Unlike `any` or `all`, however, `not`
only takes in one predicate `{}` directly, not an array of predicates `[{}]`.

Note also that by default all predicate dictionaries require all predicates in it to match, so you can imagine that all
things are wrapped in an implicit `firmament:all` element.

### String Matcher

A string matcher allows you to match almost any string. Whenever a string matcher is expected, you can use any of these
styles of creating one.

#### Direct

```json
"firmament:display_name": "Test"
```

Directly specifying a raw string value expects the string to be *exactly* equal, after removing all formatting codes.

#### Complex

A complex string matcher allows you to specify whether the string will get its color codes removed or not before matching


```json5
"firmament:display_name": {
  "color": "strip",
  "color": "preserve", 
  // When omitting the color property alltogether, you will fall back to "strip"
}
```
In that same object you can then also specify how the string will be matched using another property. You can only ever
specify one of these other matchers and one color preserving property.

```json5
"firmament:display_name": {
  "color": "strip",
  // You can use a "regex" property to use a java.util.Pattern regex. It will try to match the entire string.
  "regex": "So[me] Regex",
  // You can use an "equals" property to test if the entire string is equal to some value. 
  // Equals is faster than regex, but also more limited.  
  "equals": "Some Text"    
}
```

### Number Matchers

This matches a number against either a range or a specific number.

#### Direct number

You can directly specify a number using that value directly:
```json5
"firmament:pet": {
    "level": 100
}
```

This is best for whole numbers, since decimal numbers can be really close together but still be different.

#### Intervals

For ranges you can instead use an interval. This uses the standard mathematical notation for those as a string:


```json5
"firmament:pet": {
    "level": "(50,100]"
}
```

This is in the format of `(min,max)` or `[min,max]`. Either min or max can be omitted, which results in that boundary
being ignored (so `[50,)` would be 50 until infinity). You can also vary the parenthesis on either side independently.

Specifying round parenthesis `()` means the number is exclusive, so not including this number. For example `(50,100)`
would not match just the number `50` or `100`, but would match `51`.

Specifying square brackets `[]` means the number is inclusive. For example `[50,100]` would match both `50` and `100`.

You can mix and match parenthesis and brackets, they only ever affect the number next to it.

For more information in intervals check out [Wikipedia](https://en.wikipedia.org/wiki/Interval_(mathematics)).

#### Operators

If instead of specifying a range you just need to specify one boundary you can also use the standard operators to 
compare your number:

```json5
"firmament:pet": {
    "level": "<50"
}
```

This example would match if the level is less than fifty. The available operators are `<`, `>`, `<=` and `>=`. The
operator needs to be specified on the left. The versions of the operator with `=` also allow the number to be equal.

### Nbt Matcher

This matches a single nbt element.

Have the type of the nbt element as json key. Can be `string`, `int`, `float`, `double`, `long`, `short` and `byte`.

The `string` type matches like a regular [string matcher](#string-matcher):

```json
"string": {
    "color": "strip",
    "regex": "^aaa bbb$"
}
```

The other (numeric) types can either be matched directly against a number:

```json
"int": 10
```

Or as a range:

```json
"long": {
    "min": 0,
    "max": 1000
}
```

Min and max are both optional, but you need to specify at least one. By default `min` is inclusive and `max` is exclusive.
You can override that like so:

```json
"short": {
    "min": 0,
    "max": 1000,
    "minExclusive": true,
    "maxExclusive": false
}
```


> [!WARNING]
> This syntax for numbers is *just* for **NBT values**. This is also why specifying the type of the number is necessary.
> For other number matchers, use [the number matchers](#number-matchers)

## Armor textures

You can re-*texture* armors, but not re-*model* them with firmament. 

To retexture a piece of armor place a json file at `assets/firmskyblock/overrides/armor_models/*.json`.

```json
{
    "item_ids": [
        "TARANTULA_BOOTS",
        "TARANTULA_LEGGINGS",
        // ETC
    ],
    "layers": [
        {
            "identifier": "firmskyblock:tarantula"
        }
    ]
}
```

Only one such file can exist per item id, but multiple item ids can share one texture file this way.

The `item_ids` is the items to which this override will apply when worn. Those are neu repo ids (so what will be shown
in game as the regular SkyBlock id, not the resource pack identifier).

### Layers

The `layers` specify the multiple texture layers that will be used when rendering. For non leather armor, or armor
ignoring the leather armor ting just one layer is enough.

If you want to apply armor tint to the texture you will usually want two layers. The first layer has a tint applied:

```json
{
    "identifier": "firmskyblock:angler",
    "tint": true
}
```

This will tint the texture before it is being rendered.

The second layer will have no tint applied, but will have a suffix:

```json
{
    "identifier": "firmskyblock:angler",
    "suffix": "_overlay"
}
```

This second layer is used for the countours of the armor.

The layer identifier will resolve to a texture file path according to vanilla armor texture rules like so:

`assets/{identifier.namespace}/textures/models/armor/{identifier.path}_layer_{isLegs ? 2 : 1}{suffix}.png`

Note that there is no automatic underscore insertion for suffix, so you will need to manually specify it if you want.

The leg armor piece uses a different texture, same as with vanilla.

### Overrides

You can also apply overrides to these layers. These work similar to item predicate overrides, but only the custom
Firmament predicates will work. You will also just directly specify new layers instead of delegating to another file.

```json
{
    "item_ids": [
        "TARANTULA_BOOTS",
        "TARANTULA_LEGGINGS",
        // ETC
    ],
    "layers": [
        {
            "identifier": "firmskyblock:tarantula"
        }
    ],
    "overrides": [
        {
            "layers": [
                {
                    "identifier": "firmskyblock:tarantula_maxed"
                }
            ],
            "predicate": {
                "firmament:lore": {
                    "regex": "Piece Bonus: +285.*"
                }
            }
        }
    ]
}
```

## UI Text Color Replacement

This allows you to replace the color of text in your inventory. This includes inventory UIs like chests and anvils, but
not screens from other mods. You can also target specific texts via a [string matcher](#string-matcher).

```json
// This file is at assets/firmskyblock/overrides/text_colors.json
{
	"defaultColor": -10496,
	"overrides": [
		{
			"predicate": "Crafting",
			"override": -16711936
		}
	]
}
```

| Field                 | Required | Description                                                                                        |
|-----------------------|----------|----------------------------------------------------------------------------------------------------|
| `defaultColor`        | true     | The default color to use in case no override matches                                               |
| `overrides`           | false    | Allows you to replace colors for specific strings. Is an array.                                    |
| `overrides.predicate` | true     | This is a [string matcher](#string-matcher) that allows you to match on the text you are replacing |
| `overrides.override`  | true     | This is the replacement color that will be used if the predicate matches.                          |

## Global Item Texture Replacement

Most texture replacement is done based on the SkyBlock id of the item. However, some items you might want to re-texture
do not have an id. The next best alternative you had before was just to replace the vanilla item and add a bunch of
predicates. This tries to fix this problem, at the cost of being more performance intensive than the other re-texturing 
methods.

The entrypoint to global overrides is `firmskyblock:overrides/item`. Put your overrides into that folder, with one file
per override.

```json5
{
    "screen": "testrp:chocolate_factory",
    "model": "testrp:time_tower",
    "predicate": {
        "firmament:display_name": {
            "regex": "Time Tower.*"
        }
    }
}
```

There are three parts to the override.

The `model` is an *item id* that the item will be replaced with. This means the
model will be loaded from `assets/<namespace>/models/item/<id>.json`.  Make sure to use your own namespace to
avoid collisions with other texture packs that might use the same id for a gui.

The `predicate` is just a normal [predicate](#predicates). This one does not support the vanilla predicates. You can
still use vanilla predicates in the resolved model, but this will not allow you to fall back to other global overrides.

The `screen` specifies which screens your override will work on. This is purely for performance reasons, your filter
should work purely based on predicates if possible. You can specify multiply screens by using a json array.

### Global item texture Screens

In order to improve performance not all overrides are tested all the time. Instead you can prefilter by the screen that
is open. First the gui is resolved to `assets/<namespace>/filters/screen/<id>.json`. Make sure to use your own namespace
to avoid collisions with other texture packs that might use the same id for a screen.

```json
{
    "title": "Chocolate Factory"
}
```

Currently, the only supported filter is `title`, which accepts a [string matcher](#string-matcher). You can also use
`firmament:always` as an always on filter (this is the recommended way).

## Block Model Replacements

Firmament adds the ability to retexture block models. Supported renderers are vanilla, indigo (fabric), sodium (and 
anything sodium based). Firmament performs gentle world reloading so that even when the world data gets updated very
late by the server there should be no flicker.

If you want to replace block textures in the world you can do so using block overrides. Those are stored in 
`assets/firmskyblock/overrides/blocks/<id>.json`. The id does not matter, all overrides are loaded. This file specifies
which block models are replaced under which conditions:

```json
{
    "modes": [
        "mining_3"
    ],
    "area": [
        {
            "min": [
                -31,
                200,
                -117
            ],
            "max": [
                12,
                223,
                -95
            ]
        }
    ],
    "replacements": {
        "minecraft:blue_wool": "firmskyblock:mithril_deep",
        "minecraft:light_blue_wool": {
            "block": "firmskyblock:mithril_deep",
            "sound": "minecraft:block.wet_sponge.hit"
        }
    }
}
```

| Field                   | Required | Description                                                                                                                                                                                                                                                     |
|-------------------------|----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `modes`                 | yes      | A list of `/locraw` mode names.                                                                                                                                                                                                                                 |
| `area`                  | no       | A list of areas. Blocks outside of the coordinate range will be ignored. If the block is in *any* range it will be considered inside                                                                                                                            |
| `area.min`              | yes      | The lowest coordinate in the area. Is included in the area.                                                                                                                                                                                                     |
| `area.max`              | yes      | The highest coordinate in the area. Is included in the area.                                                                                                                                                                                                    |
| `replacements`          | yes      | A map of block id to replacement mappings                                                                                                                                                                                                                       |
| `replacements` (string) | yes      | You can directly specify a string. Equivalent to just setting `replacements.block`.                                                                                                                                                                             |
| `replacements.block`    | yes      | You can specify a block model to be used instead of the regular one. The model will be loaded from `assets/<namespace>/models/block/<path>.json` like regular block models.                                                                                     |
| `replacements.sound`    | no       | You can also specify a sound override. This is only used for the "hit" sound effect that repeats while the block is mined. The "break" sound effect played after a block was finished mining is sadly sent by hypixel directly and cannot be replaced reliably. |

> A quick note about optimization: Not specifying an area (by just omitting the `area` field) is quicker than having an
> area encompass the entire map.
> 
> If you need to use multiple `area`s for unrelated sections of the world it might be a performance improvement to move
> unrelated models to different files to reduce the amount of area checks being done for each block.
