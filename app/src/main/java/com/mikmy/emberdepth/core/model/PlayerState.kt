package com.mikmy.emberdepth.core.model

data class PlayerState(
    val gold: BigNum = BigNum.ZERO,
    val ember: BigNum = BigNum.ZERO,
    val currentFloor: Int = 1,
    val highestFloor: Int = 1,
    val rebirthCount: Int = 0,
    val rebirthTier: Int = 0,
    val totalPlayTimeMs: Long = 0,
    val lastOnlineMs: Long = System.currentTimeMillis(),
    val tutorialFlags: Int = 0,
    val dailyLastMs: Long = 0
) {
    fun hasTutorialFlag(flag: Int): Boolean = (tutorialFlags and flag) != 0
    fun withTutorialFlag(flag: Int) = copy(tutorialFlags = tutorialFlags or flag)
}

object TutorialFlag {
    const val LEVELED_HERO = 1
    const val OPENED_FORGE = 2
    const val FIRST_BOSS = 4
    const val FIRST_REBIRTH = 8
    const val TUTORIAL_COMPLETE = 16
}
