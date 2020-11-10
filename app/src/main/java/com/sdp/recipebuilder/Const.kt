package com.sdp.recipebuilder

object DeepLink {
    const val START = "/start"
    const val STOP = "/stop"
    const val ADD = "/add"

    object Params {
        const val ITEM_LIST_NAME = "itemListName"
        const val ITEM_LIST_ELEMENT_NAME = "itemListElementName"
        const val ITEM_TYPE = "itemListType"
    }

    object Actions {
        const val ACTION_TOKEN_EXTRA = "actions.fulfillment.extra.ACTION_TOKEN"
    }
}