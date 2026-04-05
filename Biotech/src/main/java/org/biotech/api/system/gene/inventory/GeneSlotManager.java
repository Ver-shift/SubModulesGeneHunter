package org.biotech.api.system.gene.inventory;

import net.minecraft.world.entity.player.Player;
import org.biotech.api.system.gene.core.manager.IGeneSlotManager;

public class GeneSlotManager implements IGeneSlotManager {

    private Player player;
    public GeneSlotManager(Player player) {
        this.player = player;
    }


    @Override
    public boolean addInventoryPage(int pageCount) {
        return false;
    }

    @Override
    public boolean removeInventoryPage(int pageCount) {
        return false;
    }

    @Override
    public boolean addFavoritePage(int pageCount) {
        return false;
    }

    @Override
    public boolean removeFavoritePage(int pageCount) {
        return false;
    }

    @Override
    public boolean addEquippedSlot(int slotCount) {
        return false;
    }

    @Override
    public boolean removeEquippedSlot(int slotCount) {
        return false;
    }

    @Override
    public boolean addMergeSlot(int slotCount) {
        return false;
    }

    @Override
    public boolean removeMergeSlot(int slotCount) {
        return false;
    }
}
