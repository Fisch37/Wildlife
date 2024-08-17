package de.fisch37.wildlife.mixin;

import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnimalEntity.class)
public abstract class AnimalEntityMixin {
    @Unique
    private static final Vec3d SEARCH_RANGE = new Vec3d(10, 10, 10);
    @Unique
    private static final int MAX_ADULTS = 8;
    @Unique
    private static final short SEARCH_TIMER = 6_000; // 5 min


    @Unique
    private short searchCooldown = SEARCH_TIMER;


    @Unique
    private AnimalEntity self() {
        return (AnimalEntity)((Object)this);
    }

    @Unique
    private void setLove() {
        final AnimalEntity obj = ((AnimalEntity)((Object)this));
        obj.lovePlayer(null);
        obj.setLoveTicks(600);
    }

    @Unique
    private Box getSearchBox() {
        final AnimalEntity obj = ((AnimalEntity)((Object)this));
        return new Box(obj.getPos().subtract(SEARCH_RANGE), obj.getPos().add(SEARCH_RANGE));
    }

    @Inject(method = "mobTick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (!self().isBaby()) {
            if (searchCooldown <= 0) {
                searchAutobreed();
                searchCooldown = SEARCH_TIMER;
            } else {
                searchCooldown--;
            }
        }
    }

    @Unique
    private void searchAutobreed() {
        final AnimalEntity obj = ((AnimalEntity)((Object)this));
        int adultCount = obj.getWorld()
                .getEntitiesByClass(obj.getClass(), getSearchBox(), o -> !o.isBaby())
                .size();
        if (adultCount < MAX_ADULTS) {
            setLove();
        }
    }


    @Unique
    private static final String SEARCH_COOLDOWN_KEY = "repopulation:autobreedCooldown";

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeModdedDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putShort(SEARCH_COOLDOWN_KEY, searchCooldown);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readModdedDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        searchCooldown = nbt.getShort(SEARCH_COOLDOWN_KEY);
    }
}
