<!--
SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>

SPDX-License-Identifier: CC0-1.0
-->

# Technical Notes for the texture pack implementation

Relevant classes:

`ItemModelManager` can be used to select an `ItemModel`. This is done from the `ITEM_MODEL` component which is defaulted by the `Item` class.

The list of available `ItemModel`s (as in `Identifier` -> `ItemModel` maps) is loaded by `BakedModelManager`. To this end, item models in particular are loaded from `ItemAssetsLoader#load`. Those `ItemAssets` are found in `assets/<ns>/items/` directly (not in the model folder) and can be used to select other models, similar to how predicates used to work
