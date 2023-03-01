package com.extole.android.sdk

/**
 * Campaign is representing the Extole Campaign which consumer is interacting with
 */
interface Campaign : Extole {

    /**
     * @return [String] - campaign current program label
     */
    fun getProgramLabel(): String

    /**
     * @return [Id]<[Campaign]> - Id of the current campaign
     */
    fun getId(): Id<Campaign>
}
