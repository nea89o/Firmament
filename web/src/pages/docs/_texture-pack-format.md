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
just use a normal minecraft item model. See https://github.com/nea89o/BadSkyblockTP/blob/master/assets/firmskyblock/models/item/magma_rod.json
as an example. The id is first turned to lower case, then gets `:` replaced with `___`, `;` with `__` and all other 
characters that cannot be used in a minecraft resource location with `__XXXX` where `XXXX` is the 4 digit hex code for 
the character.

## (Placed) Skulls by texture id

Find the texture id of a skull. This is the hash part of an url like
`https://textures.minecraft.net/texture/bc8ea1f51f253ff5142ca11ae45193a4ad8c3ab5e9c6eec8ba7a4fcb7bac40` (so after the
/texture/). You can find it in game for placed skulls using the keybinding in the Power User Config. Then place the
replacement texture at `firmskyblock:textures/placedskulls/<thathash>.png`. Keep in mind that you will probably replace
the texture with another skin texture, meaning that skin texture has its own hash. Do not mix those up, you need to use
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

## Tint Overrides

Some items get naturally tinted by Minecraft's rendering. Examples include leather armour, spawn eggs, potions and more.
If you want to avoid your textures getting tinted, one thing you can do is use a higher texture layer:

```json
{
    "parent": "minecraft:item/generated",
    "textures": {
		// Notice the layer1 instead of layer0 here
        "layer1": "firmskyblock:item/regular_texture"
    }
}
```

Some items, however, tint *all* layers. For those items you can instead specify a tint override:

```json
{
    "parent": "minecraft:item/generated",
    "textures": {
        "layer0": "firmskyblock:item/regular_texture"
    },
    "firmament:tint_overrides": {
        "0": -1
    }
}
```

This forces layer 0 to be tinted with the color `-1` (pure white, aka no tint). This property is inherited, so if you
attach it to one of your root models that you `"parent"` other models to, all those models will have their tints
overridden. When the property is inherited, only layers specified in the child actually overwrite the parent layers.
You can use `"0": null` to remove the tint override in a child, which will cause a fallback to the vanilla tinting
behaviour.

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

#### Skulls

You can match skulls using the skull textures and other properties using the skull predicate. If there are no properties specified this is equivalent to checking if the item is a `minecraft:player_head`.

```json
"firmament:skull": {
	"profileId": "cca2d452-c6d3-39cb-b695-5ec92b2d6729",
	"textureProfileId": "1d5233d388624bafb00e3150a7aa3a89",
	"skinUrl": "http://textures.minecraft.net/texture/7bf01c198f6e16965e230235cd22a5a9f4a40e40941234478948ff9a56e51775",
	"textureValue": "ewogICJ0aW1lc3RhbXAiIDogMTYxODUyMTY2MzY1NCwKICAicHJvZmlsZUlkIiA6ICIxZDUyMzNkMzg4NjI0YmFmYjAwZTMxNTBhN2FhM2E4OSIsCiAgInByb2ZpbGVOYW1lIiA6ICIwMDAwMDAwMDAwMDAwMDBKIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdiZjAxYzE5OGY2ZTE2OTY1ZTIzMDIzNWNkMjJhNWE5ZjRhNDBlNDA5NDEyMzQ0Nzg5NDhmZjlhNTZlNTE3NzUiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ"
}
```

