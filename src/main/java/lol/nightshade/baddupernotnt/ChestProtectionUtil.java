package lol.nightshade.baddupernotnt;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.type.Chest;
import org.bukkit.inventory.InventoryHolder;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChestProtectionUtil {

    private static final ConcurrentHashMap<Block, UUID> openContainers = new ConcurrentHashMap<>();
    private static BadDuperNoTNT plugin;

    private static final Set<Material> PROTECTED_TYPES = EnumSet.of(
            Material.CHEST, Material.TRAPPED_CHEST, Material.SHULKER_BOX, Material.ENDER_CHEST,
            Material.HOPPER, Material.BREWING_STAND, Material.DISPENSER, Material.DROPPER,
            Material.FURNACE, Material.SMOKER, Material.BLAST_FURNACE, Material.BARREL
    );

    public static void initialize(BadDuperNoTNT pl) {
        plugin = pl;
    }

    public static void addOpenContainer(UUID playerUUID, InventoryHolder holder) {
        if (holder == null || playerUUID == null) {
            return;
        }

        Block block = getBlockFromHolder(holder);
        if (block != null) {
            openContainers.put(block, playerUUID);
            Block otherHalf = getOtherDoubleChestHalf(block);
            if (otherHalf != null) {
                openContainers.put(otherHalf, playerUUID);
            }
        }
    }

    public static void removeOpenContainer(UUID playerUUID, InventoryHolder holder) {
        if (holder == null || playerUUID == null) {
            return;
        }

        Block block = getBlockFromHolder(holder);
        if (block != null) {
            openContainers.remove(block);
            Block otherHalf = getOtherDoubleChestHalf(block);
            if (otherHalf != null) {
                openContainers.remove(otherHalf);
            }
        }
    }

    public static boolean isProtectedContainer(Block block) {
        if (block == null || !PROTECTED_TYPES.contains(block.getType())) {
            return false;
        }

        if (openContainers.containsKey(block)) {
            return true;
        }

        Block otherHalf = getOtherDoubleChestHalf(block);
        return otherHalf != null && openContainers.containsKey(otherHalf);
    }

    private static Block getBlockFromHolder(InventoryHolder holder) {
        if (holder instanceof BlockState) {
            return ((BlockState) holder).getBlock();
        } else if (holder instanceof Container) {
            return ((Container) holder).getBlock();
        }
        return null;
    }

    private static boolean isDoubleChest(Block block) {
        if (block == null || (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST)) {
            return false;
        }

        if (!(block.getBlockData() instanceof Chest chestData)) {
            return false;
        }

        return chestData.getType() != Chest.Type.SINGLE;
    }

    private static Block getOtherDoubleChestHalf(Block block) {
        if (!isDoubleChest(block)) {
            return null;
        }

        Chest chestData = (Chest) block.getBlockData();
        BlockFace chestFace = chestData.getFacing();

        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
            Block relative = block.getRelative(face);
            if (relative.getType() == block.getType() && isDoubleChest(relative)) {
                Chest relativeData = (Chest) relative.getBlockData();
                if (relativeData.getFacing() == chestData.getFacing() && relativeData.getType() != chestData.getType()) {
                    return relative;
                }
            }
        }

        return null;
    }

    public static String getProtectedContainerMessage(String action) {
        return plugin.getConfig().getString("messages." + action, "This container is currently in use and cannot be affected.");
    }

    public static void clearOpenContainers() {
        openContainers.clear();
    }
}