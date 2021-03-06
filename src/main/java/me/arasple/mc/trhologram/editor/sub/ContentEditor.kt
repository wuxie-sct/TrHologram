package me.arasple.mc.trhologram.editor.sub

import io.izzel.taboolib.Version
import io.izzel.taboolib.module.inject.TListener
import io.izzel.taboolib.module.locale.TLocale
import io.izzel.taboolib.util.Strings
import io.izzel.taboolib.util.book.builder.BookBuilder
import io.izzel.taboolib.util.item.ItemBuilder
import io.izzel.taboolib.util.lite.Materials
import io.izzel.taboolib.util.lite.Sounds
import me.arasple.mc.trhologram.TrHologram
import me.arasple.mc.trhologram.api.TrHologramAPI
import me.arasple.mc.trhologram.editor.EditorMenu
import me.arasple.mc.trhologram.hologram.Hologram
import me.arasple.mc.trhologram.hologram.HologramManager
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerEditBookEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.meta.BookMeta

/**
 * @author Arasple
 * @date 2020/2/15 12:54
 */
object ContentEditor {

    @Suppress("DEPRECATION")
    fun openEditor(hologram: Hologram, player: Player) {
        player.closeInventory()

        val book = ItemBuilder(BookBuilder(Materials.WRITABLE_BOOK.parseItem()).title("TrHologramEditor_" + hologram.id).author("TrHologramEditor_" + hologram.id).pagesRaw(hologram.getRawContents()).build()).name(Strings.replaceWithOrder(TrHologram.SETTINGS.getStringColored("GUIS.EDITOR.BOOK.name"), hologram.id)).lore(TrHologram.SETTINGS.getStringListColored("GUIS.EDITOR.BOOK.lore")).build()
        player.inventory.addItem(book)
        Sounds.ENTITY_ITEM_PICKUP.playSound(player)
        TLocale.sendTo(player, "COMMANDS.EDIT.BOOK-EDIT")
    }

    @TListener
    private class EventListener : Listener {

        @EventHandler
        fun onInteract(e: PlayerInteractEvent) {
            val item = e.item
            try {
                if (item == null || Version.isAfter(Version.v1_9)) {
                    if (e.hand == EquipmentSlot.OFF_HAND) {
                        return
                    }
                }
            } catch (ignored: Throwable) {
            }

            if (e.action == Action.LEFT_CLICK_BLOCK && item != null && item.itemMeta is BookMeta) {
                val author = (item.itemMeta as BookMeta).author
                val title = (item.itemMeta as BookMeta).title
                if (e.player.hasPermission("trhologram.admin") && author != null && title != null && author.startsWith("TrHologramEditor_") && title.startsWith("TrHologramEditor_") && author.substring(17) == title.substring(17)) {
                    val hologram = TrHologramAPI.getHologramById(author.substring(17))
                    if (hologram != null) {
                        EditorMenu.openEditor(hologram, e.player)
                    }
                }
                e.isCancelled = true
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        fun onEdit(e: PlayerEditBookEvent) {
            val author = e.newBookMeta.author
            val title = e.newBookMeta.title
            if (e.player.hasPermission("trhologram.admin") && author != null && title != null && author.startsWith("TrHologramEditor_") && title.startsWith("TrHologramEditor_") && author.substring(17) == title.substring(17)) {
                val hologram = TrHologramAPI.getHologramById(author.substring(17))
                if (hologram != null) {
                    e.isCancelled = true
                    hologram.updateLines(e.newBookMeta.pages)
                    HologramManager.write(hologram, false)
                    Sounds.ITEM_BOTTLE_FILL.playSound(e.player)
                    print("Should be updated?")
                    TLocale.sendTo(e.player, "COMMANDS.EDIT.BOOK-EDIT-SUCCESS")
                }
            }
        }

    }

}