| Name               | Type                      | Description                                                                                                                                                                                                                                         |
|--------------------|---------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `profileId`        | UUID                      | Match the uuid of the profile component directly.                                                                                                                                                                                                   |
| `textureProfileId` | UUID                      | Match the uuid of the skin owner in the encoded texture value. This is more expensive, but can deviate from the profile id of the profile owner.                                                                                                    |
| `skinUrl`          | [string](#string-matcher) | Match the texture url of the skin. This starts with `http://`, not with `https:/` in most cases.                                                                                                                                                    |
| `textureValue`     | [string](#string-matcher) | Match the texture value. This is the encoded base64 string of the texture url along with metadata. It is faster to query than the `skinUrl`, but it can out of changed without causing any semantic changes, and is less readable than the skinUrl. |

#### Extra attributes

Filter by extra attribute NBT data:

Specify a `path` (using an [nbt prism](#nbt-prism)) to look at, separating sub elements with a `.`. You can use a `*` to check any child.

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

#### Components

You can match generic components similarly to [extra attributes](#extra-attributes). If you want to match an extra
attribute match directly using that, for better performance.

You can specify a `path` (using an [nbt prism](#nbt-prism)) and match similar to extra attributes, but in addition you can also specify a `component`. This
variable is the identifier of a component type that will then be encoded to nbt and matched according to the `match`
using a [nbt matcher](#nbt-matcher).

```json5
"firmament:component": {
    "path": "rgb",
	"component": "minecraft:dyed_color",
	"int": 255
}
// Alternatively
"firmament:component": {
	"path": "rgb",
	"component": "minecraft:dyed_color",
	"match": {
		"int": 255
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

### Nbt Prism

An nbt prism (or path) is used to specify where in a complex nbt construct to look for a value. A basic prism just looks
like a dot-separated path (`parent.child.grandchild`), but more complex paths can be constructed.

First the specified path is split into dot separated chunks: `"a.b.c"` -> `["a", "b", "c"]`. You can also directly
specify the list if you would like. Any entry in that list not starting with a `*` is treated as an attribute name or
an index:

```json
{
	"propA": {
		"propB": {
			"propC": 100,
			"propD": 1000
		}
	},
	"someOtherProp": "hello",
	"someThirdProp": "{\"innerProp\": true}",
	"someFourthProp": "aGlkZGVuIHZhbHVl"
}
```

In this example json (which is supposed to represent a corresponding nbt object), you can use a path like
`propA.propB.propC` to directly extract the value `100`.

If you want to extract all of the innermost values of `propB`
(for example if `propB` was an array instead), you could use `propA.propB.*`. You can use the `*` at any level:
`*.*.*` for example extracts all properties that are exactly at the third level. In that case you would try to match any
of the values of `[100, 1000]` to your match object.

Sometimes values are encoded in a non-nbt format inside a string. For those you can use other star based directives like
`*base64` or `*json` to decode those entries.

`*base64` turns a base64 encoded string into the base64 decoded counterpart. `*json` decodes a string into the json
object represented by that string. Note that json to nbt conversion isn't always straightforwards and the types can
end up being mangled (for example what could have been a byte ends up an int).

| Path                            | Result                          |
|---------------------------------|---------------------------------|
| `propA.propB`                   | `{"propC": 100, "propD": 1000}` |
| `propA.propB.propC`             | `100`                           |
| `propA.*.propC`                 | `100`                           |
| `propA.propB.*`                 | `100`, `1000`                   |
| `someOtherProp`                 | `"hello"`                       |
| `someThirdProp`                 | "{\"innerProp\": true}"         |
| `someThirdProp.*json`           | {"innerProp": true}             |
| `someThirdProp.*json.innerProp` | true                            |
| `someFourthProp`                | `"aGlkZGVuIHZhbHVl"`            |
| `someFourthProp.*base64`        | `"hidden value"`                |


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
ignoring the leather armor tint just one layer is enough.

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

## Screen Layout Replacement

You can change the layout of an entire screen by using screen layout overrides. These get placed in `firmskyblock:overrides/screen_layout/*.json`, with one file per screen. You can match on the title of a screen, the type of screen, replace the background texture (including extending the background canvas further than vanilla allows you) and move slots around.

### Selecting a screen

```json
{
	"predicates": {
		"label": {
			"regex": "Hyper Furnace"
		},
		"screenType": "minecraft:furnace"
	}
}
```

The `label` property is a regular [string matcher](#string-matcher) and matches against the screens title (typically the chest title, or "Crafting" for the players inventory).

The `screenType` property is an optional namespaced identifier that allows matching to a [screen type](https://minecraft.wiki/w/Java_Edition_protocol/Inventory#Types).

### Changing the background

```json
{
	"predicates": {
		"label": {
			"regex": "Hyper Furnace"
		}
	},
	"background": {
		"texture": "firmskyblock:textures/furnace.png",
		"x": -21,
		"y": -30,
		"width": 197,
		"height": 196
	}
}
```

You need to specify an x and y offset relative to where the regular screen would render. This means you just check where the upper left corner of the UI texture would be in your texture (and turn it into a negative number). You also need to specify a width and height of your texture. This is the width in pixels rendered. If you want a higher or lower resolution texture, you can scale the actual texture up (tho it is expected to meet the same aspect ratio as the one defined here).

### Moving slots around

```json
{
	"predicates": {
		"label": {
			"regex": "Hyper Furnace"
		}
	},
	"slots": [
		{
			"index": 10,
			"x": -5000,
			"y": -5000
		}
	]
}
```

You can move slots around by a specific index. This is not the index in the inventory, but rather the index in the screen (so if you have a chest screen then all the player inventory slots would be a higher index since the chest slots move them down the list). The x and y are relative to where the regular screen top left would be. Set to large values to effectively "delete" a slot by moving it offscreen.

### Moving text around

```json
{
	"predicates": {
		"label": {
			"regex": "Hyper Furnace"
		}
	},
	"playerTitle": {
		"x": 0,
		"y": 0,
		"align": "left",
		"replace": "a"
	}
}
```

You can move the window title around. The x and y are relative to the top left of the regular screen (like slots). Set to large values to effectively "delete" a slot by moving it offscreen.

The align only specifies the direction the text grows in, it does not the actual anchor point, so if you want right aligned text you will also need to move the origin of the text to the right (or it will just grow out of the left side of your screen).

You can replace the text with another text to render instead.

Available titles are

- `containerTitle` for the title of the open container, typically at the very top.
- `playerTitle` for the players inventory title. Note that in the player inventory without a chest or something open, the `containerTitle` is also used for the "Crafting" text.
- `repairCostTitle` for the repair cost label in anvils.

### Moving components around

```json
{
	"predicates": {
		"label": {
			"regex": "Hyper Furnace"
		}
	},
	"nameField": {
		"x": 10,
		"y": 10,
		"width": 100,
		"height": 12
	}
}
```

Some other components can also be moved. These components might be buttons, text inputs or other things not fitting into any category. They can have a x, y (relative to the top left of the screen), as well as sometimes a width, height, and other properties. This is more of a wild card category, and which options work depends on the type of object.

Available options

- `nameField`: x, y, width & height are all available to move the field to set the name of the item in an anvil.

### All together

| Field                     | Required | Description                                                                                                              |
|---------------------------|----------|--------------------------------------------------------------------------------------------------------------------------|
| `predicates`              | true     | A list of predicates that need to match in order to change the layout of a screen                                        |
| `predicates.label`        | true     | A [string matcher](#string-matcher) for the screen title                                                                 |
| `background`              | false    | Allows replacing the background texture                                                                                  |
| `background.texture`      | true     | The texture of the background as an identifier                                                                           |
| `background.x`            | true     | The x offset of the background relative to where the regular background would be rendered.                               |
| `background.y`            | true     | The y offset of the background relative to where the regular background would be rendered.                               |
| `background.width`        | true     | The width of the background texture.                                                                                     |
| `background.height`       | true     | The height of the background texture.                                                                                    |
| `slots`                   | false    | An array of slots to move around.                                                                                        |
| `slots[*].index`          | true     | The index in the array of all slots on the screen (not inventory).                                                       |
| `slots[*].x`              | true     | The x coordinate of the slot relative to the top left of the screen                                                      |
| `slots[*].y`              | true     | The y coordinate of the slot relative to the top left of the screen                                                      |
| `<element>Title`          | false    | The title mover (see above for valid options)                                                                            |
| `<element>Title.x`        | false    | The x coordinate of text relative to the top left of the screen                                                          |
| `<element>Title.y`        | false    | The y coordinate of text relative to the top left of the screen                                                          |
| `<element>Title.align`    | false    | How you want the text to align. "left", "center" or "right". This only changes the text direction, not its anchor point. |
| `<element>Title.replace`  | false    | Replace the text with your own text                                                                                      |
| `<extraComponent>`        | false    | Allows you to move button components and similar around                                                                  |
| `<extraComponent>.x`      | true     | The new x coordinate of the component relative to the top left of the screen                                             |
| `<extraComponent>.x`      | true     | The new y coordinate of the component relative to the top left of the screen                                             |
| `<extraComponent>.width`  | false    | The new width of the component                                                                                           |
| `<extraComponent>.height` | false    | The new height of the component                                                                                          |



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

The referenced `block` can either be a regular json block model (like the ones in `assets/minecraft/blocks/`), or it can
reference a blockstates json like in `assets/<namespace>/blockstates/<path>.json`. The blockstates.json is prefered and
needs to match the vanilla format, so it is best to copy over the vanilla blockstates.json for the block you are editing
and replace all block model paths with your own custom block models.

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
