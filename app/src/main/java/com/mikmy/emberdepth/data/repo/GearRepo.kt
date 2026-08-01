package com.mikmy.emberdepth.data.repo

import com.mikmy.emberdepth.core.model.BigNum
import com.mikmy.emberdepth.core.model.Element
import com.mikmy.emberdepth.core.model.Gear
import com.mikmy.emberdepth.core.model.GearSlot
import com.mikmy.emberdepth.core.model.Rarity
import com.mikmy.emberdepth.core.model.StatBonus
import com.mikmy.emberdepth.core.model.StatType
import com.mikmy.emberdepth.data.db.GearDao
import com.mikmy.emberdepth.data.db.GearEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GearRepo @Inject constructor(private val dao: GearDao) {

    suspend fun getAll(): List<Gear> = dao.getAll().map { it.toDomain() }

    suspend fun getById(id: Long): Gear? = dao.getById(id)?.toDomain()

    suspend fun insert(gear: Gear): Long {
        return dao.insert(gear.toEntity())
    }

    suspend fun delete(id: Long) = dao.delete(id)

    suspend fun deleteAll() = dao.deleteAll()

    private fun GearEntity.toDomain() = Gear(
        id = id,
        slot = GearSlot.valueOf(slotType),
        rarity = Rarity.valueOf(rarity),
        level = level,
        primaryStat = parseStatBonus(primaryStat),
        secondaryStats = parseStatBonusList(secondary),
        element = element?.let { Element.valueOf(it) }
    )

    private fun Gear.toEntity() = GearEntity(
        id = id,
        slotType = slot.name,
        rarity = rarity.name,
        level = level,
        primaryStat = formatStatBonus(primaryStat),
        secondary = formatStatBonusList(secondaryStats),
        element = element?.name
    )

    private fun formatStatBonus(bonus: StatBonus): String =
        "${bonus.type.name}|${bonus.value.mantissa}:${bonus.value.exponent}"

    private fun parseStatBonus(s: String): StatBonus {
        val parts = s.split("|")
        val type = StatType.valueOf(parts[0])
        val numParts = parts[1].split(":")
        val value = BigNum(numParts[0].toDouble(), numParts[1].toInt())
        return StatBonus(type, value)
    }

    private fun formatStatBonusList(list: List<StatBonus>): String =
        list.joinToString(";") { formatStatBonus(it) }

    private fun parseStatBonusList(s: String): List<StatBonus> {
        if (s.isBlank()) return emptyList()
        return s.split(";").map { parseStatBonus(it) }
    }
}
