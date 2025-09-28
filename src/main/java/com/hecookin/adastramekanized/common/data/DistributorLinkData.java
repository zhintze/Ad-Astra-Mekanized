package com.hecookin.adastramekanized.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data structure for storing linked oxygen distributor information
 */
public class DistributorLinkData {

    public static class LinkedDistributor {
        private final BlockPos pos;
        private String customName;
        private boolean enabled;

        // Runtime data (not saved)
        private transient int lastEnergy;
        private transient long lastOxygen;
        private transient float lastEfficiency;
        private transient boolean lastOnline;

        public LinkedDistributor(BlockPos pos) {
            this.pos = pos.immutable();
            this.customName = String.format("Distributor @ %d, %d, %d", pos.getX(), pos.getY(), pos.getZ());
            this.enabled = true;
        }

        public LinkedDistributor(BlockPos pos, String customName, boolean enabled) {
            this.pos = pos.immutable();
            this.customName = customName;
            this.enabled = enabled;
        }

        public BlockPos getPos() {
            return pos;
        }

        public String getCustomName() {
            return customName;
        }

        public void setCustomName(String name) {
            this.customName = name;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void updateStatus(int energy, long oxygen, float efficiency, boolean online) {
            this.lastEnergy = energy;
            this.lastOxygen = oxygen;
            this.lastEfficiency = efficiency;
            this.lastOnline = online;
        }

        public int getLastEnergy() {
            return lastEnergy;
        }

        public long getLastOxygen() {
            return lastOxygen;
        }

        public float getLastEfficiency() {
            return lastEfficiency;
        }

        public boolean isOnline() {
            return lastOnline;
        }

        public CompoundTag toNbt() {
            CompoundTag tag = new CompoundTag();
            tag.put("pos", NbtUtils.writeBlockPos(pos));
            tag.putString("name", customName);
            tag.putBoolean("enabled", enabled);
            return tag;
        }

        public static LinkedDistributor fromNbt(CompoundTag tag) {
            BlockPos pos = NbtUtils.readBlockPos(tag, "pos").orElse(BlockPos.ZERO);
            String name = tag.getString("name");
            boolean enabled = tag.getBoolean("enabled");
            return new LinkedDistributor(pos, name, enabled);
        }

        public void toNetwork(FriendlyByteBuf buf) {
            buf.writeBlockPos(pos);
            buf.writeUtf(customName);
            buf.writeBoolean(enabled);
            buf.writeInt(lastEnergy);
            buf.writeLong(lastOxygen);
            buf.writeFloat(lastEfficiency);
            buf.writeBoolean(lastOnline);
        }

        public static LinkedDistributor fromNetwork(FriendlyByteBuf buf) {
            BlockPos pos = buf.readBlockPos();
            String name = buf.readUtf();
            boolean enabled = buf.readBoolean();
            LinkedDistributor dist = new LinkedDistributor(pos, name, enabled);
            dist.lastEnergy = buf.readInt();
            dist.lastOxygen = buf.readLong();
            dist.lastEfficiency = buf.readFloat();
            dist.lastOnline = buf.readBoolean();
            return dist;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LinkedDistributor that = (LinkedDistributor) o;
            return pos.equals(that.pos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos);
        }
    }

    private final List<LinkedDistributor> linkedDistributors = new ArrayList<>();
    private static final int MAX_LINKS = 64;

    public boolean addLink(BlockPos pos) {
        if (linkedDistributors.size() >= MAX_LINKS) {
            return false;
        }

        LinkedDistributor newLink = new LinkedDistributor(pos);
        if (linkedDistributors.contains(newLink)) {
            return false; // Already linked
        }

        linkedDistributors.add(newLink);
        return true;
    }

    public boolean removeLink(BlockPos pos) {
        return linkedDistributors.removeIf(link -> link.getPos().equals(pos));
    }

    public boolean isLinked(BlockPos pos) {
        return linkedDistributors.stream().anyMatch(link -> link.getPos().equals(pos));
    }

    public List<LinkedDistributor> getLinkedDistributors() {
        return new ArrayList<>(linkedDistributors);
    }

    public void clearAll() {
        linkedDistributors.clear();
    }

    public int getLinkCount() {
        return linkedDistributors.size();
    }

    public LinkedDistributor getLink(BlockPos pos) {
        return linkedDistributors.stream()
            .filter(link -> link.getPos().equals(pos))
            .findFirst()
            .orElse(null);
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (LinkedDistributor link : linkedDistributors) {
            list.add(link.toNbt());
        }
        tag.put("links", list);
        return tag;
    }

    public void fromNbt(CompoundTag tag) {
        linkedDistributors.clear();
        ListTag list = tag.getList("links", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size() && i < MAX_LINKS; i++) {
            linkedDistributors.add(LinkedDistributor.fromNbt(list.getCompound(i)));
        }
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeInt(linkedDistributors.size());
        for (LinkedDistributor link : linkedDistributors) {
            link.toNetwork(buf);
        }
    }

    public static DistributorLinkData fromNetwork(FriendlyByteBuf buf) {
        DistributorLinkData data = new DistributorLinkData();
        int count = buf.readInt();
        for (int i = 0; i < count && i < MAX_LINKS; i++) {
            data.linkedDistributors.add(LinkedDistributor.fromNetwork(buf));
        }
        return data;
    }
}