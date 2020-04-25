/*
 *
 *  * Copyright (C) 2017-2020 Daniel Saukel, Malfrador
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.erethon.factionsxl.config;

import de.erethon.commons.config.Message;
import de.erethon.commons.config.MessageHandler;
import de.erethon.factionsxl.FactionsXL;

/**
 * An enumeration of all messages.
 * The values are fetched from the language file.
 *
 * @author Daniel Saukel
 */
public enum FMessage implements Message {

    BULL_ID("bull.id"),
    BULL_RIGHT_KLICK("bull.rightClick"),
    CHAT_CHANNEL_SWITCHED("chat.channelSwitched"),
    CHAT_PREFIX_ADMIN("chat.prefix.admin"),
    CHAT_PREFIX_MEMBER("chat.prefix.member"),
    CHAT_PREFIX_MOD("chat.prefix.mod"),
    CHAT_PUBLIC_DISABLED("chat.publicDisabled"),
    CMD_ADD_CASUS_BELLI_SUCCESS("cmd.addCasusBelli.success"),
    CMD_ADMIN_SUCCESS("cmd.admin.success"),
    CMD_CASUS_BELLI_HEAD("cmd.casusBelli.head"),
    CMD_CASUS_BELLI_NO_EXPIRATION("cmd.casusBelli.noExpiration"),
    CMD_CHATSPY_HELP("cmd.chatSpy.help"),
    CMD_CHATSPY_ON("cmd.chatSpy.on"),
    CMD_CHATSPY_OFF("cmd.chatSpy.off"),
    CMD_CLAIM_SUCCESS("cmd.claim.success"),
    CMD_CREATE_SUCCESS("cmd.create.success"),
    CMD_CREATE_BULL_SUCCESS("cmd.createBull.success"),
    CMD_DESC_SUCCESS("cmd.desc.success"),
    CMD_DISBAND_SUCCESS("cmd.disband.success"),
    CMD_GIVE_REGION_SUCCESS("cmd.giveRegion.success"),
    CMD_INVITE_FAIL("cmd.invite.fail"),
    CMD_INVITE_SUCCESS("cmd.invite.success"),
    CMD_INDEPENDENCE_HELP("cmd.independence.help"),
    CMD_INDEPENDENCE_VASSAL("cmd.independence.success.vassal"),
    CMD_INDEPENDENCE_NEWFACTION("cmd.independence.success.newFaction"),
    CMD_KICK_FAIL("cmd.kick.fail"),
    CMD_KICK_SUCCESS("cmd.kick.success"),
    CMD_LIST_TITLE("cmd.list.title"),
    CMD_MAIN_WELCOME("cmd.main.welcome"),
    CMD_MAIN_HELP("cmd.main.help"),
    CMD_MOD_PROMOTE("cmd.mod.promote"),
    CMD_MOD_DEMOTE("cmd.mod.demote"),
    CMD_MONEY_BALANCE("cmd.money.balance"),
    CMD_MONEY_DEPOSIT_FAIL("cmd.money.deposit.fail"),
    CMD_MONEY_DEPOSIT_SUCCESS("cmd.money.deposit.success"),
    CMD_MONEY_WITHDRAW_FAIL("cmd.money.withdraw.fail"),
    CMD_MONEY_WITHDRAW_SUCCESS("cmd.money.withdraw.success"),
    CMD_OPEN_CLOSED("cmd.open.closed"),
    CMD_OPEN_OPENED("cmd.open.opened"),
    CMD_POWER("cmd.power"),
    CMD_PEACE_LIST_SENT("cmd.peace.listSent"),
    CMD_PEACE_LIST_RECEIVED("cmd.peace.listReceived"),
    CMD_PEACE_CREATE("cmd.peace.create"),
    CMD_PEACE_TITLE("cmd.peace.title"),
    CMD_PEACE_ADDDEMAND("cmd.peace.add.demand"),
    CMD_PEACE_ADDOFFER("cmd.peace.add.offer"),
    CMD_PEACE_CONFIRM_HELP("cmd.peaceConfirm.help"),
    CMD_PEACE_CONFIRM_NOT_IN_WAR("cmd.peaceConfirm.notInWar"),
    CMD_PEACE_CONFIRM_REJECTED_FACTION("cmd.peaceConfirm.rejected.faction"),
    CMD_PEACE_CONFIRM_REJECTED_WARPARTY("cmd.peaceConfirm.rejected.warParty"),
    CMD_PEACE_CONFIRM_SUCCESS("cmd.peaceConfirm.success"),
    CMD_PEACE_CONFIRM_NOT_LEADER("cmd.peaceConfirm.notLeader"),
    CMD_PEACE_CONFIRM_EMPTY("cmd.peaceConfirm.empty"),
    CMD_PEACE_CONFIRM_CANTAFFORD("cmd.peaceConfirm.cantAfford"),
    CMD_REGION_CLAIMS("cmd.region.claims"),
    CMD_REGION_CORES("cmd.region.cores"),
    CMD_REGION_OWNER("cmd.region.owner"),
    CMD_REGION_POPULATION("cmd.region.population"),
    CMD_REGION_PRICE("cmd.region.price"),
    CMD_REGION_PRICE_CORE("cmd.region.priceCore"),
    CMD_REGION_TYPE("cmd.region.type"),
    CMD_REGION_OCCUPIER("cmd.region.occupier"),
    CMD_REGION_INFLUENCE("cmd.region.influence"),
    CMD_RELOAD_DONE("cmd.reload.done"),
    CMD_REQUESTS_TITLE("cmd.requests.title"),
    CMD_SET_ANTHEM_SUCCESS("cmd.setAnthem.success"),
    CMD_SET_BANNER_SUCCESS("cmd.setBanner.success"),
    CMD_SET_CAPITAL_SUCCESS("cmd.setCapital.success"),
    CMD_SET_COLOR_SUCCESS("cmd.setColor.success"),
    CMD_SET_GOVERNMENT_SUCCESS("cmd.setGovernment.success"),
    CMD_SET_HOME_CHAR_FAIL("cmd.setHome.char.fail"),
    CMD_SET_HOME_CHAR_SUCCESS("cmd.setHome.char.success"),
    CMD_SET_HOME_FACTION_FAIL("cmd.setHome.faction.fail"),
    CMD_SET_HOME_FACTION_SUCCESS("cmd.setHome.faction.success"),
    CMD_SET_POWER_SENDER("cmd.setPower.sender"),
    CMD_SET_POWER_TARGET("cmd.setPower.target"),
    CMD_SHOW_BALANCE("cmd.show.balance"),
    CMD_SHOW_CAPITAL("cmd.show.capital"),
    CMD_SHOW_DESCRIPTION("cmd.show.description"),
    CMD_SHOW_FORMER_LEADERS("cmd.show.formerLeaders"),
    CMD_SHOW_GOVERNMENT_TYPE("cmd.show.governmentType"),
    CMD_SHOW_INFO("cmd.show.info"),
    CMD_SHOW_INVITATION("cmd.show.invitation"),
    CMD_SHOW_LEADER("cmd.show.leader"),
    CMD_SHOW_MEMBERS("cmd.show.members"),
    CMD_SHOW_RELATIONS("cmd.show.relations"),
    CMD_SHOW_STABILITY("cmd.show.stability"),
    CMD_SHOW_STABILITY_MOD_BASE("cmd.show.stabilityMod.base"),
    CMD_SHOW_STABILITY_MOD_EXHAUSTION("cmd.show.stabilityMod.exhaustion"),
    CMD_SHOW_STABILITY_MOD_PROVINCES("cmd.show.stabilityMod.provinces"),
    CMD_SHOW_STABILITY_MOD_ABSENT_MONARCH("cmd.show.stabilityMod.absentMonarch"),
    CMD_SHOW_STABILITY_MOD_POWER("cmd.show.stabilityMod.power"),
    CMD_SHOW_STABILITY_MOD_WEALTH("cmd.show.stabilityMod.wealth"),
    CMD_SHOW_TAG("cmd.show.tag"),
    CMD_TAG_SUCCESS("cmd.tag.success"),
    CMD_TITLE_SUCCESS("cmd.title.success"),
    CMD_TOGGLEPUBLIC_HELP("cmd.togglePublic.help"),
    CMD_TOGGLEPUBLIC_ON("cmd.togglePublic.on"),
    CMD_TOGGLEPUBLIC_OFF("cmd.togglePublic.off"),
    CMD_UNCLAIM_FAIL("cmd.unclaim.fail"),
    CMD_UNCLAIM_SUCCESS("cmd.unclaim.success"),
    CMD_UNINVITE_SUCCESS("cmd.uninvite.success"),
    CMD_WAR_STATUS_ATTACKERS("cmd.warStatus.attackers"),
    CMD_WAR_STATUS_CASUS_BELLI("cmd.warStatus.casusBelli"),
    CMD_WAR_STATUS_DATE("cmd.warStatus.date"),
    CMD_WAR_STATUS_DEATHS("cmd.warStatus.deaths"),
    CMD_WAR_STATUS_DEFENDERS("cmd.warStatus.deFfenders"),
    CMD_WAR_STATUS_HEADLINE("cmd.warStatus.headline"),
    CMD_WAR_STATUS_KD("cmd.warStatus.kd"),
    CMD_WAR_STATUS_KILLS_AND_DEATHS("cmd.warStatus.killsAndDeaths"),
    CMD_WAR_STATUS_NO_WARS("cmd.warStatus.noWars"),
    CMD_WAR_STATUS_POINTS("cmd.warStatus.points"),
    CMD_WAR_STATUS_KILLPOINTS("cmd.warStatus.killpoints"),
    CMD_WAR_SELECTCB("cmd.war.selectCB"),
    CMD_WORLD_AUTOCLAIM_END("cmd.world.autoclaim.end"),
    CMD_WORLD_AUTOCLAIM_START("cmd.world.autoclaim.start"),
    CMD_WORLD_CHUNK_ADDED("cmd.world.chunkAdded"),
    CMD_WORLD_CHUNK_REMOVED("cmd.world.chunkRemoved"),
    CMD_WORLD_CREATE("cmd.world.create"),
    CMD_WORLD_DELETE("cmd.world.delete"),
    CMD_WORLD_LEVEL("cmd.world.level"),
    CMD_WORLD_RENAMED("cmd.world.renamed"),
    CMD_WORLD_TYPE("cmd.world.type"),
    DEATH_DEFAULT_DEATH("death.default.killed"),
    DEATH_PLAYER_KILL_KILLED("death.playerKill.killed"),
    DEATH_PLAYER_KILL_KILLER("death.playerKill.killer"),
    ERROR_ALLOD("error.allod"),
    ERROR_AT_WAR("error.atWar"),
    ERROR_CANNOT_ATTACK_ALLIED_FACTION("error.cannotAttackAlliedFaction"),
    ERROR_CANNOT_PASS_CAPITAL("cmd.cannotPass.capital"),
    ERROR_CANNOT_PASS_LAND("cmd.cannotPass.land"),
    ERROR_CANNOT_TRADE_WITH_ITSELF("error.cannotTradeWithItself"),
    ERROR_CAPITAL_MOVE_COOLDOWN("error.capitalMoveCooldown"),
    ERROR_CMD_NOT_EXIST_1("error.cmdNotExist.1"),
    ERROR_CMD_NOT_EXIST_2("error.cmdNotExist.2"),
    ERROR_DO_NOT_MOVE("error.doNotMove"),
    ERROR_ECON_DISABLED("error.econDisabled"),
    ERROR_HOME_NOT_IN_ALLIED_TERRITORY("error.homeNotInAlliedTerritory"),
    ERROR_IN_WAR("error.inWar"),
    ERROR_JOIN_FACTION("error.joinFaction"),
    ERROR_LAND_NO_CORE("error.langNoCore"),
    ERROR_LAND_NOT_FOR_SALE("error.land.notForSale"),
    ERROR_LAND_WILDERNESS("error.land.wilderness"),
    ERROR_LAND_NOT_CONNECTED("error.land.notConnected"),
    ERROR_LAND_NOT_OWNED("error.land.notOwned"),
    ERROR_LEAVE_FACTION("error.leaveFaction"),
    ERROR_MAX_IDEA_GROUPS_REACHED("error.maxIdeaGroupsReached"),
    ERROR_NAME_IN_USE("error.nameInUse"),
    ERROR_NO_FACTIONS("error.noFactions"),
    ERROR_NO_PERMISSION("error.noPermission"),
    ERROR_NO_SUCH_CASUS_BELLI("error.noSuch.casusBelli"),
    ERROR_NO_SUCH_FACTION("error.noSuch.faction"),
    ERROR_NO_SUCH_GOVERNMENT_TYPE("error.noSuch.governmentType"),
    ERROR_NO_SUCH_PLAYER("error.noSuch.player"),
    ERROR_NO_SUCH_REGION("error.noSuch.region"),
    ERROR_NO_SUCH_REGION_TYPE("error.noSuch.regionType"),
    ERROR_NO_SUCH_RELATION("error.noSuch.relation"),
    ERROR_NOT_ENOUGH_MONEY("error.notEnoughMoney"),
    ERROR_NOT_ENOUGH_MONEY_FACTION("error.notEnoughMoneyFaction"),
    ERROR_NOT_NUMERIC("error.notNumeric"),
    ERROR_NOT_VASSAL("error.notVassal"),
    ERROR_NOT_WILDERNESS("error.notWilderness"),
    ERROR_NOT_IN_WAR("error.notInWar"),
    ERROR_OWN_FACTION("error.ownFaction"),
    ERROR_PERSONAL_UNION_WITH_FACTION("error.personalUnionWithFaction"),
    ERROR_PERSONAL_UNION_WITH_FACTION_REQUIRED("error.personalUnionWithFaction"),
    ERROR_PLAYER_NOT_IN_ANY_FACTION("error.notInAnyFaction"),
    ERROR_PLAYER_NOT_IN_FACTION("error.playerNotInFaction"),
    ERROR_PLAYER_NOT_ONLINE("error.playerNotOnline"),
    ERROR_REGION_IS_ALREADY_CLAIMED("error.regionIsAlreadyClaimed"),
    ERROR_REGION_IS_CORE("error.regionIsCore"),
    ERROR_SELECT_IDEA_GROUP("error.selectIdeaGroup"),
    ERROR_SPECIFY_PLAYER("error.specifyFaction"),
    ERROR_SPECIFY_FACTION("error.specifyFaction"),
    ERROR_VASSAL("error.vassal"),
    ERROR_VASSAL_IS_MOTHER_ADMIN("error.vassalIsMotherAdmin"),
    FACTION_INTEGRATED_VASSAL("faction.integratedVassal"),
    FACTION_INVITE("faction.invite"),
    FACTION_JOIN_ACCEPT("faction.join.accept"),
    FACTION_JOIN_DENY("faction.join.deny"),
    FACTION_LEFT("faction.left"),
    FACTION_LOST_CLAIM("faction.lostClaim"),
    FACTION_LOST_CORE("faction.lostCore"),
    FACTION_NEW_CLAIM("faction.newClaim"),
    FACTION_NEW_CORE("faction.newCore"),
    FACTION_PAID("faction.paid"),
    FACTION_PLAYER_KICKED("faction.playerKicked"),
    FACTION_PLAYER_KICKED_AUTO("faction.playerKickedAuto"),
    FACTION_PERSONAL_UNION_FORMED("faction.personalUnionFormed"),
    FACTION_STORAGE("faction.storage"),
    FACTION_SELECT("faction.select"),
    GOVERNMENT_TYPE_MONARCHY("governmentType.monarchy"),
    GOVERNMENT_TYPE_REPUBLIC("governmentType.republic"),
    GOVERNMENT_TYPE_THEOCRACY("governmentType.theocracy"),
    HELP_ADD_CASUS_BELLI("help.addCasusBelli"),
    HELP_ADMIN("help.admin"),
    HELP_ALLY("help.ally"),
    HELP_CASUS_BELLI("help.casusBelli"),
    HELP_CHAT("help.chat"),
    HELP_CLAIM("help.claim"),
    HELP_CORE("help.core"),
    HELP_CONFIRM_WAR("help.confirmWar"),
    HELP_CREATE("help.create"),
    HELP_CREATE_BULL("help.createBull"),
    HELP_CREATE_VASSAL("help.createVassal"),
    HELP_DESC("help.desc"),
    HELP_DISBAND("help.disband"),
    HELP_GIVE_REGION("help.giveRegion"),
    HELP_HELP("help.help"),
    HELP_HOME("help.home"),
    HELP_IDEA("help.idea"),
    HELP_INTEGRATE("help.integrate"),
    HELP_INVITE("help.invite"),
    HELP_JOIN("help.join"),
    HELP_KICK("help.kick"),
    HELP_LEAVE("help.leave"),
    HELP_LIST("help.list"),
    HELP_LONG_TAG("help.longTag"),
    HELP_MAIN("help.main"),
    HELP_MOB("help.mob"),
    HELP_MOD("help.mod"),
    HELP_MONEY("help.money"),
    HELP_NEUTRAL("help.neutral"),
    HELP_OATH("help.oath"),
    HELP_OPEN("help.open"),
    HELP_PAYDAY("help.payday"),
    HELP_PLAYER_HOME("help.playerHome"),
    HELP_PEACE("help.peace"),
    HELP_POWER("help.power"),
    HELP_REGION("help.region"),
    HELP_REGIONS("help.regions"),
    HELP_RELATION("help.relation"),
    HELP_RELOAD("help.reload"),
    HELP_REQUESTS("help.requests"),
    HELP_ROLEPLAY("help.roleplay"),
    HELP_SCOREBOARD("help.scoreboard"),
    HELP_SET_ANTHEM("help.setAnthem"),
    HELP_SET_BANNER("help.setBanner"),
    HELP_SET_CAPITAL("help.setCapital"),
    HELP_SET_COLOR("help.setColor"),
    HELP_SET_GOVERNMENT("help.setGovernment"),
    HELP_SET_HOME("help.setHome"),
    HELP_SET_PLAYER_HOME("help.setPlayerHome"),
    HELP_SET_POWER("help.setPower"),
    HELP_SHORT_TAG("help.shortTag"),
    HELP_SHOW("help.show"),
    HELP_STORAGE("help.storage"),
    HELP_TAG("help.tag"),
    HELP_TITLE("help.title"),
    HELP_TRADE_OFFER("help.tradeOffer"),
    HELP_UNCLAIM("help.unclaim"),
    HELP_UNINVITE("help.uninvite"),
    HELP_UNITE("help.unite"),
    HELP_VASSALIZE("help.vassalize"),
    HELP_WAR("help.war"),
    HELP_WAR_STATUS("help.warStatus"),
    HELP_WORLD("help.world"),
    IDEA_GROUP_CENTRALIZATION("idea.group.centralization"),
    IDEA_GROUP_DIPLOMACY("idea.group.diplomacy"),
    IDEA_GROUP_ECONOMY("idea.group.economy"),
    IDEA_GROUP_MERCENARY("idea.group.mercenary"),
    IDEA_GROUP_RELIGION("idea.group.religion"),
    IDEA_GROUP_SETTLER("idea.group.settler"),
    IDEA_GROUP_TRADE("idea.group.trade"),
    IDEA_DESC_SETTLER_COLONIZATION("idea.desc.settler.colonization"),
    IDEA_MENU_GROUPS_TITLE("idea.menu.groups.title"),
    IDEA_MENU_GROUPS_DESELECTED("idea.menu.groups.deselected"),
    IDEA_MENU_GROUPS_SELECTED("idea.menu.groups.selected"),
    IDEA_MENU_IDEAS_TITLE("idea.menu.ideas.title"),
    IDEA_NAME_SETTLER_COLONIZATION("idea.name.settler.colonization"),
    LOG_DYNMAP_NOT_ENABLED("log.dynmapNotEnabled"),
    LOG_NEW_FACTION_DATA("log.newFactionData"),
    LOG_NEW_PLAYER_DATA("log.newPlayerData"),
    MISC_ACCEPT("misc.accept"),
    MISC_CANCEL("misc.cancel"),
    MISC_CONTINUE("misc.continue"),
    MISC_DENY("misc.deny"),
    MISC_NONE("misc.none"),
    MISC_LONER("misc.loner"),
    MISC_OPEN_REQUESTS("misc.openRequests"),
    MISC_PURCHASE_FAIL("misc.purchase.success"),
    MISC_PURCHASE_SUCCESS("misc.purchase.success"),
    MISC_SHIFT_CLICK_PURCHASE("misc.shiftClick.purchase"),
    MISC_SHIFT_CLICK_SELECT("misc.shiftClick.select"),
    MISC_WILDERNESS("misc.wilderness"),
    MOB_TRADER("mob.trader"),
    MOB_VILLAGER("mob.villager"),
    POPULATION_ADJUST_CONSUME("population.adjustConsume"),
    POPULATION_DEMANDS_BUTTON("population.demands.button"),
    POPULATION_DEMANDS_TITLE("population.demands.title"),
    POPULATION_DENY_RESOURCE("population.denyResource"),
    POPULATION_GRANT_RESOURCE("population.grantResource"),
    POPULATION_GRANTING1("population.granting1"),
    POPULATION_GRANTING2("population.granting2"),
    POPULATION_MILITARY_BUTTON("population.military.button"),
    POPULATION_MILITARY_SOLDIERS("population.military.soldiers"),
    POPULATION_MILITARY_TITLE("population.military.title"),
    POPULATION_REQUIRED("population.required"),
    POPULATION_TITLE("population.title"),
    POPULATION_WARNING_NOT_ENOUGH_RESOURCES_GRANTED("population.warning.notEnoughResourcesGranted"),
    POPULATION_WARNING_TOO_MANY_RESOURCES_GRANTED("population.warning.tooManyResourcesGranted"),
    PROTECTION_CANNOT_ATTACK_CAPITAL("protection.cannotAttack.capital"),
    PROTECTION_CANNOT_ATTACK_FACTION("protection.cannotAttack.faction"),
    PROTECTION_CANNOT_ATTACK_PLAYER("protection.cannotAttack.player"),
    PROTECTION_CANNOT_BUILD_FACTION("protection.cannotBuildFaction"),
    PROTECTION_CANNOT_BUILD_WILDERNESS("protection.cannotBuildWilderness"),
    PROTECTION_CANNOT_DESTROY_FACTION("protection.cannotDestroyFaction"),
    PROTECTION_CANNOT_DESTROY_WILDERNESS("protection.cannotDestroyWilderness"),
    PROTECTION_CANNOT_EQUIP_FACTION("protection.cannotEquipFaction"),
    PROTECTION_CANNOT_INTERACT_FACTION("protection.cannotInteractFaction"),
    PROTECTION_CANNOT_LEASH_FACTION("protection.cannotLeashFaction"),
    PROTECTION_CANNOT_SHEAR_FACTION("protection.cannotShearFaction"),
    PROTECTION_CANNOT_SPLASH_POTION_FACTION("protection.cannotSplashPotionFaction"),
    PROTECTION_CANNOT_TAME_FACTION("protection.cannotTameFaction"),
    PROTECTION_CANNOT_UNLEASH_FACTION("protection.cannotLeashFaction"),
    PROTECTION_CANNOT_REGISTER_FACTION("protection.cannotRegisterFaction"),
    PROTECTION_DAMAGE_REDUCED("protection.damageReduced"),
    REGION_BARREN("region.barren"),
    REGION_CITY("region.city"),
    REGION_DESERT("region.desert"),
    REGION_FARMLAND("region.farmland"),
    REGION_FOREST("region.forest"),
    REGION_MAGIC("region.magic"),
    REGION_MOUNTAINOUS("region.mountainous"),
    REGION_SEA("region.sea"),
    REGION_WAR_ZONE("region.warZone"),
    RELATION_ALLIANCE("relation.alliance"),
    RELATION_ALLIANCE_DESC("relation.allianceDesc"),
    RELATION_COALITION("relation.coalition"),
    RELATION_COALITION_DESC("relation.coalitionDesc"),
    RELATION_CONFIRMED("relation.confirmed"),
    RELATION_DENIED("relation.denied"),
    RELATION_ENEMY("relation.enemy"),
    RELATION_ENEMY_DESC("relation.enemyDesc"),
    RELATION_LORD("relation.lord"),
    RELATION_LORD_DESC("relation.lordDesc"),
    RELATION_OWN("relation.own"),
    RELATION_OWN_DESC("relation.ownDesc"),
    RELATION_PEACE("relation.peace"),
    RELATION_PEACE_DESC("relation.peaceDesc"),
    RELATION_PERSONAL_UNION("relation.personalUnion"),
    RELATION_PERSONAL_UNION_DESC("relation.personalUnionDesc"),
    RELATION_REAL_UNION("relation.realUnion"),
    RELATION_REAL_UNION_DESC("relation.realUnionDesc"),
    RELATION_REQUEST_BUTTON("relation.requestButton"),
    RELATION_REQUEST_VASSAL("relatio"),
    RELATION_UNITED("relation.united"),
    RELATION_VASSAL("relation.vassal"),
    RELATION_VASSAL_DESC("relation.vassalDesc"),
    RELATION_VASSALIZED("relation.vassalized"),
    RELATION_WISH("relation.wish"),
    RELATION_WISH_OWN("relation.wishOwn"),
    RESOURCE_COAL("resource.coal"),
    RESOURCE_SULPHUR("resource.sulphur"),
    RESOURCE_GOLD("resource.gold"),
    RESOURCE_IRON("resource.iron"),
    RESOURCE_DIAMOND("resource.diamond"),
    RESOURCE_EMERALD("resource.emerald"),
    RESOURCE_LAPIS_LAZULI("resource.lapisLazuli"),
    RESOURCE_QUARTZ("resource.quartz"),
    RESOURCE_REDSTONE("resource.redstone"),
    RESOURCE_ANDESITE("resource.andesite"),
    RESOURCE_DIORITE("resource.diorite"),
    RESOURCE_GRANITE("resource.granite"),
    RESOURCE_GRAVEL("resource.gravel"),
    RESOURCE_OBSIDIAN("resource.obsidian"),
    RESOURCE_STONE("resource.stone"),
    RESOURCE_CHICKEN("resource.chicken"),
    RESOURCE_COW("resource.cow"),
    RESOURCE_HORSE("resource.horse"),
    RESOURCE_PIG("resource.pig"),
    RESOURCE_RABBIT("resource.rabbit"),
    RESOURCE_SHEEP("resource.sheep"),
    RESOURCE_APPLE("resource.apple"),
    RESOURCE_BEETROOT("resource.beetroot"),
    RESOURCE_CARROT("resource.carrot"),
    RESOURCE_CHORUS("resource.chorus"),
    RESOURCE_COCOA("resource.cocoa"),
    RESOURCE_MELON("resource.melon"),
    RESOURCE_POTATO("resource.potato"),
    RESOURCE_PUMPKIN("resource.pumpkin"),
    RESOURCE_SUGAR("resource.sugar"),
    RESOURCE_WHEAT("resource.wheat"),
    RESOURCE_ACACIA("resource.acacia"),
    RESOURCE_BIRCH("resource.birch"),
    RESOURCE_DARK_OAK("resource.darkOak"),
    RESOURCE_JUNGLE("resource.jungle"),
    RESOURCE_OAK("resource.oak"),
    RESOURCE_SPRUCE("resource.spruce"),
    RESOURCE_PAPER("resource.paper"),
    RESOURCE_MUSHROOMS("resource.mushrooms"),
    RESOURCE_CODFISH("resource.codfish"),
    RESOURCE_CLOWNFISH("resource.clownfish"),
    RESOURCE_PUFFERFISH("resource.pufferfish"),
    RESOURCE_SALMON("resource.salmon"),
    RESOURCE_INK("resource.ink"),
    RESOURCE_SALT("resource.salt"),
    RESOURCE_WATER("resource.water"),
    RESOURCE_CLAY("resource.clay"),
    RESOURCE_PRISMARINE("resource.prismarine"),
    RESOURCE_DRAGON_BREATH("resource.dragonBreath"),
    RESOURCE_EXPERIENCE("resource.experience"),
    RESOURCE_NETHER_WART("resource.netherWart"),
    RESOURCE_PURPUR("resource.purpur"),
    RESOURCE_CACTUS("resource.cactus"),
    RESOURCE_GLASS("resource.glass"),
    RESOURCE_RED_SANDSTONE("resource.redSandstone"),
    RESOURCE_YELLOW_SANDSTONE("resource.yellowSandstone"),
    RESOURCE_CRAFT("resource.craft"),
    RESOURCE_MANPOWER("resource.manpower"),
    RESOURCE_TAXES("resource.taxes"),
    RESOURCE_SUBCATEGORY_ALCHEMY("resource.subcategory.alchemy"),
    RESOURCE_SUBCATEGORY_CANNONING("resource.subcategory.cannoning"),
    RESOURCE_SUBCATEGORY_DYE("resource.subcategory.dye"),
    RESOURCE_SUBCATEGORY_FUR("resource.subcategory.fur"),
    RESOURCE_SUBCATEGORY_GLASS("resource.subcategory.glass"),
    RESOURCE_SUBCATEGORY_HEATING("resource.subcategory.heating"),
    RESOURCE_SUBCATEGORY_JEWELRY("resource.subcategory.jewelry"),
    RESOURCE_SUBCATEGORY_LEATHER("resource.subcategory.leather"),
    RESOURCE_SUBCATEGORY_LITERATURE("resource.subcategory.literature"),
    RESOURCE_SUBCATEGORY_MEAT("resource.subcategory.meat"),
    RESOURCE_SUBCATEGORY_RIDERS("resource.subcategory.riders"),
    RESOURCE_SUBCATEGORY_ROADS("resource.subcategory.roads"),
    RESOURCE_SUBCATEGORY_SMITHERY("resource.subcategory.smithery"),
    RESOURCE_SUBCATEGORY_SPICES("resource.subcategory.spices"),
    RESOURCE_SUBCATEGORY_STAPLES("resource.subcategory.staples"),
    RESOURCE_SUBCATEGORY_STONE("resource.subcategory.stone"),
    RESOURCE_SUBCATEGORY_WATER("resource.subcategory.water"),
    RESOURCE_SUBCATEGORY_WOOD("resource.subcategory.wood"),
    STABILITY_LOW("error.stability.low"),
    STABILITY_HIGH("error.stability.high"),
    SATURATION_LEVEL_FULLY_SATURATED("saturationLevel.fullySaturated"),
    SATURATION_LEVEL_MOSTLY_SATURATED("saturationLevel.mostlySaturated"),
    SATURATION_LEVEL_NOT_SATURATED("saturationLevel.notSaturated"),
    SATURATION_LEVEL_PARTIALLY_SATURATED("saturationLevel.partiallySaturated"),
    SATURATION_LEVEL_POOR("saturationLevel.poor"),
    SATURATION_LEVEL_SATURATED("saturationLevel.saturated"),
    SATURATION_LEVEL_SURPLUS("saturationLevel.surplus"),
    STORAGE_NON_PHYSICAL_MANPOWER("storage.nonPhysical.manpower"),
    STORAGE_NON_PHYSICAL_TAXES("storage.nonPhysical.taxes"),
    STORAGE_PAYDAY("storage.payday"),
    STORAGE_NON_PHYSICAL_WARNING("storage.nonPhysical.warning"),
    STORAGE_STOCK("storage.stock"),
    STORAGE_TITLE("storage.title"),
    TRADE_CONSUME("trade.consume"),
    TRADE_ECONOMY("trade.economy"),
    TRADE_EXPORT("trade.export"),
    TRADE_IMPORT("trade.import"),
    TRADE_INCOME_MANAGEMENT("trade.incomeManagement"),
    TRADE_PRICE("trade.price"),
    TRADE_OFFER_CHOOSE_EXPORT("trade.offer.chooseExport"),
    TRADE_OFFER_CHOOSE_PARTNER("trade.offer.choosePartner"),
    TRADE_OFFER_AMOUNT("trade.offer.amount"),
    TRADE_OFFER_CHOOSE_RESOURCE("trade.offer.chooseResource"),
    TRADE_OFFER_SEND_TRADE("trade.offer.send.trade"),
    TRADE_OFFER_SEND_EXPORT("trade.offer.send.export"),
    TRADE_OFFER_SEND_IMPORT("trade.offer.send.import"),
    TRADE_OFFER_TITLE("trade.offer.title"),
    TRADE_RESOURCE_TITLE("trade.resourceTitle"),
    TRADE_SUCCESS_EXPORT("trade.success.export"),
    TRADE_SUCCESS_IMPORT("trade.success.import"),
    TRADE_TITLE("trade.title"),
    WAR_CALL_TO_ARMS_ALLY_1("war.callToArms.ally.1"),
    WAR_CALL_TO_ARMS_ALLY_2_ALLIED("war.callToArms.ally.2.allied"),
    WAR_CALL_TO_ARMS_ALLY_3_ALLIED("war.callToArms.ally.3.allied"),
    WAR_CALL_TO_ARMS_ALLY_2_NOT_ALLIED("war.callToArms.ally.2.notAllied"),
    WAR_CALL_TO_ARMS_ALLY_3_NOT_ALLIED("war.callToArms.ally.3.notAllied"),
    WAR_CALL_TO_ARMS_COALITION_1("war.callToArms.coalition.1"),
    WAR_CALL_TO_ARMS_COALITION_2("war.callToArms.coalition.2"),
    WAR_CALL_TO_ARMS_OWN("war.callToArms.own"),
    WAR_CALL_TO_ARMS_VASSAL_1("war.callToArms.vassal.1"),
    WAR_CALL_TO_ARMS_VASSAL_2_STRONGER("war.callToArms.vassal.2.stronger"),
    WAR_CALL_TO_ARMS_VASSAL_2_WEAKER("war.callToArms.vassal.2.weaker"),
    WAR_CALL_TO_ARMS_DEFENDER("war.callToArms.defender"),
    WAR_CALL_TO_ARMS_ADD("war.callToArms.add"),
    WAR_CALL_TO_ARMS_ADDED_FACTION("war.callToArms.addedFaction"),
    WAR_CALL_TO_ARMS_REMOVED_FACTION("war.callToArms.removedFaction"),
    WAR_CALL_TO_ARMS_REMOVE("war.callToArms.remove"),
    WAR_CALL_TO_ARMS_TITLE("war.callToArms.title"),
    WAR_CB_MENU("war.cb.menu"),
    WAR_CB_RAID("war.cb.raid"),
    WAR_CB_RAID_DESC("war.cb.raidDesc"),
    WAR_CB_BORDER("war.cb.border"),
    WAR_CB_BORDER_DESC("war.cb.borderDesc"),
    WAR_CB_CONQUEST("war.cb.conquest"),
    WAR_CB_CONQUEST_DESC("war.cb.conquest.desc"),
    WAR_CB_INDEPENDENCE("war.cb.liberation"), // Independence and liberation are switched...
    WAR_CB_INDEPENDENCE_DESC("war.cb.liberationDesc"),
    WAR_CB_RESUBJAGATION("war.cb.resubjagation"),
    WAR_CB_RESUBJAGATION_DESC("war.cb.resubjagationDesc"),
    WAR_CB_RECONQUEST("war.cb.reconquest"),
    WAR_CB_RECONQUEST_DESC("war.cb.reconquestDesc"),
    WAR_CB_INVALID("war.cb.invalid"),
    WAR_DECLARATION_ALLIES("war.declaration.allies"),
    WAR_DECLARATION_BROADCAST("war.declaration.broadcast"),
    WAR_DECLARATION_CANCELLED("war.declaration.cancelled"),
    WAR_DECLARATION_CASUS_BELLI("war.declaration.casusBelli"),
    WAR_DECLARATION_DEFENDERS("war.declaration.defenders"),
    WAR_DECLARATION_START_DATE("war.declaration.startDate"),
    WAR_DECLARATION_TITLE("war.declaration.title"),
    WAR_DEMAND_MENU_ITEM("war.demand.menu.item"),
    WAR_DEMAND_CREATION_MENU_TITLE_DEMAND("war.demand.creation.title.demand"),
    WAR_DEMAND_CREATION_MENU_TITLE_OFFER("war.demand.creation.title.offer"),
    WAR_DEMAND_CREATION_MENU_LIST("war.demand.creation.list"),
    WAR_DEMAND_CREATION_MENU_SEND("war.demand.creation.send"),
    WAR_DEMAND_CREATION_MENU_ITEM("war.demand.creation.item"),
    WAR_DEMAND_CREATION_MENU_MONEY("war.demand.creation.money"),
    WAR_DEMAND_CREATION_MENU_CLEARED("war.demand.creation.cleared"),
    WAR_DEMAND_CREATION_MENU_CLEAR_BUTTON("war.demand.creation.clearButton"),
    WAR_DEMAND_CREATION_MENU_REGION_BUTTON("war.demand.creation.regionButton"),
    WAR_DEMAND_CREATION_MENU_MAKE_DEMANDS("war.demand.creation.makeDemands"),
    WAR_DEMAND_CREATION_MENU_MAKE_OFFER("war.demand.creation.makeOffer"),
    WAR_DEMAND_REGION_TITLE("war.demand.region.title"),
    WAR_DEMAND_REGION_WARSCORE("war.demand.region.warScore"),
    WAR_DEMAND_REGION_ADDED("war.demand.region.added"),
    WAR_DEMAND_REGION_REMOVED("war.demand.region.removed"),
    WAR_DEMAND_REGION_CB_ADDED("war.demand.region.CBAdded"),
    WAR_DEMAND_REGION_REGIONS_CHAT("war.demand.region.regionsChat"),
    WAR_DEMAND_REGION_TOTAL_WARSCORE_CHAT("war.demand.region.totalWarScoreChat"),
    WAR_DEMAND_REGION_SUCCESS("war.demand.region.done"),
    WAR_DEMAND_REGION_DONE("war.demand.region.success"),
    WAR_DEMAND_MONEY_ADDED("war.demand.money.added"),
    WAR_DEMAND_MONEY_CHAT("war.demand.money.chat"),
    WAR_DEMAND_DISABLED("war.demand.disabled"),
    WAR_DEMAND_LIST("war.demand.list"),
    WAR_DEMAND_NO_WARSCORE("war.demand.noWarscore"),
    WAR_DEMAND_WARSCORE_NEEDED("war.demand.warscoreNeeded"),
    WAR_DEMAND_CANT_AFFORD("war.demand.cantAfford"),
    WAR_DEMAND_REQUEST_ALREADY_SENT("war.demand.requestAlreadySent"),
    WAR_DEMAND_CHAT_TITLE("war.demand.chat.title"),
    WAR_DEMAND_CHAT_TOTAL_WARSCORE("war.demand.chat.totalWarscore"),
    WAR_DEMAND_CHAT_SENT("war.demand.chat.sent"),
    WAR_DEMAND_CHAT_EXPLANATION_1("war.demand.chat.explanation1"),
    WAR_DEMAND_CHAT_EXPLANATION_1_SEPARATE("war.demand.chat.explanation1Separate"),
    WAR_DEMAND_CHAT_EXPLANATION_2("war.demand.chat.explanation2"),
    WAR_DEMAND_CHAT_EXPLANATION_2_SEPARATE("war.demand.chat.explanation2Separate"),
    WAR_DEMAND_CHAT_DEMANDS("war.demand.chat.demands"),
    WAR_GUI_BUTTON("war.demand.guiButton"),
    WAR_OFFER_CHAT_TITLE("war.offer.chat.title"),
    WAR_OFFER_CHAT_EXPLANATION_1("war.offer.chat.explanation1"),
    WAR_OFFER_CHAT_EXPLANATION_1_SEPARATE("war.offer.chat.explanation1Separate"),
    WAR_OFFER_CHAT_EXPLANATION_2("war.offer.chat.explanation2"),
    WAR_OFFER_CHAT_EXPLANATION_2_SEPARATE("war.offer.chat.explanation2Separate"),
    WAR_DEMAND_MENU_LIST("war.demand.menu.list"),
    WAR_DEMAND_MENU_TITLE("war.demand.menu.title"),
    WAR_REQUEST_NO_ALLIANCE("war.request.alliance"),
    WAR_REQUEST_WAR_JOINED("war.request.joined"),
    WAR_REQUEST_TITLE("war.request.title"),
    WAR_REQUEST_EMPTY("war.request.empty"),
    WAR_REQUEST_INVITE("war.request.invite"),
    WAR_REQUEST_RECEIVED("war.request.received"),
    WAR_ALLY_ABANDONED("war.ally.abandoned"),
    WAR_ALLY_LEFT_WAR("war.ally.leftWar"),
    WAR_SCORE_CHANGED("war.score.changed"),
    WAR_TRUCE_ENDED("war.truce.ended"),
    WAR_ENDED("war.ended"),
    WAR_OCCUPY_REGION_DEFEND("war.occupy.region.defended"),
    WAR_OCCUPY_REGION_ATTACKED("war.occupy.region.attacked"),
    WAR_OCCUPY_SUCCESS("war.occupy.success"),
    WAR_OCCUPY_INFLUENCE_TOO_HIGH("war.occupy.influenceTooHigh"),
    WAR_OCCUPY_NOT_ENEMY("war.occupy.notEnemy"),
    WAR_OCCUPY_ALREADY_OCCUPIED("war.occupy.alreadyOccupied"),
    WAR_OCCUPY_NOT_AT_WAR("war.occupy.notAtWar"),
    WAR_OCCUPY_HELP("war.occupy.help"),
    WAR_OCCUPY_RAID("war.occupy.raid"),
    WAR_OCCUPY_TRUCE("war.occupy.truce");

    private String path;

    FMessage(String path) {
        this.path = path;
    }

    @Override
    public MessageHandler getMessageHandler() {
        return FactionsXL.getInstance().getMessageHandler();
    }

    @Override
    public String getPath() {
        return path;
    }

}
